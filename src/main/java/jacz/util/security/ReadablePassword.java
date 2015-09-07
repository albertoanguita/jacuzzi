package jacz.util.security;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 20-ago-2008<br>
 * Last Modified: 20-ago-2008
 */
public final class ReadablePassword extends Password {

    public ReadablePassword() {
        super();
    }

    public ReadablePassword(String psw) {
        super(psw);
    }

    public String getPsw() {
        return psw;
    }
}
