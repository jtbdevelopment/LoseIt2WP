package com.jtbdevelopment.loseit2wp.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 3/20/12
 * Time: 9:24 PM
 */
public class LoseItSummaryMessage implements Parcelable {
    private long _id;  // Default to -1 if we do not yet know if we have an ID or not yet in DB
    private final String subject;
    private final String htmlContent;   //  Presumption
    private final Date mailboxTime;
    private Date sentToWPTime;
    private Boolean sentToWP;
    private Boolean newSummary;
    private Boolean skipToWP;

    public LoseItSummaryMessage(final String subject, final String content, final Date mailboxTime) {
        this(-1, subject, content, mailboxTime, false, null, true, false);
    }

    public LoseItSummaryMessage(final long _id, final String subject, final String content, final Date mailboxTime, boolean sentToWP, final Date sentToWPTime, boolean newSummary, boolean skipToWP) {
        this._id = _id;
        this.subject = subject;
        this.htmlContent = content;
        this.mailboxTime = mailboxTime;
        this.sentToWP = sentToWP;
        if(sentToWPTime != null) {
            this.sentToWPTime = sentToWPTime;
        } else {
            this.sentToWPTime = new Date(0);
        }
        this.newSummary = newSummary;
        this.skipToWP = skipToWP;
    }

    public void setNewSummary(final Boolean newSummary) {
        this.newSummary = newSummary;
    }
    
    public Boolean getNewSummary() {
        return newSummary;
    }

    public void setSkipToWP(final Boolean skipToWP) {
        this.skipToWP = skipToWP;
    }

    public void setSentToWP(final Boolean sentToWP) {
        this.sentToWP = sentToWP;
        if(sentToWP) {
            this.sentToWPTime = new Date();
            this.skipToWP = false;
        } else {
            this.sentToWPTime = new Date(0);
        }
    }

    public Boolean getSentToWP() {
        return sentToWP;
    }

    public Boolean getSkipToWP() {
        return skipToWP;
    }

    public Date getSentToWPTime() {
        return sentToWPTime;
    }
    
    public String getSubject() {
        return subject;  
    }
    
    public String getHtmlContent()  {
        return htmlContent;
    }
    
    public Date getMailboxTime() {
        return mailboxTime;
    }
    
    public long getId() {
        return _id;
    }

    public void setId(long id) {
        this._id = id;
    }

    @Override
    public boolean equals(final Object o) {
        if(o instanceof LoseItSummaryMessage) {
            if(o == this) {
                return true;
            }
            LoseItSummaryMessage compareTo = (LoseItSummaryMessage) o;

            if(this._id != -1 && compareTo._id != -1) {
                return _id == compareTo._id;
            }
            
            if(!compareTo.mailboxTime.equals(mailboxTime)) {
                return false;
            }

            if(!compareTo.subject.equals(subject)) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (mailboxTime.hashCode()) * 32 + subject.hashCode();
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(final Parcel dest, int flags) {
        dest.writeLong(this._id);
        dest.writeString(this.subject);
        dest.writeString(this.htmlContent);
        dest.writeLong(this.mailboxTime.getTime());
        dest.writeLong(this.sentToWPTime.getTime());
        dest.writeBooleanArray(new boolean[] { this.sentToWP, this.newSummary, this.skipToWP });
    }

    @SuppressWarnings("unused")
    public static final Creator<LoseItSummaryMessage> CREATOR = new Creator<LoseItSummaryMessage>() {
        public LoseItSummaryMessage createFromParcel(final Parcel in) {
            long id = in.readLong();
            String subject = in.readString();
            String htmlContent = in.readString();
            Date mailboxTime = new Date(in.readLong());
            Date sentToWPTime = new Date(in.readLong());
            boolean[] flags = new boolean[3];
            in.readBooleanArray(flags);

            return new LoseItSummaryMessage(id, subject, htmlContent, mailboxTime, flags[0], sentToWPTime, flags[1], flags[2]);
        }

        public LoseItSummaryMessage[] newArray(int size) {
            return new LoseItSummaryMessage[size];
        }
    };

}
