package com.jtbdevelopment.loseit2wp.android.activities.actions;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import com.jtbdevelopment.loseit2wp.android.activities.EmailListView;
import com.jtbdevelopment.loseit2wp.data.LoseItSummaryMessage;
import com.jtbdevelopment.loseit2wp.data.database.LoseIt2WPDataSource;
import com.jtbdevelopment.loseit2wp.data.helpers.SortByMailboxTimeDescending;
import com.jtbdevelopment.loseit2wp.data.preferences.LoseIt2WPPreferences;
import com.jtbdevelopment.loseit2wp.mail.EmailReader;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 3/17/12
 * Time: 7:06 PM
 */
public class RefreshEmailListener implements View.OnClickListener {
    private final Button button;
    private final ArrayAdapter<LoseItSummaryMessage> emailListAdapter;
    private final LoseIt2WPDataSource dataSource;
    private final SharedPreferences sharedPreferences;

    public RefreshEmailListener(final Button button, final ArrayAdapter<LoseItSummaryMessage> emailListAdapter, final LoseIt2WPDataSource dataSource, final SharedPreferences sharedPreferences) {
        super();
        this.button = button;
        this.emailListAdapter = emailListAdapter;
        this.dataSource = dataSource;
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public void onClick(View view) {
        emailListAdapter.clear();
        button.setText(EmailListView.CHECK_EMAIL_DISABLED_TEXT);
        button.setEnabled(false);
        new Refresher().execute(view.getContext());
    }

    private class Refresher extends AsyncTask<Context, Void, List<LoseItSummaryMessage>> {
        @Override
        protected List<LoseItSummaryMessage> doInBackground(final Context... contexts) {
            dataSource.removeNewFromExistingSummaries();
            List<LoseItSummaryMessage> messages = EmailReader.readLoseItEmails(sharedPreferences);
            messages = dataSource.getOrCreateSummaries(messages, LoseIt2WPPreferences.getHideSkipped(sharedPreferences), LoseIt2WPPreferences.getHideSent(sharedPreferences));
            Collections.sort(messages, new SortByMailboxTimeDescending());
            return messages;
        }

        @Override
        protected void onPostExecute(final List<LoseItSummaryMessage> messages) {
            emailListAdapter.clear();
            emailListAdapter.addAll(messages);
            LoseIt2WPPreferences.setLastEmailCheckTime(sharedPreferences, new Date());
            button.setText(EmailListView.generateRefreshButtonText(sharedPreferences));
            button.setEnabled(true);
        }
    }
}
