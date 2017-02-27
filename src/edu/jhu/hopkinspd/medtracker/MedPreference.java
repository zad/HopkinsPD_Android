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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.jhu.hopkinspd.GlobalApp;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class MedPreference extends ListPreference{

	private static final String TAG = "MedPreference";
	private GlobalApp app;
	private String key;
	private HashMap<String, List<String>> medications;

	public MedPreference(Context context, AttributeSet attrs) {
		super(context, attrs);      
		key = this.getKey();
	    app = GlobalApp.getApp();
	    medications = loadEntries();
	    setData();  
	}
	
	public void show(){
		this.showDialog(null);
	}
	
	public void setData(){
		if(medications != null){
	    	Log.d(TAG,"load medcation brands:" + medications.keySet().size());
	    	if(key.equals("medBrand")){
	    		String[] brands = medications.keySet().toArray(new String[medications.size()]);
	    		setEntries(brands);         
		        setEntryValues(brands);         
//		        setValueIndex(initializeIndex("pre_selected_medbrand"));	
	    	}else if(key.equals("medName")){
	    		String selected_brand = app.getStringPref("medBrand", null);
	    		if(selected_brand != null){
	    			List<String> names = medications.get(selected_brand);
	    			if(names != null){
	    				String[] nameArray = names.toArray(new String[names.size()]);
		    			setEntries(nameArray);
		    			setEntryValues(nameArray);	
	    			}
	    			
//	    			setValueIndex(initializeIndex("pre_selected_medname"));
	    		}
	    	}
	    		
	    }
        
	}
	
	
	public void setMedNames(String brand){
		if(medications != null){
	    	if(key.equals("medName")){
	    		if(brand != null){
	    			List<String> names = medications.get(brand);
	    			String[] nameArray = names.toArray(new String[names.size()]);
	    			setEntries(nameArray);
	    			setEntryValues(nameArray);
//	    			setValueIndex(initializeIndex("pre_selected_medname"));
	    		}
	    	}
	    		
	    }
        
	}	
	private HashMap<String, List<String>> loadEntries(){
		HashMap<String, List<String>> medications = new HashMap<String, List<String>>();
		try {
			InputStream is = app.getAssets().open("medications.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			
			boolean newBrand = true;
			String line, brand = null;
			ArrayList<String> meds = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if(newBrand && line.length()>0){
					brand = line;
					newBrand = false;
					meds = new ArrayList<String>();
				}else{
					if(line.length()>0){
						meds.add(line);
					}else{
						medications.put(brand, meds);
						newBrand = true;
						brand = "";
						meds = null;
					}
				}
			}
			
			if(brand.length()>0 && meds != null){
				medications.put(brand, meds);
			}
			br.close();
			return medications;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

//	private CharSequence[] entries() {      
//        //action to provide entry data in char sequence array for list          
//        String myEntries[] = {"one", "two", "three", "four", "five"};         
//
//        return myEntries;  
//    }  
//
//    private CharSequence[] entryValues() {      
//        //action to provide value data for list           
//     
//        String myEntryValues[] = {"ten", "twenty", "thirty", "forty", "fifty"};
//        return myEntryValues;
//    }

//    private int initializeIndex(String pre_key) {
//        //here you can provide the value to set (typically retrieved from the SharedPreferences)
//        //...
//    	return app.getIntPref(pre_key);
//    }

	
	

}
