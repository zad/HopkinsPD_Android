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
package edu.jhu.hopkinspd;

import java.io.BufferedWriter;
import java.text.DecimalFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;

import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

 
public class FragmentTabMonitor extends SherlockFragment implements SensorEventListener{
	public static final String TAG = GlobalApp.TAG + "|" + "FragmentTab1";

	
	
	BufferedWriter logTextStream = null;
	
	private Button recordButton = null;
	private TextView timeText = null;
	private TextView promptText = null;

	private static final int SENSOR_TYPE = Sensor.TYPE_PROXIMITY;
	private static final int SENSOR_RATE = SensorManager.SENSOR_DELAY_FASTEST;
	private SensorManager sensorManager = null;
	private Sensor sensor = null;

	
	/**
	 * Intents for services
	 */
	private Intent mainIntent = null;

	
	/**
	 * Battery Info
	 */
	
	
	private boolean isCharging;
	private GlobalApp app; 
    @Override
    public SherlockFragmentActivity getSherlockActivity() {
        return super.getSherlockActivity();
    }
 
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Get the view from fragmenttab1.xml
        
        app = (GlobalApp)getActivity().getApplication();

		View view = inflater.inflate(R.layout.fragmenttab1, container, false);
       
		
//		Date now = new Date();
        recordButton = (Button)view.findViewById(R.id.logbutton);
        recordButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				clickRecord(v);
			}
        	
        });
        timeText = (TextView)view.findViewById(R.id.timertext);
//        counterText = (TextView)view.findViewById(R.id.messagetext);
        promptText = (TextView)view.findViewById(R.id.prompttext);
//        counterText.setText("Current time\n" + app.prettyDateString(now));
        promptText.setText("Initializing ...");
        
        sensorManager = (SensorManager)app.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(SENSOR_TYPE);
        sensorManager.getDefaultSensor(SENSOR_TYPE);
        sensorManager.registerListener(this, sensor, SENSOR_RATE);
	    
        
        // Check SD card is available to write on
        String sdCardState = Environment.getExternalStorageState();
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() 
        		+ "/" + getString(R.string.app_name);
        
        // Store path in shared preferences accessible to all code
        app.setStringPref(GlobalApp.PREF_KEY_ROOT_PATH, rootPath);
        
        if(sdCardState.equals(Environment.MEDIA_MOUNTED)){
        	// Make output directory
        	app.createDir();
	        logTextStream = app.openLogTextFile(GlobalApp.LOG_FILE_NAME_UI);
	        app.writeLogTextLine(logTextStream, "Application started", false);
	        app.writeLogTextLine(logTextStream, "Preferences:", false);
	        app.writeLogTextLine(logTextStream, GlobalApp.PREF_KEY_ROOT_PATH + ": " + app.getStringPref(GlobalApp.PREF_KEY_ROOT_PATH), false);
	        app.writeLogTextLine(logTextStream, GlobalApp.PREF_KEY_USE_MOBILE_INTERNET + ": " + app.getBooleanPref(GlobalApp.PREF_KEY_USE_MOBILE_INTERNET), false);
	        app.writeLogTextLine(logTextStream, GlobalApp.PREF_KEY_USERID + ": " + app.getStringPref(GlobalApp.PREF_KEY_USERID), false);
	        
	        
	        
	        // Check available space
	        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
			double sdAvailBytes = (double)stat.getAvailableBlocks() * (double)stat.getBlockSize();
			double sdAvailableGb = sdAvailBytes / 1073741824;
	        DecimalFormat df = new DecimalFormat("#.##");
	        app.writeLogTextLine(logTextStream, "SD card ready with " + df.format(sdAvailableGb) + "Gb free", false);
  
	        
	    	promptText.setText("Not recording.\nPress Start to begin.");
        }else{
        	
        	Toast.makeText(app, getString(R.string.app_name) + ": SD card not ready, prepare SD card and restart", Toast.LENGTH_SHORT).show();
	    	promptText.setText("SD card not ready\nPrepare card and restart.");
        }
        return view;
    }
 
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        setUserVisibleHint(true);
    }
 
    // Receivers
 	private BroadcastReceiver batteryReceiver = new BroadcastReceiver()
 	{
 		

 		@Override
 		public void onReceive(Context context, Intent intent)
 		{
 			int batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
 			app.writeLogTextLine(logTextStream, "Battery level now " + batteryLevel + "%", false);
 			float batteryTemp = ((float)intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0))/10.0f;
 			app.writeLogTextLine(logTextStream, "Battery temperature now " + batteryTemp, false);
 			int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);  
 	        int onplug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);  
 	        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;  
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
 		}
 	};
 	
 	@Override
	public void onPause() {
 		Log.v(TAG, "onPause");
		super.onPause();
		// Unregister receivers
		app.unregisterReceiver(batteryReceiver);
		app.unregisterReceiver(contextReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.v(TAG, "onResume");
		// Register receivers
		// When the battery level changes
		app.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		app.registerReceiver(contextReceiver, new IntentFilter("android.intent.action.MAIN"));
    	// Display
    	if(app.getBooleanPref(GlobalApp.PREF_KEY_SWITCH)){
    		recordButton.setText(R.string.record_stop);
    		promptText.setText("Recording started.\nPress Stop to end.");
    		Date lastStarted = app.getDatePref(GlobalApp.PREF_KEY_RECORD_LAST_START);
        	String prettyDate = DateFormat.format("yyyy/MM/dd kk:mm:ss", lastStarted).toString();
    		timeText.setText("Session last started\n" + prettyDate);
    	}else
    	{
    		recordButton.setText(R.string.record_start);
    		promptText.setText("Not recording.\nPress Start to begin.");
    	}
    	
    
	}

	private BroadcastReceiver contextReceiver = new BroadcastReceiver()
 	{

 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if(intent.hasExtra(GlobalApp.CONTEXT_ACTIVITY)){
 				String[] items = intent.getStringArrayExtra(GlobalApp.CONTEXT_ACTIVITY);
 				Toast.makeText(app, items[2], Toast.LENGTH_SHORT).show();
 			}
 			else if(intent.hasExtra(GlobalApp.CONTEXT_ACTIVITY_CONNECTION)){
 				boolean connected = intent.getBooleanExtra(GlobalApp.CONTEXT_ACTIVITY_CONNECTION, false);
 				if(connected)
 					Toast.makeText(app, "Google Activitiy Recognition Connected", Toast.LENGTH_SHORT).show();
 				else
 					Toast.makeText(app, "Google Activitiy Recognition Connection Failed", Toast.LENGTH_SHORT).show();
 				
 			}
 		}
 		
 	};
 	
 	public void clickRecord(View v){
		Log.v(TAG,"clickRecord");
		app = (GlobalApp)getActivity().getApplication();
		if(!app.isUserInfoAvailable()){
			app.showUserSettings();
			Toast.makeText(app, R.string.msgSetupPrefs, Toast.LENGTH_LONG).show();
			return;
		}
		
		Date now = new Date();
    	String prettyDate = DateFormat.format("yyyy/MM/dd kk:mm:ss", now).toString();

		if(app.getBooleanPref(GlobalApp.PREF_KEY_SWITCH)){
			// stop recording
			app.writeLogTextLine(logTextStream, getString(R.string.userClickOff),true);
			app.setBooleanPref(GlobalApp.PREF_KEY_SWITCH, false);
    		recordButton.setText(R.string.record_start);
    		promptText.setText("Not recording.\nPress Start to begin.");
    		timeText.setText("Session last stopped\n" + prettyDate);
    		if(mainIntent == null)
    			mainIntent = new Intent(app, MainService.class);
    		app.stopService(mainIntent);
    		app.setBooleanPref(GlobalApp.PREF_KEY_SWITCH, false);
    	}else
    	{
    		app.writeLogTextLine(logTextStream, getString(R.string.userClickOn),true);
    		boolean isBatteryOK = app.isBatteryOK(logTextStream);
    		boolean isSDCardOK = app.isSDCardOK(logTextStream, promptText);
    		if(isBatteryOK && isSDCardOK)
    		{	
    			// start recording
    			app.setBooleanPref(GlobalApp.PREF_KEY_SWITCH, true);
    			recordButton.setText(R.string.record_stop);
    			mainIntent = new Intent(app, MainService.class);
    			app.startService(mainIntent);
    			promptText.setText("Recording started.\nPress Stop to finish.");
    			timeText.setText("Session last started\n" + prettyDate);
    		}else{
    			// cannot start to record
    			if(!isBatteryOK)
    			{
    				promptText.setText("Fail to start recording.\nBattery is too low.");
    				app.writeLogTextLine(logTextStream, getString(R.string.userClickFailBattery),true);
    			}
    			else
    			{
    				promptText.setText("Fail to start recording.\nSD card is not ready.");
    				app.writeLogTextLine(logTextStream, getString(R.string.userClickFailSDCard),true);
    			}
    		}
    		
    	}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.values[0] > 0)
		{
			recordButton.setEnabled(true);
		}
		else
		{
			recordButton.setEnabled(false);
		}
	}
 	

    
}
