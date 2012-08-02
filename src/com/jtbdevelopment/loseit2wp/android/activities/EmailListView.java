package com.jtbdevelopment.loseit2wp.android.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.jtbdevelopment.loseit2wp.R;
import com.jtbdevelopment.loseit2wp.android.activities.actions.RefreshEmailListener;
import com.jtbdevelopment.loseit2wp.android.activities.adapters.LoseItSummaryAdapter;
import com.jtbdevelopment.loseit2wp.android.receivers.BootReceiver;
import com.jtbdevelopment.loseit2wp.data.LoseItSummaryMessage;
import com.jtbdevelopment.loseit2wp.data.helpers.SortByMailboxTimeDescending;
import com.jtbdevelopment.loseit2wp.data.preferences.LoseIt2WPPreferences;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class EmailListView extends ActivityWithDataSource {
    private static final String CHECK_EMAIL_ENABLED_TEXT = "Refresh";
    public static final String CHECK_EMAIL_DISABLED_TEXT = "Refreshing..";
    private static final String HIDE_SENT_TEXT = "Hide Sent";
    private static final String SHOW_SENT_TEXT = "Show Sent";
    private static final String HIDE_SKIPPED_TEXT = "Hide Skipped";
    private static final String SHOW_SKIPPED_TEXT = "Show Skipped";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emaillistview);

        SharedPreferences preferences = LoseIt2WPPreferences.getSharedPreferences(this);
        doStartupSetupChecks(preferences);

        List<LoseItSummaryMessage> messageList = dataSource.getAllSummaries(LoseIt2WPPreferences.getHideSkipped(preferences), LoseIt2WPPreferences.getHideSent(preferences));
        Collections.sort(messageList, new SortByMailboxTimeDescending());
        final LoseItSummaryAdapter emailListAdaptor = new LoseItSummaryAdapter(this, messageList);
        ListView emailList = (ListView) findViewById(R.id.emaillist);
        emailList.setAdapter(emailListAdaptor);
        emailList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LoseItSummaryMessage summaryMessage = emailListAdaptor.getItem(i);
                Bundle bundle = new Bundle();
                bundle.putParcelable("summaryMessage", summaryMessage);
                Intent intent = new Intent();
                intent.setClass(view.getContext(), EmailPreview.class);
                intent.putExtras(bundle);
                view.getContext().startActivity(intent);
            }
        });
        emailList.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final LoseItSummaryMessage summaryMessage = emailListAdaptor.getItem(i);
                AlertDialog.Builder adb = new AlertDialog.Builder(EmailListView.this);
                adb.setTitle("Skip?");
                if (summaryMessage.getSentToWP()) {
                    adb.setMessage("This one was sent already.  Can't skip it now!");
                } else {
                    adb.setMessage("Don't send this one?");
                    adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dataSource.updateSummaryAsSkip(summaryMessage);
                            EmailListView.this.refreshEmailList();
                        }
                    });
                }
                adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                adb.show();
                return true;
            }
        });

        Button checkEmailButton = (Button) findViewById(R.id.checkemail);
        checkEmailButton.setOnClickListener(new RefreshEmailListener(checkEmailButton, emailListAdaptor, dataSource, preferences));
        checkEmailButton.getBackground().setColorFilter(0xFF000000 + this.getResources().getInteger(R.integer.loseitorange), PorterDuff.Mode.MULTIPLY);
        String emailButtonText = generateRefreshButtonText(preferences);
        checkEmailButton.setText(emailButtonText);
        checkEmailButton.setEnabled(true);

        //  First run of the instance
        if( savedInstanceState == null ) {
            BootReceiver.kickOffMailMonitor(this);
        }
    }

    private void doStartupSetupChecks(SharedPreferences preferences) {
        if(!LoseIt2WPPreferences.isOauthSetup(preferences)) {
            launchOAuthVerificationView();
        }
        if(!LoseIt2WPPreferences.preferencesAreSet(preferences)) {
            launchSettingsView();
        }
    }

    public static String generateRefreshButtonText(final SharedPreferences preferences) {
        return CHECK_EMAIL_ENABLED_TEXT + " (" + LoseIt2WPPreferences.getLastEmailCheckTime(preferences).toLocaleString() + ")";
    }

    @Override
    protected void onRestart() {  
        refreshEmailList();
        super.onResume();
    }

    @Override
    protected void onResume() {
        refreshEmailList();
        super.onResume();
    }

    @Override
    protected void onUserLeaveHint() {
        dataSource.removeNewFromExistingSummaries();
        super.onUserLeaveHint();
    }

    public void refreshEmailList() {
        SharedPreferences preferences = LoseIt2WPPreferences.getSharedPreferences(this);
        List<LoseItSummaryMessage> messageList = dataSource.getAllSummaries(LoseIt2WPPreferences.getHideSkipped(preferences), LoseIt2WPPreferences.getHideSent(preferences));
        Collections.sort(messageList, new SortByMailboxTimeDescending());
        ListView emailList = (ListView) findViewById(R.id.emaillist);
        LoseItSummaryAdapter adapter = (LoseItSummaryAdapter) emailList.getAdapter();
        adapter.clear();
        adapter.addAll(messageList);
        Button checkEmailButton = (Button) findViewById(R.id.checkemail);
        checkEmailButton.setText(generateRefreshButtonText(preferences));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SharedPreferences sharedPreferences = LoseIt2WPPreferences.getSharedPreferences(this);
        setHideShowSentMenuOptionText(menu.findItem(R.id.hideshowsent), sharedPreferences);
        setHideShowSkippedOptionText(menu.findItem(R.id.hideshowskiped), sharedPreferences);
        return super.onPrepareOptionsMenu(menu);
    }

    private void setHideShowSkippedOptionText(MenuItem menuItem, SharedPreferences sharedPreferences) {
        menuItem.setTitle(LoseIt2WPPreferences.getHideSkipped(sharedPreferences) ? SHOW_SKIPPED_TEXT : HIDE_SKIPPED_TEXT);
    }

    private void setHideShowSentMenuOptionText(MenuItem menuItem, SharedPreferences sharedPreferences) {
        menuItem.setTitle(LoseIt2WPPreferences.getHideSent(sharedPreferences) ? SHOW_SENT_TEXT : HIDE_SENT_TEXT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final SharedPreferences sharedPreferences = LoseIt2WPPreferences.getSharedPreferences(this);
        switch(item.getItemId()) {
            case R.id.emailsettings:
                launchOAuthVerificationView();
                return true;
            case R.id.hideshowsent:
                LoseIt2WPPreferences.toggleHideSent(sharedPreferences);
                refreshEmailList();
                invalidateOptionsMenu();
                return true;
            case R.id.hideshowskiped:
                LoseIt2WPPreferences.toggleHideSkipped(sharedPreferences);
                refreshEmailList();
                invalidateOptionsMenu();
                return true;
            case R.id.clearlocal:
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setTitle("Reset all tracking?  Are you sure?");
                    adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dataSource.clearSummaries();
                            LoseIt2WPPreferences.setLastEmailCheckTime(sharedPreferences, new Date(0));
                            refreshEmailList();
                        }
                    });
                adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                adb.show();
                return true;
            case R.id.settings:
                launchSettingsView();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void launchSettingsView() {
        Intent intent = new Intent();
        intent.setClass(this, SettingsView.class);
        this.startActivity(intent);
    }
    
    private void launchOAuthVerificationView() {
        Intent intent = new Intent();
        intent.setClass(this, OAuthSettingsView.class);
        startActivity(intent);
    }
}
