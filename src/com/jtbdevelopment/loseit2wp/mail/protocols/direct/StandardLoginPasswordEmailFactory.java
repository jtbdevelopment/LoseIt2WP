package com.jtbdevelopment.loseit2wp.mail.protocols.direct;

import android.content.SharedPreferences;
import android.util.Log;
import com.jtbdevelopment.loseit2wp.android.LoseIt2WP;
import com.jtbdevelopment.loseit2wp.data.preferences.LoseIt2WPPreferences;

import javax.mail.*;
import java.security.Security;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 7/29/12
 * Time: 1:33 PM
 */
@Deprecated
public class StandardLoginPasswordEmailFactory {
    static {
        Security.addProvider(new JSSEProvider());
    }

    public static Store getIMAP(final String hostName, final Integer hostPort, final SharedPreferences preferences) throws MessagingException {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        Log.d(LoseIt2WP.LOG_TAG, "Transport: " + props.getProperty("mail.transport.protocol"));
        Log.d(LoseIt2WP.LOG_TAG, "Store: " + props.getProperty("mail.store.protocol"));
        Log.d(LoseIt2WP.LOG_TAG, "Host: " + props.getProperty("mail.imap.host"));
        Log.d(LoseIt2WP.LOG_TAG, "Authentication: " + props.getProperty("mail.imap.auth"));
        Log.d(LoseIt2WP.LOG_TAG, "Port: " + props.getProperty("mail.imap.port"));
        Session session = Session.getInstance(System.getProperties());
        session.setDebug(false);
        Store store = session.getStore("imaps");
        store.connect(hostName, hostPort,  LoseIt2WPPreferences.getEmailLogin(preferences), LoseIt2WPPreferences.getEmailPassword(preferences));
        Log.i(LoseIt2WP.LOG_TAG, "Store: " + store.toString());
        return store;
    }
    
    public static Session getSMTPSession(final String hostName, final Integer hostPort, final SharedPreferences preferences) {
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", hostName);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", hostPort.toString());
        props.put("mail.smtp.socketFactory.port", hostPort.toString());
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(LoseIt2WPPreferences.getEmailLogin(preferences), LoseIt2WPPreferences.getEmailPassword(preferences));
            }
        });
    }
}
