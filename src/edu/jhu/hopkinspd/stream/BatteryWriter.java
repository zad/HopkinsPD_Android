package edu.jhu.hopkinspd.stream;

import java.io.*;
import java.util.Date;

import edu.jhu.hopkinspd.GlobalApp;
import android.content.*;
import android.util.Log;

public class BatteryWriter extends StreamWriter
{
	private static final String STREAM_NAME = "batt";
	private static final int STREAM_FEATURES = 2;
	private static final String TAG = GlobalApp.TAG + "|BatteryWriter";

	private DataOutputStream sensorStream = null;

    public String toString(){
    	return STREAM_NAME;
    }
	
	private BroadcastReceiver batteryReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if ((sensorStream != null) && isRecording)
			{
		    	double currentSecs = (double)System.currentTimeMillis()/1000.0d;
	        	double diffSecs = currentSecs - prevSecs;
	        	prevSecs = currentSecs;

	        	double[] battData = new double[2];
	        	battData[0] = diffSecs;
	        	battData[1] = intent.getIntExtra("level", 0);
	        	
//	        	int v = Float.floatToIntBits((float)battData[0]);
//	        	Log.i(TAG, "batt " + diffSecs + " " + intent.getIntExtra("level", 0));
//	        	Log.i(TAG, "batt " + battData[0] + " " + battData[1]);
//	        	Log.i(TAG, "batt " + v + " " + Float.intBitsToFloat(v));
				
	        	writeFeatureFrame(battData, sensorStream, dataOutputFormat);
			}
		}
	};
	
	
    public BatteryWriter(GlobalApp app)
    {
    	super(app);
    	// Save format is SHORT!!!
    	if(format == DATA_FORMAT_BINARY)
    		dataOutputFormat = OUTPUT_FORMAT_SHORT;
		// When the battery level changes
		app.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    	allocateFrameFeatureBuffer(STREAM_FEATURES);
        
        logTextStream = app.openLogTextFile(STREAM_NAME);
	    writeLogTextLine("Created " + this.getClass().getName() + " instance");
    }

    public void init()
    {
    	Log.v(TAG,"BatteryWriter initialized");
    }

    public void destroy()
    {
    	
    }
    
    public void start(Date startTime)
    {
//	    prevSecs = (double)System.currentTimeMillis()/1000.0d;
	    prevSecs = ((double)startTime.getTime())/1000.0d;
	    String timeStamp = timeString(startTime);
	    sensorStream = openStreamFile(STREAM_NAME, timeStamp, feature_stream_extension);
    	
    	isRecording = true;
	    writeLogTextLine("Battery recording started");
    }

    public void stop(Date stopTime)
    {
    	isRecording = false;
    	if (closeStreamFile(sensorStream))
    	{
		    writeLogTextLine("Battery recording successfully stopped");
		}
    	Log.v(TAG, "unregisterReceiver(batteryReceiver)");
    	try{
    		app.unregisterReceiver(batteryReceiver);
    	}catch(IllegalArgumentException e){
    		Log.v(TAG, e.getMessage());
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
		    writeLogTextLine("Battery recording successfully restarted");
    	}
    }
    
}
