package de.htwg.masilipo.nonamemail.modell;

/**
 * Created by MauRii on 15.07.2015.
 */
public class Nachrichtenordner {

    private String ordnername;
    private String vollerOrdnername;

    public Nachrichtenordner(String ordnername, String vollerOrdnername) {
        this.ordnername = ordnername;
        this.vollerOrdnername = vollerOrdnername;
    }

    public String getOrdnername() {
        return ordnername;
    }

    public String getVollerOrdnername() {
        return vollerOrdnername;
    }
}
