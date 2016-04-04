package edu.jhu.hopkinspd.test.conf;

import java.io.BufferedWriter;
import java.util.Arrays;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;
import edu.jhu.hopkinspd.test.AccelCapture;
import edu.jhu.hopkinspd.test.GyroCapture;
import edu.jhu.hopkinspd.test.TestActivity;
import edu.jhu.hopkinspd.test.TestPrepActivity;

public class GaitTestConfig extends TestConfig{
    private static final String phonePositionPref = "phone_position";
    private static final String[] PHONE_POSITIONS = {
            "Hip pocket or waist-attached holster",
            "Fanny pack",
            "Jacket or shirt pocket",
            "Armband",
            "In-hand (with less tremor, if applicable)",
            "Purse/bag with strap over shoulder",
            "Other/not specified",
            // This is the text that will be displayed as hint.
            "Please select phone position",
    };

    public GaitTestConfig(){
        test_name = R.string.test_gait;
        pre_test_text = R.string.ins_gait;
        pre_icon = R.drawable.gait_test;
        test_text = R.string.dir_gait;
        test_view = R.layout.testpage;
        test_disp_name = this.getDisplayName(test_name);
        preTestPauseDur = 5;
        testCaptureDur = 30;
        preTestVibrate = true;
        postTestVibrate = true;    
        pre_test_layout = R.layout.testpreppage_gait;
        help_link = "https://youtu.be/2BidIYn1Nrg";
        audio_ins = R.raw.gait_test;
    }
	
	
	private AccelCapture accelObj = null;
	private GyroCapture gyroObj = null;
	
	@Override
	public void runTest(TestActivity activity, BufferedWriter logWriter) {
		GlobalApp app = GlobalApp.getApp();
		// save phone position
		String phone_position = app.getStringPref(phonePositionPref);
		
		
		accelObj = new AccelCapture(app, this);
		accelObj.startRecording(phone_position);
		
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
	
	public void createPreTest(final TestPrepActivity testPrepActivity) {
	    Spinner spin = 
	        (Spinner)testPrepActivity.findViewById(R.id.phone_position_spinner);
	    spin.setOnItemSelectedListener(new PhonePositionSelectedListener());
	    
	    ArrayAdapter<String> adapter = 
	            new ArrayAdapter<String>(testPrepActivity, 
	                    R.layout.phone_position_spinner) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView)v.findViewById(android.R.id.text1)).setText("");
                    ((TextView)v.findViewById(android.R.id.text1))
                        .setHint(getItem(getCount())); //"Hint to be displayed"
                }

                return v;
            }       

            @Override
            public int getCount() {
                // you dont display last item. It is used as hint.
                return super.getCount()-1; 
            }

        };

        adapter.setDropDownViewResource(
                R.layout.phone_position_spinner);
        for(String item : PHONE_POSITIONS)
            adapter.add(item);
        
        


        spin.setAdapter(adapter);
        spin.setSelection(adapter.getCount()); //set the hint the default selection so it appears on launch.
        
	    
	    
	    
	    GlobalApp app = GlobalApp.getApp();
	    String selected = app.getStringPref(phonePositionPref, null);
	    if(selected != null){
	        spin.setSelection(Arrays.asList(PHONE_POSITIONS).indexOf(selected));
	    }
	    Button next = 
	            (Button)testPrepActivity.findViewById(R.id.button_starttest);
	    next.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                GlobalApp app = GlobalApp.getApp();
                String selected = app.getStringPref(phonePositionPref, null);
                if(selected.compareTo(
                        PHONE_POSITIONS[PHONE_POSITIONS.length-1])!=0){
                    int testNumber = testPrepActivity.getTestNumber();
                    Log.i(TestPrepActivity.TAG, "Start test " + 
                            testNumber + " button pressed");
                    Intent nextTest = new Intent(testPrepActivity, TestActivity.class);
                    nextTest.putExtra("TestNumber", testNumber);
                    testPrepActivity.startActivity(nextTest);
                    testPrepActivity.finish();    
                }
                else{
                    Toast.makeText(testPrepActivity, 
                            "Please select your phone position first!",
                            Toast.LENGTH_SHORT).show();
                }
            }

        });
	}
	
	public class PhonePositionSelectedListener 
	    implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent, View view, 
	            int pos,long id) {
	        Toast.makeText(parent.getContext(), 
	                parent.getItemAtPosition(pos).toString() + " selected!",
	        Toast.LENGTH_SHORT).show();
	        GlobalApp app = GlobalApp.getApp();
	        app.setStringPref(phonePositionPref, 
	                parent.getItemAtPosition(pos).toString());
	    }

	    @Override
	    public void onNothingSelected(AdapterView<?> arg0) {
	      
	    }
	}
}
