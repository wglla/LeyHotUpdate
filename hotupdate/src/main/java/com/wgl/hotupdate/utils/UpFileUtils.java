package com.wgl.hotupdate.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.wgl.hotupdate.engine.UpdateEngine;
import com.wgl.hotupdate.listener.FileDownLoadListener;
import com.wgl.hotupdate.listener.SuccessListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpFileUtils {
    private final static String ROOT = UpdateEngine.JS_PATCH;


    /**
     * 递归删除文件
     * @param file
     */
    public static void deleteFile(File file) {
        if (file.exists()) {//判断文件是否存在
            if (file.isFile()) {//判断是否是文件
                file.delete();//删除文件
            } else if (file.isDirectory()) {//否则如果它是一个目录
                File[] files = file.listFiles();//声明目录下所有的文件 files[];
                for (int i = 0;i < files.length;i ++) {//遍历目录下所有的文件
                    deleteFile(files[i]);//把每个文件用这个方法进行迭代
                }
                file.delete();//删除文件夹
            }
        } else {
            UpLogUtils.log("所删除的文件不存在");
        }
    }


    /**
     * 读取文件的MD5值
     * @param file
     * @return
     * @throws FileNotFoundException
     */

    public static String getMd5ByFile(File file) throws FileNotFoundException {


        String value = null;
        FileInputStream in = new FileInputStream(file);
        try {
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }

    /**
     * Load file in asset directory.
     * @param path FilePath
     * @param context Weex Context
     * @return the Content of the file
     */
    public static String loadFileContent(String path, Context context, SuccessListener successListener) {
        UpLogUtils.log("loadFileContent"+path);
        StringBuilder builder ;
        try {
            InputStream in = context.getAssets().open(path);

            builder = new StringBuilder(in.available()+10);

            BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(in));
            char[] data = new char[2048];
            int len = -1;
            while ((len = localBufferedReader.read(data)) > 0) {
                builder.append(data, 0, len);
            }
            localBufferedReader.close();
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            if (successListener!=null){
                successListener.onFailed("js文件不存在:"+path);
            }
        }

        return "";
    }

    /**
     * Load file in asset directory.
     * @param path FilePath
     * @param context Weex Context
     * @return the Content of the file
     */
    public static String loadFile(String path, Context context) {
        StringBuilder builder ;
        try {
            File file=new File(path);
            InputStream in = new FileInputStream(file);
            builder = new StringBuilder(in.available()+10);
            BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(in));
            char[] data = new char[2048];
            int len = -1;
            while ((len = localBufferedReader.read(data)) > 0) {
                builder.append(data, 0, len);
            }
            localBufferedReader.close();
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            return builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static void downloadFile(String url, final String path,final String fileName, final FileDownLoadListener listener) {
        Log.d("FileDown", "downloadFile() called with: url = [" + url + "], path = [" + path + "], listener = [" + listener + "]");
        if (UpStringUtils.isEmpty(url) || UpStringUtils.isEmpty(path)) {
            return;
        }
        Request request = new Request.Builder().url(url).build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("h_bl", "onFailure");
                if (listener != null) {
                    listener.onError("下载失败:"+e.toString());
                }
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                saveFile(response,path,fileName,listener);
            }
        });
    }

    private static void saveFile(Response response, String path, String fileName, FileDownLoadListener listener){
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len = 0;
        FileOutputStream fos = null;
//                String SDPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        try {
            is = response.body().byteStream();
            long total = response.body().contentLength();
            UpLogUtils.log("源文件大小"+total);
            if (total==0){
                try {
                    if (is != null)
                        is.close();
                } catch (IOException e) {
                }
                listener.onError("下载失败：源文件不存在");
                return;
            }

            File dir = new File(path);
            if (!dir.exists()) {
                try {
                    //按照指定的路径创建文件夹
                    boolean a=dir.mkdirs();
                } catch (Exception e) {
                    listener.onError("创建文件夹失败"+dir.getAbsolutePath()+dir.getName());
                    e.printStackTrace();
                }
            }
            File file = new File(path+"/"+fileName);
            if (!file.exists()) {
                try {
                    //在指定的文件夹中创建文件
                    file.createNewFile();
                } catch (Exception e) {
                    listener.onError("创建文件失败"+file.getAbsolutePath()+file.getName());
                    e.printStackTrace();
                }
            }

            fos = new FileOutputStream(file);
            long sum = 0;
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
                sum += len;
                int progress = (int) (sum * 1.0f / total * 100);
                if (listener != null) {
                    listener.onProgress(progress, total);
                }
            }
            fos.flush();
            if (listener != null) {
                listener.onSuccess(path+"/"+fileName);
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onError("下载失败.");
            }
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            }
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
            }
        }
    }


    /**
     * DeCompress the ZIP to the path
     * @param zipFileString  name of ZIP
     * @param outPathString   path to be unZIP
     * @throws Exception
     */
    public static int UnZipFolder(String zipFileString, String outPathString) throws Exception {
        UpLogUtils.log("开始解压："+zipFileString+"  "+outPathString);
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString));
        ZipEntry zipEntry;
        String szName = "";
        int index=0;
        while ((zipEntry = inZip.getNextEntry()) != null) {
            index++;
            szName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                UpLogUtils.log("解压 第"+index+"个文件夹"+szName);
                // get the folder name of the widget
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(outPathString + File.separator + szName);
                folder.mkdirs();
            } else {
                UpLogUtils.log("解压 第"+index+"个文件"+szName);
                File file = new File(outPathString + File.separator + szName);
                file.createNewFile();
                // get the output stream of the file
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024];
                // read (len) bytes into buffer
                while ((len = inZip.read(buffer)) != -1) {
                    // write (len) byte from buffer at the position 0
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
            }
        }
        inZip.close();
        if (index==0){
            UpLogUtils.log("解压失败");
        }

        return index;
    }

    /**
     * Compress file and folder
     * @param srcFileString   file or folder to be Compress
     * @param zipFileString   the path name of result ZIP
     * @throws Exception
     */
    public static void ZipFolder(String srcFileString, String zipFileString)throws Exception {
        //create ZIP
        ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(zipFileString));
        //create the file
        File file = new File(srcFileString);
        //compress
        ZipFiles(file.getParent()+File.separator, file.getName(), outZip);
        //finish and close
        outZip.finish();
        outZip.close();
    }

    /**
     * compress files
     * @param folderString
     * @param fileString
     * @param zipOutputSteam
     * @throws Exception
     */
    private static void ZipFiles(String folderString, String fileString, ZipOutputStream zipOutputSteam)throws Exception{
        if(zipOutputSteam == null)
            return;
        File file = new File(folderString+fileString);
        if (file.isFile()) {
            ZipEntry zipEntry =  new ZipEntry(fileString);
            FileInputStream inputStream = new FileInputStream(file);
            zipOutputSteam.putNextEntry(zipEntry);
            int len;
            byte[] buffer = new byte[4096];
            while((len=inputStream.read(buffer)) != -1)
            {
                zipOutputSteam.write(buffer, 0, len);
            }
            zipOutputSteam.closeEntry();
        }
        else {
            //folder
            String fileList[] = file.list();
            //no child file and compress
            if (fileList.length <= 0) {
                ZipEntry zipEntry =  new ZipEntry(fileString+File.separator);
                zipOutputSteam.putNextEntry(zipEntry);
                zipOutputSteam.closeEntry();
            }
            //child files and recursion
            for (int i = 0; i < fileList.length; i++) {
                ZipFiles(folderString, fileString+ File.separator+fileList[i], zipOutputSteam);
            }//end of for
        }
    }

    /**
     * return the InputStream of file in the ZIP
     * @param zipFileString  name of ZIP
     * @param fileString     name of file in the ZIP
     * @return InputStream
     * @throws Exception
     */
    public static InputStream UpZip(String zipFileString, String fileString)throws Exception {
        ZipFile zipFile = new ZipFile(zipFileString);
        ZipEntry zipEntry = zipFile.getEntry(fileString);
        return zipFile.getInputStream(zipEntry);
    }

    /**
     * return files list(file and folder) in the ZIP
     * @param zipFileString     ZIP name
     * @param bContainFolder    contain folder or not
     * @param bContainFile      contain file or not
     * @return
     * @throws Exception
     */
    public static List<File> GetFileList(String zipFileString, boolean bContainFolder, boolean bContainFile)throws Exception {
        List<File> fileList = new ArrayList<File>();
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString));
        ZipEntry zipEntry;
        String szName = "";
        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                // get the folder name of the widget
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(szName);
                if (bContainFolder) {
                    fileList.add(folder);
                }

            } else {
                File file = new File(szName);
                if (bContainFile) {
                    fileList.add(file);
                }
            }
        }
        inZip.close();
        return fileList;
    }

    //获取根目录缓存文件夹
    public static StringBuilder getRootCachePath() {
        StringBuilder path = new StringBuilder();
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            path.append(sdDir.getAbsolutePath());
        }
        path.append("/").append(ROOT);
        File pathFile = new File(path.toString());
        if (!pathFile.exists()) {
            if (!pathFile.mkdirs())
                return new StringBuilder().append("/").append("cache");
        }
        return path.append("/").append("cache");
    }
}
