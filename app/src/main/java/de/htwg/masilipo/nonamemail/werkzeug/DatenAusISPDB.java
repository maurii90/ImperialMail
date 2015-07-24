package de.htwg.masilipo.nonamemail.werkzeug;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.htwg.masilipo.nonamemail.R;
import de.htwg.masilipo.nonamemail.modell.AufgabenErgebnis;

/**
 * Created by MauRii on 07.07.2015.
 */
public class DatenAusISPDB extends AsyncTask<String, String, AufgabenErgebnis<HashMap<String, NodeList>>> {

    public final static String ERHALTE_ISP_DATEN = "ERHALTE_ISP_DATEN";

    private Activity activity;
    private ITaskAntwort erhaeltAntwort = null;
//    ProgressDialog ablaufDialog;
    ScrollView scrollAnsichtLogin;
    ProgressBar loginAblaufDialog;
    TextView loginAblaufDialogText;

    public DatenAusISPDB(Activity activity) {
        this.activity = activity;
        this.erhaeltAntwort = (ITaskAntwort) activity;
        scrollAnsichtLogin = (ScrollView) activity.findViewById(R.id.login_scroll_ansicht);
        loginAblaufDialog = (ProgressBar) activity.findViewById(R.id.login_ablaufDialog);
        loginAblaufDialogText = (TextView) activity.findViewById(R.id.login_ablaufDialog_text);

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
//        ablaufDialog = new ProgressDialog(activity);
//        ablaufDialog.setCancelable(false);
//        ablaufDialog.show();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loginAblaufDialog.setVisibility(View.VISIBLE);
                loginAblaufDialogText.setText(activity.getString(R.string.ablaufDialog_lade_ispDaten_nachricht));
                loginAblaufDialogText.setVisibility(View.VISIBLE);
                scrollAnsichtLogin.setForeground(new ColorDrawable(Color.parseColor("#B3000000")));
            }
        });


    }

    @Override
    protected AufgabenErgebnis<HashMap<String, NodeList>> doInBackground(String... params) {

//        publishProgress(activity.getString(R.string.ablaufDialog_lade_ispDaten_nachricht));

        HashMap<String, NodeList> emailProviderDaten = new HashMap<String, NodeList>();
        String providerDomain = params[0];

        if (!TextUtils.isEmpty(providerDomain))
            try {

                //Verbindung erstellen mit IPSDB
                URL url = new URL("https://autoconfig.thunderbird.net/v1.1/" + providerDomain);
                URLConnection urlVerbindung = url.openConnection();

                HttpURLConnection httpUrlVerbindung = (HttpURLConnection) urlVerbindung;
                int antwortCode = httpUrlVerbindung.getResponseCode();

                if (antwortCode == HttpURLConnection.HTTP_OK) {

                    //XML-Dai mit IPS-Inhalt verarbeiten
                    InputStream eingabeStream = httpUrlVerbindung.getInputStream();
                    DocumentBuilderFactory dokumentErzeugerFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dokumentErzeuger = dokumentErzeugerFactory.newDocumentBuilder();
                    Document dokument = dokumentErzeuger.parse(eingabeStream);

                    Element documentElement = dokument.getDocumentElement();
                    NodeList dokumentKnoten = documentElement.getChildNodes();
                    NodeList emailProviderKnotenListe = null;

                    //Element "emailProvider" in XML-Datei finden
                    int dokumentknotenAnzahl = dokumentKnoten.getLength();
                    for (int knotenIndex = 0; knotenIndex <= dokumentknotenAnzahl; knotenIndex++) {
                        if (dokumentKnoten.item(knotenIndex).getNodeName().equals("emailProvider")) {
                            emailProviderKnotenListe = dokumentKnoten.item(knotenIndex).getChildNodes();
                            break;
                        }
                    }

                    if (emailProviderKnotenListe != null) {
                        int emailProviderKnotenAnzahl = emailProviderKnotenListe.getLength();
                        //Kindknoten des Elements "emailProvider" durchsuchen
                        for (int knotenIndex = 0; knotenIndex < emailProviderKnotenAnzahl; knotenIndex++) {
                            Node serverDatenKnoten = emailProviderKnotenListe.item(knotenIndex);
                            Boolean kindknotenBenoetigt = false;

                            //Interessant sind Knoten mit dem Namen "incomingServer" & "outgoingServer" mit dem Attribut "type=imap" oder "type=smtp"-
                            if (serverDatenKnoten.hasChildNodes() &&
                                    (serverDatenKnoten.getNodeName().equals("incomingServer") &&
                                            serverDatenKnoten.getAttributes().getNamedItem("type").getNodeValue().equals("imap")) ||
                                    (serverDatenKnoten.getNodeName().equals("outgoingServer") &&
                                            serverDatenKnoten.getAttributes().getNamedItem("type").getNodeValue().equals("smtp"))) {

                                int ServerDatenKnotenAnzahl = emailProviderKnotenListe.item(knotenIndex).getChildNodes().getLength();
                                //Die Verschlüsselung muss auf SSL aufbauen, enthält ein Knoten diesen wert dann wird er gespeichert.
                                for (int serverDatenKnotenIndex = 1; serverDatenKnotenIndex < ServerDatenKnotenAnzahl; serverDatenKnotenIndex = serverDatenKnotenIndex + 2) {
                                    Node serverDatenKindknoten = serverDatenKnoten.getChildNodes().item(serverDatenKnotenIndex);

                                    if (serverDatenKindknoten.getNodeName().equals("socketType") &&
                                            serverDatenKindknoten.getFirstChild().getNodeValue().equals("SSL"))
                                        kindknotenBenoetigt = true;

                                }

                                if (kindknotenBenoetigt)
                                    emailProviderDaten.put(serverDatenKnoten.getNodeName(), serverDatenKnoten.getChildNodes());

                            }
                        }
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ConnectException e) {
                e.printStackTrace();
                return new AufgabenErgebnis<HashMap<String, NodeList>>(e);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }

        return new AufgabenErgebnis<HashMap<String, NodeList>>(emailProviderDaten);

    }

//    @Override
//    protected void onProgressUpdate(String... nachrichtenArray) {
//        super.onProgressUpdate(nachrichtenArray);
//        ablaufDialog.setMessage(nachrichtenArray[0]);
//    }

    @Override
    protected void onPostExecute(AufgabenErgebnis<HashMap<String, NodeList>> ergebnis) {
        super.onPostExecute(ergebnis);
//        ablaufDialog.dismiss();
        erhaeltAntwort.serverAufgabeErledigt(ERHALTE_ISP_DATEN, ergebnis);

    }
}
