package de.htwg.masilipo.nonamemail.applikation;

import de.htwg.masilipo.nonamemail.*;
import de.htwg.masilipo.nonamemail.modell.AccountAuthentifizierung;
import de.htwg.masilipo.nonamemail.werkzeug.ITaskAntwort;
import de.htwg.masilipo.nonamemail.werkzeug.DatenAusISPDB;
import de.htwg.masilipo.nonamemail.modell.AufgabenErgebnis;
import de.htwg.masilipo.nonamemail.werkzeug.LoginAuthentifiezierungTask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;

import javax.mail.AuthenticationFailedException;

/**
 * Created by MauRii on 07.07.2015.
 */
public final class LoginAutomatisch extends AppCompatActivity implements View.OnClickListener, ITaskAntwort {

    private AccountAuthentifizierung authentifizierung;
    private ProgressBar ablaufDiagramm;
    private TextView ablaufDiagrammText;
    private EditText et_loginEmail, et_loginPassword;
    private Button loginTaste;
    private TextInputLayout til_loginEmail;
    private String loginEmail, loginPassword, eingangsserverHost, eingangsserverPort, incomingServerSocketType,
            ausgangsserverHost, ausgangsserverPort, outgoingServerSocketType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ansicht_login_automatisch);

        //Automatische anmeldung wenn USer schon einmal angemeldet, für Neuamledung muss sich der User ausloggen
//        SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
//        Gson gson = new Gson();
//        String json = mPrefs.getString("accAuth", "");
//        authentifizierung = gson.fromJson(json, AccountAuthentifizierung.class);

//        new ImapServerAnfragenAsyncTask(this, authentifizierung, ImapServerAnfragenAsyncTask.ORDNER).execute();

        if (true) {//!json.equals("")) {
//            getWindow().setBackgroundDrawableResource(R.drawable.konstanz_imperia); -- Api<21 ..
            getWindow().setBackgroundDrawable(getDrawable(R.drawable.konstanz_imperia));


            loginTaste = (Button) findViewById(R.id.login_abschicken_button);
            loginTaste.setOnClickListener(this);

            et_loginEmail = (EditText) findViewById(R.id.login_email_edittext);
            til_loginEmail = (TextInputLayout) findViewById(R.id.login_email_textinputlayout);
            et_loginPassword = (EditText) findViewById(R.id.login_passwort_edittext);

        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.login_abschicken_button:

                loginEmail = et_loginEmail.getText().toString();
                loginPassword = et_loginPassword.getText().toString();

                //Prüfe ob Form korrekt aufgefüllt
                if (!TextUtils.isEmpty(loginPassword) && emailEingabeKorrekt(loginEmail)) {

                    String domain = loginEmail.substring(loginEmail.indexOf("@") + 1, loginEmail.length());

                    if (internetverbindungVorhanden()) {

                        et_loginEmail.setEnabled(false);
                        et_loginPassword.setEnabled(false);
                        loginTaste.setEnabled(false);

                        new DatenAusISPDB(this).execute(domain);
                    }

                }
                break;
        }
    }

    @Override
    public void serverAufgabeErledigt(String ausgefuerteAufgabe, AufgabenErgebnis ergebnis) {

        if (ergebnis != null && ergebnis.istGueltig()) {

            switch (ausgefuerteAufgabe) {
                case DatenAusISPDB.ERHALTE_ISP_DATEN:

                    HashMap<String, NodeList> ispDatenHashMap = (HashMap<String, NodeList>) ergebnis.getErgebnis();

                    NodeList eingangsserverElemente = ispDatenHashMap.get("incomingServer");
                    NodeList ausgangsserverElemente = ispDatenHashMap.get("outgoingServer");

                    if (eingangsserverElemente != null && ausgangsserverElemente != null) {
                        int elementLaenge = eingangsserverElemente.getLength();
                        for (int index = 0; index < elementLaenge; index++) {

                            Node eingangsserverKnoten = eingangsserverElemente.item(index);
                            Node ausgangsserverKnoten = ausgangsserverElemente.item(index);
                            //Set ISP-Data
                            switch (eingangsserverKnoten.getNodeName()) {
                                case "hostname":
                                    eingangsserverHost = eingangsserverKnoten.getFirstChild().getNodeValue();
                                    ausgangsserverHost = ausgangsserverKnoten.getFirstChild().getNodeValue();
                                    break;
                                case "port":
                                    eingangsserverPort = eingangsserverKnoten.getFirstChild().getNodeValue();
                                    ausgangsserverPort = ausgangsserverKnoten.getFirstChild().getNodeValue();
                                    break;
                                case "socketType":
                                    incomingServerSocketType = eingangsserverKnoten.getFirstChild().getNodeValue();
                                    outgoingServerSocketType = ausgangsserverKnoten.getFirstChild().getNodeValue();
                                    break;
                            }
                        }

                    }

                    if (pruefeIspDaten()) {

                        authentifizierung = new AccountAuthentifizierung(loginEmail, loginPassword,
                                eingangsserverHost, Integer.parseInt(eingangsserverPort), ausgangsserverHost, Integer.parseInt(ausgangsserverPort));
                        new LoginAuthentifiezierungTask(this, authentifizierung).execute();

                    } else {

                        Intent loginManuell = new Intent(this, LoginManuell.class);
                        loginManuell.putExtra(LoginManuell.LOGIN_EMAIL_ADRESSE, loginEmail);
                        loginManuell.putExtra(LoginManuell.LOGIN_PASSWORT, loginPassword);
                        startActivity(loginManuell);

                        //TODO: Else keine Daten vorhanden manuelle eingabe

                    }


                    break;

                case LoginAuthentifiezierungTask.AUTHENTIFIZIERUNG:


                    if ((Boolean) ergebnis.getErgebnis()) {

                        SharedPreferences einstellungen = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                        SharedPreferences.Editor einstEditor = einstellungen.edit();
                        String json = new Gson().toJson(authentifizierung);
                        einstEditor.putString("accAuth", json).commit();

                        Intent postfach = new Intent(this, Postfach.class);
                        startActivity(postfach);
                        finish();
//                    new ImapServerAnfragenAsyncTask(this, authentifizierung, ImapServerAnfragenAsyncTask.ORDNER).execute();
                    } else {
//                        et_loginEmail.setEnabled(true);
//                        et_loginPassword.setEnabled(true);
//                        loginTaste.setEnabled(true);
                    }
                    break;
            }

        } else if (ergebnis != null && !ergebnis.istGueltig()) {
            if (ergebnis.getFehler() instanceof AuthenticationFailedException) {

                til_loginEmail.setErrorEnabled(true);
                til_loginEmail.setError(getString(R.string.login_fehlernachricht_eingabe));
                et_loginEmail.getBackground().setColorFilter(getResources().getColor(R.color.akzentFarbe_redA400), PorterDuff.Mode.SRC_ATOP);
                et_loginPassword.getBackground().setColorFilter(getResources().getColor(R.color.akzentFarbe_redA400), PorterDuff.Mode.SRC_ATOP);

                et_loginEmail.setEnabled(true);
                et_loginPassword.setEnabled(true);
                loginTaste.setEnabled(true);
            }
            //TODO: weitere Exceptions abfangen
        }
    }

    private boolean emailEingabeKorrekt(String emailString) {
        boolean gueltigeEingabe = false;

        if (!TextUtils.isEmpty(emailString) && Patterns.EMAIL_ADDRESS.matcher(emailString).matches()) {
            til_loginEmail.setErrorEnabled(false);
            et_loginEmail.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
            gueltigeEingabe = true;

        } else {
            til_loginEmail.setErrorEnabled(true);
            til_loginEmail.setError(getString(R.string.login_fehlernachricht_email));
            et_loginEmail.getBackground().setColorFilter(getResources().getColor(R.color.akzentFarbe_redA400), PorterDuff.Mode.SRC_ATOP);
            gueltigeEingabe = false;
        }

        return gueltigeEingabe;

    }

    private boolean pruefeIspDaten() {
        return eingangsserverHost != null && eingangsserverPort != null && incomingServerSocketType != null &&
                ausgangsserverHost != null && ausgangsserverPort != null && outgoingServerSocketType != null;
    }

    private boolean internetverbindungVorhanden() {

        boolean verbunden = false;

        //Prüfe ob eine Verbindung zum Internet besteht
        ConnectivityManager verbindungsManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = verbindungsManager.getActiveNetworkInfo();
        verbunden = netInfo != null && netInfo.isConnectedOrConnecting();

        if (!verbunden) {

            AlertDialog.Builder dialogfensterErzeuger = new AlertDialog.Builder(this, R.style.Base_Theme_AppCompat_Light_Dialog_Alert);
            dialogfensterErzeuger.setTitle(R.string.dialog_keine_verbindung_titel);
            dialogfensterErzeuger.setMessage(R.string.dialog_keine_verbindung_nachricht);
            dialogfensterErzeuger.setPositiveButton(R.string.dialog_keine_verbindung_positiveTaste, null);

            AlertDialog dialogfenster = dialogfensterErzeuger.create();
            dialogfenster.show();
        }

        return verbunden;
    }

}
