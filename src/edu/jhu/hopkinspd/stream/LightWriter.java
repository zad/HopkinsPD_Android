package edu.jhu.hopkinspd.stream;

import java.io.*;
import java.util.Date;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;

import android.content.*;
import android.hardware.*;
import android.util.Log;

public class LightWriter extends StreamWriter
{
	private static final String STREAM_NAME = "light";

	private static final int SENSOR_TYPE = Sensor.TYPE_LIGHT;
	private int SENSOR_RATE;

	private static final String TAG = GlobalApp.TAG + "|" + STREAM_NAME;
	
	private SensorManager sensorManager = null;
	private Sensor sensor = null;
	private DataOutputStream sensorStream = null;
		
    public LightWriter(GlobalApp app)
    {
    	super(app);
		
		SENSOR_RATE = Integer.parseInt(getStringPref(app.getResources().getString(R.string.sensorRate)));
    	sensorManager = (SensorManager)app.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(SENSOR_TYPE);
        Log.i(TAG, "max light value " + sensor.getMaximumRange());
        logTextStream = app.openLogTextFile(STREAM_NAME);
	    writeLogTextLine("Created " + this.getClass().getName() + " instance");
    }

    public void init()
    {
        sensorManager.registerListener(this, sensor, SENSOR_RATE);
        Log.v(TAG,"lightWriter initialized");
    }

    public void destroy()
    {
    	sensorManager.unregisterListener(this);
    	sensorManager = null;
    	sensor = null;
    }
    
    public void start(Date startTime)
    {
//	    prevSecs = (double)System.currentTimeMillis()/1000.0d;
	    prevSecs = ((double)startTime.getTime())/1000.0d;
	    String timeStamp = timeString(startTime);
	    sensorStream = openStreamFile(STREAM_NAME, timeStamp, feature_stream_extension);
    	
    	isRecording = true;
	    writeLogTextLine("Light recording started");
    }

    public void stop(Date stopTime)
    {
    	isRecording = false;
    	if (closeStreamFile(sensorStream))
    	{
		    writeLogTextLine("Light recording successfully stopped");
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
		    writeLogTextLine("Light recording successfully restarted");
    	}
    }
    
	@Override
	public void onSensorChanged(SensorEvent event)
	{
//		Log.v(TAG, "receive light event");
		if ((sensorStream != null) && isRecording)
		{
//	    	double currentSecs = ((double)event.timestamp)/1000000000.0d;
	    	double currentSecs = (double)System.currentTimeMillis()/1000.0d;
        	double diffSecs = currentSecs - prevSecs;
        	prevSecs = currentSecs;

        	double[] proxData = new double[2];
        	proxData[0] = diffSecs;
        	proxData[1] = event.values[0];
//        	Log.v(TAG, "receive light event " + event.values[0]);
        	writeFeatureFrame(proxData, sensorStream, dataOutputFormat);
		}
	}
    
    public String toString(){
    	return STREAM_NAME;
    }
}
