package org.example.finder;

import org.example.visitor.FileTextRecognitionVisitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TextFinder {

    private TextFinder() {
        throw new IllegalStateException("Utility class");
    }

    public static List<Path> findText(String textToSearch, String path) {
        FileTextRecognitionVisitor fileTextRecognitionVisitor = new FileTextRecognitionVisitor(textToSearch);

        try {
            Files.walkFileTree(Path.of(path),fileTextRecognitionVisitor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return fileTextRecognitionVisitor.getValidFiles();
    }
}
