package com.qikoo.sportscamera.activity;

import com.qikoo.sportscamera.R;
import com.qikoo.sportscamera.fragment.MainActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class SplashActivity extends Activity {
    private final int SPLASH_DISPLAY_LENGHT = 3000; // 延迟3秒
    private SharedPreferences preferences;
    private Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash_screen);
        preferences = getSharedPreferences("phone", Context.MODE_PRIVATE);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (preferences.getBoolean("firststart", true)) {
                    editor = preferences.edit();
                    // 将登录标志位设置为false，下次登录时不再显示首次登录界面
                    editor.putBoolean("firststart", false);
                    editor.commit();
                    Intent intent = new Intent();
                    intent.setClass(SplashActivity.this,
                            GuideViewPagerActivity.class);
                    SplashActivity.this.startActivity(intent);
                    SplashActivity.this.finish();
                } else {
                    Intent intent = new Intent();
                    intent.setClass(SplashActivity.this, MainActivity.class);
                    SplashActivity.this.startActivity(intent);
                    SplashActivity.this.finish();

                }

            }
        }, SPLASH_DISPLAY_LENGHT);
    }

}
