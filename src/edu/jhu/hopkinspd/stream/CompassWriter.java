package edu.jhu.hopkinspd.stream;

import android.content.*;
import android.hardware.*;
import android.util.Log;

import java.io.*;
import java.util.Arrays;
import java.util.Date;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;

public class CompassWriter extends StreamWriter
{
	private static String STREAM_NAME = "cmpss";
	
	private static final int SENSOR_TYPE = Sensor.TYPE_MAGNETIC_FIELD;
	private int SENSOR_RATE = SensorManager.SENSOR_DELAY_FASTEST;
	
	private static final int STREAM_FEATURES = 14;
	private static final double SENSOR_FRAME_DURATION = 1.0;			// Frame length in seconds
	private static final double SENSOR_MAX_RATE = 100.0;				// Assumed maximum compass sampling rate

	private static final String TAG = GlobalApp.TAG + "|" + STREAM_NAME;;

	private SensorManager sensorManager = null;
	private Sensor sensor = null;
	private DataOutputStream sensorStreamRaw = null;
    private DataOutputStream sensorStreamFeatures = null;
    
	private double prevSecs;
	private double prevFrameSecs;
	private double frameTimer = 0;
	private double[][] frameBuffer = null;
	private int frameSamples = 0;
	private int frameBufferSize = 0;
	private boolean rawOn, featureOn;
    
    public CompassWriter(GlobalApp app)
    {
    	super(app);
    	SENSOR_RATE = Integer.parseInt(getStringPref(app.getResources().getString(R.string.sensorRate)));
    	rawOn = getBooleanPref(app.getResources().getString(R.string.sensorCompRawOn));
    	featureOn = getBooleanPref(app.getResources().getString(R.string.sensorCompFeatureOn));
    	
    	sensorManager = (SensorManager)app.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(SENSOR_TYPE);

        logTextStream = app.openLogTextFile(STREAM_NAME);
	    writeLogTextLine("Created " + this.getClass().getName() + " instance");
	    writeLogTextLine("Raw streaming: " + rawOn);
	    writeLogTextLine("Feature streaming: " + featureOn);
	    
        // Allocate frame buffer, assuming a maximum sampling rate
        frameBufferSize = (int)Math.ceil(SENSOR_MAX_RATE/SENSOR_FRAME_DURATION);
        frameBuffer = new double[frameBufferSize][3];
        writeLogTextLine("Compass maximum frame size (samples): " + frameBufferSize);
        writeLogTextLine("Compass maximum frame duation (secs): " + SENSOR_FRAME_DURATION);
        
        allocateFrameFeatureBuffer(STREAM_FEATURES);
    }
    
    public void init()
    {
        sensorManager.registerListener(this, sensor, SENSOR_RATE);
        Log.v(TAG,"compassWriter initialized");
    }

    public void destroy()
    {
    	sensorManager.unregisterListener(this);
    	sensorManager = null;
    	sensor = null;
    }
    
    public void start(Date startTime)
    {
	    prevSecs = ((double)startTime.getTime())/1000.0d;
//	    prevSecs = ((double)System.currentTimeMillis())/1000.0d;
	    writeLogTextLine("prevSecs: " + prevSecs);
	    
	    prevFrameSecs = prevSecs;
	    frameTimer = 0;
	    frameSamples = 0;

	    // Ensure frame buffer is cleared
		for (double[] row: frameBuffer)
			Arrays.fill(row, 0);
	    
	    // Create new stream file(s)
	    String timeStamp = timeString(startTime);
	    
	    if (rawOn)
	       	sensorStreamRaw = openStreamFile(STREAM_NAME, timeStamp, raw_stream_extension);
	    if (featureOn)
	    	sensorStreamFeatures = openStreamFile(STREAM_NAME, timeStamp, feature_stream_extension);

    	isRecording = true;
	    writeLogTextLine("Compass recording started");
    }
    
    public void stop(Date stopTime)
    {
    	isRecording = false;
    	
    	if (rawOn && closeStreamFile(sensorStreamRaw))
    	{
		    writeLogTextLine("Raw compass recording successfully stopped");
		}
    	
    	if (featureOn && closeStreamFile(sensorStreamFeatures))
    	{
		    writeLogTextLine("Compass feature recording successfully stopped");
		}
    }
    
    public void restart(Date time)
    {
    	DataOutputStream oldRaw = sensorStreamRaw;
        DataOutputStream oldFeatures = sensorStreamFeatures;
    	String timeStamp = timeString(time);
	    if (rawOn)
	      	sensorStreamRaw = openStreamFile(STREAM_NAME, timeStamp, raw_stream_extension);
	    if (featureOn)
	    	sensorStreamFeatures = openStreamFile(STREAM_NAME, timeStamp, feature_stream_extension);
	    prevSecs = ((double)time.getTime())/1000.0d;
    	if (rawOn && closeStreamFile(oldRaw))
	        writeLogTextLine("Raw compass recording successfully restarted");
		
    	if (featureOn && closeStreamFile(oldFeatures))
    	    writeLogTextLine("Compass feature recording successfully restarted");
    }
    
	@Override
	public void onSensorChanged(SensorEvent event)
	{
		if (isRecording)
		{
	    	double currentSecs = (double)(System.currentTimeMillis())/1000.0d;
        	double diffSecs = currentSecs - prevSecs;
        	prevSecs = currentSecs;

        	double magX = event.values[0];
        	double magY = event.values[1];
        	double magZ = event.values[2];
        	
        	// Write out raw compass data, if enabled
        	if (rawOn)
        	{
	        	double[] orientData = new double[4];
	        	orientData[0] = diffSecs;
	        	orientData[1] = magX;
	        	orientData[2] = magY;
	        	orientData[3] = magZ;
	        	writeFeatureFrame(orientData, sensorStreamRaw, dataOutputFormat);
        	}

        	if(featureOn)
        	{
		        // Store measurement in frame buffer
	        	frameBuffer[frameSamples][0] = magX;
	        	frameBuffer[frameSamples][1] = magY;
	        	frameBuffer[frameSamples][2] = magZ;
	        	frameSamples ++;
	        	frameTimer += diffSecs;
	
	        	// Frame complete?
	        	if ((frameTimer >= SENSOR_FRAME_DURATION) || (frameSamples == (frameBufferSize - 1)))
	        	{
	        		clearFeatureFrame();
	        		
	                double fN = (double)frameSamples;

	                double diffFrameSecs = currentSecs - prevFrameSecs;
	                prevFrameSecs = currentSecs;
	                pushFrameFeature(diffFrameSecs);
	        		pushFrameFeature(fN);
	        		
	        		// Calculate compass features for azimuth,pitch,roll
	        		for (int i = 0; i < 3; i ++)
	        		{
	            		// Mean
	        			double mean = 0;
	        			for (int j = 0; j < frameSamples; j ++)
	        				mean += frameBuffer[j][i];
	        			mean /= fN;
	        			pushFrameFeature(mean);
	            		
	        			double accum;
	
	        			// Absolute central moment
	        			accum = 0;
	        			for (int j = 0; j < frameSamples; j ++)
	        				accum += Math.abs(frameBuffer[j][i] - mean);
	        			pushFrameFeature(accum/fN);
	        			
	        			// Standard deviation
	        			accum = 0;
	        			for (int j = 0; j < frameSamples; j ++)
	        				accum += (frameBuffer[j][i] - mean)*(frameBuffer[j][i] - mean);
	        			pushFrameFeature(Math.sqrt(accum/fN));
	
	        			// Max deviation
	        			accum = 0;
	        			for (int j = 0; j < frameSamples; j ++)
	        				accum = Math.max(Math.abs(frameBuffer[j][i] - mean),accum);
	        			pushFrameFeature(accum);
	        		}
	        		
		        	// Write out features
		        	writeFeatureFrame(featureBuffer, sensorStreamFeatures, dataOutputFormat);
		        	
	        		// Reset frame buffer counters
	        		frameSamples = 0;
	        		frameTimer = 0;
	
	        		// Ensure buffer is zero-padded
	        		for (double[] row: frameBuffer)
	        			Arrays.fill(row, 0);
	        	}
        	}
		}
	}

    public String toString(){
    	return STREAM_NAME;
    }
}
