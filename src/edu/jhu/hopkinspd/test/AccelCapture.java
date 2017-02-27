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
package edu.jhu.hopkinspd.test;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.test.conf.TestConfig;
import android.content.Context;
import android.hardware.*;
import android.util.Log;

public class AccelCapture implements SensorEventListener
{
	public static final int OUTPUT_FORMAT = GlobalApp.OUTPUT_FORMAT_TXT;
	public static final String OUTPUT_EXT = GlobalApp.TXT_DATA_EXTENSION;
	
	private static final String CAPTURE_FILETYPE = "accel";

	private static final int SENSOR_TYPE = Sensor.TYPE_ACCELEROMETER;
	private static final int SENSOR_RATE = SensorManager.SENSOR_DELAY_FASTEST;

	public static final int CAPTURE_BUFFER_LENGTH = 100;
	public static final int CAPTURE_BUFFER_ENTRIES = 4;

	private SensorManager sensorManager = null;
	private Sensor sensor = null;
	public boolean isRecording = false;
	private int bufferItems = 0;
//	private int testNumber = 0;
	private TestConfig testConf;
	private GlobalApp app;
	private DataOutputStream testStreamFile = null;
	public AccelCapture(GlobalApp app, TestConfig testConf)
    {
		this.app = app;
    	sensorManager = (SensorManager)app.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(SENSOR_TYPE);
        sensorManager.registerListener(this, sensor, SENSOR_RATE);
        
        app.allocateStreamBuffer(CAPTURE_BUFFER_LENGTH, CAPTURE_BUFFER_ENTRIES);
		bufferItems = 0;
//		this.testNumber = testNumber;
		this.testConf = testConf;
    }
    
    public void destroy()
    {
        if(sensorManager != null)
            sensorManager.unregisterListener(this);
    	sensorManager = null;
    	sensor = null;
    }
	    
    public void startRecording(String phone_position)
    {
		Date time = new Date();
		String filename = app.getTestDataFilename(time, testConf.test_name, 
				CAPTURE_FILETYPE, OUTPUT_EXT);
		testStreamFile = app.openTestStreamFile(filename);
    	bufferItems = 0;
    	isRecording = true;
    	if(phone_position != null){
    	    String phonePosFileName = app.getTestDataFilename(time, 
    	            testConf.test_name, "phone", "txt");
    	    DataOutputStream phonePosFile = 
    	            app.openTestStreamFile(phonePosFileName);
    	    try {
                phonePosFile.writeChars(phone_position);
            } catch (IOException e) {
                Log.e(AccelCapture.class.getName(), 
                        "phonePosFile WriteException");
            }
    	    app.closeTestStreamFile(phonePosFile);
    	}
    }
    
    public void stopRecording()
    {
    	isRecording = false;

    	// Write out remainder of buffer if anything left
    	if (bufferItems > 0)
    	{
        	app.writeTestStreamFrames(testStreamFile, bufferItems, OUTPUT_FORMAT);
    	}
    	app.closeTestStreamFile(testStreamFile);
    }

	public void onSensorChanged(SensorEvent event)
	{
		if (isRecording)
		{
//        	GlobalApp.frameBuffer[bufferItems][0] = (double)(System.currentTimeMillis())/1000.0d;
			// the time unit is second
        	GlobalApp.streamBuffer[bufferItems][0] = ((double)event.timestamp)/1000000000.0d;
        	GlobalApp.streamBuffer[bufferItems][1] = event.values[0];
        	GlobalApp.streamBuffer[bufferItems][2] = event.values[1];
        	GlobalApp.streamBuffer[bufferItems][3] = event.values[2];
        	
        	bufferItems ++;
        	if (bufferItems == CAPTURE_BUFFER_LENGTH)
        	{
            	app.writeTestStreamFrames(testStreamFile, bufferItems, OUTPUT_FORMAT);
            	bufferItems = 0;
        	}
		}
	}

	public void onAccuracyChanged(Sensor arg0, int arg1)
	{
	}
		
}
