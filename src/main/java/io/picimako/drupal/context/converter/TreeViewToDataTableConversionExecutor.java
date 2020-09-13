package io.picimako.drupal.context.converter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Initiates the conversion from Tree View based component layouts to Table View based ones.
 */
public final class TreeViewToDataTableConversionExecutor {

    /**
     * Invoke the conversion on the contents of the argument source files, and writes them to separate result files.
     *
     * @param args filenames only in the directory the jar file is placed, without any absolute or relative paths
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("No command line argument were specified. At least one file name must be defined.");
        }
        TreeViewToDataTableConverter converter = new TreeViewToDataTableConverter();
        String targetFolder = targetFolder();
        for (String convertibleFileName : args) {
            System.out.println("Converting file: " + convertibleFileName);
            writeToFile(newFileNameFrom(targetFolder, convertibleFileName), converter.convert(readInput(targetFolder, convertibleFileName)));
        }
        System.out.println("Conversion of all specified files has finished!");
    }

    /**
     * Returns the target folder where the result file have to be saved, or in other words, returns the folder path
     * that the standalone jar file is executed from.
     */
    private static String targetFolder() {
        try {
            String path = Paths.get(TreeViewToDataTableConverter.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
            return path.endsWith(".jar") ? path.substring(0, path.lastIndexOf(File.separator) + 1) : path;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("There was a problem with the executed jar's path.", e);
        }
    }

    /**
     * Writes the argument String content to the file at {@code filePath}.
     */
    private static void writeToFile(String filePath, String convertedContent) {
        try {
            Files.writeString(Paths.get(filePath), convertedContent);
        } catch (IOException e) {
            throw new IllegalArgumentException("Something went wrong or was misconfigured during writing the converted contents to the target file.", e);
        }
    }

    /**
     * Assembles the file name for the result file in a system dependent way.
     */
    private static String newFileNameFrom(String targetFolder, String fileName) {
        return targetFolder + "converted_" + fileName;
    }

    /**
     * Reads the contents of the argument file and returns as the collection of lines from that file.
     */
    private static List<String> readInput(String targetFolder, String fileName) {
        String sourceFilePath = targetFolder + fileName;
        try {
            return Files.readAllLines(Paths.get(sourceFilePath));
        } catch (IOException e) {
            throw new IllegalArgumentException("Something went wrong or was misconfigured during reading the source file for conversion.", e);
        }
    }
}
