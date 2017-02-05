package org.aanguita.jacuzzi.io.files;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility operations for copying, moving, etc files.
 */
public class FileGenerator {

    public static final String FILE_EXTENSION_SEPARATOR = ".";

    private final static int[] ILLEGAL_FILENAME_CHARS = {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47};

    private final static int LEGAL_FILENAME_CHAR_REPLACEMENT = (int) '_';

    static {
        Arrays.sort(ILLEGAL_FILENAME_CHARS);
    }

    public static String createDirectoryWithIndex(String containerDir, String name, String preIndex, String postIndex, boolean startWithoutIndex) throws IOException {
        return createDirectoryWithIndex(Paths.get(containerDir), name, preIndex, postIndex, startWithoutIndex).toString();
    }

    public static synchronized Path createDirectoryWithIndex(Path dir, String name, String preIndex, String postIndex, boolean startWithoutIndex) throws IOException {
        int index = startWithoutIndex ? -1 : 0;
        // we must generate a path with index (either required by user, or already exists without index)
        while (true) {
            Path dirPath;
            if (index == -1) {
                dirPath = Paths.get(dir.toString(), name);
            } else {
                dirPath = Paths.get(dir.toString(), name + preIndex + index + postIndex);
            }
            if (!Files.isDirectory(dirPath)) {
                Files.createDirectories(dirPath);
                return dirPath;
            } else {
                index++;
            }
        }
    }

    public static String createFile(String dir, String name, String extension, boolean startWithoutIndex) throws IOException {
        return createFile(Paths.get(dir), name, extension, startWithoutIndex).toString();
    }

    public static Path createFile(Path dir, String name, String extension, boolean startWithoutIndex) throws IOException {
        return createFile(dir, name, extension, null, null, startWithoutIndex);
    }

    public static String createFile(String dir, String name, String extension, String preIndex, String postIndex, boolean startWithoutIndex) throws IOException {
        return createFile(Paths.get(dir), name, extension, preIndex, postIndex, startWithoutIndex).toString();
    }

    public static Path createFile(Path dir, String name, String extension, String preIndex, String postIndex, boolean startWithoutIndex) throws IOException {
        return createFiles(dir, name, Stream.of(extension).collect(Collectors.toList()), preIndex, postIndex, startWithoutIndex).get(0);
    }

    public static synchronized List<String> createFiles(@NotNull String dir, @NotNull String name, @NotNull List<String> extensions, String preIndex, String postIndex, boolean startWithoutIndex) throws IOException, IllegalArgumentException {
        List<Path> paths = createFiles(Paths.get(dir), name, extensions, preIndex, postIndex, startWithoutIndex);
        return paths.stream().map(Path::toString).collect(Collectors.toList());
    }

    public static synchronized List<Path> createFiles(@NotNull Path dir, @NotNull String name, @NotNull List<String> extensions, String preIndex, String postIndex, boolean startWithoutIndex) throws IOException, IllegalArgumentException {
        if (!Files.isDirectory(dir)) {
            throw new IOException(dir + " is not a valid directory");
        }
        if (extensions.isEmpty()) {
            throw new IllegalArgumentException("Invalid extensions");
        }
        // add file extension separator
        extensions = extensions.stream().map(extension -> extension.startsWith(FILE_EXTENSION_SEPARATOR) ? extension.substring(1) : extension).collect(Collectors.toList());
        preIndex = preIndex != null ? preIndex : "";
        postIndex = postIndex != null ? postIndex : "";
        int index = startWithoutIndex ? -1 : 0;
        List<Path> generatedPaths = new ArrayList<>(extensions.size());
        while (true) {
            generatedPaths.clear();
            boolean allPathsGood = true;
            for (String extension : extensions) {
                Path newPath;
                if (index == -1) {
                    newPath = dir.resolve(name + FILE_EXTENSION_SEPARATOR + extension);
                } else {
                    newPath = dir.resolve(name + preIndex + index + postIndex + FILE_EXTENSION_SEPARATOR + extension);
                }
                generatedPaths.add(newPath);
                if (Files.exists(newPath)) {
                    allPathsGood = false;
                    break;
                }
            }
            if (allPathsGood) {
                for (Path newPath : generatedPaths) {
                    Files.createFile(newPath);
                }
                return generatedPaths;
            } else {
                index++;
            }
        }
    }

    /**
     * Sanitizes a file name for any OS. Illegal characters are replaced by an underscore.
     * (algorithm copied from http://stackoverflow.com/questions/1155107/is-there-a-cross-platform-java-method-to-remove-filename-special-chars)
     *
     * @param filename original filename
     * @return sanitized filename
     */
    public static String sanitizeFilenameXPlatform(String filename) {
        StringBuilder cleanName = new StringBuilder();
        for (int i = 0; i < filename.length(); i++) {
            int c = (int) filename.charAt(i);
            if (Arrays.binarySearch(ILLEGAL_FILENAME_CHARS, c) < 0) {
                // legal char
                cleanName.append((char) c);
            } else {
                cleanName.append(LEGAL_FILENAME_CHAR_REPLACEMENT);
            }
        }
        return cleanName.toString();
    }
}
