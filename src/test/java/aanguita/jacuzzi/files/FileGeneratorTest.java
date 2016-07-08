package aanguita.jacuzzi.files;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tests for the file generator class
 */
public class FileGeneratorTest {

    private static String tests = "etc/tests";
    private static Path testsPath = Paths.get(tests);

    private static void setup() throws IOException {
        Files.createDirectories(testsPath);
        FileUtil.cleanDirectory(tests);
    }

    @Test
    public void testCreateDirectoryWithIndex() throws Exception {
        setup();

        FileGenerator.createDirectoryWithIndex(tests, "d", "(", ")", true);
        Assert.assertTrue(Files.isDirectory(testsPath.resolve("d")));
        FileGenerator.createDirectoryWithIndex(tests, "d", "(", ")", true);
        Assert.assertTrue(Files.isDirectory(testsPath.resolve("d(0)")));
        FileGenerator.createDirectoryWithIndex(tests, "d", "(", ")", true);
        Assert.assertTrue(Files.isDirectory(testsPath.resolve("d(1)")));
        Files.delete(testsPath.resolve("d(0)"));
        Assert.assertFalse(Files.isDirectory(testsPath.resolve("d(0)")));
        FileGenerator.createDirectoryWithIndex(tests, "d", "(", ")", true);
        Assert.assertTrue(Files.isDirectory(testsPath.resolve("d(0)")));
    }

    @Test
    public void testCreateFile() throws Exception {
        setup();

        Files.createFile(testsPath.resolve("file.txt"));
        String newFile = FileGenerator.createFile(tests, "file", "txt", true);
        Assert.assertEquals(tests + "/file0.txt", newFile);
        Assert.assertTrue(Files.exists(testsPath.resolve(Paths.get("file0.txt"))));

        newFile = FileGenerator.createFile(tests, "file", "txt", true);
        Assert.assertEquals(tests + "/file1.txt", newFile);
        Assert.assertTrue(Files.exists(testsPath.resolve(Paths.get("file1.txt"))));
    }

    @Test
    public void testCreateFile1() throws Exception {
        setup();

        Files.createFile(testsPath.resolve("file.txt"));
        String newFile = FileGenerator.createFile(tests, "file", "txt", "(", ")", true);
        Assert.assertEquals(tests + "/file(0).txt", newFile);
        Assert.assertTrue(Files.exists(testsPath.resolve(Paths.get("file(0).txt"))));

        newFile = FileGenerator.createFile(tests, "file", "txt", "(", ")", true);
        Assert.assertEquals(tests + "/file(1).txt", newFile);
        Assert.assertTrue(Files.exists(testsPath.resolve(Paths.get("file(1).txt"))));
    }

    @Test
    public void testCreateFiles() throws Exception {
        setup();

        Files.createFile(testsPath.resolve("file.txt"));
        List<String> list = FileGenerator.createFiles(tests, "file", Stream.of("tmp", "txt", "bak").collect(Collectors.toList()), "(", ")", true);
        Assert.assertEquals(Stream.of(tests + "/file(0).tmp", tests + "/file(0).txt", tests + "/file(0).bak").collect(Collectors.toList()), list);
        Assert.assertTrue(Files.exists(testsPath.resolve(Paths.get("file(0).tmp"))));
        Assert.assertTrue(Files.exists(testsPath.resolve(Paths.get("file(0).txt"))));
        Assert.assertTrue(Files.exists(testsPath.resolve(Paths.get("file(0).bak"))));

        list = FileGenerator.createFiles(tests, "file", Stream.of("tmp", "txt", "bak").collect(Collectors.toList()), "(", ")", true);
        Assert.assertEquals(Stream.of(tests + "/file(1).tmp", tests + "/file(1).txt", tests + "/file(1).bak").collect(Collectors.toList()), list);
        Assert.assertTrue(Files.exists(testsPath.resolve(Paths.get("file(1).tmp"))));
        Assert.assertTrue(Files.exists(testsPath.resolve(Paths.get("file(1).txt"))));
        Assert.assertTrue(Files.exists(testsPath.resolve(Paths.get("file(1).bak"))));
    }
}