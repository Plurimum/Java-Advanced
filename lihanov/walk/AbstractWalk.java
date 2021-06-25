package info.kgeorgiy.ja.lihanov.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractWalk implements Walker {

    // :NOTE: Доступ?
    Path inputPath;
    Path outputPath;

    @Override
    public void run() {
        try (final BufferedReader reader = Files.newBufferedReader(inputPath)) {
            try (final BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                try {
                    String stringPath;
                    while ((stringPath = reader.readLine()) != null) {
                        walk(writer, stringPath);
                    }
                } catch (final IOException e) {
                    System.err.println("Can't read file from list of files: " + e.getMessage());
                }
            } catch (final IOException e) {
                System.err.println("Can't open output file " + e.getMessage());
            }
        } catch (final IOException e) {
            System.err.println("Can't read input file " + e.getMessage());
        }
    }

    public abstract void walk(final BufferedWriter writer, final String stringPath) throws IOException;

    public boolean isIncorrectArguments(final String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Launch format: java Walk <input file> <output file>");
            return true;
        } else {
            return false;
        }
    }

    public boolean cantInitPaths(final String inputPath, final String outputPath) {
        try {
            this.inputPath = Paths.get(inputPath);
        } catch (final InvalidPathException e) {
            System.err.println("Incorrect path of input file: " + inputPath);
            return true;
        }
        try {
            this.outputPath = Paths.get(outputPath);
        } catch (final InvalidPathException e) {
            System.err.println("Incorrect path of output file: " + outputPath);
            return true;
        }
        return false;
    }
}
