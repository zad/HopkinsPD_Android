package edu.jhu.hopkinspd.test.conf;

import java.io.BufferedWriter;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;
import edu.jhu.hopkinspd.test.AudioCapture;
import edu.jhu.hopkinspd.test.TestActivity;

public class VoiceTestConfig extends TestConfig{
    public VoiceTestConfig(){
        test_name = R.string.test_voice;
        pre_test_text = R.string.ins_voice;
        pre_icon = R.drawable.voice_test;
        test_text = R.string.dir_voice;
        test_view = R.layout.testpage;
        // TODO: add voice test video
    }
    
	
	
	private AudioCapture audioObj = null;
	
	@Override
	public void runTest(TestActivity activity, BufferedWriter logWriter) {
		GlobalApp app = GlobalApp.getApp();
		audioObj = new AudioCapture(app, this);
		if(!audioObj.startRecording())
		{
			String text = "Recording is not initialized. Please try again later.";		
			app.writeLogTextLine(logWriter, text, true);
		}
	}
	@Override
	public void completeTest() {
		if(audioObj != null){
			audioObj.stopRecording();
			audioObj.destroy();	
		}
	}
	@Override
	public void cancelTest() {
		if(audioObj != null){
			audioObj.stopRecording();
			audioObj.destroy();	
		}
	}
	@Override
	public void createTest(TestActivity activity) {
		
	}
	
}
