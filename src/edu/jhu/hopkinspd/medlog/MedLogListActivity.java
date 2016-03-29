package edu.jhu.hopkinspd.medlog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;


import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;

public class MedLogListActivity extends ListActivity {

    private static final String TAG = "MedLogListActivity";
    private ArrayList<ArrayList<String>> medDose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        setTitle("Current Medications");
        setContentView(R.layout.medloglistactivity);
        readMedLogCSV();
        ArrayAdapter<ArrayList<String>> adapter = 
                new MedDoseAdapter(this, android.R.layout.simple_list_item_1,
                        medDose);
        setListAdapter(adapter);
        super.onCreate(savedInstanceState);
    }

    
    private void readMedLogCSV(){
        medDose = new ArrayList<ArrayList<String>>();
        GlobalApp app = GlobalApp.getApp();
        InputStream is;
        try {
            is = app.getAssets().open("Meds_Log.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if(!header){
                    ArrayList<String> item = new ArrayList<String>();
                    Log.d(TAG, line);
                    String[] splitted = 
                            line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                    item.add(splitted[0].replace("\"", ""));
                    for(String dose : 
                        splitted[1].replace("\"", "").split(",")){
                        item.add(dose);
                    }
                    Log.d(TAG, Arrays.toString(item.toArray()));
                    medDose.add(item);
                    
                }else
                    header = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}
