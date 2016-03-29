package edu.jhu.hopkinspd.medlog;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;



import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MedLogActivity extends Activity{
	private static final String TAG = GlobalApp.TAG + "|MedTrackerActivity";
	private GlobalApp app;
	

	
	private boolean enableDatetime = false;
	
	private Button medButton;
	
	private TextView prompt;
	
	private CheckBox checkbox;
	
	public static final String TakingMedsPref = "TakingMedsPref";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (GlobalApp) getApplication();
        Log.i(TAG, "onCreate: set default settings");
        
        setTitle("Add Medication Intake");
        setContentView(R.layout.medlogactivity);
        
        prompt = (TextView) findViewById(R.id.med_prompt);
        
        medButton = (Button)findViewById(R.id.MedButton);
        
        medButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent in = new Intent(MedLogActivity.this, 
						MedLogListActivity.class);
				startActivity(in);
			}
		});
        
        checkbox = (CheckBox)findViewById(R.id.MedLogCheckBox);
        checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton arg0, 
                    boolean takingMeds) {
                // TODO Auto-generated method stub
                GlobalApp app = GlobalApp.getApp();
                if(takingMeds){
                    app.setBooleanPref(TakingMedsPref, true);
//                    Intent in = new Intent(MedLogActivity.this, 
//                            MedLogListActivity.class);
//                    startActivity(in);
                    medButton.setEnabled(true);
                }else{
                    app.setBooleanPref(TakingMedsPref, false);
                    medButton.setEnabled(false);
                }
            }});
    }
	
	
	
	@Override
    protected void onResume() {
	    GlobalApp app = GlobalApp.getApp();
	    boolean takingMeds = app.getBooleanPref(TakingMedsPref);
        if(takingMeds){
            checkbox.setChecked(true);
            medButton.setEnabled(true);
        }else{
            checkbox.setChecked(false);
            medButton.setEnabled(false);
        }
        super.onResume();
    }



    public void back(View view){
		finish();
	}
	
	
	
	public void cancel(View v){
		finish();
	}
	


	


	
	
}
