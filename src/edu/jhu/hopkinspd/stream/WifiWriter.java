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
package edu.jhu.hopkinspd.stream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager.RegistrationListener;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellIdentityGsm;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;
import edu.jhu.hopkinspd.receiver.Alarm;


public class WifiWriter extends StreamWriter
{

	public static final String STREAM_NAME = "wifi";
	private static final String TAG = GlobalApp.TAG +  "|WifiWriter";
	private static final String LOCK_KEY = WifiWriter.class.getName();
	
	private boolean WIFI_SCAN = false;
	private int scanInterval;
	
	private int previousWifiState;
	private int numberOfAttempts;
	
	
	public static final String STREAM_NAME_WIFI = "wifi";
	public static final String STREAM_NAME_WIFISCAN = "wifiscan";
	
	private static final String WIFI_ACTION = "edu.jhu.hopkinspd.wifi";
	private static final String WIFISCAN_ACTION = "edu.jhu.hopkinspd.wifiscan";
	
	private DataOutputStream streamWifi = null, streamWifiScan = null;
	private WifiLock wifiLock;
	private WifiManager wm;
	private PendingIntent pi, piScan;
	private AlarmManager am;
	private GlobalApp app;
	
	public WifiWriter(GlobalApp app){
		super(app);
		this.app = app;
		logTextStream = app.openLogTextFile(STREAM_NAME);
		WIFI_SCAN = app.getBooleanPref(app.getString(R.string.sensorWifiScanOn));
		scanInterval = Integer.parseInt(app.getStringPref(app.getString(R.string.sensorWifiScanInt)));
		Log.d(TAG, "wifi_scan " + WIFI_SCAN + " wifi_scan_int " + scanInterval );
	    writeLogTextLine("Created " + this.getClass().getName());
	    
	}
	
	@Override
	public void init() {	
		Log.v(TAG,"WifiWriter initialized");
		writeLogTextLine("WifiWriter initialized");
	    
	}
	
    public String toString(){
    	return STREAM_NAME;
    }
	
	public void start(Date startTime)
    {
		isRecording = true;
		String timeStamp = timeString(startTime);
		prevSecs = ((double)startTime.getTime())/1000.0d;
		
		
	    // screen on/off
	    
    	streamWifi = openStreamFile(STREAM_NAME_WIFI, timeStamp,  GlobalApp.STREAM_EXTENSION_CSV);
    	streamWifiScan = openStreamFile(STREAM_NAME_WIFISCAN, timeStamp, GlobalApp.STREAM_EXTENSION_CSV);
    	wm = (WifiManager) app.getSystemService(Context.WIFI_SERVICE);
    	app.registerReceiver(wifiReceiver, new IntentFilter(WIFI_ACTION));
    	pi = PendingIntent.getBroadcast( app, 0, new Intent(WIFI_ACTION), 0 );
    	am = (AlarmManager)(app.getSystemService( Context.ALARM_SERVICE ));
    	am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 1000, pi);
    	if(WIFI_SCAN)
    	{
    		app.registerReceiver(wifiReceiver, new IntentFilter(WIFISCAN_ACTION));
    		piScan = PendingIntent.getBroadcast(app, 0, new Intent(WIFISCAN_ACTION), 0);
    		am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), scanInterval*1000, piScan);
    	}
	    writeLogTextLine("WifiWriter started");
    }
	
    public void stop(Date stopTime)
    {
    	isRecording = false;
		am.cancel(pi);
		
		app.unregisterReceiver(wifiReceiver);
		if(WIFI_SCAN){
			closeStreamFile(streamWifiScan);
		}
		closeStreamFile(streamWifi);
    } 
    
    public void restart(Date time)
    {
    	String timeStamp = timeString(time);
    	prevSecs = ((double)time.getTime())/1000.0d;	
    	DataOutputStream wifi = streamWifi;
    	streamWifi = openStreamFile(STREAM_NAME_WIFI, timeStamp, GlobalApp.STREAM_EXTENSION_CSV);
    	if(closeStreamFile(wifi))
    		writeLogTextLine("wifi recording successfully restarted");
    	DataOutputStream wifiScan = streamWifiScan;
    	streamWifiScan = openStreamFile(STREAM_NAME_WIFISCAN, timeStamp, GlobalApp.STREAM_EXTENSION_CSV);
    	if(closeStreamFile(wifiScan))
    		writeLogTextLine("wifi scan recording successfully restarted");
    }
    
    public void startWifiScan(){
    	acquireWifiLock();
	    saveWifiStateAndRunScan();
    	numberOfAttempts = 0;
    	app.registerReceiver(scanResultsReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }
    
    public void stopWifiScan(){
    	try{
	    	app.unregisterReceiver(scanResultsReceiver);
	    	releaseWifiLock();
	        loadPreviousWifiState();
    	}catch(IllegalArgumentException e){
    		writeLogTextLine("wifi scan stopped after multiple attempts");
    	}
    }
    
    public void destroy()
    {
		writeLogTextLine(this.getClass().getName() + " destroied");
    }

    private void acquireWifiLock() {
        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, LOCK_KEY);
        wifiLock.setReferenceCounted(false);
        wifiLock.acquire();
    }

	private void releaseWifiLock() {
        if (wifiLock != null) {
            if (wifiLock.isHeld()) {
                    wifiLock.release();
            }
            wifiLock = null;
        }
	}
	
	
	private void loadPreviousWifiState() {
        // Enable wifi if previous sate was enabled, otherwise disable
		try{
			wm.setWifiEnabled(previousWifiState == WifiManager.WIFI_STATE_ENABLED);
		}catch(SecurityException e){
			writeLogTextLine(e.getMessage());
		}
	}
	
	private void saveWifiStateAndRunScan() {
        int state = wm.getWifiState();
        if(state==WifiManager.WIFI_STATE_DISABLING ||state==WifiManager.WIFI_STATE_ENABLING){
            // Wait until the Wifi state stabilizes, then run
            app.registerReceiver(waitingToStartScanReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        } else {
            previousWifiState = state;
            runScan();
        }
	}
	
	

	private BroadcastReceiver waitingToStartScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent i) {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(i.getAction())) {
                try {
                    app.unregisterReceiver(this);  // TODO: sometimes this throws an IllegalArgumentException
                    saveWifiStateAndRunScan();
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Unregistered WIFIE_STATE_CHANGED receiver more than once.");
                }
            }
        }
	};
	
	private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
        	if(i.getAction().equals(WIFI_ACTION)){
        		String current = app.prettyDateString(new Date());
        		WifiInfo info = wm.getConnectionInfo();
            	String wifi = current + ", " + getConnectionInfoToString(info);
            	Log.i(TAG, "wifi:"+ wifi);
            	writeTextLine(wifi.trim(), streamWifi);
        	}
        	else if(i.getAction().equals(WIFISCAN_ACTION)){
        		startWifiScan();
        		Log.i(TAG, "start wifi scan");
        	}
        }

		private String getConnectionInfoToString(WifiInfo info) {
			String ssid = info.getSSID();
			if(ssid == null)
				return "SSID: -1, BSSID: -1, MAC: -1, Supplicant state: 0" +
						",RSSI: -200,Link speed: -1, Net ID: -1";
			if(ssid.length() > 1 && ssid.startsWith("\"") && ssid.endsWith("\""))
				ssid = ssid.substring(1, ssid.length()-1);
			String bssid = info.getBSSID();
			String mac = info.getMacAddress();
			String state = info.getSupplicantState().name();
			int rssi = info.getRssi();
			int speed = info.getLinkSpeed();
			int nid = info.getNetworkId();
			return "SSID: " + ssid
					+ ",BSSID: " + bssid
					+ ",MAC: " + mac
					+ ",Supplicant state: " + state
					+ ",RSSI: " + rssi
					+ ",Link speed: " + speed
					+ ",Net ID: " + nid;
		}
    };

    private void runScan() {
        numberOfAttempts += 1; 
        int state = wm.getWifiState();
        if (state == WifiManager.WIFI_STATE_ENABLED) {
	        boolean successfulStart = wm.startScan();
	        if (successfulStart) {
	                Log.i(TAG, "WIFI scan started succesfully");
	        } else {
	                Log.e(TAG, "WIFI scan failed.");
	        }
	        numberOfAttempts = 0;
        } else if (numberOfAttempts <= 3) { 
            // Prevent infinite recursion by keeping track of number of attempts to change wifi state
            app.registerReceiver(retryScanReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
            wm.setWifiEnabled(true);
        } else {  // After 3 attempts stop trying
            // possibly send error
            Log.e(TAG, "wifi scan number of attempts > 3");
            stopWifiScan();
        }
        
    }
    
    private BroadcastReceiver retryScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent i) {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(i.getAction())) {
                try {
                    app.unregisterReceiver(this);
                    runScan();
                } catch (IllegalArgumentException e) {
                    // Not sure why, but sometimes this is not registered
                    // Probably two intents at once
                }
            }
        }
    };
    
    private BroadcastReceiver scanResultsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                List<ScanResult> results = wm.getScanResults();
                if (results != null) {
                	String time = app.prettyDateString(new Date());
                    for (ScanResult result : results) {
                    	String str = time + "," + getScanResultToString(result);
                    	Log.i(TAG, "scanResult: " + str);
                    	writeTextLine(str, streamWifiScan);
                    }
                }
                stopWifiScan();
            }
        }

		private String getScanResultToString(ScanResult result) {
			String ssid = result.SSID;
			String bssid = result.BSSID;
			String cap = result.capabilities;
			int level = result.level;
			int freq = result.frequency;
			
			return "SSID: " + ssid
					+ ",BSSID: " + bssid
					+ ",capabilities: " + cap 
					+ ",level: " + level
					+ ",frequency: " + freq;
		}
    };
}
