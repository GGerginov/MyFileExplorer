package org.example.finder;

import org.example.visitor.FileTextRecognitionVisitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * The TextFinder class is a utility class that allows searching for a specific text in files within a given directory.
 */
public class TextFinder {

    /**
     * Private constructor to prevent instantiation of the utility class.
     */
    private TextFinder() {
        throw new IllegalStateException("Utility class");
    }


    /**
     * Finds files within the specified directory that contain the given text.
     *
     * @param textToSearch the text to search for
     * @param path         the path of the directory to search in
     * @return a list of paths to files that contain the text
     * @throws IllegalArgumentException if the text is null or empty
     */
    public static List<Path> findText(String textToSearch, String path) {

        if(textToSearch == null || textToSearch.isEmpty()){
            throw new IllegalArgumentException("Your text is not valid");
        }

        FileTextRecognitionVisitor fileTextRecognitionVisitor = new FileTextRecognitionVisitor(textToSearch);

        try {
            Files.walkFileTree(Path.of(path),fileTextRecognitionVisitor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return fileTextRecognitionVisitor.getFileContainsText();
    }
}
