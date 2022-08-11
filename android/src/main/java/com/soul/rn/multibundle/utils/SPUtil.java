package com.soul.rn.multibundle.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtil {
    public static void putString(Context context, String key, String content) {
        SharedPreferences sp = context.getSharedPreferences("RNMultiBundle",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key,content);
        editor.commit();
    }

    public static String getString(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("RNMultiBundle", Context .MODE_PRIVATE);
        return sharedPreferences.getString(key,"");
    }
}
