package com.drassapps.firebaseapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;


/**
 * Esta clase nos permite inicializar diversas banners de publicidad gracias a la herramienta de
 * AdMob incorporada en Firebase. Dependiendo del botón seleccionado, podremos ver:
 *  1. Un banner simple en la pantalla que se mantiene
 *  2. Un banner a pantalla completa
 *  3. Un video recompensado que nos da la posibilidad de recompensar al usuario por ver ese video
 *
 *  Todos los bloques están tienen un ID de prueba de Firebase por lo que no hay posibilida de
 *  falsear las estadisticas
 */

public class AdMobFirebase extends Activity implements RewardedVideoAdListener {

    private static final String TAG = "AlmacenamientoFirebase";

    private View mLayout;                      // Vista principal

    private DrawerLayout drawerLayout;         // Creacion de var DrawerLayout para su posterior uso
    private NavigationView bundle;             // Creacion de var NavigationView

    private ImageView menu_nav, bt_menu_nav;   // ImageView que controlan el navView

    private AdView adView;                     // Banner publicitario de AdMob
    private InterstitialAd mInterstitialAd;    // Interstitial publicitario de AdMob
    private RewardedVideoAd mRewardedVideoAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admob_firebase);

        // Inicializamos AdMob con nuestro APP ID
        MobileAds.initialize(this, getString(R.string.appIDAdmob));

        // Asignamos el InterstitialAd y asignamos su id de prueba, necesario generarlo fuera
        mInterstitialAd = new InterstitialAd(AdMobFirebase.this);
        mInterstitialAd.setAdUnitId(getString(R.string.appIDAdmobInter));

        // Asignamos la vista
        mLayout = findViewById(R.id.lay_main_admob);

        // Botonos de la UI
        Button banner1 = (Button) findViewById(R.id.bt_admob_banner_low);
        Button banner2 = (Button) findViewById(R.id.bt_admob_banner_full);
        Button banner3 = (Button) findViewById(R.id.bt_admob_banner_video);

        // Asignamos el adview
        adView = (AdView) findViewById(R.id.adView);

        /**
         * Los anuncios de banner son anuncios rectangulares de imagen o texto que ocupan un lugar
         * dentro del diseño de la app. Permanecen en la pantalla mientras los usuarios
         * interactúan con la app y se pueden actualizar de forma automática después
         * de un período de tiempo determinado.
         */
        banner1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Lo hacemos visitble
                adView.setVisibility(View.VISIBLE);
                // Generamos el banner
                AdRequest adRequest = new AdRequest.Builder().build();
                // Lo mostramos
                adView.loadAd(adRequest);
            }
        });

        /**
         * Los intersticiales son anuncios de pantalla completa que cubren la interfaz de una app
         * hasta que el usuario los cierra. Lo más recomendable es usarlos en pausas naturales
         * dentro del flujo de ejecución de una app, como entre distintos niveles de un
         * juego o justo después de completar una tarea.
         */
        banner2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Lo cargamos
                loadInterstitialAd();

                // Si ya esta cargado lo mostramos
                if(mInterstitialAd.isLoaded()){
                    mInterstitialAd.show();
                }
            }
        });

        /**
         * Los anuncios nativos son un formato de anuncio basado en componentes que te da la
         * libertad de personalizar la manera en que los recursos, como los encabezados y
         * los llamados a la acción, se presentan en tu app. Puedes elegir la fuente,
         * los colores y otros detalles para crear presentaciones de anuncios
         * naturales y discretas que pueden contribuir a una experiencia de
         * usuario enriquecida.
         */
        banner3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Inizializa el Video
                mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(AdMobFirebase.this);
                mRewardedVideoAd.setRewardedVideoAdListener(AdMobFirebase.this);

                // Lo carga y lo muestra
                loadRewardedVideoAd();

                // Si ya esta cargado lo mostramos
                if (mRewardedVideoAd.isLoaded()){
                    mRewardedVideoAd.show();
                }
            }
        });

        // Asignamos las ImageView de la vista
        menu_nav = (ImageView) findViewById(R.id.menu_nav_admob);
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

    // Carga la vista Interstitial y lo prepara para mostrarlo
    private void loadInterstitialAd(){
        if(!mInterstitialAd.isLoading() && !mInterstitialAd.isLoaded()){
            AdRequest adRequest = new AdRequest.Builder().build();
            mInterstitialAd.loadAd(adRequest);
            setSnackBar(mLayout,"InterstitialAd cargado, pulsa de nuevo");
        }else{
            setSnackBar(mLayout,"uhmmm");
        }
    }

    // Carga el video y lo prepara para mostrarlo
    private void loadRewardedVideoAd() {
        if (!mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.loadAd(getString(R.string.appIDAdmobVideo),
                    new AdRequest.Builder().build());
            setSnackBar(mLayout,"Video cargado, pulsa de nuevo");
        }
    }

    // Cuando cerramos el video mostrar cargamos de nuevo el video para laznarlo
    @Override
    public void onRewardedVideoAdClosed() {
        setSnackBar(mLayout,"Has cerrado el video");
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

    // [Metodos que permiten menajear los eventos del video remunerado]
    @Override
    public void onRewardedVideoAdLeftApplication() {}
    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {}
    @Override
    public void onRewardedVideoAdLoaded() {}
    @Override
    public void onRewardedVideoAdOpened() {}
    /**Cuando el video nos devuelve la recompensa, se la asignamos al usuario (depende de la app)*/
    @Override
    public void onRewarded(RewardItem reward) {}
    @Override
    public void onRewardedVideoStarted() {}
    // [FIN METODOS]

    @Override
    public void onBackPressed() {
        volverHome();
    }

    // Metodos para pasar a las otras actividades desde el menu
    public void mainvView(View v){
        Intent i = new Intent(AdMobFirebase.this, MainActivity.class);
        startActivity(i);
        finish();
    }
    public void volverHome(){
        Intent i = new Intent(AdMobFirebase.this, MainActivity.class);
        startActivity(i);
        finish();
    }
    public void registroEmailView(View v){
        Intent i = new Intent(AdMobFirebase.this, RegistroEmailFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroGoogleView(View v){
        Intent i = new Intent(AdMobFirebase.this, RegistroGoogleFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroFacebookView(View v){
        Intent i = new Intent(AdMobFirebase.this, RegistroFacebookFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroTwitterView(View v){
        Intent i = new Intent(AdMobFirebase.this, RegistroTwitterFirebase.class);
        startActivity(i);
        finish();
    }
    public void registroTelefonoView(View v){
        Intent i = new Intent(AdMobFirebase.this, RegistroTelefonoFirebase.class);
        startActivity(i);
        finish();
    }
    public void notificacionPush(View v){
        Intent i = new Intent(AdMobFirebase.this, NotificacionPushFirebase.class);
        startActivity(i);
        finish();
    }
    public void bdFirebase(View v){
        Intent i = new Intent(AdMobFirebase.this, BDFirebase.class);
        startActivity(i);
        finish();
    }
    public void almacenamientoFirebase(View v){
        Intent i = new Intent(AdMobFirebase.this, AlmacenamientoFirebase.class);
        startActivity(i);
        finish();
    }
    public void crashlyticsFirebase(View v){
        Intent i = new Intent(AdMobFirebase.this, CrashlyticsFirebase.class);
        startActivity(i);
        finish();
    }
    public void admobFirebase(View v){
        drawerLayout.closeDrawer(Gravity.START);
    }
    public void inviteLinksFirebase(View v){
        Intent i = new Intent(AdMobFirebase.this, InviteLinkFirebase.class);
        startActivity(i);
        finish();
    }
}
