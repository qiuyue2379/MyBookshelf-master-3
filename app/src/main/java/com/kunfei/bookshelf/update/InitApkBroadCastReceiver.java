package com.kunfei.bookshelf.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class InitApkBroadCastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            comm.rmoveFile("QIUYUE_");
            Toast.makeText(context , "安装包已删除" , Toast.LENGTH_LONG).show();
        }

        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            comm.rmoveFile("QIUYUE_");
            Toast.makeText(context , "安装包已删除" , Toast.LENGTH_LONG).show();
            System.out.println("");
        }

        if (Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
            comm.rmoveFile("QIUYUE_");
            Toast.makeText(context , "安装包已删除" , Toast.LENGTH_LONG).show();
        }
    }

}