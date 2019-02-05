package org.open.oauth2.service;


import org.open.oauth2.Utils;
import org.open.oauth2.config.OpenOAuth2ConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Encryptor {

    @Autowired
    private OpenOAuth2ConfigurationProperties configurationProperties;

    public final String KEY_IN_HEX = "9eb82722b77f29b21beb11c2c1026008";
    private final String ENCRYPTION_ALGORITHM;
    private SecretKey KEY;
    private final String DIGEST_ALGORITHM;

    public Encryptor() throws NoSuchAlgorithmException {
        this.ENCRYPTION_ALGORITHM = "AES";
        this.DIGEST_ALGORITHM = "SHA-256";
    }

    @PostConstruct
    public void init() {
        this.KEY = new SecretKeySpec(
                Utils.toByte(configurationProperties.getWebconsole().getEncryptionKeyInHex()), "AES");
    }

    public String encrypt(String in) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, KEY);
        byte[] b = cipher.doFinal(in.getBytes());
        String d = Utils.toHex(b);
        return d;
    }


    public String decript(String in) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, KEY);
        byte[] a = Utils.toByte(in);
        byte[] c = cipher.doFinal(a);
        String d = new String(c);
        return d;
    }

    public String digest(String in) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(DIGEST_ALGORITHM);
        byte[] b = digest.digest(in.getBytes());
        byte[] c = Base64.getEncoder().encode(b);
        String d = new String(c);
        return d;
    }

    public static String generateKeyInHex() throws NoSuchAlgorithmException {
        return Utils.toHex(KeyGenerator.getInstance("AES").generateKey().getEncoded());
    }

    public static void main(String[] args) throws Exception {
        Encryptor e = new Encryptor();
        String s = e.digest("resource-owner-1");
        System.out.println(s);
    }
}
