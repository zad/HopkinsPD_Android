package edu.jhu.hopkinspd;



import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;


import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;

public class BaseService extends Service
{
	protected BufferedWriter logTextStream = null;
	protected String logFileName = "";
	protected GlobalApp app;
	
	

	@Override
	public void onCreate() {
		app = (GlobalApp)this.getApplication();
		super.onCreate();
	}


	public String prettyDateString(Date time)
	{
    	return DateFormat.format("yyyy/MM/dd kk:mm:ss", time).toString();
	}
	
    public String getStringPref(String key)
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	return prefs.getString(key, "");
    }
    
    public String getStringPref(String key, String defaultValue)
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	return prefs.getString(key, defaultValue);
    }

    public boolean getBooleanPref(String key)
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	return prefs.getBoolean(key, false);
    }
    
    public void setBooleanPref(String key, boolean value)
    {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	Editor ed = preferences.edit();
    	ed.putBoolean(key, value);
    	ed.commit();
    }
    
    public long getLongPref(String key)
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	return prefs.getLong(key, 0l);
    }
    

    
    public Date getDatePref(String key)
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	return new Date(prefs.getLong(key, 0l));
    }
    
    public void setLongPref(String key, long value)
    {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	Editor ed = preferences.edit();
    	ed.putLong(key, value);
    	ed.commit();
    }

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	
	public String timeString(Date time)
	{
    	return DateFormat.format("yyyyMMdd_kkmmss", time).toString();
	}
}
