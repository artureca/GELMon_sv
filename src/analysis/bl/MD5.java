/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.bl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Copiada diretamente do link em baixo
 * @author http://www.java2s.com/Code/Java/Security/UseMD5toencryptastring.htm
 */
public class MD5 {
    private static MessageDigest digester;

    static {
        try {
            digester = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            //e.printStackTrace();
        }
    }

    /**
     * Encripta uma password em MD5
     * @param str password a encriptar
     * @return Password Encriptada
     */
    public static String crypt(String str) {
        if (str == null || str.length() == 0) {
            throw new IllegalArgumentException("String to encript cannot be null or zero length");
        }

        digester.update(str.getBytes());
        byte[] hash = digester.digest();
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            if ((0xff & hash[i]) < 0x10) {
                hexString.append("0").append(Integer.toHexString((0xFF & hash[i])));
            }
            else {
                hexString.append(Integer.toHexString(0xFF & hash[i]));
            }
        }
        return hexString.toString();
    }
}
