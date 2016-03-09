package com.qikoo.sportscamera.activity;

import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity {

    public BaseActivity() {
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setFullScreen(false);
        if (getActionBar() != null)
            getActionBar().hide();
    }

    protected void setFullScreen(boolean flag) {
        if (flag)
            getWindow().setFlags(1024, 1024);
        else
            getWindow().clearFlags(1024);
    }
}
