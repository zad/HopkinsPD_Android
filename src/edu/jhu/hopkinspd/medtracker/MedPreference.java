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
