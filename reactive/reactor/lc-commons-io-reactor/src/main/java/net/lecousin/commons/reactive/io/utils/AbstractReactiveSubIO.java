package net.lecousin.commons.reactive.io.utils;

import java.io.EOFException;
import java.util.Optional;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.IO.Seekable.SeekFrom;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.reactive.io.AbstractReactiveIO;
import net.lecousin.commons.reactive.io.ReactiveIO;
import net.lecousin.commons.reactive.io.ReactiveIOChecks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Abstract class for a reactive Sub-IO
 * @param <I> type of IO
 */
public abstract class AbstractReactiveSubIO<I extends ReactiveIO> extends AbstractReactiveIO implements ReactiveIO.Readable, ReactiveIO.Writable, ReactiveIO.Seekable {

	protected I io;
	protected long start;
	protected long end;
	private boolean closeMainIO;
	protected long position = 0;
	
	protected AbstractReactiveSubIO(I io, long start, long end, boolean closeMainIO) {
		this.io = io;
		this.start = start;
		this.end = end;
		this.closeMainIO = closeMainIO;
	}

	// IO
	
	@Override
	public Mono<Void> closeInternal() {
		return Mono.defer(() -> {
			if (io == null) return Mono.empty();
			I i = io;
			io = null;
			if (closeMainIO)
				return i.close();
			return Mono.empty();
		});
	}
	
	@Override
	public Scheduler getScheduler() {
		if (io == null) return Schedulers.parallel();
		return io.getScheduler();
	}
	
	// checks
	
	protected Optional<Exception> check(long pos, long amount) {
		if (pos < 0) return Optional.of(new NegativeValueException(pos, IOChecks.FIELD_POS));
		if (start + pos + amount > end) return Optional.of(new EOFException());
		return Optional.empty();
	}
	
	// Seekable
	
	@Override
	public Mono<Long> position() {
		return ReactiveIOChecks.deferNotClosed(this, () -> Mono.just(position));
	}
	
	@Override
	public Mono<Long> seek(SeekFrom from, long offset) {
		return ReactiveIOChecks.deferNotClosed(this, () -> {
			if (from == null) return Mono.error(new NullPointerException("from"));
			long p = position;
			switch (from) {
			case CURRENT: p += offset; break;
			case START: p = offset; break;
			case END: p = end - start - offset; break;
			}
			if (p < 0) return Mono.error(new IllegalArgumentException("Cannot move to position " + p));
			if (p + start > end) return Mono.error(new EOFException());
			this.position = p;
			return Mono.just(p);
		});
	}
	
	// Known Size
	
	@Override
	public Mono<Long> size() {
		return ReactiveIOChecks.deferNotClosed(this, () -> Mono.just(end - start));
	}
	
	// Readable
	
	@Override
	public Mono<Long> skipUpTo(long toSkip) {
		return ReactiveIOChecks.deferNotClosed(this, () -> {
			if (toSkip < 0) return Mono.error(new NegativeValueException(toSkip, "toSkip"));
			if (toSkip == 0) return Mono.just(0L);
			if (position == end - start) return Mono.just(-1L);
			long nb = Math.min(toSkip, end - start - position);
			this.position += nb;
			return Mono.just(nb);
		});
	}
	
	@Override
	public Mono<Void> skipFully(long toSkip) {
		return ReactiveIOChecks.deferNotClosed(this, () -> {
			if (toSkip < 0) return Mono.error(new NegativeValueException(toSkip, "toSkip"));
			if (position + toSkip > end - start) return Mono.error(new EOFException());
			this.position += toSkip;
			return Mono.empty();
		});
	}

	// Writable
	
	@Override
	@SuppressWarnings("java:S1612") // false positive because io may be null
	public Mono<Void> flush() {
		return ReactiveIOChecks.deferNotClosed(this, () -> ((ReactiveIO.Writable) io).flush());
	}

}
