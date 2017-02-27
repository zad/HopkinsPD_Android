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
import java.util.List;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;

import android.content.*;
import android.location.*;
import android.preference.PreferenceManager;
//import android.os.Handler;
import android.util.Log;

public class GPSWriter extends StreamWriter
{
	private static final String STREAM_NAME = "gps";
	private static int GPS_SAMPLERATE = 1;		// GPS update frequency in seconds
	private static boolean GPS_EXTRA_FEATURES = false;
	private static final String TAG = GlobalApp.TAG + "|" + STREAM_NAME;
//	private static String LOC_PROVIDER = LocationManager.PASSIVE_PROVIDER;
//	private static String LOC_PROVIDER = LocationManager.NETWORK_PROVIDER;
	private static String LOC_PROVIDER = LocationManager.GPS_PROVIDER;

	private LocationManager locManager = null;
	private DataOutputStream locStream = null;
	
    public GPSWriter(GlobalApp app)
    {
    	super(app);
    	if(format == DATA_FORMAT_BINARY)
    		dataOutputFormat = OUTPUT_FORMAT_DOUBLE;
		
    	logTextStream = app.openLogTextFile(STREAM_NAME);
	    writeLogTextLine("Created " + this.getClass().getName() + " instance");

    	locManager = (LocationManager)app.getSystemService(Context.LOCATION_SERVICE);
    	
    	List<String> providers = locManager.getProviders(true);
        Location loc = null;
        for (int i = providers.size()-1; i >= 0; i--)
        {
        	String prov = providers.get(i);
            loc = locManager.getLastKnownLocation(prov);
            if (loc != null)
            {
            	Log.v(TAG, "locManager:"+prov);
            	break;
            }
        }
        if (loc != null)
        {
    	    writeLogTextLine("Initial position lat: " + loc.getLatitude() +
    	    		                         " lon: " + loc.getLongitude() + " alt: " + loc.getAltitude());
        }    	
    	
    }

    public void destroy()
    {
    	locManager.removeUpdates(this);
    	locManager = null;
    }
    
    public void start(Date startTime)
    {
    	GPS_SAMPLERATE = Integer.parseInt(app.getStringPref(app.getString(R.string.sensorGARInt)));
    	GPS_EXTRA_FEATURES = app.getBooleanPref(app.getString(R.string.sensorGPSExtraFeatureOn));
    	locManager.requestLocationUpdates(LOC_PROVIDER, GPS_SAMPLERATE*1000, 0, this);
        
	    prevSecs = ((double)startTime.getTime())/1000.0d;
//	    prevSecs = (double)System.currentTimeMillis()/1000.0d;
	    String timeStamp = timeString(startTime);
	    locStream = openStreamFile(STREAM_NAME, timeStamp, feature_stream_extension);
	    
    	isRecording = true;
	    writeLogTextLine("GPS recording started");
	    Log.v(TAG, "start GPS " + GPS_SAMPLERATE);
    }

    public void stop(Date stopTime)
    {
    	isRecording = false;
    	if (closeStreamFile(locStream))
    	{
		    writeLogTextLine("GPS recording successfully stopped");
		}
    	locManager.removeUpdates(this);
    }
    
    public void restart(Date time)
    {
    	DataOutputStream oldStream = locStream;
    	String timeStamp = timeString(time);
    	locStream = openStreamFile(STREAM_NAME, timeStamp, feature_stream_extension);
	    prevSecs = ((double)time.getTime())/1000.0d;
    	if (closeStreamFile(oldStream))
    	{
		    writeLogTextLine("GPS recording successfully restarted");
    	}
    }
    
	@Override
	public void onLocationChanged(Location location)
	{
		Log.v(TAG, "onLocationChanged");
		if ((locStream != null) && isRecording)
		{
//        	double fixSecs = (double)locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getTime()/1000.0d;
	    	double fixSecs = (double)System.currentTimeMillis()/1000.0d;
        	double diffSecs = fixSecs - prevSecs;
        	prevSecs = fixSecs;
        	double[] gpsData;
        	if(GPS_EXTRA_FEATURES){
        		gpsData = new double[7]; 
        		gpsData[0] = diffSecs;
            	gpsData[1] = locManager.getLastKnownLocation(LOC_PROVIDER).getLatitude();
            	gpsData[2] = locManager.getLastKnownLocation(LOC_PROVIDER).getLongitude();
            	gpsData[3] = locManager.getLastKnownLocation(LOC_PROVIDER).getAltitude();
            	gpsData[4] = locManager.getLastKnownLocation(LOC_PROVIDER).getAccuracy();
            	gpsData[5] = locManager.getLastKnownLocation(LOC_PROVIDER).getBearing();
            	gpsData[6] = locManager.getLastKnownLocation(LOC_PROVIDER).getSpeed();
        	}else{
        		gpsData = new double[4];
	        	gpsData[0] = diffSecs;
	        	gpsData[1] = locManager.getLastKnownLocation(LOC_PROVIDER).getLatitude();
	        	gpsData[2] = locManager.getLastKnownLocation(LOC_PROVIDER).getLongitude();
	        	gpsData[3] = locManager.getLastKnownLocation(LOC_PROVIDER).getAltitude();
        	}
        	writeFeatureFrame(gpsData, locStream, dataOutputFormat);
		}
	}
	
    public String toString(){
    	return STREAM_NAME;
    }

	@Override
	public void init() {
		Log.v(TAG,"gpsWriter initialized");
	}


}
