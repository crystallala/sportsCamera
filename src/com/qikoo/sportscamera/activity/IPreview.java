package com.qikoo.sportscamera.activity;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.Surface;

import com.Unieye.smartphone.pojo.GoCloudInfo;

public interface IPreview {
    public void showProgressBar();
    public void hideProgressBar();
    public int getProgressBarState();
    
    public boolean getBackKeyState();
    
    public void showSaveFileOkMsg(String path);
    public void showSaveFileNgMsg(String path);
    public void showMemoryFullMsg();
    public void showNotEnoughStorageMsg();
    
    public void showInRecordingMsg();
    
    public void setUIStateForRecording();
    
    public void clearCameraView();
    public void backHome();
    public void leaveApp();
    public void showConstantLoadProgressUI(int mshowTime) ;
    

    
    public void showRecordVideo2PhoneTimeUi(boolean isPressVideoStop);
    public void showTempAlertBlink(int count);
    
    public void postCameraView();
    
//  public void zoomFactor(Bitmap bitmapShow, TouchEventProcess touchEventProcess, float zFactor);
    
    public void checkCloudInfoIfError(GoCloudInfo result);
    
    public void setUIStateForPowerType(String cameraBattery, int adapter);
    
    public void setTemperatureAlarm();
    public void setTemperatureNormal();
    
    
    public void showTemperature();
    public void hideTemperature();
    
    public void setTemperatureC(String tmp);
    public void setTemperatureF(String tmp);
    
    public void isShowError(boolean flag);
    
    public void setErrorLadscapeProcess();  
    public int getNightLightProgress();
    public int getVolumeProgress();
    public int getVolumeProgressMax();
//  public void showMusicDialog(int lullabyVolume, int mLullabyMaxDegree);
    
    public void setUIforRecordVideo();
    public void restoreUIformRecordVideo();
    
    public void setUIforClickRecordVideoBtn();
    public void setUIforUnclickRecordVideoBtn();
    
    public void restoreOptionStateFromTwoWayAudio();
    public void restoreOptionStateFromTwoWayAudioError();
    public void setOptionStateForTwoWayAudio();
    public void setOptionStateForTwoWayAudioOff();
    public void setOptionStateForCameraCapture();
    
    public void setMuteButtonChecked();
    
    public void setRecordVideoButtonChecked();
    public void setRecordVideoButtonUnchecked();
    
    public void disableAllOption();
    public void enableAllOption();
    
//  public void setMusicAdapter(int songIndex);
    public void setPlayBtnState(boolean lullaby);
    
    public void showLoading();
    
    public Surface getSurface();
    
    public void hidePowerInfo();
    
    public void showDateTimeInfo();
    public void hideDateTimeInfo();
    public void setDateTimeInfo(String content);
    
    public boolean isCheckedRecordVideoBtn();
    public boolean isCheckedPlayBtn();
    
    public void setPlayBtnStop();
    public void setPlayBtnPlay();

    public void setRepeatBtnRandom();
    public void setRepeatBtnAll();
    public void setRepeatBtnOne();
    

    
    public void showNotEnoughStorageFlow();
    
    public void setSandTypeInUse();
    public void setSandTypeNonUse();
    
    public void setAdapterStateInUse();
    public void setAdapterStateNonUse(String battery);
    
    public void disableOtherTab();
    public void changeTab();
    public void closeDialog();
    
    public void  hideReordLight();
    public void showRecordLayout();
    public void hideRecordTime();
    
    public int getRotate();
    
    public void updateBTOptionItemAdapter(int pos);
    public void uiSetImageBitmap(Bitmap bitmapShow, Matrix matrix);
    public void setupBtOptionItemDialog();
    public void showBtOptionItemDialog();
}
