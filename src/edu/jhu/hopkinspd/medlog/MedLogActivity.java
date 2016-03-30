package edu.jhu.hopkinspd.medlog;


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


import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;



public class MedLogActivity extends Activity{
	private static final String TAG = GlobalApp.TAG + "|MedTrackerActivity";
	
	
	public static final String[] RecentMedTakeTime = {
	        "Within five minutes",
	        "Within one hour",
	        "Within five hours",
	        "Within 12 hours",
	        "Haven't taken any medications"
	};
	

	
	private Button medButton;
	private RadioGroup rg;
	
	public static final String TakingMedsPref = "TakingMedsPref";
    public static final String LastMedUpdateDatePref = "LastMedUpdateDate";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.i(TAG, "onCreate: set default settings");
        
        setTitle("Add Medication Intake");
        setContentView(R.layout.medlogactivity);
        
        
        
        medButton = (Button)findViewById(R.id.MedButton);
        
        medButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent in = new Intent(MedLogActivity.this, 
						MedLogListActivity.class);
				startActivity(in);
			}
		});
        rg = (RadioGroup)findViewById(R.id.radioMed);
        
        rg.setOnCheckedChangeListener(new OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                
                
                
                GlobalApp app = GlobalApp.getApp();
                boolean takingMedsPref = app.getBooleanPref(TakingMedsPref);
                boolean checked = checkedId == R.id.radioMedOn;
                if(takingMedsPref != checked){
                    app.setBooleanPref(TakingMedsPref, checked);
                    app.setDatePref(LastMedUpdateDatePref, new Date());
                    if(checked)
                        checkSelectedMeds();
                }
                if(checked){
                    medButton.setEnabled(true);
                }else{
                    medButton.setEnabled(false);
                }
                
                
            }});

    }
	
	
	
	@Override
    protected void onResume() {
	    super.onResume();
	    GlobalApp app = GlobalApp.getApp();
	    boolean takingMeds = app.getBooleanPref(TakingMedsPref);
        if(takingMeds){
            rg.check(R.id.radioMedOn);
            checkSelectedMeds();
        }else{
            rg.check(R.id.radioMedOff);
        } 
    }

	private void checkSelectedMeds(){
	    if(!anyMedChecked()){
            AlertDialog.Builder builderInner = new AlertDialog.Builder(
                    this);
            builderInner.setMessage("No medication was selected!");
            builderInner.setTitle("Alert");
            builderInner.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {
                        dialog.dismiss();
                    }

                    
                });
            builderInner.show();
        }
	}

	private boolean anyMedChecked() {
        GlobalApp app = GlobalApp.getApp();
        String selectedStr = 
                app.getStringPref(MedDoseAdapter.MedDoseSelectedPref, "");
        if (selectedStr.contains(";checked;")) {
            return true;
        }
        return false;
    }

	

	public void back(View view){
        finish();
    }
    
    
    
    public void cancel(View v){
        finish();
    }
	
	
}
