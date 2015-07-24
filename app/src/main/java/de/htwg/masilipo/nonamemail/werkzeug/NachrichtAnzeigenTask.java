package de.htwg.masilipo.nonamemail.werkzeug;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMultipart;

import de.htwg.masilipo.nonamemail.R;
import de.htwg.masilipo.nonamemail.modell.AccountAuthentifizierung;
import de.htwg.masilipo.nonamemail.modell.AufgabenErgebnis;

/**
 * Created by MauRii on 21.07.2015.
 */
public class NachrichtAnzeigenTask extends AsyncTask<String, Void, AufgabenErgebnis<HashMap<String, String>>> {

    public final static String NACHRICHT_ANZEIGEN = "NACHRICHT_CONTENT";

    private Activity activity;
    private AccountAuthentifizierung authentifizierung;
    private ITaskAntwort erhaeltAntwort;
    private ProgressBar ablaufLeiste;
    private HashMap<String, String> nachrichtenInhaltListe;
    int count;

    public NachrichtAnzeigenTask(Activity activity, AccountAuthentifizierung authentifizierung) {
        this.activity = activity;
        this.authentifizierung = authentifizierung;
        erhaeltAntwort = (ITaskAntwort) activity;
        ablaufLeiste = (ProgressBar) activity.findViewById(R.id.nachrichtAnzeigen_ablaufleiste);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ablaufLeiste.setVisibility(View.VISIBLE);
    }

    @Override
    protected AufgabenErgebnis<HashMap<String, String>> doInBackground(String... params) {

        Long nachrichtUid = Long.parseLong(params[0]);
        String vollerOrdnername = params[1];
        nachrichtenInhaltListe = new HashMap<>();

        try {
            count = 0;
            Properties eigenschaften = System.getProperties();
            eigenschaften.setProperty("mail.store.protocol", "imaps");
            Session sitzung = Session.getInstance(eigenschaften, authentifizierung);
            IMAPStore store = (IMAPStore) sitzung.getStore();

            store.connect(authentifizierung.getEingangsserver(), authentifizierung.getEingangserverPort(),
                    authentifizierung.getEmailAdresse(), authentifizierung.getPassword());

            IMAPFolder ordner = (IMAPFolder) store.getFolder(vollerOrdnername);
            ordner.open(Folder.READ_WRITE);
            Message nachricht = ordner.getMessageByUID(nachrichtUid);
            nachricht.setFlag(Flags.Flag.SEEN, true);
            nachrichtDurchsuchen(nachricht);

            String f = "s";
//            MimeMultipart mime = (MimeMultipart) nachricht.getContent();
//            BodyPart body = mime.getBodyPart(1);


        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new AufgabenErgebnis<HashMap<String, String>>(nachrichtenInhaltListe);
    }

    private void nachrichtDurchsuchen(Part nachricht) throws MessagingException, IOException {

        if (nachricht.isMimeType("text/*")) {

            nachrichtenInhaltListe.put(nachricht.getContentType(), nachricht.getContent().toString());

        } else if (nachricht.isMimeType("multipart/*")) {

            MimeMultipart part = (MimeMultipart) nachricht.getContent();
            int length = part.getCount();

            for (int i = 0; i < length; i++) {
                nachrichtDurchsuchen(part.getBodyPart(i));
            }
        } else if (nachricht.isMimeType("image/*")) {

            nachrichtenInhaltListe.put(count + "_" + nachricht.getContentType(), "Bild gefunden");
        } else if (nachricht.isMimeType("audio/*")) {

            nachrichtenInhaltListe.put(count + "_" + nachricht.getContentType(), "Audio gefunden");
        } else if (nachricht.isMimeType("video/*")) {

            nachrichtenInhaltListe.put(count + "_" + nachricht.getContentType(), "Video gefunden");
        } else if (nachricht.isMimeType("application/*")) {

            nachrichtenInhaltListe.put(count + "_" + nachricht.getContentType(), "Application gefunden");
        }
        count++;
    }

    @Override
    protected void onPostExecute(AufgabenErgebnis<HashMap<String, String>> stringAufgabenErgebnis) {
        super.onPostExecute(stringAufgabenErgebnis);
        erhaeltAntwort.serverAufgabeErledigt(NACHRICHT_ANZEIGEN, stringAufgabenErgebnis);
        ablaufLeiste.setVisibility(View.GONE);
    }
}
