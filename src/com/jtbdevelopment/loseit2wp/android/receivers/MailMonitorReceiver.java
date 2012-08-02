package com.jtbdevelopment.loseit2wp.android.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.jtbdevelopment.loseit2wp.android.services.MailMonitorService;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 7/16/12
 * Time: 9:18 PM
 */
public class MailMonitorReceiver extends BroadcastReceiver {
    private static final int OFFSET_HOURLY = 60 * 60 * 1000;
    private static final int OFFSET_HALFDAY = OFFSET_HOURLY * 12;
    private static final int OFFSET_DAILY = OFFSET_HALFDAY * 2;
    
    @Override
    public void onReceive(final Context context, final Intent intent) {
        Intent serviceIntent = new Intent(context, MailMonitorService.class);
        context.startService(serviceIntent);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, MailMonitorReceiver.class), 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, determineNextWakeUp(), pendingIntent);
    }
    
    private long determineNextWakeUp() {
        int offset;
        long currentTimeMillis = System.currentTimeMillis();
        Date now = new Date(currentTimeMillis);
        Calendar cal = new GregorianCalendar(now.getYear(), now.getMonth(), now.getDay());
        switch(cal.get(Calendar.DAY_OF_WEEK)) {
            case 1:  // mondays
                offset = OFFSET_HOURLY;
                break;
            case 2:  // tuesdays
                offset = OFFSET_HALFDAY;
                break;
            default:
                offset = OFFSET_DAILY;
                break;
        }
        return currentTimeMillis + offset;
    }
}
