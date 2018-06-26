package com.morpheus.fingerprint;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

/**
 * Created by Morpheus on 26/06/2018.
 */

class FingerprintHandler extends FingerprintManager.AuthenticationCallback
{
    private Context context;

    public FingerprintHandler(Context context)
    {
        this.context = context;
    }

    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject)
    {
        CancellationSignal cancellationSignal = new CancellationSignal();
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED)
            return;

        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString)
    {
        this.update("Error al Autenticar Huella\n" + errString);
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString)
    {
        this.update("Ayuda al Autenticar Huella\n" + helpString);
    }

    @Override
    public void onAuthenticationFailed()
    {
        this.update("Fracaso al autenticar Huella\n");
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result)
    {
        this.update("Exito al Autenticar Huella\n");
        context.startActivity(new Intent(context, HomeActivity.class));
    }

    public void update(String e)
    {
        Toast.makeText(context, e, Toast.LENGTH_SHORT).show();
    }
}
