package com.zvin.wificonnect.wifi;

/**
 * Created by chenzhengwen on 2015/12/23.
 */
public interface WifiTask {
    void execute();
    WifiTask getNextTask();
    void setNextTask(WifiTask nextTask);
}
