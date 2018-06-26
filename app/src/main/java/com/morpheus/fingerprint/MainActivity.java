package com.morpheus.fingerprint;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity
{
    private KeyStore keyStore;
    private static final String KEY_NAME = "androidMorpheus";
    private static final int REQUEST = 1;
    private static final String PERMISOS[] = {Manifest.permission.USE_FINGERPRINT};
    private Cipher cipher;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inicializamos Keyguard y Fingerprint Manager
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        //Checa si tiene un sensor de autenticacion y da el permiso
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, PERMISOS, REQUEST);

        if (!fingerprintManager.isHardwareDetected())
            Toast.makeText(this, "El equipo no tiene sensor de huella", Toast.LENGTH_SHORT).show();
        //Checa si hay una huella registrada
        else if(!fingerprintManager.hasEnrolledFingerprints())
            Toast.makeText(this, "Registra una huella en Ajustes", Toast.LENGTH_SHORT).show();
        //Checa si la opcion lock screen esta habilitada
        else if(!keyguardManager.isKeyguardSecure())
            Toast.makeText(this, "No está habilitado Lock Screen en Ajustes", Toast.LENGTH_SHORT).show();
        else
        {
            generateKey();

            if(cipherInit())
            {
                FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                FingerprintHandler helper = new FingerprintHandler(this);
                helper.startAuth(fingerprintManager, cryptoObject);
            }
        }
    }

    private boolean cipherInit()
    {
        try
        {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e)
        {
            throw new RuntimeException("Fracaso al obtener Cipher", e);
        }

        try
        {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        }catch (KeyPermanentlyInvalidatedException e)
        {
            return false;
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException | UnrecoverableKeyException | InvalidKeyException e)
        {
            throw new RuntimeException("Fracaso al iniciar Cipher", e);
        }
    }

    private void generateKey()
    {
        try
        {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e)
        {
            e.printStackTrace();
        }

        KeyGenerator keyGenerator;
        try
        {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e)
        {
            throw new RuntimeException("Fracaso al generar la instancia KeyGenerator", e);
        }

        try
        {
            keyStore.load(null);

            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setUserAuthenticationRequired(true).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7).build());
            keyGenerator.generateKey();

        } catch (NoSuchAlgorithmException | CertificateException | IOException | InvalidAlgorithmParameterException e)
        {
            throw new RuntimeException(e);
        }
    }
}
