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

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.util.Date;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.test.conf.TestConfig;
import android.content.Context;
import android.hardware.*;

public class GyroCapture implements SensorEventListener
{
	public static final int OUTPUT_FORMAT = GlobalApp.OUTPUT_FORMAT_TXT;
	public static final String OUTPUT_EXT = GlobalApp.TXT_DATA_EXTENSION;
	
	private static final String CAPTURE_FILETYPE = "gyro";

	private static final int SENSOR_TYPE = Sensor.TYPE_GYROSCOPE;
	private static final int SENSOR_RATE = SensorManager.SENSOR_DELAY_FASTEST;

	private SensorManager sensorManager = null;
	private Sensor sensor = null;
	public boolean isRecording = false;
	private GlobalApp app;
	private boolean sensorAvailable = true;
	private DataOutputStream testStreamFile = null;
	private TestConfig testConf;
	
	public GyroCapture(GlobalApp app, TestConfig testConf)
    {
		this.app = app;
    	sensorManager = (SensorManager)app.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(SENSOR_TYPE);
        sensorAvailable = sensorManager.registerListener(this, sensor, SENSOR_RATE);
        
        this.testConf = testConf;
    }
    
    public void destroy()
    {
    	if(sensorAvailable)sensorManager.unregisterListener(this);
    	sensorManager = null;
    	sensor = null;
    }
	    
    public void startRecording()
    {
    	if(sensorAvailable)
		{
    		Date time = new Date();
			String filename = app.getTestDataFilename(time, testConf.test_name, 
					CAPTURE_FILETYPE, OUTPUT_EXT);
			testStreamFile = app.openTestStreamFile(filename);
	    	isRecording = true;
		}
    }
    
    public void stopRecording()
    {
    	if(sensorAvailable){
    		isRecording = false;

        	// Write out remainder of buffer if anything left
        	app.closeTestStreamFile(testStreamFile);    		
    	}
    }

	public void onSensorChanged(SensorEvent event)
	{
		if (isRecording)
		{
			// the time unit is second

		    app.writeTestStreamFrames(testStreamFile, event.timestamp, event.values, OUTPUT_FORMAT);
		}
	}

	public void onAccuracyChanged(Sensor arg0, int arg1)
	{
	}
		
}
