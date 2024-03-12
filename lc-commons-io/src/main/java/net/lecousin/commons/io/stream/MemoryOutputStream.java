package net.lecousin.commons.io.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import net.lecousin.commons.io.bytes.memory.ByteArray;

/** An OutputStream that keep data in memory, into ByteArray. */
public class MemoryOutputStream extends OutputStream {

	private LinkedList<ByteArray> buffers = new LinkedList<>();
	private int bufferSize;
	
	/**
	 * Constructor.
	 * @param bufferSize size of buffers to create when needed
	 */
	public MemoryOutputStream(int bufferSize) {
		this.bufferSize = bufferSize;
	}
	
	@Override
	public void write(int b) throws IOException {
		if (!buffers.isEmpty()) {
			ByteArray ba = buffers.getLast();
			if (ba.remaining() > 0) {
				ba.writeByte((byte) b);
				return;
			}
		}
		ByteArray ba = new ByteArray(new byte[bufferSize]);
		ba.writeByte((byte) b);
		buffers.add(ba);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (!buffers.isEmpty()) {
			ByteArray ba = buffers.getLast();
			if (len <= ba.remaining()) {
				ba.write(b, off, len);
				return;
			}
		}
		ByteArray ba = new ByteArray(b, off, len);
		ba.setPosition(len);
		buffers.add(ba);
	}
	
	/** @return next buffered bytes, or empty. */
	public Optional<ByteArray> getNextBytes() {
		if (buffers.isEmpty()) return Optional.empty();
		return Optional.of(buffers.removeFirst().flip());
	}
	
	/** @return all buffers already filled. */
	public List<ByteArray> getAllFilledBuffers() {
		LinkedList<ByteArray> list = new LinkedList<>();
		while (!buffers.isEmpty() && (buffers.size() > 1 || buffers.getFirst().remaining() == 0))
			list.add(buffers.removeFirst().flip());
		return list;
	}
	
	/** @return all buffers. */
	public List<ByteArray> getAllBuffers() {
		List<ByteArray> list = new ArrayList<>(buffers);
		list.forEach(ByteArray::flip);
		buffers.clear();
		return list;
	}
	
}
