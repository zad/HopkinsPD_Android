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
package edu.jhu.hopkinspd.medlog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.StringUtils;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.MainActivity;
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
