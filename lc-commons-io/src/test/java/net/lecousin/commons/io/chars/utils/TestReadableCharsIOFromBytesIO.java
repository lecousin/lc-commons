package net.lecousin.commons.io.chars.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import net.lecousin.commons.io.bytes.memory.ByteArray;
import net.lecousin.commons.io.chars.AbstractReadableCharsIOTest;
import net.lecousin.commons.io.chars.CharsIO;
import net.lecousin.commons.test.TestCase;

public class TestReadableCharsIOFromBytesIO extends AbstractReadableCharsIOTest {

	@Override
	public List<? extends TestCase<char[], CharsIO.Readable>> getTestCases() {
		return List.of(
			new TestCase<>("Using ByteArrayIO and UTF-8", content -> {
				Charset charset = StandardCharsets.UTF_8;
				byte[] bytes = new String(content).getBytes(charset);
				ByteArray ba = new ByteArray(bytes);
				return new ReadableCharsIOFromBytesIO(ba.asBytesIO(), charset, true);
			})
		);
	}
	
}
