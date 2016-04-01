package jacz.util.files;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Methods for generating file names
 * todo remove
 */
public class FileGeneratorRemove {

    public static String createNonExistingFilePathWithIndex(String dir, String baseFileName, String extension, boolean startWithoutIndex) throws IOException {
        return createNonExistingFilePathWithIndex(dir, baseFileName, extension, startWithoutIndex, null);
    }

    public static String createNonExistingFilePathWithIndex(String dir, String baseFileName, String extension, boolean startWithoutIndex, SimpleDateFormat simpleDateFormat) throws IOException {
        return createNonExistingFilePathWithIndex(dir, baseFileName, extension, "", "", startWithoutIndex, simpleDateFormat);
    }

    public static String createNonExistingFilePathWithIndex(String dir, String baseFileName, String extension, String preIndex, String postIndex, boolean startWithoutIndex) throws IOException {
        return createNonExistingFilePathWithIndex(dir, baseFileName, extension, preIndex, postIndex, startWithoutIndex, null);
    }

    public static String createNonExistingFilePathWithIndex(String dir, String baseFileName, String extension, String preIndex, String postIndex, boolean startWithoutIndex, SimpleDateFormat simpleDateFormat) throws IOException {
        List<String> baseFileNameList = new ArrayList<>(1);
        baseFileNameList.add(baseFileName);
        List<String> extensionList = new ArrayList<>(1);
        extensionList.add(extension);
        return createNonExistingFilePathWithIndex(dir, baseFileNameList, extensionList, preIndex, postIndex, startWithoutIndex, simpleDateFormat).get(0);
    }

    public static List<String> createNonExistingFilePathWithIndex(String dir, List<String> baseFileNameList, List<String> extensionList, boolean startWithoutIndex) throws IOException, IllegalArgumentException {
        return createNonExistingFilePathWithIndex(dir, baseFileNameList, extensionList, startWithoutIndex, null);
    }

    public static List<String> createNonExistingFilePathWithIndex(String dir, List<String> baseFileNameList, List<String> extensionList, boolean startWithoutIndex, SimpleDateFormat simpleDateFormat) throws IOException, IllegalArgumentException {
        return createNonExistingFilePathWithIndex(dir, baseFileNameList, extensionList, "", "", startWithoutIndex, simpleDateFormat);
    }

    public static List<String> createNonExistingFilePathWithIndex(String dir, List<String> baseFileNameList, List<String> extensionList, String preIndex, String postIndex, boolean startWithoutIndex) throws IOException, IllegalArgumentException {
        return createNonExistingFilePathWithIndex(dir, baseFileNameList, extensionList, preIndex, postIndex, startWithoutIndex, null);
    }

    public static List<String> createNonExistingFilePathWithIndex(String dir, List<String> baseFileNameList, List<String> extensionList, String preIndex, String postIndex, boolean startWithoutIndex, SimpleDateFormat simpleDateFormat) throws IOException, IllegalArgumentException {
        if (!FileGenerator.isDirectory(dir)) {
            throw new IOException(dir + " is not a valid directory");
        }
        if (baseFileNameList.size() != extensionList.size()) {
            throw new IllegalArgumentException("Invalid lists");
        }
        for (int i = 0; i < extensionList.size(); i++) {
            if (extensionList.get(i).startsWith(FileGenerator.FILE_EXTENSION_SEPARATOR)) {
                extensionList.set(i, extensionList.get(i).substring(1));
            }
        }
        int index;
        if (startWithoutIndex) {
            index = -1;
        } else {
            index = 0;
        }
        // look for a correct configuration by increasing the index values
        ArrayList<String> generatedPaths = new ArrayList<>(baseFileNameList.size());
        while (true) {
            try {
                generatedPaths.clear();
                boolean allPathsGood = true;
                for (int i = 0; i < baseFileNameList.size(); i++) {
                    String filePath = FileGenerator.generatePath(generateFileName(baseFileNameList.get(i), preIndex, index, postIndex, extensionList.get(i), simpleDateFormat), dir);
                    generatedPaths.add(filePath);
                    if (FileGenerator.isFile(filePath)) {
                        allPathsGood = false;
                        break;
                    }
                }
                if (allPathsGood) {
                    for (String path : generatedPaths) {
                        File file = new File(path);
                        if (!file.createNewFile()) {
                            throw new IOException("could not create file: " + path);
                        }
                    }
                    return generatedPaths;
                } else {
                    index++;
                }
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid dir: " + dir);
            }
        }
    }

    private static String generateFileName(String baseFileName, String preIndex, int index, String postIndex, String extension, SimpleDateFormat simpleDateFormat) {
        if (index >= 0) {
            return baseFileName + preIndex + index + postIndex + generateDate(simpleDateFormat) + FileGenerator.FILE_EXTENSION_SEPARATOR + extension;
        } else {
            return baseFileName + generateDate(simpleDateFormat) + FileGenerator.FILE_EXTENSION_SEPARATOR + extension;
        }
    }

    public static String createNonExistingDirPathWithIndex(String containerDir, String baseDirName, String preIndex, String postIndex, boolean startWithoutIndex, SimpleDateFormat simpleDateFormat) throws IOException {
        if (!FileGenerator.isDirectory(containerDir)) {
            throw new IOException(containerDir + " is not a valid directory");
        }
        int index;
        if (startWithoutIndex) {
            index = -1;
        } else {
            index = 0;
        }
        // we must generate a path with index (either required by user, or already exists without index)
        while (true) {
            String dirPath = FileGenerator.generatePath(generateFileName(baseDirName, preIndex, index, postIndex, "", simpleDateFormat), containerDir);
            if (!FileGenerator.isDirectory(dirPath)) {
                FileGenerator.createDirectory(dirPath);
                return dirPath;
            } else {
                index++;
            }
        }
    }

    public static String createNonExistingFileNameWithIndex(String dir, String baseFileName, String extension, boolean startWithoutIndex) throws IOException {
        return FileGenerator.getFileName(createNonExistingFilePathWithIndex(dir, baseFileName, extension, startWithoutIndex));
    }

    public static String createNonExistingFileNameWithIndex(String dir, String baseFileName, String extension, boolean startWithoutIndex, SimpleDateFormat simpleDateFormat) throws IOException {
        return FileGenerator.getFileName(createNonExistingFilePathWithIndex(dir, baseFileName, extension, startWithoutIndex, simpleDateFormat));
    }

    public static String createNonExistingFileNameWithIndex(String dir, String baseFileName, String extension, String preIndex, String postIndex, boolean startWithoutIndex) throws IOException {
        return FileGenerator.getFileName(createNonExistingFilePathWithIndex(dir, baseFileName, extension, preIndex, postIndex, startWithoutIndex));
    }

    public static String createNonExistingFileNameWithIndex(String dir, String baseFileName, String extension, String preIndex, String postIndex, boolean startWithoutIndex, SimpleDateFormat simpleDateFormat) throws IOException {
        return FileGenerator.getFileName(createNonExistingFilePathWithIndex(dir, baseFileName, extension, preIndex, postIndex, startWithoutIndex, simpleDateFormat));
    }

    public static List<String> createNonExistingFileNameWithIndex(String dir, List<String> baseFileNameList, List<String> extensionList, boolean startWithoutIndex) throws IOException, IllegalArgumentException {
        return createNonExistingFileNameWithIndex(dir, baseFileNameList, extensionList, startWithoutIndex, null);
    }

    public static List<String> createNonExistingFileNameWithIndex(String dir, List<String> baseFileNameList, List<String> extensionList, boolean startWithoutIndex, SimpleDateFormat simpleDateFormat) throws IOException, IllegalArgumentException {
        return createNonExistingFileNameWithIndex(dir, baseFileNameList, extensionList, "", "", startWithoutIndex, simpleDateFormat);
    }

    public static List<String> createNonExistingFileNameWithIndex(String dir, List<String> baseFileNameList, List<String> extensionList, String preIndex, String postIndex, boolean startWithoutIndex) throws IOException, IllegalArgumentException {
        return createNonExistingFileNameWithIndex(dir, baseFileNameList, extensionList, preIndex, postIndex, startWithoutIndex, null);
    }

    public static List<String> createNonExistingFileNameWithIndex(String dir, List<String> baseFileNameList, List<String> extensionList, String preIndex, String postIndex, boolean startWithoutIndex, SimpleDateFormat simpleDateFormat) throws IOException, IllegalArgumentException {
        List<String> filePathList = createNonExistingFilePathWithIndex(dir, baseFileNameList, extensionList, preIndex, postIndex, startWithoutIndex, simpleDateFormat);
        for (int i = 0; i < filePathList.size(); i++) {
            filePathList.set(i, FileGenerator.getFileName(filePathList.get(i)));
        }
        return filePathList;
    }

    private static String generateDate(SimpleDateFormat simpleDateFormat) {
        if (simpleDateFormat != null) {
            return simpleDateFormat.format(new Date());
        } else {
            return "";
        }
    }
}
