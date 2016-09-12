package app.update.service;


public class BsDiffTest {

	public static void main(String[] args) {
		//生成差分包 
		AppBsDiff.diff(ConstansW.OLD_APK_PATH, ConstansW.NEW_APK_PATH, ConstansW.PATCH_PATH);		
		
	}

}
