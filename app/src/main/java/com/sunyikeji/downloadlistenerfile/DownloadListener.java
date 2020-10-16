package com.sunyikeji.downloadlistenerfile;

public interface DownloadListener {
    void onProgress(int progress);//通知当前下载进度
    void onSuccess();//通知下载成功
    void onFiled();//通知下载失败
    void onPaused();//下载暂停
    void inCanceled();//下载取消
}
