package com.zvin.wificonnect.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

	private Thread.UncaughtExceptionHandler mDefaultHandler;
	private Context mContext;
	private static CrashHandler instance;

	private CrashHandler() {
	}

	public static CrashHandler getInstance() {
		if (null == instance) {
			instance = new CrashHandler();
		}
		return instance;
	}

	public void init(Context ctx) {
		mContext = ctx;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
//		saveCrash(ex);
		/*if(mContext instanceof WifiObserveService){
			((WifiObserveService)mContext).releaseLock();
		}

		if(mContext instanceof MyApp){
			try {
				((MyApp)mContext).getService().releaseWifiConnectLock();
			} catch (RemoteException e) {
				LogUtil.e(LogUtil.ERROR_TAG, e.getMessage());
			}
		}*/
		mDefaultHandler.uncaughtException(thread, ex);
	}

	private void saveCrash(Throwable ex) {
		String appvesion="";
		String appversionName="";
		PackageManager pm = mContext.getPackageManager();   
        try {
			PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(),   
			        PackageManager.GET_ACTIVITIES);
			appvesion=""+pi.versionCode;
			appversionName=pi.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		StackTraceElement[] trace = ex.getStackTrace();
		ex.setStackTrace(trace);
		ex.printStackTrace(printWriter);
		StringBuffer stacktrace = new StringBuffer(result.toString());
		printWriter.close();
		stacktrace.append("device info:\nMODEL:").append(android.os.Build.MODEL)
				.append("\nVERSION: ")
				.append(String.valueOf(android.os.Build.VERSION.RELEASE))
				.append("\nFINGERPRINT: ").append(android.os.Build.FINGERPRINT)
				.append("\napp version: ").append(appvesion).append("\napp version name: ").append(appversionName);
		LogUtil.writeCrashLog(stacktrace.toString());
	}

}
