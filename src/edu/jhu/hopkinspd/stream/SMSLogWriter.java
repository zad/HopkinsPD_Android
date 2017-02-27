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
		Uri uri = Uri.parse("content://sms");
		Cursor cursor = this.app.getContentResolver().query(uri, null, null, null, null);
		long last_tsp = 0;
		if(cursor != null)
		{
			String header = "SMS Date, SMS Type, SMS Length";
			writeTextLine(header, streamSms);
			while(cursor.moveToNext()){
				String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
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

				if(app.getLongPref(LAST_TSP) == 0 || smsDayTime.getTime() > app.getLongPref(LAST_TSP)){
					String time = app.prettyDateString(smsDayTime);
					String line = String.format("%s,%s,%s", time, typeOfSMS, body.length());
					writeTextLine(line, streamSms);
					if(smsDayTime.getTime() > last_tsp)
						last_tsp = smsDayTime.getTime();
				}
			}
			app.setLongPref(LAST_TSP, last_tsp);						
			cursor.close();	
		}
	}
}
