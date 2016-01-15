package com.zvin.wificonnect.front;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zvin.wificonnect.IWifiObserveService;
import com.zvin.wificonnect.IWifiObserver;
import com.zvin.wificonnect.R;
import com.zvin.wificonnect.connection.WifiConnnection;
import com.zvin.wificonnect.model.CMCCEntity;
import com.zvin.wificonnect.service.WifiObserveService;
import com.zvin.wificonnect.util.Const;
import com.zvin.wificonnect.util.LogUtil;
import com.zvin.wificonnect.util.SharedPreferenceUtil;
import com.zvin.wificonnect.util.WLANUtil;
import com.zvin.wificonnect.util.WifiHelper;
import com.zvin.wificonnect.wifi.WifiConnectCallback;

import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AuthFragment.AuthCallback, WifiConnectCallback{
    private final String TAG = "MainActivity--->";
    private ListView mWifiScanResults;
    private WifiManager mWifiManager;
    private WifiScanResultAdapter mAdapter;
    private WifiConnnection mWifiConn;
    private IWifiObserveService mService;
    private boolean mBound = false;

    private IWifiObserver.Stub mWifiObserver;

    private ViewGroup mCurrnetWifi;
    private TextView mCurrentSSID;
    private TextView mCurrentWifiState;

    private final int CONNECTING = 3;
    private final int COMPLETED = 4;

    private final int FAIL = -1;
    private final int NEED_AUTH = 5;
    private final int SUCCEED = 6;
    private final int ALREADY_CONNECTED = 7;
    private final int WAIT_CONNECT = 8;

    private final int WIFI_ENABLED = 9;
    private final int WIFI_DISABLED = 10;

    private final int WIFI_SCAN_FINISHED = 11;

    public static boolean mStartTest = true;
    private Messenger mServiceMessenger;

    private Context mServiceCtx;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case WAIT_CONNECT:
                    LogUtil.i(LogUtil.DEBUG_TAG, "wifi connect wait");
                    break;
                case NEED_AUTH:
                    LogUtil.i(LogUtil.DEBUG_TAG, "wifi connect need auth");
                    AuthFragment auth = new AuthFragment();

                    Bundle args = new Bundle();
                    args.putString("ssid", mWifiConn.getScanResult().SSID);
                    args.putString("wifi_security", WLANUtil.getScanResultSecurity(mWifiConn.getScanResult()));
                    auth.setArguments(args);

                    auth.show(getSupportFragmentManager(), AuthFragment.class.getSimpleName());
                    break;
                case SUCCEED:
                    LogUtil.i(LogUtil.DEBUG_TAG, "MainActivity wifi connect succeed!");
                    break;
                case ALREADY_CONNECTED:
                    try {
                        LogUtil.i(LogUtil.DEBUG_TAG, "MainActivity wifi already connected!");
                        mCurrnetWifi.setVisibility(View.VISIBLE);
                        mCurrentSSID.setText(mService.getWifiStatus().SSID);
                        mCurrentWifiState.setText("已连接");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case FAIL:
                    LogUtil.i(LogUtil.DEBUG_TAG, "wifi connect fail");
                    Toast.makeText(MainActivity.this, "wifi 连接失败，请重试", Toast.LENGTH_SHORT).show();
                    mCurrnetWifi.setVisibility(View.GONE);
                    break;
                case COMPLETED:
                    LogUtil.i(LogUtil.DEBUG_TAG, "wifi connect completed");
                    mCurrnetWifi.setVisibility(View.VISIBLE);
                    mCurrentSSID.setText(mWifiConn.getScanResult().SSID);
                    break;
                case CONNECTING:
                    mCurrnetWifi.setVisibility(View.VISIBLE);
                    mCurrentSSID.setText(mWifiConn.getScanResult().SSID);
                    mCurrentWifiState.setText("连接中...");
                    break;
                case 1:
                    mCurrnetWifi.setVisibility(View.VISIBLE);
                    try {
                        String ssid = mService.getWifiStatus().SSID;
                        mCurrentSSID.setText(ssid);

                        if(mWifiConn != null && mWifiConn.getPreloginProcess() != null){
                            WifiHelper.getInstance(MainActivity.this).executeWifiTask(mWifiConn.getPreloginProcess());
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    mCurrentWifiState.setText("已连接");
                    break;
                case 0:
                    mCurrnetWifi.setVisibility(View.GONE);
                    break;
                case WIFI_DISABLED:
                    mCurrnetWifi.setVisibility(View.GONE);
                    mAdapter.setmScanResults(null);
                    break;
                case WIFI_SCAN_FINISHED:
                    List<ScanResult> results = WifiHelper.getInstance(MainActivity.this).getScanResults();
                    mAdapter.setmScanResults(results);
                    break;
            }
        }
    };

    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IWifiObserveService.Stub.asInterface(service);
            mBound = true;

            try {
                LogUtil.i(TAG, "addWifiObserver");
                mService.addWifiObserver(Const.MAIN_OBSERBER_TAG, mWifiObserver);

                checkCurrentWifi();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    private void findViews(){
        mWifiScanResults = (ListView)findViewById(R.id.wifi_scan_results);
        mCurrnetWifi = (ViewGroup)findViewById(R.id.current_wifi);
        mCurrentSSID = (TextView)findViewById(R.id.current_ssid);
        mCurrentWifiState = (TextView)findViewById(R.id.current_wifi_state);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            mServiceCtx = createPackageContext("com.zvin.wificonnect", Context.CONTEXT_IGNORE_SECURITY);
            LogUtil.i(LogUtil.DEBUG_TAG, "serviceCtx=" + mServiceCtx);
        } catch (PackageManager.NameNotFoundException e) {
            LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
        }
        findViews();

        mWifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        mAdapter = new WifiScanResultAdapter(this);
        mWifiScanResults.setAdapter(mAdapter);

        mWifiObserver = new IWifiObserver.Stub() {
            @Override
            public void wifiConnected() throws RemoteException {
                LogUtil.i(TAG, "wifiConnected");

                mHandler.sendEmptyMessage(1);
            }

            @Override
            public void wifiDisconnected() throws RemoteException {
                LogUtil.i(TAG, "wifiDisconnected");
                mHandler.sendEmptyMessage(0);
            }

            @Override
            public void wifiEnabled() throws RemoteException {
                LogUtil.i(TAG, "wifiEnabled");
            }

            @Override
            public void wifiDisabled() throws RemoteException {
                LogUtil.i(TAG, "wifiDisabled");
                mHandler.sendEmptyMessage(WIFI_DISABLED);
            }

            @Override
            public void scanFinished(boolean isSucceed) throws RemoteException {
                LogUtil.i(TAG, "scanFinished isSucceed=" + isSucceed);
                mHandler.sendEmptyMessage(WIFI_SCAN_FINISHED);
            }
        };

        mWifiScanResults.setOnItemClickListener(this);
        Intent bindService = new Intent(this, WifiObserveService.class);
        bindService.setAction(WifiObserveService.ACTUAL_SERVICE);
        bindService(bindService, mConn, 0);

        mServiceMessenger = ((MyApp)getApplicationContext()).getServiceMessenger();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED){
            List<ScanResult> resultList = WifiHelper.getInstance(this).getScanResults();
            mAdapter.setmScanResults(resultList);

            checkCurrentWifi();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ScanResult rs = (ScanResult)mAdapter.getItem(position);
        LogUtil.i(LogUtil.DEBUG_TAG, "onItemClick ssid=" + rs.SSID + ", capabilities=" + rs.capabilities);

        mWifiConn = WifiHelper.getInstance(this).getConncetionByScanResult(rs);

        mWifiConn.setConnectCallback(this);
//        mWifiConn.connect();
        WifiHelper.getInstance(this).executeConnection(mWifiConn);
    }


    @Override
    public void onConfirm(String account, String passwd) {
        WifiHelper.getInstance(this).setConfig(mWifiConn, account, passwd);
//        mWifiConn.connect();
        WifiHelper.getInstance(this).executeConnection(mWifiConn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBound){
            try {
                LogUtil.i(TAG, "removeWifiObserver");
                mService.removeWifiObserver(Const.MAIN_OBSERBER_TAG);
                unbindService(mConn);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void checkCurrentWifi(){
       if(mService != null){
           try {
               String ssid = mService.getWifiStatus().SSID;
               NetworkInfo.State state = mService.getWifiStatus().state;

               if (NetworkInfo.State.CONNECTED.equals(state) && !TextUtils.isEmpty(ssid)) {
                   mCurrnetWifi.setVisibility(View.VISIBLE);
                   mCurrentSSID.setText(ssid);
                   mCurrentWifiState.setText("已连接");
               } else {
                   mCurrnetWifi.setVisibility(View.GONE);
               }
           } catch (RemoteException e) {
               Log.e(LogUtil.ERROR_TAG, e.getMessage());
           }
       }
    }

    @Override
    public void needAuth() {
       mHandler.sendEmptyMessage(NEED_AUTH);
    }

    @Override
    public void alreadyConnected() {
        mHandler.sendEmptyMessage(ALREADY_CONNECTED);
    }

    @Override
    public void fail() {
//        Message msg = Message.obtain(mHandler, FAIL, flag, 0);
        mHandler.sendEmptyMessage(FAIL);
//        mHandler.sendMessage(msg);
    }

    @Override
    public void completed() {
        mHandler.sendEmptyMessage(COMPLETED);
    }

    @Override
    public void connecting() {
        mHandler.sendEmptyMessage(CONNECTING);
    }

    @Override
    public void succeed(String ssid) {
        mHandler.sendEmptyMessage(SUCCEED);
    }

    @Override
    public void waitConnect() {
        mHandler.sendEmptyMessage(WAIT_CONNECT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.disconnect_current_wifi:
                mWifiManager.disconnect();
                Toast.makeText(this, "disconnect current wifi", Toast.LENGTH_SHORT).show();
               return true;
            case R.id.start_scan_wifi:
                mWifiManager.startScan();
                Toast.makeText(this, "start scan wifi", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.pref_test:

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int i = 0;
                        while (mStartTest){

                            try {
                                //use aidl
                                /*Bundle bundle = SharedPreferenceUtil.converToBundle("testkey", i++);
                                LogUtil.i(LogUtil.DEBUG_TAG, " thread id=" + Thread.currentThread().getId() + " setSharedPref testkey--->" + i);
                                mService.setSharedPref(bundle);*/

                                //use messenger
                                Message sendMsg = Message.obtain(null, SharedPreferenceUtil.SET_PREF_VALUE);
                                Bundle bundle = new Bundle();
                                CMCCEntity entity = new CMCCEntity("testkey", i);
                                bundle.putSerializable(SharedPreferenceUtil.CMCC_ENTITY, entity);

                                sendMsg.setData(bundle);

                                if(mServiceMessenger == null){
                                    mServiceMessenger = ((MyApp)getApplicationContext()).getServiceMessenger();
                                }
                                LogUtil.i(LogUtil.DEBUG_TAG, Thread.currentThread().getId() + " main--->preferences.edit() testkey ---> " + i);
                                mServiceMessenger.send(sendMsg);
                                i++;
                                Thread.sleep(500);
                            } catch (RemoteException e) {
                                LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
                            }catch (InterruptedException e){
                                LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
                            }
                        }
                    }
                }).start();

                /*new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int i = 0;
                        while (mStartTest){

                            try {
                                //use aidl
                               *//* Bundle bundle = SharedPreferenceUtil.converToBundle("testkey", i++);
                                LogUtil.i(LogUtil.DEBUG_TAG, " thread id=" + Thread.currentThread().getId() + " setSharedPref testkey--->" + i);
                                mService.setSharedPref(bundle);*//*

                                //use messenger
                                Message sendMsg = Message.obtain(null, SharedPreferenceUtil.SET_PREF_VALUE);
                                Bundle bundle = new Bundle();
                                CMCCEntity entity = new CMCCEntity("testkey", i++);
                                bundle.putSerializable(SharedPreferenceUtil.CMCC_ENTITY, entity);

                                sendMsg.setData(bundle);

                                if(mServiceMessenger == null){
                                    mServiceMessenger = ((MyApp)getApplicationContext()).getServiceMessenger();
                                }
                                mServiceMessenger.send(sendMsg);
                                Thread.sleep(1000);
                            } catch (RemoteException e) {
                                LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
                            }catch (InterruptedException e){
                                LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
                            }
                        }
                    }
                }).start();*/

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (mStartTest){

                            try {
                                Bundle bundle = mService.getSharedPref("testkey", Integer.class.getName());
                                CMCCEntity entity = (CMCCEntity)bundle.getSerializable(SharedPreferenceUtil.CMCC_ENTITY);
                                int value = (Integer)entity.getValue();
                                LogUtil.i(LogUtil.DEBUG_TAG, Thread.currentThread().getId() + " main--->preferences.getInt() testkey ---> " + value);
                                Thread.sleep(1000);
                            } catch (RemoteException e) {
                                LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
                            }catch (InterruptedException e){
                                LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
                            }
                        }
                    }
                }).start();

                /*new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int i = 0;
                        while (mStartTest){

                            try {
                                Bundle bundle = mService.getSharedPref("testkey", Integer.class.getName());
                                CMCCEntity entity = (CMCCEntity)bundle.getSerializable(SharedPreferenceUtil.CMCC_ENTITY);
                                int value = (Integer)entity.getValue();
                                LogUtil.i(LogUtil.DEBUG_TAG, "thread id " + Thread.currentThread().getId() + " getIntPref testkey--->" + value);
                                Thread.sleep(200);
                            } catch (RemoteException e) {
                                LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
                            }catch (InterruptedException e){
                                LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
                            }
                        }
                    }
                }).start();*/


                /*new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences preferences = mServiceCtx.getSharedPreferences(SharedPreferenceUtil.MAIN_PREF, Context.MODE_PRIVATE);
                        if(preferences == null){
                            LogUtil.e(LogUtil.ERROR_TAG, "mServiceCtx SharedPreferences is null");
                            return;
                        }

                        int i = 0;
                        while(mStartTest){
                            LogUtil.i(LogUtil.DEBUG_TAG, Thread.currentThread().getId() + " main--->preferences.edit() testkey ---> " + i);
                            preferences.edit().putInt("testkey", i++).commit();
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
                            }
                        }
                    }
                }).start();*/

                /*new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences preferences = mServiceCtx.getSharedPreferences(SharedPreferenceUtil.MAIN_PREF, Context.MODE_PRIVATE);
                        if(preferences == null){
                            LogUtil.e(LogUtil.ERROR_TAG, "mServiceCtx SharedPreferences is null");
                            return;
                        }

                        while(mStartTest){
                            int value = preferences.getInt("testkey", 0);
                            LogUtil.i(LogUtil.DEBUG_TAG, Thread.currentThread().getId() + " main--->preferences.getInt() testkey ---> " + value);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
                            }
                        }
                    }
                }).start();*/
                return true;
            case R.id.pref_test_switch:
                if(mStartTest){
                    mStartTest = false;
                }else{
                    mStartTest = true;
                }
                return true;
            case R.id.pref_current:
                int value = 0;
                try {
                    Bundle bundle = mService.getSharedPref("testkey", Integer.class.getName());
                    CMCCEntity entity = (CMCCEntity)bundle.getSerializable(SharedPreferenceUtil.CMCC_ENTITY);
                    value = (Integer)entity.getValue();
                } catch (RemoteException e) {
                    LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
                }
                LogUtil.i(LogUtil.DEBUG_TAG, "MainActivity getIntPref testkey=" + value);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
