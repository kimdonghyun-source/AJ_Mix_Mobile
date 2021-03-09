package kr.co.ajcc.wms.common;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedData {
    public static final String SHARED_NAME = "AJmWMS";

    //아이디 저장 여부, 아이디, 비밀번호
    public enum UserValue{
        IS_LOGIN, USER_ID, SAVE_ID, EMP_CODE
    }

    public static boolean setSharedData(Context context, String strKey, Object objData){
        return setSharedData(context, SHARED_NAME, strKey, objData);
    }

    public static boolean setSharedData(Context context, String strPrefName, String strKey, Object objData){
        SharedPreferences prefs = context.getSharedPreferences(strPrefName, Activity.MODE_PRIVATE);

        if(prefs == null || strKey == null || objData == null){
            return false;
        }

        SharedPreferences.Editor ed = prefs.edit();
        if(Boolean.class == objData.getClass()){
            ed.putBoolean(strKey, (Boolean)objData);
        }
        else if(Integer.class == objData.getClass()){
            ed.putInt(strKey, (Integer)objData);
        }
        else if(Long.class == objData.getClass()){
            ed.putLong(strKey, (Long)objData);
        }
        else if(Float.class == objData.getClass()){
            ed.putFloat(strKey, (Float)objData);
        }
        else if(String.class == objData.getClass()){
            ed.putString(strKey, (String)objData);
        }
        else{
            return false;
        }
        return ed.commit();
    }

    public static Object getSharedData(Context context, String strKey, Object objData){
        SharedPreferences prefs = context.getSharedPreferences(SHARED_NAME, Activity.MODE_PRIVATE);

        if(prefs == null || strKey == null){
            return null;
        }

        Object objReturn = null;

        if(Boolean.class == objData.getClass()){
            objReturn = new Boolean(prefs.getBoolean(strKey, (Boolean)objData));
        }
        else if(Integer.class == objData.getClass()){
            objReturn = new Integer(prefs.getInt(strKey, (Integer)objData));
        }
        else if(Long.class == objData.getClass()){
            objReturn = new Long(prefs.getLong(strKey, (Long)objData));
        }
        else if(Float.class == objData.getClass()){
            objReturn = new Float(prefs.getFloat(strKey, (Float)objData));
        }
        else if(String.class == objData.getClass()){
            objReturn = prefs.getString(strKey, (String)objData);
        }
        else{
            return null;
        }

        return objReturn;
    }
}
