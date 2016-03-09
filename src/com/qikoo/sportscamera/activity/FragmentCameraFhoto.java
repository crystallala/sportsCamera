package com.qikoo.sportscamera.activity;

import java.net.InetSocketAddress;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.Unieye.smartphone.ApiConstant;
import com.Unieye.smartphone.ble.BleManager;
import com.Unieye.smartphone.exception.ResponseException;
import com.Unieye.smartphone.model.Decoder;
import com.Unieye.smartphone.model.Mp4FileCreator;
import com.Unieye.smartphone.pojo.BaseResponse;
import com.Unieye.smartphone.pojo.CameraInfo;
import com.Unieye.smartphone.pojo.GoCloudInfo;
import com.Unieye.smartphone.rtsp.RTSPClient;
import com.Unieye.smartphone.service.CameraService;
import com.Unieye.smartphone.util.DataUtil;
import com.Unieye.smartphone.util.Log;
import com.gt.common.http.ConnectionException;
import com.gt.common.http.InvalidNetworkException;
import com.qikoo.sportscamera.R;

public class FragmentCameraFhoto extends Fragment implements SurfaceHolder.Callback, IPreview{

    private SurfaceView mCamerSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private RTSPClient client = null;
    private int streamWidth = 848;
    private int streamHeight = 480;
    private int camViewHeight = 0;
    private int camViewWidth = 0;  
    private int screenWidth;
    private int screenHeight;
    static final int AUDIO_SAMPLE_RATE = 48000;
    static final int AUDIO_CHANNEL = 2;
    private String SAVEFILE_PATH;
    private CameraInfo mCamera;
    private CameraService mCameraService;
    private SmartphoneApplication mSmartphoneApplication;
    private int cntViewFreeze = 0;
    private static String TAG = "FragmentCameraFhoto Log";
    private SmartPhoneAsyncTask<Void, Void, BaseResponse> mStartStream;
    private SmartPhoneAsyncTask<Void, Integer, Void> mStartH264Stream;
    private boolean isStreaming;
    private Preview8ActivityPresenter presenter;
    
    private IPreview preview;
    private Activity activity;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.camera_photo_f, null);
        presenter = new Preview8ActivityPresenter(this, getActivity());
        initView(view);
        return view;
    }
    
    private void initView(View view) {
        mCamerSurfaceView = (SurfaceView)view.findViewById(R.id.PagePreview_CamerSurfaceView); 
        mCamerSurfaceView.setZOrderOnTop(true);
        mSurfaceHolder = mCamerSurfaceView.getHolder();
        
        Bitmap bitmap= Bitmap.createBitmap(300, 150 ,Bitmap.Config.RGB_565);
        mCamerSurfaceView.draw(new Canvas(bitmap));
        
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSmartphoneApplication = (SmartphoneApplication) getActivity().getApplication();
        mCameraService = mSmartphoneApplication.getCameraService();
        SAVEFILE_PATH = (new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().getPath())))
                .append("/DCIM/").append(this.getResources().getString(R.string.mediafolder_label)).toString();
        //startStreamingFlow();
    }
        
    @Override
    public void surfaceChanged(final SurfaceHolder holder, int format, int width, int height) {
        if (presenter.surfaceChanged(holder)) {
            mSurfaceHolder = holder;
            Log.e("ModaLog", "surface changed but ntilCodec not ready yet!");
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //Log.i("ModaLog", "surfaceCreated, orientation:" + rotate);
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        presenter.surfaceDestroyed();
    }
    
    void startStreamingFlow() {
        startStream();
        initialChiconyFFMPEG();
        startH264Stream();
    }

            
    private void startH264Stream() {
        mStartH264Stream = new SmartPhoneAsyncTask<Void, Integer, Void>(activity, false) {
            private boolean done = false;

            @Override
            protected Void doInBackground(Void params) throws ConnectionException, ResponseException,
                    InvalidNetworkException {
                System.currentTimeMillis();
                while (!done) {
                    System.currentTimeMillis();

                    cntViewFreeze++;
                    if (cntViewFreeze == 30 * 5)
                        publishProgress(cntViewFreeze);

                    try {
                        Thread.sleep(33);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                return null;
            }
            @Override
            protected void onProgressUpdate(Integer... values) {
                int cur = values[0];
                if (cur == 30 * 5) {
                    //preview.showProgressBar();
                }
                super.onProgressUpdate(values);
            }

            @Override
            public void quit() {
                done = true;
                Log.i(TAG, "VideoProcess quit()");
            }

            @Override
            protected void onCancelled() {
                done = true;
                Log.i("ModaLog_Video", "VideoProcess onCancelled");
                super.onCancelled();
            }

            @Override
            protected void onPostExecute(Object result) {

                super.onPostExecute(result);
            }
            
            @Override
            protected void doOnSuccess(Void result) {

            }

            @Override
            protected boolean handleException(Exception ex) {
                Log.i("ModaLog_Video", "handleException, ex: " + ex);
                return super.handleException(ex);
            }
        };
        mStartH264Stream.execute();
        }


    
    private void startStream() {
        mStartStream = new SmartPhoneAsyncTask<Void, Void, BaseResponse>(activity, false) {
            @Override
            protected BaseResponse doInBackground(Void params) throws ConnectionException, ResponseException,
                    InvalidNetworkException {
                try {
                    startStreaming(false, false, 0);
                } catch (ResponseException e) {
                    e.printStackTrace();
                } catch (ConnectionException e) {
                    e.printStackTrace();
                } catch (InvalidNetworkException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void doOnSuccess(BaseResponse result) {
                isStreaming = true;
            }
        };
        mStartStream.execute();
    }
    
    private void startStreaming(boolean rtpOverRtsp, boolean onlyRTP, int rtpPort) throws ResponseException,
            ConnectionException, InvalidNetworkException {
        
        int port = rtpPort;
        if(port==0)
            port = DataUtil.randomPort();
        
        String url = "rtsp://"+ApiConstant.SP_IP;
        client = new RTSPClient(
                new InetSocketAddress(ApiConstant.SP_IP, Integer.parseInt(ApiConstant.RTSP_PORT)),
                null, url, port, rtpOverRtsp, onlyRTP);
        
    }
    
    void initialChiconyFFMPEG() {
        try {
            mCamera = mSmartphoneApplication.getCamera().getCameraInfo();
            
            Decoder.getInstance().initialize(streamWidth, streamHeight, AUDIO_CHANNEL, AUDIO_SAMPLE_RATE);
            Mp4FileCreator.getInstance().saveFrameInitial(streamWidth, streamHeight);
            Decoder.getInstance().handleUILayerInfo(getActivity(), mCamera, 1080, 613, 0, 0, SAVEFILE_PATH);


            if (streamWidth == 1280 && streamHeight == 720) {
                Mp4FileCreator.getInstance().setStreamFps(10.0);
            } else {
                Mp4FileCreator.getInstance().setStreamFps(29.97);
            }


        } catch (OutOfMemoryError e) {
            System.gc();
        }
    }
    
    public void onResume() {
        super.onResume();
        presenter.onResume();
        presenter.setChangeTabReceive();
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



        }

