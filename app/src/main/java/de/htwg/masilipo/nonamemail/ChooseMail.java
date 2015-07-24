//package de.htwg.masilipo.nonamemail;
//
//import android.accounts.Account;
//import android.accounts.AccountManager;
//import android.app.Activity;
//import android.content.Intent;
//import android.os.Bundle;
//import android.support.v7.widget.CardView;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.View.OnClickListener;
//
//
//public class ChooseMail extends Activity implements OnClickListener {
//
//    public static final String MAIL_SERVICE = "MAIL SERVICE";
//    public static final String GOOGLE = "GOOGLE";
//    public static final String GMX = "GMX";
//    public static final String YAHOO = "YAHOO";
//    public static final String OUTLOOK = "OUTLOOK";
//    public static final String OTHER_SERVICE = "OTHER SERVICE";
//    //Google
//    private static final String PREF_ACCOUNT_NAME = "accountName";
//    static final int REQUEST_ACCOUNT_PICKER = 1000;
//    static final int REQUEST_AUTHORIZATION = 1001;
//    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.__activity_choose_mail);
//
//        CardView cv_google = (CardView) findViewById(R.id.cv_google);
//        cv_google.setOnClickListener(this);
//        CardView cv_gmx = (CardView) findViewById(R.id.cv_gmx);
//        cv_gmx.setOnClickListener(this);
//        CardView cv_yahoo = (CardView) findViewById(R.id.cv_yahoo);
//        cv_yahoo.setOnClickListener(this);
//        CardView cv_outlook = (CardView) findViewById(R.id.cv_outlook);
//        cv_outlook.setOnClickListener(this);
//        CardView cv_enterService = (CardView) findViewById(R.id.cv_enterService);
//        cv_enterService.setOnClickListener(this);
//
//    }
//
//    @Override
//    public void onClick(View v) {
//
//        Intent login = new Intent(this, ChooseMailLogin.class);
//
//        switch (v.getId()) {
//            case R.id.cv_google:
//                AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
//                Account[] list = manager.getAccounts();
//
//                login.putExtra(MAIL_SERVICE, GOOGLE);
//                startActivity(login);
//                break;
//            case R.id.cv_gmx:
//                login.putExtra(MAIL_SERVICE, GMX);
//                startActivity(login);
//                break;
//            case R.id.cv_yahoo:
//                login.putExtra(MAIL_SERVICE, YAHOO);
//                startActivity(login);
//                break;
//            case R.id.cv_outlook:
//                login.putExtra(MAIL_SERVICE, OUTLOOK);
//                startActivity(login);
//                break;
//            case R.id.cv_enterService:
//                Intent loginWithOther = new Intent(this, LoginAutomatisch.class);
//                startActivity(loginWithOther);
//                break;
//        }
//
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_choose_mail, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//}
