package com.kunfei.bookshelf.update;

import android.os.Environment;
import android.content.Context;
import android.content.pm.PackageManager;

import java.io.File;

import com.kunfei.bookshelf.MApplication;

public class comm{
    public static File getPathFile(String path){
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, MApplication.packageName()), "cache");
        String apkName = path + MApplication.getVersionName() +".apk";
        File outputFile = new File(appCacheDir, apkName);
        return outputFile;
    }

    public static void rmoveFile(String path){
        File file = getPathFile(path);
        file.delete();
    }

}