package org.example.finder;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextFinderTest {

    private static final String PATH = "src/test/resources";

    Path tempDir;
    Path tempFile1;

    Path tempFile2;

    @BeforeEach
    void setUp() {

        try {
            tempDir = Files.createTempDirectory(Path.of(PATH),"test");
            tempFile1 = Files.createTempFile(tempDir, "testfile1", ".txt");
            Files.write(tempFile1, "This is a sample file for testing. Lorem ipsum".getBytes());

            tempFile2 = Files.createTempFile(tempDir, "testfile2", ".txt");
            Files.write(tempFile2, "demo".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {

        try {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void findTextIfStringIsNullMustTrow() {

        assertThrows(IllegalArgumentException.class,()-> TextFinder.findText(null,PATH));
    }


    @Test
    void findTextIfStringIsEmptyMustTrow() {

        assertThrows(IllegalArgumentException.class,()-> TextFinder.findText("",PATH));
    }

    @Test
    void testConstructorInitializeMustTrow() {

        assertThrows(IllegalStateException.class, () -> {
            try {
                Constructor<?> constructor = TextFinder.class.getDeclaredConstructor();

                constructor.setAccessible(true);
                constructor.newInstance();

            } catch (InvocationTargetException e) {

                throw e.getTargetException();
            }
        });
    }


    @Test
    void testFindTextCorrectlyReturnsFirstFile() {

        List<Path> listFiles = TextFinder.findText("demo", tempDir.toString());

        assertTrue(listFiles.contains(tempFile2));
    }

    @Test
    void testFindTextCorrectlyReturnsSecondFile() {

        List<Path> listFiles = TextFinder.findText("testing", tempDir.toString());

        assertTrue(listFiles.contains(tempFile1));
    }
}