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
