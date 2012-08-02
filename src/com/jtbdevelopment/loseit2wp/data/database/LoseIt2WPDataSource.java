package com.jtbdevelopment.loseit2wp.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.jtbdevelopment.loseit2wp.android.LoseIt2WP;
import com.jtbdevelopment.loseit2wp.data.LoseItSummaryMessage;
import com.jtbdevelopment.loseit2wp.data.database.common.DatabaseFieldDefinition;
import com.jtbdevelopment.loseit2wp.data.database.common.DatabaseTableDefinition;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 7/22/12
 * Time: 11:46 AM
 */

public class LoseIt2WPDataSource {
    private SQLiteDatabase readDatabase;
    private SQLiteDatabase writeDatabase;
    private final LoseIt2WPOpenHelper helper;
    private static final String ID = "_id";
    private static final String SUBJECT = "subject";
    private static final String HTML_CONTENT = "htmlContent";
    private static final String MAILBOX_TIME = "mailboxTime";
    private static final String SENT_TO_WP = "sentToWP";
    private static final String SENT_TO_WP_TIME = "sentToWPTime";
    private static final String NEW_SUMMARY = "newSummary";
    private static final String SKIP_TO_WP = "skipToWP";
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "LoseIt2WP";
    public static final DatabaseTableDefinition DATABASE_MESSAGE_TABLE = new DatabaseTableDefinition(
            "LoseIt2WPMessages",
            new ArrayList<DatabaseFieldDefinition>() {{
                add(new DatabaseFieldDefinition(ID, DatabaseFieldDefinition.INTEGER, true, true, false));
                add(new DatabaseFieldDefinition(SUBJECT, DatabaseFieldDefinition.TEXT, false, false, true));
                add(new DatabaseFieldDefinition(HTML_CONTENT, DatabaseFieldDefinition.TEXT, false, false, false));
                add(new DatabaseFieldDefinition(MAILBOX_TIME, DatabaseFieldDefinition.INTEGER, false, false, false));
                add(new DatabaseFieldDefinition(SENT_TO_WP, DatabaseFieldDefinition.INTEGER, false, false, true));
                add(new DatabaseFieldDefinition(SENT_TO_WP_TIME, DatabaseFieldDefinition.INTEGER, false, false, false));
                add(new DatabaseFieldDefinition(NEW_SUMMARY, DatabaseFieldDefinition.INTEGER, false, false, true));
                add(new DatabaseFieldDefinition(SKIP_TO_WP, DatabaseFieldDefinition.INTEGER, false, false, false));
            }});

    public LoseIt2WPDataSource(final Context context) {
        helper = new LoseIt2WPOpenHelper(context);
    }

    public synchronized void open() {
        if(readDatabase == null) {
            readDatabase = helper.getReadableDatabase();
        }                                                
        if(writeDatabase == null) {
            writeDatabase = helper.getWritableDatabase();
        }
    }

    public synchronized void close() {
        if (readDatabase != null) {
            readDatabase.close();
            readDatabase = null;
        }
        if (writeDatabase != null) {
            writeDatabase.close();
            writeDatabase = null;
        }
        helper.close();
    }

    public void clearSummaries() {
        writeDatabase.delete(DATABASE_MESSAGE_TABLE.getTableName(), null, null);
    }

    public List<LoseItSummaryMessage> getAllSummaries(final boolean hideSkipped, final boolean hideSent) {
        List<LoseItSummaryMessage> all = new ArrayList<LoseItSummaryMessage>();
        String whereSkip = null;
        String whereSent = null;
        String where;
        if (hideSkipped) {
            whereSkip = SKIP_TO_WP + "=0";
        }
        if (hideSent) {
            whereSent = SENT_TO_WP + "=0";
        }
        where = (whereSkip == null ? whereSent : whereSent == null ? whereSkip : whereSent + " AND " + whereSkip);
        Cursor cursor = readDatabase.query(DATABASE_MESSAGE_TABLE.getTableName(), null, where, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            all.add(createLoseItSummaryFromCursor(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return all;
    }

    private LoseItSummaryMessage createLoseItSummaryFromCursor(final Cursor cursor) {
        long id = cursor.getLong(0);
        String subject = cursor.getString(1);
        String htmlContent = cursor.getString(2);
        Date mailboxTime = new Date(cursor.getLong(3));
        boolean sentToWP = (cursor.getInt(4) == 1);
        Date sentToWPTime = new Date(cursor.getLong(5));
        boolean newSummary = (cursor.getInt(6) == 1);
        boolean skipToWP = (cursor.getInt(7) == 1);
        return new LoseItSummaryMessage(id, subject, htmlContent, mailboxTime, sentToWP, sentToWPTime, newSummary, skipToWP);
    }

    public void removeNewFromExistingSummaries() {
        ContentValues args = new ContentValues();
        args.put(NEW_SUMMARY, 0);
        try {
            writeDatabase.update(DATABASE_MESSAGE_TABLE.getTableName(), args, NEW_SUMMARY + "=1", null);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public LoseItSummaryMessage updateSummaryAsSent(final LoseItSummaryMessage summaryMessage) {
        summaryMessage.setSentToWP(true);
        summaryMessage.setNewSummary(false);
        ContentValues args = new ContentValues();
        args.put(NEW_SUMMARY, 0);
        args.put(SENT_TO_WP, 1);
        args.put(SENT_TO_WP_TIME, summaryMessage.getSentToWPTime().getTime());
        args.put(SKIP_TO_WP, 0);
        writeDatabase.update(DATABASE_MESSAGE_TABLE.getTableName(), args, ID + "=" + summaryMessage.getId(), null);
        return summaryMessage;
    }

    public LoseItSummaryMessage updateSummaryAsSkip(final LoseItSummaryMessage summaryMessage) {
        if (summaryMessage.getSentToWP()) {
            //  too late you did it already
            return summaryMessage;
        }

        summaryMessage.setSkipToWP(true);
        ContentValues args = new ContentValues();
        args.put(SKIP_TO_WP, 1);
        writeDatabase.update(DATABASE_MESSAGE_TABLE.getTableName(), args, ID + "=" + summaryMessage.getId(), null);
        return summaryMessage;
    }

    public List<LoseItSummaryMessage> getOrCreateSummaries(final List<LoseItSummaryMessage> summaryMessages, final boolean hideSkipped, final boolean hideSent) {
        List<LoseItSummaryMessage> results = new ArrayList<LoseItSummaryMessage>();
        for (LoseItSummaryMessage summaryMessage : summaryMessages) {
            results.add(getOrCreateSummary(summaryMessage));
        }
        deleteOldSummariesNotInEmaiList(results, hideSkipped, hideSent);
        return results;
    }

    public LoseItSummaryMessage getOrCreateSummary(final LoseItSummaryMessage summaryMessage) {
        Cursor cursor = readDatabase.query(DATABASE_MESSAGE_TABLE.getTableName(), null, SUBJECT + "=?", new String[]{summaryMessage.getSubject()}, null, null, null, null);
        LoseItSummaryMessage dbResult = null;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            dbResult = createLoseItSummaryFromCursor(cursor);
            cursor.moveToNext();
        }
        cursor.close();
        if (dbResult != null) {

            return dbResult;
        } else {
            return createSummaryInDB(summaryMessage);
        }
    }

    private void deleteOldSummariesNotInEmaiList(final List<LoseItSummaryMessage> emailSummaryMessages, final boolean hideSkipped, final boolean hideSent) {
        Set<Long> emailIds = new HashSet<Long>();
        for (LoseItSummaryMessage emailSummaryMessage : emailSummaryMessages) {
            emailIds.add(emailSummaryMessage.getId());
        }
        List<LoseItSummaryMessage> dbSummaryMessages = getAllSummaries(hideSkipped, hideSent);
        for (LoseItSummaryMessage dbSummaryMessage : dbSummaryMessages) {
            if (!emailIds.contains(dbSummaryMessage.getId())) {
                writeDatabase.delete(DATABASE_MESSAGE_TABLE.getTableName(), ID + "=" + dbSummaryMessage.getId(), null);
            }
        }
    }

    private LoseItSummaryMessage createSummaryInDB(final LoseItSummaryMessage summaryMessage) {
        ContentValues args = new ContentValues();
        args.put(SUBJECT, summaryMessage.getSubject());
        args.put(HTML_CONTENT, summaryMessage.getHtmlContent());
        args.put(MAILBOX_TIME, summaryMessage.getMailboxTime().getTime());
        args.put(SENT_TO_WP_TIME, summaryMessage.getSentToWPTime().getTime());
        args.put(SENT_TO_WP, summaryMessage.getSentToWP() ? 1 : 0);
        args.put(NEW_SUMMARY, summaryMessage.getNewSummary() ? 1 : 0);
        args.put(SKIP_TO_WP, summaryMessage.getSkipToWP() ? 1 : 0);
        summaryMessage.setId(writeDatabase.insert(DATABASE_MESSAGE_TABLE.getTableName(), null, args));
        return summaryMessage;
    }
}
