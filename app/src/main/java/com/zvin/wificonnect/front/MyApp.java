package com.zvin.wificonnect.front;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;

import com.zvin.wificonnect.IWifiObserveService;
import com.zvin.wificonnect.service.WifiObserveService;
import com.zvin.wificonnect.util.LogUtil;

/**
 * Created by chenzhengwen on 2015/12/15.
 */
public class MyApp extends Application {
    private IWifiObserveService mService;
    private Messenger mMessengerFromService;
    private boolean mBound = false;

    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.i(LogUtil.DEBUG_TAG, "MyApp onServiceConnected");
            mService = IWifiObserveService.Stub.asInterface(service);
            try {
                mMessengerFromService = mService.getMessenger();
            } catch (RemoteException e) {
                LogUtil.i(LogUtil.ERROR_TAG, e.getMessage());
            }
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if(mBound){
                mService = null;
                mBound = false;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Intent startServerIntent = new Intent(this, WifiObserveService.class);
        startService(startServerIntent);

        startServerIntent.setAction(WifiObserveService.ACTUAL_SERVICE);
        bindService(startServerIntent, mConn, 0);
    }

    public IWifiObserveService getService(){
        return mService;
    }

    public Messenger getServiceMessenger(){
        return mMessengerFromService;
    }
}
