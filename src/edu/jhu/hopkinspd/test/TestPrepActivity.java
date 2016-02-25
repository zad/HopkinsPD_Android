package edu.jhu.hopkinspd.test;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.*;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

public class TestPrepActivity extends Activity implements SensorEventListener
{
	private static final int SENSOR_TYPE = Sensor.TYPE_PROXIMITY;
	private static final int SENSOR_RATE = SensorManager.SENSOR_DELAY_FASTEST;
	protected static final String TAG = GlobalApp.TAG + "|" + "TestPrepActivity";;
	
	int[] testList = {R.string.ins_voice,
			R.string.ins_balance, R.string.ins_gait,
			R.string.ins_dexterity, R.string.ins_reaction,
			R.string.ins_rest_tremor, R.string.ins_postural_tremor
			};

	int[] iconList = {
			R.drawable.voice_test,
			R.drawable.balance_test,
			R.drawable.gait_test,
			R.drawable.tap_test,
			R.drawable.reaction_test,
			R.drawable.rest_tremor_test,
			R.drawable.postural_tremor_test,
			};
	
	private SensorManager sensorManager = null;
	private Sensor sensor = null;

	int testNumber = 0;
	Button next = null;
	TextView ins;
	ImageView icon;
	GlobalApp app;
	public static boolean singleTest;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
	    super.onCreate(savedInstanceState);
	    app = (GlobalApp) getApplication();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(SENSOR_TYPE);
        sensorManager.getDefaultSensor(SENSOR_TYPE);
        sensorManager.registerListener(this, sensor, SENSOR_RATE);
	    
	    setContentView(R.layout.testpreppage);
	    
	    ins = (TextView)findViewById(R.id.text_ins);
	    ins.setTextSize(GlobalApp.ACTIVE_TESTS_FONT_SIZE);
	    icon = (ImageView)findViewById(R.id.icon);
	    Bundle bundle = getIntent().getExtras();
	    if (bundle != null)
	    {
	    	// single test
	    	testNumber = bundle.getInt("TestNumber", 0);
		    
	    }else{
	    	// all tests
			testNumber = app.getNextTestNumber(0);
	    }
	    
		if(testNumber >= GlobalApp.NUMBER_OF_TESTS)
		{
			Log.i(TAG, "testNumber reaches to NUMBER_OF_TESTS: " + testNumber);
			finish();
			return;
		}
		ins.setText(testList[testNumber]);
		icon.setImageResource(iconList[testNumber]);
	 
	    
		next = (Button)findViewById(R.id.button_starttest);
        next.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
            	Log.i(TAG, "Start test " + testNumber + " button pressed");
                Intent nextTest = new Intent(TestPrepActivity.this, TestActivity.class);
                nextTest.putExtra("TestNumber", testNumber);
                startActivity(nextTest);
                finish();
            }

        });
        setTextColor(app.getBooleanPref(getString(R.string.colorHighContrastOn)));
		
    }
	
	private void setTextColor(boolean highContrast) {
		if(highContrast){
			this.next.setTextColor(Color.WHITE);
			ins.setTextColor(Color.WHITE);
		}else{
			this.next.setTextColor(Color.BLUE);
			ins.setTextColor(Color.BLACK);
		}
	}

	private long lastBackClick;
    
    @Override
    public void onBackPressed()
    {
		long current = System.currentTimeMillis();
		if(current - lastBackClick > 15*1000 ){
			lastBackClick = current;
			Toast.makeText(app, "click again to exit current test", Toast.LENGTH_LONG).show();
		}else{
			super.onBackPressed();
		}
    }

	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
	}

	public void onSensorChanged(SensorEvent event)
	{
		if (event.values[0] > 0)
		{
			next.setEnabled(true);
		}
		else
		{
			next.setEnabled(false);
		}
	}
}
