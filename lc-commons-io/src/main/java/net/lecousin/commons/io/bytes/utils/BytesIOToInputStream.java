package net.lecousin.commons.io.bytes.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import net.lecousin.commons.io.bytes.BytesIO;

/**
 * Create an InputStream from a BytesIO.Readable.
 */
public class BytesIOToInputStream extends InputStream {

	private BytesIO.Readable io;
	
	/**
	 * Constructor
	 * @param io I/O to convert into an InputStream
	 */
	public BytesIOToInputStream(BytesIO.Readable io) {
		this.io = io;
	}
	
	// CHECKSTYLE DISABLE: MagicNumber
	@Override
	public int read() throws IOException {
		try {
			return io.readByte() & 0xFF;
		} catch (EOFException e) {
			return -1;
		}
	}
	// CHECKSTYLE ENABLE: MagicNumber
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return io.readBytes(b, off, len);
	}
	
	@Override
	public long skip(long n) throws IOException {
		return io.skipUpTo(n);
	}
	
}
