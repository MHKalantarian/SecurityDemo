package com.iptsco.interview.util;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.core.util.Pair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Enumeration;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import static com.iptsco.interview.util.Constants.AES_NOPAD_TRANS;
import static com.iptsco.interview.util.Constants.ANDROID_KEYSTORE;
import static com.iptsco.interview.util.Constants.KEY_ALIAS;

public class KeystoreWrapper {

    /**
     * Create and initialize a KeyStore
     *
     * @return Initialized KeyStore
     */
    private KeyStore createKeyStore() throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        return keyStore;
    }

    /**
     * Creates symmetric key and store it in the KeyStore
     */
    private void createSymmetricKey() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
        KeyGenParameterSpec keyParams = new KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build();
        keyGenerator.init(keyParams);
    }

    /**
     * Create or get the symmetric key if exists
     *
     * @return Symmetric key
     */
    private SecretKey getSymmetricKey() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException, CertificateException, IOException {
        KeyStore keyStore = createKeyStore();
        if (!keyExists(keyStore))
            createSymmetricKey();

        return (SecretKey) keyStore.getKey(KEY_ALIAS, null);
    }

    /**
     * Checks if a key exists with the same alias
     *
     * @param keyStore KeyStore you want to check
     * @return Whether the key exists or not
     */
    private boolean keyExists(KeyStore keyStore) throws KeyStoreException {
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            return KEY_ALIAS.equals(aliases.nextElement());
        }
        return false;
    }

    /**
     * Encrypts the encoded plain data using the symmetric key and store it with the initialization vector
     *
     * @param plain Plain data
     * @return Pair<Initialization vector, Ciphered data>
     */
    public Pair<byte[], byte[]> encrypt(String plain) throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchProviderException, KeyStoreException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(AES_NOPAD_TRANS);
        cipher.init(Cipher.ENCRYPT_MODE, getSymmetricKey());
        byte[] vector = Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP).getBytes(StandardCharsets.UTF_8);
        byte[] data = Base64.encodeToString(cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)), Base64.NO_WRAP).getBytes(StandardCharsets.UTF_8);
        return new Pair<>(vector, data);
    }

    /**
     * Decrypts and decodes the encrypted data using the symmetric key and the initialization vector that's stored in the Pair
     *
     * @param encryptedData Pair<Initialization vector, Ciphered data>
     * @return Plain data
     */
    public String decrypt(Pair<byte[], byte[]> encryptedData) throws BadPaddingException, IllegalBlockSizeException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException, NoSuchPaddingException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(AES_NOPAD_TRANS);
        cipher.init(Cipher.DECRYPT_MODE, getSymmetricKey(), new GCMParameterSpec(128, Base64.decode(encryptedData.first, Base64.NO_WRAP)));
        return new String(cipher.doFinal(Base64.decode(encryptedData.second, Base64.NO_WRAP)), StandardCharsets.UTF_8);
    }
}
