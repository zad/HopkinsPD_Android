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
