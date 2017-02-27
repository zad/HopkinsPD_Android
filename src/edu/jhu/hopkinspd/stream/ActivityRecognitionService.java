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
