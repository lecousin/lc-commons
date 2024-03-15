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

	private FileChannel channel;
	private boolean canAppend;
	private long size;
	private long position;
	
	protected FileIO(FileChannel channel, boolean canAppend) {
		this.channel = channel;
		this.canAppend = canAppend;
		try {
			position = channel.position();
		} catch (IOException e) {
			position = 0;
		}
		try {
			size = channel.size();
		} catch (IOException e) {
			size = 0;
		}
	}
	
	@Override
	public void closeInternal() throws IOException {
		channel.close();
	}
	
	@Override
	public long size() throws IOException {
		if (!channel.isOpen()) throw new ClosedChannelException();
		return size;
	}
	
	@Override
	public long position() throws IOException {
		if (!channel.isOpen()) throw new ClosedChannelException();
		return position;
	}
	
	@Override
	public long seek(SeekFrom from, long offset) throws IOException {
		if (!channel.isOpen()) throw new ClosedChannelException();
		Objects.requireNonNull(from, "from");
		long p = 0;
		switch (from) {
		case END: p = size - offset; break;
		case CURRENT: p = position + offset; break;
		case START: default: p = offset; break;
		}
		if (p < 0) throw new IllegalArgumentException("Cannot move beyond the start: " + p);
		if (p > size) {
			if (!canAppend) throw new EOFException();
			channel.position(p - 1);
			channel.write(ByteBuffer.allocate(1));
			size = position = p;
		} else {
			channel.position(p);
			position = p;
		}
		return p;
	}
	
	// --- Readable ---
	
	protected byte readByte() throws IOException {
		ByteBuffer b = ByteBuffer.allocate(1);
		if (channel.read(b) <= 0) throw new EOFException();
		position++;
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
		int nb = channel.read(buffer);
		if (nb > 0) position += nb;
		return nb;
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
		position += nb;
		return Optional.of(b.flip());
	}
	
	protected long skipUpTo(long toSkip) throws IOException {
		if (!channel.isOpen()) throw new ClosedChannelException();
		if (toSkip == 0) return 0;
		NegativeValueException.check(toSkip, "toSkip");
		if (position == size) return -1;
		long target = position + toSkip;
		if (target > size) target = size;
		channel.position(target);
		long posBefore = position;
		position = target;
		return target - posBefore;
	}
	
	// --- Writable ---
	
	protected void flush() throws IOException {
		if (!channel.isOpen()) throw new ClosedChannelException();
		// not buffered, nothing to flush
	}
	
	protected void writeByte(byte value) throws IOException {
		if (!canAppend) {
			if (!channel.isOpen()) throw new ClosedChannelException();
			if (position >= size) throw new EOFException();
		}
		int nb = channel.write(ByteBuffer.wrap(new byte[] { value }));
		if (nb > 0) {
			position += nb;
			if (position > size) size = position;
		} else throw new EOFException();
	}
	
	protected void writeByteAt(long pos, byte value) throws IOException {
		if (!canAppend) {
			if (!channel.isOpen()) throw new ClosedChannelException();
			if (pos >= size) throw new EOFException();
		}
		int nb = channel.write(ByteBuffer.wrap(new byte[] { value }), pos);
		if (nb > 0) {
			if (pos + nb > size) size = pos + nb;
		} else throw new EOFException();
	}
	
	protected int writeBytes(ByteBuffer buffer) throws IOException {
		if (!canAppend) {
			if (!channel.isOpen()) throw new ClosedChannelException();
			if (buffer.remaining() == 0) return 0;
			if (position + buffer.remaining() > size) {
				int l = buffer.limit();
				buffer.limit(buffer.position() + (int) (size - position));
				int nb = channel.write(buffer);
				buffer.limit(l);
				if (nb > 0) {
					position += nb;
					return nb;
				}
				return -1;
			}
		}
		int nb = channel.write(buffer);
		if (nb > 0) {
			position += nb;
			if (position > size) size = position;
			return nb;
		}
		return buffer.remaining() == 0 ? 0 : -1;
	}
	
	protected int writeBytesAt(long pos, ByteBuffer buffer) throws IOException {
		if (isClosed()) throw new ClosedChannelException();
		NegativeValueException.check(pos, IOChecks.FIELD_POS);
		if (buffer.remaining() == 0) return 0;
		if (!canAppend) {
			if (pos >= size) return -1;
			if (pos + buffer.remaining() > size) {
				int l = buffer.limit();
				buffer.limit(buffer.position() + (int) (size - pos));
				int nb = channel.write(buffer, pos);
				buffer.limit(l);
				return nb;
			}
		}
		int nb = channel.write(buffer, pos);
		if (nb > 0 && pos + nb > size) size = pos + nb;
		return nb;
	}
	
	protected void writeBytesFully(ByteBuffer buffer) throws IOException {
		if (!channel.isOpen()) throw new ClosedChannelException();
		if (!canAppend) {
			if (buffer.remaining() == 0) return;
			if (position + buffer.remaining() > size) throw new EOFException();
		}
		while (buffer.hasRemaining()) {
			int nb = channel.write(buffer);
			if (nb <= 0) throw new EOFException();
			position += nb;
			if (position > size) size = position;
		}
	}
	
	protected void writeBytesFullyAt(long pos, ByteBuffer buffer) throws IOException {
		IOChecks.checkBufferOperation(this, pos, buffer);
		if (buffer.remaining() == 0) return;
		if (!canAppend && pos + buffer.remaining() > size) throw new EOFException();
		while (buffer.hasRemaining()) {
			int nb = channel.write(buffer, pos);
			if (nb <= 0) throw new EOFException();
			pos += nb;
			if (pos > size) size = pos;
		}
	}
	
	// --- Resizable ---
	
	protected void setSize(long newSize) throws IOException {
		if (!channel.isOpen()) throw new ClosedChannelException();
		NegativeValueException.check(newSize, "newSize");
		if (size == newSize) return;
		if (newSize < size) {
			channel.truncate(newSize);
			size = newSize;
			if (position > size) position = size;
			return;
		}
		channel.write(ByteBuffer.allocate(1), newSize - 1);
		size = newSize;
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
		@Override
		public void writeBytesFully(ByteBuffer buffer) throws IOException { super.writeBytesFully(buffer); }
		@Override
		public void writeBytesFullyAt(long pos, ByteBuffer buffer) throws IOException { super.writeBytesFullyAt(pos, buffer); }
		
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
		@Override
		public void writeBytesFully(ByteBuffer buffer) throws IOException { super.writeBytesFully(buffer); }
		@Override
		public void writeBytesFullyAt(long pos, ByteBuffer buffer) throws IOException { super.writeBytesFullyAt(pos, buffer); }

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
