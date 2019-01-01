package com.kunfei.bookshelf.update;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import com.kunfei.bookshelf.MApplication;

public class CheckUpdateTask extends AsyncTask<Void, Void, String> {
	
	private ProgressDialog dialog;
    private Context mContext;
    private int mType;
    private boolean mShowProgressDialog;
    private static final String url = Constants.UPDATE_URLL;
    private static final String urli = Constants.UPDATE_URLI;

    CheckUpdateTask(Context context, int type, boolean showProgressDialog) {

        this.mContext = context;
        this.mType = type;
        this.mShowProgressDialog = showProgressDialog;

    }
    
    protected void onPreExecute() {
        if (mShowProgressDialog) {
            dialog = new ProgressDialog(mContext);
            dialog.setMessage("正在检查版本");
            dialog.show();
        }
    }
    
    @Override
    protected void onPostExecute(String result) {

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        if (!TextUtils.isEmpty(result)) {
            parseJson(result);
        }
    }

	private void parseJson(String result) {
		// TODO Auto-generated method stub{
        try {

            JSONArray getJsonArray=new JSONArray(result);
            JSONObject getJsonObj = getJsonArray.getJSONObject(0);
            JSONObject obj = getJsonObj.getJSONObject("apkInfo");

            int apkCode = obj.getInt("versionCode");
            String apkName = obj.getString("versionName");
            String apkUrl = urli  + obj.getString("outputFile");
            String updateMessage = "新版本:" + apkName + "， 请下载更新 !";

           // String versionCode = AppUtils.getVersionName(mContext);
            int versionCode = AppUtils.getVersionCode(mContext);

            if (apkCode > versionCode) {
                if (mType == Constants.TYPE_NOTIFICATION) {
                    showNotification(mContext, updateMessage, apkUrl);
                } else if (mType == Constants.TYPE_DIALOG) {
                    showDialog(mContext, updateMessage, apkUrl);
                }
            } else if (mShowProgressDialog) {
                Toast.makeText(mContext, "已经是最新版本", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            Log.e(Constants.TAG, "parse json error");
        }
    }

	private void showDialog(Context context, String content,
			String apkUrl) {
		// TODO Auto-generated method stub
		UpdateDialog.show(context, content, apkUrl);
	}

	private void showNotification(Context context, String content,
			String apkUrl) {
		// TODO Auto-generated method stub{
        Intent myIntent = new Intent(context, DownloadService.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        myIntent.putExtra(Constants.APK_DOWNLOAD_URL, apkUrl);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int smallIcon = context.getApplicationInfo().icon;
        Notification notify = new NotificationCompat.Builder(context, MApplication.channelIdReadAloud)
                .setTicker("发现新版本，点击升级")
                .setContentTitle("发现新版本，点击升级")
                .setContentText(content)
                .setSmallIcon(smallIcon)
                .setContentIntent(pendingIntent).build();

        notify.flags = android.app.Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notify);
    }
		
	

	

	@Override
	protected String doInBackground(Void... args) {
		// TODO Auto-generated method stub
		return HttpUtils.get(url);
	}

}