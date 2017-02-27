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
