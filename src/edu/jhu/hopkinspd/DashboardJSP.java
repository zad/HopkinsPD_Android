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
