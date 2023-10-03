package org.example.utill;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The FileGenerator class is a utility class that generates files in a specified directory.
 */
public class FileGenerator {

    /**
     * Private constructor to prevent instantiation of the utility class.
     */
    private FileGenerator() {
        throw new IllegalStateException("Utility class");
    }


    /**
     * Generates files in the specified directory.
     *
     * @param directoryPath the path of the directory to generate files in
     * @param numFiles      the number of files to generate
     * @return true if the files are successfully generated, false otherwise
     */
    public static boolean generateFiles(String directoryPath, int numFiles) {
        try {
            for (int i = 1; i <= numFiles; i++) {
                String fileName = "file" + i + (i % 2 == 0 ? ".txt" : ".xml");
                String filePath = directoryPath + "/" + fileName;

                BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

                if (i % 2 == 0) {
                    writer.write("demo");
                } else {
                    writer.write("This is a sample file for testing. Lorem ipsum");
                }
                writer.newLine();
                writer.write("Version: 1.0.0");
                writer.close();

                System.out.println("Generated file: " + filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}