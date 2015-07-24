package de.htwg.masilipo.nonamemail.werkzeug;

import android.app.Activity;
import android.os.AsyncTask;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import de.htwg.masilipo.nonamemail.modell.AccountAuthentifizierung;
import de.htwg.masilipo.nonamemail.modell.AufgabenErgebnis;

/**
 * Created by MauRii on 20.07.2015.
 */
public class NachrichtSendenTask extends AsyncTask<HashMap<String, List<String>>, Void, AufgabenErgebnis<Object>> {

    public final static String NACHRICHT_SENDEN = "SENDEN";

    Activity activity;
    AccountAuthentifizierung authentifizierung;
    ITaskAntwort erhaeltAntwort;



    public NachrichtSendenTask(Activity activity, AccountAuthentifizierung authentifizierung) {
        this.activity = activity;
        this.erhaeltAntwort = (ITaskAntwort) activity;
        this.authentifizierung = authentifizierung;
    }

    @Override
    protected AufgabenErgebnis<Object> doInBackground(HashMap<String, List<String>>... params) {

        // TO; SUBJECT; CONTENT; -- CC und BCC noch machen --
        HashMap<String, List<String>> datenListe = (HashMap<String, List<String>>) params[0];

        Properties systemEigenschafgten = System.getProperties();
        systemEigenschafgten.setProperty("mail.smtp.host", authentifizierung.getAusgangserver());
        systemEigenschafgten.put("mail.smtp.auth", "true");
        systemEigenschafgten.put("mail.smtp.port", "465");
        systemEigenschafgten.put("mail.smtp.socketFactory.port", "465");
        systemEigenschafgten.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        systemEigenschafgten.put("mail.smtp.socketFactory.fallback", "false");

        Session sitzung = Session.getDefaultInstance(systemEigenschafgten, authentifizierung);

        try {

            Address[] empfaenger = new Address[datenListe.get("TO").size()];
            int index = 0;

            for(String adresse : datenListe.get("TO")){
                empfaenger[index] = new InternetAddress(adresse);
                index++;

            }

            Message nachricht = new MimeMessage(sitzung);
            nachricht.setFrom(new InternetAddress(authentifizierung.getEmailAdresse()));
            nachricht.addRecipients(Message.RecipientType.TO, empfaenger);
            nachricht.setSubject(datenListe.get("SUBJECT").get(0));
            nachricht.setSentDate(new Date());
            nachricht.setContent(datenListe.get("CONTENT").get(0), "text/html; charset=utf-8");



            Transport.send(nachricht);


        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }


        return null;
    }

    @Override
    protected void onPostExecute(AufgabenErgebnis<Object> objectAufgabenErgebnis) {
        super.onPostExecute(objectAufgabenErgebnis);
        erhaeltAntwort.serverAufgabeErledigt(NACHRICHT_SENDEN, objectAufgabenErgebnis);
    }
}
