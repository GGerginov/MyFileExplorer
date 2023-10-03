package org.example.visitor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class FileTextRecognitionVisitorTest {

    private static final String PATH = "src/test/resources";
    @Mock
    private ZipEntry mockZipEntry;
    @Mock
    private ZipFile mockZipFile;
    @Mock
    private File mockFile;

    @InjectMocks
    private FileTextRecognitionVisitor visitor;

    private Method processCompressedFile;

    private Method containsTextInFile;
    private Method containsTextInZipFile;

    private Method getFileSize;

    private Method isFileValid;

    private Method isFileArchive;

    private Method processCompressedFileForMatch;

    private File testFile;

    private File tempZipFile ;

    Path tempDir;

    @BeforeEach
    void setUp() throws NoSuchMethodException, IOException {
        tempDir = Files.createTempDirectory(Path.of(PATH), "test");
        visitor = new FileTextRecognitionVisitor("test");
        this.processCompressedFile = visitor.getClass().getDeclaredMethod("processCompressedFile", Path.class);
        this.processCompressedFile.setAccessible(true);

        this.containsTextInFile = visitor.getClass().getDeclaredMethod("containsTextInFile", Path.class);
        this.containsTextInFile.setAccessible(true);

        this.processCompressedFileForMatch = visitor.getClass().getDeclaredMethod("processCompressedFileForMatch", Path.class);
        this.processCompressedFileForMatch.setAccessible(true);

        this.containsTextInZipFile = visitor.getClass().getDeclaredMethod("containsTextInZippedFile", ZipFile.class, ZipEntry.class);
        this.containsTextInZipFile.setAccessible(true);

        this.getFileSize = visitor.getClass().getDeclaredMethod("getFileSize", Path.class);
        this.getFileSize.setAccessible(true);

        this.isFileValid = visitor.getClass().getDeclaredMethod("isFileValid", Path.class);
        this.isFileValid.setAccessible(true);

        this.isFileArchive = visitor.getClass().getDeclaredMethod("isFileArchive", File.class);
        this.isFileArchive.setAccessible(true);

        this.testFile = Files.createTempFile(tempDir,"test",".txt").toFile();
        Files.write(testFile.toPath(), "This is a sample file for testing. Lorem ipsum".getBytes());

        this.tempZipFile = File.createTempFile("myTempArchive", ".zip");


        FileOutputStream fos = new FileOutputStream(tempZipFile);

        ZipOutputStream zipOut = new ZipOutputStream(fos);

        ZipEntry entry = new ZipEntry("sample.txt");
        zipOut.putNextEntry(entry);

        String sampleData = "This is some sample test data for the ZIP file.";
        byte[] dataBytes = sampleData.getBytes();
        zipOut.write(dataBytes, 0, dataBytes.length);
        zipOut.closeEntry();

        zipOut.close();
        fos.close();
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
    void testProcessCompressedFileWhenNoMatch() throws InvocationTargetException, IllegalAccessException {

        visitor = new FileTextRecognitionVisitor("Invalid text which you can't find");

        List<Path> processedFiles = (List<Path>) processCompressedFile.invoke(visitor,tempZipFile.toPath());

        assertTrue(processedFiles.isEmpty());
    }
    @Test
    void testProcessCompressedFile() throws InvocationTargetException, IllegalAccessException {

        List<Path> processedFiles = (List<Path>) this.processCompressedFile.invoke(visitor,tempZipFile.toPath());

        assertEquals(1, processedFiles.size());
        assertEquals(tempZipFile.getName(), processedFiles.get(0).getFileName().toString());
    }

    @Test
    void testProcessCompressedFileForMatchWhenFileNotContainsTextThenReturnEmptyList() throws InvocationTargetException, IllegalAccessException {

        this.visitor = new FileTextRecognitionVisitor("BMW E92");

        List<Path> matchPats = (List<Path>) this.processCompressedFileForMatch.invoke(visitor, tempZipFile.toPath());

        matchPats.forEach(System.out::println);
        assertFalse(matchPats.contains(tempZipFile.toPath()));
    }

    @Test
    void testProcessCompressedFileForMatchWhenFileContainsTextThenReturnFilePath() throws InvocationTargetException, IllegalAccessException {


        List<Path> matchPats = (List<Path>) this.processCompressedFileForMatch.invoke(visitor, tempZipFile.toPath());

        matchPats.forEach(System.out::println);
        assertTrue(matchPats.contains(tempZipFile.toPath()));
    }

    @Test
    void testContainsTextInFileWhenFileIsEmptyThenReturnFalse() throws Exception {
        File tempFile = Files.createTempFile("tempFile", ".txt").toFile();

        boolean result = (boolean) this.containsTextInFile.invoke(visitor, tempFile.toPath());

        assertFalse(result);

        Files.delete(tempFile.toPath());
    }
    @Test
    void testContainsTextInFileWhenFileContainsTextThenReturnTrue() throws Exception {
        File tempFile = Files.createTempFile("tempFile", ".txt").toFile();
        Files.write(tempFile.toPath(), "This is a test".getBytes());

        boolean result = (boolean) this.containsTextInFile.invoke(visitor, tempFile.toPath());

        assertTrue(result);

        Files.delete(tempFile.toPath());
    }


    @Test
    void testContainsTextInZippedFileWhenIOExceptionOccursThenReturnsFalse() throws Exception {
        when(mockZipFile.getInputStream(mockZipEntry)).thenThrow(new IOException());

        boolean result = (boolean) this.containsTextInZipFile.invoke(visitor, mockZipFile, mockZipEntry);

        assertFalse(result);
    }

    @Test
    void testContainsTextInZippedFileWhenTextIsPresentThenReturnsTrue() throws Exception {
        InputStream mockInputStream = new ByteArrayInputStream("test".getBytes());
        when(mockZipFile.getInputStream(mockZipEntry)).thenReturn(mockInputStream);

        boolean result = (boolean) this.containsTextInZipFile.invoke(visitor, mockZipFile, mockZipEntry);

        assertTrue(result);
    }

    @Test
    void testContainsTextInZippedFileWhenTextIsNotPresentThenReturnsFalse() throws Exception {
        InputStream mockInputStream = new ByteArrayInputStream("".getBytes());
        when(mockZipFile.getInputStream(mockZipEntry)).thenReturn(mockInputStream);

        boolean result = (boolean) this.containsTextInZipFile.invoke(visitor, mockZipFile, mockZipEntry);

        assertFalse(result);
    }

    @Test
    void testGetFileSizeShouldReturnCorrectNumber() throws InvocationTargetException, IllegalAccessException, IOException {

        long result = (long) this.getFileSize.invoke(visitor, testFile.toPath());

        assertEquals(Files.size(this.testFile.toPath()),result);
    }

    @Test
    void testIsFileValidShouldReturnFalseIfFileIsNotExist() throws InvocationTargetException, IllegalAccessException {

        boolean result = (boolean) this.isFileValid.invoke(visitor, Path.of(PATH+"invalid"));

        assertFalse(result);
    }
    @Test
    void testIsFileValidShouldReturnFalseIfFileIsNotReadable() throws InvocationTargetException, IllegalAccessException {

        this.testFile.setReadable(false);

        boolean result = (boolean) this.isFileValid.invoke(visitor, testFile.toPath());

        assertFalse(result);
    }
    @Test
    void testIsFileValidShouldReturnTrueIfFileIsValid() throws InvocationTargetException, IllegalAccessException {

        this.testFile.setReadable(true);

        boolean result = (boolean) this.isFileValid.invoke(visitor, testFile.toPath());

        assertTrue(result);
    }

    @Test
    void testIsFileArchiveShouldReturnFalseIfFileIsSignatureIsTooShort() throws IllegalAccessException, InvocationTargetException {

        boolean result = (boolean) this.isFileArchive.invoke(visitor, mockFile);

        assertFalse(result);
    }

    @Test
    void testIsFileArchiveShouldReturnFalseIfFileIsNotArchive() throws IllegalAccessException, InvocationTargetException {

        boolean result = (boolean) this.isFileArchive.invoke(visitor, testFile);

        assertFalse(result);
    }

    @Test
    void testGetFileContainsText() {
        List<Path> files = visitor.getFileContainsText();

        assertNotNull(files);
    }
}