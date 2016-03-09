package com.qikoo.sportscamera.activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.Window;

import com.Unieye.smartphone.pojo.GoCloudInfo;
import com.qikoo.sportscamera.R;


public class CameraMainControlActivity extends FragmentActivity{

    private FragmentManager fm = getSupportFragmentManager();
    private FragmentTransaction fragmentTransaction = getSupportFragmentManager()
            .beginTransaction();

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.camera_main);
        fragmentTransaction.replace(R.id.fragmentmain, new CameraMainControlFragment());
        fragmentTransaction.commit();
    }


}
