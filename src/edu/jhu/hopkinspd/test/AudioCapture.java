package edu.jhu.hopkinspd.test;

import java.io.*;
import java.util.Date;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.test.conf.TestConfig;
import android.media.*;

public class AudioCapture
{
	private static final String CAPTURE_FILETYPE = "audio";
	private static final int CAPTURE_SAMPLERATE = 44100;
	private static final int CAPTURE_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static final int CAPTURE_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

	private AudioRecord recorder = null;
	private int bufferSize = 0;
	private Thread recordingThread = null;
	private boolean isRecording = false;
//	private int testNumber = 0;
	private TestConfig testConf;
	private GlobalApp app;

	public AudioCapture(GlobalApp app, TestConfig testConf)
	{
		this.app = app;
		bufferSize = AudioRecord.getMinBufferSize(CAPTURE_SAMPLERATE, 
				CAPTURE_CHANNELS, CAPTURE_AUDIO_ENCODING);
		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				CAPTURE_SAMPLERATE,
				CAPTURE_CHANNELS,
				CAPTURE_AUDIO_ENCODING, bufferSize);
//		this.testNumber = testNumber;
		this.testConf = testConf;
	}
	
	public boolean startRecording()
	{
		try{
			if(recorder.getState() == AudioRecord.STATE_INITIALIZED){
				recorder.startRecording();
				isRecording = true;
				recordingThread = new Thread(new Runnable()
				{
					public void run()
					{
						writeAudioDataToFile();
					}
				}, "AudioRecorder Thread");
				recordingThread.start();
				return true;
			}
			else
				return false;
		}catch(IllegalStateException e){
			return false;
		}
	}

	private void writeAudioDataToFile()
	{
		byte data[] = new byte[bufferSize];
		Date time = new Date();
		String filename = app.getTestDataFilename(time, testConf.test_name, 
				CAPTURE_FILETYPE, GlobalApp.RAW_DATA_EXTENSION);
		FileOutputStream os = app.openRawDataFile(filename);

		int read = 0;
		if (null != os)
		{
			while (isRecording)
			{
				read = recorder.read(data, 0, bufferSize);
				if (read != AudioRecord.ERROR_INVALID_OPERATION)
				{
					try 
					{
						os.write(data);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}

			try
			{
				os.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void stopRecording()
	{
		if (recorder != null)
		{
			isRecording = false;
			if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
				recorder.stop();
		}
	}
	
	public void destroy()
	{
		if (recorder != null)
		{
			recorder.release();
			recorder = null;
			recordingThread = null;
		}
	}
}
