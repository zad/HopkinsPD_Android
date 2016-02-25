package edu.jhu.hopkinspd.utils;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import edu.jhu.hopkinspd.GlobalApp;

public class FileUtil {
	private static FileUtil instance;
	public FileUtil(Context ctx) {
		localCtx = ctx;
		streams = new HashMap<String, DataOutputStream>();
	}

	public static FileUtil getInstance(Context ctx){
		if(instance == null){
			instance = new FileUtil(ctx);
		}
		return instance;
	}

	private Context localCtx;
	private HashMap<String, DataOutputStream> streams;
	
	public DataOutputStream openStreamFile(String streamName, String timeStamp, String streamExt)
	{
		String userID = getStringPref(GlobalApp.PREF_KEY_USERID);
		String rootPath = getStringPref(GlobalApp.PREF_KEY_ROOT_PATH);
		String fileName = rootPath + "/" + streamName + "_" + userID + "_" + timeStamp + "." + streamExt;
		DataOutputStream dos = null;
	    try
	    {
	    	dos = new DataOutputStream(new FileOutputStream(fileName));
	    }
	    catch (FileNotFoundException e)
	    {
	        e.printStackTrace();
	    }
	    streams.put(streamName, dos);
	    return dos;
	}
	
	public boolean closeStreamFile(String streamName)
	{
		DataOutputStream stream = streams.get(streamName);
		boolean closed = false;
		if (stream != null)
		{
	        try
	        {
	        	stream.flush();
	        	stream.close();
	        	closed = true;
	        }
	        catch (IOException e)
	        {
	            e.printStackTrace();
	        }
		}
		return closed;
	}
	
	public void writeTextLine(String[] items, String streamName)
	{
		DataOutputStream stream = streams.get(streamName);
		if (stream != null)
		{
			try
			{
				for (int i = 0; i < items.length; i ++)
				{
					// Text strings in CSV format
					if (i < (items.length - 1))
						stream.writeBytes(items[i] + ",");
					else
						stream.writeBytes(items[i]);
				}
				
				// New line for CSV files
				stream.writeByte(10);
				stream.flush();
			}
	        catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
	}
	
    public String getStringPref(String key)
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.localCtx);
    	return prefs.getString(key, "");
    }
}
