package com.qikoo.sportscamera.activity;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.Unieye.smartphone.model.WiFiApLister;
import com.Unieye.smartphone.util.Log;

public class WifiAdmin {

    private static final String TAG = "WifiAdmin";
    public static final int WIFI_CONNECTED = 1;
    public static final int WIFI_CONNECTING = 3;
    public static final int WIFI_CONNECT_FAILED = 2;
    private Context mContext;
    private List mWifiConfiguration;
    private WifiInfo mWifiInfo;
    private List mWifiList;
    private android.net.wifi.WifiManager.WifiLock mWifiLock;
    private WifiManager mWifiManager;
    private WiFiApLister mWiFiApLister;

    public WifiAdmin(Context context) {
        mContext = null;
        mContext = context;
        mWifiManager = (WifiManager) context.getSystemService("wifi");
        mWifiInfo = mWifiManager.getConnectionInfo();
        Log.v("WifiAdmin", (new StringBuilder()).append("getIpAddress = ")
                .append(mWifiInfo.getIpAddress()).toString());
    }

    public static boolean isCameraDevice(ScanResult scanresult) {
        boolean flag;
        if (scanresult == null)
            flag = false;
        else
            flag = isCameraDevice(scanresult.SSID, scanresult.BSSID);
        return flag;
    }

    public static boolean isCameraDevice(WifiInfo wifiinfo) {
        boolean flag = false;
        if (wifiinfo != null) {
            String s = wifiinfo.getSSID();
            if (s != null) {
                flag = isCameraDevice(s.replace("\"", ""), wifiinfo.getBSSID());
            }
        }
        return flag;
    }

    private static boolean isCameraDevice(String s, String s1) {
        boolean flag = false;
        if (s != null
                && s1 != null
                && (s.startsWith("R2-") || s.startsWith("SportsCamera_")
                        || s.startsWith("RZ-") || s1.toUpperCase(
                        Locale.getDefault()).startsWith("04:E6:76"))) {
            flag = true;
        }
        return flag;
    }

    public WifiConfiguration CreateWifiInfo(String s, String s1, int i) {
        WifiConfiguration wificonfiguration;
        wificonfiguration = new WifiConfiguration();
        wificonfiguration.allowedAuthAlgorithms.clear();
        wificonfiguration.allowedGroupCiphers.clear();
        wificonfiguration.allowedKeyManagement.clear();
        wificonfiguration.allowedPairwiseCiphers.clear();
        wificonfiguration.allowedProtocols.clear();
        wificonfiguration.SSID = (new StringBuilder()).append("\"").append(s)
                .append("\"").toString();
        WifiConfiguration wificonfiguration1 = isExsits(s);
        if (wificonfiguration1 != null) {
            mWifiManager.removeNetwork(wificonfiguration1.networkId);
        }
        if (i != 1) {
            if (i == 2) {
                wificonfiguration.hiddenSSID = true;
                wificonfiguration.wepKeys[0] = (new StringBuilder())
                        .append("\"").append(s1).append("\"").toString();
                wificonfiguration.allowedAuthAlgorithms.set(1);
                wificonfiguration.allowedGroupCiphers.set(3);
                wificonfiguration.allowedGroupCiphers.set(2);
                wificonfiguration.allowedGroupCiphers.set(0);
                wificonfiguration.allowedGroupCiphers.set(1);
                wificonfiguration.allowedKeyManagement.set(0);
                wificonfiguration.wepTxKeyIndex = 0;
            } else if (i == 3) {
                wificonfiguration.preSharedKey = (new StringBuilder())
                        .append("\"").append(s1).append("\"").toString();
                wificonfiguration.hiddenSSID = true;
                wificonfiguration.allowedAuthAlgorithms.set(0);
                wificonfiguration.allowedGroupCiphers.set(2);
                wificonfiguration.allowedKeyManagement.set(1);
                wificonfiguration.allowedPairwiseCiphers.set(1);
                wificonfiguration.allowedGroupCiphers.set(3);
                wificonfiguration.allowedPairwiseCiphers.set(2);
                wificonfiguration.status = 2;
            }
        } else {
            wificonfiguration.wepKeys[0] = "";
            wificonfiguration.allowedKeyManagement.set(0);
            wificonfiguration.wepTxKeyIndex = 0;
        }
        return wificonfiguration;
    }

    @SuppressWarnings("unused")
    private boolean reconnect() {
        return mWifiManager.reconnect();
    }

    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    public boolean addAndEnableNetwork(WifiConfiguration wificonfiguration) {
        boolean flag = false;
        try {
            int i = mWifiManager.addNetwork(wificonfiguration);
            if (i >= 0) {
                mWifiManager.saveConfiguration();
                mWifiManager.enableNetwork(i, true);
                saveConfiguration();
                flag = reconnect();
            } else {
                Log.e(TAG, "WifiManager add network failed");
            }
        } catch (IllegalArgumentException illegalargumentexception) {
            illegalargumentexception.printStackTrace();
            Log.e(TAG,
                    "WifiManager add network IllegalArgumentException");
        }
        return flag;
    }

    public boolean addAndEnableNetwork(String s) {
        return addAndEnableNetwork(createWifiInfo(s));
    }

    public int checkState() {
        return mWifiManager.getWifiState();
    }

    public void closeWifi() {
        if (mWifiManager.isWifiEnabled())
            mWifiManager.setWifiEnabled(false);
    }

    public void connectConfiguration(int i) {
        if (mWifiConfiguration != null && i <= mWifiConfiguration.size())
            mWifiManager.enableNetwork(
                    ((WifiConfiguration) mWifiConfiguration.get(i)).networkId,
                    true);
    }

    public void conntectToEnableWifi() {
        ScanResult scanresult;
        startScan();
        scanresult = null;
        if (mWifiConfiguration != null && mWifiList != null) {
            HashMap hashmap = new HashMap();
            Iterator iterator = mWifiConfiguration.iterator();
            do {
                if (!iterator.hasNext())
                    break;
                WifiConfiguration wificonfiguration = (WifiConfiguration) iterator
                        .next();
                if (wificonfiguration.SSID != null)
                    hashmap.put(
                            wificonfiguration.SSID.substring(1, -1
                                    + wificonfiguration.SSID.length()),
                            wificonfiguration);
            } while (true);
            Iterator iterator1 = mWifiList.iterator();
            do {
                if (!iterator1.hasNext())
                    break;
                ScanResult scanresult1 = (ScanResult) iterator1.next();
                if (!isCameraDevice(scanresult1)
                        && hashmap.containsKey(scanresult1.SSID))
                    if (scanresult == null)
                        scanresult = scanresult1;
                    else if (scanresult1.level > scanresult.level)
                        scanresult = scanresult1;
            } while (true);
            if (scanresult != null) {
                addAndEnableNetwork((WifiConfiguration) hashmap
                        .get(scanresult.SSID));
            }
        }
        return;
    }

    public void creatWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("Test");
    }

    public WifiConfiguration createWifiInfo(String s) {
        Log.v("WifiAdmin", (new StringBuilder()).append("SSID = ").append(s)
                .toString());
        WifiConfiguration wificonfiguration = isExsits(s);
        if (wificonfiguration != null)
            mWifiManager.removeNetwork(wificonfiguration.networkId);
        WifiConfiguration wificonfiguration1 = new WifiConfiguration();
        wificonfiguration1.status = 1;
        wificonfiguration1.priority = 40;
        wificonfiguration1.allowedKeyManagement.set(0);
        wificonfiguration1.allowedProtocols.set(1);
        wificonfiguration1.allowedProtocols.set(0);
        wificonfiguration1.allowedAuthAlgorithms.clear();
        wificonfiguration1.allowedPairwiseCiphers.set(2);
        wificonfiguration1.allowedPairwiseCiphers.set(1);
        wificonfiguration1.allowedGroupCiphers.set(0);
        wificonfiguration1.allowedGroupCiphers.set(1);
        wificonfiguration1.allowedGroupCiphers.set(3);
        wificonfiguration1.allowedGroupCiphers.set(2);
        wificonfiguration1.SSID = (new StringBuilder()).append("\"").append(s)
                .append("\"").toString();
        return wificonfiguration1;
    }

    public void disableAndRemoveCurrentWifi() {
        if (mWifiInfo != null) {
            disconnectWifi(mWifiInfo.getNetworkId());
            mWifiManager.removeNetwork(mWifiInfo.getNetworkId());
        }
    }

    public void disableAndRemoveWifi(String s) {
        WifiConfiguration wificonfiguration = isExsits(s);
        if (wificonfiguration != null) {
            disconnectWifi(wificonfiguration.networkId);
            mWifiManager.removeNetwork(wificonfiguration.networkId);
            saveConfiguration();
        }
    }

    public void disconnect() {
        WifiInfo wifiinfo = ((WifiManager) mContext.getSystemService("wifi"))
                .getConnectionInfo();
        if (wifiinfo != null)
            disconnectWifi(wifiinfo.getNetworkId());
    }

    public void disconnectWifi(int i) {
        mWifiManager.disableNetwork(i);
        mWifiManager.disconnect();
    }

    public boolean enableNetwork(WifiConfiguration wificonfiguration) {
        boolean flag;
        if (wificonfiguration != null) {
            mWifiManager.enableNetwork(wificonfiguration.networkId, true);
            saveConfiguration();
            flag = reconnect();
        } else {
            flag = false;
        }
        return flag;
    }

    public String getBSSID() {
        String s;
        if (mWifiInfo == null)
            s = "NULL";
        else
            s = mWifiInfo.getBSSID();
        return s;
    }

    public List getConfiguration() {
        return mWifiConfiguration;
    }

    public int getIPAddress() {
        int i;
        if (mWifiInfo == null)
            i = 0;
        else
            i = mWifiInfo.getIpAddress();
        return i;
    }

    public String getMacAddress() {
        String s;
        if (mWifiInfo == null)
            s = "NULL";
        else
            s = mWifiInfo.getMacAddress();
        return s;
    }

    public int getNetworkId() {
        int i;
        if (mWifiInfo == null)
            i = 0;
        else
            i = mWifiInfo.getNetworkId();
        return i;
    }

    public String getSSID() {
        String s;
        if (mWifiInfo == null)
            s = "NULL";
        else
            s = mWifiInfo.getSSID();
        return s;
    }

    public WifiInfo getWifiInfo() {
        return mWifiInfo;
    }

    public List getWifiList() {
        return mWifiList;
    }

    public boolean isAuthenticateFailed(WifiConfiguration wificonfiguration) {
        Class class1;
        boolean flag;
        class1 = wificonfiguration.getClass();
        flag = false;
        int i;
        try {
            Field field = class1.getDeclaredField("disableReason");
            field.setAccessible(true);
            i = field.getInt(wificonfiguration);
            if (i == 3) {
                flag = true;
            }
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException illegalaccessexception) {
            // TODO: handle exception
            illegalaccessexception.printStackTrace();
        } catch (IllegalArgumentException illegalargumentexception) {
            illegalargumentexception.printStackTrace();
        }
        return flag;
    }

    public boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    public StringBuilder lookUpScan() {
        StringBuilder stringbuilder = new StringBuilder();
        for (int i = 0; i < mWifiList.size(); i++) {
            stringbuilder.append((new StringBuilder()).append("Index_")
                    .append((new Integer(i + 1)).toString()).append(":")
                    .toString());
            stringbuilder.append(((ScanResult) mWifiList.get(i)).toString());
            stringbuilder.append("/n");
        }

        return stringbuilder;
    }

    public void openWifi() {
        try {
            if (!mWifiManager.isWifiEnabled()) {
                mWifiManager.setWifiEnabled(true);
            }
        } catch (ActivityNotFoundException activitynotfoundexception) {
            // TODO Auto-generated catch block
            activitynotfoundexception.printStackTrace();
        }
    }

    public void releaseWifiLock() {
        if (mWifiLock.isHeld())
            mWifiLock.acquire();
    }

    public void removeWifi(String s) {
        WifiConfiguration wificonfiguration = isExsits(s);
        if (wificonfiguration != null) {
            mWifiManager.removeNetwork(wificonfiguration.networkId);
            saveConfiguration();
        }
    }

    public void saveConfiguration() {
        mWifiManager.saveConfiguration();
    }

    public void startScan() {
        try {
            mWifiManager.startScan();
            mWifiList = mWifiManager.getScanResults();
            mWifiConfiguration = mWifiManager.getConfiguredNetworks();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    public void toggleMobileData(Context context, boolean flag) {
        
    }
    
    public WifiConfiguration isExsits(String s) {
        return null;
    }

    public int isWifiConnected(Context context) {
        return 0;
    }
}
