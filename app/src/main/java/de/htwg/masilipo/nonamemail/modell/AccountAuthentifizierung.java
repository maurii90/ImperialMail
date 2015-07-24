package de.htwg.masilipo.nonamemail.modell;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * Created by MauRii on 07.07.2015.
 */
public class AccountAuthentifizierung extends Authenticator {

    private String emailAdresse, password, eingangsserver, ausgangserver;
    private int eingangserverPort, ausgangserverPort;


    public AccountAuthentifizierung(String emailAdresse, String password, String eingangserver, int eingangserverPort, String ausgangserver, int ausgangserverPort) {
        this.emailAdresse = emailAdresse;
        this.password = password;
        this.eingangsserver = eingangserver;
        this.eingangserverPort = eingangserverPort;
        this.ausgangserver = ausgangserver;
        this.ausgangserverPort = ausgangserverPort;
    }

    public String getEmailAdresse() {
        return emailAdresse;
    }

    public String getPassword() {
        return password;
    }

    public int getEingangserverPort() {
        return eingangserverPort;
    }

    public String getEingangsserver() {
        return eingangsserver;
    }

    public String getAusgangserver() {
        return ausgangserver;
    }

    public int getAusgangserverPort() {
        return ausgangserverPort;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(emailAdresse, password);

    }
}
