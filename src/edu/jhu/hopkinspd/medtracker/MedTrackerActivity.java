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
package edu.jhu.hopkinspd.medtracker;

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
import android.widget.TextView;
import android.widget.Toast;

public class MedTrackerActivity extends Activity{
	private static final String TAG = GlobalApp.TAG + "|MedTrackerActivity";
	private GlobalApp app;
	
	private boolean selected_medBrand = false;
	private boolean selected_medName = false;
	private boolean selected_medDosage = false;
	private boolean selected_medDate = false;
	private boolean selected_medTime = false;
	
	private boolean enableDatetime = false;
	
	private Button medButton, addButton;
	
	private TextView prompt;
	
	private String[] NUM_TABLETS = 
		{
				"1","2","3","4","5","6","7","8","9","10"
		};
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (GlobalApp) getApplication();
        Log.i(TAG, "onCreate: set default settings");
        
        setTitle("Add Medication Intake");
        setContentView(R.layout.medtrackeractivity);
        
        prompt = (TextView) findViewById(R.id.med_prompt);
        
        medButton = (Button)findViewById(R.id.chooseMedButton);
        
        medButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent in = new Intent(MedTrackerActivity.this, 
						MedActivity.class);
				startActivity(in);
			}
		});
        
        addButton = (Button)findViewById(R.id.recordMedButton);
        
        addButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				done(v);
			}
		});        
    }
	
	public void back(View view){
		finish();
	}
	
	private void setCurrentDatetime() {
		Calendar c = Calendar.getInstance();
		int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);

		String selectedTime = hourOfDay + ":" + minute + " "; 
		
		app.setStringPref("medTime", selectedTime);
		String selectedDate = day + "-"
				+ (month + 1) + "-" + year; 
		
		app.setStringPref("medDate", selectedDate);
		selected_medDate = true;
		selected_medTime = true;
	}
	
	public void cancel(View v){
		finish();
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
//		selected_medBrand = false;
//		selected_medName = false;
//		selected_medDosage = false;
//		selected_medDate = false;
//		selected_medTime = false;
		String brandDef = app.getStringPref("medBrand");
        if(brandDef.length() > 0)
        	selected_medBrand = true;
        
		String nameDef = app.getStringPref("medName");
        if(nameDef.length() > 0)
        	selected_medName = true;
        
        String doseDef = app.getStringPref("medDosage");
        if(doseDef.length() > 0)
        	selected_medDosage = true;
        
		
        if(!selected_medBrand
        	|| !selected_medName	
        		){
        	addButton.setEnabled(false);
        	prompt.setText("Please choose your medication first!");
        }else{
        	addButton.setEnabled(true);
        	prompt.setText("Selected medication:\n" + nameDef);
        	
        }
        
	}

	public void done(View v){
		if(!enableDatetime)
			setCurrentDatetime();
		if(selected_medBrand &&
				selected_medName &&
//				selected_medDosage &&
				selected_medDate && 
				selected_medTime
			) 
		{
			// show dose selection dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("Select the number of tablets");
	        builder.setItems(NUM_TABLETS, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int item) {
	                // Do something with the selection
	            	app.setStringPref("medDosage", NUM_TABLETS[item]);
					recordDose();
					Toast.makeText(app, "Succeeded!", Toast.LENGTH_SHORT).show();
	            }

				
	        });
	        AlertDialog alert = builder.create();
	        alert.show();
			
			
			
			
			
		}
		else{
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

			// set title
			alertDialogBuilder.setTitle("Missing value");

			// set dialog message
			String missing = "";
			boolean first = true;
			if(!selected_medBrand)
			{
				if(!first) missing += ", "; 
				missing += "Brand";
				first = false;
			}
			if(!selected_medName)
			{
				if(!first) missing += ", "; 
				missing += "Name";
				first = false;
			}
			if(!selected_medDosage)
			{
				if(!first) missing += ", ";
				missing += "Number of tablets";
				first = false;
			}
			if(!selected_medDate)
			{
				if(!first) missing += ", ";
				missing += "Date";
				first = false;
			}
			if(!selected_medTime)
			{
				if(!first) missing += ", ";
				missing += "Time";
				first = false;
			}
			alertDialogBuilder
				.setMessage("Please select " + missing
						)
				.setCancelable(false)
				.setPositiveButton("Exit",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, close
						// current activity
						finish();
					}
				  })
				.setNegativeButton("Back",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
		}
	}


	private void recordDose() {
		// TODO Auto-generated method stub
		// record medication intake
		String medIntake = app.getStringPref("medBrand") 
				+ ";" + app.getStringPref("medName")
				+ ";" + app.getStringPref("medDosage")
				+ ";" + app.getStringPref("medDate")
				+ ";" + app.getStringPref("medTime");
		Log.v(TAG, app.getStringPref("medBrand"));
		Log.v(TAG, app.getStringPref("medName")); 
		Log.v(TAG, app.getStringPref("medDosage")); 
		Log.v(TAG, app.getStringPref("medDate")); 
		Log.v(TAG, app.getStringPref("medTime"));
		String filename = app.getMedTrackerFilename(new Date());
			
		try {
			File newFile = new File(filename);
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(newFile));
			dos.writeChars(medIntake);
			dos.flush();
			dos.close();
			String uploadPath = app.getStringPref(GlobalApp.PREF_KEY_ROOT_PATH, "") + "/" + GlobalApp.UPLOAD_SUBDIR;
    		String aesFilename = uploadPath + "/" + newFile.getName() + "." + GlobalApp.AES_FILE_EXTENSION;
 
			app.encryptFile(TAG, filename, aesFilename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
