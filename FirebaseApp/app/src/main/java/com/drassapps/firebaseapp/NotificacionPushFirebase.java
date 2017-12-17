package com.drassapps.firebaseapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Antes de explicar la funcionalidad de esta clase, cabe comentar los tres tipos de notificaciones
 * implementadas en el proyecto:
 *
 * 1. Notifacion push a todos los dispositivos registrados a través de la consola
 * 2. Notificacion al mismo dispositivo.
 * 3. Notificación desde un BackEnd.
 *
 * La forma más simple de enviar una notificación es a través de la consola de Firebase, que nos
 * proporciona una interfaz muy clara para enviar los mensajes, segmentando por grupos o por so
 * o por dispositivos.
 *
 * Esta clase implenenta la segunda funcionalidad, recogiendo el token del dispositivo, que junto
 * a un titulo y un mensaje lo enviamos a un BackEnd que llama a la API de firebase. Aunque carece
 * de sentido enviarse una notificaión nos permite comprobar la funcionalidad y el formato detallado
 * en la clase MyFirebaseMessaginService.
 *
 * Por últmo nos encontramos con la implementacion en un BackEnd con interfaz, este supuesto esta
 * pensado para aquellas empresas que ya tienen un BackEnd con arquitectura API-Rest no obligarles
 * a que utilicen el panel de Firebase, si no integrar firebase en su sistema. Es por ello que
 * el BackEnd tendrá una interfaz pidiendo al admisnitrador mensaje, titulo y destinatarios
 * que los recogera de Firebase o de su propia BD.
 */

public class NotificacionPushFirebase extends Activity {

    private DrawerLayout drawerLayout;         // Creacion de var DrawerLayout para su posterior uso
    private NavigationView bundle;             // Creacion de var NavigationView

    private View mLayout;                      // Vista principal

    // Url para el BackEnd
    String urlApi = "http://drassapps.es/public/notifiacion";
    String urlApi2 = "http://drassapps.es/public/guardaToken";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notificacion_push_firebase);

        // Obtenemos el token activo del usuario
        final String token = FirebaseInstanceId.getInstance().getToken();

        // Lo mostramos al usuario
        TextView tokenActivo = (TextView) findViewById(R.id.notif_token);
        tokenActivo.setText(getString(R.string.notifi_token) + "\n\n" + token);

        // Elementos de la UI
        final EditText titulo = (EditText) findViewById(R.id.noti_titulo);
        final EditText mensaje = (EditText) findViewById(R.id.noti_mensaje);
        Button enviarNoti = (Button) findViewById(R.id.bt_enviar_noti);
        final WebView httpcon = (WebView) findViewById(R.id.wb_api);
        final WebView httpcon2 = (WebView) findViewById(R.id.wb_api2);

        // Asignamos la vista global
        mLayout = findViewById(R.id.lay_main_push);

        // Configuramos la url
        urlApi2 = urlApi2 + "&" + token;

        // Nos guardamos el token en el backend
        setUPApi2(httpcon2,urlApi2);

        // Nos enviamos una notificación
        enviarNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Comprobamos si tenemos conexión para enviar la Notificacion al servidor
                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                // Si tenemos
                if (networkInfo != null && networkInfo.isConnected()) {

                    String tit,men;

                    // Obtenemos el texto introducido por el usuario
                    tit = titulo.getText().toString();
                    men = mensaje.getText().toString();

                    // Si la longitud es menor a 3 le decimos que ponga algo más
                    if(tit.length() < 3 || men.length() < 3){
                        setSnackBar(mLayout,getString(R.string.ed_error));

                    }else{ // Enviamos la url al BackEnd que se encargará de enviar la notificación

                        // Establecemos la url
                        urlApi = urlApi + "&" + tit + "&" + men + "&" + token;
                        // Habilitamos el JS
                        httpcon.getSettings().setJavaScriptEnabled(true);
                        // Permitimos conexion http
                        httpcon.setWebViewClient(new WebViewClient() {
                            @Override
                            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                if (url.startsWith("http:") || url.startsWith("https:")) {
                                    return false;
                                }

                                // De otra forma permite enviar mailt, telf...
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(intent);
                                return true;
                            }
                        });
                        // Enviamos datos al backend
                        httpcon.loadUrl(urlApi);
                    }

                } else {
                    setSnackBar(mLayout,getString(R.string.network_error));
                }
            }
        });

        // Dummy lay
        LinearLayout dummy = (LinearLayout) findViewById(R.id.dummy_line);
        dummy.requestFocus();

        // Asignamos las ImageView de la vista
        ImageView menu_nav = (ImageView) findViewById(R.id.menu_nav_noti);
        ImageView bt_menu_nav = (ImageView) findViewById(R.id.bt_menu_nav);

        // Configuración del NavigationView
        bundle = (NavigationView) findViewById(R.id.navview);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Añadimos el NavigationView a la vista
        if (bundle != null) {
            setupDrawerContent(bundle);
        }

        // El boton del lay abre el nav
        menu_nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.START);
            }
        });

        // El botón dentro del nav, cierra el nav
        bt_menu_nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(Gravity.START);
            }
        });
    }

    // Metodo para enviar el token activo a la BD del BackEnd pensando en la 3 implementacion
    public void setUPApi2(WebView con, String url){
        con.getSettings().setJavaScriptEnabled(true);
        // Permitimos conexion http
        con.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http:") || url.startsWith("https:")) {
                    return false;
                }

                // De otra forma permite enviar mailt, telf...
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }
        });
        // Enviamos datos al backend
        con.loadUrl(url);
    }

    // Fuerza la creacion de un SnackBar, personificado
    public void setSnackBar(View coordinatorLayout, String snackTitle) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, snackTitle, Snackbar.LENGTH_SHORT);
        snackbar.show();
        View view = snackbar.getView();
        TextView txtv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        txtv.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    // Crea el Drawer
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        menuItem.getItemId();
                        return true;
                    }
                });
    }

    @Override
    public void onBackPressed() {
        volverHome();
    }

    // Metodos para pasar a las otras actividades desde el menu
    public void mainvView(View v){
        Intent i = new Intent(NotificacionPushFirebase.this, MainActivity.class);
        startActivity(i);
        finish();
    }
    public void volverHome(){
        Intent i = new Intent(NotificacionPushFirebase.this, MainActivity.class);
        startActivity(i);
        finish();
    }
    public void registroEmailView(View v){
        Intent i = new Intent(NotificacionPushFirebase.this, RegistroEmailFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroGoogleView(View v){
        Intent i = new Intent(NotificacionPushFirebase.this, RegistroGoogleFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroFacebookView(View v){
        Intent i = new Intent(NotificacionPushFirebase.this, RegistroFacebookFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroTwitterView(View v){
        Intent i = new Intent(NotificacionPushFirebase.this, RegistroTwitterFirebase.class);
        startActivity(i);
        finish();
    }
    public void notificacionPush(View v){
        drawerLayout.closeDrawer(Gravity.START);

    }
    public void registroTelefonoView(View v){
        Intent i = new Intent(NotificacionPushFirebase.this, RegistroTelefonoFirebase.class);
        startActivity(i);
        finish();
    }
    public void bdFirebase(View v){
        Intent i = new Intent(NotificacionPushFirebase.this, BDFirebase.class);
        startActivity(i);
        finish();
    }
    public void almacenamientoFirebase(View v){
        Intent i = new Intent(NotificacionPushFirebase.this, AlmacenamientoFirebase.class);
        startActivity(i);
        finish();
    }
    public void crashlyticsFirebase(View v){
        Intent i = new Intent(NotificacionPushFirebase.this, CrashlyticsFirebase.class);
        startActivity(i);
        finish();
    }
    public void admobFirebase(View v){
        Intent i = new Intent(NotificacionPushFirebase.this, AdMobFirebase.class);
        startActivity(i);
        finish();
    }
    public void inviteLinksFirebase(View v){
        Intent i = new Intent(NotificacionPushFirebase.this, InviteLinkFirebase.class);
        startActivity(i);
        finish();
    }
}
