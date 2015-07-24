package de.htwg.masilipo.nonamemail.applikation;

import de.htwg.masilipo.nonamemail.*;
import de.htwg.masilipo.nonamemail.modell.AccountAuthentifizierung;
import de.htwg.masilipo.nonamemail.modell.Nachricht;
import de.htwg.masilipo.nonamemail.werkzeug.ITaskAntwort;
import de.htwg.masilipo.nonamemail.modell.AufgabenErgebnis;
import de.htwg.masilipo.nonamemail.adapter.NachrichtenlisteAdapter;
import de.htwg.masilipo.nonamemail.modell.Nachrichtenordner;
import de.htwg.masilipo.nonamemail.werkzeug.NachrichtenImPostfachordnerTask;
import de.htwg.masilipo.nonamemail.werkzeug.OrdnerImPostfachTask;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.internal.NavigationMenuPresenter;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;


public class Postfach extends AppCompatActivity implements MenuItem.OnMenuItemClickListener, ITaskAntwort, Animation.AnimationListener, AdapterView.OnItemClickListener {

    Toolbar toolbar;
    FloatingActionButton neueNachrichtFab;
    Boolean toolbarUndFabAusgeblendet;
    Animation toolbarAusblenden, toolbarEinblenden,
            neueNachrichtFabAusblenden, neueNachrichtFabEinblenden;

    AccountAuthentifizierung authentifizierung;
    AsyncTask letzteAusgefuehrteTask;
    DrawerLayout ansichtLayoutGesamt;
    ActionBarDrawerToggle navigationsanzeigeVerwalter;
    NavigationView navigationAnzeige;
    MenuItem letztesMenuelement;

    ListView nachrichtenlisteAnzeige;
    NachrichtenlisteAdapter nachrichtenlisteAdapter;
    List<Nachricht> nachrichtenListe;
    TextView keineNachrichtenAnzeige;

    HashMap<String, Nachrichtenordner> ordnerImPostfach;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ansicht_postfach);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbarUndFabAusgeblendet = false;

        neueNachrichtFab = (FloatingActionButton) findViewById(R.id.postfach_neue_nachricht_fab);
        neueNachrichtFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent neueNachrichtIntent = new Intent(Postfach.this, NachrichtSenden.class);
                neueNachrichtIntent.setAction(NachrichtSenden.NEUE_NACHRICHT);
                startActivity(neueNachrichtIntent);
            }
        });

        SharedPreferences einstellungen = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String json = einstellungen.getString("accAuth", "");
        authentifizierung = new Gson().fromJson(json, AccountAuthentifizierung.class);

        toolbarAusblenden = AnimationUtils.loadAnimation(this, R.anim.anim_toolbar_ausblenden);
        toolbarAusblenden.setAnimationListener(this);
        toolbarEinblenden = AnimationUtils.loadAnimation(this, R.anim.anim_toolbar_einblenden);
        toolbarEinblenden.setAnimationListener(this);
        neueNachrichtFabAusblenden = AnimationUtils.loadAnimation(this, R.anim.anim_neue_nachricht_fab_ausblenden);
        neueNachrichtFabAusblenden.setAnimationListener(this);
        neueNachrichtFabEinblenden = AnimationUtils.loadAnimation(this, R.anim.anim_neue_nachricht_fab_einblenden);
        neueNachrichtFabEinblenden.setAnimationListener(this);


        new OrdnerImPostfachTask(this, authentifizierung).execute();

        TextView benutzerEmail = (TextView) findViewById(R.id.navigationsmenu_email_textview);
        benutzerEmail.setText(authentifizierung.getEmailAdresse());

        ansichtLayoutGesamt = (DrawerLayout) findViewById(R.id.postfach_layoutgesamt_drawerlayout);
        navigationAnzeige = (NavigationView) findViewById(R.id.postfach_navigationanzeige_navigationview);
        nachrichtenlisteAnzeige = (ListView) findViewById(R.id.postfach_nachrichtenliste_listview);
        nachrichtenlisteAdapter = new NachrichtenlisteAdapter(this);
        nachrichtenlisteAnzeige.setAdapter(nachrichtenlisteAdapter);
        nachrichtenlisteAnzeige.setOnItemClickListener(this);

        nachrichtenlisteAnzeige.setOnScrollListener(new AbsListView.OnScrollListener() {
            int leztesErstesElement = 0;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if ((totalItemCount - visibleItemCount) >= 1)
                    if (leztesErstesElement < firstVisibleItem) {
                        toolbarAusblenden();

                    } else if (leztesErstesElement > firstVisibleItem) {
                        toolbarAnzeigen();
//                        toolbarContainer.animate().translationY(0).start();
//                        findViewById(R.id.postfach_ablaufleiste_progressbar).animate().translationY(0).start();
//                        nachrichtenlisteAnzeige.setPadding(0, toolbar.getBottom(), 0, 0);
                    }

                leztesErstesElement = firstVisibleItem;
            }

        });
        keineNachrichtenAnzeige = (TextView) findViewById(R.id.postfach_keinenachrichten_textview);
        letztesMenuelement = null;

        navigationsanzeigeVerwalter = new ActionBarDrawerToggle(this, ansichtLayoutGesamt, toolbar, R.string.navigationsmenu_offen, R.string.navigationsmenu_geschlossen);

        ansichtLayoutGesamt.setDrawerListener(navigationsanzeigeVerwalter);
        navigationsanzeigeVerwalter.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem element) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = element.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(element);
    }

    @Override
    public boolean onMenuItemClick(MenuItem element) {

        ansichtLayoutGesamt.closeDrawers();
        getSupportActionBar().setTitle(element.getTitle());

        if (letztesMenuelement != null) {
            if (letztesMenuelement != element) {

                if (letzteAusgefuehrteTask.getStatus().equals(AsyncTask.Status.RUNNING))
                    letzteAusgefuehrteTask.cancel(true);
                letztesMenuelement.setChecked(false);
                element.setChecked(true);
                keineNachrichtenAnzeige.setVisibility(View.INVISIBLE);
                nachrichtenlisteAdapter.listeLoeschen();


                toolbarAnzeigen();

                String vollerOrdnername = new String();
                if (ordnerImPostfach != null)
                    for (Nachrichtenordner ordner : ordnerImPostfach.values()) {
                        if (ordner.getOrdnername().equals(element.getTitle()) ||
                                ordner.getVollerOrdnername().equals(element.getTitle()))
                            vollerOrdnername = ordner.getVollerOrdnername();
                    }

                if (!TextUtils.isEmpty(vollerOrdnername))
                    letzteAusgefuehrteTask = new NachrichtenImPostfachordnerTask(this, authentifizierung).execute(vollerOrdnername);

            } else {
                toolbarAnzeigen();
            }
        } else {

            element.setChecked(true);
            String vollerOrdnername = new String();

            if (ordnerImPostfach != null)
                for (Nachrichtenordner ordner : ordnerImPostfach.values()) {
                    if (ordner.getOrdnername().equals(element.getTitle()) ||
                            ordner.getVollerOrdnername().equals(element.getTitle()))
                        vollerOrdnername = ordner.getVollerOrdnername();
                }

            if (!TextUtils.isEmpty(vollerOrdnername))
                letzteAusgefuehrteTask = new NachrichtenImPostfachordnerTask(this, authentifizierung).execute(vollerOrdnername);
        }

        letztesMenuelement = element;
        return true;
    }

    @Override
    public void serverAufgabeErledigt(String ausgefuerteAufgabe, AufgabenErgebnis ergebnis) {

        if (ergebnis != null && ergebnis.istGueltig()) {

            switch (ausgefuerteAufgabe) {
                case OrdnerImPostfachTask.ORDNER_IM_POSTFACH:

                    ordnerImPostfach = (HashMap<String, Nachrichtenordner>) ergebnis.getErgebnis();

                    //Erstelle Menue
                    final Menu navigationsMenu = navigationAnzeige.getMenu();
                    if (ordnerImPostfach.containsKey("inbox"))
                        navigationsMenu.add(ordnerImPostfach.get("inbox").getOrdnername()).setCheckable(true)
                                .setIcon(R.drawable.ic_action_posteingang).setOnMenuItemClickListener(this);
                    if (ordnerImPostfach.containsKey("sent"))
                        navigationsMenu.add(ordnerImPostfach.get("sent").getOrdnername()).setCheckable(true)
                                .setIcon(R.drawable.ic_action_postausgang).setOnMenuItemClickListener(this);
                    if (ordnerImPostfach.containsKey("trash"))
                        navigationsMenu.add(ordnerImPostfach.get("trash").getOrdnername()).setCheckable(true)
                                .setIcon(R.drawable.ic_action_geloescht).setOnMenuItemClickListener(this);
                    if (ordnerImPostfach.containsKey("drafts"))
                        navigationsMenu.add(ordnerImPostfach.get("drafts").getOrdnername()).setCheckable(true)
                                .setIcon(R.drawable.ic_action_entwuerfe).setOnMenuItemClickListener(this);


                    //Erstelle Untermenue "Eigene Ordner"
                    if (ordnerImPostfach.containsKey("ordner_0")) {
                        final SubMenu eigeneOrdnerUntermenu = navigationsMenu.addSubMenu(R.string.navigationsmenu_eigeneOrdner_titel);
                        int ordnerIndex = 0;

                        while (ordnerImPostfach.containsKey("ordner_" + ordnerIndex)) {
                            eigeneOrdnerUntermenu.add(ordnerImPostfach.get("ordner_" + ordnerIndex).getVollerOrdnername()).setIcon(R.drawable.ic_action_folder).setOnMenuItemClickListener(this);
                            ordnerIndex++;
                        }

                        //Google Design Klasse Bug: Submenu wird nach erstellen nicht angezeigt
                        try {
                            Field presenterField = NavigationView.class.getDeclaredField("mPresenter");
                            presenterField.setAccessible(true);
                            NavigationMenuPresenter navigationMenuPresenter = (NavigationMenuPresenter) presenterField.get(navigationAnzeige);
                            navigationMenuPresenter.updateMenuView(true);

                            /*        for (int i = 0, count = navigationAnzeige.getChildCount(); i < count; i++) {
                                final View child = navigationAnzeige.getChildAt(i);
                                if (child != null && child instanceof ListView) {
                                    final ListView menuView = (ListView) child;
                                    final HeaderViewListAdapter adapter = (HeaderViewListAdapter) menuView.getAdapter();
                                    final BaseAdapter wrapped = (BaseAdapter) adapter.getWrappedAdapter();
                                    wrapped.notifyDataSetChanged();

                                }
                            }*/

                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case NachrichtenImPostfachordnerTask.NACHRICHTEN_IM_ORDNER:

                    nachrichtenListe = (List<Nachricht>) ergebnis.getErgebnis();
                    nachrichtenlisteAdapter.aktualisiereListe(nachrichtenListe);
                    if (nachrichtenListe.isEmpty()) {
                        keineNachrichtenAnzeige.setVisibility(View.VISIBLE);

                        Animation zeilenAnimation = AnimationUtils.loadAnimation(this, R.anim.abc_grow_fade_in_from_bottom);
                        zeilenAnimation.setStartOffset(keineNachrichtenAnzeige.getOffsetForPosition(10, 10));
                        zeilenAnimation.setDuration(700);
                        keineNachrichtenAnzeige.startAnimation(zeilenAnimation);
                    }
                    break;
            }
        }
    }

    private void toolbarAnzeigen() {
        if (toolbarUndFabAusgeblendet) {
            toolbarUndFabAusgeblendet = false;

            toolbar.startAnimation(toolbarEinblenden);

//            toolbar.animate().translationYBy(toolbar.getBottom()).setDuration(500).setInterpolator(new BounceInterpolator()).start();
//            findViewById(R.id.postfach_ablaufleiste_progressbar).animate().translationYBy(toolbar.getBottom())
//                   .setDuration(500).setInterpolator(new BounceInterpolator()).start();
            neueNachrichtFab.startAnimation(neueNachrichtFabEinblenden);
//            nachrichtenlisteAnzeige.setPadding(0, toolbar.getHeight(), 0, 0);

        }
        Log.i("MyApp", "ListView:" + String.valueOf(nachrichtenlisteAnzeige.getBottom()));
    }

    private void toolbarAusblenden() {
        if (!toolbarUndFabAusgeblendet) {
            toolbarUndFabAusgeblendet = true;

            toolbar.startAnimation(toolbarAusblenden);

//            findViewById(R.id.postfach_ablaufleiste_progressbar).startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_ablaufleiste_nach_oben));
//            animate().translationYBy(-toolbar.getBottom())
//                    .setDuration(500).setInterpolator(new BounceInterpolator()).start();
            neueNachrichtFab.startAnimation(neueNachrichtFabAusblenden);


        }
//        Log.i("MyApp", "Ausblenden:" + String.valueOf(nachrichtenlisteAnzeige.getPaddingTop()));
    }

    @Override
    public void onAnimationStart(Animation animation) {
        toolbar.setBottom(toolbar.getHeight());
        toolbar.setTop(0);

        Log.i("MyApp", "---- Start: " + String.valueOf(neueNachrichtFab.getTop()));
        Log.i("MyApp", "---- Start-: " + String.valueOf(neueNachrichtFab.getBottom()));
        Log.i("MyApp", String.valueOf(neueNachrichtFab.getHeight()));
        Log.i("MyApp", String.valueOf(neueNachrichtFab.getPaddingBottom()));

        final int hoeheFab = neueNachrichtFab.getHeight();
        final int paddingUntenFab = neueNachrichtFab.getPaddingBottom();
        neueNachrichtFab.setTop(nachrichtenlisteAnzeige.getBottom() - hoeheFab - paddingUntenFab);
        neueNachrichtFab.setBottom(nachrichtenlisteAnzeige.getBottom() - paddingUntenFab);
    }

    @Override
    public void onAnimationEnd(Animation animation) {

        if (animation == toolbarAusblenden) {
            toolbar.setTop(-toolbar.getHeight());
            toolbar.setBottom(0);

        } else if (animation == neueNachrichtFabAusblenden) {
            final int hoeheFab = neueNachrichtFab.getHeight();
            final int paddingUntenFab = neueNachrichtFab.getPaddingBottom();
            neueNachrichtFab.setTop(nachrichtenlisteAnzeige.getBottom());
            neueNachrichtFab.setBottom(nachrichtenlisteAnzeige.getBottom() + hoeheFab);

            Log.i("MyApp", "---- Ausblenden: " + String.valueOf(neueNachrichtFab.getTop()));
            Log.i("MyApp", "---- Ausblenden-: " + String.valueOf(neueNachrichtFab.getBottom()));
            Log.i("MyApp", String.valueOf(hoeheFab));
            Log.i("MyApp", String.valueOf(paddingUntenFab));

        } else if (animation == neueNachrichtFabEinblenden) {

            final int hoeheFab = neueNachrichtFab.getHeight();
            final int paddingUntenFab = neueNachrichtFab.getPaddingBottom();
            neueNachrichtFab.setTop(nachrichtenlisteAnzeige.getBottom() - hoeheFab - paddingUntenFab);
            neueNachrichtFab.setBottom(nachrichtenlisteAnzeige.getBottom() - paddingUntenFab);

            Log.i("MyApp", "---- Einblenden: " + String.valueOf(neueNachrichtFab.getTop()));
            Log.i("MyApp", "---- Einblenden-: " + String.valueOf(neueNachrichtFab.getBottom()));
            Log.i("MyApp", String.valueOf(hoeheFab));
            Log.i("MyApp", String.valueOf(paddingUntenFab));
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Nachricht anzuzeigendeNachricht = nachrichtenListe.get(position);

        Intent nachrichtAnzeigenIntent = new Intent(this, NachrichtAnzeigen.class);
        nachrichtAnzeigenIntent.putExtra(NachrichtAnzeigen.ANZEIGEN_ABSENDER, anzuzeigendeNachricht.getAbsender());
        nachrichtAnzeigenIntent.putExtra(NachrichtAnzeigen.ANZEIGEN_BETREFF, anzuzeigendeNachricht.getBetreff());
        nachrichtAnzeigenIntent.putExtra(NachrichtAnzeigen.ANZEIGEN_DATUM, anzuzeigendeNachricht.getErhaltenAm());
        nachrichtAnzeigenIntent.putExtra(NachrichtAnzeigen.ANZEIGEN_IN_ORDNER, anzuzeigendeNachricht.getInOrdner());
        nachrichtAnzeigenIntent.putExtra(NachrichtAnzeigen.ANZEIGEN_NACHRICHT_UID, anzuzeigendeNachricht.getEmailNachrichtUID());
        nachrichtAnzeigenIntent.putExtra(NachrichtAnzeigen.ANZEIGEN_ANTWORTEN_ADRESSEN, anzuzeigendeNachricht.getAntwortAdressen());
        startActivity(nachrichtAnzeigenIntent);

    }
}
