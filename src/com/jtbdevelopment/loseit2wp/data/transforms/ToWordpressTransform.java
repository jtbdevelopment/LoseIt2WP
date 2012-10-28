package com.jtbdevelopment.loseit2wp.data.transforms;

import android.content.SharedPreferences;
import com.jtbdevelopment.loseit2wp.data.LoseItSummaryMessage;
import com.jtbdevelopment.loseit2wp.data.preferences.LoseIt2WPPreferences;

import java.text.DateFormatSymbols;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 3/20/12
 * Time: 9:49 PM
 */
//  TODO - xslt?
public class ToWordpressTransform {
    public static String transformContent(final SharedPreferences preferences, final LoseItSummaryMessage summaryMessage) {
        int start;
        int end;
        String newContent = summaryMessage.getHtmlContent();
        if (LoseIt2WPPreferences.getStripName(preferences)) {
            start = newContent.indexOf("<p style=\"padding-top: 0px; margin-top: 0px; padding-bottom: 10px;\">for ");
            end = newContent.indexOf("<table", start);
            newContent = newContent.substring(0, start) + newContent.substring(end, newContent.length());
        }
        if (LoseIt2WPPreferences.getStripUnsubscribe(preferences)) {
            start = newContent.indexOf("<a href=\"http://www.loseit.com/unsubscribe");
            end = newContent.indexOf("</div>", start);
            newContent = newContent.substring(0, start) + newContent.substring(end, newContent.length());
        }
        start = newContent.indexOf("<body");
        end = newContent.indexOf("<", start + 1);
        newContent = newContent.substring(0, end - 1) + "\n" + addWPShortCodes(preferences, summaryMessage) + newContent.substring(end, newContent.length());
        newContent = newContent.replace("</body>", "Posted with the assistance of LoseIt2WP, a free Android App.</body>");
        return newContent;
    }

    private static String addWPShortCodes(final SharedPreferences preferences, final LoseItSummaryMessage summaryMessage) {
        StringBuilder wpCodes = new StringBuilder();
        if (LoseIt2WPPreferences.hasWPCategories(preferences)) {
            wpCodes.append("[category ").append(LoseIt2WPPreferences.getWPCategories(preferences)).append("]\n");
        }
        if (LoseIt2WPPreferences.hasWPTags(preferences)) {
            wpCodes.append("[tags ").append(LoseIt2WPPreferences.getWPTags(preferences)).append("]\n");
        }
        if (LoseIt2WPPreferences.getDelayPublishFlag(preferences)) {
            wpCodes.append("[delay ").append(calculateDelayUntilPublishTimeInUTC(preferences, summaryMessage)).append(" minutes]\n");
        }
        return wpCodes.toString();
    }

    private static String calculateDelayUntilPublishTimeInUTC(final SharedPreferences preferences, final LoseItSummaryMessage summaryMessage) {
        Date now = new Date();
        Date publishTime = determineSummaryTime(summaryMessage);

        int preferredDayOfWeek = LoseIt2WPPreferences.getDelayPublishDayOfWeek(preferences);
        if (preferredDayOfWeek > publishTime.getDay()) {
            publishTime.setTime(publishTime.getTime() + ((preferredDayOfWeek - publishTime.getDay()) * 24 * 60 * 60 * 1000));
        }
        publishTime.setHours(LoseIt2WPPreferences.getDelayPublishHour(preferences));
        publishTime.setMinutes(0);
        publishTime.setSeconds(0);
        long delayInMS = (publishTime.getTime() - now.getTime());
        long delayInMinutes = delayInMS / 1000 / 60;
        delayInMinutes -= now.getTimezoneOffset();
        delayInMinutes = Math.max(0, delayInMinutes);   //  Skip negative numbers
        return String.format("%+d", delayInMinutes);
    }

    private final static List<String> usaShortMonthData;

    static {
        //  Force to US Local since Lose It sends out US ones
        DateFormatSymbols df = new DateFormatSymbols(Locale.US);
        usaShortMonthData = new ArrayList<String>();
        Collections.addAll(usaShortMonthData, df.getShortMonths());
    }

    private static Date determineSummaryTime(final LoseItSummaryMessage summaryMessage) {
        String subject = summaryMessage.getSubject();
        int comma = subject.indexOf(',');
        int firstSpace = subject.indexOf(' ', comma);
        int secondSpace = subject.indexOf(' ', firstSpace + 1);
        String subjectMonth = summaryMessage.getSubject().substring(firstSpace + 1, secondSpace);
        String subjectDate = summaryMessage.getSubject().substring(secondSpace + 1, secondSpace + 3);
        if (!subjectDate.matches("((-|\\\\+)?[0-9]+(\\\\.[0-9]+)?)+")) {
            subjectDate = subjectDate.substring(0, 1);
        }
        Integer dayOfMonth = Integer.parseInt(subjectDate);
        Calendar calendar = new GregorianCalendar();
        calendar.set(summaryMessage.getMailboxTime().getYear() + 1900, usaShortMonthData.indexOf(subjectMonth), dayOfMonth, 0, 0, 0);
        //  Roll it a week later.
        calendar.add(Calendar.DAY_OF_WEEK, 7);
        return calendar.getTime();
    }
}
