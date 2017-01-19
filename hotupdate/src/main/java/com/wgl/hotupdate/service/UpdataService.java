package com.wgl.hotupdate.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;

import com.wgl.hotupdate.engine.UpdateEngine;
import com.wgl.hotupdate.listener.FileDownLoadListener;
import com.wgl.hotupdate.model.VersionInfo;
import com.wgl.hotupdate.utils.UpFileUtils;
import com.wgl.hotupdate.utils.UpLogUtils;
import com.wgl.hotupdate.utils.UpSharePerferenceHelper;
import com.wgl.hotupdate.utils.UpStringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.wgl.hotupdate.engine.UpdateEngine.CURRENT_VERSION_CODE;


public class UpdataService extends Service {
    public static String UPDATE_BROADCAST="android.intent.action.UPDATE_BROADCAST";
    public static String UPDATE_NOW="android.intent.action.UPDATE_NOW";
    public static String UPDATE_TIME="UPDATE_TIME";
    private boolean clearedCache;
    private BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            UpLogUtils.log("通知升级");
            Integer time=UpdateEngine.getHotUpdateConfig().getUpdateInteveral();
            if (time!=null){
                Long lastTime=UpSharePerferenceHelper.getLong(UpdataService.this,UPDATE_TIME);
                if ((new Date().getTime()-lastTime)>UpdateEngine.getHotUpdateConfig().getUpdateInteveral()){
                    requestUpdateVersion();
                }
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        System.out.println("update UpdataService onCreate");
        super.onCreate();
        clearedCache=false;
        //注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_BROADCAST);
        registerReceiver(broadcastReceiver, filter);

//        requestUpdateVersion();
    }

    @Override
    public void onDestroy() {
        System.out.println("update UpdataService onDestroy");
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    //更新到最新版本
    public void requestUpdateVersion() {
        UpLogUtils.log("请求版本信息");
        UpSharePerferenceHelper.saveLong(this,UPDATE_TIME,new Date().getTime());

        Request request = new Request.Builder().url(UpdateEngine.getHotUpdateConfig().getUpdateInfoUrl()).build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                UpLogUtils.log("下载失败:"+e.toString());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                VersionInfo versionInfo=new VersionInfo();
                String body=response.body().string();
                try {
                    JSONObject jsonObject=new JSONObject(body);
                    JSONObject data=jsonObject.getJSONObject("data");
                    versionInfo.setVersionCode(data.getInt("versionCode"));
                    versionInfo.setMd5Code(data.getString("md5Code"));
                    versionInfo.setFileCount(data.getInt("fileCount"));
                    versionInfo.setUpdateResult(data.getInt("updateResult"));
                    versionInfo.setUpdateStrategy(data.getInt("updateStrategy"));
                    versionInfo.setPatchUrl(data.getString("patchUrl"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

//        VersionInfo versionInfo=new VersionInfo();
//        versionInfo.setVersionCode(20161106);
//        versionInfo.setMd5Code("d86fe7a59b52423979e5e7cc182974d4");
//        versionInfo.setFileCount(9);
//        versionInfo.setUpdateResult(1);
//        versionInfo.setUpdateStrategy(1);
//        versionInfo.setPatchUrl("http://10.0.0.8:8080/src/build/20161106.zip");

                if (!checkUpdateInfo(versionInfo)){
                    return;
                }

                UpLogUtils.log("最新版本号"+versionInfo.getVersionCode());
                UpdateEngine.getHotUpdateConfig().setUpdateVersionInfo(versionInfo);
                if (UpdateEngine.getCurrentVersionCode()!=versionInfo.getVersionCode().intValue()){
                    UpLogUtils.log("启动升级");
                    startUpdate();
                }else {
                    deleteOtherVersion(UpdateEngine.getCurrentVersionCode()+"");
                }
            }
        });

        VersionInfo versionInfo= (VersionInfo) UpSharePerferenceHelper.getObject(this,"test");

        if (!checkUpdateInfo(versionInfo)){
            return;
        }

        UpLogUtils.log("最新版本号"+versionInfo.getVersionCode()+"当前版本号"+UpdateEngine.getCurrentVersionCode());
        UpdateEngine.getHotUpdateConfig().setUpdateVersionInfo(versionInfo);
        if (UpdateEngine.getCurrentVersionCode()!=versionInfo.getVersionCode().intValue()){
            UpLogUtils.log("启动升级");
            startUpdate();
        }else {
            deleteOtherVersion(UpdateEngine.getCurrentVersionCode()+"");
        }
    }

    private boolean checkUpdateInfo(VersionInfo versionInfo){
        if (versionInfo==null){
            UpLogUtils.log("versionInfo 空");
            return false;
        }

        if (UpStringUtils.isEmpty(versionInfo.getMd5Code())){
            UpLogUtils.log("缺少md5值");
            return false;
        }

        if (UpStringUtils.isEmpty(versionInfo.getPatchUrl())){
            UpLogUtils.log("缺少补丁url");
            return false;
        }

        if (versionInfo.getFileCount()==null||versionInfo.getFileCount()<=0){
            UpLogUtils.log("文件数量配置错误");
            return false;
        }

        if (versionInfo.getVersionCode()==null||versionInfo.getVersionCode()<=0){
            UpLogUtils.log("版本号配置错误");
            return false;
        }

        if (versionInfo.getUpdateStrategy()==null){
            UpLogUtils.log("升级策略配置错误");
            return false;
        }

        if (versionInfo.getUpdateResult()==null){
            UpLogUtils.log("升级命令配置错误");
            return false;
        }else if (versionInfo.getUpdateResult()==0){
            UpLogUtils.log("不更新新版本");
            return false;
        }

        return true;
    }

    private void startUpdate(){
        if (UpdateEngine.getHotUpdateConfig().getUpdateVersionInfo()==null){
            return;
        }

        downloadPatch(UpdateEngine.getHotUpdateConfig().getUpdateVersionInfo());
    }

    private void downloadPatch(final VersionInfo versionInfo){
        UpLogUtils.log("开始下载 版本："+versionInfo.getVersionCode());
        UpFileUtils.downloadFile(versionInfo.getPatchUrl(), UpFileUtils.getRootCachePath().toString(), versionInfo.getVersionCode()+".data", new FileDownLoadListener() {
            @Override
            public void onError(String errorInfo) {
                UpLogUtils.log(errorInfo);
                //TODO 上报错误
                return;
            }

            @Override
            public void onProgress(int progress, long total) {
                UpLogUtils.log("下载中："+progress+"/"+total);
            }

            @Override
            public void onSuccess(String filePath) {
                UpLogUtils.log("下载完成："+filePath);
                zipFile(versionInfo,filePath);
            }
        });
    }

    private void zipFile(final VersionInfo versionInfo, final String filePath){


        if (!checkMd5(versionInfo,filePath)) {
            return;
        }
        AsyncTask asyncTask=new AsyncTask<Object,Object,Integer>() {
            @Override
            protected Integer doInBackground(Object[] params) {
                int count=0;
                try {
                    count=UpFileUtils.UnZipFolder((String) params[0],UpdateEngine.getHotUpdateConfig().getJsPath());
                } catch (Exception e) {
                    e.printStackTrace();
                    //TODO 上报错误
                    UpLogUtils.log("解压出错");
                    return 0;
                }

                return count;
            }

            @Override
            protected void onPostExecute(Integer count) {
                UpLogUtils.log("解压文件数量："+count+" 源文件文件数量"+UpdateEngine.getHotUpdateConfig().getUpdateVersionInfo().getFileCount());
                if (count.intValue()==UpdateEngine.getHotUpdateConfig().getUpdateVersionInfo().getFileCount()){
                    onZipSuccess(versionInfo);
                }

            }
        };
        asyncTask.execute(filePath);

    }

    private boolean checkMd5(final VersionInfo versionInfo, final String filePath){
        //验证MD5
        String md5="";
        try {
            md5=UpFileUtils.getMd5ByFile(new File(filePath));
        } catch (FileNotFoundException e) {
            UpLogUtils.log("获取md5值发生错误");
            e.printStackTrace();
        }
        if (UpStringUtils.isEmpty(md5)){
            UpLogUtils.log("md5值为0");
            return false;
        }

        if (!UpStringUtils.equals(md5,versionInfo.getMd5Code())){
            UpLogUtils.log("md5不正确"+md5+"  "+versionInfo.getMd5Code());
            return false;
        }
        UpLogUtils.log("md5值:"+md5);
        return true;
    }

    private void onZipSuccess(VersionInfo versionInfo){
        UpLogUtils.log("解压成功，更新版本："+versionInfo.getVersionCode());
        UpSharePerferenceHelper.saveInt(this, CURRENT_VERSION_CODE,versionInfo.getVersionCode());

        if (versionInfo.getUpdateStrategy()==0){
            //重启更新

        }else if (versionInfo.getUpdateStrategy()==1){
            //TODO 立即更新
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle("完成更新,需要重新加载页面");
//            builder.setPositiveButton("重启", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                    // TODO: 16-11-10
//                }
//            }).create().show();
            Intent intent=new Intent(UPDATE_NOW);
            UpdateEngine.setCurrentVersionCode(versionInfo.getVersionCode());
            this.sendBroadcast(intent);
        }
    }

    private void deleteOtherVersion(final String version){
        UpLogUtils.log("清理缓存");
        Thread thread=new Thread(){
            @Override
            public void run() {
                super.run();
                deletePackageCache(version);
                deleteCache();
                UpLogUtils.log("清理缓存完成");
            }
        };
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    //清除历史更新包
    private void deletePackageCache(String elseFileName){
        File file=new File(UpdateEngine.getHotUpdateConfig().getJsPath());
        if (file.isDirectory()){
            File[] files = file.listFiles();
            for (int i = 0;i < files.length;i ++) {
                System.out.println("wode "+files[i].getName()+"  "+elseFileName);
                if (!files[i].getName().equals(elseFileName)){
                    UpFileUtils.deleteFile(files[i]);
                }
            }
        }
    }

    //清除缓存
    private void deleteCache(){
        UpFileUtils.deleteFile(new File(UpFileUtils.getRootCachePath().toString()));
    }
}
