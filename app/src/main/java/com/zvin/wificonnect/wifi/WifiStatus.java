package com.zvin.wificonnect.wifi;

import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.os.Parcel;
import android.os.Parcelable;

import com.zvin.wificonnect.util.WLANUtil;

/**
 * Created by chenzhengwen on 2015/12/15.
 * this class contains the informations about the current wifi, such as SSID, ip, DNS address, gateway
 */
public class WifiStatus implements Parcelable{
    public String SSID;
    public String BSSID;
    public String capabilities;
    public NetworkInfo.State state;
    public int networkid = -1;
    public NetworkInfo.DetailedState detailedState;
    public String requestSSID;
    public String requestBSSID;
    public String requestCapabilities;
    public long requestTime;

    public WifiStatus(){}

    private WifiStatus(Parcel in) {
        readFromParcel(in);
    }

    public static final Creator<WifiStatus> CREATOR = new Creator<WifiStatus>() {
        @Override
        public WifiStatus createFromParcel(Parcel in) {
            return new WifiStatus(in);
        }

        @Override
        public WifiStatus[] newArray(int size) {
            return new WifiStatus[size];
        }
    };

    public WifiStatus copyFromWifiInfo(WifiInfo wifiInfo){
        if(wifiInfo == null)
            return this;

        SSID = WLANUtil.getSSIDWithoutQuotes(wifiInfo.getSSID());
        BSSID = wifiInfo.getBSSID();
        networkid = wifiInfo.getNetworkId();
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(SSID);
        dest.writeString(BSSID);
        dest.writeString(capabilities);
        dest.writeString(state != null ? state.name() : null);
        dest.writeInt(networkid);
        dest.writeString(detailedState != null ? detailedState.name() : null);
        dest.writeString(requestSSID);
        dest.writeString(requestBSSID);
        dest.writeString(requestCapabilities);
        dest.writeLong(requestTime);
    }

    public void readFromParcel(Parcel in){
        SSID = in.readString();
        BSSID = in.readString();
        capabilities = in.readString();

        String tempState = in.readString();
        state = tempState != null ? NetworkInfo.State.valueOf(tempState) : null;
        networkid = in.readInt();
        String tempDetailState = in.readString();
        detailedState = tempDetailState != null ? NetworkInfo.DetailedState.valueOf(tempDetailState) : null;
        requestSSID = in.readString();
        requestBSSID = in.readString();
        requestCapabilities = in.readString();
        requestTime = in.readLong();
    }

    public void clear(){
        SSID = null;
        BSSID = null;
        capabilities = null;
        networkid = -1;
    }

    public void clearRequest(){
        requestSSID = null;
        requestBSSID = null;
        requestCapabilities = null;
        requestTime = 0;
    }
}
