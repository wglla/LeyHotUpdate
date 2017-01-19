package com.wgl.hotupdate.listener;

/**
 * Created by diql on 2016/10/26.
 */

public interface FileDownLoadListener {
    void onError(String errorInfo);
    void onProgress(int progress, long total);
    void onSuccess(String path);
}
