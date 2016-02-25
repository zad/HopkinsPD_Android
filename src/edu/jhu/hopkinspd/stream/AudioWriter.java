package edu.jhu.hopkinspd.stream;

import java.io.*;
import java.util.*;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;
import edu.jhu.hopkinspd.math.FFT;
import edu.jhu.hopkinspd.math.MFCC;
import edu.jhu.hopkinspd.math.Window;

import android.content.Context;
import android.media.*;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AudioWriter extends StreamWriter
{
	private static final String TAG = GlobalApp.TAG + "|AudioWriter";

	private static String STREAM_NAME = "audio";
	
	private static int RECORDER_SOURCE = 
			MediaRecorder.AudioSource.VOICE_RECOGNITION;
	
	private static final boolean continuous = true;
			
	private static int RECORDER_SAMPLERATE = 8000;
	private static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	
	private static int FFT_SIZE = 8192;
	private static int MFCCS = 12;
	private static int MEL_BANDS = 20;
	private static int STREAM_FEATURES = 20;
	private static double[] FREQ_BANDEDGES = {50,250,500,1000,2000};

	private AudioRecord audioRecorder = null;
	private Thread recordingThread = null;
	private int bufferSize = 0;
	private int bufferSamples = 0;
    DataOutputStream audioStreamRaw = null;
    DataOutputStream audioStreamFeatures = null;
    private static int[] freqBandIdx = null;
	
    private FFT featureFFT = null;
    private MFCC featureMFCC = null;
    private Window featureWin = null;
    
    private TelephonyManager tm;
    
    private boolean rawOn, featureOn;
    
    public String toString(){
    	return STREAM_NAME;
    }
    
    public AudioWriter(GlobalApp application)
    {
    	super(application);
    	rawOn = getBooleanPref(app.getResources().getString(R.string.sensorAudioRawOn));
    	featureOn = getBooleanPref(app.getResources().getString(R.string.sensorAudioFeatureOn));
    	
		String rootPath = getStringPref(GlobalApp.PREF_KEY_ROOT_PATH);

    	bufferSize = AudioRecord.getMinBufferSize(
        		RECORDER_SAMPLERATE,
        		RECORDER_CHANNELS,
        		RECORDER_AUDIO_ENCODING);

	    bufferSize = Math.max(bufferSize, RECORDER_SAMPLERATE*2);
	    bufferSamples = bufferSize/2;
	    
	    logTextStream = app.openLogTextFile(STREAM_NAME);
	    writeLogTextLine("Created " + this.getClass().getName() + " instance");
	    writeLogTextLine("Audio bufferSize (bytes): " + bufferSize);
	    writeLogTextLine("Raw streaming: " + rawOn);
	    writeLogTextLine("Feature streaming: " + featureOn);

    	allocateFrameFeatureBuffer(STREAM_FEATURES);
    
	    featureFFT = new FFT(FFT_SIZE);
	    featureWin = new Window(bufferSamples);
	    featureMFCC = new MFCC(FFT_SIZE, MFCCS, MEL_BANDS, RECORDER_SAMPLERATE);
	    
	    freqBandIdx = new int[FREQ_BANDEDGES.length];
	    for (int i = 0; i < FREQ_BANDEDGES.length; i ++)
	    {
	    	freqBandIdx[i] = Math.round((float)FREQ_BANDEDGES[i]*((float)FFT_SIZE/(float)RECORDER_SAMPLERATE));
	    	writeLogTextLine("Frequency band edge " + i + ": " + Integer.toString(freqBandIdx[i]));
	    }
	    
	    audioRecorder = new AudioRecord(
	    		RECORDER_SOURCE,
				RECORDER_SAMPLERATE,
				RECORDER_CHANNELS,
				RECORDER_AUDIO_ENCODING,
				bufferSize);
	    
	    tm = (TelephonyManager) app.getSystemService(Context.TELEPHONY_SERVICE);
	    tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public void destroy()
    {
        isRecording = false;
	    if (null != audioRecorder)
	    {
	        audioRecorder.stop();
	        audioRecorder.release();
	        audioRecorder = null;
	        recordingThread = null;
	        tm = null;
	        mPhoneListener = null;
	    }
    }
    
	public void start(Date startTime)
	{
		if(continuous)
			startRecording();
	}
	
	private void startRecording(){
		if(isRecording)
		{
			Log.v(TAG, "already started. fail to start again.");
			return;
		}
		Date startTime = new Date();
	    prevSecs = ((double)startTime.getTime())/1000.0d;
	    String timeStamp = timeString(startTime);

	    // Create new stream file(s)
	    if (rawOn)
	       	audioStreamRaw = new DataOutputStream(openStreamFile(STREAM_NAME, timeStamp, GlobalApp.STREAM_EXTENSION_RAW));
	    if (featureOn)
	    	audioStreamFeatures = new DataOutputStream(openStreamFile(STREAM_NAME, timeStamp, GlobalApp.STREAM_EXTENSION_BIN));
	    
	    audioRecorder.startRecording();
	    isRecording = true;
	    recordingThread = new Thread(new Runnable()
	    {
	        @Override
	        public void run()
	        {
	            handleAudioStream();
	        }
	    }, "AudioRecorder Thread");
	    recordingThread.start();
	    
	    writeLogTextLine("Audio recording started");
	}
	
	private PhoneStateListener mPhoneListener = new PhoneStateListener() {
		public void onCallStateChanged(int state, String incomingNumber) {
			try {
				switch (state) {
				case TelephonyManager.CALL_STATE_RINGING:
					Log.v(TAG, "CALL_STATE_RINGING");
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					Log.v(TAG, "CALL_STATE_OFFHOOK");
					if(!continuous)
						startRecording();
					break;
				case TelephonyManager.CALL_STATE_IDLE:
					Log.v(TAG, "CALL_STATE_IDLE");
					if(!continuous)
						stop(new Date());
					break;
				default:
					Log.i("Default", "Unknown phone state=" + state);
				}
			} catch (Exception e) {
				Log.i("Exception", "PhoneStateListener() e = " + e);
			}
	  	}
	};

	public void stop(Date stopTime)
	{
        isRecording = false;
	    if (audioRecorder != null && audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
	    {
	        audioRecorder.stop();
	    }
	    writeLogTextLine("Audio recording try to stop");
	}
	
	
	
    public void restart(Date time)
    {
    	DataOutputStream oldRaw = audioStreamRaw;
        DataOutputStream oldFeatures = audioStreamFeatures;
    	String timeStamp = timeString(time);
	    if (rawOn)
	       	audioStreamRaw = new DataOutputStream(openStreamFile(STREAM_NAME, timeStamp, GlobalApp.STREAM_EXTENSION_RAW));
	    if (featureOn)
	    	audioStreamFeatures = new DataOutputStream(openStreamFile(STREAM_NAME, timeStamp, GlobalApp.STREAM_EXTENSION_BIN));
	    prevSecs = ((double)time.getTime())/1000.0d;
	    if (rawOn && closeStreamFile(oldRaw))
	    	writeLogTextLine("Raw audio stream successfully restarted");
	    if (featureOn && closeStreamFile(oldFeatures))
	    {
	    	writeLogTextLine("Audio feature stream successfully restarted");
	    }
    }
	
	
    private void handleAudioStream()
	{
        short data16bit[] = new short[bufferSamples];
    	byte data8bit[] = new byte[bufferSize];
    	
    	double fftBufferR[] = new double[FFT_SIZE];
    	double fftBufferI[] = new double[FFT_SIZE];
    	double featureCepstrum[] = new double[MFCCS];
    	
	    int readAudioSamples = 0;
	    while (isRecording)
	    {
	    	readAudioSamples = audioRecorder.read(data16bit, 0, bufferSamples);
	    	
	    	double currentSecs = (double)(System.currentTimeMillis())/1000.0d;
	    	double diffSecs = currentSecs - prevSecs;
	    	prevSecs = currentSecs;

	    	if (readAudioSamples > 0)
	    	{
	    		Log.v(TAG,"readingAudioSamples");
	    		clearFeatureFrame();
	    		double fN = (double)readAudioSamples;

    			pushFrameFeature(diffSecs);
    			
	    		// Write out raw audio, if enabled
	    		if (rawOn)
	    		{
		    		// Convert shorts to 8-bit bytes for raw audio output
		    		for (int i = 0; i < bufferSamples; i ++)
		    		{
		    			data8bit[i*2] = (byte)data16bit[i];
		    			data8bit[i*2+1] = (byte)(data16bit[i] >> 8);
		    		}
	    			try
	    			{
	    				audioStreamRaw.write(data8bit, 0, readAudioSamples*2);
	    				audioStreamRaw.flush();
	    			}
	    			catch (IOException e)
	    			{
	    				e.printStackTrace();
	    			}
	    		}
	    		
	    		if(featureOn)
	    		{
		    		// L1-norm
		    		double accum = 0;
		    		for (int i = 0; i < readAudioSamples; i ++)
		    		{
		    			accum += Math.abs((double)data16bit[i]);
		    		}
		    		pushFrameFeature(accum/fN);
	
		    		// L2-norm
		    		accum = 0;
		    		for (int i = 0; i < readAudioSamples; i ++)
		    		{
		    			accum += (double)data16bit[i]*(double)data16bit[i];
		    		}
		    		pushFrameFeature(Math.sqrt(accum/fN));
	
		    		// Linf-norm
		    		accum = 0;
		    		for (int i = 0; i < readAudioSamples; i ++)
		    		{
		    			accum = Math.max(Math.abs((double)data16bit[i]),accum);
		    		}
		    		pushFrameFeature(accum);
	
		    		// Frequency analysis
		    		Arrays.fill(fftBufferR, 0);
		    		Arrays.fill(fftBufferI, 0);
	
		    		// Convert audio buffer to doubles
		    		for (int i = 0; i < readAudioSamples; i++)
		    		{
		    			fftBufferR[i] = data16bit[i];
		    		}
	
		    		// In-place windowing
		    		featureWin.applyWindow(fftBufferR);
	
		    		// In-place FFT
		    		featureFFT.fft(fftBufferR, fftBufferI);
	
		    		// Get PSD across frequency band ranges
		    		for (int b = 0; b < (FREQ_BANDEDGES.length - 1); b ++)
		    		{
		    			int j = freqBandIdx[b];
		    			int k = freqBandIdx[b+1];
		    			accum = 0;
		    			for (int h = j; h < k; h ++)
		    			{
		    				accum += fftBufferR[h]*fftBufferR[h] + fftBufferI[h]*fftBufferI[h];
		    			}
		    			pushFrameFeature(accum/((double)(k - j)));
		    		}
	
		    		// Get MFCCs
		    		featureCepstrum = featureMFCC.cepstrum(fftBufferR, fftBufferI);
		    		for (int i = 0; i < featureCepstrum.length; i ++)
		    		{
		    			pushFrameFeature(featureCepstrum[i]);
	
		    		}
	
		    		
		    		// Write out features
		    		writeFeatureFrame(featureBuffer, audioStreamFeatures, OUTPUT_FORMAT_FLOAT);
	    		}
	    	}
	    }

	    // Try to close output streams because recording has been stopped
	    if (rawOn && closeStreamFile(audioStreamRaw))
	    	writeLogTextLine("Raw audio stream successfully stopped");
	    if (featureOn && closeStreamFile(audioStreamFeatures))
	      	writeLogTextLine("Audio feature stream successfully stopped");
	}

	@Override
	public void init() {
		Log.v(TAG,"audioWriter initialized");
	}

}
