package de.htwg.masilipo.nonamemail.applikation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

import de.htwg.masilipo.nonamemail.R;
import de.htwg.masilipo.nonamemail.modell.AccountAuthentifizierung;
import de.htwg.masilipo.nonamemail.modell.AufgabenErgebnis;
import de.htwg.masilipo.nonamemail.werkzeug.ITaskAntwort;
import de.htwg.masilipo.nonamemail.werkzeug.LoginAuthentifiezierungTask;

/**
 * Created by MauRii on 22.07.2015.
 */
public class LoginManuell extends AppCompatActivity implements View.OnClickListener, ITaskAntwort {

    public final static String LOGIN_EMAIL_ADRESSE = "EMAIL";
    public final static String LOGIN_PASSWORT = "PASSWORT";

    private AccountAuthentifizierung authentifizierung;
    private EditText et_loginEmail, et_loginPasswort, et_ausgangsserverAdresse,
            et_ausgangsserverPort, et_eingangsserverAdresse, et_eingangsserverPort;
    private Button b_loginTaste;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ansicht_login_manuell);

        getWindow().setBackgroundDrawable(getDrawable(R.drawable.konstanz_imperia));

        et_loginEmail = (EditText) findViewById(R.id.login_email_edittext);
        et_loginPasswort = (EditText) findViewById(R.id.login_passwort_edittext);
        et_ausgangsserverAdresse = (EditText) findViewById(R.id.login_ausgangsserverAdresse_edittext);
        et_ausgangsserverPort = (EditText) findViewById(R.id.login_ausgangsserverPort_edittext);
        et_eingangsserverAdresse = (EditText) findViewById(R.id.login_eingangsserverAdresse_edittext);
        et_eingangsserverPort = (EditText) findViewById(R.id.login_eingangsserverPort_edittext);
        b_loginTaste = (Button) findViewById(R.id.login_abschicken_button);
        b_loginTaste.setOnClickListener(this);

        et_loginEmail.setText(getIntent().getStringExtra(LOGIN_EMAIL_ADRESSE));
        et_loginPasswort.setText(getIntent().getStringExtra(LOGIN_PASSWORT));
    }


    @Override
    public void onClick(View v) {


        authentifizierung = new AccountAuthentifizierung(et_loginEmail.getText().toString().replace(" ", ""), et_loginPasswort.getText().toString().replace(" ", ""),
                et_eingangsserverAdresse.getText().toString().replace(" ", ""), Integer.parseInt(et_eingangsserverPort.getText().toString()),
                et_ausgangsserverAdresse.getText().toString().replace(" ", ""), Integer.parseInt(et_ausgangsserverPort.getText().toString()));

        new LoginAuthentifiezierungTask(this, authentifizierung).execute();


    }

    @Override
    public void serverAufgabeErledigt(String auszufuerendeAufgabe, AufgabenErgebnis ergebnis) {

        if ((Boolean) ergebnis.getErgebnis()) {

            SharedPreferences einstellungen = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            SharedPreferences.Editor einstEditor = einstellungen.edit();
            String json = new Gson().toJson(authentifizierung);
            einstEditor.putString("accAuth", json).commit();

            Intent postfach = new Intent(this, Postfach.class);
            startActivity(postfach);
            finish();

        }
    }


}
