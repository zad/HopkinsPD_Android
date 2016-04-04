package edu.jhu.hopkinspd.test.conf;

import java.io.BufferedWriter;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;
import edu.jhu.hopkinspd.test.AccelCapture;
import edu.jhu.hopkinspd.test.GyroCapture;
import edu.jhu.hopkinspd.test.TestActivity;

public class PosturalTremorTestConfig extends TestConfig{
    public PosturalTremorTestConfig(){
        test_name = R.string.test_postural_tremor;
        pre_test_text = R.string.ins_postural_tremor;
        test_text = R.string.dir_postural_tremor;
        generalPosturalTremorTestConfig();
    }
	
	public PosturalTremorTestConfig(String hand) {
        if(hand.compareTo("left")==0){
            test_name = R.string.test_postural_tremor_left;
            pre_test_text = R.string.ins_postural_tremor_left;
            test_text = R.string.dir_postural_tremor_left;
        }else if(hand.compareTo("right")==0){
            test_name = R.string.test_postural_tremor_left;
            pre_test_text = R.string.ins_postural_tremor_left;
            test_text = R.string.dir_postural_tremor_left;
        }
        generalPosturalTremorTestConfig();
    }

	private void generalPosturalTremorTestConfig(){
	    pre_icon = R.drawable.postural_tremor_test;
        test_view = R.layout.testpage;
        test_disp_name = this.getDisplayName(test_name);
        preTestPauseDur = 5;
        testCaptureDur = 45;
        preTestVibrate = true;
        postTestVibrate = true;
        help_link = "https://youtu.be/6QjjBa1HFVk";
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
