package com.drassapps.firebaseapp;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Clase que instancia el token del usuario para las notificaciones push.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    /**
     * Firebase actualiza constantemente los tokens de los usuarios para maximizar la seguridad,
     * por ello necesitamos recoger el token del usuario y actualizarlo
     */

    @Override
    public void onTokenRefresh() {
        // Obtiene el token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // Lo actualiza
        sendRegistrationToServer(refreshedToken);
    }


    // Nos permite registrar al usuario en Firebase para recibir notifiaciones.
    // Está implementado en la clase MainActivity
    private void sendRegistrationToServer(String token) {}
}
