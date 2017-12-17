package com.drassapps.firebaseapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Esta clase nos permite autentificar un usuario en nuestra aplicación con Firebase, siendo la
 * forma de autentificacion a través de nuestra cuenta de Facebook. Para ello es necesario
 * establecer una conexión con el SDK de Facebook y su OAuth, de tal forma que al pulsar
 * el botón accederemos a la vista de login de Facebook, una vez introducidos los datos
 * correctamente, tenemos un callback que nos permite recoger el token de sesión del
 * usuario de Facebook. Con este token, utilizamos el método de Firebase
 * "signInWithCredential" que nos creará un usuario en la consola.
 * Para este tipo de autentificación no hace falta verificar
 * la cuenta, y al ser datos personales, no nos guardamos
 * en la BD de Firebase la contraseña con la que se ha
 * logeado en Facebook.
 */

public class RegistroFacebookFirebase extends Activity {

    private static final String TAG = "FacebookLogin";

    private FirebaseAuth mAuth;                // Firebase Authentication SDK

    private View mLayout;                      // Vista principal

    private CallbackManager mCallbackManager;  // CallBack de Facebook

    private DrawerLayout drawerLayout;         // Creacion de var DrawerLayout para su posterior uso
    private NavigationView bundle;             // Creacion de var NavigationView

    private ImageView menu_nav, bt_menu_nav;   // ImageView que controlan el navView

    private TextView estado, userId;           // Estado y userId de la sesió

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro_facebook_firebase);

        // Registramso el SDK de Facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        // Inicializamos Firebase
        mAuth = FirebaseAuth.getInstance();

        // Asignamos la vista y el linear global
        mLayout = findViewById(R.id.lay_main_facebook);

        // Inicializamos el botón de login de Facebook
        mCallbackManager = CallbackManager.Factory.create();

        // TextVies
        estado = (TextView) findViewById(R.id.facebook_estado);
        userId = (TextView) findViewById(R.id.facebook_userId);

        // Logout de Facebook
        Button logoutFacebook = (Button) findViewById(R.id.bt_salir_facebook);

        // Login en Facebook
        LoginButton facebooksingin = (LoginButton) findViewById(R.id.bt_facebook);

        // Pedimos los permisos al usuario
        facebooksingin.setReadPermissions("email");

        // Al pulsar el botón se abre el OAuth de Facebook para logearnos con nuestra cuenta, si la
        // autentifiación de Facebook es correcta, nos devuelve un token que utilizaremos para
        // el registro en el Firebase
        facebooksingin.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // Si la url ha sido correcta gestionamos el OAuth
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            // Cancelada
            @Override
            public void onCancel() {
                Log.i(TAG, "facebook:onCancel");
            }

            // Error (conexion por ejemplo)
            @Override
            public void onError(FacebookException error) {
                Log.i(TAG, "facebook:onError", error);
            }
        });


        // Cerramos la sesion de Facebook
        logoutFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                updateUI(null);
            }
        });


        // Asignamos las ImageView de la vista
        menu_nav = (ImageView) findViewById(R.id.menu_nav_facebook);
        bt_menu_nav = (ImageView) findViewById(R.id.bt_menu_nav);

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

    // Al pulsar el botón se abre el OAuth de Facebook para logearnos con nuestra cuenta, si la
    // autentifiación de Facebook es correcta, nos devuelve un token que utilizaremos para
    // el registro en el Firebase
    private void handleFacebookAccessToken(AccessToken token) {

        // Obtenemos las credenciales del usuario registrado a través del OAuth de Facebook
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        // Lo registramos en Firebase
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Si el registro ha sido correcto
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);

                        } else {
                            // Error detallado
                            Log.d(TAG, "onComplete: Failed="
                                    + task.getException().getMessage());
                            // No ha podido registrarse en Firebase
                            setSnackBar(mLayout,getString(R.string.facebook_singin_err));
                            updateUI(null);
                        }
                    }
                });

    }

    // Pasamos el resultado de la actividad al boton de login
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    // Actualizamos la UI pasandole el usuario que tenemos en Firebase
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Establecemos el nombre que el usuario se ha registrado en Facebook
            estado.setText(getString(R.string.facebook_status_fmt, user.getEmail()));

            // Establecemos el id que tiene nuestro usuario registrado en Firebase
            userId.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.bt_salir_facebook).setVisibility(View.VISIBLE);
            findViewById(R.id.bt_facebook).setVisibility(View.INVISIBLE);

        } else {
            // Hemos cerrado la sesion y restablecemos los elementos de la interfaz
            estado.setText(R.string.sesion_cerrada);
            userId.setText(null);

            findViewById(R.id.bt_salir_facebook).setVisibility(View.INVISIBLE);
            findViewById(R.id.bt_facebook).setVisibility(View.VISIBLE);
        }
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
    private void setupDrawerContent(NavigationView navigationView)
    {
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

    // Al abrir la vista comprueba si hay un usuario logeado en la aplicación de tal forma
    // que no tenga que introducir los datos de inicio de sesión cada vez que abre la app
    @Override
    public void onStart() {
        super.onStart();
        // Al iniciar la vista comprueba si hay un usuario activo logeado en la aplicación
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Si hay un usuario registrado activo en la aplicación cuando se abre la vista
        // actualizamos la vista con los datos que tenemos en el firebase
        if (currentUser != null){
            updateUI(currentUser);
        }else{
            // no actualizamos
            updateUI(null);
        }
    }

    // Para mostrar el resto de funcionalidades de Auth, cerramos la sesion al salir de la vista
    @Override
    public void onStop() {
        super.onStop();
        // Al cerrar la vista comprueba si hay algun usuario activo
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Si lo hay, cerramos la sesión
        if (currentUser != null){
            mAuth.signOut();
        }else{
            updateUI(null);
        }
    }


    @Override
    public void onBackPressed() {
        // Al cerrar la vista comprueba si hay algun usuario activo
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Si lo hay, cerramos la sesion
        if (currentUser != null){
            mAuth.signOut();
            volverHome();
        }else{
            updateUI(null);
            volverHome();
        }
    }

    // Metodos para pasar a las otras actividades desde el menu
    public void mainvView(View v){
        Intent i = new Intent(RegistroFacebookFirebase.this, MainActivity.class);
        startActivity(i);
        finish();
    }
    public void volverHome(){
        Intent i = new Intent(RegistroFacebookFirebase.this, MainActivity.class);
        startActivity(i);
        finish();
    }
    public void registroEmailView(View v){
        Intent i = new Intent(RegistroFacebookFirebase.this, RegistroEmailFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroGoogleView(View v){
        Intent i = new Intent(RegistroFacebookFirebase.this, RegistroGoogleFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroFacebookView(View v){
        drawerLayout.closeDrawer(Gravity.START);
    }
    public void registroTwitterView(View v){
        Intent i = new Intent(RegistroFacebookFirebase.this, RegistroTwitterFirebase.class);
        startActivity(i);
        finish();
    }
    public void notificacionPush(View v){
        Intent i = new Intent(RegistroFacebookFirebase.this, NotificacionPushFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroTelefonoView(View v){
        Intent i = new Intent(RegistroFacebookFirebase.this, RegistroTelefonoFirebase.class);
        startActivity(i);
        finish();
    }
    public void bdFirebase(View v){
        Intent i = new Intent(RegistroFacebookFirebase.this, BDFirebase.class);
        startActivity(i);
        finish();
    }
    public void almacenamientoFirebase(View v){
        Intent i = new Intent(RegistroFacebookFirebase.this, AlmacenamientoFirebase.class);
        startActivity(i);
        finish();
    }
    public void crashlyticsFirebase(View v){
        Intent i = new Intent(RegistroFacebookFirebase.this, CrashlyticsFirebase.class);
        startActivity(i);
        finish();
    }
    public void admobFirebase(View v){
        Intent i = new Intent(RegistroFacebookFirebase.this, AdMobFirebase.class);
        startActivity(i);
        finish();
    }
    public void inviteLinksFirebase(View v){
        Intent i = new Intent(RegistroFacebookFirebase.this, InviteLinkFirebase.class);
        startActivity(i);
        finish();
    }
}
