package de.htwg.masilipo.nonamemail.werkzeug;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.ProgressBar;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import java.util.HashMap;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;

import de.htwg.masilipo.nonamemail.R;
import de.htwg.masilipo.nonamemail.modell.AccountAuthentifizierung;
import de.htwg.masilipo.nonamemail.modell.AufgabenErgebnis;
import de.htwg.masilipo.nonamemail.modell.Nachrichtenordner;

/**
 * Created by MauRii on 19.07.2015.
 */
public class OrdnerImPostfachTask extends AsyncTask<Void, Void, AufgabenErgebnis<HashMap<String, Nachrichtenordner>>> {

    public final static String ORDNER_IM_POSTFACH = "ORDNER";

    AccountAuthentifizierung authentifizierung;
    Activity activity;
    ITaskAntwort erhaeltAntwort;
    ProgressBar ablaufLeiste;

    public OrdnerImPostfachTask(Activity activity, AccountAuthentifizierung authentifizierung) {
        this.activity = activity;
        this.authentifizierung = authentifizierung;
        this.erhaeltAntwort = (ITaskAntwort) activity;
        ablaufLeiste = (ProgressBar) activity.findViewById(R.id.postfach_ablaufleiste_progressbar);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ablaufLeiste.setVisibility(ProgressBar.VISIBLE);
    }

    @Override
    protected AufgabenErgebnis<HashMap<String, Nachrichtenordner>> doInBackground(Void... params) {

        try {

            Properties eigenschaften = System.getProperties();
            eigenschaften.setProperty("mail.store.protocol", "imaps");
            Session sitzung = Session.getInstance(eigenschaften, authentifizierung);
            IMAPStore store = (IMAPStore) sitzung.getStore();
            store.connect(authentifizierung.getEingangsserver(), authentifizierung.getEingangserverPort(),
                    authentifizierung.getEmailAdresse(), authentifizierung.getPassword());

            int eigenerOrdnerIndex = 0;
            Folder[] serverOrdner = store.getDefaultFolder().list("*");
            HashMap<String, Nachrichtenordner> ordnerHashMap = new HashMap<String, Nachrichtenordner>();

            for (Folder ordner : serverOrdner) {
                IMAPFolder imapOrdner = (IMAPFolder) ordner;

                //Posteingang Ordner
                if (imapOrdner.getFullName().toLowerCase().equals("inbox")) {
                    ordnerHashMap.put("inbox", new Nachrichtenordner(activity.getString(R.string.ordnerName_posteingang), imapOrdner.getFullName()));
                    continue;
                }

                for (String attribut : imapOrdner.getAttributes()) {

                    //Gesendet Ordner
                    if (attribut.toLowerCase().contains("sent"))
                        ordnerHashMap.put("sent", new Nachrichtenordner(imapOrdner.getName(), imapOrdner.getFullName()));

                    //Papierkorb Ordner
                    if (attribut.toLowerCase().contains("trash"))
                        ordnerHashMap.put("trash", new Nachrichtenordner(imapOrdner.getName(), imapOrdner.getFullName()));

                    //Entwürfe Ordner
                    if (attribut.toLowerCase().contains("drafts"))
                        ordnerHashMap.put("drafts", new Nachrichtenordner(imapOrdner.getName(), imapOrdner.getFullName()));
                }

                //Vom User erstellte Ordner Erstellte Ordner
                if (imapOrdner.getAttributes().length <= 1 && !imapOrdner.getFullName().toLowerCase().equals("inbox")
                        && !imapOrdner.getFullName().toLowerCase().equals("outbox")) {
                    ordnerHashMap.put("ordner_" + String.valueOf(eigenerOrdnerIndex), new Nachrichtenordner(imapOrdner.getName(), imapOrdner.getFullName()));
                    eigenerOrdnerIndex++;
                    continue;
                }
            }

            return new AufgabenErgebnis<HashMap<String, Nachrichtenordner>>(ordnerHashMap);

        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(AufgabenErgebnis<HashMap<String, Nachrichtenordner>> ordnerHashMap) {
        super.onPostExecute(ordnerHashMap);
        erhaeltAntwort.serverAufgabeErledigt(ORDNER_IM_POSTFACH, ordnerHashMap);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(300);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ablaufLeiste.setVisibility(ProgressBar.GONE);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

