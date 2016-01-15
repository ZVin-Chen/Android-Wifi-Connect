package com.zvin.wificonnect.util;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.zvin.wificonnect.connection.CMCCWEBWifiConnection;
import com.zvin.wificonnect.connection.OpenWifiConnection;
import com.zvin.wificonnect.connection.WEPWifiConnection;
import com.zvin.wificonnect.connection.WPAEAPWifiConnection;
import com.zvin.wificonnect.connection.WPAPSKWifiConnection;
import com.zvin.wificonnect.connection.WifiConnnection;
import com.zvin.wificonnect.wifi.WifiTask;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chenzhengwen on 2015/12/16.
 */
public class WifiHelper {
    private static WifiHelper mWifiHelper;
    private Context mContext;
    private ExecutorService mExecutors;

    private Integer mWifiConnectLock = 0;
    private WifiManager mWifiManager;

    private WifiHelper(Context context){
        mContext = context;
        mExecutors = Executors.newFixedThreadPool(3);
    }

    public static WifiHelper getInstance(Context context){
        if(mWifiHelper == null){
            mWifiHelper = new WifiHelper(context);
        }
        return mWifiHelper;
    }

    public boolean setLock(){
        synchronized (mWifiConnectLock){
            LogUtil.i(LogUtil.DEBUG_TAG, "setLock method mWifiConnectLock=" + mWifiConnectLock);
            if(mWifiConnectLock > 0){
                return false;
            }else{
                LogUtil.i(LogUtil.DEBUG_TAG, "setLock");
                mWifiConnectLock = 1;
                return true;
            }
        }
    }

    public boolean releaseLock(){
        synchronized (mWifiConnectLock){
            LogUtil.i(LogUtil.DEBUG_TAG, "releaseLock method mWifiConnectLock=" + mWifiConnectLock);
            if(mWifiConnectLock > 0){
                LogUtil.i(LogUtil.DEBUG_TAG, "releaseLock");
                mWifiConnectLock = 0;
                return true;
            }else{
                return false;
            }
        }
    }

    public WifiConnnection getConncetionByScanResult(ScanResult scanResult){
        WifiConnnection conn = null;
        String authSecure = WLANUtil.getScanResultSecurity(scanResult);
        LogUtil.i(LogUtil.DEBUG_TAG, "getConncetionByScanResult authSecure=" + authSecure);
        if(WLANUtil.OPEN.equals(authSecure)){

            if(WLANUtil.CMCC_WEB.equals(scanResult.SSID)){
                conn = new CMCCWEBWifiConnection(mContext, scanResult);
            }else{
                conn = new OpenWifiConnection(mContext, scanResult);
            }

            return conn;
        }

        if(WLANUtil.WPA_PSK.equals(authSecure)){
            conn = new WPAPSKWifiConnection(mContext, scanResult, null);
            return conn;
        }

        if(WLANUtil.WEP.equals(authSecure)){
            conn = new WEPWifiConnection(mContext, scanResult, null);
            return conn;
        }

        if(WLANUtil.WPA_PEAP.equals(authSecure)){
            conn = new WPAEAPWifiConnection(mContext, scanResult, null, null);
            return conn;
        }

        return null;
    }

    public void setConfig(WifiConnnection conn, String account, String passwd){
        if(conn == null)
            return;

        if(conn instanceof WPAPSKWifiConnection){
            ((WPAPSKWifiConnection) conn).setPasswd(passwd);
            return;
        }

        if(conn instanceof WPAEAPWifiConnection){
            ((WPAEAPWifiConnection) conn).setIdentity(account);
            ((WPAEAPWifiConnection) conn).setPasswd(passwd);
            return;
        }

        if(conn instanceof WEPWifiConnection){
            ((WEPWifiConnection) conn).setPasswd(passwd);
            return;
        }
    }

    public List<ScanResult> getScanResults(){
        if(mWifiManager == null){
            mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        }

        List<ScanResult> results = mWifiManager.getScanResults();

        return results;
    }

    public void executeConnection(final WifiConnnection conn){
        mExecutors.execute(new Runnable() {
            @Override
            public void run() {
                conn.connect();
            }
        });
    }

    public void executeWifiTask(final WifiTask task){
        mExecutors.execute(new Runnable() {
            @Override
            public void run() {
                task.execute();
            }
        });
    }
}
