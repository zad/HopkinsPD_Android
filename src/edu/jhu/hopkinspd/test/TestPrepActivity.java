package edu.jhu.hopkinspd.test;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;
import edu.jhu.hopkinspd.test.conf.TestConfig;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.*;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

public class TestPrepActivity extends Activity implements SensorEventListener
{
	private static final int SENSOR_TYPE = Sensor.TYPE_PROXIMITY;
	private static final int SENSOR_RATE = SensorManager.SENSOR_DELAY_FASTEST;
	public static final String TAG = GlobalApp.TAG + "|" + "TestPrepActivity";;
	
	private SensorManager sensorManager = null;
	private Sensor sensor = null;
	public static final String PRETEST_INS_PREF = "pretest_ins";
	
	int testNumber = 0;
	Button next = null;
	TextView ins;
	ImageView icon;
	GlobalApp app;
	public static boolean singleTestMode;
	private TestConfig testConf;
    private MediaPlayer mediaPlayer;
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
	    super.onCreate(savedInstanceState);
	    app = (GlobalApp) getApplication();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(SENSOR_TYPE);
        sensorManager.getDefaultSensor(SENSOR_TYPE);
        sensorManager.registerListener(this, sensor, SENSOR_RATE);
	    
	    Bundle bundle = getIntent().getExtras();
	    if(bundle == null)
	    	testNumber = 0;
	    else
	    	// single test mode contains TestNumber in bundle
	    	testNumber = bundle.getInt("TestNumber", 0);	
	    if(testNumber == 0)
	        TestConfig.updateEnabledTests();
	    
		if(testNumber >= TestConfig.getNumberOfTests())
		{
			Log.i(TAG, "testNumber reaches to NUMBER_OF_TESTS: " + testNumber);
			finish();
			return;
		}
		testConf = TestConfig.getTestConfig(testNumber);
		setContentView(testConf.pre_test_layout);
        
        ins = (TextView)findViewById(R.id.text_ins);
        ins.setTextSize(GlobalApp.ACTIVE_TESTS_FONT_SIZE);
        icon = (ImageView)findViewById(R.id.text_icon);
		Resources res = getResources();
		String text = String.format(res.getString(testConf.pre_test_text), testNumber+1);
		ins.setText(text);
		icon.setImageResource(testConf.pre_icon);
	 
	    
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
		testConf.createPreTest(this);
		
		
    }
	
	
	
	@Override
    protected void onResume() {
        super.onResume();
        if(testConf.audio_ins != 0)
        {    
            mediaPlayer = MediaPlayer.create(this, testConf.audio_ins);
        }
        if(app.getBooleanPref(PRETEST_INS_PREF)){
            // start audio
            if(mediaPlayer != null)
            {   
                try {
                    mediaPlayer.start();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            }
        }
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

    public int getTestNumber() {
        return testNumber;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pretest_menu, menu);
        MenuItem help = menu.findItem(R.id.pretest_help);
        help.setIcon(R.drawable.youtube);
        MenuItem audioIns = menu.findItem(R.id.pretest_ins);
        
        audioIns.setIcon(android.R.drawable.ic_lock_silent_mode_off);
        
        boolean audioOn = app.getBooleanPref(PRETEST_INS_PREF);
        setAudioIcon(audioIns, audioOn);
        if(testConf.audio_ins == 0)
            audioIns.setVisible(false);
        return true;
    }
    
    private void setAudioIcon(MenuItem audioIns, boolean on){
        if(on)
        {
            audioIns.setIcon(android.R.drawable.ic_lock_silent_mode_off);
            app.setBooleanPref(PRETEST_INS_PREF, on);
        }
        else
        {
            audioIns.setIcon(android.R.drawable.ic_lock_silent_mode);
            app.setBooleanPref(PRETEST_INS_PREF, on);
        }
    }
    
    private void switchAudioInstruction(MenuItem audioIns, boolean on){
        setAudioIcon(audioIns, on);
        if(on){
            if(testConf.audio_ins != 0)
            {    
                mediaPlayer = MediaPlayer.create(this, testConf.audio_ins);
            }
            // start audio
            if(mediaPlayer != null)
            {   
                try {
                    mediaPlayer.start();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            }
        }
        else{
            // cancel audio
            if(mediaPlayer != null && mediaPlayer.isPlaying())
                mediaPlayer.stop();
        }
    }
    
    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        // Handle item selection
        switch(item.getItemId()){
            case R.id.pretest_help:
                if(testConf.help_link != null){
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setData(Uri.parse(testConf.help_link));
                    startActivity(intent);
                        
                }
                return true;
            case R.id.pretest_ins:
                boolean audioOn = app.getBooleanPref(PRETEST_INS_PREF);
                switchAudioInstruction(item, !audioOn);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
