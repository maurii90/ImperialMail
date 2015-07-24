//package de.htwg.masilipo.nonamemail.werkzeug;
//
//import android.app.Activity;
//import android.app.ProgressDialog;
//import android.util.Log;
//import android.widget.ProgressBar;
//
//
//import com.sun.mail.imap.IMAPFolder;
//import com.sun.mail.imap.IMAPStore;
//import com.sun.mail.imap.SortTerm;
//
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Properties;
//
//import javax.mail.Flags;
//import javax.mail.Folder;
//import javax.mail.Message;
//import javax.mail.MessagingException;
//import javax.mail.NoSuchProviderException;
//import javax.mail.Session;
//import javax.mail.Store;
//
//import de.htwg.masilipo.nonamemail.R;
//import de.htwg.masilipo.nonamemail.modell.AccountAuthentifizierung;
//import de.htwg.masilipo.nonamemail.modell.Nachricht;
//import de.htwg.masilipo.nonamemail.modell.AufgabenErgebnis;
//import de.htwg.masilipo.nonamemail.modell.Nachrichtenordner;
//
///**
// * Created by MauRii on 08.07.2015.
// */
//public class ImapServerAnfragenAsyncTask extends android.os.AsyncTask<String, String, AufgabenErgebnis<Object>> {
//
//    public final static String AUTHENTIFIZIERUNG = "AUTHENTIFIZIERUNG";
//    public final static String ORDNER = "ORDNER";
//    public final static String NACHRICHTEN_IM_ORDNER = "EMAILS";
//
//    AccountAuthentifizierung authentifizierung;
//    Activity activity;
//    ITaskAntwort erhaeltAntwort;
//    ProgressDialog ablaufDialog;
//    ProgressBar ablaufLeiste;
//    String auszufuerendeAufgabe;
//
//    public ImapServerAnfragenAsyncTask(Activity activity, AccountAuthentifizierung authentifizierung, String auszufuerendeAufgabe) {
//        this.activity = activity;
//        this.authentifizierung = authentifizierung;
//        this.erhaeltAntwort = (ITaskAntwort) activity;
//        this.auszufuerendeAufgabe = auszufuerendeAufgabe;
//        if (!auszufuerendeAufgabe.equals(AUTHENTIFIZIERUNG))
//            ablaufLeiste = (ProgressBar) activity.findViewById(R.id.postfach_ablaufleiste_progressbar);
//    }
//
//    @Override
//    protected void onPreExecute() {
//        super.onPreExecute();
//        if (auszufuerendeAufgabe.equals(AUTHENTIFIZIERUNG)) {
//            ablaufDialog = new ProgressDialog(activity);
//            ablaufDialog.show();
//            ablaufDialog.setCancelable(false);
//        } else {
//            ablaufLeiste.setVisibility(ProgressBar.VISIBLE);
//        }
//    }
//
//    @Override
//    protected AufgabenErgebnis<Object> doInBackground(String... params) {
//
//        IMAPStore store = null;
//
//        switch (auszufuerendeAufgabe) {
//            case AUTHENTIFIZIERUNG:
//
//                String emailAdresse = params[0];
//                String passwort = params[1];
//                String eingangsserverHost = params[2];
//                int eingangsserverPort = Integer.parseInt(params[3]);
//
//                publishProgress(activity.getString(R.string.ablaufDialog_authentifizierung_nachricht));
//
//                try {
//                    Properties eigenschaften = System.getProperties();
//                    eigenschaften.setProperty("mail.store.protocol", "imaps");
//                    Session sitzung = Session.getDefaultInstance(eigenschaften, authentifizierung);
//                    store = (IMAPStore) sitzung.getStore();
//                    store.connect(eingangsserverHost, eingangsserverPort, emailAdresse, passwort);
//
//                    if (store.isConnected()) {
//                        store.close();
//                        return new AufgabenErgebnis<Object>(true);
//                    }
//
//                    return new AufgabenErgebnis<Object>(false);
//
//                } catch (NoSuchProviderException e) {
//                    e.printStackTrace();
//                    return new AufgabenErgebnis<Object>(e);
//                } catch (MessagingException e) {
//                    e.printStackTrace();
//                    return new AufgabenErgebnis<Object>(e);
//                } finally {
//                    try {
//                        store.close();
//                    } catch (MessagingException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//            case ORDNER:
//
//                try {
////                        <key, shortcut = name>
////                    publishProgress(activity.getString(R.string.pd_loadInbox_message));
//
//                    Properties eigenschaften = System.getProperties();
//                    eigenschaften.setProperty("mail.store.protocol", "imaps");
//                    Session sitzung = Session.getInstance(eigenschaften, authentifizierung);
//                    store = (IMAPStore) sitzung.getStore();
//                    store.connect(authentifizierung.getEingangsserver(), authentifizierung.getEingangserverPort(),
//                            authentifizierung.getEmailAdresse(), authentifizierung.getPassword());
//
//                    int eigenerOrdnerIndex = 0;
//                    Folder[] serverOrdner = store.getDefaultFolder().list("*");
//                    HashMap<String, Nachrichtenordner> ordnerHashMap = new HashMap<String, Nachrichtenordner>();
//
//                    for (Folder ordner : serverOrdner) {
//                        IMAPFolder imapOrdner = (IMAPFolder) ordner;
//
//                        //Posteingang Ordner
//                        if (imapOrdner.getFullName().toLowerCase().equals("inbox")) {
//                            ordnerHashMap.put("inbox", new Nachrichtenordner(activity.getString(R.string.ordnerName_posteingang), imapOrdner.getFullName()));
//                            continue;
//                        }
//
//                        //Postausgang Ordner
////                        if (imapFolder.getFullName().toLowerCase().equals("outbox")) {
////                            folderHashMap.put("outbox", activity.getString(R.string.ordnerName_postausgang));
////                            continue;
////                        }
//
//                        //Vom User erstellte Ordner Erstellte Ordner
//                        if (imapOrdner.getAttributes().length <= 1 && !imapOrdner.getFullName().toLowerCase().equals("inbox")
//                                && !imapOrdner.getFullName().toLowerCase().equals("outbox")) {
//                            ordnerHashMap.put("ordner_" + String.valueOf(eigenerOrdnerIndex), new Nachrichtenordner(imapOrdner.getName(), imapOrdner.getFullName()));
//                            eigenerOrdnerIndex++;
//                            continue;
//                        }
//
//                        for (String attribut : imapOrdner.getAttributes()) {
//
//                            //Gesendet Ordner
//                            if (attribut.toLowerCase().contains("sent"))
//                                ordnerHashMap.put("sent", new Nachrichtenordner(imapOrdner.getName(), imapOrdner.getFullName()));
//
//                            //Papierkorb Ordner
//                            if (attribut.toLowerCase().contains("trash"))
//                                ordnerHashMap.put("trash", new Nachrichtenordner(imapOrdner.getName(), imapOrdner.getFullName()));
//
//                            //Entwürfe Ordner
//                            if (attribut.toLowerCase().contains("drafts"))
//                                ordnerHashMap.put("drafts", new Nachrichtenordner(imapOrdner.getName(), imapOrdner.getFullName()));
//                        }
//                    }
//
//                    return new AufgabenErgebnis<Object>(ordnerHashMap);
//
//                } catch (MessagingException e) {
//                    e.printStackTrace();
//                }
//
//                break;
//
//            case NACHRICHTEN_IM_ORDNER:
//
//                String ordnerName = params[0];
//                Message[] emailNachrichten;
//                SortTerm[] sortierAusdruckAnkunft = {SortTerm.REVERSE, SortTerm.ARRIVAL};
//                List<Nachricht> nachrichtenListe = new ArrayList<>();
//
//                try {
//
//                    Log.i("MyApp", "Start");
//                    Properties eigenschaften = System.getProperties();
//                    eigenschaften.setProperty("mail.store.protocol", "imaps");
//                    Session sitzung = Session.getInstance(eigenschaften, authentifizierung);
//                    store = (IMAPStore) sitzung.getStore("imaps");
//                    store.connect(authentifizierung.getEingangsserver(), authentifizierung.getEingangserverPort(),
//                            authentifizierung.getEmailAdresse(), authentifizierung.getPassword());
//                    boolean sortierungUnterstuetzt = store.hasCapability("sort");
//                    Log.i("MyApp", "Mit Server verbunden");
//
//
//                    IMAPFolder ordnerStore = (IMAPFolder) store.getFolder(ordnerName);
//                    ordnerStore.open(IMAPFolder.READ_ONLY);
//                    Log.i("MyApp", "Ordner geöffnet");
//
//                    if (sortierungUnterstuetzt) {
//                        emailNachrichten = ordnerStore.getSortedMessages(sortierAusdruckAnkunft);
//                    } else {
//                        emailNachrichten = ordnerStore.getMessages();
//                    }
//
//                    Log.i("MyApp", "Emails erhalten");
//                    for (Message emailNachricht : emailNachrichten) {
//                        nachrichtenListe.add(new Nachricht(emailNachricht.getFrom(), emailNachricht.getSubject(),
//                                emailNachricht.getFolder().getName(), emailNachricht.getReceivedDate(), ordnerStore.getUID(emailNachricht), emailNachricht.getFlags().contains(Flags.Flag.SEEN)));
//
//                    }
//                    Log.i("MyApp", "Foreach durch");
//
//                    if(!sortierungUnterstuetzt)
//                        Collections.sort(nachrichtenListe);
////                    HashMap<String, List<Nachricht>> test = new HashMap<String, List<Nachricht>>();
//                    return new AufgabenErgebnis<Object>(nachrichtenListe);
//
//                } catch (MessagingException e) {
//                    e.printStackTrace();
//                }
//                break;
//        }
//
//        return null;
//    }
//
//    private Store verbindeMitSmtpServer(String eingangserverHost, String eingangsserverPort) {
//
//        try {
//            Properties prop = System.getProperties();
//            prop.setProperty("mail.store.protocol", "imaps");
//            Session session = Session.getDefaultInstance(prop, authentifizierung);
//            Store store = session.getStore();
//            store.connect(eingangserverHost, Integer.parseInt(eingangsserverPort), authentifizierung.getEmailAdresse(), authentifizierung.getPassword());
//
//            return store;
//
//        } catch (NoSuchProviderException e) {
//            e.printStackTrace();
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//
//
//    }
//
//    private void sortiereNachrichtenNachAnkunft(Message[] emailNachrichten) {
//    }
//
//    @Override
//    protected void onProgressUpdate(String... nachrichten) {
//        super.onProgressUpdate(nachrichten);
//        ablaufDialog.setMessage(nachrichten[0]);
//    }
//
//    @Override
//    protected void onPostExecute(AufgabenErgebnis<Object> ergebnis) {
//        super.onPostExecute(ergebnis);
//        erhaeltAntwort.serverAufgabeErledigt(auszufuerendeAufgabe, ergebnis);
//
//        if (auszufuerendeAufgabe.equals(AUTHENTIFIZIERUNG)) {
//            ablaufDialog.dismiss();
//
//        } else {
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(300);
//                        activity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                ablaufLeiste.setVisibility(ProgressBar.GONE);
//                            }
//                        });
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//
//        }
//    }
//}
//
