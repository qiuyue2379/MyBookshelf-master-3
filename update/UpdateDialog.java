package com.kunfei.bookshelf.update;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;

public class UpdateDialog {

	  static void show(final Context context, String content, final String downloadUrl) {
	        if (isContextValid(context)) {
	            AlertDialog.Builder builder = new AlertDialog.Builder(context);
	            builder.setTitle("发现新版本");
	            builder.setMessage(Html.fromHtml(content))
	                    .setPositiveButton("立刻下载", new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int id) {
	                            goToDownload(context, downloadUrl);
	                        }
	                    })
	                    .setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int id) {
	                        }
	                    });

	            AlertDialog dialog = builder.create();
	            //点击对话框外面,对话框不消失
	            dialog.setCanceledOnTouchOutside(false);
	            dialog.show();
	        }
	    }

	    private static boolean isContextValid(Context context) {
	        return context instanceof Activity && !((Activity) context).isFinishing();
	    }


	    private static void goToDownload(Context context, String downloadUrl) {
	        Intent intent = new Intent(context.getApplicationContext(), DownloadService.class);
	        intent.putExtra(Constants.APK_DOWNLOAD_URL, downloadUrl);
	        context.startService(intent);
	    }
}