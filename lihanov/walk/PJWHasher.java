package info.kgeorgiy.ja.lihanov.walk;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PJWHasher {
    private static long PJWOperation(final byte byteRead, long hash) {
        hash = (hash << 8) + (byteRead & 0xFF);
        final long high = hash & 0xFF00000000000000L;
        if (high != 0) {
            hash ^= high >> 48;
        }
        hash &= ~high;
        return hash;
    }

    public static long getHashOfFile(final Path path) {
        long hash = 0;
        try (final BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(path))) {
            int countOfByteRead;
            final byte[] bytes = new byte[1024];
            while ((countOfByteRead = bufferedInputStream.read(bytes)) >= 0) {
                for (int i = 0; i < countOfByteRead; i++) {
                    hash = PJWOperation(bytes[i], hash);
                }
            }
        } catch (final IOException e) {
            hash = 0;
        }
        return hash;
    }
}
