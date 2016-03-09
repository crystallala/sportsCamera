package com.qikoo.sportscamera.activity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import com.Unieye.smartphone.ApiConstant;
import com.Unieye.smartphone.Constants;
import com.Unieye.smartphone.Constants.PAGE;
import com.Unieye.smartphone.Constants.RemoteControlAction;
import com.Unieye.smartphone.exception.ResponseException;
import com.Unieye.smartphone.model.Decoder;
import com.Unieye.smartphone.model.IDecoderListener;
import com.Unieye.smartphone.model.Mp4FileCreator;
import com.Unieye.smartphone.pojo.BaseResponse;
import com.Unieye.smartphone.pojo.CameraInfo;
import com.Unieye.smartphone.pojo.CameraSetting;
import com.Unieye.smartphone.pojo.CameraStatus;
import com.Unieye.smartphone.pojo.CarCamcorderGeneralSetting;
import com.Unieye.smartphone.pojo.GoCloudInfo;
import com.Unieye.smartphone.pojo.RemoteControlResponse;
import com.Unieye.smartphone.rtsp.RTSPClient;
import com.Unieye.smartphone.service.CameraService;
import com.Unieye.smartphone.util.CecString;
import com.Unieye.smartphone.util.DataUtil;
import com.Unieye.smartphone.util.Log;
import com.Unieye.smartphone.util.MemoryStatus;
import com.Unieye.smartphone.util.SystemInfo;
import com.Unieye.smartphone.util.TouchEventProcess;
import com.gt.common.http.ConnectionException;
import com.gt.common.http.InvalidNetworkException;
import com.qikoo.sportscamera.R;

public class Preview8ActivityPresenter implements  IPresenter, IDecoderListener,IBtOptionDialogAdapterPresenter{


    private static String TAG = "Preview8ActivityPresenter Log";
    private int mLullabyStart = 1;
    private int mLullabyEnd = 6;    
    private static final int minReserveStorageSpace = 100 * 1024 * 1024;// 100MB
    private static final int minForPhotoReserveStorageSpace = 20 * 1024 * 1024;// 20MB  
    
    private static final int SAMPLE_RATE_IN = 44100;
    private static final int SAMPLE_RATE_OUT = 12000;
    private static BigInteger b1 = new BigInteger("" + SAMPLE_RATE_IN);
    private static BigInteger b2 = new BigInteger("" + SAMPLE_RATE_OUT);
    private static BigInteger gcd = b1.gcd(b2);
    private static int SAMPLE_RATE_GCD = gcd.intValue();
    private static int SAMPLE_RATE_IN_DIV_GCD = SAMPLE_RATE_IN / SAMPLE_RATE_GCD;
    private static int SAMPLE_RATE_OUT_DIV_GCD = SAMPLE_RATE_OUT / SAMPLE_RATE_GCD;
    private static final int SAMPLE_INTERVAL = 3000; // milliseconds
    private static final int SAMPLE_SIZE = 2; // bytes per sample
    private static int BUF_SIZE_IN = SAMPLE_RATE_IN * SAMPLE_INTERVAL * SAMPLE_SIZE / 1000;
    private static int BUF_SIZE_OUT = SAMPLE_RATE_OUT * SAMPLE_INTERVAL * SAMPLE_SIZE / 1000;
    private int camViewHeight = 0;
    private int camViewWidth = 0;   
    private RTSPClient client = null;
    /* static */
    private static boolean bH264Path = true;
    private static boolean isShowingRecordingExit = false;
    private static boolean toCameraCloudNScanListActivityflag = false;  
    private static float zFactor = 1.0f;
    private static float centerX = 0.5f;
    private static float centerY = 0.5f;
    private boolean bRecord2MobileState;
    private String video2phonefilepath; 
    
    /* AsyncTask */
    private SmartPhoneAsyncTask<Void, Void, RemoteControlResponse> mCameraremote;
    private SmartPhoneAsyncTask<Void, Void, CameraStatus> mCameraStatusTask;
    private SmartPhoneAsyncTask<Void, Void, BaseResponse> mStartStream;
    private SmartPhoneAsyncTask<Void, Void, Void> mStartTcpAudioPost;
    private SmartPhoneAsyncTask<Void, Void, GoCloudInfo> mGetGoCloudStatusTask;// new++
    private SmartPhoneAsyncTask<Void, Integer, Void> mStartH264Stream;
    private SmartPhoneAsyncTask<Void, Void, BaseResponse> mSetCameraSetupTask;// new++
    /* flag */
    private boolean initCountFlag = false;
    private boolean bSavePhotoToMobileOK = false;
    private boolean isFirst;
    private boolean loadCameraStatu = true;
    private boolean startCamera;
    private boolean bSndLoadedCapture = false;
    private boolean bSndLoadedRecord = false;
    private boolean bSndLoaded2WayAudio = false;
    private boolean isStreaming;
    private boolean canChangeTabFlag = false;
    private boolean startRecord = false;
    private static boolean startAudio = false;
    private boolean isClickCamera;
    private boolean isClickVideo;
    private boolean isClickVideoStop;
    private boolean isPressVideo;
    private boolean isPressVideoStop;
    private boolean isPressAudio;
    private boolean isPressAudioStop;
    private boolean ischangeTab;
    private boolean ignoreCameraStatu;
    private boolean isStreamChangeflag = true;
    private boolean isDoingVolumeRemoteControl = false;
    private boolean isFirstEnterTempAlert = false;
//  private boolean motionevent = false;
//  private boolean audioevent = false;
//  private boolean temperatureevent = false;
    private boolean isFirstEnterRecord2PhoneTimer = false;
    /* count */
    private int tempalertcount = 0;
    private int isPressVideoCount = 0;
    private int isPressVideoStopCount = 0;
    private int isPressAudioCount = 0;
    private int isPressAudioStopCount = 0;
    private int cntGetCameraStatusFail = 0;
//  private String standType;
    /* Data */
    private int rotate = 0;//
    private String savedPhotoToMobileFile;  

    private boolean isICSorHigher = false;
    private int streamWidth = 848;
    private int streamHeight = 480;
    private static final int VBuf_MAXNUM = 3;
    private static final int VBuf_MAXSIZE = (848 * 480 * 3 + 54) * 1;
    private static byte[][] VBuf = new byte[VBuf_MAXNUM][VBuf_MAXSIZE]; 
    static int BmpPutIndex = 0;
    static int BmpPlayIndex = 0;
    private long heapSize;
    // --------------------------------------------------------------------------------
    private boolean isInitialFFMpegFlag;
    private String mCurrentMode;
    private final int FEEDBACK_PERIOD = 3; // sec
    private final int RESTART_RTSP_TIME = 20; // sec
    private int bitRate = 0; // kbps
    private long byteCount = 0;
    private long byteCountPrevious = 0;
    private int videoFrameReceivedRate = 0; // fps
    private int videoFrameUsefulRate = 0; // fps
    private int videoFrameDecodedRate = 0; // fps
    private int videoFrameRenderRate = 0; // fps
    private int videoDataIsZeroCount = 0;
    private boolean allowStartRestartCountFlag = false;
    private int videoFrameReceivedCount = 0;
    private int videoFrameUsefulCount = 0;
    private int videoFrameDecodedCount = 0;
    private int videoFrameRenderCount = 0;
    static private Bitmap bitmap = null;
    static private Bitmap bitmapClr = null;
    private Handler mDrawImgHandler = new Handler();
    private Runnable DrawImgRunnable;
    // 2014-01-10++
    final String WQVGA_STREAM = CecString.makeXmlElement("STREAM", "RTP_H264_WQVGA");
    final String WVGA_STREAM = CecString.makeXmlElement("STREAM", "RTP_H264_WVGA");
    private String SAVEFILE_AllPATH;
    private String SAVEFILE_PATH;
    private CameraSetting cameraSetting;
    private CameraInfo mCamera;
    private CarCamcorderGeneralSetting carCamGenSet;
    int nStreamingAudioChannels = 2;
    private int sndIdCapture;
    private int sndIdRecord;
    private int sndId2WayAudio;
    private CameraInfo mCamer;
    private String cameraBattery;
    private int adapter;
    private String rtcDate;
    private String rtcTime;
    private int rtcDisplayCount = 0;
    private int rtcTimeInt = 0;
    private Timer rtcDisplayTimer;
    private int recording;
    private int audio;
    private String cameraRecordingTime;
    private String cameraRemainingTime;
    private int songIndex = 0;
    private int repeatIndex = 0;
    private int userSongIndex = -1;
    private int userRepeatIndex = -1;
    private boolean userPlay = false;

    
    private int mAudioStatu = 0;
    private int screenWidth;
    private int screenHeight;
    /* Timer */
    private Timer cameraRecordTimer;
    private Timer cameraRecord2PhoneTimer;
    private Timer rtcCountTimer;
    private Timer timer;
    private Timer cameraStatusTimer;
    private Timer bitrateTimer;
    private Timer remainStroageSpaceTimer;
    private Timer mTimer;
    /* handler */
    private Handler handler;
    private Handler myHandlerReleaseJNI = new Handler();
    /* Resource */
    private IPreview preview;
    private Activity activity;

    private CameraService mCameraService;
    private SmartphoneApplication mSmartphoneApplication;
    private SoundPool soundPool = null;
    private MemoryStatus memorystatus;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock wakeLock;
    private TouchEventProcess touchEventProcess;
    private AudioRecord audio_recorder = null;  
    
    
    static final int AUDIO_SAMPLE_RATE = 48000;
    static final int AUDIO_CHANNEL = 2;
        
    ArrayList<String> mBtOptionList = new ArrayList<String>();
    private static final String[] mBtOptionStrings = {
        "Connect a device - Secure","Connect a device - Insecure","Make discoverable"
    };
    
    
    public Preview8ActivityPresenter(IPreview preview, Activity context) {
        this.preview = preview;
        this.activity = context;
        this.mSmartphoneApplication = (SmartphoneApplication) activity.getApplication();
        this.mCameraService = mSmartphoneApplication.getCameraService();
        this.touchEventProcess = new TouchEventProcess();
        SAVEFILE_PATH = (new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().getPath())))
                .append("/DCIM/").append(activity.getResources().getString(R.string.mediafolder_label)).toString();
        isICSorHigher = SystemInfo.isOSVerICSorHigher();
        isStreaming = false;
        bH264Path = true;

    }

    @Override
    public void onResume() {
        Decoder.getInstance().registerListener(this);
        Decoder.getInstance().onPausetakePhoto(false);
        Decoder.getInstance().takePhoto(false);     
        ischangeTab = false;
        Log.e(TAG, "Preview8Activity onResume Start isICSorHigher:" + isICSorHigher);
        // 2013-11-13 Bug ID:13236 issue++
        canChangeTabFlag = false;
        bSndLoadedCapture = false;
        bSndLoadedRecord = false;
        bSndLoaded2WayAudio = false;
        cntGetCameraStatusFail = 0;
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool sp, int sampleId, int status) {
                Log.d("SoundPool", "soundPool:" + sp + ", sampleId:" + sampleId + ", status:" + status);
                if (sampleId == sndIdCapture)
                    bSndLoadedCapture = true;
                else if (sampleId == sndIdRecord) {
                    bSndLoadedRecord = true;
                } else if (sampleId == sndId2WayAudio)
                    bSndLoaded2WayAudio = true;
            }
        });
        sndIdCapture = soundPool.load(activity, R.raw.capture, 1);
        sndIdRecord = soundPool.load(activity, R.raw.record, 1);
        sndId2WayAudio = soundPool.load(activity, R.raw.twowayaudio, 1);
        Log.d("SoundPool", "soundPool:" + soundPool + ", sndIdCapture:" + sndIdCapture + ", sndIdRecord:" + sndIdRecord
                + ", sndId2WayAudio:" + sndId2WayAudio);

        mCurrentMode = mSmartphoneApplication.getCurrentMode();
        mCamera = mSmartphoneApplication.getCamera().getCameraInfo();
        Log.i(TAG, "mCamera:" + mCamera);       

        Log.d(TAG,
                "memorystatus: getAvailableInternalMemorySize:" + memorystatus.getAvailableInternalMemorySize()
                        + " getTotalInternalMemorySize:" + memorystatus.getTotalInternalMemorySize());
        Log.d(TAG,
                "memorystatus: getAvailableExternalMemorySize:" + memorystatus.getAvailableExternalMemorySize()
                        + " getTotalExternalMemorySize:" + memorystatus.getTotalExternalMemorySize());
        // -----------------------------------------------------------------------------------------
        if ((bH264Path == true)) {
            if (!mSmartphoneApplication.getMuteStatus()) {

                Decoder.getInstance().initialAudioTrack();
            }
            isFirst = true;
            isInitialFFMpegFlag = false;


        } else
            isFirst = false;

        if (mCameraStatusTask == null)// Bug ID:0010555 issue
        {
            getGoCloudStatusSetting(mCamera.getIp());
        }

        mPowerManager = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        wakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, this.getClass().getCanonicalName());
        wakeLock.acquire();
        mCamer = mSmartphoneApplication.getCamera().getCameraInfo();
        timer = new Timer();
        setCameraView();
        startStreamingFlow();
        cameraStatusTimer = new Timer();
        setTimerTask(2, 1000);
        preview.showProgressBar();
        setTimerTask(4, 1000);
        loadCameraStatu = true;
        ignoreCameraStatu = false;
        isClickVideoStop = false;
        isClickCamera = false;
        isClickVideo = false;
        isPressVideo = false;
        isPressVideoStop = false;
        isPressAudio = false;
        isPressAudioStop = false;
        startCamera = false;

    }

    @Override
    public void onPause() {
        Decoder.getInstance().removeListener(this);
        Decoder.getInstance().onPausetakePhoto(true);
        Decoder.getInstance().takePhoto(true);
        Log.e(TAG, "Preview8Activity ==onPause Start== getRecord2MobileState:" + getRecord2MobileState()+" ,getSaveReceiveRtpState:"+Mp4FileCreator.getInstance().getSaveReceiveRtpState());

        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        // 2014-10-07 to close mp4 file used++
        checkIfRecording2CloseHandle();     
        if (bH264Path == true) {
            new Thread(new Runnable() {
                public void run() {
                    stopRTP();
                    client.closeRTSPSocket();
                
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Decoder.getInstance().audioTrackRelease();

                }
            }).start();

            Log.i(TAG, "RTSPClient isStreaming=" + isStreaming);

            if (mStartH264Stream != null) {
                mStartH264Stream.quit();
                mStartH264Stream.cancel(true);
                mStartH264Stream = null;

            }

        }
        if (ChangeTabReceive != null) {
            activity.unregisterReceiver(ChangeTabReceive);
        }
        if (bitrateTimer != null) {
            bitrateTimer.cancel();
            bitrateTimer = null;
        }

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        if (cameraStatusTimer != null) {
            cameraStatusTimer.cancel();
            cameraStatusTimer = null;
        }

        if (cameraRecordTimer != null) {
            cameraRecordTimer.cancel();
            cameraRecordTimer = null;
        }

        if (rtcCountTimer != null) {
            rtcCountTimer.cancel();
            rtcCountTimer = null;
        }

        if (mCameraStatusTask != null) {
            mCameraStatusTask.cancel(true);
            mCameraStatusTask = null;
        }
        

        if (mStartTcpAudioPost != null) {
            mStartTcpAudioPost.quit();
            mStartTcpAudioPost.cancel(true);
            mStartTcpAudioPost = null;
        }
        if (audio_recorder != null) {
            audio_recorder.release();
            audio_recorder = null;
        }

        releaseWakeLock();
        // 2013-10-30 Prevent album back to preview then exit app immediately++
        if (isInitialFFMpegFlag == true) {
            myHandlerReleaseJNI.postDelayed(mReleaseJNIRunnable, 0);
        }

    }
    public void setImageViewInfo(int camViewWidth, int camViewHeight)
    {
        this.camViewWidth = camViewWidth;
        this.camViewHeight = camViewHeight; 
        Log.d(TAG, "Preview8ActivityPresenter setImageViewInfo camViewWidth:" + camViewWidth + ", camViewHeight:" + camViewHeight);             
        Decoder.getInstance().handleUILayerInfo(activity, mCamera, camViewWidth, camViewHeight, screenWidth, screenHeight, SAVEFILE_PATH);
        
    }
    public void remoteControl(final RemoteControlAction action) {
        // Log.i(TAG, "debuglog:remoteControl(action,)"+action);
        remoteControl(action, "");
    }

    private void remoteControl(final RemoteControlAction action, final String parameter) {

        mCameraremote = new SmartPhoneAsyncTask<Void, Void, RemoteControlResponse>(activity, preview.getBackKeyState()) {
            @Override
            protected RemoteControlResponse doInBackground(Void params) throws ConnectionException, ResponseException,
                    InvalidNetworkException {
                
                RemoteControlResponse mRemoteControlResponse = mCameraService.remoteControl(action, parameter);
                if (mRemoteControlResponse.getResultStatus().equals(BaseResponse.STATUS_OK)
                        && action == RemoteControlAction.STREAM_CHANGE
                        && parameter.equals("<STREAM>HTTP_JPEG_WQVGA</STREAM>")) {
                    bH264Path = false;// for QuadView invert issue

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    bitmap = mCameraService.getCameraIcon2(mCamer.getIp(), mCamer.getHttpPort());
                    mCameraService.resetTcpStreamSocket();
                }

                if (mRemoteControlResponse.getResultStatus().equals(BaseResponse.STATUS_OK)
                        && action == RemoteControlAction.STREAM_CHANGE
                        && parameter.equals("<STREAM>HTTP_JPEG_WQVGA</STREAM>")) {
                    bH264Path = false;// for QuadView invert issue
                    bitmap = mCameraService.getCameraIcon2(mCamer.getIp(), mCamer.getHttpPort());
                    mCameraService.resetTcpStreamSocket();

                }

                if (mRemoteControlResponse.getResultStatus().equals(BaseResponse.STATUS_OK)
                        && action == RemoteControlAction.PHOTO_TO_MOBILE) {
                    bSavePhotoToMobileOK = false;
                    if (mRemoteControlResponse.getURI() != null) {

                        for (int i = 0; i < 5; i++) {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            savedPhotoToMobileFile = DownloadFromUrl("http://" + mCamer.getIp() + ":"
                                    + mCamer.getHttpPort() + mRemoteControlResponse.getURI());
                            if (savedPhotoToMobileFile != null) {
                                bSavePhotoToMobileOK = true;
                                break;
                            } else {
                                Log.e(TAG, "RemoteControl PHOTO_TO_MOBILE, download fail ! " + i);
                            }
                        }
                    } else {
                        Log.e(TAG, "RemoteControl PHOTO_TO_MOBILE, url empty !");
                    }
                }

                
                return mRemoteControlResponse;
            }

            @Override
            protected void getErrorCode(String errorCode) {         

                if (errorCode.equals("-7")) {
                    preview.isShowError(true);
                    // showError = true;
                    new Thread(new Runnable() {
                        public void run() {
                            stopRTP();
                            client.closeRTSPSocket();
                        }
                    }).start();
                }

                if ((action == RemoteControlAction.VIDEO_START || action == RemoteControlAction.PHOTO
                        || action == RemoteControlAction.PHOTO_TO_MOBILE || action == RemoteControlAction.LOOP_ON)
                        && (errorCode.equals("-5") || errorCode.equals("-6") || errorCode.equals("-8") || errorCode
                                .equals("-9"))) {
                    isClickCamera = false;
                    startCamera = false;
                    // -----------------------------------------------------------
                    isPressVideo = false;
                    isPressVideoStop = false;
                    isPressAudio = false;
                    isPressAudioStop = false;
                    isClickVideo = false;
                    ignoreCameraStatu = false;
                    preview.restoreUIformRecordVideo();

                    return;
                }

                // (-8)adapter applied
                if (action == RemoteControlAction.VIDEO_START && errorCode.equals("-8")) {

                    // mVideoStatu = 0;
                    isClickVideo = false;
                    preview.restoreUIformRecordVideo();
                    ignoreCameraStatu = false;
                    return;
                }

                preview.setErrorLadscapeProcess();

                startRecord = false;
                startCamera = false;
                isClickCamera = false;
                isClickVideo = false;
                isClickVideoStop = false;
                ignoreCameraStatu = false;

                ischangeTab = false;
                preview.restoreUIformRecordVideo();
                preview.closeDialog();
                super.getErrorCode(errorCode);
            }

            @Override
            protected boolean handleException(Exception ex) {
                Log.d(TAG, "Preview8Activity remoteControl , handleException:" + ex);
                if (action.equals(RemoteControlAction.LOGOUT)) {
                    if (toCameraCloudNScanListActivityflag == true) {

                        toCameraCloudNScanListActivityflag = false;
                        SharedPreferences settings = activity.getSharedPreferences(Constants.SETTING_INFO, 0);
                        settings.edit().putBoolean(Constants.FIRSTRUN, true).commit();
                        mCameraService.closeDatagramSocket();

                        Exit2CloseTaskandTimer();
                        preview.backHome();
                    } else {

                        SharedPreferences settings = activity.getSharedPreferences(Constants.SETTING_INFO, 0);
                        settings.edit().putBoolean(Constants.FIRSTRUN, true).commit();
                        mCameraService.closeDatagramSocket();

                        Exit2CloseTaskandTimer();
                        preview.leaveApp();
                    }
                } else if (ex instanceof ResponseException) {
                    String errorCode = null;
                    errorCode = ((ResponseException) ex).getErrorResponse().getErrorCode();

                    // notAllowOrientation = true;
                    if (errorCode.equals("-6")) {
                        preview.showMemoryFullMsg();
                        return false;
                    } else if (errorCode.equals("-13")) {
                        preview.showNotEnoughStorageMsg();
                        return false;
                    }

                }
                isDoingVolumeRemoteControl = false;
                return super.handleException(ex);
            }

            @Override
            protected void doOnSuccess(RemoteControlResponse result) {

                Log.d(TAG, "Preview8Activity remoteControl , doOnSuccess:" + result);

                if (result.getResultStatus().equals(BaseResponse.STATUS_OK)) {

                    if (action.equals(RemoteControlAction.AUDIO_ON)) {
                        startAudio = true;
                        ignoreCameraStatu = false;
                    }
                    if (action.equals(RemoteControlAction.AUDIO_OFF)) {

                        startAudio = false;
                        ignoreCameraStatu = false;
                        
                        if (bH264Path == true) {

                            Decoder.getInstance().initialAudioTrack();

                        }
                        
                    }

                    if (action.equals(RemoteControlAction.PHOTO)) {

                        ignoreCameraStatu = false;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startCamera = false;
                                isClickCamera = false;
                                // mPhoneMainActivity.enableTab(MAIN_CATEGORY_TYPE.VIEW);
                            }
                        }, 3500);
                    }

                    if (action.equals(RemoteControlAction.PHOTO_TO_MOBILE)) {

                        ignoreCameraStatu = false;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startCamera = false;
                                isClickCamera = false;
                                // mPhoneMainActivity.enableTab(MAIN_CATEGORY_TYPE.VIEW);
                            }
                        }, 300);
                        if (bSavePhotoToMobileOK) {
                            preview.showSaveFileOkMsg(SAVEFILE_PATH);
                            updateGallery(savedPhotoToMobileFile);
                            // 2014-01-03++
                            preview.showProgressBar();

                        } else
                            preview.showSaveFileNgMsg(SAVEFILE_PATH);
                    }

                    if (ischangeTab) {
                        new Thread(new Runnable() {
                            public void run() {
                                stopRTP();
                                // if(isStreaming)//After tear down must close
                                // socket!
                                client.closeRTSPSocket();
                                //mCameraService.closeRTSPSocket();
                            }
                        }).start();
                        startRecord = false;
                        
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                preview.closeDialog();
                                preview.changeTab();
                            }
                        }, 3000);
                    }

                    if (action.equals(RemoteControlAction.LOGOUT)) {

//                      mSmartphoneApplication.logout();
//                      mCameraService.resetHttpCount();

                        if (toCameraCloudNScanListActivityflag == true) {

                            toCameraCloudNScanListActivityflag = false;
                            SharedPreferences settings = activity.getSharedPreferences(Constants.SETTING_INFO, 0);
                            settings.edit().putBoolean(Constants.FIRSTRUN, true).commit();
                            mCameraService.closeDatagramSocket();
                            Exit2CloseTaskandTimer();
                            preview.backHome();

                        } else {
                            SharedPreferences settings = activity.getSharedPreferences(Constants.SETTING_INFO, 0);
                            settings.edit().putBoolean(Constants.FIRSTRUN, true).commit();
                            mCameraService.closeDatagramSocket();

                            Exit2CloseTaskandTimer();
                            preview.leaveApp();
                        }
                    }
                }
                isDoingVolumeRemoteControl = false;
            }
        };
        mCameraremote.execute();

    }

    private void callSetCameraSetup(final String strXML) {

        mSetCameraSetupTask = new SmartPhoneAsyncTask<Void, Void, BaseResponse>(activity, true) {

            @Override
            protected BaseResponse doInBackground(Void params) throws ConnectionException, ResponseException,
                    InvalidNetworkException {
                BaseResponse mBaseResponse = mCameraService.setCameraGeneralSetup(strXML);
                return mBaseResponse;
            }

            @Override
            protected void doOnSuccess(BaseResponse result) {

                if (result.getResultStatus().equals(BaseResponse.STATUS_OK)) {
                    // Log.i(TAG, "callSetCameraSetup OK");
                } else {
                    // Log.d(TAG, "callSetCameraSetup NG:" +
                    // result.getResultStatus());
                }
            }

            @Override
            protected boolean handleException(Exception ex) {
                // Log.i(TAG, "callSetCameraSetup handleException ex:"+
                // ex);
                return super.handleException(ex);
            }
        };
        mSetCameraSetupTask.execute();

    }

    public void toCameraCloudNScanListActivity() {
        Decoder.getInstance().onPausetakePhoto(true);
        Decoder.getInstance().takePhoto(true);
        new Thread(new Runnable() {
            public void run() {
//              setSavePhoto4Thumbnail();
                Decoder.getInstance().savePhoto4Thumbnail();
            }
        }).start();

        Log.d(TAG, "Preview8Activity homeKey LOGOUT!");
        RemoteControlAction mAction = RemoteControlAction.LOGOUT;
        remoteControl(mAction);
        toCameraCloudNScanListActivityflag = true;

    }

    public String DownloadFromUrl(String DownloadUrl) {

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
            Date curDate = new Date(System.currentTimeMillis());
            String str = formatter.format(curDate);
            Log.i(TAG, "PreviewCloudActivity Photo str=" + str);
            String fileName = str + ".jpg";
            File dir = new File(SAVEFILE_PATH);
            if (dir.exists() == false) {
                dir.mkdirs();
            }

            URL url = new URL(DownloadUrl); // you can write here any link
            File file = new File(dir, fileName);

            long startTime = System.currentTimeMillis();
            Log.d("DownloadFromUrl", "download begining");
            Log.d("DownloadFromUrl", "download url:" + url);
            Log.d("DownloadFromUrl", "downloaded file name:" + fileName);

            /* Open a connection to that URL. */
            URLConnection ucon = url.openConnection();

            /*
             * Define InputStreams to read from the URLConnection.
             */
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            /*
             * Read bytes to the Buffer until there is nothing more to read(-1).
             */
            ByteArrayBuffer baf = new ByteArrayBuffer(5000);
            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }

            /* Convert the Bytes read to a String. */
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baf.toByteArray());
            fos.flush();
            fos.close();
            Log.d("DownloadFromUrl", "download ready in " + (System.currentTimeMillis() - startTime) + " ms");
            return file.getPath();
        } catch (IOException e) {
            Log.d("DownloadFromUrl", "download Error: " + e);
            return null;
        }

    }

    public void updateGallery(String file) {
        File f = new File(file);
        Uri contentUri = Uri.fromFile(f);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
        activity.sendBroadcast(mediaScanIntent);
    }

    void setSaveVideo2Filepath() {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        Log.i(TAG, "Preview8Activity str=" + str);

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            SAVEFILE_AllPATH = SAVEFILE_PATH;
            Log.i(TAG, "PreviewQuadviewActivity path=" + SAVEFILE_AllPATH);

            File dirFile = new File(SAVEFILE_AllPATH);

            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
        }

        String root_sd = SAVEFILE_AllPATH + "/" + str + ".mp4";
        Mp4FileCreator.getInstance().setupArchiveDir(root_sd);
        setVideo2PhoneFilePathName(root_sd);
        if (bSndLoadedRecord) {
            soundPool.play(sndIdRecord, 1f, 1f, 1, 0, 1f);
            Log.e("soundPool", "Played record sound");
        }

    }

    // 2013-10-18 for RTC Parser/Transfer++
    public static String timeFormatTransfer(/* long ms */long unitsec) {
        // int ss = 1000;
        int ss = 1;
        int mi = ss * 60;
        int hh = mi * 60;
        int dd = hh * 24;

        long day = unitsec / dd;
        long hour = (unitsec - day * dd) / hh;
        long minute = (unitsec - day * dd - hour * hh) / mi;
        long second = (unitsec - day * dd - hour * hh - minute * mi) / ss;
        
        String strHour = hour < 10 ? "0" + hour : "" + hour;
        String strMinute = minute < 10 ? "0" + minute : "" + minute;
        String strSecond = second < 10 ? "0" + second : "" + second;
        return strHour + ":" + strMinute + ":" + strSecond;
    }

    public static String timeFormatTransferNoHour(/* long ms */long unitsec) {
        // int ss = 1000;
        int ss = 1;
        int mi = ss * 60;
        int hh = mi * 60;
        int dd = hh * 24;

        // long day = ms / dd;
        long day = unitsec / dd;
        long hour = (unitsec - day * dd) / hh;
        long minute = (unitsec - day * dd - hour * hh) / mi;
        long second = (unitsec - day * dd - hour * hh - minute * mi) / ss;
        String strHour = hour < 10 ? "0" + hour : "" + hour;
        String strMinute = minute < 10 ? "0" + minute : "" + minute;
        String strSecond = second < 10 ? "0" + second : "" + second;

        if (hour == 0)
            return strMinute + ":" + strSecond;
        else
            return strHour + ":" + strMinute + ":" + strSecond;
    }

    public static int parseRtcTime2Int(String rtcTimestr) {
        Log.i(TAG, "Debuglog: parseRtcTime2Int rtcTimestr=" + rtcTimestr);
        int sum_misec;
        int htime, mtime, stime;
        int mtime_index, stime_index;

        mtime_index = rtcTimestr.indexOf(":", 0);
        mtime_index += 1;
        stime_index = rtcTimestr.indexOf(":", mtime_index);
        stime_index += 1;

        htime = Integer.parseInt(rtcTimestr.substring(0, mtime_index - 1));
        mtime = Integer.parseInt(rtcTimestr.substring(mtime_index, stime_index - 1));
        stime = Integer.parseInt(rtcTimestr.substring(stime_index, stime_index + 2));

        sum_misec = htime * 60 * 60 + mtime * 60 + stime;
        return sum_misec;
    }

    private Runnable mMyCanChangeTabRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "RTSP canChangeTabFlag!!!");
            canChangeTabFlag = true;            
        }
    };

    private Runnable mReleaseJNIRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Debuglog: onPause bitmap:" + bitmap + " bitmapClr:" + bitmapClr + " VBuf:" + VBuf);
            // 2013-10-30 for HD Mode Out Of Memory++
            // 2013-11-01 Prevent on resume back then Canvas: trying to use a
            // recycled bitmap
            preview.clearCameraView();
            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }

            if (bitmapClr != null) {
                bitmapClr.recycle();
                bitmapClr = null;
            }

            Decoder.getInstance().release();
            Decoder.getInstance().releaseStreamingBuffer(true);

            if (mReleaseJNIRunnable != null) {
                myHandlerReleaseJNI.removeCallbacks(mReleaseJNIRunnable);
            }

        }
    };

    public void getMaxHeapSizePerApp4Device() {
        heapSize = Runtime.getRuntime().maxMemory();
        Log.d(TAG, "handleStreamingBitmapArray: determining Max Heap Size per App for a device :" + heapSize);

    }

    void initialChiconyFFMPEG() {
        try {           

            Decoder.getInstance().initialize(streamWidth, streamHeight, AUDIO_CHANNEL, AUDIO_SAMPLE_RATE);
            Mp4FileCreator.getInstance().saveFrameInitial(streamWidth, streamHeight);
            Decoder.getInstance().handleUILayerInfo(activity, mCamera, camViewWidth, camViewHeight, screenWidth, screenHeight, SAVEFILE_PATH);


            if (streamWidth == 1280 && streamHeight == 720) {
                Mp4FileCreator.getInstance().setStreamFps(10.0);
            } else {
                Mp4FileCreator.getInstance().setStreamFps(29.97);
            }
            Log.i(TAG, "Debug SaveVideo initialChiconyFFMPEG streamWidth=" + streamWidth + ", streamHeight="
                    + streamHeight);

            if (isICSorHigher) {
                if (preview.getSurface().isValid())
                    Decoder.getInstance().nativeSetSurface(preview.getSurface(), preview.getRotate());
                else
                    Log.e(TAG, "initialChiconyFFMPEG, surface is NOT Valid !");
            }

        } catch (OutOfMemoryError e) {
            Log.i(TAG, "initialChiconyFFMPEG, OOM!");
            System.gc();
        }
    }

    void checkCloudIfReStartRTSP(long byteCountTemp) {

        if (mCurrentMode.equals("DIRECT") || mCurrentMode.equals("CLOUD")) {
            Log.i(TAG, "checkCloudIfReStartRTSP byteCountPrevious:" + byteCountPrevious + " byteCountTemp: "
                    + byteCountTemp + " cntGetCameraStatusFail:" + cntGetCameraStatusFail);

            if (byteCountTemp != 0)
                cntGetCameraStatusFail = 0;

            if (byteCountTemp == 0 && byteCountPrevious != 0) {
                allowStartRestartCountFlag = true;
            }

            if (byteCountTemp == 0 && allowStartRestartCountFlag) {
                videoDataIsZeroCount++;
            } else {
                videoDataIsZeroCount = 0;
            }

            Log.i(TAG, "checkCloudIfReStartRTSP allowStartRestartCountFlag:" + allowStartRestartCountFlag
                    + " videoDataIsZeroCount: " + videoDataIsZeroCount + " byteCountTemp:" + byteCountTemp);

            if (videoDataIsZeroCount * FEEDBACK_PERIOD >= RESTART_RTSP_TIME) {
                videoDataIsZeroCount = 0;
                allowStartRestartCountFlag = false;

                if (bitrateTimer != null) {
                    bitrateTimer.cancel();
                    bitrateTimer = null;
                }

                if (bH264Path == true) {
                    new Thread(new Runnable() {
                        public void run() {
                            stopRTP();
                            Log.i(TAG, "checkCloudIfReStartRTSP RTSPClient isStreaming=" + isStreaming);
                            client.closeRTSPSocket();                       
                        }
                    }).start();

                }

                if (mStartH264Stream != null) {

                    mStartH264Stream.quit();
                    mStartH264Stream.cancel(true);
                    mStartH264Stream = null;

                }
                // sean add to release runnable
                if (DrawImgRunnable != null) {
                    mDrawImgHandler.removeCallbacks(DrawImgRunnable);
                    DrawImgRunnable = null;
                }
                preview.showProgressBar();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // 2013-12-17 stop mythread2,because bmpArray would create new
                // one,old content would become null!!
                isFirst = true;

            }

            byteCountPrevious = byteCountTemp;
        }
    }

    String temperC2F(String degreeC) {
        return String.valueOf(temperC2F(Integer.valueOf(degreeC)));
    }

    String temperF2C(String degreeF) {
        return String.valueOf((Integer.valueOf(degreeF) - 32) * 5 / 9);
    }

    int temperC2F(int degreeC) {
        return degreeC * 9 / 5 + 32;
    }

    int temperF2C(int degreeF) {
        return (degreeF - 32) * 5 / 9;
    }

    /**
     * 
     * @param sid
     */
    private void setTimerTaskforMusicList(final int sid) {
        if (sid == 1) {
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            }, 0, 1000);
        } else if (sid == 5) {
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 5;
                    handler.sendMessage(message);
                }
            }, 0, 1000);
        } else if (sid == 21) {// Lullaby Count Timer

            
        } else if (sid == 22) {// seekbar timer

            
        }
    }

    void startStreamingFlow() {
        Log.d(TAG, "startStreamingFlow isFirst:"+isFirst+" ,isStreamChangeflag:"+isStreamChangeflag+" ,bH264Path:"+bH264Path);
        if ((isFirst && isStreamChangeflag == true) && (bH264Path == true)) {
            isFirst = false;
            startStream();
            initialChiconyFFMPEG();
            startH264Stream();
            setTimerTask(99, FEEDBACK_PERIOD * 1000);
            isInitialFFMpegFlag = true;
            Handler myHandler2 = new Handler();
            myHandler2.postDelayed(mMyCanChangeTabRunnable, 5000);
        }

    }

    private int getCpuBogoMIPSInfo() {
        String str = null;
        String contentlen_str = null;
        int str_index = 0;
        int str_index_End = 0;
        int Cont_len_num = 0;
        StringBuffer sb = new StringBuffer();
        sb.append("abi: ").append(Build.CPU_ABI).append("\n");
        if (new File("/proc/cpuinfo").exists()) {

            try {
                BufferedReader br = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
                String aLine;
                while ((aLine = br.readLine()) != null) {
                    sb.append(aLine + "\n");
                }

                if (br != null) {
                    br.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        // -------------------------------------------------------
        str = sb.toString();

        try {
            str_index = str.indexOf("BogoMIPS");
            str_index_End = str.indexOf(".", str_index);

            if (str_index_End == -1)
                str_index_End = str.indexOf("\n", str_index);

        } catch (NullPointerException e) {
            e.printStackTrace();
            return 0;
        }

        try {
            // BogoMIPS\t: Offset10
            contentlen_str = str.substring((str_index + 10), str_index_End);

        } catch (IndexOutOfBoundsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return 0;
        }

        contentlen_str = contentlen_str.trim();

        try {
            // String to Int
            Cont_len_num = Integer.parseInt(contentlen_str);

        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return 0;
        }
        return Cont_len_num;

    }

    private void initCameraView() {

        if (initCountFlag) {// before 10s

            preview.setUIStateForPowerType(cameraBattery, adapter);

        } else {
            preview.hidePowerInfo();
            preview.hideTemperature();
        }

    }

    public void setTimerTask(final int messId, int time) {
        if (messId == 2) {

            cameraStatusTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = messId;
                    handler.sendMessage(message);
                }
            }, 0, time);
        } else if (messId == 15) {// Video 2 Phone Timer

            cameraRecord2PhoneTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = messId;
                    handler.sendMessage(message);
                }
            }, 0, time);
        } else if (messId == 19) {// Temp alert timer

            
        } else if (messId == 20) {// seekbar timer

            
        } else if (messId == 23) {// DateTime timer

            rtcDisplayTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = messId;
                    handler.sendMessage(message);
                }
            }, 0, time);
        } else if (messId == 99) {// send bitrate as XMPP RAW DATA in P2P mode
            if (bitrateTimer != null) {
                bitrateTimer.cancel();
                bitrateTimer = null;
            }
            bitrateTimer = new Timer();
            bitrateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = messId;
                    handler.sendMessage(message);
                }
            }, 0, time);
        } else if (messId == 100) {// Check remain stroge space Timer

            remainStroageSpaceTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = messId;
                    handler.sendMessage(message);
                }
            }, 0, time);
        } else if (messId == 115) {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 115;
                    handler.sendMessage(message);
                }
            }, 0);
        }

    }

    private void setCameraView() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int msgId = msg.what;
                switch (msgId) {
                case 2:
                    Log.i(TAG, "setCameraView.handleMessage messId is 2, ignoreCameraStatu"
                            + (ignoreCameraStatu ? " True" : " False"));
                    if (!ignoreCameraStatu) {
                        // 2013-11-28 Fixed timer would continuous
                        // increase,system would crash issue++
                        if (loadCameraStatu) {
                            mCameraStatusTask = new GetCameraStatus(activity);
                            mCameraStatusTask.execute();
//                          if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
//                              mCameraStatusTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                          } else {
//                              mCameraStatusTask.execute();
//                          }
                            
                        }
                    }
                    break;
                case 15:// Record Video to phone Timer
                    preview.showRecordVideo2PhoneTimeUi(isPressVideoStop);
                    break;

                case 19:
//                  showTempAlertBlinkUi();
                    break;

                case 20:

                    break;
                case 21:// lullaby Timer



                    break;
                case 22:

                    break;
                case 23:// DateTime Timer

                    if (rtcDisplayCount < 10) {
                        if (!preview.isCheckedRecordVideoBtn()) {
//                          dateTimeDisplay.setVisibility(View.VISIBLE);
                            preview.showDateTimeInfo();
                        }
//                      dateTimeDisplay.setText(rtcDate + " " + timeFormatTransfer(rtcTimeInt));
                        preview.setDateTimeInfo(rtcDate + " " + timeFormatTransfer(rtcTimeInt));
                        rtcTimeInt++;
                        rtcDisplayCount++;
                    } else {
                        initCountFlag = true;
                        preview.hideDateTimeInfo();
                        if (rtcDisplayTimer != null) {
                            rtcDisplayTimer.cancel();
                        }
                    }

                    break;

                case 99:// bitrate to camera
                    bitRate = (int) (byteCount * 8 / FEEDBACK_PERIOD / 1024);
                    videoFrameReceivedRate = (int) ((float) videoFrameReceivedCount / (float) FEEDBACK_PERIOD + 0.5);
                    videoFrameUsefulRate = (int) ((float) videoFrameUsefulCount / (float) FEEDBACK_PERIOD + 0.5);
                    videoFrameDecodedRate = (int) ((float) videoFrameDecodedCount / (float) FEEDBACK_PERIOD + 0.5);
                    videoFrameRenderRate = (int) ((float) videoFrameRenderCount / (float) FEEDBACK_PERIOD + 0.5);

                    Log.i(TAG, "bitrate: " + bitRate + "kbps" + "; fpsRcv: " + videoFrameReceivedRate
                            + "; fpsUse: " + videoFrameUsefulRate
                            // +"; fpsDec: "+videoFrameDecodedRate
                            // +"; fpsBmp: "+videoFrameBitmapRate
                            + "; fpsDec+Bmp: " + videoFrameDecodedRate + "; fpsRdr: " + videoFrameRenderRate);

                    checkCloudIfReStartRTSP(byteCount);
                    byteCount = 0;
                    videoFrameReceivedCount = 0;
                    videoFrameUsefulCount = 0;
                    videoFrameDecodedCount = 0;
                    videoFrameRenderCount = 0;

                    break;
                case 100:// check remain storage space
                    // 2013-11-07 check remain storage space whether<100MB ++
                    if ((memorystatus.getAvailableExternalMemorySize() < minReserveStorageSpace)
                            && getRecord2MobileState()) {
                        preview.showNotEnoughStorageFlow();
                    }

                    Log.i(TAG,
                            "memorystatus: getAvailableExternalMemorySize:"
                                    + memorystatus.getAvailableExternalMemorySize() + " getRecord2MobileState():"+ getRecord2MobileState());
                    break;
                case 115:
                    preview.showLoading();
                    break;
                default:
                    break;
                }
            }
        };
    }

    private void startStream() {
        mStartStream = new SmartPhoneAsyncTask<Void, Void, BaseResponse>(activity, false) {
            @Override
            protected BaseResponse doInBackground(Void params) throws ConnectionException, ResponseException,
                    InvalidNetworkException {
                try {
                    startStreaming(false, false, 0);
                    Log.i(TAG, "Debuglog: startStream");
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
//      if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
//          mStartStream.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//      } else {
//          mStartStream.execute();
//      }
    }
    
    private void startStreaming(boolean rtpOverRtsp, boolean onlyRTP, int rtpPort) throws ResponseException,
            ConnectionException, InvalidNetworkException {
        
        Log.i(TAG, "startStreaming, rtpOverRtsp:"+rtpOverRtsp+", onlyRTP:"+onlyRTP+", rtpPort:"+rtpPort);
        int port = rtpPort;
        if(port==0)
            port = DataUtil.randomPort();
        
        String url = "rtsp://"+ApiConstant.SP_IP;
//        String url = "rtsp://"+"192.168.1.1";
        client = new RTSPClient(
                new InetSocketAddress(ApiConstant.SP_IP, Integer.parseInt(ApiConstant.RTSP_PORT)),
                null, url, port, rtpOverRtsp, onlyRTP);
        
    }
    protected class GetCameraStatus extends SmartPhoneAsyncTask<Void, Void, CameraStatus> {

        public GetCameraStatus(Activity context) {
            super(context, false);
            Log.i(TAG, "CameraStatus constructor, loadCameraStatu:" + (loadCameraStatu ? " True" : " False"));
            if (!loadCameraStatu) {
                cancel(true);
            }
        }

        @Override
        protected CameraStatus doInBackground(Void params) throws ConnectionException, ResponseException,
                InvalidNetworkException {
            loadCameraStatu = false;
            CameraStatus mCameraStatus = mSmartphoneApplication.getCamera().getCameraStatus();
            mCameraStatus = mCameraService.getCameraStatus(PAGE.VIEW);
            // 2013-04-24 for Bootup mjpeg frame will 0/180 degree roate
            // immediately issue
            if (mCameraStatus.getResultStatus().equals(BaseResponse.STATUS_OK)) {
                
            }

            return mCameraStatus;

        }

        @Override
        protected void getErrorCode(String errorCode) {
            Log.i(TAG, "CameraStatus.getErrorCode");
            if (errorCode.equals("-2")) {
                // showError = true;
                preview.isShowError(true);
                new Thread(new Runnable() {
                    public void run() {
                        stopRTP();
                        // if(isStreaming)//After tear down must close socket!
                        client.closeRTSPSocket();
                        //mCameraService.closeRTSPSocket();
                    }
                }).start();
            }
            preview.setErrorLadscapeProcess();          
            super.getErrorCode(errorCode);
        }

        @Override
        protected boolean handleException(Exception ex) {
            Log.i(TAG, "CameraStatus.handleException, ex: " + ex);
            if (ex instanceof ConnectionException) {
                ConnectionException ce = (ConnectionException) ex;
                Log.i(TAG, "CameraStatus.handleException, ce status code: " + ce.getStatusCode());
                // if(ce.getStatusCode()==404 && cntGetCameraStatusFail<3) {
                if (cntGetCameraStatusFail < 10) {
                    loadCameraStatu = true;
                    cntGetCameraStatusFail++;
                    return false;
                } else { // Bug:0010251 issue
                    
                    // 2014-10-07 to close mp4 file used++
                    checkIfRecording2CloseHandle();
                    
                
                }
            } else if (ex instanceof ResponseException) {
                loadCameraStatu = true;
                // ------------------------------------------------------------------
                String errorCode = null;
                ResponseException responseException = (ResponseException) ex;
                errorCode = responseException.getErrorResponse().getErrorCode();
                if (errorCode.equals("-14")) {
                    Log.e(TAG, "Debuglog: ==GetCameraStatus ResponseException -14");

                    return false;
                } else if (errorCode.equals("-10")) {
                    ignoreCameraStatu = true;
                    if (isShowingRecordingExit == false)
                        preview.showInRecordingMsg();

                    isShowingRecordingExit = true;
                    return false;
                }
                // //------------------------------------------------------------------
            } else
                Log.i(TAG, "CameraStatus.handleException, else");

            ignoreCameraStatu = false;
            return super.handleException(ex);

        }

        @Override
        protected void onFinishHandle() {
            Log.i(TAG, "CameraStatus.onFinishHandle");
            loadCameraStatu = true;
            super.onFinishHandle();
        }

        @Override
        protected void doOnSuccess(CameraStatus result) {

            cntGetCameraStatusFail = 0;
            if (result != null) {
                result.getRecordingTime();
                // cameraRSSI = result.getRssi();
                cameraBattery = result.getBattery();
                recording = Integer.parseInt(result.getRecording());
                cameraRecordingTime = result.getRecordingTime();
                
                if(cameraRecordingTime != null)
                    Integer.parseInt(cameraRecordingTime);
                
                cameraRemainingTime = result.getRemainingTime();
                
                if(cameraRemainingTime != null)
                 Integer.parseInt(cameraRemainingTime);
                if(result.getAudio() != null)
                    audio = Integer.parseInt(result.getAudio());
                if(result.getInverter() != null)
                    Integer.parseInt(result.getInverter());             
                if(result.getSd() != null)
                    Integer.parseInt(result.getSd());
                
                adapter = Integer.parseInt((result.getAdaptor() == null) ? "0" : result.getAdaptor());
                Integer.parseInt((result.getGPS() == null) ? "0" : result.getGPS());
                rtcDate = result.getRTCDate();
                rtcTime = result.getRTCTime();

                if (rtcDate != null && rtcTime != null) {
                    if (!initCountFlag) {
                        if (rtcDisplayTimer == null) {// rtcDisplay
                            rtcTimeInt = parseRtcTime2Int(rtcTime);
                            rtcDisplayTimer = new Timer();
                            setTimerTask(23, 1000);
                        }

                    } else {
                        preview.hideDateTimeInfo();
                    }
                }

                if (rtcDate == null)
                    rtcDate = "";
                if (initCountFlag) {
                    if (recording == 0 && ! getRecord2MobileState()) {
                        // hide RecordingTime
                        preview.hideRecordTime();
                    }
                }
                initCameraView();

                if (isPressVideo && recording == 0) {

                    isPressVideoCount++;
                    if (isPressVideoCount > 6) {
                        isPressVideoCount = 0;
                        isPressVideo = false;
                    } else
                        return;

                } else if (isPressVideoStop && recording == 1) {

                    isPressVideoStopCount++;
                    if (isPressVideoStopCount > 6) {
                        isPressVideoStopCount = 0;
                        isPressVideoStop = false;
                    } else
                        return;
                }
                isPressVideo = false;
                isPressVideoStop = false;

                Log.i(TAG, "Debuglog: audio=" + audio + ", isPressAudio=" + isPressAudio + ", isPressAudioStop= "
                        + isPressAudioStop);
                
                if (audio == 0 && isPressAudio == true) {
                    isPressAudioCount++;
                    if (isPressAudioCount > 6) {
                        isPressAudioCount = 0;
                        isPressAudio = false;
                    } else
                        return;
                } else if (audio == 1 && isPressAudioStop == true) {
                    isPressAudioStopCount++;
                    if (isPressAudioStopCount > 6) {
                        isPressAudioStopCount = 0;
                        isPressAudioStop = false;
                    } else
                        return;
                }
                isPressAudio = false;
                isPressAudioStop = false;

                if (audio == 1 && mStartTcpAudioPost == null) {
                    mAudioStatu = 1;
                    startTcpAudioPost();
                    preview.setOptionStateForTwoWayAudio();
                    return;
                } else if (audio == 1 && mStartTcpAudioPost != null) {
                    mAudioStatu = 1;
                    preview.setOptionStateForTwoWayAudio();
                    
                    Decoder.getInstance().audioTrackRelease();
                    return;
                } else if (audio == 0) {
                    mAudioStatu = 0;
                    startAudio = false;
                    preview.restoreOptionStateFromTwoWayAudioError();
                    // -----------------------------------------------
                }

                if (recording == 0 && !ignoreCameraStatu) {
                    isClickVideo = false;
                    isClickVideoStop = false;

                    if (cameraRecordTimer != null) {
                        cameraRecordTimer.cancel();
                        cameraRecordTimer = null;

                    }

                    preview.hideReordLight();
                    if (! getRecord2MobileState())
                        preview.showRecordLayout();

                    if (startRecord) {

                        startRecord = false;
                    }

                    preview.enableAllOption();
                    if (mSmartphoneApplication.getMuteStatus()) {

                        preview.setMuteButtonChecked();
                        
                        Decoder.getInstance().audioTrackRelease();
                    }
                } else if (recording == 0 && ignoreCameraStatu) {
                    if (cameraRecordTimer != null) {
                        cameraRecordTimer.cancel();
                        cameraRecordTimer = null;

                    }

                } else if (recording == 1) {
                    if (!startRecord)
                        startRecord = true;
                    if (cameraRecordTimer == null) {
                        cameraRecordTimer = new Timer();
                        setTimerTask(8, 1000);
                    }
                    preview.setUIforRecordVideo();
                    preview.disableOtherTab();
                }
                // 2014-03-10 Bug ID:0013850++
                if (startCamera == false) {
//                  if (standType.equals("NoStand")) {
//                      preview.setSandTypeNonUse();
//                  } else {
//                      preview.setSandTypeInUse();
//
//
//                  }
                }

            }

        }

    }

    private void startTcpAudioPost() {
        Log.d(TAG, "startTcpAudioPost start");

        mStartTcpAudioPost = new SmartPhoneAsyncTask<Void, Void, Void>(activity, false) {
            private boolean done = false;
            private int bytes_read = 0;

            @Override
            protected Void doInBackground(Void params) throws ConnectionException, ResponseException,
                    InvalidNetworkException {

                if (audio_recorder == null) {
                    Log.d(TAG, "startTcpAudioPost audio_recorder is null, new AudioRecord");
                    audio_recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE_IN,
                            AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, BUF_SIZE_IN);
                }

                byte[] bufIn = new byte[BUF_SIZE_IN];
                byte[] bufOut = new byte[BUF_SIZE_OUT];
                long nSampleAvg = 0;
                int AudioGain = 100;
                int AudioTargetUpper = 10000;
                int AudioTargetLower = 5000;
                int AudioMaxGain = 400;
                int AudioMinGain = 250;
                int AudioGainIncRate = 1;
                int AudioGainDecRate = 20;

                int iconCnt = 0;
                final long t0 = System.currentTimeMillis();
                Log.i(TAG, "startTcpAudioPost state:" + audio_recorder.getState());
                if (audio_recorder.getState() != AudioRecord.STATE_INITIALIZED)
                    return null;

                Log.i(TAG, "startTcpAudioPost go startRecording");
                audio_recorder.startRecording();
                Short sample0, newSample;
                Log.i(TAG, "startTcpAudioPost BUF_SIZE_IN:" + BUF_SIZE_IN + ", BUF_SIZE_OUT:" + BUF_SIZE_OUT
                        +
                        // ", BUF_SIZE_LCM:"+BUF_SIZE_LCM+
                        ", SAMPLE_RATE_OUT_DIV_GCD:" + SAMPLE_RATE_OUT_DIV_GCD + ", SAMPLE_RATE_IN_DIV_GCD:"
                        + SAMPLE_RATE_IN_DIV_GCD);
                while (!done) {
                    Log.i(TAG, "startTcpAudioPost, iconCnt:" + iconCnt);
                    bytes_read = audio_recorder.read(bufIn, 0, BUF_SIZE_IN);

                    int iXSampleRateIn, idx;
                    for (int i = 0; i < BUF_SIZE_OUT / 2; i++) {
                        iXSampleRateIn = i * SAMPLE_RATE_IN;
                        idx = iXSampleRateIn / SAMPLE_RATE_OUT;
                        // sean add
                        sample0 = (short) (bufIn[idx * 2 + 1] << 8 | (bufIn[idx * 2] & 0xFF));

                        nSampleAvg += Math.abs(sample0 * AudioGain / 100);
                        if ((i + 1) % (12) == 0) {
                            nSampleAvg /= (12);
                            if (nSampleAvg < AudioTargetLower) {
                                if (AudioGain < AudioMaxGain)
                                    AudioGain += AudioGainIncRate;
                            } else if (nSampleAvg > AudioTargetUpper) {
                                if (AudioGain > AudioMinGain)
                                    AudioGain -= AudioGainDecRate;
                            }

                            nSampleAvg = 0;
                        }

                        newSample = (short) (sample0 * AudioGain / 100);
                        bufOut[i * 2] = (byte) (newSample & 0xFF);
                        bufOut[i * 2 + 1] = (byte) ((newSample >> 8) & 0xFF);

                    }

                    Log.i("ModaLog", "startTcpAudioPost read, bytes_read:" + bytes_read);
                    String sessionKey = mSmartphoneApplication.getCamera().getCameraInfo().getSessionKey();
                    BaseResponse mBaseResponse = mCameraService.postMobileAudio(bufOut, sessionKey);
                    Log.i("ModaLog",
                            "startTcpAudioPost postMobileAudio time: " + Long.toString(System.currentTimeMillis() - t0));
                    iconCnt++;
                }
                Log.i(TAG, "startTcpAudioPost end loop");
                audio_recorder.stop();
                // audio_recorder.release();
                // audio_recorder = null;
                mCameraService.resetTcpAudioPostSocket();
                return null;
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
            }

            @Override
            public void quit() {
                done = true;
            }

            @Override
            protected void onCancelled() {
                done = true;

                super.onCancelled();
            }

            @Override
            protected void onPostExecute(Object result) {
                super.onPostExecute(result);
            }

            @Override
            protected void doOnSuccess(Void result) {
            }
        };
        mStartTcpAudioPost.execute();
//      if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
//          mStartTcpAudioPost.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//      } else {
//          mStartTcpAudioPost.execute();
//      }
    }

    private void releaseWakeLock() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    private void stopRTP() {
        try {
            client.stopStreaming();
            isStreaming = false;
        } catch (ResponseException e) {
            e.printStackTrace();
        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (InvalidNetworkException e) {
            e.printStackTrace();
        }
    }

    private int cntViewFreeze = 0;
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
                    if (cntViewFreeze % 30 == 1)
                        Log.i(TAG, "cntViewFreeze:" + cntViewFreeze);

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
                    preview.showProgressBar();
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
//      if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
//          mStartH264Stream.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//      } else {
//          mStartH264Stream.execute();
//      }
        // }//End Else
    }

    private void Exit2CloseTaskandTimer() {
        Log.i(TAG, "Debuglog: Exit2CloseTaskandTimer");
        // 2013-04-10 for downkey leave app don't on pause------------
        if (bH264Path == true) {
            // itemCamerView.setDatagramPacket(null);
            new Thread(new Runnable() {
                public void run() {
                    stopRTP();
                    Log.i(TAG, "RTSPClient isStreaming=" + isStreaming);
                    client.closeRTSPSocket();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    Decoder.getInstance().audioTrackRelease();
                }
            }).start();

            if (mStartH264Stream != null) {
                mStartH264Stream.quit();
                mStartH264Stream.cancel(true);
                mStartH264Stream = null;
            }

        }

        if (mStartTcpAudioPost != null) {
            mStartTcpAudioPost.quit();
            mStartTcpAudioPost.cancel(true);
            mStartTcpAudioPost = null;
        }

        if (cameraStatusTimer != null) {
            cameraStatusTimer.cancel();
            cameraStatusTimer = null;
        }

        if (mCameraStatusTask != null) {
            mCameraStatusTask.cancel(true);
            mCameraStatusTask = null;
        }

    }

    private void getGoCloudStatusSetting(final String ip) {

        mGetGoCloudStatusTask = new SmartPhoneAsyncTask<Void, Void, GoCloudInfo>(activity, true) {
            @Override
            protected GoCloudInfo doInBackground(Void params) throws ConnectionException, ResponseException,
                    InvalidNetworkException {
                GoCloudInfo goCloudStatusInfo = mCameraService.getGoCloudInfo();
                cameraSetting = mCameraService.getCameraSetting();
                //
                // 2014-02-11++
                if (cameraSetting != null) {
//                  mSmartphoneApplication.setLullabyMaxVolume(cameraSetting.getlullaby_max_volume());                  
                    Log.i(TAG,"Preview8Activity getLullabyMaxVolume:" + cameraSetting.getlullaby_max_volume());
                }
                // 2013-04-16 Get setupSetting move back to setupMenu
                // End===============================================
                Log.i(TAG, "Preview8Activity getGoCloudStatusSetting:" + goCloudStatusInfo);
                Log.i(TAG, "Preview8Activity carCamGenSet:" + carCamGenSet);
                Log.i(TAG, "Preview8Activity cameraSetting:" + cameraSetting);

                return goCloudStatusInfo;
            }

            @Override
            protected boolean handleException(Exception ex) {
                Log.i(TAG, "Preview8Activity handleException ex:" + ex);
                // ------------------------------------------------------------------
                String errorCode = null;
                ResponseException responseException = (ResponseException) ex;
                errorCode = responseException.getErrorResponse().getErrorCode();
                if (errorCode.equals("-10")) {

                    return false;
                }
                // //------------------------------------------------------------------
                return super.handleException(ex);
            }

            @Override
            protected void onFinishHandle() {
                super.onFinishHandle();
            }

            @Override
            protected void doOnSuccess(GoCloudInfo result) {
                if (result != null) {
                    preview.checkCloudInfoIfError(result);
                }
            }
        };
        mGetGoCloudStatusTask.execute();

    }

    public void setChangeTabReceive() {
        IntentFilter intentFilter1 = new IntentFilter(Constants.ChangeTabBroadcast);
        activity.registerReceiver(ChangeTabReceive, intentFilter1);
    }


    private BroadcastReceiver ChangeTabReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (!canChangeTabFlag)
                return;

            if (isClickVideo && !startRecord) {
                Log.i(TAG, "ChangeTabReceive isClickVideo:" + isClickVideo + " startRecord:" + startRecord);

            } else if (startAudio) {
                Log.i(TAG, "ChangeTabReceive startAudio: " + startAudio);
                ;
            } else if (startRecord) {
                Log.i(TAG, "ChangeTabReceive startRecord: " + startRecord);
                ;
            } else if (startCamera) {
                Log.i(TAG, "ChangeTabReceive startCamera: " + startCamera);
                ischangeTab = true;
                // Bug ID:0011100
                // mPhoneMainActivity.showDialog();
            } else if (isClickCamera == true || isClickVideoStop == true) {
                Log.i(TAG, "ChangeTabReceive isClickCamera: " + isClickCamera + " isClickVideoStop: "
                        + isClickVideoStop);
                ;
            } else if (getRecord2MobileState()) {
                Log.i(TAG,"ChangeTabReceive Record2MobileState:" + getRecord2MobileState());
                ;
            } else {
                // Bug ID:0011359 issue
                if (!(preview.getRotate() == 1 || preview.getRotate() == 3)) {
                    Log.i(TAG, "ChangeTabReceive changeTab()");
                    preview.changeTab();
                }
            }

        }
    };

    
    public void clickPlayBtn(){
        Log.i(TAG, "mLullabyPlayBtn isChecked=" + preview.isCheckedPlayBtn());

        if (mCameraremote != null) {
            mCameraremote.cancel(true);
        }
        if (preview.isCheckedPlayBtn()) {

            {
                userPlay = true;
                RemoteControlAction mAction = RemoteControlAction.LULLABY;
                remoteControl(mAction, CecString.makeXmlElement("Setting", "ON"));
                preview.setPlayBtnStop();
            }
        } else {
            userPlay = false;
            RemoteControlAction mAction = RemoteControlAction.LULLABY;
            remoteControl(mAction, CecString.makeXmlElement("Setting", "OFF"));
            preview.setPlayBtnPlay();
        }

    }
    
    public void processTouchEventForImageView(View view, MotionEvent event, int rotate) {

        touchEventProcess.process(view.getWidth(), view.getHeight(), event, rotate);
        
        zFactor = touchEventProcess.getScaleRate();
        centerX = touchEventProcess.getNewCenterPoint().x / view.getWidth();
        centerY = touchEventProcess.getNewCenterPoint().y / view.getHeight();
        Decoder.getInstance().setZoomInfo(zFactor, centerX, centerY);
    }

    /**
     * processTouchEventForSurfaceView
     * 
     * @param view
     * @param event
     * @param rotate
     */
    public void processTouchEventForSurfaceView(View view, MotionEvent event, int rotate) {

        touchEventProcess.process(view.getWidth(), view.getHeight(), event, rotate);
            zFactor = touchEventProcess.getScaleRate();
            centerX = touchEventProcess.getNewCenterPoint().x / view.getWidth();
            centerY = touchEventProcess.getNewCenterPoint().y / view.getHeight();
            Decoder.getInstance().setZoom(zFactor, centerX, centerY);

        Log.d("dh", "centerX:" + centerX + ",centerY:" + centerY + ",zFactor:" + zFactor);
    }
    
    public boolean isICSHigher() {
        return isICSorHigher;
    }
    
    

    /**
     * clickCameraButton
     */
    public void clickCameraButton() {
        if (mCameraremote != null) {
            mCameraremote.cancel(true);
        }

        // 2013-11-07 for min reserve storage space for photo 20MB++
        if (memorystatus.getAvailableExternalMemorySize() < minForPhotoReserveStorageSpace) {
            preview.showNotEnoughStorageMsg();
            return;
        }

        // 2013-12-26--
        isClickCamera = true;
        startCamera = true;
        preview.setOptionStateForCameraCapture();
        
        RemoteControlAction mAction = RemoteControlAction.PHOTO_TO_MOBILE;
        remoteControl(mAction);

        if (bSndLoadedCapture) {
            soundPool.play(sndIdCapture, 1f, 1f, 1, 0, 1f);
            Log.e("soundPool", "Played capture sound");
        }
    }
    

    
    public void setMute(boolean isEnable) {
        
        if (isEnable) {

            Decoder.getInstance().audioTrackRelease();

            mSmartphoneApplication.setMuteStatus(true);
        } else {

            Decoder.getInstance().initialAudioTrack();
            mSmartphoneApplication.setMuteStatus(false);

        }
    }
    
    public void recordVideo(boolean isEnable) {
        // 2013-11-07 for min reserve storage space 100MB++
        if (memorystatus.getAvailableExternalMemorySize() < minReserveStorageSpace
                && ! getRecord2MobileState()) {
            if (isEnable){

                preview.setRecordVideoButtonUnchecked();
            }
            preview.showNotEnoughStorageMsg();

            return;
        }

        if (isEnable) {
            // 2013-10-22 Need close RTC timer first++
            if (rtcCountTimer != null) {
                rtcCountTimer.cancel();
                rtcCountTimer = null;
            }

            if (cameraRecord2PhoneTimer == null) {

                cameraRecord2PhoneTimer = new Timer();
                isFirstEnterRecord2PhoneTimer = true;
                setTimerTask(15, 1000);
                // 2013-11-07 check remain storage space whether<100MB
                remainStroageSpaceTimer = new Timer();
                setTimerTask(100, 5000);

            }
            setSaveVideo2Filepath();
            setRecord2MobileState(true);
            Mp4FileCreator.getInstance().startSaveFrame();
            
            preview.setUIforClickRecordVideoBtn();
        } else {
            if (bSndLoadedRecord) {
                soundPool.play(sndIdRecord, 1f, 1f, 1, 0, 1f);
                Log.e("soundPool", "Played record sound");
            }
            // 2013-11-07 check remain storage space whether<100MB ++
            if (remainStroageSpaceTimer != null) {
                remainStroageSpaceTimer.cancel();
                remainStroageSpaceTimer = null;
            }

            if (cameraRecord2PhoneTimer != null) {
                cameraRecord2PhoneTimer.cancel();
                cameraRecord2PhoneTimer = null;
            }

            setRecord2MobileState(false);
            Mp4FileCreator.getInstance().stopSaveFrame();
            
            preview.setUIforUnclickRecordVideoBtn();
            preview.showSaveFileOkMsg(SAVEFILE_PATH);

        }

        Log.i(TAG, "mRecordSaveVideoButton isChecked=" + isEnable
                + " setRecord2MobileState=" + getRecord2MobileState());
    }
    
    public String getAppName(){
        return mSmartphoneApplication.getAppName();
        
    }
    
    public void saveThumbNail(){
        
        Decoder.getInstance().onPausetakePhoto(true);
        Decoder.getInstance().takePhoto(true);
        new Thread(new Runnable() {
            public void run() {
                Decoder.getInstance().savePhoto4Thumbnail();
            }
        }).start();
        
    }
    
    public void processInRecording(){
        isShowingRecordingExit = false;
        ignoreCameraStatu = false;
        toCameraCloudNScanListActivity();
    }

    public boolean surfaceChanged(final SurfaceHolder holder) {
        
        new Thread(new Runnable() {
            public void run() {

                Log.i(TAG, "surfaceChanged, orientation:" + rotate/* + ", mCecFFMPEG:" + mCecFFMPEG*/);
                Decoder.getInstance().nativeSetSurface(holder.getSurface(), rotate);
            }
        }).start();
        return false;
    }
    
    public void surfaceDestroyed() {
        new Thread(new Runnable() {
            public void run() {
                Log.i(TAG, "surfaceDestroyed, orientation:" + rotate/* + ", mCecFFMPEG:" + mCecFFMPEG*/);
                Decoder.getInstance().nativeSetSurface(null, rotate);
            }
        }).start();
    }
    
    
    public void setRotate(int rotate) {
        this.rotate = rotate;
        Decoder.getInstance().setRotate(rotate);
    }
    
    public boolean isLockBackBtn(){
        return isClickVideo && !startRecord;
        
    }
    
    public void setErrorFlag(){
        
        isClickCamera = false;
        startCamera = false;
        isPressVideo = false;
        isPressVideoStop = false;
        isPressAudio = false;
        isPressAudioStop = false;       
        isClickVideo = false;
        ignoreCameraStatu = false;  
    }
    
    public boolean getfirstRecordTimerFlag(){
        return isFirstEnterRecord2PhoneTimer;
    }
    
    public void setfirstRecordTimerFlag(boolean flag){
        this.isFirstEnterRecord2PhoneTimer = flag;
    }
    
    public String getCameraName(){
    
        return mCamera.getName();
    }
    
    
    private void setVideo2PhoneFilePathName(String video2phonefilepath)
    {
        this.video2phonefilepath =  video2phonefilepath;
    }
    private String getVideo2PhoneFilePathName()
    {
        return video2phonefilepath;
    }
    private void setRecord2MobileState(boolean bRecord2MobileState)
    {
        this.bRecord2MobileState =  bRecord2MobileState;
    }
    private boolean getRecord2MobileState()
    {
        return bRecord2MobileState;
    }
    private void checkIfRecording2CloseHandle()
    {
    // 2013-10-09 to close mp4 file used++
            if (getRecord2MobileState()) {
                setRecord2MobileState(false);
                Mp4FileCreator.getInstance().stopSaveFrame();
                updateGallery(getVideo2PhoneFilePathName());
                preview.setUIStateForRecording();               
               
                if (cameraRecord2PhoneTimer != null) {
                    cameraRecord2PhoneTimer.cancel();
                    cameraRecord2PhoneTimer = null;
                }
                
                if (cameraRecordTimer != null) {
                    cameraRecordTimer.cancel();
                    cameraRecordTimer = null;
                }

            }
    }       
    /************************************************************************************/
    /*
     * (Model Interface)
     */
    /************************************************************************************/

    @Override
    public void onBitmapReceive(Bitmap bitmapShow, Matrix matrix) {
        Log.d(TAG, "Preview8ActivityPresenter, onBitmapReceive bitmapShow:" + bitmapShow);
//      preview.zoomFactor(bitmapShow, touchEventProcess,zFactor);
        preview.uiSetImageBitmap(bitmapShow, matrix);
        preview.hideProgressBar();      
    }

    @Override
    public void onSurfaceReceive() {
        if (preview.getProgressBarState() == View.VISIBLE) {
            Log.d(TAG, "Preview8ActivityPresenter, onSurfaceReceive");
            preview.hideProgressBar();
        }
    }

    @Override
    public void onSavePhoto() {
        if (bSndLoadedCapture) {
            soundPool.play(sndIdCapture, 1f, 1f, 1, 0, 1f);
            Log.e("soundPool", "Played capture sound");
        }
        
    }
    
 

    @Override
    public void toBtOptionListHandle(int position) {
        Log.i(TAG, "Preview8ActivityPresenter toBtOptionListHandle position:"+position);    
        
    }
    
    public void clickBtOptionItemBtn(){     
        preview.setupBtOptionItemDialog();
        preview.showBtOptionItemDialog();
        
    }

    public ArrayList<String> getUpdateBTOptionItemList(){
        Log.i(TAG, "Preview8ActivityPresenter getUpdateBTOptionItemList() mBtOptionList:"+mBtOptionList);
        return mBtOptionList;
    }
    
    


}
