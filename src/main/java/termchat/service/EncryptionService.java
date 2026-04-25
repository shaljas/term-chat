package termchat.service;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class EncryptionService {

    /*
    Password hashing using SHA1
    https://stackoverflow.com/questions/4895523/java-string-to-sha1#:~:text=This%20is%20my%20solution%20of%20converting%20string%20to%20sha1%2E%20It%20works%20well%20in%20my%20Android%20app
     */
    public static String encryptPassword(String password)
    {
        String sha1 = "";
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(password.getBytes("UTF-8"));
            sha1 = byteToHex(crypt.digest());
        }
        catch(NoSuchAlgorithmException e)
        {
            System.out.println("Algorithm exception: " + e.getMessage());
        }
        catch(UnsupportedEncodingException e)
        {
            System.out.println("Unsupported encoding exception: " + e.getMessage());
        }
        return sha1;
    }

    private static String byteToHex(final byte[] hash)
    {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}
