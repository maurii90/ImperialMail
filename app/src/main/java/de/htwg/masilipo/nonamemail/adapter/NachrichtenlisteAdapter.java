package de.htwg.masilipo.nonamemail.adapter;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.mail.Address;

import de.htwg.masilipo.nonamemail.R;
import de.htwg.masilipo.nonamemail.modell.Nachricht;

/**
 * Created by MauRii on 15.07.2015.
 */
public class NachrichtenlisteAdapter extends BaseAdapter {
    Activity activity;
    LayoutInflater inflater;
    List<Nachricht> nachrichtenListe;
    boolean[] wurdeAnimiert;

    public NachrichtenlisteAdapter(Activity activity) {
        this.activity = activity;
        nachrichtenListe = new ArrayList<>();

    }

    @Override
    public int getCount() {
        if (nachrichtenListe.isEmpty())
            return 0;

        return nachrichtenListe.size();
    }

    @Override
    public Object getItem(int position) {
        return nachrichtenListe.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity.getLayoutInflater();

        if (convertView == null)
            convertView = inflater.inflate(R.layout.nachrichtenliste_zeile, null);

        TextView tvNachrichtenBild = (TextView) convertView.findViewById(R.id.listenzeile_nachrichtenBild);
        TextView tvAbsender = (TextView) convertView.findViewById(R.id.listenzeile_absender);
        TextView tvBetreff = (TextView) convertView.findViewById(R.id.listenzeile_betreff);
        TextView tvDatum = (TextView) convertView.findViewById(R.id.listenzeile_datum);

        Nachricht nachricht = nachrichtenListe.get(position);

        String absenderName = "";
        for (Address absender : nachricht.getAbsender()) {

            if (absender.toString().contains("<")) {
                absenderName = absenderName + absender.toString().substring(0,
                        absender.toString().indexOf("<"));
            } else
                absenderName = absenderName + absender.toString();
        }

        if (TextUtils.isEmpty(absenderName))
            absenderName = "Mauri";

        tvAbsender.setText(absenderName);
        tvBetreff.setText(nachricht.getBetreff());

        for (char buchstabe : absenderName.toCharArray()) {
            if (Character.isAlphabetic(buchstabe)) { // Api>19
                tvNachrichtenBild.setText(String.valueOf(buchstabe).toUpperCase());
                break;
            }
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        Random zufallsfarbe = new Random();
        int color;

        if (!sp.contains(absenderName)) {

            color = Color.argb(255, zufallsfarbe.nextInt(256), zufallsfarbe.nextInt(256), zufallsfarbe.nextInt(256));
            sp.edit().putInt(absenderName, color).commit();

        } else {
            color = sp.getInt(absenderName, Color.argb(255, zufallsfarbe.nextInt(256), zufallsfarbe.nextInt(256), zufallsfarbe.nextInt(256)));
        }

        GradientDrawable nachrichtenBildHintergrund = (GradientDrawable) tvNachrichtenBild.getBackground();
        nachrichtenBildHintergrund.setColor(color);

        Date aktuellesDatum = Calendar.getInstance().getTime();
        Date nachrichtErhalten = nachricht.getErhaltenAm();

        SimpleDateFormat datumFormat = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat jahrFormat = new SimpleDateFormat("yyyy");
        if (datumFormat.format(nachrichtErhalten).equals(datumFormat.format(aktuellesDatum))) {
            tvDatum.setText(new SimpleDateFormat("HH:mm").format(nachricht.getErhaltenAm()));

        } else if (jahrFormat.format(nachrichtErhalten).equals(jahrFormat.format(aktuellesDatum))) {
            tvDatum.setText(new SimpleDateFormat("dd.MMMM").format(nachrichtErhalten));

        } else {
            tvDatum.setText(datumFormat.format(nachrichtErhalten));
        }

        if (!nachricht.isGelesen()) {
            tvAbsender.setTypeface(null, Typeface.BOLD);
            tvDatum.setTypeface(null, Typeface.BOLD);
            tvBetreff.setTypeface(null, Typeface.BOLD);

//            convertView.setBackgroundColor(Color.argb(255, 204, 204, 255));

        } else {
            tvAbsender.setTypeface(null, Typeface.NORMAL);
            tvDatum.setTypeface(null, Typeface.NORMAL);
            tvBetreff.setTypeface(null, Typeface.NORMAL);
//            nachrichtenBildHintergrund.setAlpha(90);
//            convertView.setBackgroundColor(Color.argb(255, 255, 255, 255));
        }

        if (!wurdeAnimiert[position]) {
            wurdeAnimiert[position] = true;
            convertView.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.abc_slide_in_bottom));
        }

        return convertView;
    }

    public void aktualisiereListe(List<Nachricht> neueListe) {

        nachrichtenListe.clear();
        nachrichtenListe.addAll(neueListe);
        wurdeAnimiert = new boolean[nachrichtenListe.size()];
        notifyDataSetChanged();

    }

    public void listeLoeschen() {
        nachrichtenListe.clear();
        notifyDataSetChanged();
    }
}
