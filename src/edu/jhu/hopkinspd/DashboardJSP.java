package edu.jhu.hopkinspd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

@SuppressLint("SetJavaScriptEnabled")
public class DashboardJSP extends Activity{

	RadioGroup rg = null;
	WebView webView = null;
	String dateType = "day";
	int width = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);
		
		
//		rg = (RadioGroup) findViewById(R.id.radioGroup_date);
//		rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//			
//			@Override
//			public void onCheckedChanged(RadioGroup group, int checkedId) {
//				switch(checkedId){
//				case R.id.radio0:
//					dateType = "day";
//					break;
//				case R.id.radio1:
//					dateType = "week";
//					break;
//				case R.id.radio2:
//					dateType = "month";
//					break;
//				}
//				updateDashboard();
//			}
//		});
		
		webView = (WebView)findViewById(R.id.webView);
//		webView.setBackgroundColor(0);
		webView.addJavascriptInterface(new WebAppInterface(), "Android");

        webView.getSettings().setJavaScriptEnabled(true); 
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		updateDashboard();
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
		 
		 @JavascriptInterface
		 public int getWidth() {
			 Log.d("dash", "width:" + width);
			 return width;
		 }
	}

//	@SuppressWarnings("deprecation")
	protected void updateDashboard() {
		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		
//		this.width = display.getWidth();
		
		webView.loadUrl("file:///android_asset/timeline.html");
	}

	

}
