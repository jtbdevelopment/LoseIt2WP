package com.jtbdevelopment.loseit2wp.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 7/21/12
 * Time: 7:27 PM
 */
public class LoseIt2WPPreferences {
    private static final String PREFERENCES_NAME = "LoseIt2WPPreferences";
    private static final String DEFAULT_STRING = "";

    private static final String EMAIL_LOGIN_KEY = "EMAIL_LOGIN";
//    private static final String EMAIL_PASSWORD_KEY = "EMAIL_PASSWORD";
    private static final String EMAIL_CHECK_TIMESTAMP_KEY = "EMAIL_CHECK_TIME";

    private static final String EMAIL_RECIPIENTS_KEY = "EMAIL_RECIPIENTS";
    private static final String WP_CATEGORIES = "WP_CATEGORIES";
    private static final String WP_TAGS = "WP_TAGS";
    private static final String DELAY_PUBLISH_FLAG_KEY = "DELAY_PUBLICATION";

    private static final String DELAY_PUBLISH_DAY_OF_WEEK_KEY = "DELAY_TO_DAY_OF_WEEK";
    private static final String DELAY_PUBLISH_HOUR_KEY = "DELAY_TO_HOUR";

    private static final String HIDE_SKIPPED_KEY = "HIDE_SKIPPED";
    private static final String HIDE_SENT_KEY = "HIDE_SENT";

    private static final String STRIP_NAME_KEY = "STRIP_NAME";
    private static final String STRIP_UNSUBSCRIBE_KEY = "STRIP_UNSUBSCRIBE";

    private static final String PREFERENCES_VERSION_KEY = "PREFERENCES_VERSION";
    private static final int PREFERENCES_VERSION = 1;
    
    private static final String OAUTH_READY_KEY = "OAUTH_READY";
    private static final String OAUTH_TOKEN_KEY = "OAUTH_TOKEN";
    private static final String OAUTH_TOKEN_SECRET_KEY = "OAUTH_TOKEN_SECRET";
    public static final int DEFAULT_HOUR_OF_DAY = 9;
    public static final int DEFAULT_DAY_OF_WEEK = 1;

    public static SharedPreferences getSharedPreferences(final Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isOauthSetup(final SharedPreferences preferences) {
        return preferences.getBoolean(OAUTH_READY_KEY, false);
    }
    
    public static String getOauthToken(final SharedPreferences preferences) {
        return preferences.getString(OAUTH_TOKEN_KEY,DEFAULT_STRING);
    }

    public static String getOauthTokenSecret(final SharedPreferences preferences) {
        return preferences.getString(OAUTH_TOKEN_SECRET_KEY, DEFAULT_STRING);
    }
    
    public static void setOAuthTokens(final SharedPreferences preferences, final String emailLogin, final String token, final String tokenSecret) {
        Map<String, ?> newValues = new HashMap<String, Object>() {{
            put(OAUTH_READY_KEY, Boolean.TRUE);
            put(OAUTH_TOKEN_KEY, token);
            put(OAUTH_TOKEN_SECRET_KEY, tokenSecret);
            put(EMAIL_LOGIN_KEY, emailLogin);
        }};
        setPreferences(preferences, newValues);
    }

    public static void flushOAuthTokens(final SharedPreferences preferences) {
        Map<String, ?> newValues = new HashMap<String, Object>() {{
            put(OAUTH_READY_KEY, Boolean.FALSE);
            put(OAUTH_TOKEN_KEY, DEFAULT_STRING);
            put(OAUTH_TOKEN_SECRET_KEY, DEFAULT_STRING);
        }};
        setPreferences(preferences, newValues);
    }
    
    public static String getEmailLogin(final SharedPreferences preferences) {
        return preferences.getString(EMAIL_LOGIN_KEY, "");
    }

    @Deprecated
    public static String getEmailPassword(final SharedPreferences preferences) {
        throw new UnsupportedOperationException("This is no longer a valid function");
    }

    public static boolean preferencesAreSet(final SharedPreferences preferences) {
        return (preferences.getInt(PREFERENCES_VERSION_KEY, 0) == PREFERENCES_VERSION);
    }

    public static boolean getStripUnsubscribe(final SharedPreferences preferences) {
        return preferences.getBoolean(STRIP_UNSUBSCRIBE_KEY, true);
    }

    public static boolean getStripName(final SharedPreferences preferences) {
        return preferences.getBoolean(STRIP_NAME_KEY, true);
    }

    public static boolean getDelayPublishFlag(final SharedPreferences preferences) {
        return preferences.getBoolean(DELAY_PUBLISH_FLAG_KEY, false);
    }

    public static int getDelayPublishDayOfWeek(final SharedPreferences preferences) {
        return preferences.getInt(DELAY_PUBLISH_DAY_OF_WEEK_KEY, DEFAULT_DAY_OF_WEEK);
    }

    public static int getDelayPublishHour(final SharedPreferences preferences) {
        return preferences.getInt(DELAY_PUBLISH_HOUR_KEY, DEFAULT_HOUR_OF_DAY);
    }

    public static String getEmailRecipients(final SharedPreferences preferences) {
        return preferences.getString(EMAIL_RECIPIENTS_KEY, DEFAULT_STRING);
    }

    public static boolean getHideSkipped(final SharedPreferences preferences) {
        return preferences.getBoolean(HIDE_SKIPPED_KEY, true);
    }

    public static void toggleHideSkipped(final SharedPreferences preferences) {
        setPreferences(preferences, new HashMap<String, Boolean>() {{
            put(HIDE_SKIPPED_KEY, !getHideSkipped(preferences));
        }});
    }

    public static boolean getHideSent(final SharedPreferences preferences) {
        return preferences.getBoolean(HIDE_SENT_KEY, false);
    }

    public static void toggleHideSent(final SharedPreferences preferences) {
        setPreferences(preferences, new HashMap<String, Boolean>() {{
            put(HIDE_SENT_KEY, !getHideSent(preferences));
        }});
    }

    public static boolean hasWPCategories(final SharedPreferences preferences) {
        return !getWPCategories(preferences).equals(DEFAULT_STRING);
    }

    public static String getWPCategories(final SharedPreferences preferences) {
        return preferences.getString(WP_CATEGORIES, DEFAULT_STRING);
    }

    public static boolean hasWPTags(final SharedPreferences preferences) {
        return !getWPTags(preferences).equals(DEFAULT_STRING);
    }

    public static String getWPTags(final SharedPreferences preferences) {
        return preferences.getString(WP_TAGS, DEFAULT_STRING);
    }

    public static Date getLastEmailCheckTime(final SharedPreferences preferences) {
        return new Date(preferences.getLong(EMAIL_CHECK_TIMESTAMP_KEY, 0));
    }

    public static void setLastEmailCheckTime(final SharedPreferences preferences, final Date time) {
        setPreferences(preferences, new HashMap<String, Long>() {{
            put(EMAIL_CHECK_TIMESTAMP_KEY, time.getTime());
        }});
    }

    public static void setPreferences(final SharedPreferences preferences,
                                      final String wpEmail,
                                      final String wpCategories,
                                      final String wpTags,
                                      final Boolean stripName,
                                      final Boolean stripUnsubscribe,
                                      final Boolean delayPublication,
                                      final Integer dayOfWeek,
                                      final Integer hourOfDay,
                                      final Boolean hideSkipped,
                                      final Boolean hideSent) {
        Map<String, ?> newPreferences = new HashMap<String, Object>() {
            {
                put(EMAIL_RECIPIENTS_KEY, wpEmail);
                put(WP_CATEGORIES, wpCategories);
                put(WP_TAGS, wpTags);
                put(STRIP_NAME_KEY, stripName);
                put(STRIP_UNSUBSCRIBE_KEY, stripUnsubscribe);
                put(DELAY_PUBLISH_FLAG_KEY, delayPublication);
                put(DELAY_PUBLISH_HOUR_KEY, hourOfDay);
                put(DELAY_PUBLISH_DAY_OF_WEEK_KEY, dayOfWeek);
                put(HIDE_SENT_KEY, hideSent);
                put(HIDE_SKIPPED_KEY, hideSkipped);
                put(PREFERENCES_VERSION_KEY, PREFERENCES_VERSION);
            }
        };
        setPreferences(preferences, newPreferences);
    }

    private static void setPreferences(final SharedPreferences preferences, Map<String, ?> newPreferences) {
        SharedPreferences.Editor editor = preferences.edit();
        for (Map.Entry<String, ?> entry : newPreferences.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Boolean) {
                editor.putBoolean(entry.getKey(), (Boolean) value);
            } else if (value instanceof String) {
                editor.putString(entry.getKey(), (String) value);
            } else if (value instanceof Long) {
                editor.putLong(entry.getKey(), (Long) value);
            } else if (value instanceof Integer) {
                editor.putInt(entry.getKey(), (Integer) value);
            }
        }
        editor.commit();
    }
}
