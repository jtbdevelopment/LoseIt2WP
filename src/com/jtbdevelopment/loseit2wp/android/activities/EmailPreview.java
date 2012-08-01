package com.jtbdevelopment.loseit2wp.android.activities;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import com.jtbdevelopment.loseit2wp.R;
import com.jtbdevelopment.loseit2wp.android.LoseIt2WP;
import com.jtbdevelopment.loseit2wp.data.LoseItSummaryMessage;
import com.jtbdevelopment.loseit2wp.data.preferences.LoseIt2WPPreferences;
import com.jtbdevelopment.loseit2wp.data.transforms.ToWordpressTransform;
import com.jtbdevelopment.loseit2wp.mail.EmailSender;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 3/19/12
 * Time: 6:42 AM
 */
public class EmailPreview extends ActivityWithDataSource {
    private static final String SEND_IT = "Send It!";
    private static final String RESEND_IT = "Resend It!";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emailpreview);

        final SharedPreferences preferences = LoseIt2WPPreferences.getSharedPreferences(this);
        WebView webView = (WebView) findViewById(R.id.emaildisplay);
        Bundle bundle = getIntent().getExtras();
        final LoseItSummaryMessage summaryMessage = bundle.getParcelable("summaryMessage");
        webView.loadData(ToWordpressTransform.transformContent(preferences, summaryMessage), "text/html", null);
        Button sendEmail = (Button) findViewById(R.id.sendemail);
        sendEmail.setText(generateButtonText(summaryMessage));
        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //  TODO - this is inefficient to recompute, really would like in future to provide a way to accept updates from email view
                    EmailSender.sendMail(preferences, summaryMessage.getSubject(), ToWordpressTransform.transformContent(preferences, summaryMessage));
                } catch (Exception e) {
                    new AlertDialog.Builder(view.getContext()).setMessage("Failed to send!").create();
                    Log.e(LoseIt2WP.LOG_TAG, e.getMessage());
                    throw new RuntimeException(e);
                }
                dataSource.updateSummaryAsSent(summaryMessage);
                EmailPreview.this.finish();
            }
        });
        sendEmail.getBackground().setColorFilter(0xFF000000 + this.getResources().getInteger(R.integer.loseitorange), PorterDuff.Mode.MULTIPLY);
    }

    private String generateButtonText(final LoseItSummaryMessage summaryMessage) {
        if(summaryMessage.getSentToWP()) {
            return RESEND_IT + " (" + summaryMessage.getSentToWPTime().toLocaleString() + ")";
        } else {
            return SEND_IT;
        }
    }
}
