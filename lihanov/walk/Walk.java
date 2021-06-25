package info.kgeorgiy.ja.lihanov.walk;

import java.io.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class Walk extends AbstractWalk {

    public static void main(String[] args) {
        new Walk().run(args);
    }

    @Override
    public void walk(final BufferedWriter writer, final String stringPath) throws IOException {
        long hash;
        try {
            hash = PJWHasher.getHashOfFile(Paths.get(stringPath));
        } catch (final InvalidPathException e) {
            System.err.println("Incorrect path of file from list of files: " + stringPath);
            hash = 0;
        }
        writer.write(String.format("%016x %s%n", hash, stringPath));
    }
}
