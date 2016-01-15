package com.zvin.wificonnect.prelogin;

import com.zvin.wificonnect.wifi.WifiTask;

/**
 * Created by chenzhengwen on 2015/12/23.
 */
public abstract class PreloginProcess implements WifiTask{

   public abstract void execute();

    @Override
    public WifiTask getNextTask() {
        return null;
    }

    @Override
    public void setNextTask(WifiTask nextTask) {

    }
}
