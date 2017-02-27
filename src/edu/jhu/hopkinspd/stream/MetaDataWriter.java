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
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MetaDataWriter extends StreamWriter
{
	private static final String STREAM_NAME = "meta";
	private static final String[] metaHeader = {"Android OS", "Manufacturer", "Brand", "Device", "Model", "Product", "Unique ID (IMEI/MEID/ESN)", "Session start", "Session end", "APK Version"};
	private static final String TAG = GlobalApp.TAG + "|" + STREAM_NAME;
	
	private DataOutputStream metaStream = null;
	private Date lastStartTime = null;
		
    public MetaDataWriter(GlobalApp app)
    {
    	super(app);
		
        
        logTextStream = app.openLogTextFile(STREAM_NAME);
	    writeLogTextLine("Created " + this.getClass().getName() + " instance");
    }
    
    public String toString(){
    	return STREAM_NAME;
    }

    public void init()
    {
    	Log.v(TAG,"metaDataWriter initialized");
    }

    public void destroy()
    {
    }
    
    public void start(Date startTime)
    {
    	lastStartTime = startTime;
	    String timeStamp = timeString(startTime);
	    metaStream = openStreamFile(STREAM_NAME, timeStamp, GlobalApp.STREAM_EXTENSION_CSV);
    	
    	isRecording = true;
	    writeLogTextLine("Metadata recording started");
    }

    public void stop(Date stopTime)
    {
    	isRecording = false;
    	
    	writeMetadata(lastStartTime, stopTime);
    	if (closeStreamFile(metaStream))
    	{
		    writeLogTextLine("Metadata recording successfully stopped");
		}
    }
    
    public void restart(Date time)
    {
    	writeMetadata(lastStartTime, time);
    	closeStreamFile(metaStream);
	    metaStream = openStreamFile(STREAM_NAME, timeString(time), GlobalApp.STREAM_EXTENSION_CSV);
    	lastStartTime = time;
	    writeLogTextLine("Metadata recording successfully restarted");
    }
    
    public void writeMetadata(Date sessionStart, Date sessionEnd)
    {
	    // Pull together phone details and other metadata
        TelephonyManager tManager = (TelephonyManager)app.getSystemService(Context.TELEPHONY_SERVICE);
	    String[] metaData = new String[10];
	    metaData[0] = Build.VERSION.RELEASE;
	    metaData[1] = Build.MANUFACTURER;
	    metaData[2] = Build.BRAND;
	    metaData[3] = Build.DEVICE;
	    metaData[4] = Build.MODEL;
	    metaData[5] = Build.PRODUCT;
	    metaData[6] = tManager.getDeviceId();
	    metaData[7] = timeString(sessionStart);
	    metaData[8] = timeString(sessionEnd);
	    metaData[9] = app.getVersion();
	    // Write to file
	    writeTextLine(metaHeader, metaStream);
	    writeTextLine(metaData, metaStream);
    }
}
