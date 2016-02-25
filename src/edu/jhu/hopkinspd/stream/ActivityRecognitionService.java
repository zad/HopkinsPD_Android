package edu.jhu.hopkinspd.stream;

import com.google.android.gms.location.ActivityRecognitionResult;

import edu.jhu.hopkinspd.GlobalApp;

import edu.jhu.hopkinspd.utils.ContextUtil;


import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class ActivityRecognitionService extends IntentService{
	
	private static final String TAG = GlobalApp.TAG + "|ActivityRecognition";


	public ActivityRecognitionService() {
		super("ActivityRecognitionService");
	}

	


	/**
	* Google Play Services calls this once it has analyzed the sensor data
	*/
	@Override
	protected void onHandleIntent(Intent intent) {
		if (ActivityRecognitionResult.hasResult(intent)) {
			ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
			Log.d(TAG, "ActivityRecognitionResult: "+ ContextUtil.getFriendlyName(result.getMostProbableActivity().getType()));
			Log.d(TAG, result.toString());

			// <time><type:int><type:str><confidence>
			String[] items = new String[4];
			items[0] = Long.toString(result.getTime());
			int type = result.getMostProbableActivity().getType();
			items[1] = Integer.toString(type);
			items[2] = ContextUtil.getFriendlyName(type);
			items[3] = Integer.toString(result.getMostProbableActivity().getConfidence());
			// broadcast to MainActivity and MainService
			Intent i = new Intent("android.intent.action.MAIN").putExtra(GlobalApp.CONTEXT_ACTIVITY, items);
			this.sendBroadcast(i);
		}
	}
	
	
	

}
