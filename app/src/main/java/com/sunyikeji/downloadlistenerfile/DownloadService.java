package com.sunyikeji.downloadlistenerfile;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;


import androidx.core.app.NotificationCompat;

import java.io.File;

public class DownloadService extends Service {
    private DownLoadTask downLoadTask;
    private String downloadUrl;
    //创建一个DownloadListener并实现其中的方法
    private DownloadListener downloadListener=new DownloadListener() {
        @Override//以通知的方式显示进度条
        public void onProgress(int progress) {
            //使用getNotificationManager函数构建一个用于显示下载进度的通知
            //使用notify去触发这个通知
            getNotificationManager().notify(1,getNotification("Download...",progress));
        }

        @Override
        public void onSuccess() {
            downLoadTask=null;
            //关闭前台服务通知
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("download succeed",-1));
            Toast.makeText(DownloadService.this,"Download Succeed",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFiled() {
            downLoadTask=null;
            //关闭前台服务通知
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("download filed",-1));
            Toast.makeText(DownloadService.this,"Download failed",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onPaused() {
            downLoadTask=null;
            Toast.makeText(DownloadService.this,"Download Paused",Toast.LENGTH_LONG).show();
        }

        @Override
        public void inCanceled() {
            downLoadTask=null;
            //关闭前台服务通知
            stopForeground(true);
            Toast.makeText(DownloadService.this,"Download canceled",Toast.LENGTH_LONG).show();
        }
    };

    private DownloadBinder mBinder=new DownloadBinder();




    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }
    //用于使服务可以和活动通信
    public class DownloadBinder extends Binder{
        public void startDownload(String url){
            if(downLoadTask==null){
                downloadUrl=url;
                downLoadTask=new DownLoadTask(downloadListener);
                //使用execute开启下载
                downLoadTask.execute(downloadUrl);
                //startForeground使服务成为一个前台服务以创建持续运行的通知
                startForeground(1,getNotification("download...",0));
                Toast.makeText(DownloadService.this,"Download",Toast.LENGTH_LONG).show();
            }
        }
        public void pauseDownload(){
            if (downLoadTask!=null){
                downLoadTask.pauseDownload();
            }
        }
        //取消下载后需要将下载中的任务取消
        public void cancelDownload(){
            if(downLoadTask!=null){
                downLoadTask.cancelDownload();
            }else {
                if (downloadUrl!=null)
                {
                    //取消需要将文件删除并将通知关闭
                    String fileName=downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file=new File(directory+fileName);
                    if(file.exists()){
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownloadService.this,"Canceled",Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private NotificationManager getNotificationManager(){
        return (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }
    private Notification getNotification(String title,int progress){
        Intent intent=new Intent(this, MainActivity.class);
        PendingIntent pi=PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if(progress>0){
            builder.setContentText(progress+"%");
            builder.setProgress(100,progress,false);//最大进度，当前进度，是否使用模糊进度条
        }
        return builder.build();
    }
}
