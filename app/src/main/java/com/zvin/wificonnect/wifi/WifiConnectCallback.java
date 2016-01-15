package com.zvin.wificonnect.wifi;

/**
 * Created by chenzhengwen on 2015/12/15.
 */
public interface WifiConnectCallback {

    /**
     * wificonfiguration is null, need passwd
     * */
    void needAuth();

    /**
     * current wifi already connected
     * */
    void alreadyConnected();

    /**
     * unknow exception
     * */
    void fail();

    /**
     * wifi connect completed
     * */
    void completed();

    /**
     * wifi is connecting
     * */
    void connecting();

    /**
     * wifi connect succeed
     * */
    void succeed(String ssid);

    /**
     * wifi connect waiting
     * */
    void waitConnect();
}
