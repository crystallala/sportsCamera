package com.qikoo.sportscamera.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.qikoo.sportscamera.R;
import com.qikoo.sportscamera.view.FragAdapter;

public class CameraConnectionFailedActivity extends BaseActivity {

    private boolean invalidatePassword;
    private boolean isSearchTimeOut;
    private TextView tvConnectionResult;
    private TextView tvConnectionSolution;
    private TextView tvTitle;
    private ImageView ivConectionFailed;
    private ImageView ivCameraSearching;
    private ImageButton btnBack, btnFAQ;
    private Button btnTryAgain;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.search_add_camera);
        setFullScreen(true);
        initView();
    }
    
    private void initView() {
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(getResources().getString(R.string.conection_failed));
        tvConnectionResult = (TextView) findViewById(R.id.tvConnectionState);
        tvConnectionResult.setText(getResources().getString(R.string.connect_failed_tip));
        
        ivCameraSearching = (ImageView) findViewById(R.id.ivCameraSearching);
        ivCameraSearching.setVisibility(View.GONE);
        
        btnTryAgain = (Button) findViewById(R.id.btnTryAgain);
        btnTryAgain.setVisibility(View.VISIBLE);
        btnTryAgain.setOnClickListener(new OnClickListener() {            
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CameraConnectionFailedActivity.this, CameraConnectionActivity.class);
                startActivity(intent);
            }
        });
        
        isSearchTimeOut = getIntent().getBooleanExtra("searchTimeout", false);
        ivConectionFailed = (ImageView) findViewById(R.id.ivConnectionFailed);
        ivConectionFailed.setVisibility(View.VISIBLE);
        if(isSearchTimeOut) {
            
        }
        
        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        btnFAQ = (ImageButton) findViewById(R.id.btnFAQ);
        btnFAQ.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

            }
        });
    } 
}
