package com.jtbdevelopment.loseit2wp.android.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.jtbdevelopment.loseit2wp.R;
import com.jtbdevelopment.loseit2wp.android.LoseIt2WP;
import com.jtbdevelopment.loseit2wp.android.activities.EmailListView;
import com.jtbdevelopment.loseit2wp.data.LoseItSummaryMessage;
import com.jtbdevelopment.loseit2wp.data.database.LoseIt2WPDataSource;
import com.jtbdevelopment.loseit2wp.data.preferences.LoseIt2WPPreferences;
import com.jtbdevelopment.loseit2wp.mail.EmailReader;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 7/28/12
 * Time: 5:10 PM
 */
public class MailMonitorService extends IntentService {
    public static final String NEW_LOSE_IT_MESSAGES = "New Lose It! Messages = ";
    private LoseIt2WPDataSource dataSource = null;

    public MailMonitorService() {
        super(MailMonitorService.class.getName());
    }

    @Override
    public void onCreate() {
        dataSource = new LoseIt2WPDataSource(this);
        dataSource.open();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (dataSource != null) {
            dataSource.close();
        }
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(LoseIt2WP.LOG_TAG, "Responding to intent " + intent.toString());
        SharedPreferences sharedPreferences = LoseIt2WPPreferences.getSharedPreferences(this);
        if(!LoseIt2WPPreferences.isOauthSetup(sharedPreferences)) {
            Log.d(LoseIt2WP.LOG_TAG, "OAuth not set yet - skipping");
            return;
        }

        dataSource.removeNewFromExistingSummaries();
        List<LoseItSummaryMessage> messages = EmailReader.readLoseItEmails(sharedPreferences);
        messages = dataSource.getOrCreateSummaries(messages, LoseIt2WPPreferences.getHideSkipped(sharedPreferences), LoseIt2WPPreferences.getHideSent(sharedPreferences));

        int newMails = 0;
        for (LoseItSummaryMessage message : messages) {
            newMails += message.getNewSummary() ? 1 : 0;
        }
        LoseIt2WPPreferences.setLastEmailCheckTime(sharedPreferences, new Date());

        Log.i(LoseIt2WP.LOG_TAG, "Reread mail and newMails = " + newMails);
        if (newMails > 0) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification(R.drawable.ic_menu_flash, NEW_LOSE_IT_MESSAGES + newMails, System.currentTimeMillis());
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.number += 1;
            Intent notificationIntent = new Intent(this, EmailListView.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            notification.setLatestEventInfo(this, notification.tickerText, notification.tickerText, pendingIntent);
            notificationManager.notify(1, notification);
        }
    }
}
