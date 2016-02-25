package edu.jhu.hopkinspd.utils;



import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefUtil {
	private static PrefUtil instance;
	private Context localCtx;
	public PrefUtil(Context ctx) {
		localCtx = ctx;
	}

	public static PrefUtil getInstance(Context ctx){
		if(instance == null){
			instance = new PrefUtil(ctx);
		}
		return instance;
	}
	
	public String getStringPref(String key)
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.localCtx);
    	return prefs.getString(key, "");
    }
	
    public int getIntPref(String key)
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.localCtx);
    	return prefs.getInt(key, -1);
    }

    public boolean getBooleanPref(String key)
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.localCtx);
    	return prefs.getBoolean(key, false);
    }
}
