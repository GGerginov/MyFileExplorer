package org.example.visitor;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class FileTextRecognitionVisitor extends SimpleFileVisitor<Path> {

    private static final List<String> ARCHIVE_SIGNATURE = List.of("0x504B0304","0x377ABCAF271C","0x526172211A0700"
            ,"0x1F8B08","0x425A68","0x7573746172","0x504B0304","0x1F8B08");
    private final List<Path> fileContainsText;
    private final String textToSearch;

    public FileTextRecognitionVisitor(String textToSearch) {
        this.fileContainsText = new ArrayList<>();
        this.textToSearch = textToSearch;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

        if (Files.isRegularFile(file)){

            if (isArchive(file.toFile())){
                handleCompressedFile(file);
            }
            else if (isValid(file) && isContains(file)) {
                fileContainsText.add(file);
            }
        }
        return super.visitFile(file, attrs);
    }

    private void handleCompressedFile(Path compressedFilePath) {

        checkCompressedFileForMatch(compressedFilePath);

        checkCompressedFileForDir(compressedFilePath);
    }

    private void checkCompressedFileForDir(Path compressedFilePath) {
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
    }

    private void checkCompressedFileForMatch(Path compressedFilePath) {
        try (ZipFile zipFile = new ZipFile(compressedFilePath.toFile())) {
            zipFile.stream()
                    .filter(entry -> !entry.isDirectory())
                    .forEach(entry -> {
                        if (isContainsZipped(zipFile,entry)) fileContainsText.add(compressedFilePath);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isContains(Path file) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file.toFile()))) {
            return bufferedReader.lines().anyMatch(line -> line.contains(this.textToSearch));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isContainsZipped(ZipFile zipFile, ZipEntry entry) {
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

    private long getFileSize(Path file) {
        try {
            return Files.size(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private boolean isValid(Path path) {

        File file = path.toFile();

        return file.canRead() && file.exists();
    }

    private static boolean isArchive(File f) {

        if (f.length() < 4) {
            // File is too small to contain a signature
            return false;
        }

        int fileSignature = 0;
        try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            fileSignature = raf.readInt();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ARCHIVE_SIGNATURE.contains(String.valueOf(fileSignature));
    }

    public List<Path> getFileContainsText() {

        this.fileContainsText.sort(Comparator.comparingLong(this::getFileSize));

        return fileContainsText;
    }
}
