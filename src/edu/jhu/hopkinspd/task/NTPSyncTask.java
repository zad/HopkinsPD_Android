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
package edu.jhu.hopkinspd.task;

import java.io.BufferedWriter;
import java.util.Date;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;
import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;
import edu.jhu.hopkinspd.utils.ShellInterface;
import edu.jhu.hopkinspd.utils.SntpClient;

public class NTPSyncTask extends AsyncTask<String,String,Long> {
	private GlobalApp app;
	public static final long NTP_SU_ERROR = 0;
	public static final long NTP_SERVER_ERROR = 1;
	private static final String TAG = "NTP";
	protected BufferedWriter logTextStream = null;
	protected String logFileName = "";
	// Number of seconds between Jan 1, 1970 and Jan 1, 2010
    // 40 years plus 10 leap days
    private static final long OFFSET_1970_TO_2010 = ((365L * 40L) + 10L) * 24L * 60L * 60L * 1000L;

	@Override
	protected Long doInBackground(String... arg0) {
		app = GlobalApp.getApp();
		logFileName = GlobalApp.LOG_FILE_NAME_NTP;
		// Open log file
		logTextStream = app.openLogTextFile(logFileName);
        app.writeLogTextLine(logTextStream, "ntp task started", false);
        
//		Log.i(TAG, "2010:" + new Date(OFFSET_1970_TO_2010));
		long time = ntpSync();
		if(time == NTP_SERVER_ERROR
				|| time < OFFSET_1970_TO_2010)
			return NTP_SERVER_ERROR;
		if(setTime(time))
			return time;
		else
			return NTP_SU_ERROR;
	}
	
	/**
	 * Superuser privilege is required.
	 * @param time
	 */
	public boolean setTime(long time) {
		if (ShellInterface.isSuAvailable()) {
			ShellInterface.runCommand("chmod 666 /dev/alarm");
			SystemClock.setCurrentTimeMillis(time);
			ShellInterface.runCommand("chmod 664 /dev/alarm");
			Log.i(TAG, "setTime succeed.");
			app.writeLogTextLine(logTextStream, "setTime succeed.", false);
			return true;
	    }else
	    {
	    	Log.i(TAG, "su is not available");
	    	app.writeLogTextLine(logTextStream, "su is not available", false);
	    	return false;
	    }
	}
	
	public long ntpSync() {
		SntpClient client = new SntpClient();
		if (client.requestTime("pool.ntp.org", 30000)) {
		    long now = client.getNtpTime() + 
		    		SystemClock.elapsedRealtime()
		    		- client.getNtpTimeReference();
		    Date current = new Date(now);
		    Log.i(TAG, "curr:"+current.toString() + " now:" + now);	    
		    app.writeLogTextLine(logTextStream, "curr:"+current.toString(), false);
		    return now;
		}else
		{
			Log.i(TAG, "Sync failed.");
			app.writeLogTextLine(logTextStream, "Sync failed.", false);
			return NTPSyncTask.NTP_SERVER_ERROR;
		}
	}

	@Override
	protected void onPostExecute(Long now) {
		String msg = null;
		if(now == NTP_SU_ERROR)
		{
			msg = "NTP Sync Failed: su is not available!";
		}else if(now == NTP_SERVER_ERROR)
		{
			msg = "NTP Sync Failed: NTP server has no response!";	
		}else{
			msg = String.format(app.getString(R.string.toast_ntp), app.prettyDateString(new Date(now)));
		}
		Toast.makeText(app, msg, Toast.LENGTH_LONG).show();
		Log.i(TAG, msg);
		app.closeLogTextStream(logTextStream);
		super.onPostExecute(now);
	}
	
}
