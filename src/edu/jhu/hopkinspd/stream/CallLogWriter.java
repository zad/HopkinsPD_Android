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
import java.util.Date;

import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;

import edu.jhu.hopkinspd.GlobalApp;

public class CallLogWriter extends StreamWriter{
	public static final String STREAM_NAME = "calllog";
	private static final String TAG = GlobalApp.TAG +  "|CallLogWriter";
	private DataOutputStream streamCall = null;
	private static final String LAST_TSP = "last_call_tsp";

	public CallLogWriter(GlobalApp application) {
		super(application);
		logTextStream = app.openLogTextFile(STREAM_NAME);
	    writeLogTextLine("Created " + this.getClass().getName());
	}



	@Override
	public void init() {
		Log.v(TAG,"CallLogWriter initialized");
		writeLogTextLine("CallLogWriter initialized");
	}

	@Override
	public void start(Date startTime) {
		String timeStamp = timeString(startTime);
		streamCall = openStreamFile(STREAM_NAME, timeStamp, GlobalApp.STREAM_EXTENSION_CSV);
		try{
			getCallDetails();
		}catch(Exception e){
			writeLogTextLine(e.getLocalizedMessage());
		}
		writeLogTextLine("call log stream started");
	}

	@Override
	public void restart(Date time) {
    	DataOutputStream old = streamCall;
    	String timeStamp = timeString(time);
    	streamCall = openStreamFile(STREAM_NAME, timeStamp, GlobalApp.STREAM_EXTENSION_CSV);
    	if(closeStreamFile(old))
    		writeLogTextLine("call log recording successfully restarted");
	}

	@Override
	public void stop(Date now) {
		closeStreamFile(streamCall);
		writeLogTextLine("call log stopped");    
	}

	@Override
	public void destroy() {
		
	}
	
	private void getCallDetails() 
	{ 
		Cursor managedCursor = this.app.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null); 
		int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE); 
		int date = managedCursor.getColumnIndex(CallLog.Calls.DATE); 
		int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION); 
		long last_tsp = 0;
		
		String header = "Call Date, Call Type, Call Duration in Secs";
		writeTextLine(header, streamCall);
		while (managedCursor.moveToNext()) {  
			String callType = managedCursor.getString(type); 
			String callDate = managedCursor.getString(date); 
			Date callDayTime = new Date(Long.valueOf(callDate)); 
			String callDuration = managedCursor.getString(duration); 
			String dir = null; int dircode = Integer.parseInt(callType); 
			switch (dircode) {
			case CallLog.Calls.OUTGOING_TYPE: 
				dir = "OUTGOING"; 
				break; 
			case CallLog.Calls.INCOMING_TYPE: 
				dir = "INCOMING"; 
				break; 
			case CallLog.Calls.MISSED_TYPE: 
				dir = "MISSED"; 
				break; 
			} 
			if(app.getLongPref(LAST_TSP) == 0 || callDayTime.getTime() > app.getLongPref(LAST_TSP)){
				String time = app.prettyDateString(callDayTime);
				String line = String.format("%s,%s,%s", time, dir, callDuration);
				writeTextLine(line, streamCall);
				if(callDayTime.getTime() > last_tsp)
					last_tsp = callDayTime.getTime();
				
			}
		} 
		app.setLongPref(LAST_TSP, last_tsp);
	}



	@Override
	public String toString() {
		return STREAM_NAME;
	}
	
}
