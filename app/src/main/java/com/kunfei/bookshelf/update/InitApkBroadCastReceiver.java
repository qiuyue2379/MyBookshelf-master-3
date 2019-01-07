package com.kunfei.bookshelf.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


public class InitApkBroadCastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String urlStr = intent.getStringExtra(Constants.APK_DOWNLOAD_URL);
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            comm.rmoveFile(urlStr);
            Toast.makeText(context , "监听到系统广播添加" , Toast.LENGTH_LONG).show();
        }

        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            comm.rmoveFile(urlStr);
            Toast.makeText(context , "监听到系统广播移除" , Toast.LENGTH_LONG).show();
            System.out.println("");
        }

        if (Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
            comm.rmoveFile(urlStr);
            Toast.makeText(context , "监听到系统广播替换" , Toast.LENGTH_LONG).show();
        }
    }
}