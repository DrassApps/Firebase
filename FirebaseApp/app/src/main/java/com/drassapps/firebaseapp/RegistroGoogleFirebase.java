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

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Esta clase nos permite autentificar un usuario en nuestra aplicación con Firebase, siendo la
 * forma de autentificacion a través de nuestra cuenta de Google. Para ello, es necesario
 * establecer una conexión con el SDK de Google y su OAuth. Al ser Firebase una
 * herramienta de Google no es necesario generar un OAuth como en otros
 * servicios, simplemente lanzar una actividad especial y recoger el
 * resultado que esta nos da. Para este tipo de autentificación no
 * hace falta verificar la cuenta, y al ser datos personales,
 * no nos guardamos en la BD de Firebase la contraseña con
 * la que se ha logeado en Google ID.
 */

public class RegistroGoogleFirebase extends Activity {

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;

    private FirebaseAuth mAuth;                // Firebase Authentication SDK

    private View mLayout;                      // Vista principal

    private DrawerLayout drawerLayout;         // Creacion de var DrawerLayout para su posterior uso
    private NavigationView bundle;             // Creacion de var NavigationView

    private ImageView menu_nav, bt_menu_nav;   // ImageView que controlan el navView

    private TextView estado, userId;           // TextView de estado y userId del usuario

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro_google_firebase);

        // Inicializamos Firebase
        mAuth = FirebaseAuth.getInstance();

        // Asignamos la vista y el linear global
        mLayout = findViewById(R.id.lay_main_google);

        // Botones de login y singout
        final SignInButton googleSignIn = (SignInButton) findViewById(R.id.bt_registro_google);
        final Button googleSignOut = (Button) findViewById(R.id.bt_salir_google);

        // TextView
        estado = (TextView) findViewById(R.id.googleId_estado);
        userId = (TextView) findViewById(R.id.googleId_userId);

        // Registramos y entramos con Google
        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        // Cerramoas el usuario
        googleSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                updateUI(null);
            }
        });

        // Asignamos las ImageView de la vista
        menu_nav = (ImageView) findViewById(R.id.menu_nav);
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

    // Llama a la actividad de Google para el registro y esperamos una respuesta
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Recogemos los datos de la vista de SingIn
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

        // Si el resultado del registros es correcto
        if (requestCode == RC_SIGN_IN) {

            // Obtenemos el resultado del SignIn con Google
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            // Si el registro ha sido correcto nos autentificamso con FireBase
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

            } else {
                // Si no mostramos un erorLog.d(TAG, "onComplete: Failed="
                Log.i(TAG,task.getException().getMessage());
                setSnackBar(mLayout,getString(R.string.google_err));
            }

        }else if (resultCode == RESULT_CANCELED){
            setSnackBar(mLayout,getString(R.string.google_err));
        }
    }

    // Autentificacion con Google
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        // Obtenemos los creedenciales
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Usuario logeado y autentificado -> actualizamos UI
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        }
                        else {
                            // Error detallado
                            Log.d(TAG, "onComplete: Failed="
                                    + task.getException().getMessage());
                            // Si tenemos un error
                            setSnackBar(mLayout,getString(R.string.facebook_singin_err));
                            updateUI(null);
                        }
                    }
                });
    }

    // Actualizamos la UI pasandole el usuario que tenemos en Firebase
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Establecemos el nombre que el usuario se ha registrado en Google
            estado.setText(getString(R.string.google_status_fmt, user.getEmail()));

            // Establecemos el id que tiene nuestro usuario registrado en Firebase
            userId.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.bt_registro_google).setVisibility(View.INVISIBLE);
            findViewById(R.id.linear_salir_google).setVisibility(View.VISIBLE);
        } else {

            // Hemos cerrado la sesion y restablecemos los elementos de la interfaz
            estado.setText(R.string.sesion_cerrada);
            userId.setText(null);

            findViewById(R.id.bt_registro_google).setVisibility(View.VISIBLE);
            findViewById(R.id.linear_salir_google).setVisibility(View.INVISIBLE);
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

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.API_KEY_BackEnd))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

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
        Intent i = new Intent(RegistroGoogleFirebase.this, MainActivity.class);
        startActivity(i);
        finish();
    }
    public void volverHome(){
        Intent i = new Intent(RegistroGoogleFirebase.this, MainActivity.class);
        startActivity(i);
        finish();
    }
    public void registroEmailView(View v){
        Intent i = new Intent(RegistroGoogleFirebase.this, RegistroEmailFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroGoogleView(View v){
        drawerLayout.closeDrawer(Gravity.START);
    }
    public void registroFacebookView(View v){
        Intent i = new Intent(RegistroGoogleFirebase.this, RegistroFacebookFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroTwitterView(View v){
        Intent i = new Intent(RegistroGoogleFirebase.this, RegistroTwitterFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroTelefonoView(View v){
        Intent i = new Intent(RegistroGoogleFirebase.this, RegistroTelefonoFirebase.class);
        startActivity(i);
        finish();
    }
    public void notificacionPush(View v){
        Intent i = new Intent(RegistroGoogleFirebase.this, NotificacionPushFirebase.class);
        startActivity(i);
        finish();
    }
    public void bdFirebase(View v){
        Intent i = new Intent(RegistroGoogleFirebase.this, BDFirebase.class);
        startActivity(i);
        finish();
    }
    public void almacenamientoFirebase(View v){
        Intent i = new Intent(RegistroGoogleFirebase.this, AlmacenamientoFirebase.class);
        startActivity(i);
        finish();
    }
    public void crashlyticsFirebase(View v){
        Intent i = new Intent(RegistroGoogleFirebase.this, CrashlyticsFirebase.class);
        startActivity(i);
        finish();
    }
    public void admobFirebase(View v){
        Intent i = new Intent(RegistroGoogleFirebase.this, AdMobFirebase.class);
        startActivity(i);
        finish();
    }
    public void inviteLinksFirebase(View v){
        Intent i = new Intent(RegistroGoogleFirebase.this, InviteLinkFirebase.class);
        startActivity(i);
        finish();
    }
}
