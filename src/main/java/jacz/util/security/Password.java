package jacz.util.security;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 20-ago-2008<br>
 * Last Modified: 20-ago-2008
 */
public abstract class Password {

    public static final String NO_PSW = "";

    protected String psw;


    public Password() {
        this.psw = NO_PSW;
    }

    public Password(String psw) {
        this.psw = psw;
    }

    public boolean isPassword(String psw) {
        return this.psw.compareTo(psw) == 0;
    }

    public boolean hasPassword() {
        return isPassword(NO_PSW);
    }
}
