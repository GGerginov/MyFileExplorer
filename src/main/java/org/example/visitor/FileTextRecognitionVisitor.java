package org.example.visitor;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * The FileTextRecognitionVisitor class is a visitor implementation that traverses a file system hierarchy and
 * identifies files that contain a specific text. It supports searching for text in regular files as well as
 * compressed files (e.g., ZIP files).
 */
public class FileTextRecognitionVisitor extends SimpleFileVisitor<Path> {

    private static final List<byte[]> ARCHIVE_SIGNATURE = List.of(
            new byte[] { 0x50, 0x4B, 0x03, 0x04 },
            new byte[] { 0x37, 0x7A, (byte) 0xBC, (byte) 0xAF, 0x27, 0x1C },
            new byte[] { 0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00 },
            new byte[] { 0x1F, (byte) 0x8B, 0x08 },
            new byte[] { 0x42, 0x5A, 0x68 },
            new byte[] { 0x75, 0x73, 0x74, 0x61, 0x72 },
            new byte[] { 0x50, 0x4B, 0x03, 0x04 },
            new byte[] { 0x1F, (byte) 0x8B, 0x08 },
            new byte[] { 0x20, 0x00, 0x08, 0x00 },
            new byte[] { 20, 0, 8, 0}
    );
    /**
     * The list of files that contain the specified text.
     */
    private final List<Path> fileContainsText;
    /**
     * The text to search for in the files.
     */
    private final String textToSearch;

    /**
     * Constructs a new FileTextRecognitionVisitor with the specified text to search.
     *
     * @param textToSearch the text to search for in the files
     */
    public FileTextRecognitionVisitor(String textToSearch) {
        this.fileContainsText = new ArrayList<>();
        this.textToSearch = textToSearch;
    }

    /**
     * Visits a file and checks if it contains the specified text. If the file is an archive (e.g., ZIP file),
     * it is handled accordingly.
     *
     * @param file  the file to visit
     * @param attrs the file attributes
     * @return the visit result
     * @throws IOException if an I/O error occurs
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

        if (Files.isRegularFile(file)){

            if (isFileArchive(file.toFile())){
                this.fileContainsText.addAll(processCompressedFile(file));
            }
            else if (isFileValid(file) && containsTextInFile(file)) {
                this.fileContainsText.add(file);
            }
        }
        return super.visitFile(file, attrs);
    }

    /**
     * Handles a compressed file by checking for matches within the file and processing any directories within it.
     *
     * @param compressedFilePath the path of the compressed file
     */
    private List<Path> processCompressedFile(Path compressedFilePath) {
        List<Path> processedFiles = new ArrayList<>();

        processedFiles.addAll(processCompressedFileForMatch(compressedFilePath));

        try (ZipFile zipFile = new ZipFile(compressedFilePath.toFile())) {

            zipFile.stream()
                    .filter(ZipEntry::isDirectory)
                    .forEach(entry -> {
                        try {
                            Path entryPath = Paths.get(entry.getName());

                            Files.copy(zipFile.getInputStream(entry), entryPath, StandardCopyOption.REPLACE_EXISTING);

                            Files.walkFileTree(entryPath, this);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return processedFiles;
    }

    private List<Path> processCompressedFileForMatch(Path compressedFilePath) {

        List<Path> result = new ArrayList<>();

        try (ZipFile zipFile = new ZipFile(compressedFilePath.toFile())) {
            zipFile.stream()
                    .filter(entry -> !entry.isDirectory())
                    .forEach(entry -> {
                        if (containsTextInZippedFile(zipFile,entry)) result.add(compressedFilePath);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Checks if a file contains the specified text.
     *
     * @param file the file to check
     * @return true if the file contains the text, false otherwise
     */
    private boolean containsTextInFile(Path file) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file.toFile()))) {
            return bufferedReader.lines().anyMatch(line -> line.contains(this.textToSearch));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Checks if a zipped file contains the specified text.
     *
     * @param zipFile the ZIP file
     * @param entry   the ZIP entry
     * @return true if the zipped file contains the text, false otherwise
     */
    private boolean containsTextInZippedFile(ZipFile zipFile, ZipEntry entry) {
        boolean flag = false;
        try (InputStream inputStream = zipFile.getInputStream(entry);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(textToSearch)) {
                    flag = true;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return flag;
    }

    /**
     * Retrieves the size of a file.
     *
     * @param file the file
     * @return the size of the file in bytes
     */
    private long getFileSize(Path file) {
        try {
            return Files.size(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Checks if a file is valid by verifying its readability and existence.
     *
     * @param path the path of the file
     * @return true if the file is valid, false otherwise
     */
    private boolean isFileValid(Path path) {

        File file = path.toFile();

        return file.canRead() && file.exists();
    }

    /**
     * Checks if a file is an archive by examining its signature.
     *
     * @param file the file
     * @return true if the file is an archive, false otherwise
     */
    private static boolean isFileArchive(File file) {

        if (file.length() < 4) {
            // File is too small to contain a signature
            return false;
        }

        byte[] fileSignature = new byte[4];
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.read(fileSignature);
        } catch (IOException e) {
            e.printStackTrace();
        }


        for (byte[] signature : ARCHIVE_SIGNATURE) {
            if (Arrays.equals(fileSignature, signature)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the list of files that contain the specified text. The list is sorted based on the file size.
     *
     * @return the list of files that contain the text
     */
    public List<Path> getFileContainsText() {

        this.fileContainsText.sort(Comparator.comparingLong(this::getFileSize));

        return fileContainsText;
    }
}
