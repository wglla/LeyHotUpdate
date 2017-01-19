package com.wgl.hotupdate.engine;

import android.content.Context;
import android.content.Intent;


import com.wgl.hotupdate.service.UpdataService;
import com.wgl.hotupdate.utils.UpLogUtils;
import com.wgl.hotupdate.utils.UpSharePerferenceHelper;

import static com.wgl.hotupdate.service.UpdataService.UPDATE_BROADCAST;

/**
 * Created by wgl on 16-11-4.
 */

public class UpdateEngine {
    public static String Log="";

    //常量
    public static final String JS_PATCH ="JS_PATCH";
    public static final String CURRENT_VERSION_CODE ="CURRENT_VERSION_CODE";

    //变量
    private static int currentVersionCode=-1;//当前版本号

    private static HotUpdateConfig hotUpdateConfig;
    public static Context context;

    public static int getCurrentVersionCode() {
        return currentVersionCode;
    }

    public static HotUpdateConfig getHotUpdateConfig() {
        return hotUpdateConfig;
    }

    public static void setCurrentVersionCode(int currentVersionCode) {
        UpdateEngine.currentVersionCode = currentVersionCode;
    }

    public static String getJsRootDirectory() {
        return UpdateEngine.getHotUpdateConfig().getJsPath()+UpdateEngine.getCurrentVersionCode()+"/";
    }

    public static void initialize(Context context, HotUpdateConfig config){
        hotUpdateConfig=config;
        UpdateEngine.context=context;

        Integer version= UpSharePerferenceHelper.getInt(context, CURRENT_VERSION_CODE);
        if (version>0){
            currentVersionCode=version;
        }

        UpLogUtils.log("当前版本号"+currentVersionCode);
        runService();
    }

    private static void runService() {
        if (context==null)return;
        Intent intent=new Intent(context, UpdataService.class);
        context.startService(intent);
    }

    public static void checkUpdate(){
        Intent intent=new Intent(UPDATE_BROADCAST);
        context.sendBroadcast(intent);
    }

    public static void restore(){
        UpdateEngine.currentVersionCode=-1;
        UpSharePerferenceHelper.saveInt(context, CURRENT_VERSION_CODE,-1);
    }
}
