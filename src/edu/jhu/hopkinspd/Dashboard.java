package edu.jhu.hopkinspd;


import edu.jhu.hopkinspd.task.NTPSyncTask;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

@SuppressLint("SetJavaScriptEnabled")
public class Dashboard extends Activity{

	
	String dateType = "day";
	int width = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.dashboard);
		
		setupTabs();
		
	}
	
	
	
	@SuppressLint("NewApi")
	public class MyTabListener implements ActionBar.TabListener {
		Fragment fragment;
		
		public MyTabListener(Fragment fragment) {
			this.fragment = fragment;
		}
		
	    public void onTabSelected(Tab tab, FragmentTransaction ft) {
			ft.replace(R.id.fragment_container, fragment);
		}
		
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			ft.remove(fragment);
		}
		
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// nothing done here
		}
	}

	
	@SuppressLint("NewApi")
	private void setupTabs() {
		ActionBar.Tab tab1, tab2, tab3;
		
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		tab1 = actionBar.newTab().setText("Day");
		tab2 = actionBar.newTab().setText("Week");
		tab3 = actionBar.newTab().setText("Month");
		
		Fragment f1 = new DashboardFragment("Day");
		Fragment f2 = new DashboardFragment("Week");
		Fragment f3 = new DashboardFragment("Month");
		
		tab1.setTabListener(new MyTabListener(f1));
		tab2.setTabListener(new MyTabListener(f2));
		tab3.setTabListener(new MyTabListener(f3));
		
		actionBar.addTab(tab1);
		actionBar.addTab(tab2);
		actionBar.addTab(tab3);
	}





	
}
