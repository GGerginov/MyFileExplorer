package org.example;

import org.example.finder.TextFinder;
import org.example.utill.FileGenerator;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter a valid path: ");
        String path = scanner.nextLine();

        System.out.print("Enter a text to search: ");
        String textToSearch = scanner.nextLine();

        FileGenerator.generateFiles(path,10);

        System.out.print(System.lineSeparator());

        TextFinder.findText(textToSearch, path).forEach(f -> System.out.printf("%s %d %n",f.getFileName(),f.toFile().length()));
    }


}