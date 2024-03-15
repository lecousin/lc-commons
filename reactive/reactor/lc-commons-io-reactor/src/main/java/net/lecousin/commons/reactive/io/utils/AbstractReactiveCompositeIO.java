package net.lecousin.commons.reactive.io.utils;

import java.io.EOFException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.IO.Seekable.SeekFrom;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.reactive.io.AbstractReactiveIO;
import net.lecousin.commons.reactive.io.ReactiveIO;
import net.lecousin.commons.reactive.io.ReactiveIOChecks;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Abstract class for a Composite-IO.
 * @param <I> type of IO
 */
// CHECKSTYLE DISABLE: VisibilityModifier
@SuppressWarnings({"java:S1104"})
public abstract class AbstractReactiveCompositeIO<I extends ReactiveIO> extends AbstractReactiveIO implements ReactiveIO.Seekable, ReactiveIO.Readable, ReactiveIO.Writable {

	protected List<? extends I> ios;
	protected final boolean closeIosOnClose;
	protected final Scheduler scheduler;
	protected Cursor posCursor;

	protected AbstractReactiveCompositeIO(List<? extends I> ios, boolean closeIosOnClose) {
		this.ios = new ArrayList<>(ios);
		this.closeIosOnClose = closeIosOnClose;
		this.scheduler = computeScheduler();
		posCursor = new Cursor();
	}
	
	private Scheduler computeScheduler() {
		if (ios == null)
			return Schedulers.parallel();
		if (ios.isEmpty())
			return Schedulers.parallel();
		Iterator<? extends I> it = ios.iterator();
		Scheduler s = it.next().getScheduler();
		while (it.hasNext()) {
			if (it.next().getScheduler() != s) {
				s = Schedulers.boundedElastic();
				break;
			}
		}
		return s;
	}
	
	// Cursor
	
	protected final class Cursor {
		private int index;
		public I io;
		private long posInIO;
		private long posGlobal;
		
		public Mono<Boolean> goNext() {
			if (index >= ios.size() - 1) {
				io = null;
				return Mono.just(false);
			}
			io = ios.get(++index);
			posInIO = 0;
			if (io instanceof ReactiveIO.Seekable s)
				return s.seek(SeekFrom.START, 0).thenReturn(true);
			return Mono.just(true);
		}
		
		public Mono<Boolean> goPrevious() {
			if (index == 0) {
				return Mono.just(false);
			}
			io = ios.get(--index);
			this.posGlobal -= this.posInIO;
			return ((ReactiveIO.Seekable) io).seek(SeekFrom.END, 0)
				.map(p -> {
					this.posInIO = p;
					return true;
				});
		}
		
		public void moved(long move) {
			posInIO += move;
			posGlobal += move;
		}
	}
	
	protected Mono<Void> resetCursor(Cursor cursor) {
		cursor.index = -1;
		cursor.posInIO = 0;
		cursor.posGlobal = 0;
		return cursor.goNext().then();
	}
	
	protected Mono<Long> moveCursorAt(Cursor cursor, long pos) {
		if (pos < 0) return Mono.error(new IllegalArgumentException("Cannot move beyond the start: " + pos));
		if (pos == 0) return resetCursor(cursor).then(Mono.fromSupplier(() -> cursor.posGlobal));
		return Mono.just(pos)
			.expand(toReach -> {
				if (toReach == cursor.posGlobal) return Mono.empty();
				if (toReach > cursor.posGlobal) {
					// move forward
					if (cursor.io == null) return Mono.error(new EOFException());
					return ((ReactiveIO.KnownSize) cursor.io).size()
						.flatMap(ioSize -> {
							long moveInIO = toReach - cursor.posGlobal;
							if (cursor.posInIO + moveInIO <= ioSize) {
								return ((ReactiveIO.Seekable) cursor.io)
									.seek(SeekFrom.CURRENT, moveInIO)
									.flatMap(done -> {
										cursor.moved(moveInIO);
										return Mono.empty();
									});
							}
							cursor.moved(ioSize - cursor.posInIO);
							return cursor.goNext().flatMap(ok -> ok.booleanValue() ? Mono.just(toReach) : Mono.error(new EOFException()));
						});
				}
				// move backward
				long moveInIO = toReach - cursor.posGlobal;
				if (cursor.posInIO > 0 && -moveInIO <= cursor.posInIO) {
					// just need to move backward inside current IO
					return ((ReactiveIO.Seekable) cursor.io)
						.seek(SeekFrom.CURRENT, moveInIO)
							.flatMap(done -> {
								cursor.moved(moveInIO);
								return Mono.empty();
							});
				}
				return cursor.goPrevious().thenReturn(toReach);
			})
			.then(Mono.fromSupplier(() -> cursor.posGlobal));
	}

	// SeekableCursor
	
	protected final class SeekableCursor {
		private int index;
		public ReactiveIO.Seekable io;
		public long posGlobal;
		public long ioSize;
		
		public Mono<Void> goNext() {
			if (++index >= ios.size())
				return Mono.error(new EOFException());
			io = (ReactiveIO.Seekable) ios.get(index);
			posGlobal += ioSize;
			return io.size().doOnSuccess(size -> ioSize = size).then();
		}
	}
	
	protected Mono<SeekableCursor> createSeekableCursor(long pos) {
		if (pos < 0) return Mono.error(new NegativeValueException(pos, IOChecks.FIELD_POS));
		SeekableCursor cursor = new SeekableCursor();
		cursor.index = 0;
		cursor.posGlobal = 0;
		if (this.ios.isEmpty()) {
			if (pos == 0) return Mono.just(cursor);
			return Mono.error(new EOFException());
		}
		cursor.io = (ReactiveIO.Seekable) this.ios.get(0);
		return moveSeekableCursorTo(cursor, pos);
	}
	
	protected Mono<SeekableCursor> moveSeekableCursorTo(SeekableCursor cursor, long pos) {
		return ((ReactiveIO.KnownSize) cursor.io).size()
			.flatMap(size -> {
				if (pos >= cursor.posGlobal + size) {
					if (++cursor.index >= this.ios.size())
						return Mono.error(new EOFException());
					cursor.io = (ReactiveIO.Seekable) this.ios.get(cursor.index);
					cursor.posGlobal += size;
					return moveSeekableCursorTo(cursor, pos);
				}
				cursor.ioSize = size;
				return Mono.just(cursor);
			});
	}

	// IO
	
	@Override
	public Mono<Void> closeInternal() {
		return Mono.defer(() -> {
			if (ios == null) return Mono.empty();
			List<? extends ReactiveIO> list = ios;
			ios = null;
			if (!closeIosOnClose) return Mono.empty();
			return Flux.fromIterable(list)
				.flatMap(ReactiveIO::close)
				.then();
		});
	}
	
	@Override
	public Scheduler getScheduler() {
		return scheduler;
	}
	
	
	// KnownSize & Seekable
	
	@Override
	public Mono<Long> position() {
		return Mono.fromCallable(() -> {
			if (isClosed()) throw new ClosedChannelException();
			return posCursor.posGlobal;
		});
	}
	
	@Override
	public Mono<Long> size() {
		return Flux.defer(() -> {
			if (isClosed()) return Flux.error(new ClosedChannelException());
			return Flux.fromIterable(ios);
		})
		.flatMap(io -> ((ReactiveIO.KnownSize) io).size())
		.reduce(0L, (size, add) -> size + add);
	}
	
	@Override
	public Mono<Long> seek(SeekFrom from, long offset) {
		return ReactiveIOChecks.deferNotClosed(this, () -> {
			Objects.requireNonNull(from, "from");
			switch (from) {
			case START:
				return moveCursorAt(posCursor, offset);
			case CURRENT:
				return moveCursorAt(posCursor, posCursor.posGlobal + offset);
			case END:
				return size()
					.flatMap(size -> moveCursorAt(posCursor, size - offset));
			}
			return position();
		});
	}

	
	
	@Override
	public Mono<Long> skipUpTo(long nbBytes) {
		return ReactiveIOChecks.deferNotClosed(this, () -> {
			if (nbBytes < 0) return Mono.error(new NegativeValueException(nbBytes, "nbBytes"));
			if (nbBytes == 0) return Mono.just(0L);
			if (posCursor.io == null) return Mono.just(-1L);
			return skipNextUpTo(nbBytes);
		});
	}
	
	private Mono<Long> skipNextUpTo(long nbBytes) {
		return ((ReactiveIO.Readable) posCursor.io).skipUpTo(nbBytes)
			.flatMap(nb -> {
				if (nb > 0) {
					posCursor.moved(nb);
					return Mono.just(nb);
				}
				return posCursor.goNext()
					.flatMap(ok -> ok.booleanValue() ? skipNextUpTo(nbBytes) : Mono.just(-1L));
			});
	}
	


	@Override
	public Mono<Void> flush() {
		return Flux.defer(() -> {
			if (isClosed()) return Flux.error(new ClosedChannelException());
			return Flux.fromIterable(ios);
		})
		.flatMap(io -> ((ReactiveIO.Writable) io).flush())
		.then();	
	}
}
