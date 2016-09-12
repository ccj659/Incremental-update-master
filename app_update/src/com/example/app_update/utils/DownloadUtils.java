package com.example.app_update.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Environment;

public class DownloadUtils {

	/**
	 * 下载差分包
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static File download(String url){
		File file = null;
		InputStream is = null;
		FileOutputStream os = null;
		try {
			file = new File(Environment.getExternalStorageDirectory(),Constants.PATCH_FILE);
			if (file.exists()) {
				file.delete();
			}
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setDoInput(true);
			is = conn.getInputStream();
			os = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int len = 0;
			while((len = is.read(buffer)) != -1){
				os.write(buffer, 0, len);
			}
		} catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}
	
}
