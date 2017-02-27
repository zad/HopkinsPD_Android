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
package edu.jhu.hopkinspd.receiver;

import java.io.BufferedWriter;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.internal.am;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.MainService;
import edu.jhu.hopkinspd.R;
import edu.jhu.hopkinspd.SyncService;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver{
	public static final String TAG = GlobalApp.TAG + "|" + "BootReceiver";
	BufferedWriter logTextStream = null;
	private GlobalApp app;
	public static final String REBOOT = "reboot";
	
	
	@Override
	public void onReceive(final Context context, Intent intent) {
		// reboot the main service if the app is started
		Log.i(TAG, "Boot intent received.");
//		final Intent service = new Intent(context, MainService.class);
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		app = GlobalApp.getApp();
		Log.i(TAG, "lastModifiedDate:"+app.prettyDateString(app.getLastModifiedDate()));
		logTextStream = app.openLogTextFile(GlobalApp.LOG_FILE_NAME_BOOT);
		Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
//            	service.putExtra(REBOOT, true);
            	
                // run main service
            	if(prefs.getBoolean(GlobalApp.PREF_KEY_SWITCH, false)){
            		Log.i(TAG, "starting main service from bootReceiver.");
        			app.writeLogTextLine(logTextStream, "start main service", false);
//        			context.startService(service);
        			app.startMainService(false);
        		}else{
        			Log.i(TAG, "did not start main service from bootReceiver.");
        			Log.i(TAG, "lastModifiedDate:"+app.prettyDateString(app.getLastModifiedDate()));
        			app.writeLogTextLine(logTextStream, "did not start main service", false);
        		}
            }
        }, 5000);
		
		
	}

}
