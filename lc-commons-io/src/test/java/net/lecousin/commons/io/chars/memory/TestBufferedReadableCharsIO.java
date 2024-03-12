package net.lecousin.commons.io.chars.memory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.commons.io.chars.AbstractReadableCharsIOTest;
import net.lecousin.commons.io.chars.CharsIO;
import net.lecousin.commons.io.chars.utils.CompositeCharsIO;
import net.lecousin.commons.test.TestCase;

public class TestBufferedReadableCharsIO extends AbstractReadableCharsIOTest {

	@Override
	public List<? extends TestCase<char[], CharsIO.Readable>> getTestCases() {
		return List.of(
			new TestCase<>("Using CharArrayIO", content -> new BufferedReadableCharsIO(new CharArray(content).asCharsIO(), false)),
			new TestCase<>("Using Composite of CharArray of 3 chars", content -> {
				List<CharsIO.Readable> list = new LinkedList<>();
				int pos = 0;
				while (pos + 3 <= content.length) {
					list.add(new CharArray(content, pos, 3).asCharsIO());
					pos += 3;
				}
				if (pos < content.length)
					list.add(new CharArray(content, pos, content.length - pos).asCharsIO());
				try {
					return new BufferedReadableCharsIO(CompositeCharsIO.fromReadable(list, true, true), true);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			})
		);
	}
	
}
