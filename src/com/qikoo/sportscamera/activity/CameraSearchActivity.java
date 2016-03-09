package com.qikoo.sportscamera.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.Unieye.smartphone.ApiConstant.WifiReceiveAction;
import com.Unieye.smartphone.exception.ResponseException;
import com.Unieye.smartphone.model.IWiFiApListerListener;
import com.Unieye.smartphone.model.IWiFiConnectorListener;
import com.Unieye.smartphone.model.WiFiApLister;
import com.Unieye.smartphone.pojo.BaseResponse;
import com.Unieye.smartphone.pojo.Camera;
import com.Unieye.smartphone.pojo.CameraInfo;
import com.Unieye.smartphone.pojo.CameraSetting;
import com.Unieye.smartphone.pojo.LoginResponse;
import com.Unieye.smartphone.pojo.PushServerInfo;
import com.Unieye.smartphone.pojo.WifiAP;
import com.Unieye.smartphone.service.CameraService;
import com.Unieye.smartphone.util.CecString;
import com.Unieye.smartphone.util.CountryCode;
import com.Unieye.smartphone.util.DataUtil;
import com.Unieye.smartphone.util.Log;
import com.Unieye.smartphone.util.SystemInfo;
import com.gt.common.http.ConnectionException;
import com.gt.common.http.InvalidNetworkException;
import com.qikoo.sportscamera.R;

public class CameraSearchActivity extends BaseActivity implements
        android.widget.AdapterView.OnItemClickListener, IWiFiApListerListener,
        IWiFiConnectorListener {

    private Handler mHandler;
    private Runnable loadCameraListRunnable;
    private ImageView ivCameraSearching;
    private AnimationDrawable anim;
    private List<WifiAP> mCameraAPList;
    private WifiAdmin mWifiAdmin;
    private WifiAdapter adapter;
    private WifiAP mCameraAP;

    private TextView tvConnectionState;

    private WiFiApLister mWiFiApLister;
    private RelativeLayout rlCameraList;
    private ListView lvCameraList;
    
    private  Dialog  dialog;
    private InputMethodManager imm;
    private static final String default_password = "12345678";
    public static final String GCM_API_KEY = "AIzaSyA0vgcdToS3jJW9CD0_CuumEfcy3puya3g";
    public static final  String BAIDU_API_KEY = "XlrgFHs6BcokBw1F6NxgtujU";
    public static final String BAIDU_SECRET_KEY = "u5ps4jBqWsH4em3ShAvrvOfMAYV42xql";
    private String mPowerFrequencyInMobileLocale = null;
    private SmartphoneApplication mApplication;
    private CameraService mCameraService;
    private BaseResponse mBaseResponse;
    private CameraInfo cameraInfo;
    private SmartPhoneAsyncTask<Void, Void, Object> mLoginCameraTask;

    private class LoginCamAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            PushServerInfo pushServerInfo = new PushServerInfo();
            pushServerInfo.setBaiduUserId("");
            pushServerInfo.setGcmDeviceId("");
            
            LoginResponse  mLoginResponse = null;
//            Camera camera = new Camera();
//            //CameraInfo cameraInfo = camera.getCameraInfo();
//            CameraInfo cameraInfo = mApplication.getCamera().getCameraInfo();
//            cameraInfo.setIp("192.168.1.1");
//            cameraInfo.setName(mCameraAP.getSSID());
//            cameraInfo.setHttpPort("80");
//            cameraInfo.setRtspPort("554");
            //mApplication.setCurrentMode("DIRECT");

            
            
            try {
                if ((SystemInfo.getAvaiProceNumInfo() >= 2 || SystemInfo.getCpuProcessorNumInfo() >= 1)
                        || SystemInfo.getCpuBogoMIPSInfo() >= 1000) {
                    Log.e("123", "callAutoLoginCameraApi cameraInfo:" + cameraInfo+" ,password:"+params[0] + " ,mode:"+mApplication.getCurrentMode());
                    mLoginResponse = mCameraService.loginCamera(cameraInfo, params[0], pushServerInfo,
                            mApplication.getCurrentMode());
                } else {
                    // bH264Path=false;
                    mLoginResponse = mCameraService.loginCamera(cameraInfo, params[0], pushServerInfo,
                            mApplication.getCurrentMode());
                }
                    
                if (mLoginResponse.getResultStatus().equals(BaseResponse.STATUS_OK)) {
                    if (mPowerFrequencyInMobileLocale != null) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(CecString.makeXmlElement("TVSystem", mPowerFrequencyInMobileLocale));

                        String strXML = sb.toString();
                        mBaseResponse = mCameraService.setCameraGeneralSetup(strXML);
                    }
                    setMobileSettingToCamera();
                    startLoading();
                }

            } catch (ResponseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ConnectionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvalidNetworkException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
     
            return null;
        }
        
        
        
    }
    
    private String findSupportLanguage() {
        String mobileLang = "EN";
        String language = Locale.getDefault().getLanguage();
        String country = Locale.getDefault().getCountry();
        if (language.equals("zh")) {
            if (country.equals("CN"))
                mobileLang = "ZHS";
            else
                mobileLang = "ZHT";
        } else {
            mobileLang = language.toUpperCase();
        }

        return mobileLang;
    }

    
    private void setMobileSettingToCamera() throws ResponseException, ConnectionException, InvalidNetworkException {

        CameraSetting cameraSetting = mCameraService.getCameraSetting();
        String supportLanguage = cameraSetting.getSupport_language();
        String[] supportLanguageList = supportLanguage.split(",");

        String langCode = Locale.getDefault().toString();
        String mobileLang = findSupportLanguage();

        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);
        StringBuilder sb = new StringBuilder();
        sb.append("<CameraSetup>");
        sb.append("<LANGUAGE>");
        sb.append(mobileLang);
        sb.append("</LANGUAGE>");
        sb.append("<Time>");
        sb.append("<Year>");
        sb.append(mYear);
        sb.append("</Year>");
        sb.append("<Month>");
        sb.append(mMonth + 1);
        sb.append("</Month>");
        sb.append("<Day>");
        sb.append(mDay);
        sb.append("</Day>");
        sb.append("<Hour>");
        sb.append(mHour);
        sb.append("</Hour>");
        sb.append("<Minute>");
        sb.append(mMinute);
        sb.append("</Minute>");
        sb.append("</Time>");
        sb.append("</CameraSetup>");

        String strXML = sb.toString();
        try {
            mBaseResponse = mCameraService.setCameraGeneralSetup(strXML);
            
            StringBuilder xmlSb = new StringBuilder();
            xmlSb.append("<CloudServiceKey>");
            xmlSb.append("<GCM>" + GCM_API_KEY+"</GCM>");
            xmlSb.append("<Baidu>" + BAIDU_API_KEY+"</Baidu>");
            xmlSb.append("<BaiduSecret>" + BAIDU_SECRET_KEY+"</BaiduSecret>");
            xmlSb.append("</CloudServiceKey>");
            strXML = xmlSb.toString();
            mBaseResponse = mCameraService.updateCloudServiceKey(strXML);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private class mCheckMobileCountryTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String country = SystemInfo.getMobileCountry(CameraSearchActivity.this);
            CountryCode cc = null;
            if (country != null && country.length() > 0) {
                cc = CountryCode.getCountryCodeA2(country.toUpperCase());
                if (cc != null)
                    mPowerFrequencyInMobileLocale = cc.getCountryPF() + "HZ"; // 50HZ
                                                                                // or
                                                                                // 60HZ
            }
            
            return null;
        }

    }
    
    public void goCheckMobileCountry() {
        mCheckMobileCountryTask checkCountry = new mCheckMobileCountryTask();
        checkCountry.execute();
    }

    public void onResume() {
        super.onResume();
        goCheckMobileCountry();
    }

    public CameraSearchActivity() {
        mHandler = new Handler();        
        loadCameraListRunnable = new Runnable() {
            public void run() {
                loadCameraList();
                mHandler.postDelayed(loadCameraListRunnable, 2000L);
            }
        };
    }

    private void loadCameraList() {
        mWiFiApLister = new WiFiApLister(CameraSearchActivity.this, true,
                false, "R2-", "RZ-");
        //mWiFiApLister.registerListener(this);
//        mWifiAdmin = new WifiAdmin(this);
//        if (!mWifiAdmin.isWifiEnabled()) {
//            mWifiAdmin.openWifi();
//        }
        mWiFiApLister.startScan();
        mCameraAPList = mWiFiApLister.getCameraApList();
        rlCameraList.setVisibility(View.GONE);
        if (mCameraAPList != null) {
            adapter = new WifiAdapter();
            adapter.setWifiList(mCameraAPList);
            adapter.notifyDataSetChanged();
            showList();
        }
    }

    private void showInputPasswordDialog(WifiAP cameraAP) {
        dialog = new Dialog(CameraSearchActivity.this);

        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.camera_dialog_input_wifi_password);
        
        lp.width = (int) DataUtil.dip2px(CameraSearchActivity.this, 260);
        lp.y = (int) DataUtil.dip2px(CameraSearchActivity.this, 20);
        dialogWindow.setAttributes(lp);
        dialog.show();
        TextView mtitle = (TextView) dialog.findViewById(R.id.tvTitle); 
        mtitle.setText(cameraAP.getSSID());
        TextView tv_cancel_btn = (TextView) dialog.findViewById(R.id.left_button);
        TextView tv_ok_btn = (TextView) dialog.findViewById(R.id.right_button);
        final EditText password_input_et = (EditText) dialog.findViewById(R.id.edtPassword);

        final String ssid = cameraAP.getSSID();
        final String macAdress = cameraAP.getMac();
        
        password_input_et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        tv_cancel_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //presenter.DoRefreshCloudOrRescanDirect();
            }
        });

        tv_ok_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = password_input_et.getText().toString().trim();
                
                imm.hideSoftInputFromWindow(password_input_et.getWindowToken(), 0);
                if(TextUtils.isEmpty(password)) {
                    password = default_password;
                }
                
                LoginCameraApi(password);

                dialog.dismiss();
                //callLoginCameraApi(presenter.getCameraInfo(), dialog, password);
            }
        });

        mWiFiApLister.setStopScan(true);
    }
    
    private void LoginCameraApi(String password){
        if (mCameraAP == null) {
            return;
        }
        itemClickCameraAPLV(password);


    }

    public void itemClickCameraAPLV(String paw) {
        Camera camera = new Camera();
        LoginResponse  mLoginResponse = null;
        //CameraInfo cameraInfo = camera.getCameraInfo();
        CameraInfo cameraInfo = mApplication.getCamera().getCameraInfo();
        cameraInfo.setIp("192.168.1.1");
        cameraInfo.setName(mCameraAP.getSSID());
        cameraInfo.setHttpPort("80");
        cameraInfo.setRtspPort("554");
        mApplication.setCurrentMode("DIRECT");
        mCameraService = mApplication.getCameraService();
        callLoginCameraApi(mApplication.getCamera().getCameraInfo(), null, paw);

//        LoginCamAsyncTask loginTask = new LoginCamAsyncTask();
//        loginTask.execute(paw);
    }

    public void callLoginCameraApi(final CameraInfo cameraInfo, final Dialog dialog, final String password){

        mLoginCameraTask = new SmartPhoneAsyncTask<Void, Void, Object>(this, false, false) {
            @Override
            protected Object doInBackground(Void params) throws ConnectionException, ResponseException,
                    InvalidNetworkException {
                //-------------------------------------------------------------------
                PushServerInfo pushServerInfo = new PushServerInfo();
//              pushServerInfo.setBaiduUserId(mApplication.getGCMDeviceID());
//              pushServerInfo.setGcmDeviceId(mApplication.getBaiduUserId());
                pushServerInfo.setBaiduUserId("");
                pushServerInfo.setGcmDeviceId("");
                //-------------------------------------------------------------------
//              CameraInfo cameraInfo = mApplication.getCamera().getCameraInfo();
                LoginResponse  mLoginResponse = null;
                if ((SystemInfo.getAvaiProceNumInfo() >= 2 || SystemInfo.getCpuProcessorNumInfo() >= 1)
                        || SystemInfo.getCpuBogoMIPSInfo() >= 1000) {
                    mLoginResponse = mCameraService.loginCamera(cameraInfo, password, pushServerInfo,
                            mApplication.getCurrentMode());
                } else {
                    // bH264Path=false;
                    mLoginResponse = mCameraService.loginCamera(cameraInfo, password, pushServerInfo,
                            mApplication.getCurrentMode());
                }
                
                if (mLoginResponse.getResultStatus().equals(BaseResponse.STATUS_OK)) {
                    if (mPowerFrequencyInMobileLocale != null) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(CecString.makeXmlElement("TVSystem", mPowerFrequencyInMobileLocale));

                        String strXML = sb.toString();
                        mBaseResponse = mCameraService.setCameraGeneralSetup(strXML);
                    }
                    setMobileSettingToCamera();
                    startLoading();
                }
                return mLoginResponse;
            }

            @Override
            protected boolean handleException(Exception ex) {
                if (ex instanceof ResponseException) {
                    if (dialog != null) {
                        dialog.cancel();
                    }
                    String errorCode = null;
                    ResponseException responseException = (ResponseException) ex;
                    errorCode = responseException.getErrorResponse().getErrorCode(); 

                    return false;
                } 

                return super.handleException(ex);
            }

            @Override
            protected void doOnSuccess(Object result) {}

            @Override
            protected void onPostExecute(Object result) {
                super.onPostExecute(result);
            }

        };
        mLoginCameraTask.execute();
    }
    
    private void showList() {
        ivCameraSearching.setVisibility(View.VISIBLE);
        rlCameraList.setVisibility(View.VISIBLE);
        
        lvCameraList.setOnItemClickListener(this);
        
        lvCameraList.setAdapter(adapter);

    }

    private void startLoading() {
        Intent intent = new Intent(this, CameraMainControlActivity.class);
        startActivity(intent);
        finish();
    }

    
    public static class SearchStepTwo extends Fragment {
        public View onCreateView(LayoutInflater layoutinflater,
                ViewGroup viewgroup, Bundle bundle) {
            return layoutinflater
                    .inflate(R.layout.search_camera_step_two, null);
        }

    }

    public static class SearchStepOne extends Fragment {
        public View onCreateView(LayoutInflater layoutinflater,
                ViewGroup viewgroup, Bundle bundle) {
            return layoutinflater
                    .inflate(R.layout.search_camera_step_one, null);
        }
    }

    private class WifiAdapter extends BaseAdapter {
        private List<WifiAP> mWifiList;

        public int getCount() {
            return mWifiList.size();
        }

        public Object getItem(int i) {
            return mWifiList.get(i);
        }

        public long getItemId(int i) {
            return (long) i;
        }

        public View getView(int i, View view, ViewGroup viewgroup) {
            View view1 = View.inflate(CameraSearchActivity.this,
                    R.layout.camera_item_list, null);
            ((TextView) view1.findViewById(R.id.tvWifiName))
                    .setText(mCameraAPList.get(i).getSSID());
            return view1;
        }

        public void setWifiList(List<WifiAP> list) {
            mWifiList = list;
        }

        private WifiAdapter() {
            super();
            mWifiList = new ArrayList<WifiAP>();
        }

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen(true);
        setContentView(R.layout.search_add_camera);
        initView();
        mApplication = (SmartphoneApplication) this.getApplication();
    }

    private void initView() {
        ivCameraSearching = (ImageView) findViewById(R.id.ivCameraSearching);
        ivCameraSearching.setVisibility(View.VISIBLE);

        tvConnectionState = (TextView) findViewById(R.id.tvConnectionState);
        tvConnectionState.setText(getResources().getString(R.string.searching));
        
        rlCameraList = (RelativeLayout)findViewById(R.id.rlList);
        lvCameraList = (ListView)findViewById(R.id.lvCameraList);
        rlCameraList.setVisibility(View.GONE);
        
        imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);      

        anim = (AnimationDrawable) ivCameraSearching.getBackground();
        anim.start();
        mHandler.post(loadCameraListRunnable);
    }

    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(loadCameraListRunnable);
        if (mWiFiApLister != null) {
            mWiFiApLister.removeListener(this);
            mWiFiApLister.setStopScan(true);
        }

        if (anim != null) {
            anim.stop();
        }
    }

    @Override
    public void update(WifiReceiveAction newValue) {
        // TODO Auto-generated method stub
        switch (newValue) {
        case WIFI_RECEIVE_UPDATE_SCAN_CAMERA_AP_RESULTS:
            mCameraAPList = mWiFiApLister.getCameraApList();
            // mView.scanResultAvailableOK(mCameraAPList, true);
            break;
        default:
            break;
        }
    }

    
    @Override
    public void onItemClick(AdapterView<?> adapterview, View view, int i, long l) {
        // TODO Auto-generated method stub
        showInputPasswordDialog(mCameraAPList.get((int) l));
        mCameraAP = mCameraAPList.get((int) l);
        if (anim != null) {
            anim.stop();
        }
        ivCameraSearching.setVisibility(View.INVISIBLE);

    }
}
