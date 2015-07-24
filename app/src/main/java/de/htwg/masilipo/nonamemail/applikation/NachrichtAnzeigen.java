package de.htwg.masilipo.nonamemail.applikation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.Address;

import de.htwg.masilipo.nonamemail.R;
import de.htwg.masilipo.nonamemail.modell.AccountAuthentifizierung;
import de.htwg.masilipo.nonamemail.modell.AufgabenErgebnis;
import de.htwg.masilipo.nonamemail.werkzeug.ITaskAntwort;
import de.htwg.masilipo.nonamemail.werkzeug.NachrichtAnzeigenTask;

/**
 * Created by MauRii on 21.07.2015.
 */
public class NachrichtAnzeigen extends AppCompatActivity implements ITaskAntwort, ViewTreeObserver.OnScrollChangedListener, Animation.AnimationListener {


    public final static String ANZEIGEN_ABSENDER = "NACHRICHT_ABSENDER";
    public final static String ANZEIGEN_BETREFF = "NACHRICHT_BETREFF";
    public final static String ANZEIGEN_DATUM = "NACHRICHT_DATUM";
    public final static String ANZEIGEN_IN_ORDNER = "NACHRICHT_IM_ORDNER";
    public final static String ANZEIGEN_NACHRICHT_UID = "NACHRICHT_UID";
    public final static String ANZEIGEN_ANTWORTEN_ADRESSEN = "NACHRICHT_ANTWORTEN";

    private Toolbar toolbar;
    private WebView nachrichtAnzeigenInhalt;
    private ScrollView scrollBehaelter;
    private View nachrichtAnzeigenDivisor;
    FloatingActionButton antwortenFab;

    private boolean toolbarAusgeblendet;
    private int letztesScrollY;

    private Animation toolbarAusblenden, toolbarEinblenden, antwortenFabAusblenden, antwortenFabEinblenden;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ansicht_nachricht_anzeigen);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_activity_schliessen);
        toolbarAusgeblendet = false;

        scrollBehaelter = (ScrollView) findViewById(R.id.nachrichtAnzeigen_scrollBehaelter);
        scrollBehaelter.getViewTreeObserver().addOnScrollChangedListener(this);
        nachrichtAnzeigenDivisor = findViewById(R.id.nachrichtAnzeigen_divisor_kopf);

        TextView nachrichtAnzeigenAbsender = (TextView) findViewById(R.id.nachrichtAnzeigen_absender);
        TextView nachrichtAnzeigenBetreff = (TextView) findViewById(R.id.nachrichtAnzeigen_betreff);
        TextView nachrichtAnzeigenDatum = (TextView) findViewById(R.id.nachrichtAnzeigen_datum);
        nachrichtAnzeigenInhalt = (WebView) findViewById(R.id.nachrichtAnzeigen_inhalt);


        toolbarAusblenden = AnimationUtils.loadAnimation(this, R.anim.anim_toolbar_ausblenden);
        toolbarAusblenden.setAnimationListener(this);
        toolbarEinblenden = AnimationUtils.loadAnimation(this, R.anim.anim_toolbar_einblenden);
        toolbarEinblenden.setAnimationListener(this);
        antwortenFabAusblenden = AnimationUtils.loadAnimation(this, R.anim.anim_neue_nachricht_fab_ausblenden);
        antwortenFabAusblenden.setAnimationListener(this);
        antwortenFabEinblenden = AnimationUtils.loadAnimation(this, R.anim.anim_neue_nachricht_fab_einblenden);
        antwortenFabEinblenden.setAnimationListener(this);

        Intent nachrichtAnzeigenIntent = getIntent();
        Long nachrichtUid = nachrichtAnzeigenIntent.getLongExtra(ANZEIGEN_NACHRICHT_UID, -1l);
        String vollerOrdnername = nachrichtAnzeigenIntent.getStringExtra(ANZEIGEN_IN_ORDNER);
        Address[] absenderAdressen = (Address[]) nachrichtAnzeigenIntent.getSerializableExtra(ANZEIGEN_ABSENDER);
        final Address[] antwortAdressen = (Address[]) nachrichtAnzeigenIntent.getSerializableExtra(ANZEIGEN_ANTWORTEN_ADRESSEN);
        final String betreffString = nachrichtAnzeigenIntent.getStringExtra(ANZEIGEN_BETREFF);
        Date empfangsDatum = (Date) nachrichtAnzeigenIntent.getSerializableExtra(ANZEIGEN_DATUM);
        String empfangsDatumString = new SimpleDateFormat("dd.MMMM HH:mm").format(empfangsDatum);

        antwortenFab = (FloatingActionButton) findViewById(R.id.nachrichtAnzeigen_antworten_fab);
        antwortenFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nachrichtAntwortenIntent = new Intent(NachrichtAnzeigen.this, NachrichtSenden.class);
                nachrichtAntwortenIntent.setAction(NachrichtSenden.AUF_NACHRICHT_ANTWORTEN);
                nachrichtAntwortenIntent.putExtra(NachrichtSenden.ANTWORT_NACHRICHT_EMPFAENGER, getIntent().getSerializableExtra(NachrichtAnzeigen.ANZEIGEN_ANTWORTEN_ADRESSEN));
                nachrichtAntwortenIntent.putExtra(NachrichtSenden.ANTWORT_NACHRICHT_BETREFF, betreffString);
                startActivity(nachrichtAntwortenIntent);
            }
        });

        nachrichtAnzeigenAbsender.setText(absenderAdressen[0].toString());
        nachrichtAnzeigenBetreff.setText(getIntent().getStringExtra(ANZEIGEN_BETREFF));
        nachrichtAnzeigenDatum.setText(empfangsDatumString);

        SharedPreferences einstellungen = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String json = einstellungen.getString("accAuth", "");
        AccountAuthentifizierung authentifizierung = new Gson().fromJson(json, AccountAuthentifizierung.class);

        new NachrichtAnzeigenTask(this, authentifizierung).execute(nachrichtUid.toString(), vollerOrdnername);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_toolbar_nachricht_anzeigen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void serverAufgabeErledigt(String auszufuerendeAufgabe, AufgabenErgebnis ergebnis) {

        Map<String, String> nachrichtInhaltListe = (HashMap<String, String>) ergebnis.getErgebnis();
        String mimeType;
        String encoding;
        String inhalt;

        nachrichtAnzeigenInhalt.getSettings().setUseWideViewPort(true);
        nachrichtAnzeigenInhalt.getSettings().setLoadWithOverviewMode(true);
        nachrichtAnzeigenInhalt.getSettings().setSupportZoom(true);
        nachrichtAnzeigenInhalt.getSettings().setBuiltInZoomControls(true);
        nachrichtAnzeigenInhalt.getSettings().setDisplayZoomControls(true);
        nachrichtAnzeigenInhalt.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);

        nachrichtAnzeigenInhalt.getSettings().setAllowFileAccessFromFileURLs(false);
        nachrichtAnzeigenInhalt.getSettings().setAllowContentAccess(false);
        nachrichtAnzeigenInhalt.getSettings().setDefaultFontSize(30);
        nachrichtAnzeigenInhalt.getSettings().setAllowContentAccess(false);
        nachrichtAnzeigenInhalt.getSettings().setAllowUniversalAccessFromFileURLs(false);




        for(Map.Entry<String, String> eintrag : nachrichtInhaltListe.entrySet()){

            if(eintrag.getKey().toLowerCase().contains("text/html")){
                String key = eintrag.getKey();
                mimeType = key.split(", ")[0];
                encoding = key.substring(key.indexOf("=") + 1, key.length());
                inhalt = eintrag.getValue();
                if( inhalt.contains("<head>") && !inhalt.contains("width=device-width"))
                    inhalt = inhalt.replace("<head>", "<head><meta name=\"viewport\" content=\"width=device-width, user-scalable=yes\" /></head>");
                nachrichtAnzeigenInhalt.loadDataWithBaseURL("", inhalt, mimeType, encoding, "");
                break;

            } else if(eintrag.getKey().toLowerCase().contains("text/plain")){

                String key = eintrag.getKey();
                mimeType = key.split(", ")[0];
                encoding = key.substring(key.indexOf("=") + 1, key.length());
                inhalt = eintrag.getValue();
                nachrichtAnzeigenInhalt.loadDataWithBaseURL("", inhalt, mimeType, encoding, "");
            }

            Log.i("MyApp", eintrag.getKey());
        }
    }

    @Override
    public void onScrollChanged() {

        int scrollY = scrollBehaelter.getScrollY();


        if(!toolbarAusgeblendet && letztesScrollY<scrollY && (scrollY + toolbar.getHeight())>nachrichtAnzeigenDivisor.getTop()){

            toolbarAusgeblendet = true;
            toolbar.startAnimation(toolbarAusblenden);
            antwortenFab.startAnimation(antwortenFabAusblenden);

        } else if(toolbarAusgeblendet && letztesScrollY>scrollY && (scrollY + toolbar.getHeight())< nachrichtAnzeigenDivisor.getTop()){

            toolbarAusgeblendet = false;
            toolbar.startAnimation(toolbarEinblenden);
            antwortenFab.startAnimation(antwortenFabEinblenden);

        }

        letztesScrollY = scrollY;

    }

    @Override
    public void onAnimationStart(Animation animation) {
        toolbar.setBottom(toolbar.getHeight());
        toolbar.setTop(0);

        final int hoeheFab = antwortenFab.getHeight();
        final int paddingUntenFab = antwortenFab.getPaddingBottom();
        antwortenFab.setTop(scrollBehaelter.getBottom() - hoeheFab - paddingUntenFab);
        antwortenFab.setBottom(scrollBehaelter.getBottom() - paddingUntenFab);
    }

    @Override
    public void onAnimationEnd(Animation animation) {

        if(animation == toolbarAusblenden){
            toolbar.setTop(-toolbar.getHeight());
            toolbar.setBottom(0);
        }

        else if(animation == antwortenFabAusblenden){
            final int hoeheFab = antwortenFab.getHeight();
            final int paddingUntenFab = antwortenFab.getPaddingBottom();
            antwortenFab.setTop(scrollBehaelter.getBottom());
            antwortenFab.setBottom(scrollBehaelter.getBottom() + hoeheFab);

        } else if (animation == antwortenFabEinblenden) {

            final int hoeheFab = antwortenFab.getHeight();
            final int paddingUntenFab = antwortenFab.getPaddingBottom();
            antwortenFab.setTop(scrollBehaelter.getBottom() - hoeheFab - paddingUntenFab);
            antwortenFab.setBottom(scrollBehaelter.getBottom() - paddingUntenFab);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }


}
