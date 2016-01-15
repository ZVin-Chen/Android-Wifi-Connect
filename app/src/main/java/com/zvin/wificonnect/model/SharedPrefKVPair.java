package com.zvin.wificonnect.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by chenzhengwen on 2015/12/29.
 */
public class SharedPrefKVPair<T> implements Parcelable {
    private String key;
    private T value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public SharedPrefKVPair(String key, T value){
        this.key = key;
        this.value = value;
    }

    protected SharedPrefKVPair(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in){
        key = in.readString();
    }

    public static final Creator<SharedPrefKVPair> CREATOR = new Creator<SharedPrefKVPair>() {
        @Override
        public SharedPrefKVPair createFromParcel(Parcel in) {
            return new SharedPrefKVPair(in);
        }

        @Override
        public SharedPrefKVPair[] newArray(int size) {
            return new SharedPrefKVPair[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
