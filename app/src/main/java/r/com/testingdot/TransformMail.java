package r.com.testingdot;

/**
 * Created by rufotamo on 21/05/2018.
 */

public class TransformMail {
    public static String transformMail(String mail){
        String nMail = null;
        nMail = mail.replace(".","_");
        return nMail;
    }
}
