package edu.jhu.hopkinspd.test.conf;

import java.io.BufferedWriter;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;
import edu.jhu.hopkinspd.test.AccelCapture;
import edu.jhu.hopkinspd.test.GyroCapture;
import edu.jhu.hopkinspd.test.TestActivity;

public class BalanceTestConfig extends TestConfig{
    public BalanceTestConfig(){
        test_name = R.string.test_balance;
        pre_test_text = R.string.ins_balance;
        pre_icon = R.drawable.balance_test;
        test_text = R.string.dir_balance;
        test_view = R.layout.testpage;
        preTestPauseDur = 5;
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
	@Override
	public void createTest(TestActivity activity) {
		
	}
}
