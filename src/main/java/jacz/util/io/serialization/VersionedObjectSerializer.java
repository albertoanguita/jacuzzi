package jacz.util.io.serialization;

import jacz.util.files.FileReaderWriter;
import jacz.util.hash.CRC;
import jacz.util.hash.CRCMismatchException;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Class with static methods for saving and restoring objects implementing the VersionedObject interface
 */
public class VersionedObjectSerializer {

    private static final byte[] STRING_TYPE = Serializer.serialize("String");
    private static final byte[] BOOLEAN_TYPE = Serializer.serialize("Boolean");
    private static final byte[] BYTE_TYPE = Serializer.serialize("Byte");
    private static final byte[] SHORT_TYPE = Serializer.serialize("Short");
    private static final byte[] INTEGER_TYPE = Serializer.serialize("Integer");
    private static final byte[] LONG_TYPE = Serializer.serialize("Long");
    private static final byte[] FLOAT_TYPE = Serializer.serialize("Float");
    private static final byte[] DOUBLE_TYPE = Serializer.serialize("Double");
    private static final byte[] ENUM_TYPE = Serializer.serialize("Enum");
    private static final byte[] BYTE_ARRAY_TYPE = Serializer.serialize("ByteArray");
    private static final byte[] SERIALIZABLE_TYPE = Serializer.serialize("Serializable");

    public static void serialize(List<? extends VersionedObject> versionedObjectList, String path, String... backupPaths) throws IOException {
        FragmentedByteArray byteArray = new FragmentedByteArray();
        for (VersionedObject versionedObject : versionedObjectList) {
            byteArray.add(serialize(versionedObject));
        }
        byte[] data = byteArray.generateArray();
        FileReaderWriter.writeBytes(path, data);
        for (String backupPath : backupPaths) {
            FileReaderWriter.writeBytes(backupPath, data);
        }
    }

    public static void serialize(VersionedObject versionedObject, String path, String... backupPaths) throws IOException {
        byte[] data = serialize(versionedObject);
        FileReaderWriter.writeBytes(path, data);
        for (String backupPath : backupPaths) {
            FileReaderWriter.writeBytes(backupPath, data);
        }
    }

    public static void serialize(List<? extends VersionedObject> versionedObjectList, int CRCBytes, String path, String... backupPaths) throws IOException {
        FragmentedByteArray byteArray = new FragmentedByteArray();
        for (VersionedObject versionedObject : versionedObjectList) {
            byteArray.add(serialize(versionedObject, CRCBytes));
        }
        byte[] data = byteArray.generateArray();
        FileReaderWriter.writeBytes(path, data);
        for (String backupPath : backupPaths) {
            FileReaderWriter.writeBytes(backupPath, data);
        }
    }

    public static void serialize(VersionedObject versionedObject, int CRCBytes, String path, String... backupPaths) throws IOException {
        byte[] data = serialize(versionedObject, CRCBytes);
        FileReaderWriter.writeBytes(path, data);
        for (String backupPath : backupPaths) {
            FileReaderWriter.writeBytes(backupPath, data);
        }
    }

    public static byte[] serialize(List<? extends VersionedObject> versionedObjectList) throws NotSerializableException {
        FragmentedByteArray byteArray = new FragmentedByteArray();
        for (VersionedObject versionedObject : versionedObjectList) {
            byteArray.add(serialize(versionedObject));
        }
        return byteArray.generateArray();
    }

    public static byte[] serialize(VersionedObject versionedObject) throws NotSerializableException {
        return serialize(versionedObject, 0);
    }

    public static byte[] serialize(List<? extends VersionedObject> versionedObjectList, int CRCBytes) throws NotSerializableException {
        FragmentedByteArray byteArray = new FragmentedByteArray();
        for (VersionedObject versionedObject : versionedObjectList) {
            byteArray.add(serialize(versionedObject, CRCBytes));
        }
        return byteArray.generateArray();
    }

    public static byte[] serialize(VersionedObject versionedObject, int CRCBytes) throws NotSerializableException {
        FragmentedByteArray data = new FragmentedByteArray(Serializer.serializeObject(versionedObject.getCurrentVersion().toArrayList()));
        Map<String, Serializable> attributes = versionedObject.serialize();
        data.add(Serializer.serialize(attributes.size()));
        for (Map.Entry<String, Serializable> entry : attributes.entrySet()) {
            byte[] attributeName = Serializer.serialize(entry.getKey());
            // find the type of the attributes
            byte[] type;
            byte[] attributeArray;
            Object attribute = entry.getValue();
            if (attribute instanceof String) {
                type = STRING_TYPE;
                attributeArray = Serializer.serialize((String) attribute);
            } else if (attribute instanceof Boolean || attribute == null) {
                // null value are serialized as a null Boolean
                type = BOOLEAN_TYPE;
                attributeArray = Serializer.serialize((Boolean) attribute);
            } else if (attribute instanceof Byte) {
                type = BYTE_TYPE;
                attributeArray = Serializer.serialize((Byte) attribute);
            } else if (attribute instanceof Short) {
                type = SHORT_TYPE;
                attributeArray = Serializer.serialize((Short) attribute);
            } else if (attribute instanceof Integer) {
                type = INTEGER_TYPE;
                attributeArray = Serializer.serialize((Integer) attribute);
            } else if (attribute instanceof Long) {
                type = LONG_TYPE;
                attributeArray = Serializer.serialize((Long) attribute);
            } else if (attribute instanceof Float) {
                type = FLOAT_TYPE;
                attributeArray = Serializer.serialize((Float) attribute);
            } else if (attribute instanceof Double) {
                type = DOUBLE_TYPE;
                attributeArray = Serializer.serialize((Double) attribute);
            } else if (attribute instanceof Enum<?>) {
                type = ENUM_TYPE;
                attributeArray = Serializer.addArrays(Serializer.serialize(attribute.getClass().getName()), Serializer.serialize((Enum) attribute));
            } else if (attribute instanceof byte[]) {
                type = BYTE_ARRAY_TYPE;
                attributeArray = Serializer.serialize((byte[]) attribute);
            } else {
                type = SERIALIZABLE_TYPE;
                attributeArray = Serializer.serializeObject((Serializable) attribute);
            }
            data.add(attributeName, type, attributeArray).generateArray();
        }
        return CRC.addCRC(data.generateArray(), CRCBytes, true);
    }

    public static void deserialize(VersionedObject versionedObject, String path, String... backupPaths) throws VersionedSerializationException, IOException {
        deserialize(versionedObject, path, false, backupPaths);
    }

    public static List<String> deserialize(VersionedObject versionedObject, String path, boolean repairIfBroken, String... backupPaths) throws VersionedSerializationException, IOException {
        try {
            byte[] data = FileReaderWriter.readBytes(path);
            deserialize(versionedObject, data);
            return new ArrayList<>();
        } catch (VersionedSerializationException | IOException e) {
            // try with backup
            if (backupPaths.length > 0) {
                String newPath = backupPaths[0];
                backupPaths = Arrays.copyOfRange(backupPaths, 1, backupPaths.length);
                List<String> repairedFiles = deserialize(versionedObject, newPath, repairIfBroken, backupPaths);
                if (repairIfBroken) {
                    Files.copy(Paths.get(newPath), Paths.get(path), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                    repairedFiles.add(path);
                }
                return repairedFiles;
            } else {
                throw e;
            }
        }
    }

    public static void deserialize(VersionedObject versionedObject, byte[] data) throws VersionedSerializationException {
        deserialize(versionedObject, data, new MutableOffset());
    }

    public static void deserialize(VersionedObject versionedObject, byte[] data, MutableOffset offset) throws VersionedSerializationException {
        VersionStack versionStack = null;
        Map<String, Object> attributes = new HashMap<>();
        try {
            data = CRC.extractDataWithCRC(data, offset);
            MutableOffset dataOffset = new MutableOffset();
            versionStack = new VersionStack ((ArrayList<String>) Serializer.deserializeObject(data, dataOffset));
            int attributeCount = Serializer.deserializeIntValue(data, dataOffset);
            for (int i = 0; i < attributeCount; i++) {
                String attributeName = Serializer.deserializeString(data, dataOffset);
                String type = Serializer.deserializeString(data, dataOffset);
                if (type == null) {
                    throw new RuntimeException();
                }
                switch (type) {
                    case "String":
                        attributes.put(attributeName, Serializer.deserializeString(data, dataOffset));
                        break;

                    case "Boolean":
                        attributes.put(attributeName, Serializer.deserializeBoolean(data, dataOffset));
                        break;

                    case "Byte":
                        attributes.put(attributeName, Serializer.deserializeByte(data, dataOffset));
                        break;

                    case "Short":
                        attributes.put(attributeName, Serializer.deserializeShort(data, dataOffset));
                        break;

                    case "Integer":
                        attributes.put(attributeName, Serializer.deserializeInt(data, dataOffset));
                        break;

                    case "Long":
                        attributes.put(attributeName, Serializer.deserializeLong(data, dataOffset));
                        break;

                    case "Float":
                        attributes.put(attributeName, Serializer.deserializeFloat(data, dataOffset));
                        break;

                    case "Double":
                        attributes.put(attributeName, Serializer.deserializeDouble(data, dataOffset));
                        break;

                    case "Enum":
                        String enumType = Serializer.deserializeString(data, dataOffset);
                        Class enumClass = Class.forName(enumType);
                        attributes.put(attributeName, Serializer.deserializeEnum(enumClass, data, dataOffset));
                        break;

                    case "ByteArray":
                        attributes.put(attributeName, Serializer.deserializeBytes(data, dataOffset));
                        break;

                    case "Serializable":
                        attributes.put(attributeName, Serializer.deserializeObject(data, dataOffset));
                        break;

                    default:
                        // the VersionedObjectSerializer failed when deserializing the byte array
                        throw new RuntimeException("Unexpected type: " + type);
                }
            }
            versionedObject.deserialize(versionStack.retrieveVersion(), attributes, versionStack);
        } catch (RuntimeException e) {
            throw new VersionedSerializationException(versionStack, attributes, VersionedSerializationException.Reason.INCORRECT_DATA);
        } catch (UnrecognizedVersionException e) {
            throw new VersionedSerializationException(versionStack, attributes, VersionedSerializationException.Reason.UNRECOGNIZED_VERSION);
        } catch (ClassNotFoundException e) {
            throw new VersionedSerializationException(versionStack, attributes, VersionedSerializationException.Reason.CLASS_NOT_FOUND);
        } catch (CRCMismatchException e) {
            throw new VersionedSerializationException(null, attributes, VersionedSerializationException.Reason.CRC_MISMATCH);
        }
    }
}
