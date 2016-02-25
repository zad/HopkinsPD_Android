package edu.jhu.hopkinspd.stream;
import java.io.DataOutputStream;

import java.util.Date;

import android.content.Context;
import android.content.IntentFilter;

import android.telephony.PhoneStateListener;

import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;



import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.receiver.PhoneStateReceiver;



public class TelephonyWriter extends StreamWriter
{

	public static final String STREAM_NAME = "telephony";
	private static final String TAG = GlobalApp.TAG +  "|TelephonyWriter";
	
	

	private TelephonyManager tm;
	
	private PhoneStateReceiver phoneStateReceiver;
	
	
	
	
	
	private DataOutputStream streamCell = null;
	private int networkType = 0;
	private int phoneType;
	
	public TelephonyWriter(GlobalApp app){
		super(app);
		logTextStream = app.openLogTextFile(STREAM_NAME);
	    writeLogTextLine("Created " + this.getClass().getName());
	    
	}
	
	@Override
	public void init() {
		Log.v(TAG,"telephonyWriter initialized");
		writeLogTextLine("telephonyWriter initialized");
	    
	}
	
    public String toString(){
    	return STREAM_NAME;
    }
	
	public void start(Date startTime)
    {
		isRecording = true;
		String timeStamp = timeString(startTime);
		prevSecs = ((double)startTime.getTime())/1000.0d;
		
		// call state
		
//			LocationManager lm = (LocationManager) app.getSystemService(Context.LOCATION_SERVICE);
//			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		streamCell = openStreamFile(STREAM_NAME, timeStamp, GlobalApp.STREAM_EXTENSION_CSV);
		tm = (TelephonyManager) app.getSystemService(Context.TELEPHONY_SERVICE);
//		tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
		tm.listen(mPhoneListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		this.phoneStateReceiver = new PhoneStateReceiver(this); 
		app.registerReceiver(this.phoneStateReceiver, new IntentFilter("android.intent.action.PHONE_STATE"));
		app.registerReceiver(this.phoneStateReceiver, new IntentFilter("android.intent.action.NEW_OUTGOING_CALL"));
		networkType = tm.getNetworkType();
		phoneType = tm.getPhoneType();
		Log.i(TAG, "phoneType " + phoneType);
		
		
		writeLogTextLine("listening to cell status");
	
		writeLogTextLine("telephony stream started");
    }
	
    public void stop(Date stopTime)
    {
    	isRecording = false;
    	
//    		LocationManager lm = (LocationManager) app.getSystemService(Context.LOCATION_SERVICE);
//			lm.removeUpdates(this);
		tm = (TelephonyManager) app.getSystemService(Context.TELEPHONY_SERVICE);
		tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
		app.unregisterReceiver(phoneStateReceiver);
		
		closeStreamFile(streamCell);
		writeLogTextLine("unlistening to cell status");    	
    } 
    
    public void restart(Date time)
    {
    	prevSecs = ((double)time.getTime())/1000.0d;
    	DataOutputStream old = streamCell;
    	String timeStamp = timeString(time);
    	streamCell = openStreamFile(STREAM_NAME, timeStamp, GlobalApp.STREAM_EXTENSION_CSV);
    	if(closeStreamFile(old))
    		writeLogTextLine("telephony recording successfully restarted");
    }
    
    public void destroy()
    {
    	
    	
    	tm = null;
        mPhoneListener = null;
		writeLogTextLine(this.getClass().getName() + " destroied");
    }

	
		
	private PhoneStateListener mPhoneListener = new PhoneStateListener() {
//		public void onCallStateChanged(int state, String incomingNumber) {
//			// Save to log file?
//			try {
//				switch (state) {
//				case TelephonyManager.CALL_STATE_RINGING:
//					Log.v(TAG, "CALL_STATE_RINGING");
//					writeLogTextLine("CALL_STATE_RINGING");
//					break;
//				case TelephonyManager.CALL_STATE_OFFHOOK:
//					Log.v(TAG, "CALL_STATE_OFFHOOK");
//					writeLogTextLine("CALL_STATE_OFFHOOK");
//					break;
//				case TelephonyManager.CALL_STATE_IDLE:
//					Log.v(TAG, "CALL_STATE_IDLE");
//					writeLogTextLine("CALL_STATE_IDLE");
//					break;
//				default:
//					Log.i("Default", "Unknown phone state=" + state);
//					writeLogTextLine("CALL_STATE_DEFAULT " + state);
//				}
//			} catch (Exception e) {
//				Log.i("Exception", "PhoneStateListener() e = " + e);
//			}
//	  	}

		// has bug !!!
//		private String signalStrengthToString(SignalStrength ss){
//			
//			return ("gsmRss: " + ss.getGsmSignalStrength()
//			        + ", gsmBer: " + ss.getGsmBitErrorRate()
//			        + ", cdmaDbm: " + ss.getCdmaDbm()
//			        + ", cdmaEcio: " + ss.getCdmaEcio()
//			        + ", evdoDbm: " + ss.getEvdoDbm()
//			        + ", evdoEcio: " + ss.getEvdoEcio()
//			        + ", evdoSnr: " + ss.getEvdoSnr()
//			        + ", isGsm: " + (ss.isGsm() ? "gsm" : "cdma"));
//		}
		
		private String signalStrengthToString(SignalStrength ss){
			String raw = ss.toString();
			StringBuilder sb = new StringBuilder();
			String[] items = raw.split(" ");
			for(int i = 1; i < items.length ;i++)
				sb.append(items[i]).append(",");
				
			return sb.toString();
		}
		
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			Log.i(TAG, signalStrength.toString());
			String signal_str = signalStrengthToString(signalStrength); 
			Log.i(TAG, signal_str);
			
			String current = app.prettyDateString(new Date());
			String cell_str = cellToString();
			String result = current + "," + signal_str + 
					networkType + "," 
					+ cell_str;
		
			writeTextLine(result, streamCell);
			
			Log.i(TAG, "cell signal strength: " + result);		
			super.onSignalStrengthsChanged(signalStrength);
		}

		@Override
		public void onDataConnectionStateChanged(int state, int networkType) {
			TelephonyWriter.this.networkType = networkType;
			super.onDataConnectionStateChanged(state, networkType);
		}

	};
	
	private String cellToString(){
		if(tm == null) {
			tm = (TelephonyManager) app.getSystemService(Context.TELEPHONY_SERVICE);
		}
//		boolean isNull = false;
		if(tm.getCellLocation() == null)
		{
			Log.d(TAG, "cellLocation is null");
//			isNull = true;
		}
//		if(!isNull)
//			Log.i(TAG, tm.getCellLocation().toString());
		switch(phoneType){
		case TelephonyManager.PHONE_TYPE_CDMA:
			// cdma: "[mBaseStationId,mBaseStationLatitude,mBaseStationLongitude,mSystemId,mNetworkId]";
			try{
				CdmaCellLocation location = (CdmaCellLocation) tm.getCellLocation();
	//			return "bsId: " + location.getBaseStationId()
	//					+ ", bsLat: " + location.getBaseStationLatitude()
	//					+ ", bsLon: " + location.getBaseStationLongitude()
	//					+ ", sysId: " + location.getSystemId()
	//					+ ", netId: " + location.getSystemId();
				return location.getBaseStationId()
				+ "," + location.getBaseStationLatitude()
				+ "," + location.getBaseStationLongitude()
				+ "," + location.getSystemId()
				+ "," + location.getNetworkId();
			} catch (Exception e){
				return "0,0,0,0,0";
			}

		case TelephonyManager.PHONE_TYPE_GSM:
			try{
				GsmCellLocation gsmLocation = (GsmCellLocation) tm.getCellLocation();
				return gsmLocation.getLac() 
						+ "," + gsmLocation.getCid();
			}catch(Exception e){
				return "0,0";
			}
//			return "lac: " + gsmLocation.getLac() 
//					+ ", cid: " + gsmLocation.getCid();
		}
		return "";
	}

}
