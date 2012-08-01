package com.jtbdevelopment.loseit2wp.data.helpers;

import com.jtbdevelopment.loseit2wp.data.LoseItSummaryMessage;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 7/22/12
 * Time: 1:16 PM
 */
public class SortByMailboxTimeDescending implements Comparator<LoseItSummaryMessage> {
    @Override
    public int compare(LoseItSummaryMessage summaryMessage1, LoseItSummaryMessage summaryMessage2) {
        return summaryMessage2.getMailboxTime().compareTo(summaryMessage1.getMailboxTime());
    }
}
