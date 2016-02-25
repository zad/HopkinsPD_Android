package edu.jhu.hopkinspd.stream;

import java.io.DataOutputStream;
import java.util.Date;

import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.util.Log;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.utils.Csv;

public class SMSLogWriter extends StreamWriter{
	public static final String STREAM_NAME = "smslog";
	private static final String TAG = GlobalApp.TAG +  "|SMSLogWriter";
	private DataOutputStream streamSms = null;
	private static final String LAST_TSP = "last_sms_tsp";
	
	public SMSLogWriter(GlobalApp application) {
		super(application);
		logTextStream = app.openLogTextFile(STREAM_NAME);
	    writeLogTextLine("Created " + this.getClass().getName());
	}

	@Override
	public String toString() {
		return STREAM_NAME;
	}

	@Override
	public void init() {
		Log.v(TAG,"CallLogWriter initialized");
		writeLogTextLine("CallLogWriter initialized");
	}

	@Override
	public void start(Date startTime) {
		String timeStamp = timeString(startTime);
		streamSms = openStreamFile(STREAM_NAME, timeStamp, GlobalApp.STREAM_EXTENSION_CSV);
		try{
			getSMSDetails();
		}catch(Exception e){
			writeLogTextLine(e.getLocalizedMessage());
		}
		writeLogTextLine("sms log stream started");
	}

	@Override
	public void restart(Date time) {
		DataOutputStream old = streamSms;
    	String timeStamp = timeString(time);
    	streamSms = openStreamFile(STREAM_NAME, timeStamp, GlobalApp.STREAM_EXTENSION_CSV);
    	if(closeStreamFile(old))
    		writeLogTextLine("call log recording successfully restarted");
	}

	@Override
	public void stop(Date now) {
		closeStreamFile(streamSms);
		writeLogTextLine("sms log stopped");    
	}

	@Override
	public void destroy() {
		
	}
	
	private void getSMSDetails() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("*********SMS History*************** :");
		Uri uri = Uri.parse("content://sms");
		Cursor cursor = this.app.getContentResolver().query(uri, null, null, null, null);
		long last_tsp = 0;
		if(cursor != null)
		{
			String header = "SMS Date, Phone Number, SMS Type, SMS Body";
			writeTextLine(header, streamSms);
			while(cursor.moveToNext()){
				String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
				String number = cursor.getString(cursor.getColumnIndexOrThrow("address"));
				String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
				Date smsDayTime = new Date(Long.valueOf(date));
				String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
				String typeOfSMS = null;
				switch (Integer.parseInt(type)) {
				case 1:
					typeOfSMS = "INBOX";
					break;

				case 2:
					typeOfSMS = "SENT";
					break;

				case 3:
					typeOfSMS = "DRAFT";
					break;
				}

				stringBuffer.append("\nPhone Number:--- " + number + " \nMessage Type:--- "
						+ typeOfSMS + " \nMessage Date:--- " + smsDayTime
						+ " \nMessage Body:--- " + body);
				stringBuffer.append("\n----------------------------------");
				
				if(app.getLongPref(LAST_TSP) == 0 || smsDayTime.getTime() > app.getLongPref(LAST_TSP)){
					String time = app.prettyDateString(smsDayTime);
					String line = String.format("%s,%s,%s,%s", time, number, typeOfSMS, Csv.escape(body));
					writeTextLine(line, streamSms);
					if(smsDayTime.getTime() > last_tsp)
						last_tsp = smsDayTime.getTime();
				}
			}
//			Log.i(TAG, stringBuffer.toString());
			app.setLongPref(LAST_TSP, last_tsp);						
			cursor.close();	
		}
	}
}
