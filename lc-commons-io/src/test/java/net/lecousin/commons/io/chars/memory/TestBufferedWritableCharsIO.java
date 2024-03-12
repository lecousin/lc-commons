package net.lecousin.commons.io.chars.memory;

import java.util.List;

import org.junit.jupiter.api.Assertions;

import net.lecousin.commons.io.chars.AbstractWritableCharsIOTest;
import net.lecousin.commons.io.chars.CharsIO;
import net.lecousin.commons.test.TestCase;

public class TestBufferedWritableCharsIO extends AbstractWritableCharsIOTest {

	@Override
	public List<? extends TestCase<Integer, WritableTestCase<? extends CharsIO.Writable, ?>>> getTestCases() {
		return List.of(
			new TestCase<>("Using CharArray", size -> {
				CharArray ba = new CharArray(new char[Math.min(size, 1)]);
				BufferedWritableCharsIO<CharArrayIO.Appendable> buffered = new BufferedWritableCharsIO<>(ba.asAppendableCharsIO(11), false);
				return new WritableTestCase<>(buffered, ba);
			})
		);
	}
	
	@Override
	protected void checkWrittenData(CharsIO.Writable io, Object object, char[] expected) throws Exception {
		char[] found = null;
		CharArray ba = (CharArray) object;
		found = new char[ba.getSize()];
		System.arraycopy(ba.getArray(), ba.getArrayStartOffset(), found, 0, found.length);
		Assertions.assertArrayEquals(expected, found);
	}
	
}
