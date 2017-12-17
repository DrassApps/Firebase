package com.drassapps.firebaseapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Esta clase nos permite autentificar un usuario en nuestra aplicación con Firebase, siendo la
 * forma de autentificacion email y contraseña. Para ello es necesario pedir estos datos al
 * usuario y validándolos para que tengan un formato apropiado. Para mostrar todas las
 * funcionalidades, la vista se actualiza cuando el usuario se registra o hace login
 * en la aplicación, obteniendo los datos del usuario y mostrándolos en la interfaz.
 * También tenemos la posibilidad de enviar un email de verificación con una
 * plantilla que nos ofrece Firebase, siendo pues, de forma automática y
 * sin tener que preocuparnos por nada. Por último, al registrarse
 * correctamente un usuario, guardamos los datos en la BD que
 * tiene por defecto Firebase.
 */

public class RegistroEmailFirebase extends Activity {

    private static final String TAG = "EmailPassword";

    private FirebaseAuth mAuth;                // Firebase Authentication SDK

    private DrawerLayout drawerLayout;         // Creacion de var DrawerLayout para su posterior uso
    private NavigationView bundle;             // Creacion de var NavigationView

    private LinearLayout dummy_line;           // Dumy linear que ser el focus en onStart

    private ImageView menu_nav, bt_menu_nav;   // ImageView que controlan el navView

    private View mLayout;                      // Vista principal

    private String email, password;            // String que contiene el email y la pass
    private EditText emailEd, passwordEd;      // EditText de email y pass
    private TextView estado, userId;           // TextView de estado y userId del usuario

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro_email_firebase);

        // Inicializamos Firebase
        mAuth = FirebaseAuth.getInstance();

        // EditText
        emailEd = (EditText) findViewById(R.id.ed_email_em);
        passwordEd = (EditText) findViewById(R.id.ed_password_em);

        // TextView
        estado = (TextView) findViewById(R.id.ed_estado);
        userId = (TextView) findViewById(R.id.ed_userId);

        // Asignamos la vista y el linear global
        mLayout = findViewById(R.id.lay_main_email);

        // Botones de gestion
        Button registro = (Button) findViewById(R.id.bt_registro_email);
        Button inisesion = (Button) findViewById(R.id.bt_iniciosesion_email);
        Button salirsesion = (Button) findViewById(R.id.bt_salir_email);
        Button verificarEmail = (Button) findViewById(R.id.bt_verificar_email);

        // Registrarse
        registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Obtenemos el texto de los EditTexts
                email = emailEd.getText().toString();
                password = passwordEd.getText().toString();

                // Validamoas los campos
                if (correoValido(email) && password.length() > 5){

                    // Creamos un usuario con Email & Passwrod
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegistroEmailFirebase.this,
                            new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    // Si la creacion del usuario falla, damos un mensaje de error,
                                    // en caso de registro correcto, obtenemos el usuario y
                                    // actualizamos la vista con los datos del usuario

                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);

                                    // Solo para el registro con Email, nos guardamos la contraseña
                                    // del usuario en la BD como ejemplo de que si se puede tener
                                    // la contraseña de un usario en la consola de Firebase. Al
                                    // guardarnos datos sensibles de los usuarios, debemos
                                    // tener en cuenta la LOPD y LPI

                                    // Cogemos tambien el token de sesion activa del usuario
                                    String token = FirebaseInstanceId.getInstance().getToken();

                                    // Guardamos los datos
                                    guardarDatosUsuario(email,password,token);

                                    if (!task.isSuccessful()) {
                                        // No ha podido registrarse y nos muestra el fallo que
                                        // devuelve firebase
                                        Log.d(TAG, "onComplete: Failed="
                                                + task.getException().getMessage());

                                        // Manejamos el fallo para que el usuario sepa realmente
                                        // el fallo
                                        if (task.getException().getMessage().
                                                contains("badly formatted")){
                                            setSnackBar(mLayout,"Email incorrecto");
                                        }
                                        updateUI(null);
                                    }

                                }
                            });

                }else{ setSnackBar(mLayout,"Datos de registro incorrectos."); }
            }
        });

        // Iniciar sesion
        inisesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Obtenemos el texto de los EditTexts
                email = emailEd.getText().toString();
                password = passwordEd.getText().toString();

                // Verificamos los datos de entrada e intentamos logerarnos
                if(correoValido(email) && password.length() > 5){
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegistroEmailFirebase.this,
                                    new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    // Obtenemos el usuario
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                    // Si recibimos un usuario actualizamos la vista
                                    if (user != null) {
                                        updateUI(user);
                                    }

                                    // Si firebase nos devuelve un error, le deimos al usuario que
                                    // se ha equivocado
                                    if (!task.isSuccessful()) {
                                        setSnackBar(mLayout,"Datos de acceso incorrectos");
                                        updateUI(null);
                                    }
                                }
                            });

                }else{ setSnackBar(mLayout,"Datos de acceso incorrectos"); }
            }
        });

        // Cerrar Sesion
        salirsesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                updateUI(null);
                setSnackBar(mLayout,"Sesión cerrada");
            }
        });

        // Verificar Email, envida un email al correo para que el usuario verifique su registro
        // cabe destacar que Firebase tambien implementa plantillas para recuprar contraseña
        // actualizar email o contraseña.
        verificarEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                AlertDialog.Builder builder = new AlertDialog.Builder(RegistroEmailFirebase.this);

                builder.setMessage(R.string.textVerificar)
                        .setTitle(R.string.verificar)
                        .setNegativeButton(R.string.emailNoVerificado, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Cerramos el dialogo
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(R.string.emailVerificado, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Enviamos el email al usuario
                                enviarVerificacion(user);
                            }
                        });

                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();

            }
        });

        // Asignamos las ImageView de la vista
        menu_nav = (ImageView) findViewById(R.id.menu_nav_email);
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

        // Dumy line para que cuando se abra la aplicación el EditTet no salga focuseado
        dummy_line = (LinearLayout) findViewById(R.id.dummy_line);
        dummy_line.requestFocus();
    }

    // Guardamos los datos del usuario en la BD
    private void guardarDatosUsuario(String email, String pass, String token){
        // Accedemos al raiz de la BD de nuestro proyecto en Firebase
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        // Accedemos a su hijo, que al tener solo uno sera Usuario
        final DatabaseReference mDatabaseRefUsuario = mDatabaseRef.child("Usuario");

        // Inicializamos un usuario de la BD
        UsuarioObjectDBFirebase usuarioBD = new UsuarioObjectDBFirebase(email,token,pass);

        // Actualizamos los datos
        mDatabaseRefUsuario.setValue(usuarioBD);
    }

    // Enviar correo de verificacion
    private void enviarVerificacion(FirebaseUser user){
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            setSnackBar(mLayout,"Email de verificación enviado.");
                        }
                        if (!task.isSuccessful()){
                            setSnackBar(mLayout,"Error al enviar el email.");
                        }
                    }
                });
    }

    // Permite gestionar si los inputs del usuario son correctos
    private boolean correoValido(String correo) {
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            emailEd.setError("Email invalido");
            return false;
        } else { emailEd.setError(null); }

        return true;
    }


    // Actualizamos la UI pasandole el usuario que tenemos en Firebase
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Establecemos el nombre que el usuario se ha registrado en Firebase (email)
            if (user.isEmailVerified()){
                estado.setText(getString(R.string.emailpassword_status_fmt,
                        user.getEmail(), getString(R.string.emailVerificado)));
            }else {
                estado.setText(getString(R.string.emailpassword_status_fmt,
                        user.getEmail(), getString(R.string.emailNoVerificado)));
            }

            // Establecemos el id que tiene nuestro usuario registrado en Firebase
            userId.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.email_password_buttons).setVisibility(View.GONE);
            findViewById(R.id.email_password_fields).setVisibility(View.GONE);
            findViewById(R.id.signed_in_buttons).setVisibility(View.VISIBLE);

        } else {

            // Hemos cerrado la sesion y restablecemos los elementos de la interfaz
            estado.setText(R.string.sesion_cerrada);
            userId.setText(null);

            findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
            findViewById(R.id.signed_in_buttons).setVisibility(View.GONE);
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


    // Al abrir la vista comprueba si hay un usuario logeado en la aplicación de tal forma
    // que no tenga que introducir los datos de inicio de sesión cada vez que abre la app
    @Override
    public void onStart() {
        super.onStart();
        // Al iniciar la vista comprueba si hay un usuario activo logeado en la aplicacion
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Si hay un usuario registrado activo en la aplicacion cuando se abre la vista
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

        // Si lo hay, cerramos la sesion
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

    // Metodos para pasar a las otras actividades desde el menú
    public void mainvView(View v){
        Intent i = new Intent(RegistroEmailFirebase.this, MainActivity.class);
        startActivity(i);
        finish();
    }
    public void registroEmailView(View v){
        drawerLayout.closeDrawer(Gravity.START);
    }
    public void registroGoogleView(View v){
        Intent i = new Intent(RegistroEmailFirebase.this, RegistroGoogleFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroFacebookView(View v){
        Intent i = new Intent(RegistroEmailFirebase.this, RegistroFacebookFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroTwitterView(View v){
        Intent i = new Intent(RegistroEmailFirebase.this, RegistroTwitterFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroTelefonoView(View v){
        Intent i = new Intent(RegistroEmailFirebase.this, RegistroTelefonoFirebase.class);
        startActivity(i);
        finish();
    }
    public void notificacionPush(View v){
        Intent i = new Intent(RegistroEmailFirebase.this, NotificacionPushFirebase.class);
        startActivity(i);
        finish();
    }
    public void bdFirebase(View v){
        Intent i = new Intent(RegistroEmailFirebase.this, BDFirebase.class);
        startActivity(i);
        finish();
    }
    public void almacenamientoFirebase(View v){
        Intent i = new Intent(RegistroEmailFirebase.this, AlmacenamientoFirebase.class);
        startActivity(i);
        finish();
    }
    public void crashlyticsFirebase(View v){
        Intent i = new Intent(RegistroEmailFirebase.this, CrashlyticsFirebase.class);
        startActivity(i);
        finish();
    }
    public void admobFirebase(View v){
        Intent i = new Intent(RegistroEmailFirebase.this, AdMobFirebase.class);
        startActivity(i);
        finish();
    }
    public void volverHome(){
        Intent i = new Intent(RegistroEmailFirebase.this, MainActivity.class);
        startActivity(i);
        finish();
    }
    public void inviteLinksFirebase(View v){
        Intent i = new Intent(RegistroEmailFirebase.this, InviteLinkFirebase.class);
        startActivity(i);
        finish();
    }
}
