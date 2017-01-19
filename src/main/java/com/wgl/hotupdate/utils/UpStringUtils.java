package com.wgl.hotupdate.utils;

import android.content.ContentResolver;
import android.content.res.Resources;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 杨观回 2015/4/27
 *         <p/>
 *         字符串的一些操作 比如：判读是否为空，是否为手机号..
 */
public class UpStringUtils {
    public static final int SMSCODE_LENGTH = 6;

    public static final int PWD_MIN_LENGTH = 6;

    public static final int PWD_MAX_LENGTH = 12;

    public static final String FILE_URI_PREFX = "file://";

    /**
     * @return
     * @author ygh 2015/4/27
     * <p/>
     * 是否为手机号
     */
    public static boolean isMobileNO(String mobiles) {
        if (mobiles == null) {
            return false;
        }
        Pattern p = Pattern.compile("^(1[3578])[0-9]{9}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    /**
     * @author ygh 2015/4/28
     * <p/>
     * 验证码长度
     */
    public static boolean isSmsCode(String smsCode) {
        if (smsCode != null) {
            return true;
        }
        return false;
    }

    /**
     * @author ygh 2015/4/28
     * <p/>
     * 验证密码长度
     */
    public static boolean verifyPwd(String pwd) {
        if (pwd != null && pwd.length() >= PWD_MIN_LENGTH) {
            return true;
        }
        return false;
    }

    /**
     * 截取url中的imagekey
     */
    public static String cropImageKey(String imageKey) {
        //如果imagekey为http开头，则取其中的imgKey
        String temp1 = imageKey;
        int end = temp1.lastIndexOf("?");
        if (end < 0) {
            end = imageKey.length();
        }
        String temp2 = temp1.substring(0, end);
        int start = temp2.lastIndexOf("/");
        if (start < 0) {
            start = -1;
        }
        return temp2.substring(start + 1, end);
    }


    /**
     * @author ygh 2015/4/28
     * <p/>
     * 判断非空
     */
    public static boolean isEmpty(String str) {
        if (str == null || "".equals(str.trim()) || "null".equals(str.trim())||"nullnull".equals(str.trim())) {
            return true;
        }
        return false;
    }

    public static int stringToInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * @param str
     * @param split
     * @param index
     * @return
     * @author ygh 2015/5/13 取一段字符串
     */
    public static String getSplitString(String str, String split, int index) {
        String[] strArr = str.split(split);
        if (index < 0) {
            if (strArr.length + index < 0) {
                return "";
            } else {
                return strArr[strArr.length + index];
            }
        } else {
            return strArr[index];
        }
    }

    public static String removeFilePrefix(String path) {
        if (!isEmpty(path) && path.startsWith(FILE_URI_PREFX)) {
            return path.substring(FILE_URI_PREFX.length());
        }
        return null;
    }

    /**
     * 转换图片大小 地址
     */
    public static String changeImageParam(String url, String param) {
        if (url != null) {
            StringBuilder sb = new StringBuilder();
            if (url.contains("?")) {
                sb.append(url.substring(0, url.indexOf("?")));
            } else {
                sb.append(url);
            }
            if (param != null) {
                sb.append(param);
            }
            return sb.toString();
        }
        return null;
    }

    /**
     * 字符串 后面的数+1
     */
    public static String changeNum(String str, int flag) {
        String strs[] = str.split(" ");
        String text = Integer.parseInt(strs[1]) + (flag == 1 ? 1 : -1) + "";
        return strs[0] + " " + text;
    }

    /**
     * 字符串 去掉数字 加上参数
     */
    public static String changeText(String str, String num) {
        String strs[] = str.split(" ");
        return strs[0] + " " + num;
    }

    /**
     * 字符串 去掉数字 加上参数
     */
    public static String changeText(String str, long num) {
        String strs[] = str.split(" ");
        return strs[0] + " " + num;
    }

    public static String trim(String name) {
        return name.trim();
    }

    // 去掉http请求？后面的字符串, 返回带？
    public static String removeHttpSuffix(String imgUrl) {
        if (imgUrl.contains("?")) {
            int tempIndex = imgUrl.indexOf("?");
            return imgUrl.substring(0, tempIndex + 1);
        }
        return imgUrl;
    }

    public static boolean equals(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        } else if (str1 != null && str2 != null) {
            if (str1.equals(str2)) {
                return true;
            }
        }
        return false;
    }

    //处理地址名称
    public static String dealCityName(String cityName) {
        if (cityName.equals("市辖区") || cityName.equals("县") || cityName.equals("市")) {
            return "";
        } else {
            return "-" + cityName;
        }
    }

    //更改万单位
    public static String convertCount(Integer count) {
        if (count == null) {
            return "0";
        }
        String strCount="";
        strCount=count+"";
        if (count<10000){
            return strCount;
        }else if (count<10000*10000){
            return strCount.substring(0,strCount.length()-4)+"."+strCount.substring(strCount.length()-4,strCount.length()-2)+"万";
        }else {
            return strCount.substring(0,strCount.length()-8)+"."+strCount.substring(strCount.length()-8,strCount.length()-6)+"亿";
        }
    }

    //int转字符串
    public static String convertToCount(Integer count) {
        if (count == null) {
            return "0";
        } else {
            return count + "";
        }
    }

    //字符串集合中移除字符串
    public static void removeStringFromList(ArrayList<String> arrayList, String str) {
        for (int i = 0; i < arrayList.size(); i++) {
            if (arrayList.get(i).trim().equals(str.trim())) {
                arrayList.remove(i);
            }
        }
    }

    //截取字符串前部分，再加上省略号
    public static String getShortString(String str, int count) {
        if (str != null && str.length() > count) {
            return str.substring(0, count) + "...";
        } else {
            return str;
        }
    }

    public static int getPrice(String s) {
        int price = 1;
        if (s.startsWith("¥")) {
            s = s.substring(1);
            try {
                price = (int) (Float.parseFloat(s) * 100);
                return price;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return price;
    }

    /**
     * @param actionArgument
     * @return
     * @author diql edited on 2016/3/2, 防止传入为 json 时， 无法解析。
     */
    public static JSONObject convertUrlParamToJson(String actionArgument) {
        JSONObject jsonObject = new JSONObject();
        if (isEmpty(actionArgument)) {
            return null;
        }
        if (actionArgument.startsWith("yushang://")) {
            String param = actionArgument.replace("yushang://", "");
            String[] params = param.split("&&");
            for (String paramStr : params) {
                try {
                    String[] keyValue = paramStr.split("=", 2);
                    jsonObject.put(keyValue[0], keyValue[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                jsonObject = new JSONObject(actionArgument);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    public static String hideMidString(String text){
        if (text.length()<8){
            return text;
        }
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append(text.substring(0,2));
        stringBuffer.append("***");
        stringBuffer.append(text.substring(text.length()-3,text.length()));
        return stringBuffer.toString();
    }

    //unicode 转中文
    public static String decodeUnicode(String theString) {

        char aChar;

        int len = theString.length();

        StringBuffer outBuffer = new StringBuffer(len);

        for (int x = 0; x < len; ) {

            aChar = theString.charAt(x++);

            if (aChar == '\\') {

                aChar = theString.charAt(x++);

                if (aChar == 'u') {

                    // Read the xxxx

                    int value = 0;

                    for (int i = 0; i < 4; i++) {

                        aChar = theString.charAt(x++);

                        switch (aChar) {

                            case '0':

                            case '1':

                            case '2':

                            case '3':

                            case '4':

                            case '5':

                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed   \\uxxxx   encoding.");
                        }

                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';

                    else if (aChar == 'n')

                        aChar = '\n';

                    else if (aChar == 'f')

                        aChar = '\f';

                    outBuffer.append(aChar);

                }

            } else

                outBuffer.append(aChar);

        }

        return outBuffer.toString();

    }

    //把错误的json字符串替换
    public static String delToJsonString(String json) {
        //json 字符串 替换 "[  替换为 [ ]"替换为] "{替换为{ }"替换为}
        return json.replace("\"[", "[")
                .replace("]\"", "]")
                .replace("\"{", "{")
                .replace("}\"", "}");
    }

    //取访问页面名字，比如http://m2.xunbaozl.com/index.html 则取 index
    public static String getHtmlName(String url) {
        if (UpStringUtils.isEmpty(url)) {
            return "";
        }
        int lastSpliteIndex = url.lastIndexOf("?");
        if (lastSpliteIndex > -1) {
            url = url.substring(0, lastSpliteIndex);
        }
        int lastIndexStart = url.lastIndexOf("/");
        int lastIndexEnd = url.lastIndexOf(".");
        if (lastIndexEnd > lastIndexStart && url.length() > lastIndexEnd - 1) {
            return url.substring(lastIndexStart + 1, lastIndexEnd);
        }
        //不是 .html这种方式
        if (lastSpliteIndex > lastIndexStart && url.length() > lastSpliteIndex - 1) {
            return url.substring(lastIndexStart + 1, lastSpliteIndex);
        }
        return "";
    }

    /**
     * 获取drawable本地路径uri
     */

    public static String getFileUri(Resources resource, int resId) {
        StringBuilder sb = new StringBuilder(ContentResolver.SCHEME_ANDROID_RESOURCE + "://");
        sb.append(resource.getResourcePackageName(resId)).append("/")
                .append(resource.getResourceTypeName(resId)).append("/")
                .append(resource.getResourceEntryName(resId));
        return sb.toString();
    }

}
