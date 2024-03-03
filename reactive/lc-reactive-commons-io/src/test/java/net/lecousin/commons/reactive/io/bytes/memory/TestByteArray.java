package net.lecousin.commons.reactive.io.bytes.memory;

import java.util.List;

import net.lecousin.commons.io.bytes.memory.ByteArray;
import net.lecousin.commons.reactive.io.bytes.AbstractReadWriteReactiveBytesIOTest;
import net.lecousin.commons.reactive.io.bytes.ReactiveBytesIO;
import net.lecousin.commons.test.TestCase;

public class TestByteArray extends AbstractReadWriteReactiveBytesIOTest<ReactiveBytesIO.ReadWrite> {

	@Override
	public List<? extends TestCase<Integer, ReactiveBytesIO.ReadWrite>> getTestCases() {
		return List.of(
			new TestCase<>("fromByteArray", size -> ReactiveBytesIO.fromByteArray(new ByteArray(new byte[size]))),
			new TestCase<>("fromByteArrayAppendable", size -> ReactiveBytesIO.fromByteArrayAppendable(new ByteArray(new byte[size])))
		);
	}
	
}
