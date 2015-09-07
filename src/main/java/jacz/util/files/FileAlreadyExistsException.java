package jacz.util.files;

import java.io.IOException;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 24-ago-2008<br>
 * Last Modified: 24-ago-2008
 */
public class FileAlreadyExistsException extends IOException {

    public FileAlreadyExistsException() {
        super();
    }

    public FileAlreadyExistsException(String message) {
        super(message);
    }
}
