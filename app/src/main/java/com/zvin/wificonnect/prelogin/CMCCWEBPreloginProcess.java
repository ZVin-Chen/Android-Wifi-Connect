package com.zvin.wificonnect.prelogin;

import com.zvin.wificonnect.migration.AuthenPortal;
import com.zvin.wificonnect.migration.PortalHttp;
import com.zvin.wificonnect.util.Const;
import com.zvin.wificonnect.util.LogUtil;

/**
 * Created by chenzhengwen on 2015/12/23.
 */
public class CMCCWEBPreloginProcess extends PreloginProcess {
    @Override
    public void execute() {
        LogUtil.i(LogUtil.DEBUG_TAG, "CMCCWEBPreloginProcess");
        AuthenPortal authenPortal = new AuthenPortal();
        int preloginResult = authenPortal.preLogin(new PortalHttp(), Const.JUDGE_ROAMING);
        LogUtil.i(LogUtil.DEBUG_TAG, "prelogin result=" + preloginResult);
    }
}
