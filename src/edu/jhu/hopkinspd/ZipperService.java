package edu.jhu.hopkinspd;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import edu.jhu.hopkinspd.stream.StreamWriter;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ZipperService extends IntentService
{
	private static final String TAG = GlobalApp.TAG + "|ZipperService";
	protected BufferedWriter logTextStream = null;
	protected String logFileName = "";
	protected GlobalApp app;
	
	public ZipperService(){
		super(TAG);
		
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		app = (GlobalApp) this.getApplication();
		logFileName = GlobalApp.LOG_FILE_NAME_ZIP;
		
		// Open log file
		logTextStream = app.openLogTextFile(logFileName);
		
        
        
		Bundle params = intent.getExtras();
		
		String userID = app.getStringPref(GlobalApp.PREF_KEY_USERID);
		String rootPath = app.getStringPref(GlobalApp.PREF_KEY_ROOT_PATH);

		// TODO sometimes lastStartTime >>> lastStopTime?
		String lastStartTimeStamp = params.getString("lastStartTimeStamp");
		String lastStopTimeStamp = params.getString("lastStopTimeStamp");
		
		app.writeLogTextLine(logTextStream, 
				"Zip service started: " + lastStartTimeStamp + ":" 
		+ lastStopTimeStamp, false);
		
		String pid = app.getStringPhoneID();
    	String sessionName = getString(R.string.app_name) + "_" + StreamWriter.STREAM_PREFIX + "_" + userID + "_" + pid + "_" + lastStartTimeStamp + "_" + lastStopTimeStamp;

    	File[] listFiles = listStreamAndLogFiles(lastStartTimeStamp);
    	if(listFiles != null && listFiles.length>0){
    		String zipFileName = rootPath + "/" + sessionName + ".zip";
        	zipFiles(listFiles, zipFileName);
        	app.writeLogTextLine(logTextStream, "encrypting zipped files", false);
        	String uploadPath = app.getStringPref(GlobalApp.PREF_KEY_ROOT_PATH, "") + "/" + GlobalApp.UPLOAD_SUBDIR;
    		String aesFilename = uploadPath + "/" + sessionName + ".zip." + GlobalApp.AES_FILE_EXTENSION;

        	app.encryptFile(TAG, zipFileName, aesFilename);
        	// XXX delete the zip file
    		new File(zipFileName).delete();
            
        	app.writeLogTextLine(logTextStream, "Zipping time stamp: " + lastStartTimeStamp, false);
    		for (File lf: listFiles)
    		{
    			app.writeLogTextLine(logTextStream, lf.getName(), false);
    		}
    		emptyLogFilesAndDeleteStreamFiles(listFiles);
    	}
    	
    	
		
		Intent cast = new Intent(GlobalApp.ZIPPER_DONE_ACTION);
		sendBroadcast(cast);
	}
	


	@Override
    public void onDestroy()
	{
		
	}
	
    public File[] listStreamFiles(String uniqueTimeStamp)
    {
		File rootDir = new File(app.getStringPref(GlobalApp.PREF_KEY_ROOT_PATH));
		final String uid = uniqueTimeStamp;
		FilenameFilter fnFilter = new FilenameFilter()
		{
		    public boolean accept(File dir, String name)
		    {
		        return name.contains(uid) &
		        		(name.contains(".bin") | name.contains(".csv") | name.contains(".raw") | name.contains(".txt"));
		    }
		};
		return rootDir.listFiles(fnFilter);
    }
    
    public File[] listStreamAndLogFiles(String uniqueTimeStamp)
    {
    	// list stream files
    	// list log files
		File streamDir = new File(app.getStringPref(GlobalApp.PREF_KEY_ROOT_PATH)+"/"+GlobalApp.STREAMS_SUBDIR);
		File logDir = new File(app.getStringPref(GlobalApp.PREF_KEY_ROOT_PATH)+"/"+GlobalApp.LOGS_SUBDIR);
		final String uid = uniqueTimeStamp;
		FilenameFilter fnFilter = new FilenameFilter()
		{
		    public boolean accept(File dir, String name)
		    {
		        return (name.contains(uid) &
		        		(name.contains(".bin") | name.contains(".csv") | name.contains(".raw") | name.contains(".txt")))
		        	  | name.contains(".log");
		    }
		};
		File[] streamFiles = streamDir.listFiles(fnFilter);
		File[] logFiles = logDir.listFiles(fnFilter);
		if (streamFiles != null && streamFiles.length > 0)
		{	
			ArrayList<File> list = new ArrayList<File>(Arrays.asList(streamFiles));
			list.addAll(Arrays.asList(logFiles));
			File[] all = new File[streamFiles.length + logFiles.length];
			list.toArray(all);
			return all;
		}
		return null;
    }
    
    
    
	private void emptyLogFilesAndDeleteStreamFiles(File[] listFiles) {
		
		for(File file : listFiles){
			if(file.getName().endsWith("log")){
				// Empty log files
				PrintWriter writer;
				try {
					writer = new PrintWriter(file);
					writer.print("");
					writer.close();
				} catch (FileNotFoundException e) {
					Log.e(TAG, e.getLocalizedMessage());
				}
				
			}else{
				// Delete stream files
				file.delete();
			}
		}
	}
    
	public File[] listAllZipFiles()
    {
		File rootDir = new File(app.getStringPref(GlobalApp.PREF_KEY_ROOT_PATH));
		FilenameFilter fnFilter = new FilenameFilter()
		{
		    public boolean accept(File dir, String name)
		    {
		        return name.contains(".zip");
		    }
		};
		return rootDir.listFiles(fnFilter);
    }
    
    public void zipFiles(File[] listFiles, String zipFileName)
    {
    	app.writeLogTextLine(logTextStream, "Zip files started", false);
    	final int bufferSize = 65536;
    	
    	try
    	{
    		BufferedInputStream origin = null;
    		FileOutputStream dest = new FileOutputStream(zipFileName);
    		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

    		byte data[] = new byte[bufferSize];

    		for(int i = 0; i < listFiles.length; i++)
    		{
    			FileInputStream fi = new FileInputStream(listFiles[i]);
    			String fileName = listFiles[i].getAbsolutePath();
    			origin = new BufferedInputStream(fi, bufferSize);
    			ZipEntry entry = new ZipEntry(fileName.substring(fileName.lastIndexOf("/") + 1));
    			out.putNextEntry(entry);
    			int count;
    			while ((count = origin.read(data, 0, bufferSize)) != -1)
    			{
    				out.write(data, 0, count);
    			}
    			out.closeEntry();
    			origin.close();
    		}
			out.flush();
			out.finish();
    		out.close();
    	}
    	catch (Exception e)
    	{
    		app.writeLogTextLine(logTextStream, "Zip service exception:" + e.getLocalizedMessage(), false);
    	}
    	app.writeLogTextLine(logTextStream, "Zip files end", false);
    }
    
//    public void deleteFiles(File[] list)
//	{
//		for (File f: list)
//			f.delete();
//	}
	
}
