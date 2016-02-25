package edu.jhu.hopkinspd;



import java.io.IOException;
import java.util.Locale;
import java.util.regex.Pattern;




import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.BaseAdapter;

import android.widget.Toast;


public class UserSettingActivity extends PreferenceActivity implements
OnSharedPreferenceChangeListener

{
    
	private static final String TAG = GlobalApp.TAG + "|UserSettingActivity";
	private GlobalApp app;
	
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (GlobalApp) getApplication();
        Log.i(TAG, "onCreate: set default settings");
        addPreferencesFromResource(R.xml.settings);
    }

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
	    super.onResume();
//	    adapter = (BaseAdapter)getPreferenceScreen().getRootAdapter();
	    // Set up a listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	    SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        findPreference(GlobalApp.PREF_KEY_USERID).setSummary(
        		sp.getString(GlobalApp.PREF_KEY_USERID,
        				getString(R.string.default_userID)));
        findPreference(GlobalApp.PREF_KEY_USERID).setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    
                	if( Pattern.matches(GlobalApp.USERID_REX, (String) newValue))
                		return true;
                	else{
                		Toast.makeText(app, R.string.prompt_userid_invalid, Toast.LENGTH_LONG).show();
    					return false;
                	}
                }

            });
//        
//        findPreference(GlobalApp.PREF_KEY_USERID).setEnabled(!GlobalApp.FIXED_CONFIG_USERID);
//        findPreference(GlobalApp.PREF_KEY_ENCRYPT_KEY).setEnabled(!GlobalApp.FIXED_CONFIG_USERPASSWORD);
        if(!getStringPref(GlobalApp.PREF_KEY_ENCRYPT_KEY).equals(""))
        	findPreference(GlobalApp.PREF_KEY_ENCRYPT_KEY).setSummary("******");
        findPreference(GlobalApp.PREF_KEY_BATTERY_MIN_LEVEL).setSummary(
        		String.format("App will be paused when the batttery level is lower than %s percent",
        				sp.getString(GlobalApp.PREF_KEY_BATTERY_MIN_LEVEL,
        				getString(R.string.default_battery_threshold))));

        findPreference(getResources().getString(R.string.sensorRate)).setSummary(
        		getResources().getStringArray(R.array.sensorRatePref)
        		[Integer.parseInt(getStringPref(getResources().getString(R.string.sensorRate)))]);
        findPreference(getResources().getString(R.string.sensorDataFormat)).setSummary(
        		getResources().getStringArray(R.array.sensorDataFormatPref)
        		[Integer.parseInt(getStringPref(getResources().getString(R.string.sensorDataFormat)))]);
        findPreference(getResources().getString(R.string.wakeLock)).setSummary(
        		getResources().getStringArray(R.array.wakeLockPref)
        		[
        		 getStringArrayIndex(getResources().getStringArray(R.array.wakeLockValuesPref),
        				 getStringPref(getResources().getString(R.string.wakeLock)))
        		 ]);
        findPreference(getResources().getString(R.string.sensorGPSInt)).setSummary(
        		sp.getString(getString(R.string.sensorGPSInt), ""));
        findPreference(getResources().getString(R.string.sensorGARInt)).setSummary(
        		sp.getString(getString(R.string.sensorGARInt), ""));
        findPreference(getResources().getString(R.string.zipInterval)).setSummary(
        		getResources().getStringArray(R.array.intervelPref)
        		[
        		 getStringArrayIndex(getResources().getStringArray(R.array.intervelValuesPref), 
        				getStringPref(getResources().getString(R.string.zipInterval)))
        		 ]);
        findPreference(getResources().getString(R.string.uploadInterval)).setSummary(
        		getResources().getStringArray(R.array.intervelPref)
        		[getStringArrayIndex(getResources().getStringArray(R.array.intervelValuesPref), 
        				getStringPref(getResources().getString(R.string.uploadInterval)))
        		 ]);
        
        // reset list preference
        ListPreference listPref = (ListPreference) findPreference(getResources().getString(R.string.reset));
        initResetListPreference(listPref);
        listPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				String setting = ((String) newValue).substring(0, ((String)newValue).length()-4);
				// reset task
				//(new ResetTask()).execute(setting);
				if(app.reset(setting, true)){
					//adapter.notifyDataSetChanged();
					Toast.makeText(app, "Restored to setting: " + setting, Toast.LENGTH_LONG).show();
					finish();
					app.showUserSettings();
				}else{
					Toast.makeText(app, "Fail to restored to setting: " + setting, Toast.LENGTH_LONG).show();
				}	
				return false;
			}
        });
        
        // language list preference
        ListPreference lanPref = (ListPreference) findPreference(getResources().getString(R.string.language));
        Log.d(TAG, "lang:" + app.getStringPref(getString(R.string.language)));
        lanPref.setValue(app.getStringPref(getString(R.string.language), getString(R.string.default_language)));
        lanPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String lang = (String)newValue;
				Log.d(TAG, "new lang:" + lang);
				app.setStringPref(getString(R.string.language), lang);
				Log.d(TAG, "lang change to:" + app.getStringPref(getString(R.string.language)));
		        
				Toast.makeText(app, "Set language to: " + lang, Toast.LENGTH_LONG).show();
				Log.d(TAG, "set language to " + lang);
				setLocale(lang);
				return false;
			}
		});
        
        if(app.getBooleanPref("disableUserEdit")){
        	// Disable user info edit in setting activity
        	findPreference(GlobalApp.PREF_KEY_USERID).setSelectable(false);
        	findPreference(GlobalApp.PREF_KEY_ENCRYPT_KEY).setSelectable(false);
        }
        
        if(app.getBooleanPref("hideAdvancedConfig")){
        	// Hide advanced config
        	findPreference("Advanced").setSelectable(false);
        }
	}
	
	public void setLocale(String lang) {
		app.setLanguage(lang);
        Intent refresh = new Intent(this, UserSettingActivity.class);
        finish();
        startActivity(refresh);
    }
	
	private void initResetListPreference(ListPreference listPref) {
		String[] entries = getSettings();
		// TODO Auto-generated method stub
		if(entries != null)
		{
			listPref.setEntries(entries);
			listPref.setEntryValues(entries);
		}
		
	}

	private String[] getSettings() {
		// TODO Auto-generated method stub
		AssetManager am=this.getAssets();
		try {
			String[] settings = am.list("settings");
			return settings;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	


	private int getStringArrayIndex( String[] array, String value){
		for(int i=0;i<array.length;i++){
			if(value.equals(array[i])){
				return i;
			}
		}
		return -1;
	}
	
    public String getStringPref(String key)
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	return prefs.getString(key, "");
    }
    
   

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
	    super.onPause();
	    // Unregister the listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// Set values in summaries
		Log.v(TAG, key);
		if(key.equals(GlobalApp.PREF_KEY_USERID)){
			
			Preference pref = findPreference(key);
			pref.setSummary(sharedPreferences.getString(key,""));
		} 
		else if(key.equals(GlobalApp.PREF_KEY_ENCRYPT_KEY)) 
		{
			Preference pref = findPreference(key);
			pref.setSummary("******");
		}
		else if (key.equals(GlobalApp.PREF_KEY_BATTERY_MIN_LEVEL))
		{
			Preference pref = findPreference(key);
			pref.setSummary(
					String.format("App will be paused when the batttery level is lower than %s percent", 
							sharedPreferences.getString(key,"")));
		}
		
		else if (key.equals(getResources().getString(R.string.sensorRate)))
			findPreference(key).setSummary(
	        		getResources().getStringArray(R.array.sensorRatePref)
	        		[Integer.parseInt(getStringPref(getResources().getString(R.string.sensorRate)))]);
		else if (key.equals(getResources().getString(R.string.sensorDataFormat)))
			findPreference(key).setSummary(
	        		getResources().getStringArray(R.array.sensorDataFormatPref)
	        		[Integer.parseInt(getStringPref(getResources().getString(R.string.sensorDataFormat)))]);
		else if (key.equals(getResources().getString(R.string.wakeLock)))
			findPreference(getResources().getString(R.string.wakeLock)).setSummary(
	        		getResources().getStringArray(R.array.wakeLockPref)
	        		[
	        		 getStringArrayIndex(getResources().getStringArray(R.array.wakeLockValuesPref),
	        				 getStringPref(getResources().getString(R.string.wakeLock)))
	        		 ]);
		else if (key.equals(getResources().getString(R.string.sensorGPSInt)))
			findPreference(getResources().getString(R.string.sensorGPSInt)).setSummary(
					getStringPref(getResources().getString(R.string.sensorGPSInt)));
		else if (key.equals(getResources().getString(R.string.sensorGARInt)))
			findPreference(getResources().getString(R.string.sensorGARInt)).setSummary(
					getStringPref(getResources().getString(R.string.sensorGARInt)));
		else if (key.equals(getResources().getString(R.string.zipInterval)))
			findPreference(getResources().getString(R.string.zipInterval)).setSummary(
	        		getResources().getStringArray(R.array.intervelPref)
	        		[
	        		 getStringArrayIndex(getResources().getStringArray(R.array.intervelValuesPref), 
	        				getStringPref(getResources().getString(R.string.zipInterval)))
	        		 ]);
		else if (key.equals(getResources().getString(R.string.uploadInterval)))
		{	
			findPreference(getResources().getString(R.string.uploadInterval)).setSummary(
	        		getResources().getStringArray(R.array.intervelPref)
	        		[getStringArrayIndex(getResources().getStringArray(R.array.intervelValuesPref), 
	        				getStringPref(getResources().getString(R.string.uploadInterval)))
	        		 ]);
			if(app.isUploadServiceOn())
			{
				app.startUploadService();
				Log.i(TAG, "startUploadService");
			}
			
		}
		else if (key.equals(getResources().getString(R.string.uploadTimeRandom))){
			if(app.isUploadServiceOn())
			{
				app.startUploadService();
				Log.i(TAG, "startUploadService");
			}
		}
		else if (key.equals(getResources().getString(R.string.uploadServiceOn))){
			
			if(app.isUploadServiceOn())
			{
				app.startUploadService();
				Log.i(TAG, "startUploadService");
			}else{
				app.stopUploadService();
				Log.i(TAG, "stopUploadService");
			}
		}
		else if(key.equals(getResources().getString(R.string.ntpSyncOn))){
			if(app.isNTPSyncServiceOn())
			{
				app.startNTPSyncService();
				Log.i(TAG, "startNTPService");
			}else{
				app.stopNTPSyncService();
				Log.i(TAG, "stopNTPService");
			}
		}
		else if(key.equals(getResources().getString(R.string.ntpSyncInt))){
			if(app.isNTPSyncServiceOn())
			{
				app.startNTPSyncService();
				Log.i(TAG, "startNTPService");
			}
		}
	}
	
	
    
}
