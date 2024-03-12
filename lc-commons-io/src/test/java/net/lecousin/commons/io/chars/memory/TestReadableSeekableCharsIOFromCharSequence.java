package net.lecousin.commons.io.chars.memory;

import java.util.List;

import net.lecousin.commons.io.chars.AbstractReadableSeekableCharsIOTest;
import net.lecousin.commons.io.chars.CharsIO;
import net.lecousin.commons.io.chars.CharsIO.Readable.Seekable;
import net.lecousin.commons.test.TestCase;

public class TestReadableSeekableCharsIOFromCharSequence extends AbstractReadableSeekableCharsIOTest {

	@Override
	public List<? extends TestCase<char[], Seekable>> getTestCases() {
		return List.of(
			new TestCase<>("ReadableSeekableCharsIOFromCharSequence", content -> CharsIO.asReadableSeekable(new String(content)))
		);
	}
	
}
