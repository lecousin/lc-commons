package net.lecousin.commons.io.stream;

import java.io.IOException;
import java.io.OutputStream;

/** An OutputStream that ignore first bytes. */
public class SkipHeaderOutputStream extends OutputStream {

	private OutputStream out;
	private int toSkip;
	
	/** Constructor.
	 * 
	 * @param out output
	 * @param toSkip number of bytes to ignore before to start writing
	 */
	public SkipHeaderOutputStream(OutputStream out, int toSkip) {
		this.out = out;
		this.toSkip = toSkip;
	}
	
	@Override
	public void write(int b) throws IOException {
		if (toSkip > 0)
			toSkip--;
		else
			out.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (toSkip > 0) {
			if (len <= toSkip) {
				toSkip -= len;
				return;
			}
			len -= toSkip;
			off += toSkip;
			toSkip = 0;
		}
		if (len > 0) out.write(b, off, len);
	}
	
}
