package com.example.app_update.utils;

import java.io.File;

import android.os.Environment;

public class Constants {

	public static final String PATCH_FILE = "apk.patch";
	
	//服务中差分包的网络路径
	public static final String URL_PATCH_DOWNLOAD = "http://www.***.top/"+PATCH_FILE;
	
	public static final String PACKAGE_NAME = "com.example.app_update";
	
	public static final String SD_CARD = Environment.getExternalStorageDirectory() + File.separator;
	
	//新版本apk的目录
	public static final String NEW_APK_PATH = SD_CARD+"dn_apk_new.apk";
	
	public static final String PATCH_FILE_PATH = SD_CARD+PATCH_FILE;
	
}
