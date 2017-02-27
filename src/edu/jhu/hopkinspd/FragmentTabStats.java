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
import java.util.Date;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.androidplot.pie.PieChart;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;

import edu.jhu.cs.hinrg.dailyalert.android.activities.FormEntryActivity;


public class FragmentTabStats extends SherlockFragment {
	public static final String TAG = GlobalApp.TAG + "|" + "FragmentTabStats";
	private static final Object[] DATE = {"Today", "This Week", "This Month", "All"};
	private int dateIdx = 0;
	private GlobalApp app;
	BufferedWriter logTextStream = null;
	private Spinner timeSpinner;
	private ProgressDialog pd;
	
    private PieChart pie;
    
    

//	
    
    @Override
	public void onDestroy() {
    	if (pd!=null) {
			pd.dismiss();
		}
    	super.onDestroy();
    }
    
    @Override
    public SherlockFragmentActivity getSherlockActivity() {
        return super.getSherlockActivity();
    }
 
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	app = (GlobalApp)getActivity().getApplication();
    	
//		if(!app.checkUserInfo()){
//			app.showUserSettings();
//			Toast.makeText(app, R.string.msgSetupPrefs, Toast.LENGTH_LONG).show();
//		}
		// check sdcard?
		app.createSurveyForm();
		View view = inflater.inflate(R.layout.fragmenttab3, container, false);
		
		timeSpinner = (Spinner) view.findViewById(R.id.timeSpinner);
		timeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View arg1,
					int pos, long arg3) {
//				Toast.makeText(parent.getContext(), 
//						"Show data in " + parent.getItemAtPosition(pos).toString(),
//						Toast.LENGTH_SHORT).show();
				drawCharts(parent.getItemAtPosition(pos).toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
		
		// initialize our XYPlot reference:
        pie = (PieChart) view.findViewById(R.id.mySimplePieChart);
        
        
		
        
		return view;
    }
 
    protected void drawCharts(String date) {
		if(date.equals(DATE[0])){
			// today
			dateIdx = 0;
		}else if (date.equals(DATE[1])){
			dateIdx = 1;
		}else if (date.equals(DATE[2])){
			dateIdx = 2;
		}else if (date.equals(DATE[3])){
			dateIdx = 3;
		}
		


		drawChart();
	}

    private String[] activity_name = {"driving", "biking", "walking", "still"};
    private int[] activity_color = {Color.WHITE, Color.RED, Color.GREEN, Color.BLUE};
    
	private void drawChart() {
		Log.i(TAG,"drawChart");
		AsyncTask<Void, Void, Void> drawChartTask = new AsyncTask<Void, Void, Void>(){

			@Override
			protected void onPreExecute() {
//				if(FragmentTabStats.this.isVisible())
//				{
//					pd = new ProgressDialog(app);
//					pd.setTitle("Processing...");
//					pd.setMessage("Please wait.");
//					pd.setCancelable(false);
//					pd.setIndeterminate(true);
//					pd.show();
//				}
				
			}
			
			@Override
			protected Void doInBackground(Void... params) {
		        pie.getTitleWidget().getLabelPaint().setTextSize(20);
		        int[] activities = app.getActivityStats(dateIdx);
		        String[] activity_time = getActivityTime(activities);
		        
		        Paint paint = new Paint();
		        paint.setTextSize(40);
		        
//		        pie.getSeriesSet().clear();
		        
		        pie.clear();
		        
		        for(int i=0;i<4;i++)
		        {
		        	if(activities[i] != 0)
		        	{
		        		SegmentFormatter sf = new SegmentFormatter(activity_color[i], 
		        				Color.DKGRAY,Color.DKGRAY, Color.DKGRAY);
		        		sf.setLabelPaint(paint);
		        		pie.addSeries(new Segment(activity_name[i]+"\n"+activity_time[i], activities[i]), sf);
		        	}
		        }
		        pie.redraw();
				return null;
			}
			
			private String[] getActivityTime(int[] activities) {
				String[] activity_time = new String[4];
				for(int i=0;i<4;i++){
					int seconds = activities[i];  
					if(seconds != 0){
						if(seconds < 60){
							activity_time[i] = seconds + "s";
						}else if (seconds < (60*60)){
							activity_time[i] = seconds/60 + "m" + seconds%60 + 's';
						}else
							activity_time[i] = seconds/(60*60) + "h" + (seconds%3600)/60 + "m" + seconds%60 + 's';
					}
				}
				return activity_time;
			}

			@Override
			protected void onPostExecute(Void result) {
				if (pd!=null) {
					pd.dismiss();
				}
			}
		};
		drawChartTask.execute();
	}

	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        setUserVisibleHint(true);
    }
    
	 
 
}
