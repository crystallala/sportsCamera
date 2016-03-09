package com.qikoo.sportscamera.activity;

import java.io.File;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.Unieye.smartphone.ApiConstant;
import com.Unieye.smartphone.Constants;
import com.Unieye.smartphone.Constants.RemoteControlAction;
import com.Unieye.smartphone.exception.ResponseException;
import com.Unieye.smartphone.model.Mp4FileCreator;
import com.Unieye.smartphone.service.CameraService;
import com.Unieye.smartphone.util.ItemUtil;
import com.Unieye.smartphone.util.Log;
import com.Unieye.smartphone.util.MyToast;
import com.gt.common.http.ConnectionException;
import com.gt.common.http.InvalidNetworkException;
import com.qikoo.sportscamera.R;

public abstract class SmartPhoneAsyncTask<Params, Progress, Result> extends AsyncTask <Params, Progress, Object>{

    
    private static final String tag = "SmartPhoneAsyncTask";
    private Params p;
    
    private boolean isShowLoading = true;
    private boolean canCloseLoading = false;
    private boolean isLadscape = false;
    
    private boolean isShowingLoading = false;
    private boolean longEnough2CloseLoading = false;
    
    private static boolean isShowingLowBatteryExit;
    
    private String message;
    
    private SmartphoneApplication mSmartphoneApplication;
    private Activity activity;
    private ProgressDialog progressDialog;
    private boolean isConnectionException;
    
    private Timer timer;
    private Timer timer2;
    private Timer timer3;   
    //2013-11-07 for loading tmeout++
    private Timer timer4;
    private Handler handler;
    
    private ProgressDialog progressDlg = null;
    
    public SmartPhoneAsyncTask(Activity context){
        this.activity = context;
        this.isShowLoading = true;
        //message = context.getString(R.string.loading);
        this.message = context.getString(R.string.ID_Waiting);
        this.timer =  new Timer();
        this.timer4 = new Timer();
        setCameraView();
    }
    
    
    public SmartPhoneAsyncTask(Activity context,boolean isShowLoading){
        this.activity = context;
        this.isShowLoading = isShowLoading;
        //message = context.getString(R.string.loading);
        //this.message = context.getString(R.string.ID_Waiting);
        this.timer =  new Timer();
        this.timer4 = new Timer();
        setCameraView();
    }

    public SmartPhoneAsyncTask(Activity context, boolean isShowLoading, boolean canClosLoading){
        this.activity = context;
        this.isShowLoading = isShowLoading;
        this.canCloseLoading = canClosLoading;
        //message = context.getString(R.string.loading);
        this.message = context.getString(R.string.ID_Waiting);
        this.timer =  new Timer(); 
        this.timer4 = new Timer();
        setCameraView();
    }
    
    public SmartPhoneAsyncTask(Activity context, boolean isShowLoading, boolean canClosLoading, String message){
        this.activity = context;
        this.isShowLoading = isShowLoading;
        this.canCloseLoading = canClosLoading;
        //this.message = message;
        this.message = context.getString(R.string.ID_Waiting);
        this.timer = new Timer();
        this.timer4 = new Timer();
        setCameraView();
    }
    
    @SuppressWarnings("unchecked")
    public void execute() {
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            super.execute();
        }
    }

    private void setCameraView(){
         handler = new Handler(){
                @Override 
                public void handleMessage(Message msg) { 
                    super.handleMessage(msg); 
                    int msgId = msg.what;
//                  Log.i("ModaLog", "SmartPhoneAsyncTask timer: "+ Integer.toString(msgId) + ", isShowLoading:"+isShowLoading+", isShowingLoading:"+isShowingLoading+", longEnough2CloseLoading:"+longEnough2CloseLoading);
                    switch (msgId) { 
                    case 1:
                        if(isShowLoading){
                            progressDlg=ItemUtil.showLoading(activity, canCloseLoading, message);
                            isShowingLoading = true;
                            timer2 =  new Timer();
                            timer2.schedule(new TimerTask() { 
                                @Override 
                                public void run() { 
                                    Message message = new Message(); 
                                    message.what = 2; 
                                    handler.sendMessage(message); 
                                }
                            }, 1000); 
                        }
                        break;
                    case 2:
                        if(isShowLoading&&isShowingLoading){
                            longEnough2CloseLoading = true;
                        }
                        break;
                    case 3:
                        if(isShowLoading&&isShowingLoading&&longEnough2CloseLoading){
                            timer3.cancel();
                            ItemUtil.closeLoading();
                        }
                        break;
                    case 4:
                        if(isShowLoading && progressDlg!=null){
                            Log.d("ModaLog", "SmartPhoneAsyncTask timer: Force close loading");
//                          Toast.makeText(activity,"SmartPhoneAsyncTask timer: Force close loading",Toast.LENGTH_LONG).show();
                            timer4.cancel();
                            ItemUtil.closeLoading();
                        }
                        else
                        {
                            Log.d("ModaLog", "SmartPhoneAsyncTask timer4.cancel");
                            timer4.cancel();
                        }
                        
                        break;  
                    default: 
                        break; 
                    } 
                } 
            };
    }
    
    private void setTimerTask() { 
        timer.schedule(new TimerTask() { 
            @Override 
            public void run() { 
                Message message = new Message(); 
                message.what = 1; 
                handler.sendMessage(message); 
            }
        }, 1000); 
    }
    
    
    private void setTimerTask2() { 
        timer4.schedule(new TimerTask() { 
            @Override 
            public void run() { 
                Message message = new Message(); 
                message.what = 4; 
                handler.sendMessage(message); 
            }
        }, 1000); 
    }
    
    public boolean isConnectionException() {
        return isConnectionException;
    }


    public void setConnectionException(boolean isConnectionException) {
        this.isConnectionException = isConnectionException;
    }

    public boolean isLadscape() {
        return isLadscape;
    }


    public void setLadscape(boolean isLadscape) {
        this.isLadscape = isLadscape;
    }

    @Override
    protected void onCancelled() {
        Log.i("ModaLog", "SmartPhoneAsyncTask onCancelled, isShowLoading:"+isShowLoading+", canCloseLoading:"+canCloseLoading+", isShowingLoading:"+isShowingLoading);
        if(isShowLoading&&!canCloseLoading){
            if(isShowingLoading)
                ItemUtil.closeLoading();
            else {
                if(timer!=null){
                    timer.cancel();
                    timer = null;
                }
            }
        }
        super.onCancelled();
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
//      if(isShowLoading){
//          ItemUtil.showLoading(activity, canCloseLoading, message);
//      }
        setTimerTask();
    }
    
    @Override
    protected Object doInBackground(Params... params) {
        Object obj = null;
        p = null;
        if(params != null && params.length > 0){
            p = params[0];
        }
        try{
            obj = this.doInBackground(p);
        }
        catch(Exception e){
            obj = e;
        }
        
        return obj;
    }

    protected boolean handleException(Exception ex){
        return true;
    }
    
    
    @Override
    protected void onPostExecute(Object result) {
        
        try{
            super.onPostExecute(result);
            mSmartphoneApplication = (SmartphoneApplication)activity.getApplicationContext();
            timer.cancel();
            
            Log.i("result ","Sean result "+result + ", mSmartphoneApplication=" + mSmartphoneApplication);
            
            if(result != null){
                if(result instanceof InvalidNetworkException){
                    boolean isShowDefExceptionAlert = this.handleException((InvalidNetworkException)result);
                    if(isShowDefExceptionAlert){
                        ItemUtil.showConnectionNotOpenAlert(activity, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onFinishHandle();
                                dialog.cancel();
                            }
                        });
                    }
                }
                else if(result instanceof ConnectionException){
                    ConnectionException ce = (ConnectionException)result;
                    URL url = new URL(ce.getUrl());
                    boolean isShowDefExceptionAlert = this.handleException((ConnectionException)result);
                    if(isShowDefExceptionAlert){
                        if(url.getPath().equals(ApiConstant.GET_CAMERA_ICON_URI)) {
                            ;
                        } else if(url.getPath().equals(ApiConstant.GET_CAMERA_ICON_URI2)) {
                            ;
                        } else if(url.getHost().indexOf("google")!=-1) {
                            ;
                        } else if(url.getHost().indexOf("ustream")==-1 && url.getHost().indexOf("sensr")==-1) {
                            
                            Log.i("SeanLog", "Sean ConnectionException debug1");
                            Log.i("ModaLog", "Debug SaveVideo ConnectionException");
                            //Log.i("ModaLog", "Debug SaveVideo ConnectionException Record2MobileState="+mSmartphoneApplication.getRecord2MobileState()+" SaveReceiveRtpState="+mSmartphoneApplication.getFFMPEGObject().CFFMPEGSaveReceiveRtpState());
                            
//                          Log.i("ModaLog", "Debug SaveVideo ConnectionException Record2MobileState="+mSmartphoneApplication.getRecord2MobileState());
//                          Log.i("ModaLog", "Debug SaveVideo ConnectionException getFFMPEGObject="+mSmartphoneApplication.getFFMPEGObject());
                            Log.i("ModaLog", "Debug SaveVideo ConnectionException SaveReceiveRtpState="+Mp4FileCreator.getInstance().getSaveReceiveRtpState());
                            
                            
                            
//                          if(mSmartphoneApplication.get4in1Record2MobileState())
//                          {
//                              Mp4FileCreator.getInstance().stopSaveFrame4in1();   
//                              updateGallery(mSmartphoneApplication.getVideo2Phone4in1FilePathName());
//                              
//                          }
                            
                            Log.i("SeanLog", "Sean ConnectionException debug2");
                                                    
                            Log.i("SeanLog", "Sean ConnectionException debug3");

                            String errorMessage = activity.getString(R.string.ID_CameraDisconnected);
                                ItemUtil.showConnectionAlert(activity, errorMessage, activity.getString(R.string.ID_OK), new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //handleLogout();
//                                      handleNoRemoteLogout();
                                        if(mSmartphoneApplication.getCurrentMode() != null
                                                && mSmartphoneApplication.getCurrentMode().equals("WIZARD"))
                                            handleToWizardHomePage();
                                        else
                                            handleToHomePage();
                                        
                                        onFinishHandle();
                                        dialog.cancel();
                                    }
                                });
                                
                                Log.i("SeanLog", "Sean ConnectionException debug4");
                        } else {
//                          String errorMessage = activity.getString(R.string.error_network_not_open);
//                          ItemUtil.showAlert(activity, errorMessage, new OnClickListener() {
//                              @Override
//                              public void onClick(DialogInterface dialog, int which) {
//                                  dialog.dismiss();
//                              }
//                          });
                            Log.i("SeanLog", "Sean ConnectionException debug5");
                        }
                    }
                }
                else if(result instanceof ResponseException){
                    boolean isShowDefExceptionAlert=this.handleException((Exception)result);
                    if(isShowDefExceptionAlert){
                        String errorCode = null;
                        ResponseException responseException = (ResponseException) result;
                        errorCode = responseException.getErrorResponse().getErrorCode();
                        getErrorCode(errorCode);
                        handleErrorMessage(errorCode);
                    }
                }
                else if(result instanceof InterruptedException){
                    Log.e(tag, "InterruptedException error" + result.toString());
                }
//              else if(result instanceof NullPointerException){
//                  Log.e(tag, "NullPointerException error" + result.toString());
//              }
                else{
                    onFinishHandle();
                    doOnSuccess((Result)result);
                }
            }
            else{
//              Log.i(tag, "Sean onPostExecute before doOnSuccess");
                onFinishHandle();
                //doOnSuccess((Result) result);
                doOnSuccess(null);
//              Log.i(tag, "Sean onPostExecute After doOnSuccess");
            }
        }
        catch(Exception e){
//          Log.e(tag, "Sean onPostExecute before error" + e.toString());
            Log.e(tag, "Sean onPostExecute exception: " + e.toString());
            e.printStackTrace();
//          Log.e(tag, "Sean onPostExecute after error" + e.toString());
        }
        finally{
            if(isShowLoading&&isShowingLoading){
//              while(!longEnough2CloseLoading)
//              {
//                  try{
//                      Thread.sleep(100);
//                  }catch(InterruptedException e)
//                  {
//                      Log.i("ModaLog", "onPostExecute, finally, !longEnough2CloseLoading "+e.toString());
//                      break;
//                  }
//              }
                
//              ItemUtil.closeLoading();
                Log.e(tag, "Sean onPostExecute finally schedule timer3");
                
                timer3 =  new Timer();
                timer3.schedule(new TimerTask() { 
                    @Override 
                    public void run() { 
                        Message message = new Message(); 
                        message.what = 3; 
                        handler.sendMessage(message); 
                    }
                }, 0, 500);                 
                
            }
            else
            {
                //2013-11-07 Loading Timeout++
                setTimerTask2();
            }
            
        }
    }
    
    private void handleErrorMessage(final String errorCode) {
        
    
        String errorMessage = null;
        switch (Integer.parseInt(errorCode)) {
        
        case -1:
            errorMessage = activity.getString(R.string.ID_IncorrectPassword);
            //errorMessage = activity.getString(R.string.ID_InconsistentPassword);
            break;
        case -2:        
            errorMessage = activity.getString(R.string.ID_CameraDisconnected);
            break;
        case -3:
            errorMessage = activity.getString(R.string.ID_InvalidFile);
            break;
        case -4:
            errorMessage = activity.getString(R.string.ID_CameraDisconnected);
            break;
        case -5:
            errorMessage = activity.getString(R.string.ID_CardLock);
            break;
        case -6:
            errorMessage = activity.getString(R.string.ID_MemoryFull);
            break;
        case -7:
            errorMessage = activity.getString(R.string.ID_LowBattery);
            break;
        case -8:
            //errorMessage = activity.getString(R.string.adapter_applied);
            errorMessage = activity.getString(R.string.ID_NoSDCard);
            break;
        case -9:
            errorMessage = activity.getString(R.string.ID_CameraOccupied);
            break;
        case -10:
            errorMessage = activity.getString(R.string.ID_InRecording);
            break;
        case -11:
            errorMessage = activity.getString(R.string.ID_SaveNG);
            break;  
        case -15:
            errorMessage = activity.getString(R.string.ID_CameraInEmergency);
            break;  
        }
        
        if(Integer.parseInt(errorCode)==-8)
        {
            MyToast.makeText(activity,activity.getString(R.string.ID_NoSDCard),Toast.LENGTH_LONG).show();   
            onFinishHandle();
        }
        else if(Integer.parseInt(errorCode)==-14)
        {
            
        }
        else if(Integer.parseInt(errorCode)==-7)
        {
            
            
            if(isShowingLowBatteryExit==false)
            ItemUtil.showAlert(activity, errorMessage,activity.getString(R.string.ID_OK), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onFinishHandle();
                    dialog.cancel();
                    isShowingLowBatteryExit=false;
//                      handleLogout();
                    if(mSmartphoneApplication.getCurrentMode() != null
                            && mSmartphoneApplication.getCurrentMode().equals("WIZARD"))
                        handleLogout2WizardHomePage();
                    else
                        handleLogout2HomePage();
                                        
                }
            });
            
            isShowingLowBatteryExit=true;
            
            
        }
        else
        ItemUtil.showAlert(activity, errorMessage, activity.getString(R.string.ID_OK), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onFinishHandle();
                dialog.cancel();
                if(errorCode.equals("-10") || errorCode.equals("-11") || errorCode.equals("-5")){//dh
                    
                }
                else if(errorCode.equals("-2") || errorCode.equals("-4") )
                {                   
                    if(mSmartphoneApplication.getCurrentMode() != null
                            && mSmartphoneApplication.getCurrentMode().equals("WIZARD"))
                        handleLogout2WizardHomePage();
                    else
                        handleLogout2HomePage();
                    
                }
                else
                {   
                    if(mSmartphoneApplication.getCurrentMode() != null
                            && mSmartphoneApplication.getCurrentMode().equals("WIZARD"))
                        handleToWizardHomePage();
                    else
//                  handleNoRemoteLogout(); 
                        handleToHomePage();
                    
                }   
            }
        });
    }

    protected void handleLogout() {
        
//      mSmartphoneApplication = (SmartphoneApplication)activity.getApplicationContext();
//      mSmartphoneApplication.setCamera(null);
//      mSmartphoneApplication.setSessionKey(null);
//      mSmartphoneApplication.setCameraPassword(null);
//      
//      Intent in = new Intent();
//      //in.setClass(this.activity, CameraListActivity.class);
//      //in.setClass(this.activity, CameraList2in1Activity.class);
//      in.setClass(this.activity, CameraCloudNScanListActivity.class);
//      ((Activity)this.activity).startActivity(in);
//      ((Activity)this.activity).finish();
        
                
        mSmartphoneApplication = (SmartphoneApplication)activity.getApplicationContext();
        CameraService mCameraService = mSmartphoneApplication.getCameraService();
        try {
            mCameraService.remoteControl(RemoteControlAction.LOGOUT);
        } catch (ResponseException e) {
            e.printStackTrace();
        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (InvalidNetworkException e) {
            e.printStackTrace();
        }

        SharedPreferences settings = ((Activity)this.activity).getSharedPreferences(Constants.SETTING_INFO, 0);
        settings.edit().putBoolean(Constants.FIRSTRUN, true).commit();

        
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ((Activity)this.activity).startActivity(startMain);
        System.exit(0);
        
    }
    
    protected void handleLogout2WizardHomePage() {  
                
        
    }
    
    protected void handleLogout2HomePage() {
            
        mSmartphoneApplication = (SmartphoneApplication)activity.getApplicationContext();
        CameraService mCameraService = mSmartphoneApplication.getCameraService();
        try {
            mCameraService.remoteControl(RemoteControlAction.LOGOUT);
        } catch (ResponseException e) {
            e.printStackTrace();
        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (InvalidNetworkException e) {
            e.printStackTrace();
        }

        SharedPreferences settings = ((Activity)this.activity).getSharedPreferences(Constants.SETTING_INFO, 0);
        settings.edit().putBoolean(Constants.FIRSTRUN, true).commit();
        
        Intent in = new Intent();
        in.setClass(this.activity, CameraSearchActivity.class);
        ((Activity)this.activity).startActivity(in);
        ((Activity)this.activity).finish();
        
        
    }
    protected void handleToWizardHomePage() {
        
    
        
    }
    protected void handleToHomePage() {
        
        mSmartphoneApplication = (SmartphoneApplication)activity.getApplicationContext();

        SharedPreferences settings = ((Activity)this.activity).getSharedPreferences(Constants.SETTING_INFO, 0);
        settings.edit().putBoolean(Constants.FIRSTRUN, true).commit();
        
        Intent in = new Intent();
        in.setClass(this.activity, CameraSearchActivity.class);
        ((Activity)this.activity).startActivity(in);
        ((Activity)this.activity).finish();
        
        
    }
    
protected void handleNoRemoteLogout() {
        

                
        mSmartphoneApplication = (SmartphoneApplication)activity.getApplicationContext();
        CameraService mCameraService = mSmartphoneApplication.getCameraService();

        SharedPreferences settings = ((Activity)this.activity).getSharedPreferences(Constants.SETTING_INFO, 0);
        settings.edit().putBoolean(Constants.FIRSTRUN, true).commit();
        
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ((Activity)this.activity).startActivity(startMain);
        System.exit(0);
        
    }
    
    abstract protected Result doInBackground(Params params) throws ConnectionException, ResponseException, InvalidNetworkException, InterruptedException;
    
    abstract protected void doOnSuccess(Result result);

    protected void onFinishHandle() {}
    protected void getErrorCode(String errorCode){}
    
    public void quit() {}
    
    public void updateGallery(String file) {
        File f = new File(file);
        Uri contentUri = Uri.fromFile(f);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,contentUri);
        mSmartphoneApplication.sendBroadcast(mediaScanIntent);
    }


}
