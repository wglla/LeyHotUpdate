package com.wgl.hotupdate.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.wgl.hotupdate.engine.UpdateEngine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 处理 存储SharePerference里的工具类
 */
public class UpSharePerferenceHelper {

    private static final String DEFAULT_STRING_VALUE = "NOVALUE";

    public static void saveInt(Context context,String key, int value) {
        SharedPreferences sp = context
                .getSharedPreferences(UpdateEngine.JS_PATCH, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void saveLong(Context context,String key, long value) {
        SharedPreferences sp = context
                .getSharedPreferences(UpdateEngine.JS_PATCH, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(key, value);
        editor.commit();
    }


    public static long getLong(Context context,String key) {
        SharedPreferences sp = context
                .getSharedPreferences(UpdateEngine.JS_PATCH, Context.MODE_PRIVATE);
        long value = sp.getLong(key, 0l);
        return value;
    }

    public static int getInt(Context context,String key) {
        SharedPreferences sp = context
                .getSharedPreferences(UpdateEngine.JS_PATCH, Context.MODE_PRIVATE);
        int value = sp.getInt(key, 0);
        return value;
    }


    public static String getString(Context context,String key) {
        SharedPreferences sp = context
                .getSharedPreferences(UpdateEngine.JS_PATCH, Context.MODE_PRIVATE);
        String value = sp.getString(key, DEFAULT_STRING_VALUE);
        if (DEFAULT_STRING_VALUE.equals(value)) {
            return null;
        }
        return value;
    }

    // 保存对象
    public static boolean saveObject(Context context, String key, Object object) {
        SharedPreferences sp = context.getSharedPreferences("MyAppName",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (object == null) {
            editor.remove(key);
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(object);
                editor.putString(
                        key,
                        new String(Base64.encode(bos.toByteArray(),
                                Base64.DEFAULT)));
                editor.commit();
                bos.close();
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static Object getObject(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("MyAppName",
                Context.MODE_PRIVATE);

        String value = sp.getString(key, DEFAULT_STRING_VALUE);
        if (DEFAULT_STRING_VALUE.equals(value)) {
            return null;
        } else {
            ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decode(
                    value.getBytes(), Base64.DEFAULT));
            try {
                ObjectInputStream ois = new ObjectInputStream(bis);
                Object object = ois.readObject();
                ois.close();
                bis.close();
                return object;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
