package edu.jhu.hopkinspd.test.conf;

import java.io.BufferedWriter;

import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;
import edu.jhu.hopkinspd.test.TapCapture;
import edu.jhu.hopkinspd.test.TestActivity;

public class TapTestConfig extends TestConfig{
    public TapTestConfig(){
        test_name = R.string.test_dexterity;
        pre_test_text = R.string.ins_dexterity;
        pre_icon = R.drawable.tap_test;
        test_text = R.string.dir_dexterity;
        test_view = R.layout.testtappage;
        postTestPauseDur = 2;            
    }
	
	private TapCapture tapObj = null;
	private Button button1 = null, button2 = null;
	@Override
	public void runTest(TestActivity activity,BufferedWriter logWriter) {
		button1.setVisibility(View.VISIBLE);
		button2.setVisibility(View.VISIBLE);
		tapObj = new TapCapture(GlobalApp.getApp(), this);
		tapObj.startRecording();
	}
	@Override
	public void completeTest() {
		button1.setVisibility(View.INVISIBLE);
		button2.setVisibility(View.INVISIBLE);
		tapObj.stopRecording();
		tapObj.destroy();
	}
	@Override
	public void cancelTest() {
		button1.setVisibility(View.INVISIBLE);
		button2.setVisibility(View.INVISIBLE);
		if (tapObj != null){
			tapObj.stopRecording();
			tapObj.destroy();
		}
	}
	@Override
	public void createTest(TestActivity activity) {
		button1 = (Button)activity.findViewById(R.id.tap1_button);
		button1.setVisibility(View.INVISIBLE);
		button2 = (Button)activity.findViewById(R.id.tap2_button);
		button2.setVisibility(View.INVISIBLE);
	}
	@Override
	public void dispatchTouchEvent(MotionEvent me) {
		if (tapObj != null)
		{
			tapObj.handleTouchEvent(me);
		}
	}
}