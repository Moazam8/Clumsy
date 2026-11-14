package com.clumsy.finance;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

@CapacitorPlugin(name = "BiometricUnlock")
public class BiometricUnlockPlugin extends Plugin {
    private static final String KEY_ALIAS = "clumsy_biometric_unlock_v1";
    private static final String PREFS_NAME = "clumsy_biometric_unlock";
    private static final String PREF_ENCRYPTED_PASSWORD = "encrypted_password";
    private static final int GCM_TAG_LENGTH_BITS = 128;

    @PluginMethod
    public void isAvailable(PluginCall call) {
        int result = BiometricManager.from(getContext())
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
        JSObject response = new JSObject();
        response.put("available", result == BiometricManager.BIOMETRIC_SUCCESS);
        call.resolve(response);
    }

    @PluginMethod
    public void hasSavedPassword(PluginCall call) {
        boolean available = BiometricManager.from(getContext())
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS;
        boolean saved = getContext().getSharedPreferences(PREFS_NAME, 0)
            .contains(PREF_ENCRYPTED_PASSWORD);
        JSObject response = new JSObject();
        response.put("enabled", available && saved);
        call.resolve(response);
    }

    @PluginMethod
    public void savePassword(PluginCall call) {
        String password = call.getString("password");
        if (password == null || password.isEmpty()) {
            call.reject("Enter your Clumsy encryption password first.");
            return;
        }
        try {
            authenticate(call, true, password, null);
        } catch (GeneralSecurityException error) {
            call.reject("Could not prepare fingerprint unlock.", error);
        }
    }

    @PluginMethod
    public void unlockPassword(PluginCall call) {
        String encrypted = getContext().getSharedPreferences(PREFS_NAME, 0)
            .getString(PREF_ENCRYPTED_PASSWORD, null);
        if (encrypted == null) {
            call.reject("Fingerprint unlock is not enabled.");
            return;
        }
        try {
            String[] parts = encrypted.split(":", 2);
            if (parts.length != 2) throw new GeneralSecurityException("Saved unlock data is invalid.");
            byte[] iv = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] ciphertext = Base64.decode(parts[1], Base64.NO_WRAP);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            authenticate(call, false, null, cipher);
        } catch (GeneralSecurityException | IllegalArgumentException error) {
            call.reject("Could not prepare fingerprint unlock.", error);
        }
    }

    @PluginMethod
    public void clearPassword(PluginCall call) {
        getContext().getSharedPreferences(PREFS_NAME, 0).edit()
            .remove(PREF_ENCRYPTED_PASSWORD)
            .apply();
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            if (keyStore.containsAlias(KEY_ALIAS)) keyStore.deleteEntry(KEY_ALIAS);
            call.resolve();
        } catch (GeneralSecurityException | java.io.IOException error) {
            call.reject("Could not remove fingerprint unlock data.", error);
        }
    }

    private void authenticate(PluginCall call, boolean encrypt, String password, Cipher cipher)
        throws GeneralSecurityException {
        if (encrypt) {
            cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
        }
        Executor executor = ContextCompat.getMainExecutor(getContext());
        Cipher authenticationCipher = cipher;
        BiometricPrompt prompt = new BiometricPrompt(
            getActivity(),
            executor,
            new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Cipher authenticatedCipher = result.getCryptoObject().getCipher();
                    try {
                        if (encrypt) {
                            byte[] ciphertext = authenticatedCipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
                            String iv = Base64.encodeToString(authenticatedCipher.getIV(), Base64.NO_WRAP);
                            String data = Base64.encodeToString(ciphertext, Base64.NO_WRAP);
                            getContext().getSharedPreferences(PREFS_NAME, 0).edit()
                                .putString(PREF_ENCRYPTED_PASSWORD, iv + ":" + data)
                                .apply();
                            call.resolve();
                        } else {
                            String unlocked = new String(
                                authenticatedCipher.doFinal(ciphertextFromPreferences()),
                                StandardCharsets.UTF_8
                            );
                            JSObject response = new JSObject();
                            response.put("password", unlocked);
                            call.resolve(response);
                        }
                    } catch (GeneralSecurityException | IllegalArgumentException error) {
                        call.reject("Fingerprint unlock could not open the saved password.", error);
                    }
                }

                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    call.reject("Fingerprint unlock was not completed: " + errString);
                }
            }
        );
        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
            .setTitle(encrypt ? "Enable Clumsy fingerprint unlock" : "Unlock Clumsy")
            .setSubtitle(encrypt ? "Confirm your fingerprint to protect your app password" : "Confirm your fingerprint to open your financial records")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build();
        prompt.authenticate(info, new BiometricPrompt.CryptoObject(authenticationCipher));
    }

    private byte[] ciphertextFromPreferences() throws GeneralSecurityException {
        String encrypted = getContext().getSharedPreferences(PREFS_NAME, 0)
            .getString(PREF_ENCRYPTED_PASSWORD, null);
        if (encrypted == null) throw new GeneralSecurityException("Saved unlock data is missing.");
        String[] parts = encrypted.split(":", 2);
        if (parts.length != 2) throw new GeneralSecurityException("Saved unlock data is invalid.");
        return Base64.decode(parts[1], Base64.NO_WRAP);
    }

    private SecretKey getOrCreateKey() throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            if (keyStore.containsAlias(KEY_ALIAS)) {
                return ((KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null)).getSecretKey();
            }
            KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            KeyGenParameterSpec.Builder spec = new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .setUserAuthenticationRequired(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                spec.setUserAuthenticationParameters(
                    0,
                    KeyProperties.AUTH_BIOMETRIC_STRONG
                );
            } else {
                spec.setUserAuthenticationValidityDurationSeconds(-1);
            }
            generator.init(spec.build());
            return generator.generateKey();
        } catch (java.io.IOException | java.security.KeyStoreException |
                 java.security.NoSuchAlgorithmException | java.security.UnrecoverableEntryException |
                 java.security.InvalidKeyException | java.security.InvalidAlgorithmParameterException error) {
            throw new GeneralSecurityException("Could not access Android Keystore.", error);
        }
    }
}
