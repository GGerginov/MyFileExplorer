package org.example.visitor;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class FileTextRecognitionVisitor extends SimpleFileVisitor<Path> {

    private final List<Path> validFiles;
    private final String textToSearch;

    public FileTextRecognitionVisitor(String textToSearch) {
        this.validFiles = new ArrayList<>();
        this.textToSearch = textToSearch;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

        if (isValid(file)) {
            validFiles.add(file);
        }

        return super.visitFile(file, attrs);
    }

    private boolean isValid(Path file) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file.toFile()))) {
            return bufferedReader.lines().anyMatch(line -> line.contains(this.textToSearch));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private long getFileSize(Path file) {
        try {
            return Files.size(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Path> getValidFiles() {

        this.validFiles.sort(Comparator.comparingLong(this::getFileSize));

        return this.validFiles;
    }
}
