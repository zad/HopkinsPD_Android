package edu.jhu.hopkinspd.medtracker;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import com.google.android.gms.plus.model.people.Person.Name;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;
import edu.jhu.hopkinspd.GlobalApp.DataFileListItem;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TimePicker;

public class MedActivity extends PreferenceActivity{
	private static final String TAG = GlobalApp.TAG + "|MedTrackerActivity";
	private GlobalApp app;
	
	private boolean selected_medBrand = false;
	private boolean selected_medName = false;
	private boolean selected_medDosage = false;
	private boolean selected_medDate = false;
	private boolean selected_medTime = false;
	
	private boolean enableDatetime = false;
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ActionBar actionBar = getActionBar();
//        actionBar.setHomeButtonEnabled(true);
//        actionBar.setDisplayHomeAsUpEnabled(true);
        app = (GlobalApp) getApplication();
        Log.i(TAG, "onCreate: set default settings");
        addPreferencesFromResource(R.xml.medintake);
        
        ListView v = getListView();
        Button back = new Button(this);
        back.setTextSize(20);
        back.setText("Back");
        back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
        v.addFooterView(back);
        setTitle("Set Medication");
        
        final MedPreference name = (MedPreference)findPreference("medName");
        String nameDef = app.getStringPref("medName");
        if(nameDef.length() > 0)
        {
        	selected_medName = true;
        	name.setSummary(nameDef);
        }
        name.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary((CharSequence) newValue);
				selected_medName = true;
				((ListPreference)name).setValue((String) newValue);
				return false;
			}
		});
        
        final Preference brand = (Preference)findPreference("medBrand");
        String brandDef = app.getStringPref("medBrand");
        if(brandDef.length() > 0)
        {
        	selected_medBrand = true;
        	brand.setSummary(brandDef);
        }
        brand.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary((CharSequence) newValue);
				selected_medBrand = true;
				app.setStringPref("medName", "");
				name.setSummary(getString(R.string.medName_summary));
				selected_medName = false;
				name.setMedNames((String) newValue);
				name.setValue("");
				((ListPreference)brand).setValue((String) newValue);
				name.show();
				return false;
			}
		});
        
        
        
        /*
        final EditTextPreference dosage = (EditTextPreference)findPreference("medDosage");
        String doseDef = app.getStringPref("medDosage");
        if(doseDef.length() > 0)
        {
        	selected_medDosage = true;
        	dosage.setSummary(doseDef);
        }
        
        dosage.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
        
        dosage.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String dose = (String)newValue;
				dosage.setSummary(dose);
				app.setStringPref("medDosage", dose);
				selected_medDosage = true;
				dosage.setText((String) newValue);
				return false;
			}
		});
		*/
        
        final Preference date = (Preference)findPreference("medDate");
        date.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				final Calendar c = Calendar.getInstance();
				int year = c.get(Calendar.YEAR);
				int month = c.get(Calendar.MONTH);
				int day = c.get(Calendar.DAY_OF_MONTH);
				final Dialog dateDialog = new DatePickerDialog(MedActivity.this, 
					new DatePickerDialog.OnDateSetListener() {
						
						@Override
						public void onDateSet(DatePicker view, int year, int monthOfYear,
								int dayOfMonth) {
							String selectedDate = dayOfMonth + "-"
									+ (monthOfYear + 1) + "-" + year; 
							date.setSummary(selectedDate);
							app.setStringPref("medDate", selectedDate);
							selected_medDate = true;
						}
					}, year, month, day);
				dateDialog.show();
				return false;
			}
		});
        final Preference time = (Preference)findPreference("medTime");
        time.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				final Calendar c = Calendar.getInstance();
				int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
				int minute = c.get(Calendar.MINUTE);
				
				boolean is24HourView = true;
				
				final Dialog timeDialog = new TimePickerDialog(MedActivity.this, 
						new TimePickerDialog.OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						String selectedTime = hourOfDay + ":" + minute + " "; 
						time.setSummary(selectedTime);
						app.setStringPref("medTime", selectedTime);
						selected_medTime = true;
					}
				}, hourOfDay, minute, is24HourView); 
						 
				timeDialog.show();
				return false;
			}
		});
        
        if(!enableDatetime){
        	date.setEnabled(false);
        	time.setEnabled(false);
        	
        	PreferenceScreen ps = getPreferenceScreen();
        	ps.removePreference(date);
        	ps.removePreference(time);
        }
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
	}

	public void done(View v){
		if(!enableDatetime)
			setCurrentDatetime();
		if(selected_medBrand &&
				selected_medName &&
				selected_medDosage &&
				selected_medDate && 
				selected_medTime
			) 
		{
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
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

			// set title
			alertDialogBuilder.setTitle("Succeed!");
			alertDialogBuilder
			.setCancelable(false)
			.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, close
					// current activity
					finish();
				}
			  });
			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
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


	
	
}
