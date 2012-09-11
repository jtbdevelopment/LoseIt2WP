package com.jtbdevelopment.loseit2wp.mail.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by IntelliJ IDEA.
 * User: Joe
 * Date: 9/10/12
 * Time: 9:40 PM
 */
public class NetworkStatus {
    public static boolean isOnline(final Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }
}
