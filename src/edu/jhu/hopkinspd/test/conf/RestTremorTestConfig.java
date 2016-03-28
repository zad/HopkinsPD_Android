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
        help_link = "https://youtu.be/cPd1Ct0xOCg";
    }
	
	public RestTremorTestConfig(String hand) {
        if(hand.compareTo("left")==0){
            test_name = R.string.test_rest_tremor_left;
            pre_test_text = R.string.ins_rest_tremor_left;
            test_text = R.string.dir_rest_tremor_left;
        }else if(hand.compareTo("right")==0){
            test_name = R.string.test_rest_tremor_right;
            pre_test_text = R.string.ins_rest_tremor_right;
            test_text = R.string.dir_rest_tremor_right;
        }
        pre_icon = R.drawable.rest_tremor_test;
        test_view = R.layout.testpage;
        preTestPauseDur = 5;
        testCaptureDur = 45;
        preTestVibrate = true;
        postTestVibrate = true;   
        help_link = "https://youtu.be/cPd1Ct0xOCg";
    }

    private AccelCapture accelObj = null;
	private GyroCapture gyroObj = null;
	
	@Override
	public void runTest(TestActivity activity, BufferedWriter logWriter) {
		GlobalApp app = GlobalApp.getApp();
		accelObj = new AccelCapture(app, this);
		accelObj.startRecording(null);
		
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
