package com.zvin.wificonnect.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.zvin.wificonnect.model.CMCCEntity;
import com.zvin.wificonnect.model.CMCCKeyValueList;

/**
 * Created by chenzhengwen on 2015/12/28.
 */
public class SharedPreferenceUtil {
    public static final String CMCC_ENTITY = "cmcc_entity";
    public static final String CMCC_ENTITY_LIST = "cmcc_entity_list";
    public static final int SET_PREF_VALUE = 1;
    public static final int SET_PREF_VALUE_LIST = 2;

    public static final String MAIN_PREF = "main_pref";

    public static void setSharedPref(Context context, CMCCEntity pref){
        SharedPreferences.Editor editor = context.getSharedPreferences(MAIN_PREF, Context.MODE_PRIVATE).edit();

        Object value = pref.getValue();
        if(value != null){
            if(value instanceof Integer){
                editor.putInt(pref.getKey(), (Integer)value);
            }else if(value instanceof String){
                editor.putString(pref.getKey(), (String)value);
            }else if(value instanceof Boolean){
                editor.putBoolean(pref.getKey(), (Boolean) value);
            }else if(value instanceof Long){
                editor.putLong(pref.getKey(), (Long) value);
            }else if(value instanceof Float){
                editor.putFloat(pref.getKey(), (Float) value);
            }
        }

        editor.commit();
    }

    public static void setSharedPrefList(Context context, CMCCKeyValueList valueList){
        if(valueList == null || valueList.getUpdateList().size() == 0)
            return;

        SharedPreferences.Editor editor = context.getSharedPreferences(MAIN_PREF, Context.MODE_PRIVATE).edit();

        for(CMCCEntity pref : valueList.getUpdateList()){
            Object value = pref.getValue();
            if(value != null){
                if(value instanceof Integer){
                    editor.putInt(pref.getKey(), (Integer)value);
                }else if(value instanceof String){
                    editor.putString(pref.getKey(), (String)value);
                }else if(value instanceof Boolean){
                    editor.putBoolean(pref.getKey(), (Boolean) value);
                }else if(value instanceof Long){
                    editor.putLong(pref.getKey(), (Long) value);
                }else if(value instanceof Float){
                    editor.putFloat(pref.getKey(), (Float) value);
                }
            }
        }

        editor.commit();
    }

    public static Bundle getSharedPref(Context context, String key, String cls){
        SharedPreferences preferences = context.getSharedPreferences(MAIN_PREF, Context.MODE_PRIVATE);
        Bundle bundle = new Bundle();
        CMCCEntity entity = new CMCCEntity();
        entity.setKey(key);
        Object val = null;
        if(cls.equals(Integer.class.getName())){
            val = preferences.getInt(key, 0);
        }else if(cls.equals(String.class.getName())){
            val = preferences.getString(key, "");
        }else if(cls.equals(Long.class.getName())){
            val = preferences.getLong(key, 0);
        }else if(cls.equals(Float.class.getName())){
            val = preferences.getFloat(key, 0f);
        }else if(cls.equals(Boolean.class.getName())){
            val = preferences.getBoolean(key, false);
        }
        entity.setValue(val);
        bundle.putSerializable(CMCC_ENTITY, entity);
        return bundle;
    }
}
