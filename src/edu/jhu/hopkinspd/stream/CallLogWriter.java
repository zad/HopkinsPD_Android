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
