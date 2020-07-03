package com.xlhd.kit;

import com.alibaba.fastjson.JSON;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by yangjianhui on 2020-06-26.
 */
public class ReleaseMainKit {
  //日志tag
  private static final String APP_TAG = "#欢乐招财犬#";
  //apk的文件夹目录
  public static final String FILE_APK_ROOT = "/Users/mac/apk";
  public static final String FILE_APK = FILE_APK_ROOT + "/travel";
  public static final String USER_NAME = "yang";
  private static final String BASE_URL = "http://dev-control.pupupuwa1.cn/api/v1/file/app-upload";

  //psvm
  public static void main(String[] args) {
    //sout
    System.out.println(APP_TAG + "----------begin-------------");
    File appFile = new File(FILE_APK);
    if (appFile.exists()) {
      System.out.println(APP_TAG + "----------文件夹存在，开始读取文件-------------");
      if (appFile != null && appFile.isDirectory()) {
        //第一步：如果是文件目录,读取文件夹，把文件夹里需要上传的apk按照规范处理好
        List<ApkFileInfo> apkFileInfoList = readAppFileDirectory(appFile);
        //第二步:查看是否有apk数据，如果有执行串行上传任务
        if (apkFileInfoList != null && apkFileInfoList.size() > 0) {
          startUpload(apkFileInfoList);
        }
      } else {
        System.err.println(APP_TAG + "apkfile(" + FILE_APK + ") is error please check ");
      }
    } else {
      System.err.println(APP_TAG + "app的文件夹不存在");
    }
  }

  private static int lastProgress;
  private static int index;

  private static void startUpload(List<ApkFileInfo> apkFileInfoList) {
    index = 0;
    doUpload(apkFileInfoList);
  }

  private static void doUpload(final List<ApkFileInfo> apkFileInfoList) {
    if (index >= apkFileInfoList.size()) {
      System.err.println("上传成功，总共上传了-" + apkFileInfoList.size() + "个文件");
      return;
    }
    final ApkFileInfo apkFileInfo = apkFileInfoList.get(index);
    File uploadfile = new File(apkFileInfo.getPath());
    RequestBody requestFile =
        RequestBody.create(MediaType.parse("multipart/form-data"), uploadfile);
    FileRequestBody fileRequestBody =
        new FileRequestBody(requestFile, new FileRequestBody.LoadingListener() {
          @Override
          public void onProgress(long currentLength, long contentLength) {
            float rate = (float) currentLength / (float) contentLength;
            int progress = (int) (rate * 100);
            if (progress != lastProgress) {
              lastProgress = progress;
              System.out.println(APP_TAG
                  + " 第"
                  + (index + 1)
                  + "个apk,name:"
                  + apkFileInfo.getFileName()
                  + " is uploading-------"
                  + progress
                  + "%");
            }
          }
        });
    MultipartBody.Builder builder = new MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("app_channel", "10009")
        .addFormDataPart("user", "yangjianhui")
        .addFormDataPart("upgrade_type", "2")
        .addFormDataPart("version_code", apkFileInfo.getVersionCode())
        .addFormDataPart("file", apkFileInfo.getFileName(), fileRequestBody);

    OkHttpClient okHttpClient = new OkHttpClient();
    Request request = new Request.Builder()
        .url(BASE_URL)
        .post(builder.build())
        .build();
    okHttpClient.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        System.err.println("----上传失败--"+e.getMessage());
        e.printStackTrace();
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        ResponseBody responseBody = response.body();
        int code = response.code();
        System.err.println("----http--code-" + code);
        if (code == 200) {
          String string = responseBody.string();
          BaseResponse<String> baseResponse = JSON.parseObject(string, BaseResponse.class);
          System.err.println("上传服务的数据返回" + JSON.toJSONString(baseResponse));
          int responseCode = baseResponse.getCode();
          if (responseCode == 200) {
            System.err.println(code + "----上传成功--" + string);
            index++;
            doUpload(apkFileInfoList);
          } else {
            if (baseResponse.getMessage().equals("渠道不存在")) {
              index++;
              doUpload(apkFileInfoList);
            }
            System.err.println("----上传失败--" + responseCode);
          }
        }
      }
    });
  }

  /**
   * 读取app文件目录
   */
  private static List<ApkFileInfo> readAppFileDirectory(File appFileDirectory) {
    List<ApkFileInfo> apkFileInfoList = null;
    File[] files = appFileDirectory.listFiles();
    if (files.length == 0) {
      System.err.println(APP_TAG + "该目录没有任何文件，请检查");
    } else {
      System.out.println(APP_TAG + "遍历得到文件有" + files.length + "个，来自目录：" + FILE_APK);
      apkFileInfoList = createNewDirectory(files);
    }
    return apkFileInfoList;
  }

  /**
   * 创建新的文件夹目录放新的文件
   */
  private static List<ApkFileInfo> createNewDirectory(File[] files) {
    List<ApkFileInfo> apkFlies = new ArrayList<>();
    String newDirectoryPath = "";
    for (int i = 0; i < files.length; i++) {
      File f = files[i];
      String fileName = f.getName();
      if (fileName.contains("_jiagu_sign.apk")) {
        try {
          f.delete();
        } catch (Exception e) {
          e.printStackTrace();
        }
        continue;
      }
      boolean isApk = fileName.endsWith(".apk") && f.isFile();
      if (isApk) {
        String dogName = fileName.substring(fileName.indexOf("v"),
            fileName.lastIndexOf("_sign.apk"));
        String[] s = dogName.split("_");
        ApkFileInfo apkFileInfo = new ApkFileInfo();
        for (int j = 0; j < s.length; j++) {
          if (j == 0 && s[j].contains("v")) {
            apkFileInfo.setVersionName(s[j]);
          } else if (j == 1) {
            apkFileInfo.setVersionCode(s[j]);
          } else if (j == 3) {
            apkFileInfo.setChannelID(s[j]);
          }
        }
        String newFileName = "dog-" + apkFileInfo.getChannelID() + ".apk";
        newDirectoryPath = FILE_APK_ROOT + "/" + apkFileInfo.getVersionCode() + "apk";
        File newDirectoryFile = new File(newDirectoryPath);
        if (!newDirectoryFile.exists()) {
          newDirectoryFile.mkdir();
        }
        File newFile = new File(newDirectoryPath + "/" + newFileName);
        fileCopy(f, newFile);
        apkFileInfo.setPath(newFile.getPath());
        apkFileInfo.setFileName(newFile.getName());
        apkFlies.add(apkFileInfo);
      } else {
        try {
          f.delete();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    System.out.println(APP_TAG + "遍历得到apk文件有" + apkFlies.size() + "个，来自目录：" + newDirectoryPath);
    return apkFlies;
  }

  /**
   * 复制文件
   */
  public static void fileCopy(File s, File t) {
    FileInputStream fi = null;
    FileOutputStream fo = null;
    FileChannel in = null;
    FileChannel out = null;
    try {
      fi = new FileInputStream(s);
      fo = new FileOutputStream(t);
      in = fi.getChannel();//得到对应的文件通道
      out = fo.getChannel();//得到对应的文件通道
      in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("------error=" + e.getMessage());
    } finally {
      try {
        fi.close();
        in.close();
        fo.close();
        out.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
