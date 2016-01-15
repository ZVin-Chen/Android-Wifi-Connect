// IWifiObserveService.aidl
package com.zvin.wificonnect;
import com.zvin.wificonnect.wifi.WifiStatus;
import com.zvin.wificonnect.IWifiObserver;
import android.os.Messenger;

interface IWifiObserveService {
    void setWifiStatus(in WifiStatus wifiStatus);
    WifiStatus getWifiStatus();
    void clearRequestWifiStatus();
    //add wifi observer for callback event
    void addWifiObserver(String tag, in IWifiObserver observer);
    //remove wifi observer
    void removeWifiObserver(String tag);

    //useless for temporary
    boolean setWifiConnectLock();
    //useless for temporary
    boolean releaseWifiConnectLock();

    //get SharedPreference value from Service process
    Bundle getSharedPref(String key, String clsName);

    //get messenger from service
    Messenger getMessenger();
}
