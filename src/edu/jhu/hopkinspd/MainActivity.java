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
import java.io.IOException;
import java.text.DecimalFormat;

import java.util.Date;
import java.util.regex.Pattern;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StatFs;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import edu.jhu.hopkinspd.medlog.MedLogActivity;
import edu.jhu.hopkinspd.medtracker.MedTrackerActivity;
import edu.jhu.hopkinspd.task.NTPSyncTask;
import edu.jhu.hopkinspd.test.TestPrepActivity;


public class MainActivity extends Activity implements SensorEventListener{
	
    private GlobalApp app;
    BufferedWriter logTextStream = null, motionLabelStream = null;
	
	private Button recordButton = null, dashButton = null;
	private Button medButton, medLogButton;
	private TextView timeText = null;
	private TextView promptText = null;

	private static final int SENSOR_TYPE = Sensor.TYPE_PROXIMITY;
	private static final int SENSOR_RATE = SensorManager.SENSOR_DELAY_FASTEST;
	private static final String TAG = MainActivity.class.getName();
	private SensorManager sensorManager = null;
	private Sensor sensor = null;
	
	private Button testButton = null;
	private Spinner spinner = null;
	/**
	 * Intents for services
	 */
//	private Intent mainIntent = null;
	
	/**
	 * Battery Info
	 */	
	private boolean isCharging;
	
	
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the view from activity_main.xml
        setContentView(R.layout.mainactivity);
        
        app = (GlobalApp) getApplication();
//        app.checkVersion();
        
        String version = app.getVersion();
        if(version != null)
        	setTitle(getString(R.string.app_name)+ " " + version);

        spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, GlobalApp.MOTIONS);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            	int past = app.getIntPref("motion");
            	if(past != position)
            	{
            		// position changed
            		Toast.makeText(app, "Your current motion is set to " + GlobalApp.MOTIONS[position], Toast.LENGTH_SHORT).show();
                	String current = app.prettyDateString(new Date());
                	String motion_msg = current + ", " + position + ", " + GlobalApp.MOTIONS[position] + "\n";
                	
                	
                	app.setIntPref("motion", position);
                	try {
    					motionLabelStream.write(motion_msg);
    					motionLabelStream.flush();
    				} catch (IOException e) {
    					
    					e.printStackTrace();
    				}
            	}
            	
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        
        medButton = (Button)findViewById(R.id.medButton);
        medButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent in = new Intent(app, MedTrackerActivity.class);
				startActivity(in);

			}
			
		});
        
        medLogButton = (Button)findViewById(R.id.medLogButton);
        medLogButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent in = new Intent(app, MedLogActivity.class);
                startActivity(in);

            }
            
        });
        
        testButton = (Button)findViewById(R.id.testButton);
        
        
        
        testButton.setOnClickListener(new OnClickListener(){
       
			@Override
			public void onClick(View v) {
				Log.i(TAG, "Take test button pressed");
				if(!app.isUserInfoAvailable()){
					app.showUserSettings();
					Toast.makeText(app, R.string.msgSetupPrefs, 
					        Toast.LENGTH_LONG).show();
					return;
				}
				
				if(!app.getBooleanPref(getString(R.string.medLogOn))){
				    startTests();
				    return;
				}
				
				// if needs med update, dialog to ask for med update
				Date lastMedUpdate = 
				        app.getDatePref(MedLogActivity.LastMedUpdateDatePref);
				int day_diff = (int) ((System.currentTimeMillis() - 
				        lastMedUpdate.getTime())/1000/3600/24);
				Log.d(TAG, "day_diff:" + day_diff);
				if(day_diff > 90)
				{
				    // ask for med update
				    AlertDialog.Builder alertDialogBuilder = 
				            new AlertDialog.Builder(MainActivity.this);

		            // set title
		            alertDialogBuilder.setTitle(
		                    "Please update your medication log");

		            // set dialog message
		            alertDialogBuilder
		                .setMessage("Click yes to view medication log!")
		                .setCancelable(false)
		                .setPositiveButton("Yes",
		                        new DialogInterface.OnClickListener() {
		                    public void onClick(DialogInterface dialog,int id) {
		                        app.setDatePref(
		                                MedLogActivity.LastMedUpdateDatePref, 
		                                new Date());
		                        Intent medUpdate = new Intent(app, 
		                                MedLogActivity.class);
		                        startActivity(medUpdate);
		                    }
		                });

	                // create alert dialog
	                AlertDialog alertDialog = alertDialogBuilder.create();

	                // show it
	                alertDialog.show();
		            
			        
				}else{
    				// if the user is taking meds, 
				    boolean takingMeds = 
				            app.getBooleanPref(MedLogActivity.TakingMedsPref);
				    if(takingMeds)
				    {
				        AlertDialog.Builder builderSingle = 
				                new AlertDialog.Builder(MainActivity.this);
				        builderSingle.setIcon(R.drawable.ic_launcher);
				        builderSingle.setTitle("When did you take your medications:");

				        final ArrayAdapter<String> arrayAdapter = 
				                new ArrayAdapter<String>(
				                    MainActivity.this,
				                    android.R.layout.select_dialog_singlechoice);
				        for(String time : MedLogActivity.RecentMedTakeTime){
				            arrayAdapter.add(time);
				        }
//				        builderSingle.setCancelable(false);
				        builderSingle.setNegativeButton("Cancel", 
			                new DialogInterface.OnClickListener() {
                                
                                @Override
                                public void onClick(DialogInterface dialog, 
                                        int which) {
                                    dialog.dismiss();
                                }
                            });
				        // ask the most recent time of med intake
				        builderSingle.setAdapter(
			                arrayAdapter,
			                new DialogInterface.OnClickListener() {
			                    @Override
			                    public void onClick(DialogInterface dialog, int which) {
			                        final String strName = arrayAdapter.getItem(which);
			                        AlertDialog.Builder builderInner = new AlertDialog.Builder(
			                                MainActivity.this);
			                        builderInner.setMessage(strName);
			                        builderInner.setTitle("Your Selected Item is");
			                        builderInner.setPositiveButton(
		                                "Ok",
		                                new DialogInterface.OnClickListener() {
		                                    @Override
		                                    public void onClick(
		                                            DialogInterface dialog,
		                                            int which) {
		                                        dialog.dismiss();
		                                        app.saveRecentMedIntake(strName);
		                                        startTests();
		                                    }

                                            
		                                });
			                        builderInner.show();
			                    }
			                });
				        builderSingle.show();
				    }
				    else
				        startTests();
    				
				}
								
			}

            private void startTests() {
                if(app.getBooleanPref(getString(R.string.single_test))){
                    // start test selection dialog
                    Intent selectTest = new Intent(app, SelectTestActivity.class);
                    startActivity(selectTest);
                }else{
                    Intent takeTests = new Intent(app, TestPrepActivity.class);

                    // Jump to specific test
//                  takeTests.putExtra("TestNumber", GlobalApp.TEST_REACTION);
                    app.initActiveTests();
                    startActivity(takeTests);
    
                }
            }
        	
        });
		
        recordButton = (Button)findViewById(R.id.logbutton);
        recordButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				clickRecord(v);
			}
        	
        });
        
        dashButton = (Button)findViewById(R.id.dashbutton);
        dashButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent dash = new Intent(app, Dashboard.class);
				startActivity(dash);
			}
        	
        });
        
        timeText = (TextView)findViewById(R.id.timertext);
        promptText = (TextView)findViewById(R.id.prompttext);
        
        sensorManager = (SensorManager)app.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(SENSOR_TYPE);
        sensorManager.getDefaultSensor(SENSOR_TYPE);
        sensorManager.registerListener(this, sensor, SENSOR_RATE);
	    
        // Check SD card is available to write on
        boolean isSDCardOK = app.isSDCardOK(logTextStream, promptText);
//        String sdCardState = Environment.getExternalStorageState();
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() 
        		+ "/" + getString(R.string.app_name);
        Log.i(TAG, "rootPath: " + rootPath);
        // Store path in shared preferences accessible to all code
        app.setStringPref(GlobalApp.PREF_KEY_ROOT_PATH, rootPath);
        
        if(isSDCardOK){
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
			@SuppressWarnings("deprecation")
			double sdAvailBytes = (double)stat.getAvailableBlocks() * (double)stat.getBlockSize();
			double sdAvailableGb = sdAvailBytes / 1073741824;
	        DecimalFormat df = new DecimalFormat("#.##");
	        app.writeLogTextLine(logTextStream, "SD card ready with " + df.format(sdAvailableGb) + "Gb free", false);
	        promptText.setText("R.string.prompt_ready");
        }else{
        	
        	Toast.makeText(app, getText(R.string.sdcard_not_ready), Toast.LENGTH_SHORT).show();
	    	promptText.setText(R.string.prompt_sdcard_not_ready);
        }
        
        if(!app.isUserInfoAvailable()){
        	enterUserInfo();
			Toast.makeText(app, R.string.msgSetupPrefs, Toast.LENGTH_LONG).show();
		}
    }
	
	
	private void enterUserInfo(){
		LayoutInflater factory = LayoutInflater.from(this);            
	    final View textEntryView = GlobalApp.FIXED_CONFIG_USERPASSWORD ? 
	    		factory.inflate(R.layout.userid, null):
	    		factory.inflate(R.layout.user, null);
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("User Register");
		
		dialog.setView(textEntryView);
		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				String newUserID = ((EditText)textEntryView.findViewById(R.id.userNameEditText)).getText().toString();
				boolean match = Pattern.matches(GlobalApp.USERID_REX, newUserID);
				if (!match)
				{
					Toast.makeText(app, R.string.prompt_userid_invalid, Toast.LENGTH_LONG).show();
					enterUserInfo();
				}else
				{
					app.setStringPref(GlobalApp.PREF_KEY_USERID, newUserID);
					if(!GlobalApp.FIXED_CONFIG_USERPASSWORD){
						String newPwd = ((EditText)textEntryView.findViewById(R.id.passwordEditText)).getText().toString();
						app.setStringPref(GlobalApp.PREF_KEY_ENCRYPT_KEY, newPwd);
					}
					
//					Log.i(TAG, newUserID+" "+newPwd);
//					if(newUserID == "" || newPwd == "")
//					{
//						Toast.makeText(app, "You need to input your USER ID and PASSWORD to enter this app.", Toast.LENGTH_SHORT).show();
//				    	finish();
//					}
					String message = "new user id " + newUserID;
					app.writeLogTextLine(logTextStream, message, false);
					GlobalApp.clearAllDataFiles();
					return;
				}
				
			}
		});
		dialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(app, getText(R.string.userinfo_request), Toast.LENGTH_SHORT).show();
		    	finish();
			}
			
		});
		dialog.show();
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
		String sdCardState = Environment.getExternalStorageState();
        if(sdCardState.equals(Environment.MEDIA_MOUNTED)){
			// Unregister receivers
			app.unregisterReceiver(batteryReceiver);
			app.unregisterReceiver(contextReceiver);
			app.unregisterReceiver(serviceCastRcvr);
        }
	}
 	
 	// Receivers
 	private BroadcastReceiver serviceCastRcvr = new BroadcastReceiver()
 	{
 		@Override
 	    public void onReceive(Context context, Intent intent)
 		{
 			if (intent.getAction().equals(GlobalApp.SERVICE_MSG_ACTION))
 			{
 				String stringMsg = intent.getStringExtra("stringMsg");
 				app.writeLogTextLine(logTextStream, stringMsg, true);
 				
 			}else if(intent.getAction().equals(GlobalApp.SERVICE_NOTIFICATION)){
 				String stringMsg = intent.getStringExtra("stringMsg");
 				promptText.setText(stringMsg);
 			}
 		}
 	};
 	

 	
	@Override
	public void onResume() {
		super.onResume();

		Log.v(TAG, "onResume");
		testButton.setText(R.string.testButton);
		promptText.setText(R.string.prompt_init);
        
		// Check SD card is available to write on
		boolean isSDCardOK = app.isSDCardOK(logTextStream, promptText);
//        String sdCardState = Environment.getExternalStorageState();
        if(isSDCardOK){
    		// Register receivers
    		// When the battery level changes
            app.registerReceiver(serviceCastRcvr, new IntentFilter(GlobalApp.SERVICE_MSG_ACTION));
            app.registerReceiver(serviceCastRcvr, new IntentFilter(GlobalApp.SERVICE_NOTIFICATION));
    		app.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    		app.registerReceiver(contextReceiver, new IntentFilter("android.intent.action.MAIN"));
        	// Display
        	if(app.getBooleanPref(GlobalApp.PREF_KEY_SWITCH)){
        		recordButton.setText(R.string.record_stop);
        		promptText.setText(R.string.prompt_on);
        		Date lastStarted = app.getDatePref(GlobalApp.PREF_KEY_RECORD_LAST_START);
            	String prettyDate = DateFormat.format("yyyy/MM/dd kk:mm:ss", lastStarted).toString();
        		timeText.setText("Session last started\n" + prettyDate);
        	}else
        	{
        		recordButton.setText(R.string.record_start);
        		promptText.setText(R.string.prompt_ready);
        	}
        	if(app.getBooleanPref(getString(R.string.motionLabelOn)))
        	{
        		int position = app.getIntPref("motion");
        		spinner.setSelection(position);
        		spinner.setVisibility(View.VISIBLE);
        		motionLabelStream = app.openLogTextFile(GlobalApp.LOG_FILE_NAME_MOTION);
        	}else{
        		spinner.setVisibility(View.GONE);
        		app.closeLogTextStream(motionLabelStream);
        	}

        }else{
        	finish();
        }
        
        if(app.getBooleanPref(getString(R.string.dashboardOn))){
        	dashButton.setVisibility(View.VISIBLE);
        	dashButton.setText("Dashboard");
        }else
        	dashButton.setVisibility(View.GONE);
        
        if(app.getBooleanPref(getString(R.string.medTrackerOn))){
        	medButton.setVisibility(View.VISIBLE);
        	
        }else
        	medButton.setVisibility(View.GONE);
        
        if(app.getBooleanPref(getString(R.string.medLogOn))){
            medLogButton.setVisibility(View.VISIBLE);
            
        }else
            medLogButton.setVisibility(View.GONE);
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
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    super.onPrepareOptionsMenu(menu);
	    MenuItem clearDataItem = menu.findItem(R.id.clear_data);
	    clearDataItem.setVisible(app.getBooleanPref(getString(R.string.clearDataOn)));
	    clearDataItem.setEnabled(app.getBooleanPref(getString(R.string.clearDataOn)));
	    MenuItem uploadItem = menu.findItem(R.id.uploadItem);
	    uploadItem.setVisible(app.getBooleanPref(getString(R.string.uploadItemOn)));
	    uploadItem.setEnabled(app.getBooleanPref(getString(R.string.uploadItemOn)));
	    MenuItem ntpItem = menu.findItem(R.id.ntpItem);
	    ntpItem.setVisible(app.getBooleanPref(getString(R.string.ntpItemOn)));
	    ntpItem.setEnabled(app.getBooleanPref(getString(R.string.ntpItemOn)));
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected( MenuItem item) {
		// Handle item selection
		switch(item.getItemId()){
			case R.id.action_settings:
				if(app.getBooleanPref(GlobalApp.PREF_KEY_SWITCH)){
					Toast.makeText(getApplicationContext(), R.string.forbid_to_enter_settings, Toast.LENGTH_SHORT).show();
				}else
					app.showUserSettings();
				return true;
			case R.id.clear_data:
				new ClearDataTask().execute("");
				return true;
			case R.id.uploadItem:
				app.runLastUpload(this, this.logTextStream);
				return true;
			case R.id.ntpItem:
				Toast.makeText(app, R.string.toast_ntp_start, Toast.LENGTH_SHORT).show();
				new NTPSyncTask().execute();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private class ClearDataTask extends AsyncTask<String,String,String> {

		@Override
		protected String doInBackground(String... arg0) {
			app.clearData();
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(app, R.string.data_cleared, Toast.LENGTH_LONG).show();
			super.onPostExecute(result);
		}
		
		
	}
	
	
	
 	public void clickRecord(View v){
		Log.v(TAG,"clickRecord");
		// crash test
//		app.crashTest();
		
		Date now = new Date();
    	String prettyDate = DateFormat.format("yyyy/MM/dd kk:mm:ss", now).toString();

		if(app.getBooleanPref(GlobalApp.PREF_KEY_SWITCH)){
			// stop recording
			app.writeLogTextLine(logTextStream, getString(R.string.userClickOff),false);
			app.setBooleanPref(GlobalApp.PREF_KEY_SWITCH, false);
    		recordButton.setText(R.string.record_start);
    		promptText.setText(R.string.prompt_ready);
    		timeText.setText("Session last stopped\n" + prettyDate);
    		app.stopMainService();
    		app.setBooleanPref(GlobalApp.PREF_KEY_SWITCH, false);
    		int wakeLockOn = Integer.parseInt(app.getStringPref(app.getResources().getString(R.string.wakeLock)));
	    	if (wakeLockOn == PowerManager.ACQUIRE_CAUSES_WAKEUP
	    			|| (wakeLockOn == GlobalApp.WAKE_LOCK_AUTO && android.os.Build.VERSION.SDK_INT < 11))
			{
//	    		Timer setScreenTimer = new Timer();
//	    		setScreenTimer.schedule(new SetScreenTask(app.loadScreenBirghtness()), 1000);
	    		recoverScreen();
			}
    	}else
    	{
    		if(!app.isUserInfoAvailable()){
            	enterUserInfo();
    			Toast.makeText(app, R.string.msgSetupPrefs, Toast.LENGTH_LONG).show();
    			return;
    		}
    		app.writeLogTextLine(logTextStream, getString(R.string.userClickOn),false);
    		boolean isBatteryOK = app.isBatteryOK(logTextStream);
    		boolean isSDCardOK = app.isSDCardOK(logTextStream, promptText);
    		if(isBatteryOK && isSDCardOK)
    		{	
    			// start recording
    			app.setBooleanPref(GlobalApp.PREF_KEY_SWITCH, true);
    			recordButton.setText(R.string.record_stop);
    			app.startMainService(false);
    			promptText.setText(R.string.prompt_on);
    			timeText.setText("Session last started\n" + prettyDate);
    			
				int wakeLockOn = Integer.parseInt(app.getStringPref(app.getResources().getString(R.string.wakeLock)));
		    	if (wakeLockOn == PowerManager.ACQUIRE_CAUSES_WAKEUP
		    			|| (wakeLockOn == GlobalApp.WAKE_LOCK_AUTO && android.os.Build.VERSION.SDK_INT < 11))
				{
//		    		Timer setScreenTimer = new Timer();
//		    		setScreenTimer.schedule(new SetScreenTask(20f), 1000);
		    		dimScreen();
				}
    		}else{
    			// cannot start to record
    			if(!isBatteryOK)
    			{
    				promptText.setText("Fail to start Monitor.\nBattery is too low.");
    				app.writeLogTextLine(logTextStream, getString(R.string.userClickFailBattery),true);
    			}
    			else
    			{
    				promptText.setText("Fail to start Monitor.\nSD card is not ready.");
    				app.writeLogTextLine(logTextStream, getString(R.string.userClickFailSDCard),true);
    			}
    		}
    		
    	}
	}
 	
 	private void dimScreen(){
 		int prevScreenBrightness, prevScreenTimeout;
 		try {
 			prevScreenTimeout = Settings.System.getInt(app.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
			prevScreenBrightness = Settings.System.getInt(app.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
//			prevScreenBrightnessMode = Settings.System.getInt(app.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
			app.saveScreenBirghtness(prevScreenBrightness, prevScreenTimeout);
			Settings.System.putInt(app.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, -1);
			Settings.System.putInt(app.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0);
//			Settings.System.putInt(app.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
 		float brightness = 20f;
 		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = brightness/255.0f;
	    Log.i(TAG, "current brightness " + lp.screenBrightness);
	    getWindow().setAttributes(lp);
 	}
 	
    private void recoverScreen(){
		int prevScreenBrightness = app.getIntPref(GlobalApp.SCREEN_KEY_BRIGHTNESS);
		Settings.System.putInt(app.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, prevScreenBrightness );
		int prevScreenTimeout = app.getIntPref(GlobalApp.SCREEN_KEY_TIMEOUT);
		Settings.System.putInt(app.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, prevScreenTimeout );
//		int prevScreenBrightnessMode = app.getIntPref(GlobalApp.SCREEN_KEY_BRIGHTNESS_MODE);
//		Settings.System.putInt(app.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, prevScreenBrightnessMode);
		float brightness = prevScreenBrightness>20?prevScreenBrightness:20;
 		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = brightness/255.0f;
	    Log.i(TAG, "current brightness " + lp.screenBrightness);
	    getWindow().setAttributes(lp);
    }
 	
// 	private class SetScreenTask extends TimerTask{
// 		private float brightness;
// 		
// 		public SetScreenTask(float brightness){
// 			this.brightness = brightness;
// 		}
//		@Override
//		public void run() {
//			runOnUiThread(new Runnable(){
//
//				@Override
//				public void run() {
//					WindowManager.LayoutParams lp = getWindow().getAttributes();
//					lp.screenBrightness = brightness/255.0f;
//				    Log.i(TAG, "current brightness " + lp.screenBrightness);
//				    getWindow().setAttributes(lp);
//				}
//			});
//		}
// 	}
	
	


	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
	    if (app.getBooleanPref(GlobalApp.PREF_KEY_SWITCH)){
    		if (event.values[0] > 0)
    		{
    			recordButton.setEnabled(true);
    			testButton.setEnabled(true);
    		}
    		else
    		{
    			recordButton.setEnabled(false);
    			testButton.setEnabled(false);
    		}
	    }
	}
	

}
