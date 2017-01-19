package com.wgl.hotupdate.model;

import java.io.Serializable;

/**
 * Created by wgl on 16-11-4.
 */

public class VersionInfo implements Serializable{
    /**
     * 请求操作
     * 1 更新新版本
     * 0 不更新
     */
    private Integer updateResult;
    ///版本号和md5值和文件信息。
    private Integer versionCode;//版本号
    private String md5Code;//Md5
    private Integer fileCount;//js文件数量
    private String patchUrl;//补丁Url地址

    /**
     * 更新策略
     * 0 重启更新
     * 1 立即更新
     */
    private Integer updateStrategy;

    public Integer getUpdateResult() {
        return updateResult;
    }

    public void setUpdateResult(Integer updateResult) {
        this.updateResult = updateResult;
    }

    public Integer getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(Integer versionCode) {
        this.versionCode = versionCode;
    }

    public String getMd5Code() {
        return md5Code;
    }

    public void setMd5Code(String md5Code) {
        this.md5Code = md5Code;
    }

    public Integer getFileCount() {
        return fileCount;
    }

    public void setFileCount(Integer fileCount) {
        this.fileCount = fileCount;
    }

    public String getPatchUrl() {
        return patchUrl;
    }

    public void setPatchUrl(String patchUrl) {
        this.patchUrl = patchUrl;
    }

    public Integer getUpdateStrategy() {
        return updateStrategy;
    }

    public void setUpdateStrategy(Integer updateStrategy) {
        this.updateStrategy = updateStrategy;
    }
}
