package com.jtbdevelopment.loseit2wp.mail;

import android.content.SharedPreferences;
import android.util.Log;
import com.jtbdevelopment.loseit2wp.data.LoseItSummaryMessage;
import com.jtbdevelopment.loseit2wp.data.transforms.FromEmailTransform;
import com.jtbdevelopment.loseit2wp.mail.protocols.oauth.XoauthLoginEmailFactory;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Store;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 3/17/12
 * Time: 6:47 PM
 */
//  TODO - support non-GMAIL
public class EmailReader extends javax.mail.Authenticator {
    public static final String GMAIL_IMAP_HOST = "imap.gmail.com";
    public static final Integer GMAIL_IMAP_PORT = 993;

    //  TODO - synchronized probably at wrong level - really want it to cover DB side as well most likely
    public synchronized static List<LoseItSummaryMessage> readLoseItEmails(final SharedPreferences preferences) {
        List<LoseItSummaryMessage> messages = new LinkedList<LoseItSummaryMessage>();
        try {
            Store store = XoauthLoginEmailFactory.getIMAP(GMAIL_IMAP_HOST, GMAIL_IMAP_PORT, preferences);
            Folder folder = store.getFolder("Inbox");
            folder.open(Folder.READ_ONLY);
            for (Message message : folder.getMessages()) {
                LoseItSummaryMessage summaryMessage = FromEmailTransform.loseItSummaryFromEmail(message);
                if (summaryMessage != null) {
                    messages.add(summaryMessage);
                }
            }
            folder.close(false);
            store.close();
            return messages;
        } catch (Exception e) {
            //  TODO - define exception
            throw new RuntimeException(e);
        }
    }
}
