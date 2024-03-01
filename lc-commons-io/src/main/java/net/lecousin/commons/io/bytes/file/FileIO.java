package net.lecousin.commons.io.bytes.file;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import lombok.Generated;
import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.AbstractIO;
import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.io.bytes.BytesIO;

/**
 * File BytesIO.
 */
// CHECKSTYLE DISABLE: MagicNumber
public abstract class FileIO extends AbstractIO implements BytesIO, IO.Seekable {

	protected FileChannel channel;
	protected boolean canAppend;
	
	protected FileIO(FileChannel channel, boolean canAppend) {
		this.channel = channel;
		this.canAppend = canAppend;
	}
	
	@Override
	public void closeInternal() throws IOException {
		channel.close();
	}
	
	@Override
	public long size() throws IOException {
		return channel.size();
	}
	
	@Override
	public long position() throws IOException {
		return channel.position();
	}
	
	@Override
	public long seek(SeekFrom from, long offset) throws IOException {
		Objects.requireNonNull(from, "from");
		long s = channel.size();
		long p = 0;
		switch (from) {
		case START: p = offset; break;
		case END: p = s - offset; break;
		case CURRENT: p = channel.position() + offset; break;
		}
		if (p < 0) throw new IllegalArgumentException("Cannot move beyond the start: " + p);
		if (p > s) {
			if (!canAppend) throw new EOFException();
			channel.position(p - 1);
			channel.write(ByteBuffer.allocate(1));
		} else {
			channel.position(p);
		}
		return p;
	}
	
	// --- Readable ---
	
	protected byte readByte() throws IOException {
		ByteBuffer b = ByteBuffer.allocate(1);
		if (channel.read(b) <= 0) throw new EOFException();
		b.flip();
		return b.get();
	}
	
	protected byte readByteAt(long pos) throws IOException {
		if (!channel.isOpen()) throw new ClosedChannelException();
		NegativeValueException.check(pos, IOChecks.FIELD_POS);
		ByteBuffer b = ByteBuffer.allocate(1);
		if (channel.read(b, pos) <= 0)
			throw new EOFException();
		return b.flip().get();
	}
	
	protected int readBytes(ByteBuffer buffer) throws IOException {
		return channel.read(buffer);
	}
	
	protected int readBytesAt(long pos, ByteBuffer buffer) throws IOException {
		if (!channel.isOpen()) throw new ClosedChannelException();
		NegativeValueException.check(pos, IOChecks.FIELD_POS);
		return channel.read(buffer, pos);
	}
	
	protected Optional<ByteBuffer> readBuffer() throws IOException {
		ByteBuffer b = ByteBuffer.allocate(8192);
		int nb = channel.read(b);
		if (nb <= 0) return Optional.empty();
		return Optional.of(b.flip());
	}
	
	protected long skipUpTo(long toSkip) throws IOException {
		long max = channel.size();
		long pos = channel.position();
		NegativeValueException.check(toSkip, "toSkip");
		long target = pos + toSkip;
		if (target > max) target = max;
		channel.position(target);
		return target - pos;
	}
	
	// --- Writable ---
	
	protected void flush() throws IOException {
		if (!channel.isOpen()) throw new ClosedChannelException();
		// not buffered, nothing to flush
	}
	
	protected void writeByte(byte value) throws IOException {
		if (!canAppend) {
			long s = channel.size();
			long p = channel.position();
			if (p >= s) throw new EOFException();
		}
		if (channel.write(ByteBuffer.wrap(new byte[] { value })) <= 0) throw new EOFException(); // impossible to reproduce in test (need disk full)
	}
	
	protected void writeByteAt(long pos, byte value) throws IOException {
		if (!canAppend) {
			long s = channel.size();
			if (pos >= s) throw new EOFException();
		}
		if (channel.write(ByteBuffer.wrap(new byte[] { value }), pos) <= 0) throw new EOFException();  // impossible to reproduce in test (need disk full)
	}
	
	protected int writeBytes(ByteBuffer buffer) throws IOException {
		if (!canAppend) {
			long s = channel.size();
			if (buffer.remaining() == 0) return 0;
			long p = channel.position();
			if (p + buffer.remaining() > s) {
				int l = buffer.limit();
				buffer.limit(buffer.position() + (int) (s - p));
				int nb = channel.write(buffer);
				buffer.limit(l);
				return nb <= 0 ? -1 : nb;
			}
		}
		int nb = channel.write(buffer);
		if (nb > 0)
			return nb;
		return buffer.remaining() == 0 ? 0 : -1;
	}
	
	protected int writeBytesAt(long pos, ByteBuffer buffer) throws IOException {
		if (isClosed()) throw new ClosedChannelException();
		NegativeValueException.check(pos, IOChecks.FIELD_POS);
		if (buffer.remaining() == 0) return 0;
		if (!canAppend) {
			long s = channel.size();
			if (pos >= s) return -1;
			if (pos + buffer.remaining() > s) {
				int l = buffer.limit();
				buffer.limit(buffer.position() + (int) (s - pos));
				int nb = channel.write(buffer, pos);
				buffer.limit(l);
				return nb;
			}
		}
		return channel.write(buffer, pos);
	}
	
	// --- Resizable ---
	
	protected void setSize(long newSize) throws IOException {
		NegativeValueException.check(newSize, "newSize");
		long current = channel.size();
		if (current == newSize) return;
		if (newSize < current) {
			channel.truncate(newSize);
			return;
		}
		writeAtEndAndWaitForSizeToBeUpdated(newSize);
	}
	
	@Generated // all lines cannot be reproduced
	private void writeAtEndAndWaitForSizeToBeUpdated(long newSize) throws IOException {
		int trial = 1;
		do {
			channel.write(ByteBuffer.allocate(1), newSize - 1);
			if (channel.size() == newSize) return;
		} while (++trial < 10);
		throw new IOException("FileIO.setSize failed");
	}
	
	
	// CHECKSTYLE DISABLE: LeftCurly
	// CHECKSTYLE DISABLE: RightCurly
	// CHECKSTYLE DISABLE: EmptyLineSeparator
	
	/** Read-only FileIO. */
	public static class Readable extends FileIO implements BytesIO.Readable.Seekable {
		
		/**
		 * Constructor.
		 * @param channel file channel
		 */
		public Readable(FileChannel channel) {
			super(channel, false);
		}
		
		/**
		 * Constructor.
		 * @param path file to open
		 * @param options options specifying how the file is opened
		 * @throws IOException if the file cannot be open
		 */
		public Readable(Path path, Set<? extends OpenOption> options) throws IOException {
			this((FileChannel) Files.newByteChannel(path, options));
		}
		
		/** Constructor.
		 * @param path file to open
		 * @throws IOException if the file cannot be open
		 */
		public Readable(Path path) throws IOException {
			this(path, Set.of(StandardOpenOption.READ));
		}
		
		@Override
		public byte readByte() throws IOException { return super.readByte(); }
		@Override
		public byte readByteAt(long pos) throws IOException { return super.readByteAt(pos); }
		@Override
		public int readBytes(ByteBuffer buffer) throws IOException { return super.readBytes(buffer); }
		@Override
		public int readBytesAt(long pos, ByteBuffer buffer) throws IOException { return super.readBytesAt(pos, buffer); }
		@Override
		public Optional<ByteBuffer> readBuffer() throws IOException { return super.readBuffer(); }
		@Override
		public long skipUpTo(long toSkip) throws IOException { return super.skipUpTo(toSkip); }
	}
	
	/** Write-only FileIO. */
	public static class Writable extends FileIO implements BytesIO.Writable.Seekable {
		
		protected Writable(FileChannel channel, boolean appendable) {
			super(channel, appendable);
		}
		
		/**
		 * Constructor.
		 * @param channel file channel
		 */
		public Writable(FileChannel channel) {
			this(channel, false);
		}

		/**
		 * Constructor.
		 * @param path file to open
		 * @param options options specifying how the file is opened
		 * @param attrs an optional list of file attributes to set atomically when creating the file
		 * @throws IOException if the file cannot be open
		 */
		public Writable(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
			this((FileChannel) Files.newByteChannel(path, options, attrs));
		}
		
		/** Constructor.
		 * @param path file to open
		 * @throws IOException if the file cannot be open
		 */
		public Writable(Path path) throws IOException {
			this(path, Set.of(StandardOpenOption.WRITE));
		}
		
		@Override
		public void flush() throws IOException { super.flush(); }
		@Override
		public void writeByte(byte value) throws IOException { super.writeByte(value); }
		@Override
		public void writeByteAt(long pos, byte value) throws IOException { super.writeByteAt(pos, value); }
		@Override
		public int writeBytes(ByteBuffer buffer) throws IOException { return super.writeBytes(buffer); }
		@Override
		public int writeBytesAt(long pos, ByteBuffer buffer) throws IOException { return super.writeBytesAt(pos, buffer); }
		
		/** Write-only and appendable FileIO. */
		public static class Appendable extends FileIO.Writable implements BytesIO.Writable.Seekable.Appendable {
			
			/**
			 * Constructor.
			 * @param channel file channel
			 */
			public Appendable(FileChannel channel) {
				super(channel, true);
			}

			/**
			 * Constructor.
			 * @param path file to open
			 * @param options options specifying how the file is opened
			 * @param attrs an optional list of file attributes to set atomically when creating the file
			 * @throws IOException if the file cannot be open
			 */
			public Appendable(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
				this((FileChannel) Files.newByteChannel(path, options, attrs));
			}
			
			/** Constructor.
			 * @param path file to open
			 * @throws IOException if the file cannot be open
			 */
			public Appendable(Path path) throws IOException {
				this(path, Set.of(StandardOpenOption.WRITE));
			}
		}
		
		/** Write-only and resizable FileIO. */
		public static class Resizable extends FileIO.Writable implements BytesIO.Writable.Seekable.Resizable {
			protected Resizable(FileChannel channel, boolean appendable) {
				super(channel, appendable);
			}
			
			/**
			 * Constructor.
			 * @param channel file channel
			 */
			public Resizable(FileChannel channel) {
				this(channel, false);
			}

			/**
			 * Constructor.
			 * @param path file to open
			 * @param options options specifying how the file is opened
			 * @param attrs an optional list of file attributes to set atomically when creating the file
			 * @throws IOException if the file cannot be open
			 */
			public Resizable(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
				this((FileChannel) Files.newByteChannel(path, options, attrs));
			}
			
			/** Constructor.
			 * @param path file to open
			 * @throws IOException if the file cannot be open
			 */
			public Resizable(Path path) throws IOException {
				this(path, Set.of(StandardOpenOption.WRITE));
			}
			
			@Override
			public void setSize(long newSize) throws IOException { super.setSize(newSize); }
		}
		
		/** Write-only, appendable and resizable FileIO. */
		public static class AppendableResizable extends FileIO.Writable.Resizable implements BytesIO.Writable.Seekable.AppendableResizable {
			/**
			 * Constructor.
			 * @param channel file channel
			 */
			public AppendableResizable(FileChannel channel) {
				super(channel, true);
			}

			/**
			 * Constructor.
			 * @param path file to open
			 * @param options options specifying how the file is opened
			 * @param attrs an optional list of file attributes to set atomically when creating the file
			 * @throws IOException if the file cannot be open
			 */
			public AppendableResizable(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
				this((FileChannel) Files.newByteChannel(path, options, attrs));
			}
			
			/** Constructor.
			 * @param path file to open
			 * @throws IOException if the file cannot be open
			 */
			public AppendableResizable(Path path) throws IOException {
				this(path, Set.of(StandardOpenOption.WRITE));
			}
		}
		
	}

	/** Read and Write FileIO. */
	public static class ReadWrite extends FileIO implements BytesIO.ReadWrite {
		
		protected ReadWrite(FileChannel channel, boolean appendable) {
			super(channel, appendable);
		}
		
		/**
		 * Constructor.
		 * @param channel file channel
		 */
		public ReadWrite(FileChannel channel) {
			this(channel, false);
		}
		
		/**
		 * Constructor.
		 * @param path file to open
		 * @param options options specifying how the file is opened
		 * @param attrs an optional list of file attributes to set atomically when creating the file
		 * @throws IOException if the file cannot be open
		 */
		public ReadWrite(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
			this((FileChannel) Files.newByteChannel(path, options, attrs));
		}
		
		/** Constructor.
		 * @param path file to open
		 * @throws IOException if the file cannot be open
		 */
		public ReadWrite(Path path) throws IOException {
			this(path, Set.of(StandardOpenOption.READ, StandardOpenOption.WRITE));
		}
		
		@Override
		public byte readByte() throws IOException { return super.readByte(); }
		@Override
		public byte readByteAt(long pos) throws IOException { return super.readByteAt(pos); }
		@Override
		public int readBytes(ByteBuffer buffer) throws IOException { return super.readBytes(buffer); }
		@Override
		public int readBytesAt(long pos, ByteBuffer buffer) throws IOException { return super.readBytesAt(pos, buffer); }
		@Override
		public Optional<ByteBuffer> readBuffer() throws IOException { return super.readBuffer(); }
		@Override
		public long skipUpTo(long toSkip) throws IOException { return super.skipUpTo(toSkip); }
		
		@Override
		public void flush() throws IOException { super.flush(); }
		@Override
		public void writeByte(byte value) throws IOException { super.writeByte(value); }
		@Override
		public void writeByteAt(long pos, byte value) throws IOException { super.writeByteAt(pos, value); }
		@Override
		public int writeBytes(ByteBuffer buffer) throws IOException { return super.writeBytes(buffer); }
		@Override
		public int writeBytesAt(long pos, ByteBuffer buffer) throws IOException { return super.writeBytesAt(pos, buffer); }

		/** Read-Write Appendable FileIO. */
		public static class Appendable extends FileIO.ReadWrite implements BytesIO.ReadWrite.Appendable {
			
			/**
			 * Constructor.
			 * @param channel file channel
			 */
			public Appendable(FileChannel channel) {
				super(channel, true);
			}

			/**
			 * Constructor.
			 * @param path file to open
			 * @param options options specifying how the file is opened
			 * @param attrs an optional list of file attributes to set atomically when creating the file
			 * @throws IOException if the file cannot be open
			 */
			public Appendable(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
				this((FileChannel) Files.newByteChannel(path, options, attrs));
			}
			
			/** Constructor.
			 * @param path file to open
			 * @throws IOException if the file cannot be open
			 */
			public Appendable(Path path) throws IOException {
				this(path, Set.of(StandardOpenOption.READ, StandardOpenOption.WRITE));
			}
		}
		
		/** Read-Write Resizable FileIO. */
		public static class Resizable extends FileIO.ReadWrite implements BytesIO.ReadWrite.Resizable {
			protected Resizable(FileChannel channel, boolean appendable) {
				super(channel, appendable);
			}
			
			/**
			 * Constructor.
			 * @param channel file channel
			 */
			public Resizable(FileChannel channel) {
				this(channel, false);
			}

			/**
			 * Constructor.
			 * @param path file to open
			 * @param options options specifying how the file is opened
			 * @param attrs an optional list of file attributes to set atomically when creating the file
			 * @throws IOException if the file cannot be open
			 */
			public Resizable(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
				this((FileChannel) Files.newByteChannel(path, options, attrs));
			}
			
			/** Constructor.
			 * @param path file to open
			 * @throws IOException if the file cannot be open
			 */
			public Resizable(Path path) throws IOException {
				this(path, Set.of(StandardOpenOption.READ, StandardOpenOption.WRITE));
			}
			
			@Override
			public void setSize(long newSize) throws IOException { super.setSize(newSize); }

		}
		
		/** Read-Write Appendable and Resizable FileIO. */
		public static class AppendableResizable extends FileIO.ReadWrite.Resizable implements BytesIO.ReadWrite.AppendableResizable {
			
			/**
			 * Constructor.
			 * @param channel file channel
			 */
			public AppendableResizable(FileChannel channel) {
				super(channel, true);
			}

			/**
			 * Constructor.
			 * @param path file to open
			 * @param options options specifying how the file is opened
			 * @param attrs an optional list of file attributes to set atomically when creating the file
			 * @throws IOException if the file cannot be open
			 */
			public AppendableResizable(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
				this((FileChannel) Files.newByteChannel(path, options, attrs));
			}
			
			/** Constructor.
			 * @param path file to open
			 * @throws IOException if the file cannot be open
			 */
			public AppendableResizable(Path path) throws IOException {
				this(path, Set.of(StandardOpenOption.READ, StandardOpenOption.WRITE));
			}
		}
	}
	
}
