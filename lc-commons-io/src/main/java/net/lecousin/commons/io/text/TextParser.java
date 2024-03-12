package net.lecousin.commons.io.text;

import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.function.Function;

import net.lecousin.commons.io.bytes.BytesIO;
import net.lecousin.commons.io.chars.CharsIO;

/**
 * Interface for a text parser.
 * @param <T> type of generated content
 */
public interface TextParser<T> {

	/**
	 * Parse new characters.
	 * @param chars characters to parse
	 */
	void parse(CharBuffer chars);
	
	/**
	 * Signal the end of input, and generate the content.
	 * @return generated content
	 */
	T endOfInput();
	
	/**
	 * Map this parser to generate another result.
	 * @param <R> type of result
	 * @param mapper mapper
	 * @return new parser
	 */
	default <R> TextParser<R> map(Function<T, R> mapper) {
		final TextParser<T> that = this;
		return new TextParser<R>() {
			@Override
			public void parse(CharBuffer chars) {
				that.parse(chars);
			}
			
			@Override
			public R endOfInput() {
				return mapper.apply(that.endOfInput());
			}
		};
	}
	
	/**
	 * Parse from an InputStream, using a specific Charset.
	 * @param input input
	 * @param charset charset to use to decode the input into characters
	 * @param closeStream if true, the InputStream will be closed at the end
	 * @return the result of the parsing
	 * @throws IOException in case an error occurs while reading on the input
	 */
	default T parse(InputStream input, Charset charset, boolean closeStream) throws IOException {
		return parse(BytesIO.from(input, closeStream), charset, true);
	}
	
	/**
	 * Parse from a BytesIO, using a specific Charset.
	 * @param input input
	 * @param charset charset to use to decode the input into characters
	 * @param closeIo if true, the BytesIO will be closed at the end
	 * @return the result of the parsing
	 * @throws IOException in case an error occurs while reading on the input
	 */
	default T parse(BytesIO.Readable input, Charset charset, boolean closeIo) throws IOException {
		try (var io = CharsIO.fromBytesIO(input, charset, closeIo)) {
			return parse(io);
		}
	}
	
	/**
	 * Parse from a CharsIO.
	 * @param input input
	 * @return the result of the parsing
	 * @throws IOException in case an error occurs while reading on the input
	 */
	default T parse(CharsIO.Readable input) throws IOException {
		Optional<CharBuffer> b;
		while ((b = input.readBuffer()).isPresent())
			parse(b.get());
		return endOfInput();
	}
	
}
