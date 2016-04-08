package edu.jhu.hopkinspd.test;

import java.io.BufferedWriter;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;
import edu.jhu.hopkinspd.test.conf.TestConfig;
import android.app.Activity;
import android.content.*;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

public class TestActivity extends Activity
{
	protected static final String TAG = "TestActivity";


	int testNumber = 0;
	private Button button = null;
	private GlobalApp app;
	private Button nextButton = null;

	private long lastBackClick;
	
	

	private String logFileType = "activetest";

	private BufferedWriter logWriter;

	private CountDownTimer preTestTimer;
	private CountDownTimer inTestTimer = null;
	private CountDownTimer postTestTimer;

	TextView ins;
	
	TestConfig testConf;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		app = (GlobalApp) getApplication();
		logWriter = app.openLogTextFile(logFileType );
		Bundle bundle = getIntent().getExtras();
		testNumber = 0;
		if (bundle != null)
		{
			testNumber = bundle.getInt("TestNumber", 0);
		}
		
		testConf = TestConfig.getTestConfig(testNumber);
		setContentView(testConf.test_view);
		ins = (TextView)findViewById(R.id.dir_text);
		ins.setTextSize(GlobalApp.ACTIVE_TESTS_FONT_SIZE);
		
		Resources res = getResources();
		String text = String.format(res.getString(testConf.test_text), 
				testNumber+1);
		ins.setText(text);
		
		testConf.createTest(this);
		

		nextButton = (Button)findViewById(R.id.button_next);
		nextButton.setVisibility(View.GONE);
		
		
		nextButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Log.i(TAG, "onClick next button");
				// skip test
				skipTest();
			}
			
		});
		
		// Pre-test pause
		preTestTimer =
		new CountDownTimer(testConf.preTestPauseDur*1000, 1000)
		{
			public void onTick(long millisLeft){}
			public void onFinish()
			{
				// Pre-test vibrate?
				if (testConf.preTestVibrate)
				{
					((Vibrator)getSystemService(Context.VIBRATOR_SERVICE))
						.vibrate(GlobalApp.VIBRATE_DUR*1000);
				}
				
				runTest();
				
				if(app.isInTestDemo()){
					nextButton.setVisibility(View.VISIBLE);
				}else
					nextButton.setVisibility(View.GONE);
				
			}
		}.start();

	}

	private void setTextColor(boolean highContrast) {
		if(highContrast){
			this.nextButton.setTextColor(Color.WHITE);
			if(button != null)
				this.button.setTextColor(Color.WHITE);
			ins.setTextColor(Color.WHITE);
		}else{
			this.nextButton.setTextColor(Color.BLUE);
			if(button != null)
				this.button.setTextColor(Color.BLUE);
			
			ins.setTextColor(Color.BLACK);
		}
	}
	
	/**
	 * Used in demo mode.
	 * Skip the current test
	 */
	private void skipTest(){
		if(inTestTimer != null)
		{
			inTestTimer.cancel();
			finishTest();
		}
	}

	private synchronized void runTest()
	{
		testConf.runTest(this, logWriter);


		inTestTimer = 
				new CountDownTimer(testConf.testCaptureDur*1000, 
						GlobalApp.CHANGE_REACT_DUR*1000)
		{
			public void onTick(long millisLeft)
			{
				testConf.onInTestTimerTick(TestActivity.this);

			}

			public void onFinish()
			{
				finishTest();
			}
		}.start();
	}
	
	public synchronized void finishTest(){
		testConf.completeTest();

		postTestPause();
	}

	private void postTestPause()
	{
		// Post-test pause
		postTestTimer = 
		new CountDownTimer(testConf.postTestPauseDur*1000, 1000)
		{
			public void onTick(long millisLeft){}
			public void onFinish()
			{
				// Post-test vibrate?
				if (testConf.postTestVibrate)
				{
					((Vibrator)getSystemService(Context.VIBRATOR_SERVICE))
						.vibrate(GlobalApp.VIBRATE_DUR * 1000);
				}
				testComplete();
			}
		}.start();
	}
	
	private void testComplete()
	{
		Intent nextPage = null;

		// When we're done, roll onto the next test prep page
		testNumber += 1;
		if (!TestPrepActivity.singleTestMode 
				&& testNumber < TestConfig.getNumberOfTests())
		{
			if (app.isNextTestScreenOn()){
				nextPage = new Intent(app, TestPostActivity.class);
				nextPage.putExtra("TestNumber", testNumber);
			}else{
				nextPage = new Intent(app, TestPrepActivity.class);
				nextPage.putExtra("TestNumber", testNumber);
			}
		}
		else
		{
			nextPage = new Intent(app, TestEndActivity.class);
			if(TestPrepActivity.singleTestMode)
				Log.i(TAG, "single test end");
			else
				Log.i(TAG, "testNumber reaches to NUMBER_OF_TESTS: " + testNumber);
			
		}
		startActivity(nextPage);
		finish();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent me)
	{
		testConf.dispatchTouchEvent(me);

		if(app.isInTestDemo())
			return super.dispatchTouchEvent(me);
		else
			return false;
		
	}



	@Override
	public void onBackPressed()
	{
		long current = System.currentTimeMillis();
		if(current - lastBackClick > 15*1000 ){
			lastBackClick = current;
			Toast.makeText(app, "click again to exit current test", Toast.LENGTH_LONG).show();
		}else{
			testConf.cancelTest();
			if(inTestTimer != null)
				inTestTimer.cancel();
			if(preTestTimer != null)
				preTestTimer.cancel();
			if(postTestTimer != null)
				postTestTimer.cancel();

			super.onBackPressed();
		}
	}
}
