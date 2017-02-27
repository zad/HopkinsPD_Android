/*
 * Copyright (c) 2015 Johns Hopkins University. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 * - Neither the name of the copyright holder nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
