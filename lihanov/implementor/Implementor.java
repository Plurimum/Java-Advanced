package info.kgeorgiy.ja.lihanov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * Simple implementation of {@link Impler} interface.
 * <p>
 * Implements method {@link Impler#implement(Class, Path)} only for interfaces. Provides methods to generate them.
 *
 * @author Maksim Lihanov
 */
public class Implementor implements JarImpler {

    /**
     * Opening curly brace token.
     */
    private static final String CURLY_BRACE_OPEN = "{";
    /**
     * Closing curly brace token.
     */
    private static final String CURLY_BRACE_CLOSE = "}";
    /**
     * Semicolon token.
     */
    private static final String SEMICOLON = ";";
    /**
     * Opening bracket token.
     */
    private static final String BRACKET_OPEN = "(";
    /**
     * Closing bracket token.
     */
    private static final String BRACKET_CLOSE = ")";
    /**
     * Implemented class suffix token.
     */
    private static final String CLASS_SUFFIX = "Impl";
    /**
     * Java file suffix token.
     */
    private static final String FILE_SUFFIX = ".java";
    /**
     * Default value of boolean token.
     */
    private static final String BOOLEAN_DEFAULT_VALUE = "false";
    /**
     * Default value of non primitive types token.
     */
    private static final String NON_PRIMITIVE_DEFAULT_VALUE = "null";
    /**
     * Default value of number types token.
     */
    private static final String NUMBER_DEFAULT_VALUE = "0";
    /**
     * Keyword {@code class} token.
     */
    private static final String CLASS_KEYWORD = "class ";
    /**
     * Keyword {@code implements} token.
     */
    private static final String IMPLEMENTS_KEYWORD = " implements ";
    /**
     * Space token.
     */
    private static final String SPACE = " ";
    /**
     * Prefix of argument token.
     */
    private static final String ARGUMENT_PREFIX = "arg";
    /**
     * Comma token.
     */
    private static final String COMMA = ", ";
    /**
     * Keyword {@code return} token.
     */
    private static final String RETURN_KEYWORD = "return ";
    /**
     * Keyword {@code throws} token.
     */
    private static final String THROWS_KEYWORD = "throws ";
    /**
     * Dot token.
     */
    protected static final Character DOT = '.';

    /**
     * Default constructor.
     */
    public Implementor() {
    }

    /**
     * Main method which provides the console interface.
     * Usage:<p>
     * [interface full name] [root] <p>
     * [-jar] [interface full name] [{@code <file name>}.jar]
     *
     * @param args an arguments which entered to command line
     */
    public static void main(String[] args) {
        if (args.length != 2 && args.length != 3) {
            System.out.println("Usage:\n1) Simple implement arguments: [interface full name] [root]\n" +
                    "2) Jar implement arguments: [-jar] [interface full name] [<file name>.jar]");
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                System.out.println("Arguments should not be null");
                return;
            }
        }
        Implementor jarImplementor = new Implementor();
        boolean jarMode = args.length == 3;
        if (jarMode) {
            args[0] = args[1];
            args[1] = args[2];
        }
        try {
            Class<?> token = Class.forName(args[0]);
            Path path = Paths.get(args[1]);
            if (jarMode) {
                jarImplementor.implementJar(token, path);
            } else {
                jarImplementor.implement(token, path);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("There is no class '" + args[0] + "'");
        } catch (InvalidPathException e) {
            System.err.println("Invalid path: " + args[1]);
        } catch (ImplerException e) {
            System.err.println("Error during implementation: " + e.getMessage());
        }
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (Modifier.isFinal(token.getModifiers()) ||
                Modifier.isPrivate(token.getModifiers()) ||
                !token.isInterface()
        ) {
            throw new ImplerException("Unsupported type");
        } else {
            try (final BufferedWriter writer = Files.newBufferedWriter(createPath(token, root))) {
                writer.write(implementClass(token));
            } catch (IOException e) {
                throw new ImplerException("Can't write output file: " + e.getMessage());
            }
        }
    }

    /**
     * Creates {@link Path} from {@code root} to implementation file of {@code token} with class suffix
     * {@value CLASS_SUFFIX} and file suffix {@value FILE_SUFFIX}.
     *
     * @param token type token of implementation file.
     * @param root  root directory.
     * @return {@link Path} of class implementation.
     */
    public static Path getClassImplementationPath(Class<?> token, Path root) {
        String filePath = token.getPackageName().replace(DOT, File.separatorChar);
        return root.resolve(filePath).resolve(token.getSimpleName() + CLASS_SUFFIX + FILE_SUFFIX);
    }

    /**
     * Creates missing parent directories for implementation path and returns produced class implementation path.
     * Gets class implementation path from {@link Implementor#getClassImplementationPath(Class, Path)}.
     * If implementation path has parent directories creates them.
     *
     * @param token type token of implementation file.
     * @param root  root directory.
     * @return {@link Path} where implementation should be created.
     * @throws ImplerException when parent directories cannot be created.
     */
    private static Path createPath(Class<?> token, Path root) throws ImplerException {
        root = getClassImplementationPath(token, root);
        Path parentPath = root.getParent();
        if (parentPath != null) {
            try {
                Files.createDirectories(parentPath);
            } catch (IOException e) {
                throw new ImplerException("Can't create output directory", e);
            }
        }
        return root;
    }

    /**
     * Converts given {@code string} to Unicode escaping.
     *
     * @param string a {@link String} which converting is needed.
     * @return a formatted {@link String}.
     */
    private static String formatToCorrect(String string) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char symbol = string.charAt(i);
            stringBuilder.append(symbol < 128 ? symbol : "\\u" + String.format("%04x", (int) symbol));
        }
        return stringBuilder.toString();
    }

    /**
     * Creates class implementation in a {@link String} representation. Joining together results of methods
     * {@link Implementor#getPackageString(Class)},
     * {@link Implementor#createClassDeclaration(Class)},
     * {@link Implementor#createMethods(Class)}.
     *
     * @param token interface token which implementation is required.
     * @return {@link String} representation of implemented class.
     */
    private static String implementClass(Class<?> token) {
        return formatToCorrect(getPackageString(token) +
                createClassDeclaration(token) +
                createMethods(token) +
                CURLY_BRACE_CLOSE
        );
    }

    /**
     * Creates {@link String} representation of package of given interface.
     * If package of this interface is null returns empty {@link String}.
     *
     * @param token type token to create package directive for.
     * @return string directive of package.
     */
    private static String getPackageString(Class<?> token) {
        Package pckg = token.getPackage();
        if (pckg == null) {
            return "";
        } else {
            return pckg.toString() + SEMICOLON;
        }
    }

    /**
     * Creates {@link String} representation of declaration of implemented class with suffix {@value CLASS_SUFFIX}.
     *
     * @param token interface token to create implemented class declaration for.
     * @return {@link String} of class declaration.
     */
    private static String createClassDeclaration(Class<?> token) {
        return CLASS_KEYWORD +
                token.getSimpleName() +
                CLASS_SUFFIX +
                IMPLEMENTS_KEYWORD +
                token.getCanonicalName() +
                CURLY_BRACE_OPEN;
    }

    /**
     * Creates {@link String} declaration for given {@code method}. Concatenates the {@link Modifier},
     * the return type taken with {@link Method#getReturnType()},
     * the {@code method} name by {@link Method#getName()},
     * the arguments taken with {@link Implementor#getMethodArgs(Method)} and
     * the exceptions taken with {@link Implementor#getExceptions(Method)} of the given {@code method}.
     *
     * @param method a {@link Method} which declaration is required.
     * @return a {@link String} of declaration for given {@code method}.
     */
    private static String createMethodDeclaration(Method method) {
        return Modifier.toString(method.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT) +
                SPACE +
                method.getReturnType().getCanonicalName() +
                SPACE +
                method.getName() +
                BRACKET_OPEN +
                getMethodArgs(method) +
                BRACKET_CLOSE +
                getExceptions(method) +
                CURLY_BRACE_OPEN;
    }

    /**
     * Extracts the argument types of the given {@code method} and
     * creates a {@link String} from the generated arguments for the implemented {@link Method}.
     *
     * @param method a {@link Method} which arguments needs to get in {@link String} representation.
     * @return a {@link String} of arguments separated with {@value COMMA}.
     */
    private static String getMethodArgs(Method method) {
        AtomicInteger id = new AtomicInteger();
        return Arrays.stream(method.getParameterTypes())
                .map(type -> type.getCanonicalName() + SPACE + ARGUMENT_PREFIX + (id.getAndIncrement()))
                .collect(Collectors.joining(COMMA));
    }

    /**
     * Creates a {@link String} from the methods generated for the implemented class.
     * Extracts methods from {@code token} by {@link Class#getMethods()} and creates {@link String}
     * by converting them with {@link Implementor#createMethod(Method)} and collects them by
     * {@link java.util.stream.Stream#collect(Collector)}.
     *
     * @param token interface token to create implemented class methods for.
     * @return {@link String} representation of methods of implemented class.
     */
    private static String createMethods(Class<?> token) {
        return Arrays.stream(token.getMethods()).map(Implementor::createMethod).collect(Collectors.joining());
    }

    /**
     * Creates {@link String} of full given {@code method} description. Concatenates the results of
     * {@link Implementor#createMethodDeclaration(Method)} and
     * {@link Implementor#createMethodBody(Method)}.
     *
     * @param method a {@link Method} which {@link String} representation is needed.
     * @return {@link String} concatenation of method declaration and method body.
     */
    private static String createMethod(Method method) {
        return createMethodDeclaration(method) + createMethodBody(method) + CURLY_BRACE_CLOSE;
    }

    /**
     * Creates {@link String} of thrown exceptions for given {@code method}. If given
     * {@code method} has no throwing exceptions returns an empty {@link String}, else
     * returns a {@link String} with concatenation separated by {@value COMMA}
     * of keyword {@value THROWS_KEYWORD} and canonical names of exception types
     * by using {@link Method#getExceptionTypes()} and {@link Class#getCanonicalName()}.
     *
     * @param method a {@link Method} which {@link String} representation of exceptions needs to get.
     * @return an exceptions of given {@code method} in {@link String} representation.
     */
    private static String getExceptions(Method method) {
        Class<?>[] exceptionTypes = method.getExceptionTypes();
        if (exceptionTypes.length == 0) {
            return "";
        } else {
            return THROWS_KEYWORD + Arrays.stream(exceptionTypes)
                    .map(Class::getCanonicalName)
                    .collect(Collectors.joining(COMMA));
        }
    }

    /**
     * Creates the body of given {@code method}. The generated method body ignores its arguments
     * and only returns the default values. For example, method body for
     * the {@code int foo(int a, int b)} method would be: {@code {return 0;}}.
     *
     * @param method the {@link Method} which {@link String} representation body is needed.
     * @return {@link String} of the body of given {@code method} with default return value.
     */
    private static String createMethodBody(Method method) {
        Class<?> returnType = method.getReturnType();
        String returnString = RETURN_KEYWORD;
        if (!returnType.isPrimitive()) {
            returnString += NON_PRIMITIVE_DEFAULT_VALUE;
        } else if (returnType.equals(boolean.class)) {
            returnString += BOOLEAN_DEFAULT_VALUE;
        } else if (!returnType.equals(void.class)) {
            returnString += NUMBER_DEFAULT_VALUE;
        }
        return returnString + SEMICOLON;
    }

    /**
     * Slash token.
     */
    private static final Character SLASH = '/';
    /**
     * Manifest version token.
     */
    private static final String VERSION = "1.0.0";
    /**
     * Suffix of implemented class file.
     */
    private static final String CLASS_FILE_SUFFIX = "Impl.class";

    /**
     * Default constructor.
     */

    @Override
    public void implementJar(Class<?> token, Path jarTargetPath) throws ImplerException {
        Path root = jarTargetPath.getParent();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new ImplerException("Can't create directories");
        }
        Path implementationPath;
        try {
            implementationPath = Files.createTempDirectory(root, "tmpDir");
        } catch (IOException e) {
            throw new ImplerException("Can't create temp directory");
        }
        try {
            implement(token, implementationPath);
            compile(token, implementationPath);
            buildJar(token, jarTargetPath, implementationPath);
        } catch (ImplerException e) {
            throw new ImplerException("Error during implementation: " + e.getMessage());
        } finally {
            deleteDirectories(implementationPath);
        }
    }

    /**
     * Compiles the {@code .java} file of given {@code token}.
     *
     * @param token              the interface which implementation compilation is needed.
     * @param implementationPath the temporary directory for classes.
     * @throws ImplerException if implementation cannot be compiled.
     */
    private void compile(Class<?> token, Path implementationPath) throws ImplerException {
        String path = getClassImplementationPath(token, implementationPath).toString();
        String sourcePath;
        try {
            sourcePath = Paths.get(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (URISyntaxException e) {
            throw new ImplerException("URI syntax error: " + e.getMessage());
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String[] arguments = {
                "-cp",
                implementationPath.toString() + File.pathSeparator + sourcePath,
                path
        };
        int compilationResult = compiler.run(null, null, null, arguments);
        if (compilationResult != 0) {
            throw new ImplerException("Compilation error. Code: " + compilationResult);
        }
    }

    /**
     * Creates {@code .jar} file with compiled implemented class of given {@code token}. Write a files from {@code filesPath}
     * to {@code .jar} file of given {@code jarTargetPath} by using a {@link JarOutputStream}.
     *
     * @param token         the interface token which {@link Class} should be implemented,
     *                      compiled and packed in {@code .jar} file.
     * @param jarTargetPath a {@link Path} to {@code .jar} file which should be generated.
     * @param filesPath     a directory with files to write.
     * @throws ImplerException if an error occurs during building {@code .jar} file.
     */
    private void buildJar(Class<?> token, Path jarTargetPath, Path filesPath) throws ImplerException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, VERSION);
        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarTargetPath), manifest)) {
            String jarFile = token.getPackageName().replace(DOT, SLASH) +
                    SLASH +
                    token.getSimpleName() +
                    CLASS_FILE_SUFFIX;
            try {
                jarOutputStream.putNextEntry(new ZipEntry(jarFile));
            } catch (IOException e) {
                throw new ImplerException("Can't put entry");
            }
            try {
                Files.copy(Paths.get(filesPath.toString(), jarFile), jarOutputStream);
            } catch (IOException e) {
                throw new ImplerException("Can't copy file");
            }
        } catch (IOException e) {
            throw new ImplerException("Can't initialize JarOutputStream");
        }
    }

    /**
     * Deletes all contents of directory specified by {@code directory}.
     * @param directory {@link Path} target directory to delete.
     * @throws ImplerException if can not delete temporary directories.
     */
    private static void deleteDirectories(Path directory) throws ImplerException {
        File[] files = directory.toFile().listFiles();
        if (files != null) {
            for (File subFile : files) {
                deleteDirectories(subFile.toPath());
            }
        }
        try {
            Files.delete(directory);
        } catch (IOException e) {
            throw new ImplerException("Failed to delete temporary directories.");
        }
    }
}