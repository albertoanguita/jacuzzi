package jacz.util.files;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;

/**
 * File system utility methods
 */
public class FileUtilExtended {

    public static boolean isEmpty(String dir) throws IOException {
        return isEmpty(Paths.get(dir));
    }

    public static boolean isEmpty(Path dir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            return !stream.iterator().hasNext();
        }
    }

    public static void cleanDirectory(String dir) throws IOException, InvalidPathException {
        cleanDirectory(Paths.get(dir));
    }

    public static void cleanDirectory(Path dir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file: stream) {
                if (Files.isDirectory(file)) {
                    cleanDirectory(file);
                }
                Files.delete(file);
            }
        } catch (DirectoryIteratorException x) {
            throw new IOException(x);
        }
    }

    public static void deleteDirectoryAndContents(String dir) throws IOException {
        deleteDirectoryAndContents(Paths.get(dir));
    }

    public static void deleteDirectoryAndContents(Path dir) throws IOException {
        cleanDirectory(dir);
        Files.delete(dir);
    }

    /**
     * Transforms a given route using a mapping of directories
     *
     * @param route  the route to modify
     * @param mapDir the mappings of directories
     * @return the new route
     */
    public static String transformRoute(String route, Map<String, String> mapDir) {
        return transformRoute(Paths.get(route), mapDir).toString();
    }

    /**
     * Transforms a given route using a mapping of directories
     *
     * @param path   the path to modify
     * @param mapDir the mappings of directories
     * @return the new route
     */
    public static Path transformRoute(Path path, Map<String, String> mapDir) {
        Path resultPath = path.getRoot() != null ? Paths.get(path.getRoot().toUri()) : null;
        for (int i = 0; i < path.getNameCount(); i++) {
            String newName = mapDir.containsKey(path.getName(i).toString()) ?
                    mapDir.get(path.getName(i).toString()) :
                    path.getName(i).toString();
            if (resultPath == null) {
                resultPath = Paths.get(newName);
            } else {
                resultPath = resultPath.resolve(newName);
            }
        }
        return resultPath;
    }
}
