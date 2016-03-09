package com.qikoo.sportscamera.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.qikoo.sportscamera.R;
import com.qikoo.sportscamera.util.IconPageIndicator;
import com.qikoo.sportscamera.util.PageIndicator;
import com.qikoo.sportscamera.view.FragAdapter;



public class CameraConnectionActivity extends FragmentActivity    {

    private ImageButton btnBack, btnFAQ, btnClose;
    private AnimationDrawable mSearchAnimation;
    private TextView tvTitle, tvConnectionState;
    private ImageView ivCameraSearching;
    private ImageView ivConectionFailed;

    private Button btnTryAgain;
    private PageIndicator mIndicator;
    
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.search_add_camera);
        initView();
    }

    private void initView() {
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(getResources().getString(R.string.conection_camera));

        tvConnectionState = (TextView) findViewById(R.id.tvConnectionState);
        tvConnectionState.setText(getResources().getString(R.string.searching));

        ivCameraSearching = (ImageView) findViewById(R.id.ivCameraSearching);
        ivCameraSearching.setVisibility(View.VISIBLE);

        ivConectionFailed = (ImageView) findViewById(R.id.ivConnectionFailed);
        ivConectionFailed.setVisibility(View.GONE);

        btnTryAgain = (Button) findViewById(R.id.btnTryAgain);
        btnTryAgain.setVisibility(View.GONE);
        
        btnClose = (ImageButton) findViewById(R.id.btn_close);
        btnClose.setVisibility(View.GONE);

        mSearchAnimation = (AnimationDrawable) ivCameraSearching
                .getBackground();

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
                setUpViewPager();  
            }
        });
    }

    private void setUpViewPager() {
        //构造适配器
        List<Fragment> fragments=new ArrayList<Fragment>();
        fragments.add(new CameraSearchActivity.SearchStepOne());
        fragments.add(new CameraSearchActivity.SearchStepTwo());
        FragAdapter adapter = new FragAdapter(getSupportFragmentManager(), fragments);
        //设定适配器
        ViewPager vp = (ViewPager)findViewById(R.id.vpFaqPager);
        vp.setAdapter(adapter);
        btnClose.setVisibility(View.VISIBLE);
        mIndicator = (IconPageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(vp);
    }  

    protected void onStart() {
        super.onStart();
        if (mSearchAnimation != null && !mSearchAnimation.isRunning())
            mSearchAnimation.start();
    }

    protected void onStop() {
        super.onStop();
        if (mSearchAnimation != null && mSearchAnimation.isRunning())
            mSearchAnimation.stop();
        searchTimeOut();
    }

    public void searchTimeOut() {
        Intent intent = new Intent(this, CameraConnectionFailedActivity.class);
        intent.putExtra("searchTimeout", true);
        startActivity(intent);
        finish();
    }
}
