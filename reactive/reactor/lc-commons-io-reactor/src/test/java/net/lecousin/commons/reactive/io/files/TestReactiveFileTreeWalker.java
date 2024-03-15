package net.lecousin.commons.reactive.io.files;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import org.apache.commons.io.file.NoopPathVisitor;
import org.junit.jupiter.api.Test;

import net.lecousin.commons.reactive.io.files.ReactiveFileTreeWalker.WalkerEvent;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class TestReactiveFileTreeWalker {

	@Test
	void test() throws Exception {
		Path root = Files.createTempDirectory("test-lc-reactive-files");
		Path file1 = Files.createFile(root.resolve("file1"));
		Path dir1 = Files.createDirectory(root.resolve("dir1"));
		Path file2 = Files.createFile(dir1.resolve("file2"));
		Path dir2 = Files.createDirectory(dir1.resolve("dir2"));
		
		List<WalkerEvent> result = ReactiveFileTreeWalker.start(root, new NoopPathVisitor()).collectList().block();
		assertThat(result).hasSize(5);
		assertThat(result.stream().map(WalkerEvent::getPath)).containsExactlyInAnyOrder(root, dir1, dir2, file1, file2);
	}
	
	@Test
	void testDirectoryDoesNotExist() throws Exception {
		Path root = Files.createTempDirectory("test-lc-reactive-files");
		Files.delete(root);
		Mono<List<WalkerEvent>> walk = ReactiveFileTreeWalker.start(root, new NoopPathVisitor()).collectList();
		StepVerifier.create(walk).expectError(NoSuchFileException.class).verify();
	}
	
	@Test
	void testExcludeFile() throws Exception {
		Path root = Files.createTempDirectory("test-lc-reactive-files");
		Path file1 = Files.createFile(root.resolve("file1"));
		Path dir1 = Files.createDirectory(root.resolve("dir1"));
		Path file2 = Files.createFile(dir1.resolve("file2"));
		Path dir2 = Files.createDirectory(dir1.resolve("dir2"));
		
		List<WalkerEvent> result = ReactiveFileTreeWalker.start(root, new FileVisitor<>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (file.equals(file2))
					return FileVisitResult.TERMINATE;
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
			
		}).collectList().block();
		assertThat(result).hasSize(4);
		assertThat(result.stream().map(WalkerEvent::getPath)).containsExactlyInAnyOrder(root, dir1, dir2, file1);
	}
	
}
