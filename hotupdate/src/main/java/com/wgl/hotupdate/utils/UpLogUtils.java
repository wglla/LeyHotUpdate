package com.wgl.hotupdate.utils;

import android.content.Intent;
import android.util.Log;

import com.wgl.hotupdate.engine.UpdateEngine;

/**
 * Created by wgl on 16-11-1.
 */

public class UpLogUtils {
    public static final String UPDATE_LOG ="update_log";

    public static void log(String log){

        Log.d(UPDATE_LOG,log);

        //test
        UpdateEngine.Log=UpdateEngine.Log+"\n"+log;
        Intent intent=new Intent("myaction");
        UpdateEngine.context.sendBroadcast(intent);
    }
}
