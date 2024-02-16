package net.lecousin.commons.io.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.EOFException;
import java.nio.channels.ClosedChannelException;
import java.util.function.Function;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.io.IO.Seekable.SeekFrom;
import net.lecousin.commons.test.ParameterizedTestUtils;
import net.lecousin.commons.test.ParameterizedTestUtils.CompositeArgumentsProvider;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractSeekableBytesDataIOTest implements TestCasesProvider<Integer, BytesDataIO.Seekable> {

	public static class SeekableArgsProvider extends CompositeArgumentsProvider {
		public SeekableArgsProvider() {
			super(new BytesDataIOTestUtils.SizeProvider(), new ParameterizedTestUtils.TestCasesArgumentsProvider());
		}
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(SeekableArgsProvider.class)
	void testBasicsSeekable(String displayName, int size, Function<Integer, BytesDataIO.Seekable> streamSupplier) throws Exception {
		BytesDataIO.Seekable stream = streamSupplier.apply(size);
		assertEquals((long) size, stream.size());
		assertEquals(0L, stream.seek(SeekFrom.START, 0L));
		assertEquals(0L, stream.position());
		assertEquals((long) size, stream.seek(SeekFrom.END, 0L));
		assertEquals((long) size, stream.position());
		assertEquals((long) (size - size / 2), stream.seek(SeekFrom.CURRENT, -(size / 2)));
		assertEquals((long) (size - size / 2), stream.position());
		assertEquals((long) size, stream.seek(SeekFrom.CURRENT, size / 2));
		assertEquals((long) size, stream.position());
		
		assertThrows(IllegalArgumentException.class, () -> stream.seek(SeekFrom.START, -1L));
		assertThrows(NullPointerException.class, () -> stream.seek(null, 0L));
		assertThrows(EOFException.class, () -> stream.seek(SeekFrom.START, size + 1));
		assertThrows(EOFException.class, () -> stream.seek(SeekFrom.END, -1));
		assertThrows(EOFException.class, () -> stream.seek(SeekFrom.CURRENT, 1));
		
		assertEquals((long) size, stream.position());
		
		assertFalse(stream.isClosed());
		stream.close();
		assertTrue(stream.isClosed());
		
		assertThrows(ClosedChannelException.class, () -> stream.position());
		assertThrows(ClosedChannelException.class, () -> stream.size());
		assertThrows(ClosedChannelException.class, () -> stream.seek(SeekFrom.START, 0));
		assertThrows(ClosedChannelException.class, () -> stream.seek(SeekFrom.CURRENT, 0));
		assertThrows(ClosedChannelException.class, () -> stream.seek(SeekFrom.END, 0));
		assertThrows(ClosedChannelException.class, () -> stream.seek(null, 0));
	}
	
}
