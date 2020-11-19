package com.iptsco.interview.ui;

import android.os.Bundle;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.iptsco.interview.R;
import com.iptsco.interview.data.viewmodel.MainViewModel;
import com.iptsco.interview.databinding.ActivityMainBinding;
import com.iptsco.interview.util.AESWrapper;
import com.iptsco.interview.util.KeystoreWrapper;
import com.iptsco.interview.util.SharedPreferenceHelper;
import com.iptsco.interview.util.StorageHelper;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static com.iptsco.interview.util.Constants.DATA_FILE_NAME;
import static com.iptsco.interview.util.Constants.FIRST_RUN;
import static com.iptsco.interview.util.Constants.VECTOR_FILE_NAME;

public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private KeystoreWrapper keystoreWrapper;
    private AESWrapper aesWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        keystoreWrapper = new KeystoreWrapper();

        init();
    }

    /**
     * Initiates UI and Observer
     */
    private void init() {
        // Observers
        if (SharedPreferenceHelper.getSharedPreferenceBoolean(mContext, FIRST_RUN, true)) {
            if (!viewModel.getGeneratePasswordLiveData().hasObservers())
                viewModel.getGeneratePasswordLiveData().observe(this, resource -> {
                    switch (resource.getState()) {
                        case SUCCESS:
                            hideLoading();
                            try {
                                savePassword(resource.getPasswords().get(0));
                                SharedPreferenceHelper.setSharedPreferenceBoolean(mContext, FIRST_RUN, false);
                            } catch (IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | NoSuchProviderException | KeyStoreException | BadPaddingException | IllegalBlockSizeException e) {
                                e.printStackTrace();
                                new MaterialAlertDialogBuilder(mContext)
                                        .setTitle(R.string.error_dialog_title)
                                        .setMessage(e.getMessage())
                                        .setPositiveButton(R.string.error_dialog_positive_text, (dialogInterface, i) -> {
                                            viewModel.generatePassword();
                                        })
                                        .setNegativeButton(getString(R.string.error_dialog_negative_text), (dialogInterface, i) -> {
                                            finishAffinity();
                                        })
                                        .show();
                            }
                            break;
                        case ERROR:
                            hideLoading();
                            new MaterialAlertDialogBuilder(mContext)
                                    .setTitle(R.string.error_dialog_title)
                                    .setMessage(resource.getMessage())
                                    .setPositiveButton(R.string.error_dialog_positive_text, (dialogInterface, i) -> {
                                        viewModel.generatePassword();
                                    })
                                    .setNegativeButton(getString(R.string.error_dialog_negative_text), (dialogInterface, i) -> {
                                        finishAffinity();
                                    })
                                    .show();
                            break;
                        case LOADING:
                            showLoading();
                            break;
                    }
                });
            viewModel.generatePassword();
        }

        // OnClicks
        binding.encryptBtn.setOnClickListener(view -> {
            if (hasBiometric())
                biometricAuthentication();
            else {
                try {
                    aesWrapper = new AESWrapper(retrievePassword());
                    binding.cipherIl.getEditText().setText(Base64.encodeToString(encryptPlainText(binding.plainIl.getEditText().getText().toString()), Base64.NO_WRAP));
                    binding.decipherIl.getEditText().setText(aesWrapper.decrypt(Base64.decode(binding.cipherIl.getEditText().getText().toString(), Base64.NO_WRAP)));
                } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | CertificateException | UnrecoverableKeyException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | KeyStoreException | NoSuchProviderException e) {
                    e.printStackTrace();
                    new MaterialAlertDialogBuilder(mContext)
                            .setTitle(R.string.error_dialog_title)
                            .setMessage(e.getMessage())
                            .setNegativeButton(getString(R.string.error_dialog_negative_text), (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            })
                            .show();
                }
            }
        });
    }

    /**
     * Encrypts password
     *
     * @param password Password to be encrypted
     * @return Encrypted password
     */
    private Pair<byte[], byte[]> encryptPassword(String password) throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, KeyStoreException, NoSuchProviderException, IllegalBlockSizeException {
        return keystoreWrapper.encrypt(password);
    }

    /**
     * Encrypts and saves password
     *
     * @param password Password to be saved
     */
    private void savePassword(String password) throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, NoSuchProviderException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, KeyStoreException, IllegalBlockSizeException {
        Pair<byte[], byte[]> encryptedPassword = encryptPassword(password);
        StorageHelper.setDataInStorage(mContext, VECTOR_FILE_NAME, encryptedPassword.first);
        StorageHelper.setDataInStorage(mContext, DATA_FILE_NAME, encryptedPassword.second);
    }

    /**
     * Decrypts password
     *
     * @param encryptedPassword Password to be decrypted
     * @return Decrypted password
     */
    private String decryptPassword(Pair<byte[], byte[]> encryptedPassword) throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, NoSuchProviderException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, KeyStoreException {
        return keystoreWrapper.decrypt(encryptedPassword);
    }

    /**
     * Retrieves password from storage and decrypts it
     *
     * @return Decrypted password
     */
    private String retrievePassword() throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, KeyStoreException, NoSuchProviderException {
        Pair<byte[], byte[]> encryptedPassword = new Pair<>(
                StorageHelper.getDataFromStorage(mContext, VECTOR_FILE_NAME),
                StorageHelper.getDataFromStorage(mContext, DATA_FILE_NAME)
        );
        return decryptPassword(encryptedPassword);
    }

    /**
     * Encrypts plain text
     *
     * @param plainText Text to be encrypted
     */
    private byte[] encryptPlainText(String plainText) {
        try {
            return aesWrapper.encrypt(plainText);
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            new MaterialAlertDialogBuilder(mContext)
                    .setTitle(R.string.error_dialog_title)
                    .setMessage(e.getMessage())
                    .setNegativeButton(getString(R.string.error_dialog_negative_text), (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .show();
        }
        return null;
    }


    /**
     * Checks Biometric availability
     */
    private boolean hasBiometric() {
        return BiometricManager.from(mContext).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS;
    }

    /**
     * Authenticates user via available biometric method
     */
    private void biometricAuthentication() {
        new BiometricPrompt(this, ContextCompat.getMainExecutor(mContext), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                new MaterialAlertDialogBuilder(mContext)
                        .setTitle(R.string.error_dialog_title)
                        .setMessage(errString)
                        .setNegativeButton(getString(R.string.error_dialog_negative_text), (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        })
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                try {
                    aesWrapper = new AESWrapper(retrievePassword());
                    binding.cipherIl.getEditText().setText(Base64.encodeToString(encryptPlainText(binding.plainIl.getEditText().getText().toString()), Base64.NO_WRAP));
                    binding.decipherIl.getEditText().setText(aesWrapper.decrypt(Base64.decode(binding.cipherIl.getEditText().getText().toString(), Base64.NO_WRAP)));
                } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException | IOException | CertificateException | UnrecoverableKeyException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | KeyStoreException e) {
                    e.printStackTrace();
                    new MaterialAlertDialogBuilder(mContext)
                            .setTitle(R.string.error_dialog_title)
                            .setMessage(e.getMessage())
                            .setNegativeButton(getString(R.string.error_dialog_negative_text), (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            })
                            .show();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        }).authenticate(new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_dialog_title))
                .setNegativeButtonText(getString(R.string.biometric_dialog_negative_text))
                .build());
    }

    /**
     * Clear ViewBinding to prevent memory leak
     * Destroys open instances
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}