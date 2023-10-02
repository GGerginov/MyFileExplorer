package org.example.utill;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FileGeneratorTest {

    private static final String PATH = "src/test/resources";

    Path tempDir;

    @BeforeEach
    void setUp() {
        try {
            tempDir = Files.createTempDirectory(Path.of(PATH), "test");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        try (Stream<Path> pathStream = Files.walk(tempDir)) {
            pathStream
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (!file.delete()) {
                            System.err.println("Failed to delete file: " + file.getAbsolutePath());
                        }
                    });
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void testGenerateFilesWhenFilesGeneratedThenReturnTrue() {
        String directoryPath = tempDir.toString();
        int numFiles = 5;

        boolean result = FileGenerator.generateFiles(directoryPath, numFiles);

        assertTrue(result);
        File[] files = tempDir.toFile().listFiles();
        if (files != null) {
            assertEquals(numFiles, files.length);
        } else {
            fail("Failed to list files in the directory");
        }
    }

    @Test
    void testGenerateFilesWithInvalidDirectoryMustTrow() {
        String invalidPath = "/invalid/directory";
        int numFiles = 5;

        boolean result = FileGenerator.generateFiles(invalidPath, numFiles);

        assertFalse(result, "Expected generateFiles to return false");
    }

    @Test
    void testConstructorMustTrow() throws NoSuchMethodException {

        Constructor<FileGenerator> constructor = FileGenerator.class.getDeclaredConstructor();

        constructor.setAccessible(true);

        assertThrows(Exception.class, constructor::newInstance);
    }
}