package com.qikoo.sportscamera.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

import com.Unieye.smartphone.ble.BleManager.BleState;
import com.Unieye.smartphone.ble.IBleListener;
import com.Unieye.smartphone.pojo.GoCloudInfo;
import com.Unieye.smartphone.util.Log;
import com.qikoo.sportscamera.R;

public class Preview8Activity extends Activity implements Runnable,IPreview, SurfaceHolder.Callback, IBleListener{

    private Preview8ActivityPresenter presenter;
    private Context mContext;
    private SurfaceView mCamerSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private int screenWidth;
    private int screenHeight;
    private int rotate = 0;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new Preview8ActivityPresenter(this, this);
        mContext = this;
        getScreenSizeandDensity();
        presenter.getMaxHeapSizePerApp4Device();

        findViews();
        initData();
        setListener();
    }

    private void findViews() {
        this.setContentView(R.layout.camera_photo_f);
        mCamerSurfaceView = (SurfaceView) findViewById(R.id.PagePreview_CamerSurfaceView);      
    }

    private void initData() {
        /* init data */
        mSurfaceHolder = mCamerSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        Log.d("ModaLog", "Preview8Activity initData screenWidth:" + screenWidth + ", screenHeight:" + screenHeight);                
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
    }
    
    private void setListener() {

        /* setListener */
        if (presenter.isICSHigher()) {
            //mCamerView.setVisibility(View.INVISIBLE);
            mCamerSurfaceView.setVisibility(View.VISIBLE);
            mCamerSurfaceView.setOnTouchListener(cameraSurfaceTouchListener);
        } else {
            //mCamerView.setVisibility(View.VISIBLE);
            mCamerSurfaceView.setVisibility(View.GONE);
            //mCamerView.setOnTouchListener(camerViewTouchListener);
        }


    }
 
    private final OnTouchListener cameraSurfaceTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            presenter.processTouchEventForSurfaceView(view, event, rotate);
            return true;
        }
    };

    
    void getScreenSizeandDensity() {
        // ===============================================================
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels; // Width pixels
        int height = metric.heightPixels; // Height Pixels
        float density = metric.density; // 0.75 / 1.0 / 1.5
        int densityDpi = metric.densityDpi; // 120 / 160 / 240
        // ================================================================
        Log.i("ModaLog", "Debuglog: getScreenSizeandDensity width=" + width + " height=" + height + " density="
                + density + " densityDpi=" + densityDpi);

    }
    
    @Override
    public void onBleUpdate(BleState arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub
        if (presenter.surfaceChanged(holder)) {
            mSurfaceHolder = holder;
            Log.e("ModaLog", "surface changed but ntilCodec not ready yet!");
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        presenter.surfaceDestroyed();
    }

    @Override
    public void showProgressBar() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void hideProgressBar() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int getProgressBarState() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean getBackKeyState() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void showSaveFileOkMsg(String path) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showSaveFileNgMsg(String path) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showMemoryFullMsg() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showNotEnoughStorageMsg() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showInRecordingMsg() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setUIStateForRecording() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void clearCameraView() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void backHome() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void leaveApp() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showConstantLoadProgressUI(int mshowTime) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showRecordVideo2PhoneTimeUi(boolean isPressVideoStop) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showTempAlertBlink(int count) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void postCameraView() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void checkCloudInfoIfError(GoCloudInfo result) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setUIStateForPowerType(String cameraBattery, int adapter) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTemperatureAlarm() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTemperatureNormal() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showTemperature() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void hideTemperature() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTemperatureC(String tmp) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTemperatureF(String tmp) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void isShowError(boolean flag) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setErrorLadscapeProcess() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int getNightLightProgress() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getVolumeProgress() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getVolumeProgressMax() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setUIforRecordVideo() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void restoreUIformRecordVideo() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setUIforClickRecordVideoBtn() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setUIforUnclickRecordVideoBtn() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void restoreOptionStateFromTwoWayAudio() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void restoreOptionStateFromTwoWayAudioError() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setOptionStateForTwoWayAudio() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setOptionStateForTwoWayAudioOff() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setOptionStateForCameraCapture() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setMuteButtonChecked() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setRecordVideoButtonChecked() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setRecordVideoButtonUnchecked() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void disableAllOption() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void enableAllOption() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setPlayBtnState(boolean lullaby) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showLoading() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Surface getSurface() {
        // TODO Auto-generated method stub
        return mSurfaceHolder.getSurface();
    }

    @Override
    public void hidePowerInfo() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showDateTimeInfo() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void hideDateTimeInfo() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setDateTimeInfo(String content) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isCheckedRecordVideoBtn() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCheckedPlayBtn() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setPlayBtnStop() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setPlayBtnPlay() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setRepeatBtnRandom() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setRepeatBtnAll() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setRepeatBtnOne() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showNotEnoughStorageFlow() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setSandTypeInUse() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setSandTypeNonUse() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setAdapterStateInUse() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setAdapterStateNonUse(String battery) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void disableOtherTab() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void changeTab() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void closeDialog() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void hideReordLight() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showRecordLayout() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void hideRecordTime() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int getRotate() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void updateBTOptionItemAdapter(int pos) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void uiSetImageBitmap(Bitmap bitmapShow, Matrix matrix) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setupBtOptionItemDialog() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showBtOptionItemDialog() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        
    }

}
