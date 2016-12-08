package com.android.launcher3.download;

import android.view.View;

import com.android.launcher3.utils.PackageUtil;

/**
 * Created by cgx on 2016/12/7.
 */

public class DownloadController implements DownloadManager.DownloadObserver{

    private OnDownloadRefreshUI mTargView;
    private DownloadManager mDownloadManager;
    private int mState;
    private DownloadInfo info;

    public DownloadController(OnDownloadRefreshUI view){
        mTargView = view;
        mDownloadManager = DownloadManager.getInstance();
    }

    @Override
    public void onDownloadStateChanged(final DownloadTaskInfo info) {
        mState = info.downloadState;
        ((View)mTargView).post(new Runnable() {
            @Override
            public void run() {
                mTargView.onRefreshUI(info);
            }
        });
    }

    @Override
    public void onDownloadProgressed(final DownloadTaskInfo info) {
        mState = info.downloadState;
        ((View)mTargView).post(new Runnable() {
            @Override
            public void run() {
                mTargView.onRefreshUI(info);
            }
        });
    }

    public void registerObserver(){
        mDownloadManager.registerObserver(this);
    }

    public void unRegisterObserver(){
        mDownloadManager.unRegisterObserver(this);
    }

    public void setDwonloadInfo(DownloadInfo info){
        this.info = info;
        DownloadTaskInfo taskInfo = mDownloadManager.getDownloadMap().get(info.id);
        if (taskInfo != null){
            onDownloadStateChanged(taskInfo);
        }
    }

    public void executeClick(DownloadInfo info){
        if (mState == DownloadManager.STATE_NONE || mState == DownloadManager.STATE_PAUSED || mState == DownloadManager.STATE_ERROR) {
            mDownloadManager.download(info);
        }else if (mState == DownloadManager.STATE_WAITING || mState == DownloadManager.STATE_DOWNLOADING) {
            mDownloadManager.pause(info);
        } else if (mState == DownloadManager.STATE_DOWNLOADED) {
            PackageUtil.installApkNormal(DownloadTaskInfo.getPath(info.name));
        }
    }

}
