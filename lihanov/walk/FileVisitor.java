package info.kgeorgiy.ja.lihanov.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileVisitor extends SimpleFileVisitor<Path> {

    private final BufferedWriter writer;

    FileVisitor(final BufferedWriter bufferedWriter) {
        this.writer = bufferedWriter;
    }

    @Override
    public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException {
        writer.write(String.format("%016x %s%n", PJWHasher.getHashOfFile(path), path));
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(final Path path, final IOException exc) throws IOException {
        writer.write(String.format("%016x %s%n", 0, path));
        return FileVisitResult.CONTINUE;
    }
}