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
