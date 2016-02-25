package edu.jhu.hopkinspd.utils;

import com.google.android.gms.location.DetectedActivity;

public class ContextUtil {
	/**
	* When supplied with the integer representation of the activity returns the activity as friendly string
	* @param type the DetectedActivity.getType()
	* @return a friendly string of the
	*/
	public static String getFriendlyName(int detected_activity_type){
		switch (detected_activity_type ) {
		case DetectedActivity.IN_VEHICLE:
			return "in vehicle";
		case DetectedActivity.ON_BICYCLE:
			return "on bike";
		case DetectedActivity.ON_FOOT:
			return "on foot";
		case DetectedActivity.TILTING:
			return "tilting";
		case DetectedActivity.STILL:
			return "still";
		default:
			return "unknown";
		}
	}
}
