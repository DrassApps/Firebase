package com.drassapps.firebaseapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;

/**
 * Esta clase nos permite interactuar con el almacenamiento de Firebase, pensado para almacenar
 * archivos de cualquier de tipo. Hay que diferenciarlo de la BD en tiempo real pues en ella
 * se guarda informacion en texto plano, mientras que en esta se almacenan videos, fotos,
 * documentos...
 *
 * La actividad nos permite recoger una imagen de la galería y subirla al almacenamiento utilizando
 * la API de Firebase, además también podremos recuperar una imagen de prueba
 */

public class AlmacenamientoFirebase extends Activity {

    private static final String TAG = "AlmacenamientoFirebase";

    private View mLayout;                      // Vista principal

    public static final int PICK_IMAGE = 1;    // ID para el codigo de ActityResult

    private Bitmap selectedImage;              // Bitmap de la imagen seleccionada en galeria
    private Uri imageUri;                      // URI a la ubicación de la imagen

    private ImageView image_foto;              // Imagen a elegir

    private DrawerLayout drawerLayout;         // Creacion de var DrawerLayout para su posterior uso
    private NavigationView bundle;             // Creacion de var NavigationView

    private ImageView menu_nav, bt_menu_nav;   // ImageView que controlan el navView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.almacenamiento_firebase);

        // Elementos de la UI
        image_foto = (ImageView) findViewById(R.id.almacenamiento_imagen);
        mLayout = findViewById(R.id.lay_main_alm);
        Button descargarImagen = (Button) findViewById(R.id.bt_descargar_imagen);
        Button subirImagen = (Button) findViewById(R.id.bt_subir_imagen);

        // Abrimos un intent a la galeria para que el usuario elija una imagen de la galeria
        image_foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });

        // Subimos la imagen al almacenamiento de Firebase
        subirImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Comprobamos que hemos elegido una imagen
                if (imageUri != null){

                    // Creamos una referencia al amacenamiento de nuestra app en Firebase
                    StorageReference ref =
                            FirebaseStorage.getInstance().getReference().child("imagen1");

                    // Subimos la imagen aplicacion Firebase a nuestra referencia
                    UploadTask uploadTask = ref.putFile(imageUri);

                    // Generamos un listener
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Error al subir la imagen
                            setSnackBar(mLayout,getString(R.string.err_imagen));
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Imagen subida correctamente
                            setSnackBar(mLayout,getString(R.string.ok_imagen));
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            Log.d(TAG,downloadUrl.toString());
                        }
                    });

                }else{ setSnackBar(mLayout,getString(R.string.selec_imagen)); }
            }
        });

        // Descargamos la imagen del almacenamiento de Firebase
        descargarImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Creamos una referencia a la imagen random almacenda en Firebase
                StorageReference ref =
                        FirebaseStorage.getInstance().getReference().child("25824257.png");

                // Cargamos la imagen usando Glide
                Glide.with(AlmacenamientoFirebase.this)
                        .using(new FirebaseImageLoader())
                        .load(ref)
                        .into(image_foto);

                setSnackBar(mLayout,getString(R.string.dwm_imagen));
            }
        });

        // Asignamos las ImageView de la vista
        menu_nav = (ImageView) findViewById(R.id.menu_nav_alm);
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

    // Recogemos la imagen elegida por el usuario
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Si ha elegido una imagen
        if (requestCode == PICK_IMAGE) {
            try{
                // Si tenemos datos
                if (data != null){

                    // Obtenemos una URI de la ubicación de la imagen
                    imageUri = data.getData();

                    // Abrimos la URI a través de un InputStram
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);

                    // Convertimos ese inputstream a un BitMap
                    selectedImage = BitmapFactory.decodeStream(imageStream);

                    // La asignamos a la ImagevIEW
                    image_foto.setImageBitmap(selectedImage);

                    // Si no hay data significa que el usuario no ha elegido ninguna imagen
                }else{ setSnackBar(mLayout,getString(R.string.error_pick_imagen)); }

            } catch (IOException e) { e.printStackTrace(); }

        }else{ setSnackBar(mLayout,getString(R.string.error_pick_imagen)); }
    }

    // Fuerza la creacion de un SnackBar, personificado
    public static void setSnackBar(View coordinatorLayout, String snackTitle) {
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
        Intent i = new Intent(AlmacenamientoFirebase.this, MainActivity.class);
        startActivity(i);
        finish();
    }
    public void volverHome(){
        Intent i = new Intent(AlmacenamientoFirebase.this, MainActivity.class);
        startActivity(i);
        finish();
    }
    public void registroEmailView(View v){
        Intent i = new Intent(AlmacenamientoFirebase.this, RegistroEmailFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroGoogleView(View v){
        Intent i = new Intent(AlmacenamientoFirebase.this, RegistroGoogleFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroFacebookView(View v){
        Intent i = new Intent(AlmacenamientoFirebase.this, RegistroFacebookFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroTwitterView(View v){
        Intent i = new Intent(AlmacenamientoFirebase.this, RegistroTwitterFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroTelefonoView(View v){
        Intent i = new Intent(AlmacenamientoFirebase.this, RegistroTelefonoFirebase.class);
        startActivity(i);
        finish();
    }
    public void notificacionPush(View v){
        Intent i = new Intent(AlmacenamientoFirebase.this, NotificacionPushFirebase.class);
        startActivity(i);
        finish();
    }
    public void bdFirebase(View v){
        Intent i = new Intent(AlmacenamientoFirebase.this, BDFirebase.class);
        startActivity(i);
        finish();
    }
    public void almacenamientoFirebase(View v){
        drawerLayout.closeDrawer(Gravity.START);
    }
    public void crashlyticsFirebase(View v){
        Intent i = new Intent(AlmacenamientoFirebase.this, CrashlyticsFirebase.class);
        startActivity(i);
        finish();
    }
    public void admobFirebase(View v){
        Intent i = new Intent(AlmacenamientoFirebase.this, AdMobFirebase.class);
        startActivity(i);
        finish();
    }
    public void inviteLinksFirebase(View v){
        Intent i = new Intent(AlmacenamientoFirebase.this, InviteLinkFirebase.class);
        startActivity(i);
        finish();
    }
}
