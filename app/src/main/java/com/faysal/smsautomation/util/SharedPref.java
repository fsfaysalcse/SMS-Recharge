package com.faysal.smsautomation.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;

    public static void putString(Context context, String Key, String Value) {
        sharedPreferences = context.getSharedPreferences("Cache", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString(Key, Value);
        editor.commit();

    }

    public static String getString(Context contextGetKey, String Key) {
        sharedPreferences = contextGetKey.getSharedPreferences("Cache", Context.MODE_PRIVATE);
        String Value = sharedPreferences.getString(Key, null);
        return Value;

    }


    public static void putlong(Context context, String Key, long Value) {
        sharedPreferences = context.getSharedPreferences("Cache", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putLong(Key, Value);
        editor.commit();

    }

    public static long getlong(Context contextGetKey, String Key) {
        sharedPreferences = contextGetKey.getSharedPreferences("Cache", Context.MODE_PRIVATE);
        long Value = sharedPreferences.getLong(Key, 5);
        return Value;

    }

    public static void putBoolean(Context context, String Key, Boolean Value) {
        sharedPreferences = context.getSharedPreferences("Cache", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putBoolean(Key, Value);
        editor.commit();

    }

    public static Boolean getBoolean(Context contextGetKey, String Key) {
        sharedPreferences = contextGetKey.getSharedPreferences("Cache", Context.MODE_PRIVATE);
        Boolean Value = sharedPreferences.getBoolean(Key, false);
        return Value;

    }

    public static void clearSharedPreferences(Context context)
    {
        sharedPreferences = context.getSharedPreferences("Cache", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().commit();
    }


}
