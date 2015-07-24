package de.htwg.masilipo.nonamemail.werkzeug;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;

import de.htwg.masilipo.nonamemail.R;
import de.htwg.masilipo.nonamemail.modell.AccountAuthentifizierung;
import de.htwg.masilipo.nonamemail.modell.AufgabenErgebnis;
import de.htwg.masilipo.nonamemail.modell.Nachricht;

/**
 * Created by MauRii on 19.07.2015.
 */
public class NachrichtenImPostfachordnerTask extends AsyncTask<String, Void, AufgabenErgebnis<List<Nachricht>>> {


    public final static String NACHRICHTEN_IM_ORDNER = "EMAILS_IN";

    AccountAuthentifizierung authentifizierung;
    Activity activity;
    ITaskAntwort erhaeltAntwort;
    ProgressBar ablaufLeiste;

    public NachrichtenImPostfachordnerTask(Activity activity, AccountAuthentifizierung authentifizierung) {
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
    protected AufgabenErgebnis<List<Nachricht>> doInBackground(String... params) {

        String ordnerName = params[0];
        Message[] emailNachrichten;

        List<Nachricht> nachrichtenListe = new ArrayList<>();

        try {

            Log.i("MyApp", "Start");
            Properties eigenschaften = System.getProperties();
            eigenschaften.setProperty("mail.store.protocol", "imaps");
            Session sitzung = Session.getInstance(eigenschaften, authentifizierung);
            IMAPStore store = (IMAPStore) sitzung.getStore("imaps");
            store.connect(authentifizierung.getEingangsserver(), authentifizierung.getEingangserverPort(),
                    authentifizierung.getEmailAdresse(), authentifizierung.getPassword());
            boolean sortierungUnterstuetzt = store.hasCapability("sort");
            Log.i("MyApp", "Mit Server verbunden");

            IMAPFolder ordnerStore = (IMAPFolder) store.getFolder(ordnerName);
            ordnerStore.open(IMAPFolder.READ_ONLY);
            Log.i("MyApp", "Ordner geöffnet");

            emailNachrichten = ordnerStore.getMessages();

            Log.i("MyApp", "Emails erhalten");
            for (Message emailNachricht : emailNachrichten) {
                nachrichtenListe.add(new Nachricht(emailNachricht.getFrom(), emailNachricht.getSubject(),
                        emailNachricht.getFolder().getFullName(), emailNachricht.getReceivedDate(), ordnerStore.getUID(emailNachricht), emailNachricht.getFlags().contains(Flags.Flag.SEEN), emailNachricht.getReplyTo()));

            }
            Log.i("MyApp", "Foreach durch");

            Collections.sort(nachrichtenListe);

            return new AufgabenErgebnis<List<Nachricht>>(nachrichtenListe);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(AufgabenErgebnis<List<Nachricht>> nachrichtenListe) {
        super.onPostExecute(nachrichtenListe);
        erhaeltAntwort.serverAufgabeErledigt(NACHRICHTEN_IM_ORDNER, nachrichtenListe);

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
