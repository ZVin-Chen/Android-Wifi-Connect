// IWifiObserver.aidl
package com.zvin.wificonnect;

oneway interface IWifiObserver {
    void wifiConnected();
    void wifiDisconnected();

    void wifiEnabled();
    void wifiDisabled();

    void scanFinished(boolean isSucceed);
}
