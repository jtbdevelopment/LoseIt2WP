package com.jtbdevelopment.loseit2wp.mail.protocols.oauth;

import android.content.SharedPreferences;
import com.google.code.samples.xoauth.XoauthAuthenticator;
import com.jtbdevelopment.loseit2wp.android.LoseIt2WP;
import com.jtbdevelopment.loseit2wp.data.preferences.LoseIt2WPPreferences;
import com.sun.mail.smtp.SMTPTransport;
import net.oauth.OAuthConsumer;

import javax.mail.Session;
import javax.mail.Store;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 7/29/12
 * Time: 1:51 PM
 */

//  TODO - eliminate this or oauth - have signpost + xoauth + oauth
public class XoauthLoginEmailFactory {
    static {
        XoauthAuthenticator.initialize();
    }

    public static Store getIMAP(final String hostName, final Integer hostPort, final SharedPreferences preferences) throws Exception {
        return XoauthAuthenticator.connectToImap(hostName,
                hostPort,
                LoseIt2WPPreferences.getEmailLogin(preferences),
                LoseIt2WPPreferences.getOauthToken(preferences),
                LoseIt2WPPreferences.getOauthTokenSecret(preferences),
                new OAuthConsumer(LoseIt2WP.LOSE_IT2_WP_CALLBACK, LoseIt2WP.ANONYMOUS, LoseIt2WP.ANONYMOUS, null),
                false);
    }

    public static Session getSMTPSession() throws Exception {
        return XoauthAuthenticator.getSessionForSmtp(false);
    }

    public static SMTPTransport getSMTPTransport(final Session session, final String hostName, final Integer hostPort, final SharedPreferences preferences) throws Exception {
        return XoauthAuthenticator.connectToSmtp(
                session,
                hostName,
                hostPort,
                LoseIt2WPPreferences.getEmailLogin(preferences),
                LoseIt2WPPreferences.getOauthToken(preferences),
                LoseIt2WPPreferences.getOauthTokenSecret(preferences),
                XoauthAuthenticator.getAnonymousConsumer());
    }
}
