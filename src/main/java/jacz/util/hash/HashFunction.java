package jacz.util.hash;

import jacz.util.notification.ProgressNotification;
import jacz.util.numeric.NumericUtil;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Easy implementation of hash methods, algorithm independent
 */
public class HashFunction {

    private MessageDigest messageDigest;

    private final Integer hashLength;

    public HashFunction(String algorithm) throws NoSuchAlgorithmException {
        this(algorithm, null);
    }

    public HashFunction(String algorithm, Integer hashLength) throws NoSuchAlgorithmException {
        initialize(algorithm);
        this.hashLength = hashLength;
    }

    protected HashFunction(Integer hashLength) {
        messageDigest = null;
        this.hashLength = hashLength;
    }

    protected void initialize(String algorithm) throws NoSuchAlgorithmException {
        messageDigest = MessageDigest.getInstance(algorithm);
    }

    public String getAlgorithm() {
        return messageDigest.getAlgorithm();
    }

    public Integer getHashLength() {
        return hashLength;
    }

    public HashFunction update(byte[] data) {
        messageDigest.update(data);
        return this;
    }

    public HashFunction update(String str) {
        if (str != null) {
            update(str.getBytes());
        }
        return this;
    }

    public HashFunction update(Object o) {
        if (o != null) {
            update(o.toString());
        }
        return this;
    }

    public void update(File f) throws IOException {
        update(f, null);
    }

    public void update(File file, ProgressNotification<Integer> progressNotification) throws IOException {
        if (file == null) {
            return;
        }
        if (!file.exists()) {
            throw new FileNotFoundException(file.toString());
        }
        long totalLength = file.length();
        long length = 0;
        int bytesRead;
        long percentage = 0;
        InputStream is = new FileInputStream(file);
        DigestInputStream dis = new DigestInputStream(is, messageDigest);
        byte[] buffer = new byte[8192];
        while ((bytesRead = dis.read(buffer)) != -1) {
            // keep reading file and report progress
            if (progressNotification != null) {
                length += bytesRead;
                long newPercentage = NumericUtil.displaceInRange(length, 0L, totalLength, 0L, 100L);
                if (newPercentage > percentage) {
                    percentage = newPercentage;
                    progressNotification.addNotification((int) percentage);
                }
            }
        }
        dis.close();
        is.close();
        if (progressNotification != null) {
            progressNotification.completeTask();
        }
    }

    public byte[] digest() {
        return adjustLength(messageDigest.digest(), hashLength);
    }

    public byte[] digest(byte[] data) {
        return adjustLength(messageDigest.digest(data), hashLength);
    }

    private static byte[] adjustLength(byte[] data, Integer length) {
        if (length == null || length == data.length) {
            return data;
        } else {
            return Arrays.copyOf(data, length);
        }
    }

    public byte[] digest(String str) {
        if (str != null) {
            return digest(str.getBytes());
        } else {
            return digest();
        }
    }

    public byte[] digest(Object o) {
        if (o != null) {
            return digest(o.toString());
        } else {
            return digest();
        }
    }

    public byte[] digest(File f) throws IOException {
        update(f);
        return digest();
    }

    public String digestAsHex() {
        return asHex(digest());
    }

    public String digestAsHex(byte[] data) {
        return asHex(digest(data));
    }

    public String digestAsHex(String str) {
        return digestAsHex(str.getBytes());
    }

    public String digestAsHex(Object o) {
        return digestAsHex(o.toString());
    }

    public String digestAsHex(File f) throws IOException {
        return asHex(digest(f));
    }

    public static String asHex(byte[] data) {
        return new HexBinaryAdapter().marshal(data);
    }

    public static byte[] asBinary(String str) {
        return new HexBinaryAdapter().unmarshal(str);
    }
}
