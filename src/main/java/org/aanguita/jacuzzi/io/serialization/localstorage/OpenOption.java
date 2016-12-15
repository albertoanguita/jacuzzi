package org.aanguita.jacuzzi.io.serialization.localstorage;

import java.nio.file.LinkOption;

/**
 * Created by Alberto on 14/12/2016.
 */
public enum OpenOption {

    /**
     * Open for read access.
     */
    READ,

    /**
     * Open for write access.
     */
    WRITE,

    /**
     * Create a new file if it does not exist.
     * This option is ignored if the {@link #CREATE_NEW} option is also set.
     * The check for the existence of the file and the creation of the file
     * if it does not exist is atomic with respect to other file system
     * operations.
     */
    CREATE,

    /**
     * Create a new file, failing if the file already exists.
     * The check for the existence of the file and the creation of the file
     * if it does not exist is atomic with respect to other file system
     * operations.
     */
    CREATE_NEW,

    /**
     * Sparse file. When used with the {@link #CREATE_NEW} option then this
     * option provides a <em>hint</em> that the new file will be sparse. The
     * option is ignored when the file system does not support the creation of
     * sparse files.
     */
    SPARSE,
}
