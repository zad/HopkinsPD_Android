package edu.jhu.hopkinspd.test;

import java.io.DataOutputStream;
import java.util.Date;

import edu.jhu.hopkinspd.GlobalApp;
import android.content.Context;
import android.hardware.*;

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
	private int testNumber = 0;
	private GlobalApp app;
	private DataOutputStream testStreamFile = null;
	public AccelCapture(GlobalApp app, int testNumber)
    {
		this.app = app;
    	sensorManager = (SensorManager)app.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(SENSOR_TYPE);
        sensorManager.registerListener(this, sensor, SENSOR_RATE);
        
        app.allocateStreamBuffer(CAPTURE_BUFFER_LENGTH, CAPTURE_BUFFER_ENTRIES);
		bufferItems = 0;
		this.testNumber = testNumber;
    }
    
    public void destroy()
    {
    	sensorManager.unregisterListener(this);
    	sensorManager = null;
    	sensor = null;
    }
	    
    public void startRecording()
    {
		Date time = new Date();
		String filename = app.getTestDataFilename(time, testNumber, CAPTURE_FILETYPE, OUTPUT_EXT);
		testStreamFile = app.openTestStreamFile(filename);
    	bufferItems = 0;
    	isRecording = true;
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
