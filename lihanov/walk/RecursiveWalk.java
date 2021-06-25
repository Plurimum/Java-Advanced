package info.kgeorgiy.ja.lihanov.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;

public class RecursiveWalk extends AbstractWalk{

    public static void main(final String[] args) {
        final Walker walker = new RecursiveWalk();
        if (walker.isIncorrectArguments(args) || walker.cantInitPaths(args[0], args[1])) {
            return;
        }
        walker.run();
    }

    @Override
    public void walk(final BufferedWriter writer, final String stringPath) throws IOException {
        try {
            // :NOTE: Новый на строку
            final FileVisitor fileVisitor = new FileVisitor(writer);
            final Path pathOfFile = Paths.get(stringPath);
            Files.walkFileTree(pathOfFile, fileVisitor);
        } catch (final InvalidPathException e) {
            System.err.println("Incorrect path of file from list of files: " + stringPath);
            writer.write(String.format("%016x %s%n", 0, stringPath));
        }
    }
}
