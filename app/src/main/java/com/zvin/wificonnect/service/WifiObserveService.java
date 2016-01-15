package com.zvin.wificonnect.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.zvin.wificonnect.IWifiObserveService;
import com.zvin.wificonnect.IWifiObserver;
import com.zvin.wificonnect.model.CMCCEntity;
import com.zvin.wificonnect.model.CMCCKeyValueList;
import com.zvin.wificonnect.util.LogUtil;
import com.zvin.wificonnect.util.SharedPreferenceUtil;
import com.zvin.wificonnect.wifi.WifiStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenzhengwen on 2015/12/15.
 */
public class WifiObserveService extends Service {
    public static final String ACTUAL_SERVICE = "actual_service";
    public static final String MESSENGER_SERVICE = "messenger_service";

    private final String NETWOR_DETAILSTATE_TAG = "network_detailstate--->";

    private WifiStatus mWifiStatus;
    private boolean mIsInit = false;

    private WifiManager mWifiManager;
    private WifiObserveReceiver mWifiObserveReceiver;

    private Integer mWifiConnectLock = 0;

    private Map<String, IWifiObserver> mWifiStateObservers = new HashMap<String, IWifiObserver>();

    final Messenger mMessenger = new Messenger(new ServicePreferenceHandler());

    private Context mPrefCtx;

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (i < 1000){

                    try {
                        //use messenger
                        Message sendMsg = Message.obtain(null, SharedPreferenceUtil.SET_PREF_VALUE);
                        Bundle bundle = new Bundle();
                        CMCCEntity entity = new CMCCEntity("testkey", i);
                        bundle.putSerializable(SharedPreferenceUtil.CMCC_ENTITY, entity);

                        sendMsg.setData(bundle);

                        LogUtil.i(LogUtil.DEBUG_TAG, Thread.currentThread().getId() + " service--->preferences.edit() testkey ---> " + i);
                        mMessenger.send(sendMsg);
                        i++;
                        Thread.sleep(600);
                    } catch (RemoteException e) {
                        LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
                    }catch (InterruptedException e){
                        LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
                    }
                }
            }
        }).start();

        /*try {
            mPrefCtx = createPackageContext("com.zvin.wificonnect", Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
        }*/

        /*new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferences = mPrefCtx.getSharedPreferences(SharedPreferenceUtil.MAIN_PREF, Context.MODE_PRIVATE);
                if(preferences == null){
                    LogUtil.e(LogUtil.ERROR_TAG, "mServiceCtx SharedPreferences is null");
                    return;
                }

                int i = 0;
                while(i < 1000){
                    LogUtil.i(LogUtil.DEBUG_TAG, Thread.currentThread().getId() + " service--->preferences.edit() testkey ---> " + i);
                    preferences.edit().putInt("testkey", i++).commit();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
                    }
                }
            }
        }).start();*/

        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferences = WifiObserveService.this.getSharedPreferences(SharedPreferenceUtil.MAIN_PREF, Context.MODE_PRIVATE);
                if(preferences == null){
                    LogUtil.e(LogUtil.ERROR_TAG, "mServiceCtx SharedPreferences is null");
                    return;
                }

                int i = 0;
                while(i < 1000){
                    int value = preferences.getInt("testkey", 0);
                    LogUtil.i(LogUtil.DEBUG_TAG, Thread.currentThread().getId() + " service--->preferences.getInt() testkey ---> " + value);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
                    }
                    i++;
                }
            }
        }).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if(ACTUAL_SERVICE.equals(intent.getAction())){
            return mBinder;
        }else if(MESSENGER_SERVICE.equals(intent.getAction())){
            return mMessenger.getBinder();
        }

        return null;
    }

    private final IWifiObserveService.Stub mBinder = new IWifiObserveService.Stub() {
        @Override
        public WifiStatus getWifiStatus() throws RemoteException {
            return getStatus();
        }

        @Override
        public void addWifiObserver(String tag, IWifiObserver observer) throws RemoteException {
            addObserver(tag, observer);
        }

        @Override
        public void removeWifiObserver(String tag) throws RemoteException {
            removeObserver(tag);
        }

        @Override
        public void setWifiStatus(WifiStatus wifiStatus) throws RemoteException {
            setStatus(wifiStatus);
        }

        @Override
        public void clearRequestWifiStatus() throws RemoteException{
            clearStatus();
        }

        @Override
        public boolean setWifiConnectLock() throws RemoteException{
//            return setLock();
            return false;
        }

        @Override
        public boolean releaseWifiConnectLock() throws RemoteException{
//            return releaseLock();
            return false;
        }

        @Override
        public Bundle getSharedPref(String key, String clsName) throws RemoteException {
            return getSharedPreference(key, clsName);
        }

        @Override
        public Messenger getMessenger() throws RemoteException{
            return mMessenger;
        }
    };


    private WifiStatus getStatus(){
        return mWifiStatus;
    }

    private void setStatus(WifiStatus wifiStatus){
        synchronized (mWifiStatus){
            this.mWifiStatus = wifiStatus;
        }
    }

    private synchronized Bundle getSharedPreference(String key, String clsName){
        return SharedPreferenceUtil.getSharedPref(WifiObserveService.this, key, clsName);
    }

    private void clearStatus(){
        synchronized (mWifiStatus){
            this.mWifiStatus.clearRequest();
        }
    }

    public void addObserver(String tag, IWifiObserver observer){
        synchronized (mWifiStateObservers){
            mWifiStateObservers.put(tag, observer);
        }
    }

    public void removeObserver(String tag){
        synchronized (mWifiStateObservers){
            mWifiStateObservers.remove(tag);
        }
    }

    /*private boolean setLock(){
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
    }*/

    /*public boolean releaseLock(){
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
    }*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.i(LogUtil.DEBUG_TAG, "WifiObserveService onStartCommand");
        doInit();
        return Service.START_STICKY;
    }

    private synchronized void doInit(){
        //already initialize
        if(mIsInit)
            return;

//        CrashHandler.getInstance().init(this);
        mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        if(mWifiStatus == null){
            mWifiStatus = new WifiStatus();
        }

        LogUtil.i(LogUtil.DEBUG_TAG, "WifiObserveService doInit");

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        if(mWifiObserveReceiver == null){
            mWifiObserveReceiver = new WifiObserveReceiver();
        }

        this.registerReceiver(mWifiObserveReceiver, filter);

        mIsInit = true;
    }

    //sync the data and keep in the right state
    public class WifiObserveReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.i(LogUtil.DEBUG_TAG, "WifiObserveReceiver onReceive action=" + action);

            if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)){
                handleWifiScanAvailAction(intent);
            }

            if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)){
                handleNetworkAction(intent);
            }

            if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)){
                handleWifiStateAction(intent);
            }
        }
    }

    //handle wifi scan result action
    public void handleWifiScanAvailAction(Intent intent){
        boolean isScanSucceed = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
        onWifiScanFinished(isScanSucceed);
    }

    //handle wifi state change action
    public void handleWifiStateAction(Intent intent){
        int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
        switch (wifiState){
            case WifiManager.WIFI_STATE_ENABLED:
                onWifiEnabled();
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                onWifiDisabled();
                break;
        }
    }

    //handle network state change action
    public void handleNetworkAction(Intent intent){
        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

        if(networkInfo != null && ConnectivityManager.TYPE_WIFI == networkInfo.getType()){
            handleWifiNetworkAction(networkInfo, intent);
        }
    }

    //handle wifi network state action
    public void handleWifiNetworkAction(NetworkInfo networkInfo, Intent intent){
        WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
        mWifiStatus.state = networkInfo.getState();
        mWifiStatus.detailedState = networkInfo.getDetailedState();

        LogUtil.i(NETWOR_DETAILSTATE_TAG, "detail " + networkInfo.getDetailedState().name());
        LogUtil.i(NETWOR_DETAILSTATE_TAG, "state " + networkInfo.getState().name());


        if(NetworkInfo.State.CONNECTED.equals(networkInfo.getState()) && wifiInfo != null){
            mWifiStatus.copyFromWifiInfo(wifiInfo);

            LogUtil.i(NETWOR_DETAILSTATE_TAG, "ssid=" + wifiInfo.getSSID());
            onWifiConnected();
        }

        if(NetworkInfo.State.DISCONNECTED.equals(networkInfo.getState())){
            mWifiStatus.clear();
            onWifiDisconnected();
        }
    }

    private void onWifiScanFinished(boolean isSucceed){
        synchronized (mWifiStateObservers){
            for(IWifiObserver observer : mWifiStateObservers.values()){
                try {
                    observer.scanFinished(isSucceed);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void onWifiEnabled(){
        synchronized (mWifiStateObservers){
            for(IWifiObserver observer : mWifiStateObservers.values()){
                try {
                    observer.wifiEnabled();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void onWifiDisabled(){
        synchronized (mWifiStateObservers){
            for(IWifiObserver observer : mWifiStateObservers.values()){
                try {
                    observer.wifiDisabled();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void onWifiConnected(){
        synchronized (mWifiStateObservers){
            for(IWifiObserver observer : mWifiStateObservers.values()){
                try {
                    observer.wifiConnected();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void onWifiDisconnected(){
        synchronized (mWifiStateObservers){
            for(IWifiObserver observer : mWifiStateObservers.values()){
                try {
                    observer.wifiDisconnected();
                } catch (RemoteException e) {
                    LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mWifiObserveReceiver != null){
            try {
                unregisterReceiver(mWifiObserveReceiver);
            }catch (Exception e){
                LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
            }
        }
    }

    public class ServicePreferenceHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle;

            switch (msg.what){
                case SharedPreferenceUtil.SET_PREF_VALUE:
                    bundle = msg.getData();
                    CMCCEntity entity = (CMCCEntity)bundle.getSerializable(SharedPreferenceUtil.CMCC_ENTITY);

//                    LogUtil.i(LogUtil.DEBUG_TAG, "ServicePreferenceHandler SET_PREF_VALUE " + entity.getKey() + " ---> " + entity.getValue());
                    SharedPreferenceUtil.setSharedPref(WifiObserveService.this, entity);
                    break;
                case SharedPreferenceUtil.SET_PREF_VALUE_LIST:
                    bundle = msg.getData();
                    CMCCKeyValueList valueList = (CMCCKeyValueList)bundle.getSerializable(SharedPreferenceUtil.CMCC_ENTITY_LIST);
//                    LogUtil.i(LogUtil.DEBUG_TAG, "ServicePreferenceHandler SET_PREF_VALUE_LIST size=" + valueList.getUpdateList().size());
                    SharedPreferenceUtil.setSharedPrefList(WifiObserveService.this, valueList);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

}
