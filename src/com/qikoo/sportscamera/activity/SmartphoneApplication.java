package com.qikoo.sportscamera.activity;

import android.app.Application;
import android.content.Context;

import com.Unieye.smartphone.pojo.Camera;
import com.Unieye.smartphone.pojo.CameraSetting;
import com.Unieye.smartphone.service.CameraService;
import com.Unieye.smartphone.service.impl.CameraServiceImpl;
import com.qikoo.sportscamera.R;

public class SmartphoneApplication extends Application {
    private static Camera camera = new Camera();
    private CameraService cameraService;
    private CameraSetting mCameraSetting;
    private String mode;    // "DIRECT" or "CLOUD"
    private static Context context;
    private boolean bMuteStatusOn=false;
    
    public Camera getCamera() {
        return this.camera;
    }
    
    public CameraService getCameraService() {
        if(cameraService == null){
            cameraService = new CameraServiceImpl(this);
        }
        cameraService.updateCameraInfo(camera.getCameraInfo());
        return cameraService;
    }
    
    public CameraSetting getCameraSetting() {
        return mCameraSetting;
    }
    
    public void setCameraSetting(CameraSetting mCameraSetting) {
        this.mCameraSetting = mCameraSetting;
    }
    
    public String getCurrentMode() {
        return mode;
    }
    
    public void setCurrentMode(String mode) {
        this.mode = mode;
    }

    public void setMuteStatus(boolean bMuteStatusOn) {
        this.bMuteStatusOn=bMuteStatusOn;
    }
    
    public boolean getMuteStatus() {
        return bMuteStatusOn;
    }
    
    public String getAppName() {
        //return AppName;
        return getString(R.string.app_name);
    }
}
