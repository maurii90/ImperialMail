package de.htwg.masilipo.nonamemail.werkzeug;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sun.mail.imap.IMAPStore;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;

import de.htwg.masilipo.nonamemail.R;
import de.htwg.masilipo.nonamemail.modell.AccountAuthentifizierung;
import de.htwg.masilipo.nonamemail.modell.AufgabenErgebnis;

/**
 * Created by MauRii on 19.07.2015.
 */
public class LoginAuthentifiezierungTask extends AsyncTask<Void, String, AufgabenErgebnis<Boolean>> {

    public final static String AUTHENTIFIZIERUNG = "AUTHENTIFIZIERUNG";
    AccountAuthentifizierung authentifizierung;
    Activity activity;
    ITaskAntwort erhaeltAntwort;
//    ProgressDialog ablaufDialog;
    ScrollView scrollAnsichtLogin;
    ProgressBar loginAblaufDialog;
    TextView loginAblaufDialogText;


    public LoginAuthentifiezierungTask(Activity activity, AccountAuthentifizierung authentifizierung) {
        this.activity = activity;
        this.authentifizierung = authentifizierung;
        erhaeltAntwort = (ITaskAntwort) activity;
        scrollAnsichtLogin = (ScrollView) activity.findViewById(R.id.login_scroll_ansicht);
        loginAblaufDialog = (ProgressBar) activity.findViewById(R.id.login_ablaufDialog);
        loginAblaufDialogText = (TextView) activity.findViewById(R.id.login_ablaufDialog_text);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
//        ablaufDialog = new ProgressDialog(activity);
//        ablaufDialog.show();
//        ablaufDialog.setCancelable(false);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loginAblaufDialog.setVisibility(View.VISIBLE);
                loginAblaufDialogText.setText(activity.getString(R.string.ablaufDialog_authentifizierung_nachricht));
            }
        });
    }

    @Override
    protected AufgabenErgebnis<Boolean> doInBackground(Void... params) {

        IMAPStore store = null;

//        publishProgress(activity.getString(R.string.ablaufDialog_authentifizierung_nachricht));

        try {
            Properties systemEigenschaften = System.getProperties();
            systemEigenschaften.setProperty("mail.store.protocol", "imaps");
            Session sitzung = Session.getDefaultInstance(systemEigenschaften, authentifizierung);
            store = (IMAPStore) sitzung.getStore();
            store.connect(authentifizierung.getEingangsserver(), (int) authentifizierung.getEingangserverPort(),
                    authentifizierung.getEmailAdresse(), authentifizierung.getPassword());

            return new AufgabenErgebnis<Boolean>(store.isConnected());

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            return new AufgabenErgebnis<Boolean>(e);
        } catch (MessagingException e) {
            // Host is unresolved ..
            e.printStackTrace();
            return new AufgabenErgebnis<Boolean>(e);
        } finally {
            try {
                if(store.isConnected())
                    store.close();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

//    @Override
//    protected void onProgressUpdate(String... nachrichten) {
//        super.onProgressUpdate(nachrichten);
//        ablaufDialog.setMessage(nachrichten[0]);
//    }

    @Override
    protected void onPostExecute(AufgabenErgebnis<Boolean> booleanAufgabenErgebnis) {
        super.onPostExecute(booleanAufgabenErgebnis);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loginAblaufDialog.setVisibility(View.GONE);
                loginAblaufDialogText.setVisibility(View.GONE);
                scrollAnsichtLogin.setForeground(new ColorDrawable(Color.parseColor("#00000000")));
            }
        });
        erhaeltAntwort.serverAufgabeErledigt(AUTHENTIFIZIERUNG, booleanAufgabenErgebnis);
//        ablaufDialog.dismiss();

    }
}
