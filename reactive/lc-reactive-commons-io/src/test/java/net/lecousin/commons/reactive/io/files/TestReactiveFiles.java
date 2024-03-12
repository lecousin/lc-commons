package net.lecousin.commons.reactive.io.files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import reactor.test.StepVerifier;

//because file access is not efficient using multi-threading, we use a single thread
@Execution(ExecutionMode.SAME_THREAD)
class TestReactiveFiles {

	@Test
	void test() throws Exception {
		Path root = Files.createTempDirectory("test-lc-reactive-files");
		
		Path dir1 = root.resolve("dir1");
		ReactiveFiles.createDirectory(dir1).block();
		assertThat(Files.isDirectory(dir1)).isTrue();
		
		assertThat(ReactiveFiles.listDirectoryContent(root).block()).containsExactly(dir1);
		
		Path dir2 = root.resolve(Path.of("subdir", "subdir2", "dir2"));
		assertEquals(dir2, ReactiveFiles.ensureDirectoryExists(dir2).block());
		assertThat(Files.isDirectory(dir2)).isTrue();
		assertThat(ReactiveFiles.listDirectoryContent(root).block()).containsExactlyInAnyOrder(dir1, root.resolve("subdir"));
		
		ReactiveFiles.deleteDirectoryWithContent(root).block();
		assertThat(Files.isDirectory(dir1)).isFalse();
		assertThat(Files.isDirectory(dir2)).isFalse();
		assertThat(Files.isDirectory(root)).isFalse();
	}
	
	@Test
	void testEnsureDirectoryExists_butItIsAFile() throws Exception {
		Path file = Files.createTempFile("test", "lc-reactive-file");
		StepVerifier.create(ReactiveFiles.ensureDirectoryExists(file.resolve("other"))).expectError(FileAlreadyExistsException.class).verify();
		ReactiveFiles.deleteFile(file).block();
	}
	
	@Test
	void testListDirectoryContentWithAttributes() throws Exception {
		Path root = Files.createTempDirectory("test-lc-reactive-files");
		Path file1 = Files.createFile(root.resolve("file1"));
		Path dir1 = Files.createDirectory(root.resolve("dir1"));
		Path file2 = Files.createFile(root.resolve("file2"));
		Path dir2 = Files.createDirectory(root.resolve("dir2"));
		
		var list = ReactiveFiles.listDirectoryContentWithAttributes(root).block();
		assertThat(list).hasSize(4)
		.anyMatch(tuple -> tuple.getT1().equals(file1) && tuple.getT2().isDirectory() == false)
		.anyMatch(tuple -> tuple.getT1().equals(file2) && tuple.getT2().isDirectory() == false)
		.anyMatch(tuple -> tuple.getT1().equals(dir1) && tuple.getT2().isDirectory() == true)
		.anyMatch(tuple -> tuple.getT1().equals(dir2) && tuple.getT2().isDirectory() == true)
		;
		
		ReactiveFiles.deleteDirectoryWithContent(root).block();
	}
	
}
