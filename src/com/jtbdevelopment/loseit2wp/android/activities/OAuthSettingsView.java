package com.jtbdevelopment.loseit2wp.android.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import com.jtbdevelopment.loseit2wp.R;
import com.jtbdevelopment.loseit2wp.android.LoseIt2WP;
import com.jtbdevelopment.loseit2wp.data.preferences.LoseIt2WPPreferences;
import com.jtbdevelopment.loseit2wp.mail.EmailReader;
import com.jtbdevelopment.loseit2wp.mail.EmailSender;
import com.jtbdevelopment.loseit2wp.mail.protocols.oauth.OAuthHelper;
import com.jtbdevelopment.loseit2wp.mail.protocols.oauth.XoauthLoginEmailFactory;
import com.sun.mail.smtp.SMTPMessage;
import com.sun.mail.smtp.SMTPTransport;
import oauth.signpost.OAuth;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 7/28/12
 * Time: 10:13 PM
 */

//  TODO - would be good to consolidate OAuth code - using both signpost and oauth between this and email providers
public class OAuthSettingsView extends Activity {

    private static final String GMAIL_COM = "gmail.com";
    private static final String AUTHORIZATION_PREVIOUSLY_ESTABLISHED = "Authorization Previously Established.";
    public static final String MULTIPLE_WARNING1 = "If you have multiple gmail accounts -";
    public static final String MULTIPLE_WARNING2 = "Make sure to authorize the correct one when the web page appears.";
    public static final String HTTPS_MAIL_GOOGLE_COM = "https://mail.google.com/";

    private OAuthHelper helper = null;
    private Spinner emailAccounts;
    private TextView statusRead;
    private TextView statusWrite;
    private Button checkButton;
    private Button grantButton;
    private Button revokeButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oauthsettings);

        final SharedPreferences preferences = LoseIt2WPPreferences.getSharedPreferences(this);
        statusRead = (TextView) findViewById(R.id.oauthStatusRead);
        statusWrite = (TextView) findViewById(R.id.oauthStatusWrite);
        emailAccounts = (Spinner) findViewById(R.id.emailAdress);
        checkButton = (Button) findViewById(R.id.checkOAuth);
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testOAuth();
            }
        });
        grantButton = (Button) findViewById(R.id.grantOAuth);
        grantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                grantOAuth();
            }
        });

        revokeButton = (Button) findViewById(R.id.revokeOAuth);
        revokeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder adb = new AlertDialog.Builder(OAuthSettingsView.this);
                adb.setTitle("Are you sure?");
                //  TODO - send revoke request instead
                adb.setMessage("This will clear access tokens in LoseIt2WP.  You will need to revoke access in GMail as well.");
                adb.setPositiveButton("Go Ahead", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        LoseIt2WPPreferences.flushOAuthTokens(preferences);
                        OAuthSettingsView.this.resetScreen(true);
                    }
                });
                adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                adb.show();
            }
        });

        resetScreen(true);
    }

    private void resetScreen(boolean overwriteStatues) {
        final SharedPreferences preferences = LoseIt2WPPreferences.getSharedPreferences(this);

        Account[] accounts = AccountManager.get(this).getAccounts();
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item);
        emailAccounts.setAdapter(accountAdapter);
        String existingEmailLogin = LoseIt2WPPreferences.getEmailLogin(preferences);
        int existingCount = -1;
        for (Account account : accounts) {
            if (account.name.endsWith(GMAIL_COM)) {
                accountAdapter.add(account.name);
                if (account.name.equals(existingEmailLogin)) {
                    existingCount = accountAdapter.getCount();
                }
            }
        }
        accountAdapter.notifyDataSetChanged();
        if (existingCount >= 0) {
            emailAccounts.setSelection(existingCount - 1);
        }
        if (LoseIt2WPPreferences.isOauthSetup(LoseIt2WPPreferences.getSharedPreferences(this))) {
            checkButton.setEnabled(true);
            checkButton.setVisibility(View.VISIBLE);
            revokeButton.setEnabled(true);
            grantButton.setVisibility(View.INVISIBLE);
            grantButton.setEnabled(false);
            emailAccounts.setEnabled(false);
        } else {
            checkButton.setEnabled(false);
            checkButton.setVisibility(View.INVISIBLE);
            revokeButton.setEnabled(false);
            grantButton.setVisibility(View.VISIBLE);
            grantButton.setEnabled(true);
            emailAccounts.setEnabled(true);
        }

        if(overwriteStatues) {
        if (LoseIt2WPPreferences.isOauthSetup(LoseIt2WPPreferences.getSharedPreferences(this))) {
            statusRead.setText(AUTHORIZATION_PREVIOUSLY_ESTABLISHED);
            statusWrite.setText(AUTHORIZATION_PREVIOUSLY_ESTABLISHED);
        } else {
            statusRead.setText(MULTIPLE_WARNING1);
            statusWrite.setText(MULTIPLE_WARNING2);
        }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        SharedPreferences preferences = LoseIt2WPPreferences.getSharedPreferences(this);
        if (LoseIt2WPPreferences.isOauthSetup(preferences) || helper == null) {
            return;
        }
        try {
            statusRead.setText("Processing response ..");
            statusWrite.setText("");
            Log.i(LoseIt2WP.LOG_TAG, "onNewIntent");
            Uri uri = intent.getData();
            if (uri != null) {
                Log.i(LoseIt2WP.LOG_TAG, "uri.getScheme()? " + uri.getScheme());
                if (uri.getScheme().equals(LoseIt2WP.OAUTH_APPNAME.toLowerCase())) {
                    Log.i(LoseIt2WP.LOG_TAG, "got it");
                    String positionString = uri.getQueryParameter("selection");
                    if (positionString == null || positionString.equals("") || Integer.parseInt(positionString) < 0) {  //More validating parseable
                        statusRead.setText("Something went wrong - no account info returned");
                    } else {
                        String oauthVerifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
                        String[] tokens = helper.getAccessToken(oauthVerifier);
                        String token = tokens[0], tokenSecret = tokens[1];
                        Log.i(LoseIt2WP.LOG_TAG, token);
                        Log.i(LoseIt2WP.LOG_TAG, tokenSecret);
                        LoseIt2WPPreferences.setOAuthTokens(preferences, emailAccounts.getItemAtPosition(Integer.parseInt(positionString)).toString(), token, tokenSecret);
                        statusRead.setText("Got it ..");
                        statusWrite.setText("Probably should test it out.. :)");
                        helper = null;
                        resetScreen(false);
                    }
                } else {
                    Log.i(LoseIt2WP.LOG_TAG, "non matching schema");
                    statusRead.setText("Something went wrong - non matching schema");
                }
            } else {
                Log.i(LoseIt2WP.LOG_TAG, "uri is null");
                statusRead.setText("Something went wrong - null uri");
            }
        } catch (Exception e) {
            Log.i(LoseIt2WP.LOG_TAG, "exception", e);
            statusRead.setText("Something went wrong - general failure");
        }
    }

    private void grantOAuth() {
        try {
            Log.i(LoseIt2WP.LOG_TAG, "Initializing OAuthHelper");
            helper = new OAuthHelper(LoseIt2WP.ANONYMOUS, LoseIt2WP.ANONYMOUS, HTTPS_MAIL_GOOGLE_COM, LoseIt2WP.LOSE_IT2_WP_CALLBACK + emailAccounts.getSelectedItemPosition(), LoseIt2WP.OAUTH_APPNAME);
            Log.i(LoseIt2WP.LOG_TAG, "Getting Request Token");
            String url = helper.getRequestToken();
            Log.i(LoseIt2WP.LOG_TAG, "Activity with url " + url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_FROM_BACKGROUND);
            startActivity(intent);
        } catch (Exception e) {
            Log.i(LoseIt2WP.LOG_TAG, "exception ", e);
            statusRead.setText("Failed to start ..");
        }
    }

    private void testOAuth() {
        SharedPreferences preferences = LoseIt2WPPreferences.getSharedPreferences(this);
        Log.i(LoseIt2WP.LOG_TAG, "Testing OAUTH");
        try {
            Session session = XoauthLoginEmailFactory.getSMTPSession();

            SMTPTransport smtpTransport = XoauthLoginEmailFactory.getSMTPTransport(
                    session,
                    EmailSender.GMAIL_SMTP_HOST,
                    EmailSender.GMAIL_SMTP_OATH_PORT,
                    preferences);
            statusWrite.setText("Successfully authenticated to SMTP.");
            Message testMail = new SMTPMessage(session);
            InternetAddress internetAddress = new InternetAddress(LoseIt2WPPreferences.getEmailLogin(preferences));
            testMail.setFrom(internetAddress);
            testMail.setRecipient(Message.RecipientType.TO, internetAddress);
            testMail.setSubject("Test email from LoseIt2WP - Good luck!");
            testMail.setContent("Welcome aboard, hope the app is useful for you!", "text/plain");
            smtpTransport.sendMessage(testMail, testMail.getRecipients(Message.RecipientType.TO));
            statusWrite.setText("Sent test message which you should receive.");

            Store imapStore = XoauthLoginEmailFactory.getIMAP(EmailReader.GMAIL_IMAP_HOST, EmailReader.GMAIL_IMAP_PORT, preferences);
            Folder folder = imapStore.getFolder("Inbox");
            folder.open(Folder.READ_ONLY);
            int count = folder.getMessages().length;
            statusRead.setText("Verified ability to check inbox - you have " + count + " emails.");
            folder.close(false);
        } catch (Exception e) {
            Log.i(LoseIt2WP.LOG_TAG, "exception", e);
            statusRead.setText("Houston we have a problem");
        }
    }
}
