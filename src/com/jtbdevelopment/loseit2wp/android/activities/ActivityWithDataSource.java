package com.jtbdevelopment.loseit2wp.android.activities;

import android.app.Activity;
import android.os.Bundle;
import com.jtbdevelopment.loseit2wp.data.database.LoseIt2WPDataSource;

/**
 * Created by IntelliJ IDEA.
 * JTB Development
 * Date: 7/28/12
 * Time: 3:35 PM
 */
public abstract class ActivityWithDataSource extends Activity {
    protected LoseIt2WPDataSource dataSource;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataSource = new LoseIt2WPDataSource(this);
        dataSource.open();
    }

    @Override
    protected void onStart() {
        dataSource.open();
        super.onStart();
    }

    @Override
    protected void onRestart() {
        dataSource.open();
        super.onRestart();
    }

    @Override
    protected void onResume() {
        dataSource.open();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataSource.close();
        dataSource = null;
    }

}
