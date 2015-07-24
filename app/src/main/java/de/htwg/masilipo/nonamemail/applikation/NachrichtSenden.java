package de.htwg.masilipo.nonamemail.applikation;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.mail.Address;

import de.htwg.masilipo.nonamemail.R;
import de.htwg.masilipo.nonamemail.modell.AccountAuthentifizierung;
import de.htwg.masilipo.nonamemail.modell.AufgabenErgebnis;
import de.htwg.masilipo.nonamemail.werkzeug.ITaskAntwort;
import de.htwg.masilipo.nonamemail.werkzeug.NachrichtSendenTask;

/**
 * Created by MauRii on 19.07.2015.
 */
public class NachrichtSenden extends AppCompatActivity implements View.OnClickListener, ITaskAntwort, ViewTreeObserver.OnScrollChangedListener, Animation.AnimationListener {

    /*Action welche die Klasse ausführen kann*/
    public final static String AUF_NACHRICHT_ANTWORTEN = "ANTWORTEN";
    public final static String NEUE_NACHRICHT = "NEUE_NACHRICHT";

    /*Übergabeparameternamen wenn die Action "AUF_NACHRICHT_ANTWORTEN entspricht*/
    public final static String ANTWORT_NACHRICHT_EMPFAENGER = "ANTWORT_SENDEN_AN";
    public final static String ANTWORT_NACHRICHT_BETREFF = "ANTWORT_BETREFF";

    private Toolbar toolbar;
    private AccountAuthentifizierung authentifizierung;

    private ScrollView scrollAnsicht;
    private RelativeLayout ansichtContainer;
    private TextInputLayout sendenAn_til;
    private TextInputLayout cc_til;
    private TextInputLayout bcc_til;
    private TextInputLayout betreff_til;
    private int letztePosition;

    private EditText nachrichtEmpfaenger;
    private EditText nachrichtCc;
    private EditText nachrichtBcc;
    private EditText nachrichtBetreff;
    private EditText nachrichtInhalt;

    private ImageButton menuSchalter;

    private Animation toolbarAusblenden;
    private Animation toolbarEinblenden;
    private Boolean toolbarAusgeblendet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ansicht_nachricht_senden);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_activity_schliessen);

        ansichtContainer = (RelativeLayout) findViewById(R.id.senden_ansicht_container_relativelayout);

        scrollAnsicht = (ScrollView) findViewById(R.id.senden_ansicht_scrollview);
        scrollAnsicht.getViewTreeObserver().addOnScrollChangedListener(this);

        toolbarAusgeblendet = false;
        toolbarAusblenden = AnimationUtils.loadAnimation(this, R.anim.anim_toolbar_ausblenden);
        toolbarAusblenden.setAnimationListener(this);
        toolbarEinblenden = AnimationUtils.loadAnimation(NachrichtSenden.this, R.anim.anim_toolbar_einblenden);
        toolbarEinblenden.setAnimationListener(this);

        sendenAn_til = (TextInputLayout) findViewById(R.id.senden_sendenAn_textinputlayout);
        cc_til = (TextInputLayout) findViewById(R.id.senden_cc_textinputlayout);
        bcc_til = (TextInputLayout) findViewById(R.id.senden_bcc_textinputlayout);
        betreff_til = (TextInputLayout) findViewById(R.id.senden_betreff_textinputlayout);
        menuSchalter = (ImageButton) findViewById(R.id.senden_ccbcc_menu_button);
        nachrichtInhalt = (EditText) findViewById(R.id.senden_inhalt_nachricht);
        menuSchalter.setOnClickListener(this);

        nachrichtEmpfaenger = (EditText) findViewById(R.id.senden_sendenAn);
        nachrichtCc = (EditText) findViewById(R.id.senden_cc);
        nachrichtBcc = (EditText) findViewById(R.id.senden_bcc);
        nachrichtBetreff = (EditText) findViewById(R.id.senden_betreff);

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String json = mPrefs.getString("accAuth", "");
        authentifizierung = new Gson().fromJson(json, AccountAuthentifizierung.class);

        /*Auf Nachricht Antworten*/
        if(getIntent().getAction().equals(AUF_NACHRICHT_ANTWORTEN)){

            String antwortBetreff = getIntent().getStringExtra(ANTWORT_NACHRICHT_BETREFF);

            Address[] antwortAdressen = (Address[]) getIntent().getSerializableExtra(ANTWORT_NACHRICHT_EMPFAENGER);
            StringBuilder antwortAdressenSB = new StringBuilder();
            String antwortAdressenString = new String();
            for(Address antwortAdresse : antwortAdressen){
                antwortAdressenSB.append(antwortAdresse.toString());
                antwortAdressenSB.append("; ");
            }
            antwortAdressenString = antwortAdressenSB.toString();

            nachrichtEmpfaenger.setText(antwortAdressenString);
            nachrichtBetreff.setText("RE: " + antwortBetreff);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_toolbar_nachricht_senden, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem element) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (element.getItemId()) {
            case android.R.id.home:
                InputMethodManager eingabeManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                eingabeManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                onBackPressed();
                break;

            case R.id.action_nachricht_senden:

                HashMap<String, List<String>> datenHashMap = new HashMap<>();

                String empfaengerString = nachrichtEmpfaenger.getText().toString();
                String ccString = nachrichtCc.getText().toString();
                String bccString = nachrichtBcc.getText().toString();

                datenHashMap.put("TO", erzeugeEmpfaengerlisteAusString(empfaengerString));

                if (!TextUtils.isEmpty(ccString))
                    datenHashMap.put("CC", erzeugeEmpfaengerlisteAusString(ccString));
                if (!TextUtils.isEmpty(bccString))
                    datenHashMap.put("BCC", erzeugeEmpfaengerlisteAusString(bccString));

                List<String> betreffListe = new ArrayList<>();
                betreffListe.add(nachrichtBetreff.getText().toString());

                List<String> inhaltListe = new ArrayList<>();
                inhaltListe.add(nachrichtInhalt.getText().toString());

                datenHashMap.put("SUBJECT", betreffListe);
                datenHashMap.put("CONTENT", inhaltListe);

                new NachrichtSendenTask(this, authentifizierung).execute(datenHashMap);

                break;
        }

        return super.onOptionsItemSelected(element);
    }

    private List<String> erzeugeEmpfaengerlisteAusString(String empfaengerString) {

        String[] string = {};
        if(empfaengerString.contains(";")){

            string = empfaengerString.split("; ");
        } else if(empfaengerString.contains(", ")){

            string = empfaengerString.split("; ");
        }

        List<String> list = new ArrayList<>(string.length);
        for (String e : string)
            list.add(e);

        return list;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.senden_ccbcc_menu_button:

                //Anfangszustand des Kopfs
                if (cc_til.getVisibility() != View.VISIBLE) {

                    Animation ccBccEinblenden = AnimationUtils.loadAnimation(this, R.anim.abc_grow_fade_in_from_bottom);
                    ccBccEinblenden.setDuration(600);

                    Animation menuSchalterAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_menu_ccbcc_oeffnen);

                    cc_til.setVisibility(View.VISIBLE);
                    cc_til.startAnimation(ccBccEinblenden);

                    bcc_til.setVisibility(View.VISIBLE);
                    bcc_til.startAnimation(ccBccEinblenden);

                    menuSchalter.startAnimation(menuSchalterAnimation);

                } else {

                    Animation ccBccAusblenden = AnimationUtils.loadAnimation(this, R.anim.abc_shrink_fade_out_from_bottom);
                    ccBccAusblenden.setDuration(600);
                    ccBccAusblenden.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            cc_til.setVisibility(View.GONE);
                            bcc_til.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    Animation menuSchalterAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_menu_ccbcc_schliessen);

                    cc_til.startAnimation(ccBccAusblenden);
                    bcc_til.startAnimation(ccBccAusblenden);
                    menuSchalter.startAnimation(menuSchalterAnimation);
                }
                break;
        }
    }

    @Override
    public void serverAufgabeErledigt(String auszufuerendeAufgabe, AufgabenErgebnis ergebnis) {

        switch (auszufuerendeAufgabe) {
            case NachrichtSendenTask.NACHRICHT_SENDEN:

                break;
        }

    }

    @Override
    public void onScrollChanged() {

        int scrollY = scrollAnsicht.getScrollY();

        if (!toolbarAusgeblendet && letztePosition < scrollY && scrollY > nachrichtEmpfaenger.getBottom()) {
            toolbarAusgeblendet = true;
            Log.i("MyApp", "Scroll:" + "Toolbar ausgeblendet");

            toolbar.startAnimation(toolbarAusblenden);
//                    ansichtContainer.animate().translationYBy(-toolbar.getBottom()).setInterpolator(new BounceInterpolator()).setDuration(500).start();


        } else if (toolbarAusgeblendet && letztePosition > scrollY && scrollY < nachrichtEmpfaenger.getBottom()) {
            toolbarAusgeblendet = false;


            toolbar.startAnimation(toolbarEinblenden);
//                    ansichtContainer.animate().translationYBy(toolbar.getBottom()).setInterpolator(new BounceInterpolator()).setDuration(500).start();
//                    scrollAnsicht.animate().translationYBy(toolbar.getBottom()).setDuration(600).start();

            Log.i("MyApp", "Scroll:" + "Toolbar eingeblendet");

        }

        Log.i("MyApp", "ScrollY:" + String.valueOf(scrollY + toolbar.getHeight()));
        Log.i("MyApp", "ScrollY - Added:" + String.valueOf(scrollY + toolbar.getHeight()));
        Log.i("MyApp", "betreff getTop():" + String.valueOf(betreff_til.getTop()));
        Log.i("MyApp", "-----------------:" + String.valueOf(toolbarAusgeblendet));

        letztePosition = scrollY;
    }

    @Override
    public void onAnimationStart(Animation animation) {
        Log.i("MyApp", "Toolbar height - start:" + String.valueOf(toolbar.getHeight()));

        toolbar.setBottom(toolbar.getHeight());
        toolbar.setTop(0);

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        Log.i("MyApp", "Toolbar height - ende:" + String.valueOf(toolbar.getHeight()));

        if (animation == toolbarAusblenden) {
            toolbar.setTop(-toolbar.getHeight());
            toolbar.setBottom(0);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
