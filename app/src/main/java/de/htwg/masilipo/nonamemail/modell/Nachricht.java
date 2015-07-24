package de.htwg.masilipo.nonamemail.modell;

import java.util.Date;

import javax.mail.Address;

/**
 * Created by MauRii on 15.07.2015.
 */
public class Nachricht implements Comparable<Nachricht>{

    private Long emailNachrichtUID;
    private Address[] absender;
    private String betreff, inOrdner;
    private Date erhaltenAm;
    private boolean gelesen;
    private Address[] antwortAdressen;

    public Nachricht(Address[] sender, String betreff, String inOrdner, Date erhaltenAm, Long emailNachrichtUID, boolean gelesen, Address[] antwortAdressen) {
        this.absender = sender;
        this.betreff = betreff;
        this.inOrdner = inOrdner;
        this.erhaltenAm = erhaltenAm;
        this.emailNachrichtUID = emailNachrichtUID;
        this.gelesen = gelesen;
        this.antwortAdressen = antwortAdressen;
    }

    public Address[] getAbsender() {
        return absender;
    }

    public String getBetreff() {
        return betreff;
    }

    public String getInOrdner() {
        return inOrdner;
    }

    public Date getErhaltenAm() {
        return erhaltenAm;
    }

    public Long getEmailNachrichtUID() {
        return emailNachrichtUID;
    }

    public boolean isGelesen() {
        return gelesen;
    }

    public Address[] getAntwortAdressen() {
        return antwortAdressen;
    }

    @Override
    public int compareTo(Nachricht nachricht) {
//        Absteigende sortierung der Nachrichten
        return nachricht.getErhaltenAm().compareTo(this.erhaltenAm);
    }
}
