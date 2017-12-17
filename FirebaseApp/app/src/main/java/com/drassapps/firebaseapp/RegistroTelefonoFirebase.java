package com.drassapps.firebaseapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.twitter.sdk.android.core.TwitterCore;

import java.util.concurrent.TimeUnit;

/**
 * Esta clase nos permite autentificar un usuario en nuestra aplicación con Firebase, siendo la
 * forma de autentificacion a través de un número de teléfono y un código de verifiación. Para
 * ello es necesario pedir un número de teléfono al usuario al que enviaremos un SMS con el
 * código tipico de validación, una vez recogido el código registraremos al usuario si
 * el código es el correcto. Cabe destacar que podemos reenviar el código al usuario,
 * y éste cambiará, aumentando al seguridad, además no hará falta una validación
 * posterior pues el usuario ya se ha validado a través de SMS.
 */

/**
 * IMPORTANTE:
 * ESTA FUNCIONALIDAD NO ESTA INCLUIDA EN LA LINCENCIA GRATUITA, PERO SE HA IMPLEMENTADO
 * DE TAL FORMA QUE CUANDO SE MEJORE LA LICENCIA PODRÁ SER UTILIZADA
 */

public class RegistroTelefonoFirebase extends Activity {

    private static final String TAG = "TelefonoActivity";

    private FirebaseAuth mAuth;                // Firebase Authentication SDK

    // Callback para el Auth de Telefono
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    // Datos obtenidos en el callback al enviar el SMS
    public String verifID;
    private PhoneAuthProvider.ForceResendingToken tokenSend;

    private DrawerLayout drawerLayout;         // Creacion de var DrawerLayout para su posterior uso
    private NavigationView bundle;             // Creacion de var NavigationView

    private LinearLayout dummy_line;           // Dumy linear que ser el focus en onStart

    private ImageView menu_nav, bt_menu_nav;   // ImageView que controlan el navView

    private View mLayout;                      // Vista principal

    private String numero, codigo;             // String que contiene el email y la pass
    private EditText numeroEd, codeEd;         // EditText de numero y codigo verificacion
    private TextView estado, userID;           // TextView de estado y userId del usuario


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro_telefono_firebase);

        // Inicializamos Firebase
        mAuth = FirebaseAuth.getInstance();

        // Asignamos la vista y el linear global
        mLayout = findViewById(R.id.lay_main_telf);

        // TextViews
        estado = (TextView) findViewById(R.id.telfono_estado);
        userID = (TextView) findViewById(R.id.telefono_userid);

        // Boton de gestion de registro
        Button enviar = (Button) findViewById(R.id.button_start_verification);
        Button verificar = (Button) findViewById(R.id.button_verify_phone);
        Button reenviar = (Button) findViewById(R.id.button_resend);
        Button cerrarSesion = (Button) findViewById(R.id.sign_out_button);

        // EditTexts
        numeroEd = (EditText) findViewById(R.id.field_phone_number);
        codeEd = (EditText) findViewById(R.id.field_verification_code);

        // Enviamos el código de verificacion por SMS
        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                numero = numeroEd.getText().toString();

                // Si el numero de telefono introducido por el usuario es valido, enviamos el SMS
                if (numeroTelfValido(numero)){
                  //  numero = "+34" + numero;
                    envarCodeRegistro(numero);
                }

                // Si no, le mostramos al usuario un error
                else{ numeroEd.setError("Numero incorrecto"); }
            }
        });

        // Verificamos el código introducido por el usuario
        verificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                codigo = codeEd.getText().toString();
                verifyPhoneNumberWithCode(verifID,codigo);
            }
        });


        // Reenviamos el código
        reenviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Reenviamos el código de verificacion al número que tenemos asociado con el token

                if (tokenSend != null){
                    resendVerificationCode(numero,tokenSend);
                }else  {
                 setSnackBar(mLayout,"No has enviado ningún SMS");
                }
            }
        });

        // Cerramos la sesion
        cerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                updateUI(null);
            }
        });

        // Creamos un callback con diferentes opciones para la verificaion
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // Este callback se invoca en dos situaciones:
                // 1 - Cuando se produce una verificación instantánea. En algunos casos el numero de
                //     teléfono puede ser verificado sin necesidad de enviar o indicar el código

                // 2 - Con el Auto-retrieval de Google. En algunos dispositivos Google Play services
                //     puede automáticamente detectar un SMS de verificación e introducirlo sin
                //     necesidad de intervención por parte del usuario

                Log.d(TAG, "onVerificationCompleted:" + credential);

                setSnackBar(mLayout,"verificaiton completed?");

                // Nos intentamos registramos
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // Este callback se invoca cuando se crea una peticial de verificacion invalida,
                // por ejemplo si el numero de telefono no tiene un formato apropiado
                Log.w(TAG, "onVerificationFailed", e);

                userID.setText(e.toString());

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Petición invalida
                    setSnackBar(mLayout,"Fallo al enviar el código");
                    updateUI(null);

                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // Se ha superado el tiempo de vida del codigo enviado por SMS
                    setSnackBar(mLayout,"Tiempo de vida del código superado");
                    updateUI(null);
                }
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // Enviamos enviado correctamente el SMS la usuario y nos guardamos algunos datos
                // que pueden ser interesantes recuperar mas tarde (como por ejemplo para reenviar
                // el código), nos guardamos el id de verificacion y el token de envio
                Log.d(TAG, "onCodeSent:" + verificationId);

                setSnackBar(mLayout,"Le hemos enviado un códiigo a su número.");

                verifID = verificationId;
                tokenSend = token;
            }
        };

        // Asignamos las ImageView de la vista
        menu_nav = (ImageView) findViewById(R.id.menu_nav_telf);
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

    // Enviamos el código de registro al numero dicho por el usuario
    private void envarCodeRegistro(String numero){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                numero,             // Número de teléfono a verificar
                60,                 // Tiempo de vida del código
                TimeUnit.SECONDS,   // Medida del tiempo devida
                this,               // Activity (para el callback)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    // Reenviamos el SMS al número de teléfono, es muy importante especificar el token que hemos
    // obtenido en el callback al enviar el SMS
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    // Verificamos el código introducido por el usuario
    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    // Registrar en Firebase con un número de telefono
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // El registro se ha producido actualizamos la UI con los datos del
                            // usuaro
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            updateUI(user);

                        } else {
                            // El registro ha fallado
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                            // Intentamos averiguar los posibles fallos, si es un fallo de creendial
                            // es debido a que el code no se corresponde con el code del SMS enviado
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                setSnackBar(mLayout,"Codigo de verificación incorrecto");
                            }

                            // Actualizamos la UI
                            updateUI(null);
                            setSnackBar(mLayout,"Error en el registro");
                        }
                    }
                });
    }

    // Permite gestionar si los inputs del usuario son correctos
    private boolean numeroTelfValido(String phone) {
        if (!Patterns.PHONE.matcher(phone).matches() || numeroEd.getText().toString().length() > 9
                || numeroEd.getText().toString().length() < 9) {
            numeroEd.setError("Numero incorrecto");
            return false;
        } else { numeroEd.setError(null); }

        return true;
    }

    // Actualizamos la UI pasandole el usuario que tenemos en Firebase
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Establecemos el nombre que el usuario tiene en Firebase (número de telefono)
            estado.setText(getString(R.string.phone_tfm, user.getDisplayName()));

            // Establecemos el id que tiene nuestro usuario registrado en Firebase
            userID.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.phone_auth_fields).setVisibility(View.GONE);
            findViewById(R.id.signed_out_buttons).setVisibility(View.VISIBLE);

        } else {
            // Cerramos la sesion y restablecemos los elementos de la interfaz
            estado.setText(R.string.sesion_cerrada);
            userID.setText(null);

            findViewById(R.id.phone_auth_fields).setVisibility(View.VISIBLE);
            findViewById(R.id.signed_out_buttons).setVisibility(View.GONE);
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

    // Metodos para pasar a las otras actividades desde el menu
    public void mainvView(View v){
        Intent i = new Intent(RegistroTelefonoFirebase.this, MainActivity.class);
        startActivity(i);
        finish();
    }
    public void volverHome(){
        Intent i = new Intent(RegistroTelefonoFirebase.this, MainActivity.class);
        startActivity(i);
        finish();
    }
    public void registroEmailView(View v){
        Intent i = new Intent(RegistroTelefonoFirebase.this, RegistroEmailFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroGoogleView(View v){
        Intent i = new Intent(RegistroTelefonoFirebase.this, RegistroGoogleFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroFacebookView(View v){
        Intent i = new Intent(RegistroTelefonoFirebase.this, RegistroFacebookFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroTwitterView(View v){
        Intent i = new Intent(RegistroTelefonoFirebase.this, RegistroTwitterFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroTelefonoView(View v){
        drawerLayout.closeDrawer(Gravity.START);
    }
    public void notificacionPush(View v){
        Intent i = new Intent(RegistroTelefonoFirebase.this, NotificacionPushFirebase.class);
        startActivity(i);
        finish();
    }
    public void bdFirebase(View v){
        Intent i = new Intent(RegistroTelefonoFirebase.this, BDFirebase.class);
        startActivity(i);
        finish();
    }
    public void almacenamientoFirebase(View v){
        Intent i = new Intent(RegistroTelefonoFirebase.this, AlmacenamientoFirebase.class);
        startActivity(i);
        finish();
    }
    public void crashlyticsFirebase(View v){
        Intent i = new Intent(RegistroTelefonoFirebase.this, CrashlyticsFirebase.class);
        startActivity(i);
        finish();
    }
    public void admobFirebase(View v){
        Intent i = new Intent(RegistroTelefonoFirebase.this, AdMobFirebase.class);
        startActivity(i);
        finish();
    }
    public void inviteLinksFirebase(View v){
        Intent i = new Intent(RegistroTelefonoFirebase.this, InviteLinkFirebase.class);
        startActivity(i);
        finish();
    }

}
