package edu.jhu.hopkinspd;


import java.util.Date;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Message;

import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MainService extends BaseService{
	public static final String TAG = GlobalApp.TAG + "|" + "MainService";
	
	private static final int RECORDER_THREAD_PRIORITY = 8;

	private static final int ONGOING_NOTIFICATION_ID = 1;
	// Main thread to record data
	private RecorderHandlerThread recorder = null;
	
	// Battery Information updated by battery receiver
//	private boolean isCharging;
	
	private PendingIntent zipIntent;
	private AlarmManager am;

//	private boolean runLastUpload = false;
	
	public static final String IS_MONITORING = "is monitoring";
	
	@Override
	public void onCreate() {
    	super.onCreate();
    	// Initialize log file
		logFileName = GlobalApp.LOG_FILE_NAME_MAIN;
		logTextStream = app.openLogTextFile(logFileName);
		app.writeLogTextLine(logTextStream, "onCreate", false);
    	
    	
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		boolean isBatteryOk = app.isBatteryOK(logTextStream);
		boolean isBatteryCharging = app.isBatteryCharging(logTextStream); 
		// if battery is ok, start monitoring and notify the monitoring is started
		if(isBatteryOk && (!isBatteryCharging || app.enableRecordingWhileCharging())){
			startMonitoring();
			boolean watchdog = false;
			if(intent != null && intent.hasExtra("watchdog"))
				watchdog = intent.getBooleanExtra("watchdog", false);
			if(watchdog)
				startNotification(getString(R.string.notification_title), 
						String.format(getString(R.string.notification_running_watchdog), app.prettyDateString(new Date())));
			else
				startNotification(getString(R.string.notification_title), 
						String.format(getString(R.string.notification_running), app.prettyDateString(new Date())));	
		}
		// else notify why the monitoring is paused
		else{
			if(!isBatteryOk){
				startNotification(getString(R.string.notification_title), 
						String.format(getString(R.string.notification_pause_low_battery), app.prettyDateString(new Date())));
			}else if(isBatteryCharging){
				startNotification(getString(R.string.notification_title), 
						String.format(getString(R.string.notification_pause_in_charge), app.prettyDateString(new Date())));
			}
		}
		// TODO start to monitor battery status
		// Register receivers
		// Use this broadcast action to send messages back to the main class
//		registerReceiver(serviceCastRcvr, new IntentFilter(GlobalApp.SERVICE_MSG_ACTION));
		// This one comes back from the Zip service to say it's done zipping
    	registerReceiver(serviceCastRcvr, new IntentFilter(GlobalApp.ZIPPER_DONE_ACTION));
    	registerReceiver(serviceCastRcvr, new IntentFilter(GlobalApp.ZIPPER_START_ACTION));
    	// When the battery level changes
    	registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    	// When the context changes
//		    	registerReceiver(contextReceiver, new IntentFilter("android.intent.action.MAIN"));
//    	if(intent.getBooleanExtra(BootReceiver.REBOOT, false)){
//            if(app.isUploadServiceOn())
//            	app.startUploadService();
//    		else{
//    			String message = "uploadService is off";
//    			Log.v(TAG, message);
//    		}
//    	}
		return START_STICKY;
	}
	
	private void boardcastNotification(String message)
	{
		Intent cast = new Intent(GlobalApp.SERVICE_NOTIFICATION);
		cast.putExtra("stringMsg", message);
		sendBroadcast(cast);
	}

	
	@SuppressWarnings("deprecation")
	private void startNotification(String title, String message) {
		stopForeground(true);
		Notification notification = new Notification(R.drawable.neurometric,
//				getText(R.string.notification),
				message,
		        System.currentTimeMillis());
		// add custom view
		RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_motion);
		
		notification.contentView = contentView;
		Intent notificationIntent = new Intent(this, MainActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(this, title, message, pendingIntent);
		Toast.makeText(app, message, Toast.LENGTH_SHORT).show();
		
		startForeground(ONGOING_NOTIFICATION_ID, notification);
		notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_FOREGROUND_SERVICE | Notification.FLAG_NO_CLEAR;
		 
		boardcastNotification(message);
	}


	private void startMonitoring(){
		// Start recorder/sensors
	    Log.d(TAG, "enter startMonitoring");
		recorder = new RecorderHandlerThread("recorder");
		recorder.setApp((GlobalApp) getApplication());
		recorder.start();
		recorder.prepareHandler();
		// Initialize streams
		recorder.postTask(recorder.initTask);
//        sendRecorderThreadMsg(GlobalApp.STREAM_INIT, this.getApplication());
        startRecording(new Date());
		
		am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		
		// start zip service
		if(app.isZipServiceOn())
		{
			startZipService();
		}else{
			String message = "zipService is off";
			Log.v(TAG, message);
			app.writeLogTextLine(logTextStream, message, false);
		}
	
		// Start upload service
//		if(app.isUploadServiceOn())
//			startUploadService();
//		else{
//			String message = "uploadService is off";
//			Log.v(TAG, message);
//			app.writeLogTextLine(logTextStream, message, false);
//		}
		app.setBooleanPref(IS_MONITORING, true);
		Log.d(TAG, "complete startMonitoring");
	}
	
	private void pauseMonitoring(){
		app.setBooleanPref(IS_MONITORING, false);
		// Stop recording and destroy recorder
    	if(recorder != null)
    	{
    		stopRecording(new Date());
    		recorder.postTask(recorder.destroyTask);
//    		sendRecorderThreadMsg(GlobalApp.STREAM_DESTROY, null);
    		recorder = null;
    	}
    	
    	if(app.isZipServiceOn())
    	{
    		stopZipService(); 
    		// zip all files last time
    		runLastZip();
    	}
    	

    	
    	app.writeLogTextLine(logTextStream, getString(R.string.serviceOnPause), false);
	}
	
	private void runLastZip() {
		Date prevRestart = getDatePref(GlobalApp.PREF_KEY_RECORD_LAST_RESTART);
		if(prevRestart.getTime() != 0)
		{
			app.startZip(prevRestart, new Date());
			app.writeLogTextLine(logTextStream, "Session paused: run last zip", false);
		}
	}

	private void stopMonitoring(){
    	// cancel scheduled services
//    	if(app.isUploadServiceOn())
//    	{
//    		runLastUpload  = true;
//    	}
		if(app.getBooleanPref(IS_MONITORING))
			pauseMonitoring();
		app.setBooleanPref(GlobalApp.PREF_KEY_SWITCH, false);
		// Unregister receivers
		try{
			unregisterReceiver(serviceCastRcvr);
			unregisterReceiver(batteryReceiver);
		}catch(Exception e){
			Log.e(TAG, e.getLocalizedMessage());
		}
    	String message = String.format(getString(R.string.notification_stop), app.prettyDateString(new Date()));
    	Toast.makeText(app, message, Toast.LENGTH_SHORT).show();
		app.writeLogTextLine(logTextStream, getString(R.string.serviceOnDestroy), false);
		stopForeground(true);
	}

	private void startZipService() {
    	// create the pending intent
	    Log.d(TAG, "enter startZipService");
    	Intent intent = new Intent(GlobalApp.ZIPPER_START_ACTION);
		zipIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		// setup alarm service to wake up and start service periodically
		AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		int intervalMillis = Integer.parseInt(app.getStringPref(getString(R.string.zipInterval)));
//		long intervalMillis = interval * 60000;
//		long currTime = SystemClock.elapsedRealtime();
		long currTime = System.currentTimeMillis();
		long triggerAtMillis = currTime/intervalMillis*intervalMillis + intervalMillis;
//		int minutes = (int) ((currTime / (1000*60)) % 60);
//		int residual = minutes % interval;
//		long startTime =  currTime + intervalMillis + residual * 1000*60;
		Log.i(TAG, "startZipService. current " + currTime + " triggerAt " + triggerAtMillis 
				+ " interval "+ intervalMillis);
		
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMillis, zipIntent);
//		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
//				SystemClock.elapsedRealtime(), intervalMills,
//				zipIntent);
		Log.d(TAG, "complete startZipService");
	}

    private void stopZipService() {
    	if(am != null && zipIntent != null)
    		am.cancel(zipIntent);
    }

//	private void startUploadService() {
//		// create the pending intent
//    	Intent intent = new Intent(this, UploadService.class);
//		uploadIntent = PendingIntent.getService(
//				this, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//		// setup alarm service to wake up and start service periodically
//		
//		long intervalMillis =  Integer.parseInt(app.getStringPref(getString(R.string.uploadInterval)));
//		long currTime = System.currentTimeMillis();
//		long triggerAtMillis = currTime/intervalMillis*intervalMillis + intervalMillis;
//		am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMillis, uploadIntent);
////		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
////				SystemClock.elapsedRealtime(), intervalMillis , uploadIntent);
//		Log.i(TAG, "startZipService. current " + currTime + " triggerAt " + triggerAtMillis 
//				+ " interval "+ intervalMillis);
//		
//		Log.i(TAG, "start upload service " + intervalMillis);
//	}
//	
//	private void stopUploadService() {
//		if(am != null && uploadIntent != null)
//			am.cancel(uploadIntent);
//	}






	@Override
	public void onTaskRemoved(Intent rootIntent) {
		// operations when the service is killed by user/apps
    	Log.v(TAG, "be removed.");
    	stopMonitoring();
		super.onTaskRemoved(rootIntent);
	}






	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy()");
		stopMonitoring();
		super.onDestroy();
	}

	
	

   
	
	private boolean startRecording(Date time)
	{
	    recorder.setStartTime(time);
	    recorder.postTask(recorder.startTask);
//        sendRecorderThreadMsg(GlobalApp.STREAM_START, time);
		long tsp = time.getTime();
		this.setLongPref(GlobalApp.PREF_KEY_RECORD_LAST_START, tsp);
		this.setLongPref(GlobalApp.PREF_KEY_RECORD_LAST_RESTART, tsp);
		return true;
	}

	private void stopRecording(Date time)
	{
	    recorder.setStopTime(time);
	    recorder.postTask(recorder.stopTask);
//        sendRecorderThreadMsg(GlobalApp.STREAM_STOP, time);
        this.setLongPref(GlobalApp.PREF_KEY_RECORD_LAST_STOP, time.getTime());
        Log.v(TAG,"stopRecording");
	}

	private void restartRecording(Date prev)
	{
	    recorder.setRestartTime(prev);
	    recorder.postTask(recorder.restartTask);
//        sendRecorderThreadMsg(GlobalApp.STREAM_RESTART, prev);
	}


	private void sendRecorderThreadMsg(int what, Object obj)
    {
        Message msg = new Message();
        msg.what = what;
        msg.obj = obj;
        recorder.handler.sendMessage(msg);
    }
	
	private Runnable zipTask = new Runnable()
	{
		public void run()
		{
			Log.v(TAG, "zipTask run");
			app.writeLogTextLine(logTextStream, "zipTask run", false);
			if (recorder !=null && recorder.getRecorderState() != RecorderThread.STOPPED)
			{
				Date prevRestart = getDatePref(GlobalApp.PREF_KEY_RECORD_LAST_RESTART);
				restartRecording(prevRestart);
		    	app.writeLogTextLine(logTextStream, "Session restarted", false);
			}
		}
	};


	
	// Receivers
	private BroadcastReceiver serviceCastRcvr = new BroadcastReceiver()
	{
		@Override
	    public void onReceive(Context context, Intent intent)
		{
//			if (intent.getAction().equals(GlobalApp.SERVICE_MSG_ACTION))
//			{
//				String stringMsg = intent.getStringExtra("stringMsg");
//				app.writeLogTextLine(logTextStream, stringMsg, false);
//			}
//			else 
			app.writeLogTextLine(logTextStream, "receive zip action broadcast", false);
			if (intent.getAction().equals(GlobalApp.ZIPPER_DONE_ACTION))
			{
				Log.v(TAG, GlobalApp.ZIPPER_DONE_ACTION);
				app.writeLogTextLine(logTextStream, "Session zipped", false);
//				if(runLastUpload)
//					runLastUpload();
			}
			else if (intent.getAction().equals(GlobalApp.ZIPPER_START_ACTION)){
				Log.i(TAG, GlobalApp.ZIPPER_START_ACTION);
				app.writeLogTextLine(logTextStream, "start zip task", false);
				zipTask.run();
			}
	    }
	};
	
	private BroadcastReceiver batteryReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// Monitor current battery level and charge status
			int batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			app.writeLogTextLine(logTextStream, "Battery level now " + batteryLevel + "%", false);
			float batteryTemp = ((float)intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0))/10.0f;
			app.writeLogTextLine(logTextStream, "Battery temperature now " + batteryTemp, false);  
	        int onplug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);  
	        boolean isCharging = onplug != 0;   
	        boolean onUSB = onplug == BatteryManager.BATTERY_PLUGGED_USB;  
	        boolean onAC = onplug == BatteryManager.BATTERY_PLUGGED_AC;  
	        String strStatus = "Charging on ";  
	        if (isCharging && onUSB)  
	        	strStatus += "USB";  
	        else if (isCharging && onAC)  
	            strStatus += "AC Power";  
	        else  
	            strStatus = "Battery Discharging";
	        app.writeLogTextLine(logTextStream, strStatus, false);
	  
	        Date cur = new Date();
	        boolean isBatteryOk = app.isBatteryOK(logTextStream);
	        if(app.getBooleanPref(IS_MONITORING)){
	        	if(!isBatteryOk || (isCharging && !app.enableRecordingWhileCharging()))
	        	{
	        		// Need to stop recording
		        	Log.i(TAG, "Need to stop recording");
		        	pauseMonitoring();
		        	if(!isBatteryOk)
		        		startNotification(getString(R.string.notification_title), 
								String.format(getString(R.string.notification_pause_low_battery), app.prettyDateString(new Date())));
		        	else
		        		startNotification(getString(R.string.notification_title), 
								String.format(getString(R.string.notification_pause_in_charge), app.prettyDateString(new Date())));
	        	}
	        	
	        }else{
	        	// Attempt to start recording
	        	Log.i(TAG, "Attempt to start recording");
	        	if(isBatteryOk && !isCharging)
	        	{
	        		startMonitoring();
	        		startNotification(getString(R.string.notification_title), 
	        			String.format(getString(R.string.notification_resume), app.prettyDateString(cur)));
	        	}
	        }
		}
	};
}
