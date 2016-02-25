package edu.jhu.hopkinspd;

import edu.jhu.hopkinspd.task.NTPSyncTask;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class NTPService extends IntentService{

	private static final String TAG = GlobalApp.TAG +  "|" + "NTPService";
	
	
	public NTPService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "start NTPService");
		new NTPSyncTask().execute();
	}


}
