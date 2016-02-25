package edu.jhu.hopkinspd;

import java.util.ArrayList;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.RelativeLayout;

@SuppressLint({ "NewApi", "SetJavaScriptEnabled" })
public class DashboardFragment extends Fragment{
	WebView webView = null;
	String type;
	BarChart chart;

	public DashboardFragment(String t) {
		type = t;
		
	}

	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_container, container);
//		webView = (WebView)v.findViewById(R.id.webView);
		
//	    updateDashboard();
		chart = new BarChart(getActivity());
		RelativeLayout rl = (RelativeLayout)v.findViewById(R.id.relativeLayout);
		rl.addView(chart);
		chart.setDescription("Symptom Fluctuation 03/06/2015");
		chart.setDescriptionTextSize(15f);
		chart.setDrawBarShadow(false);
		chart.setDrawGridBackground(false);
		chart.getAxisLeft().setDrawGridLines(false);
		chart.getAxisLeft().setDrawLabels(false);
		chart.getAxisRight().setDrawGridLines(false);
		chart.getAxisRight().setDrawLabels(false);
		chart.animateY(1500);
		setData(24, 5);
		Legend l = chart.getLegend();
		l.setPosition(LegendPosition.BELOW_CHART_LEFT);
		l.setForm(LegendForm.SQUARE);
		l.setFormSize(9f);
		l.setTextSize(11f);
		l.setXEntrySpace(4f);
		
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	public static final int[] VORDIPLOM_COLORS = {
		Color.rgb(140, 234, 255),
		Color.rgb(255, 247, 140),
		Color.rgb(192, 255, 140), 
		Color.rgb(255, 208, 140),
		Color.rgb(255, 140, 157)
		
		};
	
	@SuppressLint("ResourceAsColor")
	private void setData(int count, int range) {
		ArrayList<String> xVals = new ArrayList<String>();
		for(int i = 0; i < count; i++)
			xVals.add((float)(i) + "");
		
		ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
		int values[] = {-1,-1,-1,-1,-1,-1,-1,-1,0,1,2,2,3,4,3,2,1,2,2,2,2,2,-1,-1};
		for (int i = 0; i < count; i++) {
			
			int val = (int) values[i];
			float[] vals = new float[5];
			if(val >= 0) vals[val] = val + 1;
			yVals1.add(new BarEntry(vals, i));
		}
		BarDataSet set1 = new BarDataSet(yVals1, "");
		set1.setStackLabels(new String[] {
				"Frozen", "Slow Movement", "Good Movement", "Some Dyskinesia", "Severe Dyskinesia"
				});
		set1.setColors(VORDIPLOM_COLORS);
		set1.setDrawValues(false);
		ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
		dataSets.add(set1);
		BarData data = new BarData(xVals, dataSets);
		// data.setValueFormatter(new MyValueFormatter());
		data.setValueTextSize(10f);
//		data.setValueTypeface(mTf);
		chart.setData(data);
		}


	@Override
	public void onResume() {
//		updateDashboard();  
		super.onResume();
	}




	public class WebAppInterface {

	     @JavascriptInterface
		 public int getNum1() {
	    	 return 1;
		 }
		  
		 @JavascriptInterface
		 public int getNum2() {
			 return 2;
		 }
		  
		 @JavascriptInterface
		 public int getNum3() {
			 return 3;
		 }
		  
		 @JavascriptInterface
		 public int getNum4() {
			 return 4;
		 }
		  
		 @JavascriptInterface
		 public int getNum5() {
			 return 5;
		 }
		 
		 
	}


	protected void updateDashboard() {		
		if(true || type.equals("Day"))
		{
			
			webView.setBackgroundColor(0);
			webView.addJavascriptInterface(new WebAppInterface(), "Android");
		
		    webView.getSettings().setJavaScriptEnabled(true); 
		    webView.getSettings().setLoadWithOverviewMode(true);
		    webView.getSettings().setUseWideViewPort(true);
			webView.loadUrl("file:///android_asset/timeline.html");
			Log.d("DashboardFragment", "update dashboard");
		}
		
	}
}
