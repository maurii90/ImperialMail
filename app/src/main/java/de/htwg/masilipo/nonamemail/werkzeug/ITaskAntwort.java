package de.htwg.masilipo.nonamemail.werkzeug;

import de.htwg.masilipo.nonamemail.modell.AufgabenErgebnis;

/**
 * Created by MauRii on 10.07.2015.
 */
public interface ITaskAntwort {
    void serverAufgabeErledigt(String auszufuerendeAufgabe, AufgabenErgebnis ergebnis);
}
