package com.zvin.wificonnect.util;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by chenzhengwen on 2015/12/16.
 */
public class WLANUtil {
    public static final String WPA_PSK = "PSK";
    public static final String WEP = "WEP";
    public static final String WPA_PEAP = "EAP";
    public static final String OPEN = "Open";

    public static final String CMCC_WEB = "CMCC-WEB";
    public static final String CMCC = "CMCC";
    public static final String CMCC_FREE = "CMCC-FREE";
    public static final String CMCC_EDU = "CMCC-EDU";

    /**
     * @return The security of a given {@link ScanResult}.
     */
    public static String getScanResultSecurity(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] securityModes = { WEP, WPA_PSK, WPA_PEAP};
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }

        return OPEN;
    }


    public static String getWifiConfigurationSecurity(WifiConfiguration wifiConfig) {

        if (!TextUtils.isEmpty(wifiConfig.preSharedKey)) {
            return WPA_PSK;
        } else if (!TextUtils.isEmpty(wifiConfig.wepKeys[0])) {
            return WEP;
        }

        if(Build.VERSION.SDK_INT >= 18){
            String identity = wifiConfig.enterpriseConfig.getIdentity();
            String passwd = wifiConfig.enterpriseConfig.getPassword();
            int eapMethod = wifiConfig.enterpriseConfig.getEapMethod();
            int phas2Method = wifiConfig.enterpriseConfig.getPhase2Method();

            if(eapMethod == 0 && !TextUtils.isEmpty(identity) && !TextUtils.isEmpty(passwd)){
                return WPA_PEAP;
            }
        }else {
            Object eapMethod = getEnterpriseFieldValue(wifiConfig, "eap");
            Object identity = getEnterpriseFieldValue(wifiConfig, "identity");
            Object passwd = getEnterpriseFieldValue(wifiConfig, "password");

            if(eapMethod != null && "PEAP".equals(eapMethod.toString())
                    && identity != null
                    && !TextUtils.isEmpty(identity.toString())
                    && passwd != null
                    && !TextUtils.isEmpty(passwd.toString())){
                return WPA_PEAP;
            }
        }
        return OPEN;
    }

    public static Object getFieldValue(Object obj, String fieldname) {
        Class<?> classz = obj.getClass();
        Field field = null;
        Object retValue = null;
        try {
            field = classz.getField(fieldname);
            field.setAccessible(true);
            retValue = field.get(obj);
        } catch (NoSuchFieldException ex) {
            LogUtil.e(LogUtil.ERROR_TAG, "getFieldValue " + ex + " " + ex.getMessage());
        } catch (Exception ex) {
            LogUtil.e(LogUtil.ERROR_TAG, "getFieldValue "+ ex+" "+ ex.getMessage());
        }
        return retValue;
    }

    public static Object callMethod(Object obj, String methodname, Class<?> types[], Object values[]) {
        Class<?> classz = obj.getClass();
        Method method = null;
        Object retValue = null;
        try {
            method = classz.getMethod(methodname, types);
            retValue = method.invoke(obj, values);
        } catch (NoSuchMethodException ex) {
            LogUtil.e(LogUtil.ERROR_TAG, "callMethod "+ ex +" "+ ex.getMessage());
        } catch (Exception ex) {
            LogUtil.e(LogUtil.ERROR_TAG, "callMethod "+ ex +" "+ ex.getMessage());
        }
        return retValue;
    }

    public static String getSSIDWithoutQuotes(String ssid){
        if(ssid.startsWith("\"") && ssid.endsWith("\"")){
            ssid = ssid.substring(1, ssid.length() - 1);
        }

        return ssid;
    }

    public static boolean isHex(String key) {
        for (int i = key.length() - 1; i >= 0; i--) {
            final char c = key.charAt(i);
            if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f')) {
                return false;
            }
        }

        return true;
    }

    public static boolean isHexWepKey(String wepKey) {
        final int len = wepKey.length();

        // WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
        if (len != 10 && len != 26 && len != 58) {
            return false;
        }

        return isHex(wepKey);
    }

    public static void setEnterpriseFieldValue(WifiConfiguration config, String field, String value) {
        Object enterpriseConfig = getFieldValue(config, field);
        if (enterpriseConfig != null) {
            callMethod(enterpriseConfig, "setValue",
                    new Class[] {String.class}, new Object[] {value});
        }
    }

    public static Object getEnterpriseFieldValue(WifiConfiguration config, String field){
        Object enterpriseConfig = getFieldValue(config, field);
        if(enterpriseConfig != null){
            return callMethod(enterpriseConfig, "value", null, null);
        }

        return null;
    }

    public static WifiConfiguration getSavedNetworkByScanResult(Context context, ScanResult scanResult){
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> wifiConfigurationList = wifiManager.getConfiguredNetworks();

        if(wifiConfigurationList == null || wifiConfigurationList.size() == 0){
            return null;
        }

        String tempSSID = "\"" + scanResult.SSID + "\"";
        String tempSecure = getScanResultSecurity(scanResult);
        for(WifiConfiguration config : wifiConfigurationList){
            String wifiSecure = getWifiConfigurationSecurity(config);
            if(config.SSID.equals(tempSSID) && wifiSecure.equals(tempSecure)){
                return config;
            }
        }
        return null;
    }

}
