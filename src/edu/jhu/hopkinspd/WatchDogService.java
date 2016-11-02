package edu.jhu.hopkinspd;

import java.io.BufferedWriter;
import java.io.File;
import java.util.Date;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class WatchDogService extends IntentService{

	private static final String TAG = "watchdog";
	private static final long WATCHDOG_THRESHOLD = 5*60*1000;	// 5 mintues
	private GlobalApp app;
	protected BufferedWriter logTextStream = null;
	protected String logFileName = "";
	
	public WatchDogService() {
		super(TAG);
		
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(TAG, "watchdog Service started");
		app = GlobalApp.getApp();
		logFileName = GlobalApp.LOG_FILE_NAME_WATCHDOG;
		// Open log file
		logTextStream = app.openLogTextFile(logFileName);
		app.writeLogTextLine(logTextStream, "watchdog Service started", false);
		// is recording required to be on?
		if(recordingRequired())
		{
			app.writeLogTextLine(logTextStream, "recording is required right now", false);
			// if yes, is recording running now? look at streams/ files
			if(isRecordingRunning()){
				app.writeLogTextLine(logTextStream, "recording is running right now", false);
			}else{
				app.writeLogTextLine(logTextStream, "recording is not running right now, mainService/recordThread may be killed", false);
				// if recording thread has been killed, start main service again!
				app.writeLogTextLine(logTextStream, "start main service from watchdog", false);
				app.startMainService(true);
			}
			
		}else
			app.writeLogTextLine(logTextStream, "recording is not required right now", false);
		
	}

	

	private boolean isRecordingRunning() {
		String rootPath = app.getStringPref(GlobalApp.PREF_KEY_ROOT_PATH);
		String streamsPath = rootPath + "/" + GlobalApp.STREAMS_SUBDIR;
		File streamsDir = new File(streamsPath);
		long last = 0;
		File[] files = streamsDir.listFiles();
		if(files != null)
		{
    		for(File streamFile : files){
    			long lastM = streamFile.lastModified();
    			if(lastM > last) last = lastM;
    		}
    		long current = (new Date()).getTime();
    		if(current - last > WATCHDOG_THRESHOLD){
    			return false;
    		}else
    			return true;
		}else
		    return false;
	}

	private boolean recordingRequired() {
		if(app.getBooleanPref(GlobalApp.PREF_KEY_SWITCH)){
			boolean isBatteryOK = app.isBatteryOK(logTextStream);
			boolean isSDCardOK = app.isSDCardOK(logTextStream, null);
			boolean isBatteryCharging = app.isBatteryCharging(logTextStream); 
			if(isBatteryOK || isSDCardOK){
				if(!isBatteryCharging || app.enableRecordingWhileCharging()){
					return true;
				}
			}
		}
		return false;
	}



}
