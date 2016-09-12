package com.example.app_update;

import java.io.File;

import com.example.app_update.utils.ApkUtils;
import com.example.app_update.utils.BsPatch;
import com.example.app_update.utils.Constants;
import com.example.app_update.utils.DownloadUtils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.btn_update).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ApkUpdateTask apkUpdateTask=new ApkUpdateTask();
				apkUpdateTask.execute();
			}
		});
	}
	
	class ApkUpdateTask extends AsyncTask<Void, Void, Boolean>{

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				//1.下载差分包
				Log.d("ccj", "开始下载");
				File patchFile = DownloadUtils.download(Constants.URL_PATCH_DOWNLOAD);
				
				//获取当前应用的apk文件/data/app/app
				String oldfile = ApkUtils.getSourceApkPath(MainActivity.this, getPackageName());
				//2.合并得到最新版本的APK文件
				String newfile = Constants.NEW_APK_PATH;
				String patchfile = patchFile.getAbsolutePath();
				BsPatch.patch(oldfile, newfile, patchfile);
				
				Log.i("ccj", "oldfile:"+oldfile);
				Log.i("ccj", "newfile:"+newfile);
				Log.i("ccj", "patch:"+patchfile);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
		  
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			Log.d("ccj", "下载完成");
			//3.安装
			if(result){
				Toast.makeText(MainActivity.this, "您正在进行更新", Toast.LENGTH_SHORT).show();
				ApkUtils.installApk(MainActivity.this, Constants.NEW_APK_PATH);
			}
		}
		
	}
}
