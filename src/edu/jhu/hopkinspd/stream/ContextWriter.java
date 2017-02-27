/*
 * Copyright (c) 2015 Johns Hopkins University. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 * - Neither the name of the copyright holder nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.jhu.hopkinspd.stream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.nsd.NsdManager.RegistrationListener;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;
import edu.jhu.hopkinspd.receiver.Alarm;


public class ContextWriter extends StreamWriter
implements GooglePlayServicesClient.ConnectionCallbacks, 
GooglePlayServicesClient.OnConnectionFailedListener
{

	public static final String STREAM_NAME = "context";
	private static final String TAG = GlobalApp.TAG +  "|ContextWriter";
	private static ActivityRecognitionClient mActivityRecognitionClient;
	private static PendingIntent callbackIntent;

	
	private boolean fgAppDetection = true, 
			SCREEN = true;
//	private Alarm fgAppDetector;
	
	public static final String STREAM_NAME_GAR = "activity";
	public static final String STREAM_NAME_APP = "foreapp";
	public static final String STATS_NAME_GAR = "stats/stats_activity";
	private DataOutputStream sensorStreamGar = null, statsStreamGar = null, appStream = null;
	
	
	
	
	public ContextWriter(GlobalApp app){
		super(app);
		logTextStream = app.openLogTextFile(STREAM_NAME);
	    writeLogTextLine("Created " + this.getClass().getName());
	    
	}
	
	@Override
	public void init() {
		if(app.getBooleanPref(app.getString(R.string.sensorGAROn)))
		{
			mActivityRecognitionClient	= new ActivityRecognitionClient(app, this, this);
			writeLogTextLine("ActivityRecognitionClient Initialized");
		}
		Log.v(TAG,"contextWriter initialized");
		writeLogTextLine("contextWriter initialized");
	    
	}
	
    public String toString(){
    	return STREAM_NAME;
    }
	
	public void start(Date startTime)
    {
		isRecording = true;
		String timeStamp = timeString(startTime);
		// activity
		if(app.getBooleanPref(app.getString(R.string.sensorGAROn)))
		{
			mActivityRecognitionClient.connect();
			sensorStreamGar = openStreamFile(STREAM_NAME_GAR, timeStamp, GlobalApp.STREAM_EXTENSION_CSV);
			int intervalSecs = Integer.parseInt(getStringPref(app.getString(R.string.sensorGARInt)));
			statsStreamGar = openStatsFile(STATS_NAME_GAR + "_" + intervalSecs, timeStamp, GlobalApp.STREAM_EXTENSION_CSV);
			app.registerReceiver(contextReceiver, new IntentFilter("android.intent.action.MAIN"));
			writeLogTextLine("ActivityRecognitionClient connecting");
		}
		prevSecs = ((double)startTime.getTime())/1000.0d;
		appStream = openStreamFile(STREAM_NAME_APP, timeStamp, GlobalApp.STREAM_EXTENSION_CSV);
		
	    // screen on/off
	    if(SCREEN){
	    	IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
	    	filter.addAction(Intent.ACTION_SCREEN_OFF);
	    	app.registerReceiver(screenReceiver, filter);
	    }
//	    if(fgAppDetection)
//			fgAppDetector.startForegroundAppDetection(app, this, appStream);
//		fgAppDetector = new Alarm(this, appStream);
	    if(fgAppDetection)
	    	startForegroundAppDetection();
	    writeLogTextLine("activity recognition started");
    }
	
    public void stop(Date stopTime)
    {
    	isRecording = false;
    	if(app.getBooleanPref(app.getString(R.string.sensorGAROn)))
    	{
    		try{
    			app.unregisterReceiver(contextReceiver);
				mActivityRecognitionClient.removeActivityUpdates(callbackIntent);
				Log.d(TAG,"stopActivityRecognitionScan");
				writeLogTextLine("ActivityRecognitionClient stopped");
				closeStreamFile(sensorStreamGar);
				closeStreamFile(statsStreamGar);
			} catch (Exception e){
				// probably the scan was not set up, we'll ignore
				Log.e(TAG,"probably the scan was not set up, we'll ignore:" + e.getLocalizedMessage());
			}
    	}
    	
    	if (SCREEN)
    		app.unregisterReceiver(screenReceiver);
    	if(fgAppDetection)
    		cancelForegroundAppDetection();
    	closeStreamFile(appStream);
    } 
    
    public void restart(Date time)
    {
    	String timeStamp = timeString(time);
    	if(app.getBooleanPref(app.getString(R.string.sensorGAROn)))
    	{
    		DataOutputStream oldCsv = sensorStreamGar;
    		sensorStreamGar= openStreamFile(STREAM_NAME_GAR, timeStamp, GlobalApp.STREAM_EXTENSION_CSV);
    		if(closeStreamFile(oldCsv)){
    			writeLogTextLine("Google activity successfully restarted");
    		}
    		oldCsv = statsStreamGar;
    		int intervalSecs = Integer.parseInt(getStringPref(app.getString(R.string.sensorGARInt)));
    		statsStreamGar= openStatsFile(STATS_NAME_GAR + "_" + intervalSecs, timeStamp, GlobalApp.STREAM_EXTENSION_CSV);
    		closeStreamFile(oldCsv);
    	}
    	DataOutputStream oldCsv = appStream;
    	appStream = openStreamFile(STREAM_NAME_APP, timeStamp, GlobalApp.STREAM_EXTENSION_CSV);
    	if(closeStreamFile(oldCsv)){
			writeLogTextLine("context writer successfully restarted");
		}
    	prevSecs = ((double)time.getTime())/1000.0d;
    	
    }
    
    public void destroy()
    {
    	mActivityRecognitionClient = null;
    	callbackIntent = null;
        
		writeLogTextLine(this.getClass().getName() + " destroied");
    }
	
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		writeLogTextLine("ActivityRecognitionClient connection failed");
		Log.d(TAG,"onConnectionFailed");
		// broadcast to MainActivity
		Intent i = new Intent("android.intent.action.MAIN").putExtra(
				GlobalApp.CONTEXT_ACTIVITY_CONNECTION, false);
		app.sendBroadcast(i);
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Intent intent = new Intent(app, ActivityRecognitionService.class);
		callbackIntent = PendingIntent.getService(app, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		int interval = 1000*Integer.parseInt(getStringPref(app.getString(R.string.sensorGARInt)));
		mActivityRecognitionClient.requestActivityUpdates(
				interval, callbackIntent); // 0 sets it to update as fast as possible, just use this for testing!
		Log.v(TAG, "connected to GAR " + interval);
		writeLogTextLine("ActivityRecognitionClient connected with interval " + interval + " seconds");
		Intent i = new Intent("android.intent.action.MAIN").putExtra(
				GlobalApp.CONTEXT_ACTIVITY_CONNECTION, true);
		app.sendBroadcast(i);
	}

	@Override
	public void onDisconnected() {
		writeLogTextLine("ActivityRecognitionClient disconnected");
	}
	
	
	

	
	private BroadcastReceiver screenReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	String current = app.prettyDateString(new Date());
	        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
	            // DO WHATEVER YOU NEED TO DO HERE
	            Log.i(TAG, "screen off");
	            
				String line = current + ",ACTION_SCREEN_OFF";
				writeTextLine(line, appStream);
	            if(fgAppDetection)
	            	cancelForegroundAppDetection();
	        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
	            // AND DO WHATEVER YOU NEED TO DO HERE
	            Log.i(TAG, "screen on");
	            String line = current + ",ACTION_SCREEN_ON";
				writeTextLine(line, appStream);
	            if(fgAppDetection)
	            	startForegroundAppDetection();
	        }
	    }
	 
	};


	
	private BroadcastReceiver contextReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.hasExtra(GlobalApp.CONTEXT_ACTIVITY)){
				//<time><type:int><type:str><confidence>
				String[] items = intent.getStringArrayExtra(GlobalApp.CONTEXT_ACTIVITY);
				String time = app.prettyDateString(new Date(Long.parseLong(items[0])));
				String line = time + "," + items[1] + "," + items[2] + "," + items[3] + "\n";
				try {
					sensorStreamGar.writeChars(line);
					sensorStreamGar.flush();
					statsStreamGar.writeChars(line);
					statsStreamGar.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(intent.hasExtra(GlobalApp.CONTEXT_ACTIVITY_CONNECTION)){
				boolean connected = intent.getBooleanExtra(GlobalApp.CONTEXT_ACTIVITY_CONNECTION, false);
				if(connected)
					writeLogTextLine("Google Activity connected");
				else
					writeLogTextLine("Google Activity connection failed");
				
			}
		}
	};
	
	private PendingIntent pi;
	public static final String ACTION_APP_DETECTION = "android.intent.action.ForegroundAppDetection";
	public void startForegroundAppDetection(){
		Log.i(TAG, "startForegroundAppDetection");
		if(pi != null)
			return;
		app.registerReceiver(foregroundAppDetector, new IntentFilter(ACTION_APP_DETECTION));
		AlarmManager am = (AlarmManager)app.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(ACTION_APP_DETECTION);
        pi = PendingIntent.getBroadcast(app, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 5, pi); // 5 seconds
       
	}
	
	public void cancelForegroundAppDetection(){
		try{
			app.unregisterReceiver(foregroundAppDetector);
			if(pi != null)
	        {	
				AlarmManager alarmManager = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
				alarmManager.cancel(pi);
				pi = null;
				Log.i(TAG, "cancelForegroundAppDetection");
	        }
		}catch(Exception e){
			writeLogTextLine(e.toString());
		}
        
	}
	private BroadcastReceiver foregroundAppDetector = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			int api_level = android.os.Build.VERSION.SDK_INT;
			Log.d(TAG, "api_level:" + api_level);
			if(api_level < 21){
				Date now = new Date();
		    	String prettyDate = DateFormat.format("yyyy/MM/dd kk:mm:ss", now).toString();
				
				ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
				if(am.getRunningTasks(1).size() > 0){
					String packageName = am.getRunningTasks(1).get(0).topActivity.getPackageName();
					String className = am.getRunningTasks(1).get(0).topActivity.getClassName();
					String line = prettyDate + "," + packageName + "," + className; 
					Log.i(TAG, line);
					if(appStream != null)
					{	
						try
						{
							writeTextLine(line, appStream);
						}catch(NullPointerException e){
							Log.w(TAG, e.getMessage());
						}
					}	
				}
					
			}
			else{
				// TODO api 21
//				mUsageStatsManager = (UsageStatsManager) getActivity()
//					       .getSystemService(Context.USAGE_STATS_SERVICE);
			}
		}
		
	};
}
