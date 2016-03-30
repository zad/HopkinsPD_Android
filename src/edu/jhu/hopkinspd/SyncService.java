package edu.jhu.hopkinspd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


 



import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class SyncService extends IntentService
{
	

	private static final String TAG = GlobalApp.TAG +  "|" + "SyncService";
	
	
	
	
	
	protected BufferedWriter logTextStream = null;
	protected String logFileName = "";
	protected GlobalApp app;





	private String inetType;
	
	public SyncService() {
		super(TAG);
		
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v(TAG,"onHandleIntent");
		app = (GlobalApp) getApplication();
		
		logFileName = GlobalApp.LOG_FILE_NAME_UPLOAD;
		
		// Open log file
		logTextStream = app.openLogTextFile(logFileName);
        app.writeLogTextLine(logTextStream, "uploadService started", false);
		
		Log.v(TAG, "run");
//		int lastZipListLength = -1;
		

		// If there are any session Zip files, email them, then delete them
        boolean haveInternet = false;
        
		// Check that we have the right kind of connection, if at all
        boolean wifi = app.haveConnectionType(ConnectivityManager.TYPE_WIFI, logTextStream);
        boolean mobile = app.haveConnectionType(ConnectivityManager.TYPE_MOBILE, logTextStream);
		
		if (wifi || (!wifi && mobile && app.getBooleanPref(GlobalApp.PREF_KEY_USE_MOBILE_INTERNET)))
		{
			// OK to upload any zipped sessions
			
			haveInternet = true;
		}
		else
		{
			haveInternet = false;
		    
		}
		
		// If we acquired or lost an acceptable Internet connection, notify user
		
		if (haveInternet)
		{
			if (wifi)
			{
				app.writeLogTextLine(logTextStream, "Using WiFi internet", false);
				inetType  = "WiFi";
			}
			else
			{
				inetType = "Mobile";
				app.writeLogTextLine(logTextStream, "Using mobile internet", false);
			}
			// run upload task
			new Thread(uploadTask).run();
			// run auto update if recording is not running, sdcard and battery is ok
			boolean enabled = app.getBooleanPref(getString(R.string.autoUpdateOn));
			boolean recording = app.getBooleanPref(GlobalApp.PREF_KEY_SWITCH);
			if(enabled && !recording && app.isSDCardOK(logTextStream, null) && app.isBatteryOK(logTextStream))
				runAutoUpdate();
		}
		else
		{
			app.writeLogTextLine(logTextStream, "No available internet connection", false);
			sendServiceMessage("Failed to upload: No available Internet connection.");
			
		}
		cleanOldFiles();
	}
	
	

		
	private void cleanOldFiles() {
        // TODO Auto-generated method stub
	 // clean files generated one week ago
        cleanOldFiles(GlobalApp.LOGS_SUBDIR, 7);
        cleanOldFiles(GlobalApp.STREAMS_SUBDIR, 7);
        cleanOldFiles(GlobalApp.TESTS_SUBDIR, 7);
    }

    public void runAutoUpdate() {
		String local_version = app.getVersion();
		String server_version = getLatestVersion(logTextStream);
		if(server_version != null)
		{
			Log.i(TAG, "version:" + local_version + " " + server_version);
			if(local_version.compareTo(server_version) == 0){
				app.writeLogTextLine(logTextStream, "Don't need to update. version:" + local_version, false);
//				sendServiceMessage("Don't need to update. version:" + local_version);
			}else{
				app.writeLogTextLine(logTextStream, "Downloading the latest version: " + server_version, false);
				sendServiceMessage("Downloading the latest version: " + server_version);
				
				installAPK();
			}
		}else{
			app.writeLogTextLine(logTextStream, "get server version error: null", false);
		}
	}
	
	public String getLatestVersion(BufferedWriter logTextStream) {
		
		HttpClient httpclient = new DefaultHttpClient();

	    // Prepare a request object
	    HttpGet httpget = new HttpGet(GlobalApp.URL_APK_VERSION); 

	    // Execute the request
	    HttpResponse response;
	    try {
	        response = httpclient.execute(httpget);
	        // Examine the response status
	        Log.i(TAG,response.getStatusLine().toString());

	        // Get hold of the response entity
	        HttpEntity entity = response.getEntity();
	        // If the response does not enclose an entity, there is no need
	        // to worry about connection release

	        if (entity != null) {

	            // A Simple JSON Response Read
	            InputStream instream = entity.getContent();
	            String result= convertStreamToString(instream).trim();
	            // now you have the string representation of the HTML request
	            instream.close();
	            return result;
	        }


	    } catch (Exception e) {
	    	Log.e(TAG, e.getLocalizedMessage());
	    	app.writeLogTextLine(logTextStream, e.getLocalizedMessage(), false);
	    }
		return null;
	}
	
	private static String convertStreamToString(InputStream is) {
		 /*
		  * To convert the InputStream to String we use the BufferedReader.readLine()
		  * method. We iterate until the BufferedReader return null which means
		  * there's no more data to read. Each line will appended to a StringBuilder
		  * and returned as String.
		  */
		 BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		 StringBuilder sb = new StringBuilder();

		 String line = null;
		 try {
			 while ((line = reader.readLine()) != null) {
				 sb.append(line + "\n");
			 }
		 } catch (IOException e) {
			 e.printStackTrace();
		 } finally {
			 try {
				 is.close();
			 } catch (IOException e) {
				 e.printStackTrace();
			 }
		 }
		 return sb.toString();
	 }

	private void installAPK() {
		
		try {
            URL url = new URL(app.getAPKUrl());
            Log.i(TAG, "apk url: " + url);
            URLConnection c = url.openConnection();
           
            String sdCardPath = Environment.getExternalStorageDirectory().toString();
            File subDir = new File(sdCardPath + "/" + getString(R.string.app_name));
            subDir.mkdir();
            File outputFile = new File(subDir, "hopkinspd.apk");
            FileOutputStream fos = new FileOutputStream(outputFile);

            InputStream is = c.getInputStream();

            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);
            }
            fos.close();
            is.close();//till here, it works fine - .apk is download to my sdcard in download file

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(
            		Uri.fromFile(outputFile),
            		"application/vnd.android.package-archive");
            
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
        } catch (IOException e) {
        	e.printStackTrace();
            Log.e(TAG, "Update error:" + e.getLocalizedMessage());
        }
	}
		
	
	
	private Runnable uploadTask = new Runnable(){

		@Override
		public void run() {
			
			// If a new session zip has been created, or uploaded, notify user
	    	File[] listAesFiles = listAllAESFiles();
	    	
	    	if (listAesFiles != null && listAesFiles.length > 0)
			{
	    		int zipListLength = listAesFiles.length; 
				app.writeLogTextLine(logTextStream, zipListLength + " session(s) now pending upload", false);
				
				// Upload files to the server
				String result = app.uploadFiles(TAG, listAesFiles);
				// Post processing: clean uploaded files
	            if (result.equals("Successful"))
	            {
	            	sendServiceMessage(zipListLength + " Session(s) successfully uploaded via " + inetType + " network.");
	            	for(File uploaded : listAesFiles){
	            		uploaded.delete();
	            		app.writeLogTextLine(logTextStream, "Upload succeeded: " + uploaded.getName(), false);
	            	}
		            
	            }
	            else
	            {
		        	app.writeLogTextLine(logTextStream, "Upload failed, " + result, false);
		        	if(result.length()>0)
		        		sendServiceMessage("Upload failed, " + result );
		        	else
		        		sendServiceMessage("Upload failed.");
	            }
				
			}
			else
			{
				app.writeLogTextLine(logTextStream, "No sessions pending upload", false);
				sendServiceMessage("No sessions pending upload.");
				return;
			}
		}
		
	};
 
	
	


	public File[] listAllAESFiles()
    {
		// get all zip files in upload folder
		File rootDir = new File(app.getStringPref(GlobalApp.PREF_KEY_ROOT_PATH) + "/" + GlobalApp.UPLOAD_SUBDIR);
		FilenameFilter fnFilter = new FilenameFilter()
		{
		    public boolean accept(File dir, String name)
		    {
		        return name.contains(".aes");
		    }
		};
		File[] files = rootDir.listFiles(fnFilter);
		return files;
//		// get all zip files in test folder
//		rootDir = new File(app.getStringPref(GlobalApp.PREF_KEY_ROOT_PATH) + "/" + GlobalApp.TEST_UPLOAD_SUBDIR);
//		File[] testFiles = rootDir.listFiles(fnFilter);
//		ArrayList<File> list = new ArrayList<File>(Arrays.asList(files));
//		list.addAll(Arrays.asList(testFiles));
//		File[] all = new File[files.length + testFiles.length];
//		list.toArray(all);
//		return all;
    }
	

	
	private void sendServiceMessage(String message)
	{
		Intent cast = new Intent(GlobalApp.SERVICE_MSG_ACTION);
		cast.putExtra("stringMsg", message);
		sendBroadcast(cast);
		app.writeLogTextLine(logTextStream, message, false);
	}

	public void cleanOldFiles(String subdir, int outOfDays){
	    Log.i(TAG, "clean old files in " + subdir);
        File dir = new File(app.getStringPref(GlobalApp.PREF_KEY_ROOT_PATH) 
                + "/" + subdir);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -outOfDays);
        Date outDay = cal.getTime();
        FilenameFilter fnFilter = new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".bin") 
                        | name.endsWith(".csv") 
                        | name.endsWith(".raw") 
                        | name.endsWith(".txt")
                        | name.endsWith(".zip")
                        | name.endsWith(".log");
            }
        };
        File[] files = dir.listFiles(fnFilter);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        for(File file: files){
            String fileName = file.getName();
            String[] items = fileName.split("_");
            if(items.length >= 2){
                String dayStr = items[items.length-2];
                try {
                    Date day = format.parse(dayStr);
                    if(day.before(outDay)){
                        file.delete();
                        Log.i(TAG, "clean old file:"+fileName);
                    }
                } catch (ParseException e) {
                    Log.e(TAG,  "cleanOldFiles parse exception");
                }
                
            }
        }
    }
	
}
