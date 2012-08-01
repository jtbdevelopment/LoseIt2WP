package com.jtbdevelopment.loseit2wp.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.jtbdevelopment.loseit2wp.R;
import com.jtbdevelopment.loseit2wp.data.preferences.LoseIt2WPPreferences;

import static android.provider.ContactsContract.CommonDataKinds.Email.*;
import static com.jtbdevelopment.loseit2wp.android.LoseIt2WP.LOG_TAG;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 7/28/12
 * Time: 12:24 PM
 */
public class SettingsView extends Activity {
    private static int CONTRACT_REQUEST_CODE = 12345;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingsview);

        final Spinner dayOfWeek = (Spinner) findViewById(R.id.dayOfWeek);
        final Spinner hourOfDay = (Spinner) findViewById(R.id.hours);
        final CheckBox hideSent = (CheckBox) findViewById(R.id.hideSent);
        final CheckBox hideSkipped = (CheckBox) findViewById(R.id.hideSkipped);
        final CheckBox delayPublication = (CheckBox) findViewById(R.id.delayPublication);
        final TextView tags = (TextView) findViewById(R.id.tags);
        final TextView categories = (TextView) findViewById(R.id.categories);
        final TextView wpEmail = (TextView) findViewById(R.id.wpEmail);
        final CheckBox stripName = (CheckBox) findViewById(R.id.stripName);
        final CheckBox stripUnsubscribe = (CheckBox) findViewById(R.id.stripUnsubscribe);

        final SharedPreferences preferences = LoseIt2WPPreferences.getSharedPreferences(this);
        if (LoseIt2WPPreferences.getDelayPublishFlag(preferences)) {
            delayPublication.setChecked(true);
            enableDelayPublicationFields(dayOfWeek, hourOfDay);
        } else {
            delayPublication.setChecked(false);
            disableDelayPublicationFields(dayOfWeek, hourOfDay);
        }
        delayPublication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
                    enableDelayPublicationFields(dayOfWeek, hourOfDay);
                } else {
                    disableDelayPublicationFields(dayOfWeek, hourOfDay);
                }
            }
        });
        dayOfWeek.setSelection(LoseIt2WPPreferences.getDelayPublishDayOfWeek(preferences));
        hourOfDay.setSelection(LoseIt2WPPreferences.getDelayPublishHour(preferences));
        hideSent.setChecked(LoseIt2WPPreferences.getHideSent(preferences));
        hideSkipped.setChecked(LoseIt2WPPreferences.getHideSkipped(preferences));
        tags.setText(LoseIt2WPPreferences.getWPTags(preferences));
        categories.setText(LoseIt2WPPreferences.getWPCategories(preferences));
        wpEmail.setText(LoseIt2WPPreferences.getEmailRecipients(preferences));
        stripName.setChecked(LoseIt2WPPreferences.getStripName(preferences));
        stripUnsubscribe.setChecked(LoseIt2WPPreferences.getStripUnsubscribe(preferences));


        Button browseContacts = (Button) findViewById(R.id.wpEmailBrowse);
        browseContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent contactPicker = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(contactPicker, CONTRACT_REQUEST_CODE);
            }
        });

        Button saveSettings = (Button) findViewById(R.id.saveSettings);
        saveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoseIt2WPPreferences.setPreferences(preferences,
                        wpEmail.getText().toString(),
                        categories.getText().toString(),
                        tags.getText().toString(),
                        stripName.isChecked(),
                        stripUnsubscribe.isChecked(),
                        delayPublication.isChecked(),
                        dayOfWeek.getSelectedItemPosition(),
                        hourOfDay.getSelectedItemPosition(),
                        hideSkipped.isChecked(),
                        hideSent.isChecked()
                );
                SettingsView.this.finish();
            }
        });
    }

    private void disableDelayPublicationFields(final Spinner dayOfWeek, final Spinner hourOfDay) {
        controlDelayPublicationFields(dayOfWeek, hourOfDay, false, View.INVISIBLE);
    }

    private void enableDelayPublicationFields(final Spinner dayOfWeek, final Spinner hourOfDay) {
        controlDelayPublicationFields(dayOfWeek, hourOfDay, true, View.VISIBLE);
    }

    private void controlDelayPublicationFields(Spinner dayOfWeek, Spinner hourOfDay, boolean enabled, int visible) {
        dayOfWeek.setEnabled(enabled);
        dayOfWeek.setVisibility(visible);
        hourOfDay.setEnabled(enabled);
        hourOfDay.setVisibility(visible);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == CONTRACT_REQUEST_CODE) {
            Cursor cursor = null;
            String email = "";
            try {
                Uri result = data.getData();
                Log.v(LOG_TAG, "Got a contact result: "
                        + result.toString());

                // get the contact id from the Uri
                String id = result.getLastPathSegment();

                // query for everything email
                cursor = getContentResolver().query(CONTENT_URI, null, CONTACT_ID + "=?", new String[]{id}, null);

                int emailIdx = cursor.getColumnIndex(DATA);

                // let's just get the first email
                if (cursor.moveToFirst()) {
                    email = cursor.getString(emailIdx);
                    Log.v(LOG_TAG, "Got email: " + email);
                } else {
                    Log.w(LOG_TAG, "No results");
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Failed to get email data", e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                EditText emailEntry = (EditText) findViewById(R.id.wpEmail);
                emailEntry.setText(email);
                if (email.length() == 0) {
                    Toast.makeText(this, "No email found for contact.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
