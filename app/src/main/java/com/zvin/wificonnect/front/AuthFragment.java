package com.zvin.wificonnect.front;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.zvin.wificonnect.R;
import com.zvin.wificonnect.util.WLANUtil;

/**
 * Created by chenzhengwen on 2015/12/21.
 */
public class AuthFragment extends DialogFragment implements View.OnClickListener{
    private EditText mAccount;
    private EditText mPasswd;
    private Button mConfirm;
    private String mTitle;
    private String mSecurity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mTitle = args.getString("ssid");
        mSecurity = args.getString("wifi_security");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.white);
        getDialog().setCanceledOnTouchOutside(true);
//        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setTitle(mTitle);

        View view = inflater.inflate(R.layout.fragment_auth, container, false);
        mAccount = (EditText)view.findViewById(R.id.account);
        mPasswd = (EditText)view.findViewById(R.id.passwd);
        mConfirm = (Button)view.findViewById(R.id.confirm);
        mConfirm.setOnClickListener(this);

        if(WLANUtil.WPA_PEAP.equals(mSecurity)){
            mAccount.setVisibility(View.VISIBLE);
        }else{
            mAccount.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        if(getActivity() instanceof AuthCallback){
            this.dismiss();
            ((AuthCallback)getActivity()).onConfirm(mAccount.getText().toString(), mPasswd.getText().toString());
        }else{
            throw new IllegalStateException("Parent Activity should implements AuthCallback interface!");
        }
    }

    public interface AuthCallback{
        void onConfirm(String account, String passwd);
    }
}
