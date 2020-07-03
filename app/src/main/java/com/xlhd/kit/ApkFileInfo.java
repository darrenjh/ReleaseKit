package com.xlhd.kit;

/**
 * Created by yangjianhui on 2020-06-26.
 */
public class ApkFileInfo {
  private String versionName;
  private String versionCode;
  private String channelID;
  private String path;
  private String fileName;

  public String getVersionName() {
    return versionName;
  }

  public void setVersionName(String versionName) {
    this.versionName = versionName;
  }

  public String getVersionCode() {
    return versionCode;
  }

  public void setVersionCode(String versionCode) {
    this.versionCode = versionCode;
  }

  public String getChannelID() {
    return channelID;
  }

  public void setChannelID(String channelID) {
    this.channelID = channelID;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  @Override public String toString() {
    return "ApkFileInfo{" +
        "versionName='" + versionName + '\'' +
        ", versionCode='" + versionCode + '\'' +
        ", channelID='" + channelID + '\'' +
        ", path='" + path + '\'' +
        ", fileName='" + fileName + '\'' +
        '}';
  }
}
