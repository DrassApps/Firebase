package com.drassapps.firebaseapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Esta clase nos permite recoger datos de la BD de nuestro proyecto Firebase estructurados en un
 * formato Json, al proporcionar Firebase un BD en tiempo real tras modificar los datos o
 * actualizarlos en la propia BD se modificaran en los dispositivos sin tener que
 * estar consultando constantemente. Tanto para hacer las actualizaciones
 * como para consultar nos pide en el método un argumento que será
 * un Objeto de nuestra BD.
 */

public class BDFirebase extends Activity{

    private static final String TAG = "BDFirebase";

    TextView bdNombre, bdToken;                // Usuario y Token de la BD

    private FirebaseAuth mAuth;                // Firebase Authentication SDK

    private View mLayout;                      // Vista principal
    private LinearLayout dummy_line;           // Dumy linear que ser el focus en onStart

    private DrawerLayout drawerLayout;         // Creacion de var DrawerLayout para su posterior uso
    private NavigationView bundle;             // Creacion de var NavigationView

    private ImageView menu_nav, bt_menu_nav;   // ImageView que controlan el navView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basedatos_firebase);

        // Inicializamos Firebase
        mAuth = FirebaseAuth.getInstance();

        // Accedemos al raiz de la BD de nuestro proyecto en Firebase
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        // Accedemos a su hijo, que al tener solo uno sera Usuario
        final DatabaseReference mDatabaseRefUsuario = mDatabaseRef.child("Usuario");

        // Elementos de la UI
        mLayout = findViewById(R.id.lay_main_bd);
        bdNombre = (TextView) findViewById(R.id.bd_nombre);
        bdToken = (TextView) findViewById(R.id.bd_token);
        final EditText bdNombreEd = (EditText) findViewById(R.id.ed_bd_nombre);
        Button modificarNombrebBd = (Button) findViewById(R.id.bt_modi_nombre_bd);

        // Actualizamos los datos en la BD
        modificarNombrebBd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Cogemos en un string el nombre introducido por el usuario
                String nombre = bdNombreEd.getText().toString();

                // Si es mayor de 3
                if (nombre.length() > 3) {

                    // Cogemos tambien el token de sesion activa del usuario
                    String token = FirebaseInstanceId.getInstance().getToken();

                    // Inicializamos un usuario de la BD
                    /** UsuarioObjectDBFirebase es una clase especial que se ha creado **/
                    UsuarioObjectDBFirebase usuarioBD =
                            new UsuarioObjectDBFirebase(nombre,token,null);

                    // Actualizamos los datos
                    mDatabaseRefUsuario.setValue(usuarioBD);

                }else{ setSnackBar(mLayout,getString(R.string.error_text_bd)); }
            }
        });

        // Este método se llama una vez cuando pedimos datos a la BD y siempre que haya una
        // modificacion en la BD de Firebase de tal forma que siempre tenemos los datos
        // actualizados sin tener que estar preguntando. Es por ello que al pulsar
        // sobre modificar el TextView se cambiará al texto subido en la BD.
        mDatabaseRefUsuario.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Obtenemos un objeto de nuestra BD
                UsuarioObjectDBFirebase usu = dataSnapshot.getValue(UsuarioObjectDBFirebase.class);

                if (usu != null){
                    bdNombre.setText(usu.Nombre);
                    bdToken.setText(getString(R.string.token_usu_bd)+ "\n\n" + usu.Token);

                }else{
                    // Error no hay datos
                    setSnackBar(mLayout,getString(R.string.null_data));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Fallo al leer un valor
                Log.w(TAG, getString(R.string.error_leer_bd), error.toException());
                setSnackBar(mLayout,getString(R.string.error_leer_bd));
            }
        });

        // Asignamos las ImageView de la vista
        menu_nav = (ImageView) findViewById(R.id.menu_nav_bd);
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

    // Para mostrar el resto de funcionalidades de Auth, cerramos la sesion al salir de la vista
    @Override
    public void onStop() {
        super.onStop();
        // Al cerrar la vista comprueba si hay algun usuario activo
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Si lo hay, cerramos la sesion
        if (currentUser != null){
            mAuth.signOut();
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
            volverHome();
        }
    }

    // Metodos para pasar a las otras actividades desde el menu
    public void mainvView(View v){
        Intent i = new Intent(BDFirebase.this, MainActivity.class);
        startActivity(i);
        finish();
    }
    public void volverHome(){
        Intent i = new Intent(BDFirebase.this, MainActivity.class);
        startActivity(i);
        finish();
    }
    public void registroEmailView(View v){
        Intent i = new Intent(BDFirebase.this, RegistroGoogleFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroGoogleView(View v){
        Intent i = new Intent(BDFirebase.this, RegistroGoogleFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroFacebookView(View v){
        Intent i = new Intent(BDFirebase.this, RegistroFacebookFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroTwitterView(View v){
        Intent i = new Intent(BDFirebase.this, RegistroTwitterFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroTelefonoView(View v){
        Intent i = new Intent(BDFirebase.this, RegistroTelefonoFirebase.class);
        startActivity(i);
        finish();
    }
    public void notificacionPush(View v){
        Intent i = new Intent(BDFirebase.this, NotificacionPushFirebase.class);
        startActivity(i);
        finish();
    }
    public void bdFirebase(View v){
        drawerLayout.closeDrawer(Gravity.START);
    }
    public void almacenamientoFirebase(View v){
        Intent i = new Intent(BDFirebase.this, AlmacenamientoFirebase.class);
        startActivity(i);
        finish();
    }
    public void crashlyticsFirebase(View v){
        Intent i = new Intent(BDFirebase.this, CrashlyticsFirebase.class);
        startActivity(i);
        finish();
    }
    public void admobFirebase(View v){
        Intent i = new Intent(BDFirebase.this, AdMobFirebase.class);
        startActivity(i);
        finish();
    }
    public void inviteLinksFirebase(View v){
        Intent i = new Intent(BDFirebase.this, InviteLinkFirebase.class);
        startActivity(i);
        finish();
    }
}
