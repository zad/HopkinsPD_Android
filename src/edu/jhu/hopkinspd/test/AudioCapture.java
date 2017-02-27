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
