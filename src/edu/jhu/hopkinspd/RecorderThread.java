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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import edu.jhu.hopkinspd.stream.*;
import android.content.*;
import android.os.*;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.WindowManager;

public class RecorderThread extends Thread
{
//	private static RecorderThread instance;
	public  Handler handler;
	public static final String TAG = GlobalApp.TAG + "|" + "RecorderThread";

	
	private static final int WAKE_LOCK_ACQUIRE_PAUSE = 1000;

	private ArrayList<StreamWriter> writers = null;
	
	private PowerManager powerManager = null;
	private PowerManager.WakeLock wakeLock = null;
	private int wakeLockOn;

	public GlobalApp app = null;
	
	private int state;
	private int prevScreenTimeout;
//	private int prevScreenBrightness;
	private Timer keepAliveTimer;
	private int accelStopCnt;
	private int wakelockAutoState;
	protected int accelRunCnt;
	
	public static final int INIT = 1;
	public static final int STARTED = 2;
	public static final int RESTARTED = 3;
	public static final int STOPPED = 4;
	
	
	private BufferedWriter logTextStream = null;
	
	private static final int[] 	WRITER_IDS = {
		R.string.sensorAccelOn,
		R.string.sensorAudioOn,
		R.string.sensorBattOn,
		R.string.sensorCompOn,
		R.string.sensorCtxOn,
		R.string.sensorGPSOn,
		R.string.sensorLightOn,
		R.string.sensorMetaOn,
		R.string.sensorProxOn,
		R.string.sensorTelephonyOn,
		R.string.sensorWifiOn,
		R.string.sensorCallLogOn,
		R.string.sensorSmsLogOn
	};
	

	
	private static final String[] WRITER_CLASSES = {
		"AccelWriter",
		"AudioWriter",
		"BatteryWriter",
		"CompassWriter",
		"ContextWriter",
		"GPSWriter",
		"LightWriter",
		"MetaDataWriter",
		"ProxWriter",
		"TelephonyWriter",
		"WifiWriter",
		"CallLogWriter",
		"SMSLogWriter",
	};
	protected static final int AUTO_STATE_KEEP_SCREEN_ON = 1;
	protected static final int AUTO_STATE_UNKNOWN = 0;
	protected static final int AUTO_STATE_PARTIAL = 2;
	private static final String logFileType = "recorderthread";

	
	public int getRecorderState(){
		return state;
	}
	
	public void run()
    {
        Looper.prepare();
        handler = new Handler()
        {
            public void handleMessage(Message msg)
            {
            	switch (msg.what)
            	{
            	case GlobalApp.STREAM_INIT:
            		app = (GlobalApp)msg.obj;
            		logTextStream = app.openLogTextFile(logFileType);
            		initStreams();
            		break;
            		
            	case GlobalApp.STREAM_START:
            		startStreams((Date)msg.obj);
            		break;
            		
            	case GlobalApp.STREAM_RESTART:
            		restartStreams((Date)msg.obj);
            		break;
            		
            	case GlobalApp.STREAM_STOP:
            		stopStreams((Date)msg.obj);
            		break;
            		
            	case GlobalApp.STREAM_DESTROY:
            		destroyStreams();
            		break;
            	}
            }
        };
        Looper.loop();
    }
	
	private void initStreams()
	{
		state = INIT;
		writers = new ArrayList<StreamWriter>();
		registerWriters();
		for(StreamWriter writer : writers){
			writer.init();
		}
		String message = "inited streams"; 
		app.writeLogTextLine(logTextStream, message, false);
		Log.v(TAG, message);
	}
	
	private void registerWriters() {
		for(int i = 0; i < WRITER_IDS.length; i++){
			int id = WRITER_IDS[i];
			String key = app.getResources().getString(id);
			if(getBooleanPref(key)){
				try {
					String className = "edu.jhu.hopkinspd.stream."+WRITER_CLASSES[i];
					Log.v(TAG, "register " + className);
					@SuppressWarnings("unchecked")
					Class<StreamWriter> wClass = 
							(Class<StreamWriter>) Class.forName(className);
					Constructor<StreamWriter> constructor = wClass.getConstructor(GlobalApp.class);
					writers.add(constructor.newInstance(app));
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}else
				Log.v(TAG, "unchecked:"+key);
		}
	}

	private void startStreams(Date now)
	{
    	// Start recording
		String msg = "started streams";
		app.writeLogTextLine(logTextStream, msg, false);
		Log.v(TAG, msg);
		powerHold();
		for(StreamWriter writer : writers){
			writer.start(now);
		}
    	state = STARTED;
		
	}
	
	private void restartStreams(Date start)
	{
		Date curr = new Date();
		// Restart recording
		for(StreamWriter writer : writers){
			writer.restart(curr);
		}
		String msg = "restarted streams";
		app.writeLogTextLine(logTextStream, msg, false);
		Log.v(TAG, "restarted streams " + app.prettyDateString(start));
		app.startZip(start, curr);
		app.setLongPref(GlobalApp.PREF_KEY_RECORD_LAST_RESTART, curr.getTime());
		state = RESTARTED;
	}
	
	
	
	private void stopStreams(Date now)
	{
        // Stop recording
		powerRelease();

		for(StreamWriter writer : writers){
			writer.stop(now);
		}
    	state = STOPPED;
    	String msg = "stopped streams";
		Log.v(TAG, msg);
		app.writeLogTextLine(logTextStream, msg, false);
	}
	
	private void destroyStreams()
	{
		for(StreamWriter writer : writers){
			writer.destroy();
		}
		String msg = "destroied streams";
		app.writeLogTextLine(logTextStream, msg, false);
		Log.v(TAG, msg);
	}

    private void powerHold()
    {
    	wakeLockOn = Integer.parseInt(app.getStringPref(app.getResources().getString(R.string.wakeLock)));
    	if(wakeLockOn != 0)
    	{
    		// Attempt to keep screen on (v3)
        	if (wakeLockOn == PowerManager.ACQUIRE_CAUSES_WAKEUP
        			|| (wakeLockOn == GlobalApp.WAKE_LOCK_AUTO && android.os.Build.VERSION.SDK_INT < 11)
        			)
        	{
        		app.writeLogTextLine(logTextStream, "enter acquire causes wakeup mode", false);
//    			try
//    			{
////    				prevScreenTimeout = Settings.System.getInt(app.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
////    				prevScreenBrightness = Settings.System.getInt(app.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
////    				app.saveScreenBirghtness(prevScreenBrightness);
////    				Log.i(TAG, "prev brightness " + prevScreenBrightness);
//    			}
//    			catch (SettingNotFoundException e)
//    			{
//    			}
    			
    	    	powerManager = (PowerManager)app.getSystemService(Context.POWER_SERVICE);
        		
    	    	keepAliveTimer = new Timer();
    			keepAliveTimer.schedule(new TimerTask()
    			{
    				@Override
    				public void run()
    				{
    					if(powerManager.isScreenOn())
    					{
    						Log.i(TAG, "screen is on");
    						return;
    					}
    					if(wakeLock != null && wakeLock.isHeld())
    					{
    						wakeLock.release();
    						app.writeLogTextLine(logTextStream, "keepAliveTimer release olde wakeup", false);
    					}
    					wakeLock = powerManager.newWakeLock(
    							PowerManager.SCREEN_DIM_WAKE_LOCK |
    							PowerManager.ACQUIRE_CAUSES_WAKEUP, "RecorderOn");
    					wakeLock.acquire();
    					app.writeLogTextLine(logTextStream, "keepAliveTimer request acquire new causes wakeup", false);
    				}
    				
    			}, 0, WAKE_LOCK_ACQUIRE_PAUSE);
        	}else if(wakeLockOn == PowerManager.PARTIAL_WAKE_LOCK
        			|| (wakeLockOn == GlobalApp.WAKE_LOCK_AUTO && android.os.Build.VERSION.SDK_INT >= 11)
        			)
    		{
        		app.writeLogTextLine(logTextStream, "enter partial wake lock mode", false);
        		powerManager = (PowerManager)app.getSystemService(Context.POWER_SERVICE);
	    		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RecorderOn");
	    		wakeLock.acquire();
	    		Log.v(TAG, "wakelock acquire");
    		}
        	/*
        	else if(wakeLockOn == WAKE_LOCK_AUTO){
    			app.writeLogTextLine(logTextStream, "enter auto wake lock mode", false);
    			powerManager = (PowerManager)app.getSystemService(Context.POWER_SERVICE);
	    		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RecorderOn");
	    		wakeLock.acquire();
	    		Log.v(TAG, "wakelock acquire");
	    		app.writeLogTextLine(logTextStream, "partial wakelock acquire", false);
	    		keepAliveTimer = new Timer();
    			keepAliveTimer.schedule(new TimerTask()
    			{
					@Override
    				public void run()
    				{
						if(wakelockAutoState == AUTO_STATE_UNKNOWN){
							if(!powerManager.isScreenOn()){
	    						long currentMillis = System.currentTimeMillis();
	        					long accelLastestMillis = ((AccelWriter) writers.get(0)).getLatestMillis();
	        					if(Math.abs(currentMillis - accelLastestMillis) < 5000){
	        						accelStopCnt ++;
	        						app.writeLogTextLine(logTextStream, "accelStopCnt " + accelStopCnt, false);
	        					}else{
	        						accelRunCnt ++;
	        						accelStopCnt = 0;
	        						app.writeLogTextLine(logTextStream, "accelRunCnt " + accelRunCnt, false);
	        					}
	        					if(accelStopCnt > 3){
	        						wakelockAutoState = AUTO_STATE_KEEP_SCREEN_ON;
	        						try
	        		    			{
	        		    				prevScreenTimeout = Settings.System.getInt(app.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
//	        		    				prevScreenBrightness = Settings.System.getInt(app.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
	        		    			}
	        		    			catch (SettingNotFoundException e)
	        		    			{
	        		    			}
	        		    			Settings.System.putInt(app.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, -1);
	        		    			Settings.System.putInt(app.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0);
	        					}
	        					if(accelRunCnt > 300) // 5 minutes
	        						wakelockAutoState = AUTO_STATE_PARTIAL;
	    					}
						}else if (wakelockAutoState == AUTO_STATE_KEEP_SCREEN_ON){
							Log.i(TAG, "AUTO_STATE_KEEP_SCREEN_ON");
							app.writeLogTextLine(logTextStream, "AUTO_STATE_KEEP_SCREEN_ON", false);
							wakeLock = powerManager.newWakeLock(
	    							PowerManager.SCREEN_DIM_WAKE_LOCK |
	    							PowerManager.ACQUIRE_CAUSES_WAKEUP, "RecorderOn");
	    					wakeLock.acquire();
						}else{
							app.writeLogTextLine(logTextStream, "AUTO_STATE_PARTIAL", false);
							Log.i(TAG, "AUTO_STATE_PARTIAL");
							keepAliveTimer.cancel();
						}
    				}
    				
    			}, 0, WAKE_LOCK_ACQUIRE_PAUSE);
    		}*/
    	}
		
    }
    
    private void powerRelease()
    {
    	if (wakeLockOn != 0)
		{
			if (wakeLock != null && wakeLock.isHeld())
	    	{
				wakeLock.release();
				app.writeLogTextLine(logTextStream, "release wakelock", false);
	    		Log.v(TAG, "wakelock release");
	    	}
			if(keepAliveTimer != null)
    			keepAliveTimer.cancel();
		}
    	
    }
	
	private boolean getBooleanPref(String key)
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);
    	return prefs.getBoolean(key, false);
    }

}
