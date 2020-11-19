package com.iptsco.interview.util;

import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import static com.iptsco.interview.util.Constants.AES_CBC_PKCS7PAD;

/**
 * Created by MHK on 11/19/2020.
 * www.MHKSoft.com
 */
public class AESWrapper {
    private final IvParameterSpec ivParameterSpec;
    private final SecretKeySpec secretKeySpec;

    /**
     * Generates key from passphrase to use in AES/CBC
     *
     * @param passPhrase CBC pass phrase
     */
    public AESWrapper(String passPhrase) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Secure salt & iv to prevent hacker from establishing the initial state
        SecureRandom sr = SecureRandom.getInstanceStrong();

        byte[] salt = new byte[256];
        sr.nextBytes(salt);

        byte[] iv = new byte[16];
        sr.nextBytes(iv);

        ivParameterSpec = new IvParameterSpec(iv);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, 1234, 256);
        byte[] keyBytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(pbeKeySpec).getEncoded();
        secretKeySpec = new SecretKeySpec(keyBytes, KeyProperties.KEY_ALGORITHM_AES);
    }

    /**
     * Encrypts the encoded plain data using the symmetric key and store it with the initialization vector
     *
     * @param plain Plain data
     * @return Pair<Initialization vector, Ciphered data>
     */
    public byte[] encrypt(String plain) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS7PAD);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        return Base64.encodeToString(cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)), Base64.NO_WRAP).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Decrypts and decodes the encrypted data using the symmetric key and the initialization vector that's stored in the Pair
     *
     * @param encryptedData Pair<Initialization vector, Ciphered data>
     * @return Plain data
     */
    public String decrypt(byte[] encryptedData) throws BadPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS7PAD);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        return new String(cipher.doFinal(Base64.decode(encryptedData, Base64.NO_WRAP)), StandardCharsets.UTF_8);
    }
}
