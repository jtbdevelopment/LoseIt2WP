package com.jtbdevelopment.loseit2wp.mail;


import android.content.SharedPreferences;
import android.util.Log;
import com.jtbdevelopment.loseit2wp.data.preferences.LoseIt2WPPreferences;
import com.jtbdevelopment.loseit2wp.mail.protocols.oauth.XoauthLoginEmailFactory;
import com.sun.mail.smtp.SMTPTransport;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 3/19/12
 * Time: 8:44 PM
 */
//  TODO - support non-GMAIL
public class EmailSender {
    final static public String GMAIL_SMTP_HOST = "smtp.gmail.com";
//    final static Integer GMAIL_SMTP_STANDARD_PORT = 465;
    final static public Integer GMAIL_SMTP_OATH_PORT = 587;

    public static synchronized void sendMail(final SharedPreferences preferences, final String subject, final String htmlContent) throws Exception {
        final String user = LoseIt2WPPreferences.getEmailLogin(preferences);
        final String recipients = LoseIt2WPPreferences.getEmailRecipients(preferences);
        try {
            Session session = XoauthLoginEmailFactory.getSMTPSession();
            MimeMessage message = new MimeMessage(session);
            message.setDataHandler(new DataHandler(new ByteArrayDataSource(htmlContent.getBytes(), "text/html")));
            message.setSender(new InternetAddress(user));
            message.setSubject(subject);
            if (recipients.indexOf(',') > 0) {
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            } else {
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
            }
            SMTPTransport transport = XoauthLoginEmailFactory.getSMTPTransport(session, GMAIL_SMTP_HOST, GMAIL_SMTP_OATH_PORT, preferences);
            transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
        } catch (Exception e) {
            //
        }
    }
}