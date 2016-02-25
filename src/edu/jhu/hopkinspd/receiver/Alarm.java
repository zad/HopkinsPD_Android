package edu.jhu.hopkinspd.receiver;

import java.io.DataOutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.stream.ContextWriter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.format.DateFormat;
import android.util.Log;

@SuppressLint("Instantiatable")
public class Alarm extends BroadcastReceiver {

	private static final String TAG = GlobalApp.TAG + "|Alarm";
	private PendingIntent pi;
	private ContextWriter writer;
	private DataOutputStream stream;
	public Alarm(ContextWriter contextWriter, DataOutputStream appStream) {
		super();
		this.writer = contextWriter;
		this.stream = appStream;
	}



	@Override
	public void onReceive(Context context, Intent intent) {
		Date now = new Date();
    	String prettyDate = DateFormat.format("yyyy/MM/dd kk:mm:ss", now).toString();
		
		ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
		
		String packageName = am.getRunningTasks(1).get(0).topActivity.getPackageName();
		String className = am.getRunningTasks(1).get(0).topActivity.getClassName();
		String line = prettyDate + "," + packageName + "," + className; 
		Log.i(TAG, line);
		if(writer != null && stream != null)
		{	
			try
			{
				writer.writeTextLine(line, stream);
			}catch(NullPointerException e){
				Log.w(TAG, e.getMessage());
			}
		}
	}
	


	public void startForegroundAppDetection(Context localCtx){
		if(pi != null)
			return;
		AlarmManager am = (AlarmManager)localCtx.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent("android.intent.action.ALARM");
        pi = PendingIntent.getBroadcast(localCtx, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 5, pi); // 5 seconds
        Log.i(TAG, "startForegroundAppDetection");
	}
	
	public void cancelForegroundAppDetection(Context localCtx){
		if(pi != null)
        {	
			AlarmManager alarmManager = (AlarmManager) localCtx.getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(pi);
			pi = null;
			Log.i(TAG, "cancelForegroundAppDetection");
        }
        
	}
}
