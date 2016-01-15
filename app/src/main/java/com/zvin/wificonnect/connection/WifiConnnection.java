package com.zvin.wificonnect.connection;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.RemoteException;

import com.zvin.wificonnect.IWifiObserveService;
import com.zvin.wificonnect.front.MyApp;
import com.zvin.wificonnect.prelogin.PreloginProcess;
import com.zvin.wificonnect.util.LogUtil;
import com.zvin.wificonnect.util.WLANUtil;
import com.zvin.wificonnect.util.WifiHelper;
import com.zvin.wificonnect.wifi.WifiConnectCallback;
import com.zvin.wificonnect.wifi.WifiStatus;

/**
 * Created by chenzhengwen on 2015/12/15.
 * this is a class specific a connection to wifi
 * */
public abstract class WifiConnnection {
    protected int mNetworkId;
    protected WifiManager mWifiManager;
    private Context mContext;
    private ScanResult mScanResult;
    protected WifiConfiguration mWifiConfig;

    private WifiConnectCallback mConnectCallback;
    private IWifiObserveService mService;
    private int mRetryTime = 0;

    public void setConnectCallback(WifiConnectCallback mConnectCallback) {
        this.mConnectCallback = mConnectCallback;
    }

    public Context getContext() {
        return mContext;
    }

    public ScanResult getScanResult() {
        return mScanResult;
    }

    public WifiConnnection(Context context, ScanResult scanResult){
        mContext = context;
        mScanResult = scanResult;
        mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        mNetworkId = -1;

        mService = ((MyApp)mContext.getApplicationContext()).getService();
    }

    /**
     * when execute, the connection to the wifi will be establish
     * #should contain retry mechanism#
     * */
    public void connect(){
        if(mConnectCallback == null)
            throw new IllegalStateException("the WifiConnectCallback should not be null, please check and try again!");

        boolean locked = false;
        boolean needClearRequest = false;
        WifiStatus wifiStatus = null;
        try {
//            locked = mService.setWifiConnectLock();
            locked = WifiHelper.getInstance(mContext).setLock();

            if(!locked){
                mConnectCallback.waitConnect();
                return;
            }
            mRetryTime = 0;
            mWifiConfig = WLANUtil.getSavedNetworkByScanResult(mContext, mScanResult);

            int rs = prepare();

            if (rs == 2) {
                mConnectCallback.needAuth();
                return;
            }

            if (rs == 4) {
                mConnectCallback.fail();
                return;
            }

            mConnectCallback.connecting();

            wifiStatus = mService.getWifiStatus();
            if (wifiStatus != null) {
                //if current wifi has connected
                if (NetworkInfo.State.CONNECTED.equals(wifiStatus.state) && mScanResult.SSID.equals(wifiStatus.SSID)) {
                    LogUtil.i(LogUtil.DEBUG_TAG, "WifiConnection current wifi is connected!");
                    mConnectCallback.alreadyConnected();
                    return;
                }

                wifiStatus.requestSSID = mScanResult.SSID;
                wifiStatus.requestBSSID = mScanResult.BSSID;
                wifiStatus.requestCapabilities = mScanResult.capabilities;
                wifiStatus.requestTime = System.currentTimeMillis();

                mService.setWifiStatus(wifiStatus);
                needClearRequest = true;
            } else {
                mConnectCallback.fail();
                return;
            }

            LogUtil.i(LogUtil.DEBUG_TAG, "WifiConnnection connect");

            if (mNetworkId == -1) {
                mConnectCallback.fail();
                return;
            }

            wifiStatus = mService.getWifiStatus();
            String ssid = getScanResult().SSID;
            NetworkInfo.State tempState = wifiStatus.state;

            LogUtil.i(LogUtil.DEBUG_TAG, "WifiConnection requestSSID=" + wifiStatus.requestSSID);

            if(!ssid.equals(wifiStatus.SSID)){
                mWifiManager.disconnect();
                boolean result = mWifiManager.enableNetwork(mNetworkId, true);
                mWifiManager.reconnect();
                if(result){
                    mConnectCallback.completed();

                    while(mRetryTime < getRetryTime()){
                        LogUtil.i(LogUtil.DEBUG_TAG, "WifiConnection mRetryTime=" + mRetryTime);
                        Thread.sleep(3000);

                        wifiStatus = mService.getWifiStatus();
                        if(wifiStatus != null && NetworkInfo.State.CONNECTED.equals(wifiStatus.state)
                                && wifiStatus.requestSSID.equals(wifiStatus.SSID)){

                            LogUtil.i(LogUtil.DEBUG_TAG, "WifiConnection connect succeed!");
                            mConnectCallback.succeed(wifiStatus.SSID);
                            return;
                        }

                        LogUtil.i(LogUtil.DEBUG_TAG, "WifiConnection connect fail and retry again");
                        boolean result2 = mWifiManager.enableNetwork(mNetworkId, true);
                        mWifiManager.reconnect();
                        mRetryTime++;
                    }

//                    LogUtil.i(LogUtil.DEBUG_TAG, "remove config network networkId=" + mNetworkId);
//                    removeWifiConfig(mNetworkId);
//                    if(needAuth()){
//                        mConnectCallback.needAuth();
//                    }

                    mConnectCallback.fail();
                }else{
                    mConnectCallback.fail();
                }
                return;
            }

            if(!NetworkInfo.State.CONNECTED.equals(tempState)){
                mWifiManager.disconnect();
                boolean result = mWifiManager.enableNetwork(mNetworkId, true);
                mWifiManager.reconnect();
                if(result){
                    mConnectCallback.completed();

                    while(mRetryTime < 2){
                        LogUtil.i(LogUtil.DEBUG_TAG, "WifiConnection mRetryTime=" + mRetryTime);
                        Thread.sleep(3000);

                        wifiStatus = mService.getWifiStatus();
                        if(wifiStatus != null && NetworkInfo.State.CONNECTED.equals(wifiStatus.state)
                                && wifiStatus.requestSSID.equals(wifiStatus.SSID)){

                            LogUtil.i(LogUtil.DEBUG_TAG, "WifiConnection connect succeed!");
                            mConnectCallback.succeed(wifiStatus.SSID);
                            return;
                        }

                        LogUtil.i(LogUtil.DEBUG_TAG, "WifiConnection connect fail and retry again");
                        boolean result2 = mWifiManager.enableNetwork(mNetworkId, true);
                        mWifiManager.reconnect();
                        mRetryTime++;
                    }

//                    LogUtil.i(LogUtil.DEBUG_TAG, "remove config network networkId=" + mNetworkId);
//                    removeWifiConfig(mNetworkId);
//                    if(needAuth()){
//                        mConnectCallback.needAuth();
//                    }
                }else{
                    mConnectCallback.fail();
                }

                return;
            }

            return;
        } catch (RemoteException e) {
            LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
            mConnectCallback.fail();
        }catch (InterruptedException e){
            LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
            mConnectCallback.fail();
        }finally {
            try {
                if(locked){
//                    mService.releaseWifiConnectLock();
                    WifiHelper.getInstance(mContext).releaseLock();
                }
                if(needClearRequest){
                    clearRequestWifiStatus();
                }
            } catch (RemoteException e) {
                LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
            }
        }
    }

    public void removeWifiConfig(int netId){
        mWifiManager.removeNetwork(netId);
    }

    public void clearRequestWifiStatus() throws RemoteException{
        mService.clearRequestWifiStatus();
    }

    public PreloginProcess getPreloginProcess(){
        return null;
    }

    /**
     * prepare for the {@link WifiConfiguration}
     *
     * @return 1 for ok, 2 for need auth, 4 for unknow failure
     * */
    public abstract int prepare();

    /**
     * get suggest retry time
     * */
    public abstract int getRetryTime();

    /**
     * check if need auth
     * */
    public abstract boolean needAuth();

    /**
     * Created by chenzhengwen on 2015/12/15.
     * this enum indicate the connect status of the wifi
     */
    public enum WifiConnectStatus {
        /**
         * wifi is disconnected
         * */
        DISCONNECTED,

        /**
         * wifi is disconnecting
         * */
        DISCONNECTING,

        /**
         * wifi is connecting
         * */
        CONNECTING,

        /**
         * wifi is connected
         * */
        CONNECTED
    }
}
