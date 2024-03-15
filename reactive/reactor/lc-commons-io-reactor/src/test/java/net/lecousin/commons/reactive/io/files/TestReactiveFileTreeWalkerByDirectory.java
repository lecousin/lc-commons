package net.lecousin.commons.reactive.io.files;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.lecousin.commons.reactive.io.files.ReactiveFileTreeWalkerByDirectory.DirectoryContent;

class TestReactiveFileTreeWalkerByDirectory {

	@Test
	void test() throws Exception {
		Path root = Files.createTempDirectory("test-lc-reactive-files");
		Files.createFile(root.resolve("file1"));
		Path dir1 = Files.createDirectory(root.resolve("dir1"));
		Files.createFile(dir1.resolve("file2"));
		Path dir2 = Files.createDirectory(dir1.resolve("dir2"));
		
		List<DirectoryContent> events = ReactiveFileTreeWalkerByDirectory.walk(root).collectList().block();
		assertThat(events).hasSize(3);
		
		DirectoryContent dir = events.get(0);
		assertThat(dir.getRealPath()).isEqualTo(root);
		assertThat(dir.getFiles()).hasSize(1).containsKey("file1");
		assertThat(dir.getSubDirectories()).hasSize(1).containsKey("dir1");
		
		dir = events.get(1);
		assertThat(dir.getRealPath()).isEqualTo(dir1);
		assertThat(dir.getFiles()).hasSize(1).containsKey("file2");
		assertThat(dir.getSubDirectories()).hasSize(1).containsKey("dir2");
		
		dir = events.get(2);
		assertThat(dir.getRealPath()).isEqualTo(dir2);
		assertThat(dir.getFiles()).isEmpty();
		assertThat(dir.getSubDirectories()).isEmpty();
	}
	
}
