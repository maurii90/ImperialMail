package de.htwg.masilipo.nonamemail.modell;

/**
 * Created by MauRii on 08.07.2015.
 * Java Generics<T> - nach Coding-Conventions (Mueller)
 */
public class AufgabenErgebnis<T> {

    private Exception fehler;

    private T ergebnis;

    public AufgabenErgebnis(final T ergebnis) {
        this.ergebnis = ergebnis;
    }

    public AufgabenErgebnis(final Exception fehler) {
        this.fehler = fehler;
    }

    public final boolean istGueltig() {
        return fehler == null;
    }

    public final Exception getFehler() {
        return fehler;
    }

    public final T getErgebnis() {
        return ergebnis;
    }
}
