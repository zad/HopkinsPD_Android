package edu.jhu.hopkinspd.test.conf;

import java.io.BufferedWriter;


import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;
import edu.jhu.hopkinspd.test.ReactCapture;
import edu.jhu.hopkinspd.test.TestActivity;

public class ReactionTestConfig extends TestConfig{
    public ReactionTestConfig(){
        test_name = R.string.test_reaction;
        pre_test_text = R.string.ins_reaction;
        pre_icon = R.drawable.reaction_test;
        test_text = R.string.dir_reaction;
        test_view = R.layout.testreactpage;
        testCaptureDur = 30;
        help_link = "https://youtu.be/Brz2yZp_O7M";
    }
    
	private ReactCapture reactObj = null;
	private boolean reactButtonOn = false;
	
	private Button button;

	@Override
	public void runTest(final TestActivity activity, BufferedWriter logWriter) {
		reactObj = new ReactCapture(GlobalApp.getApp(), this);
		reactObj.startRecording();
	}
	@Override
	public void completeTest() {
		reactObj.stopRecording();
		reactObj.destroy();
	}
	@Override
	public void cancelTest() {
		
		if(reactObj != null)
		{
			reactObj.stopRecording();
			reactObj.destroy();
		}
	}
	@Override
	public void createTest(TestActivity activity) {
		button = (Button)activity.findViewById(R.id.button_react);
		button.setVisibility(View.INVISIBLE);
	}
	@Override
	public void dispatchTouchEvent(MotionEvent me) {
		if (reactObj != null)
		{
			reactObj.handleTouchEvent(me, reactButtonOn);
		}
	}
	
	public void onInTestTimerTick(TestActivity activity) {
		// Choose new random tap button status
		int reactButtonVisible = View.INVISIBLE;
		boolean random = Math.random() > 0.5;
		if (random != reactButtonOn)
		{
			reactButtonOn = random;
			button = (Button)activity.findViewById(R.id.button_react);
			if (reactButtonOn)
			{
				reactButtonVisible = View.VISIBLE;
			}
			button.setVisibility(reactButtonVisible);
			reactObj.handleTouchEvent(null, reactButtonOn);
		}
	}
	
}
