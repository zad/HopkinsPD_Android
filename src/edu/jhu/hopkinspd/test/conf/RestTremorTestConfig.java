package edu.jhu.hopkinspd.test.conf;

import java.io.BufferedWriter;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;
import edu.jhu.hopkinspd.test.AccelCapture;
import edu.jhu.hopkinspd.test.GyroCapture;
import edu.jhu.hopkinspd.test.TestActivity;

public class RestTremorTestConfig extends TestConfig{
    public RestTremorTestConfig(){
        test_name = R.string.test_rest_tremor;
        pre_test_text = R.string.ins_rest_tremor;
        pre_icon = R.drawable.rest_tremor_test;
        test_text = R.string.dir_rest_tremor;
        test_view = R.layout.testpage;
        preTestPauseDur = 5;
        testCaptureDur = 45;
        preTestVibrate = true;
        postTestVibrate = true;    
    }
	
	private AccelCapture accelObj = null;
	private GyroCapture gyroObj = null;
	
	@Override
	public void runTest(TestActivity activity, BufferedWriter logWriter) {
		GlobalApp app = GlobalApp.getApp();
		accelObj = new AccelCapture(app, this);
		accelObj.startRecording();
		
		if(gyro_on){
			gyroObj = new GyroCapture(app, this);
			gyroObj.startRecording();
		}
	}
	@Override
	public void completeTest() {
		if(accelObj != null){
			accelObj.stopRecording();
			accelObj.destroy();	
		}
		if(gyro_on && gyroObj != null){
			gyroObj.stopRecording();
			gyroObj.destroy();
		}
	}
	@Override
	public void cancelTest() {
		if(accelObj != null){
			accelObj.stopRecording();
			accelObj.destroy();	
		}
		if(gyro_on && gyroObj != null){
			gyroObj.stopRecording();
			gyroObj.destroy();
		}
	}

}
