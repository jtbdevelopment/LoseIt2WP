package com.jtbdevelopment.loseit2wp.mail.helpers;

import android.util.Log;
import static com.jtbdevelopment.loseit2wp.android.LoseIt2WP.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import java.io.IOException;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 3/20/12
 * Time: 9:30 PM
 */
public class MailParser {
    public static String getSubject(final Message message) {
        try {
            return message.getSubject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Date getSentTime(final Message message) {
        try {
            return message.getSentDate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getContent(final Part p) {
        try {
            return getContentPrivate(p);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getContentPrivate(final Part p) throws
            MessagingException, IOException {

        if (p.isMimeType("text/*")) {
            return (String) p.getContent();
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getContent(bp);
                } else if (bp.isMimeType("text/html")) {
                    String s = getContent(bp);
                    if (s != null)
                        return s;
                } else {
                    return getContent(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getContent(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }

        return null;
    }
}
