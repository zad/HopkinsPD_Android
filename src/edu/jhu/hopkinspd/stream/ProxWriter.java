package edu.jhu.hopkinspd.stream;

import java.io.*;
import java.util.Date;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;

import android.content.*;
import android.hardware.*;
import android.util.Log;

public class ProxWriter extends StreamWriter
{
	private static final String STREAM_NAME = "prox";

	private static final int SENSOR_TYPE = Sensor.TYPE_PROXIMITY;

	private static final String TAG = GlobalApp.TAG + "|" + STREAM_NAME;
	private int SENSOR_RATE;
	
	private SensorManager sensorManager = null;
	private Sensor sensor = null;
	private DataOutputStream sensorStream = null;

	public ProxWriter(GlobalApp app)
    {
		super(app);
		
		SENSOR_RATE = Integer.parseInt(getStringPref(app.getResources().getString(R.string.sensorRate)));
    	sensorManager = (SensorManager)app.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(SENSOR_TYPE);
        Log.i(TAG, "max proximity value " + sensor.getMaximumRange());
        logTextStream = app.openLogTextFile(STREAM_NAME);
	    writeLogTextLine("Created " + this.getClass().getName() + " instance");
	}
	
    public String toString(){
    	return STREAM_NAME;
    }

    public void init()
    {
        sensorManager.registerListener(this, sensor, SENSOR_RATE);
    }

    public void destroy()
    {
    	sensorManager.unregisterListener(this);
    	sensorManager = null;
    	sensor = null;
    }
    
    public void start(Date startTime)
    {
//    	prevSecs = (double)System.currentTimeMillis()/1000.0d;
	    prevSecs = ((double)startTime.getTime())/1000.0d;
	    String timeStamp = timeString(startTime);
	    sensorStream = openStreamFile(STREAM_NAME, timeStamp, feature_stream_extension);
    	
    	isRecording = true;
	    writeLogTextLine("Proximity recording started");
    }

    public void stop(Date stopTime)
    {
    	isRecording = false;
    	if (closeStreamFile(sensorStream))
    	{
		    writeLogTextLine("Proximity recording successfully stopped");
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
		    writeLogTextLine("Proximity recording successfully restarted");
    	}
    }

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		if ((sensorStream != null) && isRecording)
		{
//	    	double currentSecs = ((double)event.timestamp)/1000000000.0d;
	    	double currentSecs = (double)System.currentTimeMillis()/1000.0d;
        	double diffSecs = currentSecs - prevSecs;
        	prevSecs = currentSecs;

        	double[] proxData = new double[2];
        	proxData[0] = diffSecs;
        	proxData[1] = event.values[0];
        	Log.v(TAG, "receive proximity event " + event.values[0]);
        	writeFeatureFrame(proxData, sensorStream, dataOutputFormat);
		}
	}
    
}
