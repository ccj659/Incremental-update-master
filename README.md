# incremental-update-master
incremental update   采用bsdiff开源库 的增量更新,差分更新 服务器端&amp;客户端,
---
## 差分(增量)更新 ##
---
##前言
随着应用越来越大,应用更新耗时间和流量的问题,就显得格外突出.

目前原生app的更新分为两种:重新下载源文件,还有一种就是差分包更新,也叫增量更新.

在有些应用市场,例如google play,会对安装包进行拆分和合并,来达到差分更新的目的.

首先解释一下差分包:
差分包是apk新版本和旧版本之间的包,可以称之为patch.

##应用流程:


![流程图](http://img.blog.csdn.net/20160909160843311)


---

##操作流程

- 确保客户端是old_app
- 改变app大小生成新的new_app
- 执行服务器生成patch程序
- 将patch包放在服务器供客户端下载
- 服务器合并安装

---
##实现原理:


###1.相应下载

自己的github项目(包括服务器端,android端,C++端),阅读此文之前,最好下载完毕研究一下[https://github.com/ccj659/incremental-update-master](https://github.com/ccj659/incremental-update-master)

[原理是采用的是bsdiff,而它是一个优秀的开源C库,大家可以去看下 ](https://github.com/mendsley/bsdiff "github地址")

linux 的相关diff/patch下载 [http://www.daemonology.net/bsdiff/](http://www.daemonology.net/bsdiff/)

windows 上的bsdiff  [http://sites.inka.de/tesla/others.html#bsdiff](http://sites.inka.de/tesla/others.html#bsdiff)

相关依赖bzip文档及下载[http://www.bzip.org/downloads.html](http://www.bzip.org/downloads.html)


###2.原理分析
Binary diff是依赖bzip压缩库的开源库,其实是一种文件比较的一种算法实现,是一个二进制比较工具.
这里有两个文件:老版本的app:old_app.apk  新版本的app:new_app.apk.
首先是Binarys diff:

1.首先将老文件old_app转为二进制文件.

2.在新文件new_app中找到和老文件相同的二进制数据.

3.在新文件生成的二进制数据中,分离new_app中老文件数据和新的二进制数据patch.

4.将patch数据打上新数据的标签,重新打包生成apk.patch.




然后是Binarys patch: 
1.通过bzip压缩算法,将old_app和patch重新打包.
关于bzip,



##实现过程



###windows服务器端

####1.分析bsdiff.cpp源码,找到main入口

```c

	/*阅读源码得知,此处第一个参数argc必须是4,argv是一个字符串指针数组*/
	/***如下,此处需要四个参数 1.随便的值,2.ldfile 	3.newfile 4.patchfile***************************/
	int bsdiff_main(int argc,char *argv[])
	{
	int fd;
	u_char *old,*_new;
	off_t oldsize,newsize;
	off_t *I,*V;
	off_t scan,pos,len;
	off_t lastscan,lastpos,lastoffset;
	off_t oldscore,scsc;
	off_t s,Sf,lenf,Sb,lenb;
	off_t overlap,Ss,lens;
	off_t i;
	off_t dblen,eblen;
	u_char *db,*eb;
	u_char buf[8];
	u_char header[32];
	FILE * pf;
	BZFILE * pfbz2;
	int bz2err;
	/**********************如下,此处需要四个参数 1.随便的值,2.ldfile 3.newfile 4.patchfile***************************/
	if(argc!=4) errx(1,"usage: %s oldfile newfile patchfile\n",argv[0]);

	/* Allocate oldsize+1 bytes instead of oldsize bytes to ensure
		that we never try to malloc(0) and get a NULL pointer */
	//org:
	//if(((fd=open(argv[1],O_RDONLY,0))<0) ||
	//	((oldsize=lseek(fd,0,SEEK_END))==-1) ||
	//	((old=malloc(oldsize+1))==NULL) ||
	//	(lseek(fd,0,SEEK_SET)!=0) ||
	//	(read(fd,old,oldsize)!=oldsize) ||
	//	(close(fd)==-1)) err(1,"%s",argv[1]);
	//new:
	//Read in chunks, don't rely on read always returns full data!
	if(((fd=open(argv[1],O_RDONLY|O_BINARY|O_NOINHERIT,0))<0) ||
		((oldsize=lseek(fd,0,SEEK_END))==-1) ||
		((old=(u_char*)malloc(oldsize+1))==NULL) ||
		(lseek(fd,0,SEEK_SET)!=0))
				err(1,"%s",argv[1]);
```

####2.新建javaWeb项目,并生成需要的头文件.
	生成的操作步骤请看我的 
 [JNI开发极简教程](http://blog.csdn.net/ccj659/article/details/52189128 "")

![这里写图片描述](http://img.blog.csdn.net/20160912182904538)



####3.根据下载的bsdiff4.3-win32-src代码，生成dll动态库，用于得到差分包

	在visual studio下 新建C++项目,并导入bsdiff源码(c,cpp,h)
![这里写图片描述](http://img.blog.csdn.net/20160912183443440)

要注意的是,编译过程并不是一帆风顺的,这里需要做什么修正.
> 用了不安全的函数->在首处添加 #define _CRT_SECURE_NO_WARNINGS
	
> 用了过时的函数->添加 #define _CRT_NONSTDC_NO_DEPRECATE

>  如果还报错,可以选择关闭SDL检查 
>   ![这里写图片描述](http://img.blog.csdn.net/20160912183915684)
 



####4.修改bsdiff.cpp源文件编写JNI函数供Java层调用（注意统一编码）

	1.在此文件中,引入头文件 #include"app_update_service_AppBsDiff.h". 并实现其中的方法(在文件末尾实现).
	2.将main函数作为jni调用的函数.即将main函数改名为bsdiff_main,然后由jni调用.
	
```c

/*
* Class:     app_update_service_AppBsDiff
* Method:    diff
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
*/

JNIEXPORT void JNICALL Java_app_update_service_AppBsDiff_diff
(JNIEnv *env, jclass jcls, jstring oldfile_jstr, jstring newfile_jstr, jstring patchfile_jstr){
	int argc = 4;
	char* oldfile = (char*)env->GetStringUTFChars(oldfile_jstr, NULL);
	char* newfile = (char*)env->GetStringUTFChars(newfile_jstr, NULL);
	char* patchfile = (char*)env->GetStringUTFChars(patchfile_jstr, NULL);

	//参数（第一个参数无效）
	char *argv[4];
	argv[0] = "bsdiff";
	argv[1] = oldfile;
	argv[2] = newfile;
	argv[3] = patchfile;

	bsdiff_main(argc, argv);

	env->ReleaseStringUTFChars(oldfile_jstr, oldfile);
	env->ReleaseStringUTFChars(newfile_jstr, newfile);
	env->ReleaseStringUTFChars(patchfile_jstr, patchfile);
}
```

####5.编译,生成解决方案,生成 E:\WorkSpace\VSWork\app_bsdiff\x64\Debug\app_bsdiff.dll文件
	如何生成,请参照下面的教程
  [JNI开发极简教程](http://blog.csdn.net/ccj659/article/details/52189128 "")

####6.将dll.放入web工程的根目录.将应用生成的两个新旧apk放到指定目录,运行即可c生成差分包apk.patch
  详情参照我的代码 --[增量更新github](https://github.com/ccj659/incremental-update-master "")

![](http://img.blog.csdn.net/20160912191704763)

####7.将生成的apk.patch放到web服务器上供客户端下载.
	这边的服务器上传配置等,我还没来得及整理,可百度...
![这里写图片描述](http://img.blog.csdn.net/20160912192159349)


###android客户端(类似于服务器端)
	客户端要做的就是bspatch,整合old_app和patch生成new_app.
代码参考-[github的android应用项目app_update-](https://github.com/ccj659/incremental-update-master "")

####1.编写native方法,生成头文件(别忘了添加相应权限).
![这里写图片描述](http://img.blog.csdn.net/20160912192858921)

####2.添加本地支持
博文请参考[ eclipse搭建NDK开发环境](http://blog.csdn.net/ccj659/article/details/52299365)

####3.将bzip2源码,bspatch.c引入到项目的jni目录,并且将android.mk中的bspatch.cpp改为bspatch.c
![这里写图片描述](http://img.blog.csdn.net/20160912193504320)


####4.修改bspatch.c源码,并实现native方法.

详情请参考代码-[github的android应用项目app_update-](https://github.com/ccj659/incremental-update-master "")
```java

//合并
JNIEXPORT void JNICALL Java_com_example_app_1update_utils_BsPatch_patch
  (JNIEnv *env, jclass jcls, jstring oldfile_jstr, jstring newfile_jstr, jstring patchfile_jstr){
	int argc = 4;
	char* oldfile = (char*)(*env)->GetStringUTFChars(env,oldfile_jstr, NULL);
	char* newfile = (char*)(*env)->GetStringUTFChars(env,newfile_jstr, NULL);
	char* patchfile = (char*)(*env)->GetStringUTFChars(env,patchfile_jstr, NULL);

	//参数（第一个参数无效）
	char *argv[4];
	argv[0] = "bspatch";
	argv[1] = oldfile;
	argv[2] = newfile;
	argv[3] = patchfile;

	bspatch_main(argc,argv);

	(*env)->ReleaseStringUTFChars(env,oldfile_jstr, oldfile);
	(*env)->ReleaseStringUTFChars(env,newfile_jstr, newfile);
	(*env)->ReleaseStringUTFChars(env,patchfile_jstr, patchfile);

}


```
####5.编写更新下载方法

详情请参考代码-[github的android应用项目app_update-](https://github.com/ccj659/incremental-update-master "")
```java

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

```
---

##操作流程

- 确保客户端是old_app
- 改变app大小生成新的new_app
- 执行服务器生成patch程序
- 将patch包放在服务器供客户端下载
- 服务器合并安装

---

---
##About Me


- [我的github](https://github.com/ccj659/)
- [我的csdn博客](http://blog.csdn.net/ccj659/)
