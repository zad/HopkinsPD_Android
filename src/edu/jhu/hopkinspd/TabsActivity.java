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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;


public class TabsActivity extends SherlockFragmentActivity{
	
    private ActionBar mActionBar;
    private ViewPager mPager;
    private Tab tab;
    private GlobalApp app;
    private ViewPagerAdapter viewpageradapter;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the view from activity_main.xml
        setContentView(R.layout.activity_main);
 
        app = (GlobalApp) getApplication();
        // Activate Navigation Mode Tabs
        mActionBar = getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        
        // Locate ViewPager in activity_main.xml
        mPager = (ViewPager) findViewById(R.id.pager);
 
        // Activate Fragment Manager
        FragmentManager fm = getSupportFragmentManager();
 
        // Capture ViewPager page swipes
        ViewPager.SimpleOnPageChangeListener ViewPagerListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Find the ViewPager Position
                mActionBar.setSelectedNavigationItem(position);
            }
        };
 
        mPager.setOnPageChangeListener(ViewPagerListener);
        // Locate the adapter class called ViewPagerAdapter.java
        viewpageradapter = new ViewPagerAdapter(fm);
        // Set the View Pager Adapter into ViewPager
        mPager.setAdapter(viewpageradapter);
 
        // Capture tab button clicks
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
 
            @Override
            public void onTabSelected(Tab tab, FragmentTransaction ft) {
                // Pass the position on tab click to ViewPager
                mPager.setCurrentItem(tab.getPosition());
            }
 
            @Override
            public void onTabUnselected(Tab tab, FragmentTransaction ft) {
                
            }
 
            @Override
            public void onTabReselected(Tab tab, FragmentTransaction ft) {
                
            }
        };
 
        // Create first Tab
        tab = mActionBar.newTab().setText(getString(R.string.fragment1)).setTabListener(tabListener);
        mActionBar.addTab(tab);
 
        // Create second Tab
        tab = mActionBar.newTab().setText(getString(R.string.fragment2)).setTabListener(tabListener);
        mActionBar.addTab(tab);
 
        // Create third Tab
        tab = mActionBar.newTab().setText(getString(R.string.fragment3)).setTabListener(tabListener);
        mActionBar.addTab(tab);
 
        if(!app.isUserInfoAvailable()){
			app.showUserSettings();
			Toast.makeText(app, R.string.msgSetupPrefs, Toast.LENGTH_LONG).show();
		}
    }
	
	
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    super.onPrepareOptionsMenu(menu);
	    MenuItem clearDataItem = menu.findItem(R.id.clear_data);
	    clearDataItem.setEnabled(app.getBooleanPref(getString(R.string.clearDataOn)));
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		// Handle item selection
		switch(item.getItemId()){
			case R.id.action_settings:
				if(app.getBooleanPref(GlobalApp.PREF_KEY_SWITCH)){
					Toast.makeText(getApplicationContext(), "You cannot change settings when the app is running", Toast.LENGTH_SHORT).show();
				}else
					showUserSettings();
				return true;
			case R.id.clear_data:
				new clearDataTask().execute("");
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private class clearDataTask extends AsyncTask<String,String,String> {

		@Override
		protected String doInBackground(String... arg0) {
			app.clearData();
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(app, "data cleared", Toast.LENGTH_LONG).show();
			super.onPostExecute(result);
		}
		
		
	}
	

	
	private void showUserSettings() {
		// Go to UserSettingActivity
		Intent i = new Intent(app, UserSettingActivity.class);
		startActivity(i);
	}
}
