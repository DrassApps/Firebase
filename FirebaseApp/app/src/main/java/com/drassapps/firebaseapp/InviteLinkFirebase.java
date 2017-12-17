package com.drassapps.firebaseapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.appinvite.AppInviteInvitation;

public class InviteLinkFirebase extends Activity {

    private static final String TAG = "InviteLinkFirebase";

    private static final int REQUEST_INVITE = 0;

    private DrawerLayout drawerLayout;         // Creacion de var DrawerLayout para su posterior uso
    private NavigationView bundle;             // Creacion de var NavigationView

    private ImageView menu_nav, bt_menu_nav;   // ImageView que controlan el navView


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invitelinks_firebase);

        Button inviteLink = (Button) findViewById(R.id.bt_invite);

        inviteLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(InviteLinkFirebase.this);

                builder.setMessage(getString(R.string.invitation_d2))
                        .setTitle(getString(R.string.invitation_d1))
                        .setNegativeButton(getString(R.string.emailNoVerificado),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .setPositiveButton(getString(R.string.emailVerificado),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // Enviamos el intivte
                                        onInviteClicked();
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
        menu_nav = (ImageView) findViewById(R.id.menu_nav_invite);
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

    private void onInviteClicked() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d(TAG, "onActivityResult: sent invitation " + id);
                }
            } else {Log.d(TAG,"onActivityResult: Fail");}
        }
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
        Intent i = new Intent(InviteLinkFirebase.this, MainActivity.class);
        startActivity(i);
        finish();
    }
    public void volverHome(){
        Intent i = new Intent(InviteLinkFirebase.this, MainActivity.class);
        startActivity(i);
        finish();
    }
    public void registroEmailView(View v){
        Intent i = new Intent(InviteLinkFirebase.this, RegistroEmailFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroGoogleView(View v){
        Intent i = new Intent(InviteLinkFirebase.this, RegistroGoogleFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroFacebookView(View v){
        Intent i = new Intent(InviteLinkFirebase.this, RegistroFacebookFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroTwitterView(View v){
        Intent i = new Intent(InviteLinkFirebase.this, RegistroTwitterFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroTelefonoView(View v){
        Intent i = new Intent(InviteLinkFirebase.this, RegistroTelefonoFirebase.class);
        startActivity(i);
        finish();
    }
    public void notificacionPush(View v){
        Intent i = new Intent(InviteLinkFirebase.this, NotificacionPushFirebase.class);
        startActivity(i);
        finish();
    }
    public void bdFirebase(View v){
        Intent i = new Intent(InviteLinkFirebase.this, BDFirebase.class);
        startActivity(i);
        finish();
    }
    public void almacenamientoFirebase(View v){
        Intent i = new Intent(InviteLinkFirebase.this, AlmacenamientoFirebase.class);
        startActivity(i);
        finish();
    }
    public void crashlyticsFirebase(View v){
        Intent i = new Intent(InviteLinkFirebase.this, CrashlyticsFirebase.class);
        startActivity(i);
        finish();
    }
    public void admobFirebase(View v){
        Intent i = new Intent(InviteLinkFirebase.this, AdMobFirebase.class);
        startActivity(i);
        finish();
    }
    public void inviteLinksFirebase(View v){
        drawerLayout.closeDrawer(Gravity.START);
    }
}
