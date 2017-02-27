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
import java.util.*;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;


import android.content.SharedPreferences;
import android.hardware.*;
import android.location.*;
import android.os.*;
import android.preference.PreferenceManager;
import android.text.format.*;

public abstract class StreamWriter 
implements LocationListener, SensorEventListener
{
	public static final String STREAM_PREFIX = "stream";
	public static final int OUTPUT_FORMAT_TXT = 0;
	public static final int OUTPUT_FORMAT_SHORT = 1;
	public static final int OUTPUT_FORMAT_FLOAT = 2;
	public static final int OUTPUT_FORMAT_DOUBLE = 3;
	
	protected GlobalApp app;
	protected BufferedWriter debugTextStream = null;
	protected BufferedWriter logTextStream = null;
	public String logTextFileName = null;
    public double prevSecs = 0;
	public boolean isRecording = false;
	public double[] featureBuffer = null;
	public double[] featureMult = null;
	public int featureCount = 0;
	public int featureSize = 0;
	protected int format;
	protected String raw_stream_extension, feature_stream_extension;
	protected int dataOutputFormat;
	public static final int DATA_FORMAT_TEXT = 0;
	public static final int DATA_FORMAT_BINARY = 1;
	
	public StreamWriter(GlobalApp application){
		app = application;
		format = Integer.parseInt(app.getStringPref(app.getString(R.string.sensorDataFormat)));
	
	    	
		switch (format){
		case DATA_FORMAT_TEXT:
			raw_stream_extension = GlobalApp.STREAM_EXTENSION_CSV;
			feature_stream_extension = GlobalApp.STREAM_EXTENSION_CSV;
			dataOutputFormat = GlobalApp.OUTPUT_FORMAT_TXT;
			break;
		case DATA_FORMAT_BINARY:
			raw_stream_extension = GlobalApp.STREAM_EXTENSION_RAW;
			feature_stream_extension = GlobalApp.STREAM_EXTENSION_BIN;
			dataOutputFormat = GlobalApp.OUTPUT_FORMAT_FLOAT;
			break;
		}
	}
	
	public abstract String toString();
	
	public String timeString(Date time)
	{
    	return DateFormat.format("yyyyMMdd_kkmmss", time).toString();
	}
	
	public DataOutputStream openStreamFile(String streamName, String timeStamp, String streamExt)
	{
		String userID = getStringPref(GlobalApp.PREF_KEY_USERID);
		String rootPath = getStringPref(GlobalApp.PREF_KEY_ROOT_PATH);
		String phoneID = app.getStringPhoneID();
		String fileName = rootPath + "/" + GlobalApp.STREAMS_SUBDIR + "/" + GlobalApp.PREFIX + "_" + STREAM_PREFIX + "_" 
				+ streamName + "_" + userID + "_"  + phoneID + "_" + timeStamp + "." + streamExt;
		DataOutputStream dos = null;
	    try
	    {
	    	dos = new DataOutputStream(new FileOutputStream(fileName));
	    }
	    catch (FileNotFoundException e)
	    {
	        e.printStackTrace();
	    }
	    return dos;
	}
	
	public DataOutputStream openStatsFile(String streamName, String timeStamp, String streamExt)
	{
		String userID = getStringPref(GlobalApp.PREF_KEY_USERID);
		String rootPath = getStringPref(GlobalApp.PREF_KEY_ROOT_PATH);
//		String phoneID = app.getStringPhoneID();
		String fileName = rootPath + "/" 
				+ streamName + "_" + userID + "_" + timeStamp + "." + streamExt;
		DataOutputStream dos = null;
	    try
	    {
	    	dos = new DataOutputStream(new FileOutputStream(fileName));
	    }
	    catch (FileNotFoundException e)
	    {
	        e.printStackTrace();
	    }
	    return dos;
	}
	
	public boolean closeStreamFile(DataOutputStream stream)
	{
		boolean closed = false;
		if (stream != null)
		{
	        try
	        {
	        	stream.flush();
	        	stream.close();
	        	closed = true;
	        }
	        catch (IOException e)
	        {
	            e.printStackTrace();
	        }
		}
		return closed;
	}
	
//	public boolean deleteStreamFile(String streamName, String timeStamp, String streamExt)
//	{
//		String userID = getStringPref(GlobalApp.PREF_KEY_USERID);
//		String rootPath = getStringPref(GlobalApp.PREF_KEY_ROOT_PATH);
//		String phoneID = app.getStringPhoneID();
//		String fileName = rootPath + "/" + GlobalApp.PREFIX + "_" + STREAM_PREFIX + "_" 
//				+ streamName + "_" + userID + "_"  + phoneID + "_" + timeStamp + "." + streamExt;
//		File streamFile = new File(fileName);
//	    return streamFile.delete();
//	}

	public void allocateFrameFeatureBuffer(int features)
	{
		featureBuffer = new double[features];
		featureMult = new double[features];
		Arrays.fill(featureBuffer, 0);
		Arrays.fill(featureMult, 1.0);
		featureCount = 0;
		featureSize = features;
	}
	
	public boolean pushFrameFeature(double value)
	{
		if (featureCount < featureSize)
		{
			featureBuffer[featureCount] = value;
			featureCount ++;
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void clearFeatureFrame()
	{
		Arrays.fill(featureBuffer, 0);
		featureCount = 0;
	}

	public void writeTextLine(String[] items, DataOutputStream stream)
	{
		if (stream != null)
		{
			try
			{
				for (int i = 0; i < items.length; i ++)
				{
					// Text strings in CSV format
					if (i < (items.length - 1))
						stream.writeBytes(items[i] + ",");
					else
						stream.writeBytes(items[i]);
				}
				
				// New line for CSV files
				stream.writeByte(10);
				stream.flush();
			}
	        catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
	}
	
	public void writeTextLine(String csv_string, DataOutputStream stream){
		if(stream != null){
			try{
				stream.writeBytes(csv_string);
				stream.writeByte(10);
				stream.flush();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void writeFeatureFrame(double[] features, DataOutputStream stream, int outputFormat)
	{
		if (stream != null)
		{
			try
			{
				for (int i = 0; i < features.length; i ++)
				{
					switch (outputFormat)
					{
					// Text strings in CSV format
					case OUTPUT_FORMAT_TXT:
						if (i < (features.length - 1))
							stream.writeBytes(Double.toString(features[i]) + ",");
						else
							stream.writeBytes(Double.toString(features[i]));
						break;
						
					// Raw 64-bit, double big-endian format
					case OUTPUT_FORMAT_DOUBLE:
						stream.writeDouble(features[i]);
						break;
						
				    // Raw 32-bit, float big-endian format
					case OUTPUT_FORMAT_FLOAT:
						stream.writeFloat((float)features[i]);
						break;
					
						// Compact 16-bit, big-endian with variable precision multiplier
					case OUTPUT_FORMAT_SHORT:
						stream.writeShort((short)Math.round(features[i]*featureMult[i]));
						break;
					}
				}
				
				// New line for CSV files
				if (outputFormat == OUTPUT_FORMAT_TXT)
				{
					stream.writeByte(10);
				}
				
				stream.flush();
			}
	        catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
//	public void openLogTextFile(String streamName, String streamPath)
//    {
//		String userID = getStringPref(GlobalApp.PREF_KEY_USERID);
//		String rootPath = getStringPref(GlobalApp.PREF_KEY_ROOT_PATH);
//		logTextFileName = rootPath + "/" + streamName + "_" + userID + ".log";
//	    try
//	    {
//			logTextStream = new BufferedWriter(new FileWriter(logTextFileName, true));
//		}
//	    catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//    }
    
    public void writeLogTextLine(String message)
    {
        try
        {
        	Date now = new Date();
        	String prettyDate = prettyDateString(now);
			logTextStream.write(prettyDate + ": " + message);
	        logTextStream.newLine();
	        logTextStream.flush();
		}
        catch (IOException e)
		{
			e.printStackTrace();
		}
    }

    public String getStringPref(String key)
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.app);
    	return prefs.getString(key, "");
    }
    
    public int getIntPref(String key)
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.app);
    	return prefs.getInt(key, -1);
    }

    public boolean getBooleanPref(String key)
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.app);
    	return prefs.getBoolean(key, false);
    }
    
    
	public String prettyDateString(Date time)
	{
		return DateFormat.format("yyyy/MM/dd kk:mm:ss", time).toString();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
	}

	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	public abstract void init();

	public abstract void start(Date now);

	public abstract void restart(Date now);

	public abstract void stop(Date now) ;

	public abstract void destroy();
}
