package com.bis.stresstest.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.TrafficStats;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.bis.stresstest.R;
import com.bis.stresstest.activity.AgentActivity;
import com.bis.stresstest.app.MyApplication;
import com.bis.stresstest.model.AndroidPlatform;
import com.bis.stresstest.model.BaseInfoModel;
import com.bis.stresstest.model.RunningModel;
import com.bis.stresstest.util.AndroidOS;
import com.bis.stresstest.util.Config;
import com.bis.stresstest.util.MacUtils;
import com.bis.stresstest.util.ShellUtils;
import com.google.gson.Gson;

import androidx.core.app.NotificationCompat;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import static androidx.core.app.NotificationCompat.PRIORITY_MAX;
import static com.bis.stresstest.util.AndroidOS.getOSAndroidPlatform;


public class BackGroundService extends Service {
    Notification notification;
    private Context mContext;
    private MediaPlayer bgmediaPlayer;

    public BackGroundService() {
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = this;
        Intent notificationIntent = new Intent(this, AgentActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Log.i(Config.TAG, "Android 8.0开启通知栏");
        String channelId = "chat";
        String channelName = "聊天消息";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        createNotificationChannel(channelId, channelName, importance);

        channelId = "subscribe";
        channelName = "订阅消息";
        importance = NotificationManager.IMPORTANCE_DEFAULT;
        createNotificationChannel(channelId, channelName, importance);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification = new NotificationCompat.Builder(this, "subscribe")
                .setSmallIcon(R.drawable.article_collect_select)
                .setWhen(System.currentTimeMillis())
                .setTicker("StressTestAPK")
                .setContentTitle("StressTestAPK")
                .setContentText("StressTestAPK运行中")
                .setOngoing(true)
                .setPriority(PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .build();
        manager.notify(2, notification);

        //3.最关键的神来之笔，也是最投机的动作，没办法要骗过CPU
        //这就是播放音乐类APP不被杀的做法，自己找个无声MP3放进来循环播放
        if (bgmediaPlayer == null) {
            bgmediaPlayer = MediaPlayer.create(this, R.raw.silent);
            bgmediaPlayer.setLooping(true);
            bgmediaPlayer.start();
        }
        startForeground(1, this.notification);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    //Android 8.0之后通知栏设置
    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }


    @Override
    public void onDestroy() {
        stopForeground(true);
        if (bgmediaPlayer != null) {
            bgmediaPlayer.release();
        }
        stopSelf();
        super.onDestroy();
    }

    private void restartAPP() {
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        assert intent != null;
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //与正常页面跳转一样可传递序列化数据,在Launch页面内获得
        intent.putExtra("REBOOT", "reboot");
        startActivity(intent);
    }

}