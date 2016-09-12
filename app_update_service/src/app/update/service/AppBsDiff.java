package app.update.service;
//app.update.service.AppBsDiff

public class AppBsDiff {
	
	
	
	
	public native static void diff(String oldFile,String newFile,String pathch);
		
	
	static{
		System.loadLibrary("app_bsdiff");
	}
	
	
}
