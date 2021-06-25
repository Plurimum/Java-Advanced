package info.kgeorgiy.ja.lihanov.walk;

public interface Walker {
    void run();

    boolean isIncorrectArguments(final String[] args);

    boolean cantInitPaths(final String inputPath, final String outputPath);

    default void run(String[] args) {
        if (isIncorrectArguments(args) || cantInitPaths(args[0], args[1])) {
            return;
        }
        run();
    }
}