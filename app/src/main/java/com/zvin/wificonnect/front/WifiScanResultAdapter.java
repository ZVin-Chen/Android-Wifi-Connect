package com.zvin.wificonnect.front;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zvin.wificonnect.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenzhengwen on 2015/12/16.
 */
public class WifiScanResultAdapter extends BaseAdapter {
    private Context mContext;
    private List<ScanResult> mScanResults = new ArrayList<ScanResult>();

    public WifiScanResultAdapter(Context context){
        this.mContext = context;
    }

    public WifiScanResultAdapter(Context context, List<ScanResult> scanResultList){
        this.mContext = context;
        this.mScanResults = scanResultList;
        notifyDataSetChanged();
    }

    public void setmScanResults(List<ScanResult> scanResultList){
        this.mScanResults = scanResultList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mScanResults != null ? mScanResults.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mScanResults != null ? mScanResults.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if(convertView == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.item_wifi_scan_result, parent, false);

            holder = new ViewHolder();
            holder.ssid = (TextView)view.findViewById(R.id.ssid);
            holder.capability = (TextView)view.findViewById(R.id.capability);
            view.setTag(holder);
        }else{
            view = convertView;
            holder = (ViewHolder)view.getTag();
        }

        ScanResult rs = mScanResults.get(position);
        holder.ssid.setText(rs.SSID);
        holder.capability.setText(rs.capabilities);

        return view;
    }

    static class ViewHolder{
        TextView ssid;
        TextView capability;
    }
}
