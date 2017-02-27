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

import java.io.*;
import java.util.Date;

import edu.jhu.hopkinspd.GlobalApp;
import android.content.*;
import android.util.Log;

public class BatteryWriter extends StreamWriter
{
	private static final String STREAM_NAME = "batt";
	private static final int STREAM_FEATURES = 2;
	private static final String TAG = GlobalApp.TAG + "|BatteryWriter";

	private DataOutputStream sensorStream = null;

    public String toString(){
    	return STREAM_NAME;
    }
	
	private BroadcastReceiver batteryReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if ((sensorStream != null) && isRecording)
			{
		    	double currentSecs = (double)System.currentTimeMillis()/1000.0d;
	        	double diffSecs = currentSecs - prevSecs;
	        	prevSecs = currentSecs;

	        	double[] battData = new double[2];
	        	battData[0] = diffSecs;
	        	battData[1] = intent.getIntExtra("level", 0);
	        	
//	        	int v = Float.floatToIntBits((float)battData[0]);
//	        	Log.i(TAG, "batt " + diffSecs + " " + intent.getIntExtra("level", 0));
//	        	Log.i(TAG, "batt " + battData[0] + " " + battData[1]);
//	        	Log.i(TAG, "batt " + v + " " + Float.intBitsToFloat(v));
				
	        	writeFeatureFrame(battData, sensorStream, dataOutputFormat);
			}
		}
	};
	
	
    public BatteryWriter(GlobalApp app)
    {
    	super(app);
    	// Save format is SHORT!!!
    	if(format == DATA_FORMAT_BINARY)
    		dataOutputFormat = OUTPUT_FORMAT_SHORT;
		// When the battery level changes
		app.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    	allocateFrameFeatureBuffer(STREAM_FEATURES);
        
        logTextStream = app.openLogTextFile(STREAM_NAME);
	    writeLogTextLine("Created " + this.getClass().getName() + " instance");
    }

    public void init()
    {
    	Log.v(TAG,"BatteryWriter initialized");
    }

    public void destroy()
    {
    	
    }
    
    public void start(Date startTime)
    {
//	    prevSecs = (double)System.currentTimeMillis()/1000.0d;
	    prevSecs = ((double)startTime.getTime())/1000.0d;
	    String timeStamp = timeString(startTime);
	    sensorStream = openStreamFile(STREAM_NAME, timeStamp, feature_stream_extension);
    	
    	isRecording = true;
	    writeLogTextLine("Battery recording started");
    }

    public void stop(Date stopTime)
    {
    	isRecording = false;
    	if (closeStreamFile(sensorStream))
    	{
		    writeLogTextLine("Battery recording successfully stopped");
		}
    	Log.v(TAG, "unregisterReceiver(batteryReceiver)");
    	try{
    		app.unregisterReceiver(batteryReceiver);
    	}catch(IllegalArgumentException e){
    		Log.v(TAG, e.getMessage());
    	}
    }
    
    public void restart(Date time)
    {
    	DataOutputStream oldStream = sensorStream;
    	String timeStamp = timeString(time);
    	sensorStream = openStreamFile(STREAM_NAME, timeStamp, feature_stream_extension);
	    prevSecs = ((double)time.getTime())/1000.0d;
    	if (closeStreamFile(oldStream))
    	{
		    writeLogTextLine("Battery recording successfully restarted");
    	}
    }
    
}
