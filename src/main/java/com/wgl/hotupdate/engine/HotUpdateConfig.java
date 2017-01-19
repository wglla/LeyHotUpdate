package com.wgl.hotupdate.engine;
import com.wgl.hotupdate.model.VersionInfo;

/**
 * Created by wgl on 16-11-3.
 */

public final class HotUpdateConfig {
    private Integer updateInteveral;
    private String updateInfoUrl;
    private String jsPath;

    //最新获取的补丁版本
    private VersionInfo updateVersionInfo;

    public VersionInfo getUpdateVersionInfo() {
        return updateVersionInfo;
    }

    public void setUpdateVersionInfo(VersionInfo updateVersionInfo) {
        this.updateVersionInfo = updateVersionInfo;
    }

    public String getJsPath() {
        return jsPath;
    }

    public void setJsPath(String jsPath) {
        this.jsPath = jsPath;
    }

    public Integer getUpdateInteveral() {
        return updateInteveral;
    }

    public String getUpdateInfoUrl() {
        return updateInfoUrl;
    }

    public static class Build{
        public Build(){

        }

        //每隔多少时间请求一次升级信息
        private Integer updateInteveral =3*60*60*1000;

        //更新信息后台接口
        private String updateInfoUrl;

        //js版本保存地址
        private String jsPath;

        public Build setJsVersionPath(String jsPath) {
            this.jsPath = jsPath;
            return this;
        }

        public Build setUpdateInfoUrl(String updateInfoUrl) {
            this.updateInfoUrl = updateInfoUrl;
            return this;
        }

        public Build setUpdateInteveral(Integer updateInteveral) {
            this.updateInteveral = updateInteveral;
            return this;
        }

        public HotUpdateConfig build(){
            HotUpdateConfig hotUpdateConfig= new HotUpdateConfig();
            hotUpdateConfig.updateInteveral = updateInteveral;
            hotUpdateConfig.updateInfoUrl=updateInfoUrl;
            hotUpdateConfig.jsPath=jsPath;
            return hotUpdateConfig;
        }
    }
}
