package com.android.launcher3.download;

import com.android.launcher3.LauncherApplication;

/**
 * Created by cgx on 2016/12/6.
 */

public class DownloadTaskInfo {
    public int id;       //唯一标识
    public String name;  //名称
    public String url;    //地址
    public long size;    //大小

    public long currentSize = 0;//当前的size
    public int downloadState = 0;//下载的状态

    public static DownloadTaskInfo clone(DownloadInfo downloadInfo){
        DownloadTaskInfo downloadTaskInfo = new DownloadTaskInfo();

        downloadTaskInfo.id = downloadInfo.id;
        downloadTaskInfo.name = downloadInfo.name;
        downloadTaskInfo.url = downloadInfo.url;
        downloadTaskInfo.size = downloadInfo.size;

        return downloadTaskInfo;
    }

    public float getCurrentProgress(){
        if (size == 0) return 0;
        return currentSize*1.0f/size;
    }

    public int getDownloadState() {
        return downloadState;
    }

    public int getId(){
        return id;
    }

    public void setDownloadState(int state){
        downloadState = state;
    }

    public void setCurrentSize(long currentSize){
        this.currentSize = currentSize;
    }

    public long getCurrentSize(){
        return currentSize;
    }

    public String getUrl(){
        return url;
    }

    public String getPath(){
        return getPath(name);
    }

    public static String getPath(String name){
        return LauncherApplication.getInstance().getDoanloadDir()+"/"+name+".apk";
    }
}
