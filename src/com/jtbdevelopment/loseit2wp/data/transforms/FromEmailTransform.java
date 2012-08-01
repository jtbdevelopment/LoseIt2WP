package com.jtbdevelopment.loseit2wp.data.transforms;

import com.jtbdevelopment.loseit2wp.data.LoseItSummaryMessage;
import com.jtbdevelopment.loseit2wp.mail.helpers.MailParser;

import javax.mail.Message;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 3/20/12
 * Time: 9:29 PM
 */
public class FromEmailTransform {
    private static final String WEEKLY_EMAIL_START = "Lose It! Weekly Summary for Week of";

    public static LoseItSummaryMessage loseItSummaryFromEmail(final Message message) {
        String subject = MailParser.getSubject(message);
        if(isLoseItWeeklySummary(subject)) {
            return new LoseItSummaryMessage(subject, MailParser.getContent(message), MailParser.getSentTime(message));
        } else {
            return null;
        }
    }

    private static boolean isLoseItWeeklySummary(final String subject) {
        return subject != null && subject.indexOf(WEEKLY_EMAIL_START) == 0;
    }
}
