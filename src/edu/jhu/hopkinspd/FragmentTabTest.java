package edu.jhu.hopkinspd;

import java.io.BufferedWriter;
import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;


import edu.jhu.cs.hinrg.dailyalert.android.activities.FormEntryActivity;
import edu.jhu.hopkinspd.test.TestPrepActivity;
 
public class FragmentTabTest extends SherlockFragment {
	public static final String TAG = GlobalApp.TAG + "|" + "FragmentTabSurvey";
	private GlobalApp app;
	BufferedWriter logTextStream = null;
	
	private Button testButton = null;
	private Button surveyButton = null;
//	private TextView timeText = null;
	private TextView counterText = null;

//	private TextView promptText = null;

    @Override
    public SherlockFragmentActivity getSherlockActivity() {
        return super.getSherlockActivity();
    }
 
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	// Get the view from fragmenttab1.xml
    	app = (GlobalApp)getActivity().getApplication();
//		if(!app.checkUserInfo()){
//			app.showUserSettings();
//			Toast.makeText(app, R.string.msgSetupPrefs, Toast.LENGTH_LONG).show();
//		}
		app.initActiveTests();
		View view = inflater.inflate(R.layout.fragmenttab2, container, false);
		Date now = new Date();
        testButton = (Button)view.findViewById(R.id.testButton);
        testButton.setText(R.string.testButton);
        
        testButton.setOnClickListener(new OnClickListener(){
       
			@Override
			public void onClick(View v) {
				Log.i(TAG, "Take test button pressed");
				if(!app.isUserInfoAvailable()){
					app.showUserSettings();
					Toast.makeText(app, R.string.msgSetupPrefs, Toast.LENGTH_LONG).show();
					return;
				}
				Intent takeTests = new Intent(app, TestPrepActivity.class);

				// Jump to specific test
//				takeTests.putExtra("TestNumber", GlobalApp.TEST_REACTION);
				startActivity(takeTests);
				
			}
        	
        });
        
        surveyButton = (Button)view.findViewById(R.id.updrsButton);
        surveyButton.setText(R.string.surveyButton);
        surveyButton.setOnClickListener(new OnClickListener(){
       
			@Override
			public void onClick(View v) {
				if(!app.isUserInfoAvailable()){
					app.showUserSettings();
					Toast.makeText(app, R.string.msgSetupPrefs, Toast.LENGTH_LONG).show();
					return;
				}
				String path = app.getFormPath();
				Intent i = new Intent("edu.jhu.cs.hinrg.dailyalert.android.action.FormEntry");
                i.putExtra(FormEntryActivity.KEY_FORMPATH, path);
				startActivity(i);
			}
        	
        });
//        timeText = (TextView)view.findViewById(R.id.timertext);
        counterText = (TextView)view.findViewById(R.id.messagetext);
//        promptText = (TextView)view.findViewById(R.id.prompttext);
        counterText.setText("Current time\n" + app.prettyDateString(now));
//        promptText.setText("Initializing ...");
		return view;
    }
 
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        setUserVisibleHint(true);
    }
 
}
