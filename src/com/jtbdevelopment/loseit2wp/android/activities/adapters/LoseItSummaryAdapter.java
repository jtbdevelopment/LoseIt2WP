package com.jtbdevelopment.loseit2wp.android.activities.adapters;

import android.app.Activity;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.jtbdevelopment.loseit2wp.R;
import com.jtbdevelopment.loseit2wp.data.LoseItSummaryMessage;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 4/2/12
 * Time: 9:38 PM
 */
public class LoseItSummaryAdapter extends ArrayAdapter<LoseItSummaryMessage> {
    private final Activity context;
    private final List<LoseItSummaryMessage> summaryMessages;
    private final static float[] cmArray;

    static {
        cmArray = new float[]{
                0.8823F, 0, 0, 0, 0,
                0, 0.4824F, 0, 0, 0,
                0, 0, 0.1412F, 0, 0,
                0, 0, 0, 1, 0
        };
    }

    public LoseItSummaryAdapter(final Activity context, final List<LoseItSummaryMessage> summaryMessages) {
        super(context, R.layout.emaillistviewitem, android.R.id.text1, summaryMessages);
        this.context = context;
        this.summaryMessages = summaryMessages;
    }

    static class ViewHolder {
        public TextView txtTitle;
        public ImageView imgIcon;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            row = inflater.inflate(R.layout.emaillistviewitem, parent, false);

            holder = new ViewHolder();
            holder.imgIcon = (ImageView) row.findViewById(R.id.imgIcon);
            holder.imgIcon.setColorFilter(new ColorMatrixColorFilter(cmArray));
            holder.txtTitle = (TextView) row.findViewById(R.id.txtTitle);
            holder.txtTitle.setTextColor(0xFF000000 + context.getResources().getInteger(R.integer.loseitorange));

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        LoseItSummaryMessage summaryMessage = summaryMessages.get(position);
        holder.txtTitle.setText(summaryMessage.getSubject());
        if (summaryMessage.getSentToWP()) {
            holder.imgIcon.setImageResource(R.drawable.ic_menu_tick);
        } else if (summaryMessage.getSkipToWP()) {
            holder.imgIcon.setImageResource(R.drawable.ic_menu_stop);
        } else if (summaryMessage.getNewSummary()) {
            holder.imgIcon.setImageResource(R.drawable.ic_menu_flash);
        } else {
            holder.imgIcon.setImageResource(R.drawable.ic_menu_piechart);
        }

        return row;
    }
}

