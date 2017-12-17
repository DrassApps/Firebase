package com.drassapps.firebaseapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Clase que gestiona las notificaciones PUSH recibidas por la API de firebase.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    // Sobreescribimos el metodo que se llama cuando recibimos el mensaje
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        /**
         * Los mensajes pueden llegar tanto si la app está en foreground, background o cerrada.
         * De estar cerrada o en background, nos saldrá una notifiacion en la parte superior
         * con el mensaje, titulo e icono (por defecto) y si pulsamos sobre la notifiación
         * accederemos directamente a la aplicación
         *
         * Para el caso de la app en Foreground, debemos de establecer nosotros la llamada
         * a la notificacion.
         */

        // El parámetro remoteMessage representa un Objeto del mensaje recibido desde Firebase
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Comprobamos si el mensaje tiene datos
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Data payload: " + remoteMessage.getData());
        }

        // Comprobamos si el mensaje es una notificacion
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Body: " + remoteMessage.getNotification().getBody());

        }
        // Llamamos a la notifiacion por si recibimos una notifiacion del backend estando la
        // app en foreground
        recibirPush(remoteMessage);
    }

    public void recibirPush(RemoteMessage remoteMessage){
        // Crea y muestra una notifiacion enviada a través de FCM
        NotificationCompat.Builder builder = new  NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.firebase)
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody());
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

}
