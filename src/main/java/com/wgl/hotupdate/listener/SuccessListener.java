package com.wgl.hotupdate.listener;

/**
 * Created by wgl on 16-11-8.
 */

public interface SuccessListener {
    void onSuccess(String msg);
    void onFailed(String msg);
}
