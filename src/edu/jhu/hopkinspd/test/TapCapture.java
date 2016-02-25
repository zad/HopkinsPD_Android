package edu.jhu.hopkinspd.test;

import java.io.DataOutputStream;
import java.util.Date;

import edu.jhu.hopkinspd.GlobalApp;

import android.view.MotionEvent;

public class TapCapture
{
	public static final int OUTPUT_FORMAT = GlobalApp.OUTPUT_FORMAT_TXT;
	public static final String OUTPUT_EXT = GlobalApp.TXT_DATA_EXTENSION;

	private static final String CAPTURE_FILETYPE = "tap";

	public static final int CAPTURE_BUFFER_LENGTH = 100;
	public static final int CAPTURE_BUFFER_ENTRIES = 3;

	public boolean isRecording = false;
	private int bufferItems = 0;
	private int testNumber = 0;
	private GlobalApp app;
	private DataOutputStream testStreamFile = null;
	public TapCapture(GlobalApp app, int testNumber)
    {
		this.app = app;
		bufferItems = 0;
		this.testNumber = testNumber;
		app.allocateStreamBuffer(CAPTURE_BUFFER_LENGTH, CAPTURE_BUFFER_ENTRIES);
    }
    
    public void destroy()
    {
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

	public boolean handleTouchEvent(MotionEvent me)
	{
		if (isRecording)
		{
			GlobalApp.streamBuffer[bufferItems][0] = (double)(me.getEventTime())/1000.0d;
			GlobalApp.streamBuffer[bufferItems][1] = me.getX();
			GlobalApp.streamBuffer[bufferItems][2] = me.getY();
//			GlobalApp.streamBuffer[bufferItems][3] = me.getSize();
//			GlobalApp.streamBuffer[bufferItems][4] = me.getPressure();
			
			bufferItems ++;
			if (bufferItems == CAPTURE_BUFFER_LENGTH)
			{
				app.writeTestStreamFrames(testStreamFile, bufferItems, OUTPUT_FORMAT);
				bufferItems = 0;
			}
		}
		return true;
    }

}
