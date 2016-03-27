package edu.jhu.hopkinspd;



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.SecretKey;



import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

















import edu.jhu.hopkinspd.security.AESCrypt;
import edu.jhu.hopkinspd.task.NTPSyncTask;
import edu.jhu.hopkinspd.utils.CrashReportSender;



import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StatFs;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

@ReportsCrashes(formKey = "", // will not be used
				formUri = ServerConfig.crashLog_url,
				mode = ReportingInteractionMode.TOAST,
				resToastText = R.string.crash_toast_text)
public class GlobalApp extends Application{
	private static final String CONFIG_FILE = "config.txt";
	public static boolean FIXED_CONFIG_USERID = false;
	public static boolean FIXED_CONFIG_USERPASSWORD = false;
	/**
	 * password to encrypt user password
	 */
	private static final String PASSWORD = "hopkinspd";
	private Encryptor encryptor;
	abstract class Encryptor {
        SecretKey key;

        abstract SecretKey deriveKey(String passpword, byte[] salt);

        abstract String encrypt(String plaintext, String password);

        abstract String decrypt(String ciphertext, String password);

        String getRawKey() {
            if (key == null) {
                return null;
            }

            return Crypto.toHex(key.getEncoded());
        }
    }
	
	private final Encryptor PADDING_ENCRYPTOR = new Encryptor() {

        @Override
        public SecretKey deriveKey(String password, byte[] salt) {
            return Crypto.deriveKeyPad(password);
        }

        @Override
        public String encrypt(String plaintext, String password) {
            key = deriveKey(password, null);
            Log.d(TAG, "Generated key: " + getRawKey());

            return Crypto.encrypt(plaintext, key, null);
        }

        @Override
        public String decrypt(String ciphertext, String password) {
            SecretKey key = deriveKey(password, null);

            return Crypto.decryptNoSalt(ciphertext, key);
        }
    };
    
    public static final String USERID_REX = "^[a-zA-Z0-9]+$";
	/**
	 * Preference keys
	 */
	public static final String PREF_KEY_VERISON = "versionPref";
	public static final String PREF_KEY_USERID = "userIDPref";
	
	public static final String PREF_KEY_ENCRYPT_KEY = "userPasswordPref";
	public static final String PREF_KEY_ROOT_PATH = "rootPathPref";
	public static final String PREF_KEY_UNIQUE_PHONE_ID = "uniquePhoneID";
	public static final String PREF_KEY_SWITCH = "switchPref";
	public static final String PREF_KEY_PROJECT = "projectPref";
	public static final String PREF_KEY_CONFIG = "configPref";
	// Battery 
	public static final String PREF_KEY_BATTERY_MIN_LEVEL = "batteryThresholdPref";

	// Network
	public static final String PREF_KEY_USE_MOBILE_INTERNET = "useMobileInternetPref";
	public static final String PREF_KEY_CHARGE_ONLY_INTERNET = "chargeOnlyInternetPref";
	// Data privacy
	public static final String PREF_KEY_RAW_GPS_ENABLED = "allowGPSRawDataPref";
	public static final String PREF_KEY_RAW_AUDIO_ENABLED = "allowAudioRawDataPref";
	
	// Timing 
	public static final String PREF_KEY_RECORD_LAST_START = "lastStartPref";
	public static final String PREF_KEY_RECORD_LAST_STOP = "lastStopPref";
	public static final String PREF_KEY_RECORD_LAST_RESTART = "lastRestartPref";
	
	
	
	// previous screen brightness parameters
	public static final String SCREEN_KEY_BRIGHTNESS = "screen_brightness";
	public static final String SCREEN_KEY_TIMEOUT = "screen_timeout";
	public static final String SCREEN_KEY_BRIGHTNESS_MODE = "screen_brightnessmode";
	
	public static final int WAKE_LOCK_AUTO = 515151;
	/**
	 * System values synchronized with the server
	 */
	// MainService
	public static boolean SYS_VALUE_RECORDING_WHEN_CHARGING = true;
	// ZipService
	public static int SYS_VALUE_ZIPTASK_INTERVAL_MILLSECS = 60000;
	
	// Data configuration
//	public static boolean SYS_VALUE_RAW_STREAM_ENABLED = true;
	
	
	/**
	 * motion labels
	 */
	public static String[] MOTIONS = {"STILL", "WALK", "BIKE", "CAR", "BUS", "TRAIN", "METRO"};
	
	/**
	 * Stream Writers' Settings
	 */
	
	public static final String PREFIX = "HopkinsPD";
	
	/**
	 * Stream Control States
	 */
	public static final int STREAM_INIT = 0;
	public static final int STREAM_START = 1;
	public static final int STREAM_RESTART = 2;
	public static final int STREAM_STOP = 3;
	public static final int STREAM_DESTROY = 4;
	public static final int STREAM_REGISTER = 5;
	
	/**
	 * broadcastReceiver actions
	 */
	public static final String SERVICE_MSG_ACTION = "edu.jhu.hopkinspd.SERVICE_MESSAGE";
	public static final String SERVICE_NOTIFICATION = "edu.jhu.hopkinspd.SERVICE_NOTIFICATION";
	public static final String ZIPPER_DONE_ACTION = "edu.jhu.hopkinspd.ZIPPER_DONE_ACTION";
	public static final String ZIPPER_START_ACTION = "edu.jhu.hopkinspd.ZIPPER_START_ACTION";
	
	/**
	 * Log File Settings
	 */
    public static final String LOG_FILE_PREFIX = "log";
	public static final String LOG_FILE_NAME_UI = "ui";
	public static final String LOG_FILE_NAME_MAIN = "main"; 
	public static final String LOG_FILE_NAME_ZIP = "zip";
	public static final String LOG_FILE_NAME_UPLOAD = "upload";
	public static final String LOG_FILE_NAME_ACC = "acc";
	public static final String LOG_FILE_NAME_BOOT = "reboot";
	public static final String LOG_FILE_NAME_WATCHDOG = "watchdog";
	public static final String LOG_FILE_NAME_NTP = "ntp";
	public static final String LOG_FILE_NAME_MOTION = "motion";
	public static final String LOG_FILE_NAME_CRASH = "crash";
	/**
	 * Stream extensions
	 */
	public static final String STREAM_EXTENSION_BIN = "bin";
	public static final String STREAM_EXTENSION_RAW = "raw";
	public static final String STREAM_EXTENSION_CSV = "csv";
	
	public static String RAW_DATA_EXTENSION = "raw";
	public static String TXT_DATA_EXTENSION = "csv";
	public static String LOG_FILE_EXTENSION = "log";
	public static String ZIP_FILE_EXTENSION = "zip";
	public static String AES_FILE_EXTENSION = "aes";
	
	public static final int OUTPUT_FORMAT_TXT = 0;
	public static final int OUTPUT_FORMAT_FLOAT = 2;
	public static final int OUTPUT_FORMAT_DOUBLE = 3;
	
	/**
	 * Storage constants
	 */
	public static String UPLOAD_SUBDIR = "upload";
	public static String STATS_SUBDIR = "stats";
	public static String STREAMS_SUBDIR = "streams";
	public static String LOGS_SUBDIR = "logs";
	
	private static final int AES_FILE_VERSION = 1;
	
	/**
	 * Debug
	 */
	public static final String TAG = "HopkinsPD";
	
	/**
	 * Server Information
	 */
	public static final String uploadURL = ServerConfig.upload_url;
	public static final String LogURL = ServerConfig.crashLog_url;
	public static final String URL_APK_VERSION = ServerConfig.apkVersion_url;
	public static final String URL_APK = ServerConfig.apk_url;
	/**
	 * Context
	 */
	public static final String CONTEXT_ACTIVITY = "CONTEXT_ACTIVITY";
	public static final String CONTEXT_ACTIVITY_CONNECTION = "CONTEXT_ACTIVITY_CONNECT";
	
	
	
	/**
	 * Survey form
	 */
	public static final String FORM = "updrs.xml";
	public static final String FORM_SUBDIR = "form";
	public static final String FORM_PREFIX = "form";
	public static final String PREF_KEY_ALLOW_SCREEN_BLANK = "allow_screen_blank";
	/**
	 * Tests
	 */
//	private static DataOutputStream testStreamFile = null;
	public static double[][] streamBuffer = null;
	
//	public static final int TEST_VOICE = 0;
//	public static final int TEST_BALANCE = 1;
//	public static final int TEST_GAIT = 2;
//	public static final int TEST_DEXTERITY = 3;
//	public static final int TEST_REACTION = 4;
//	
//	public static final int TEST_REST_TREMOR = 5;
//	
//	public static final int TEST_POSTURAL_TREMOR = 6;
	

	
	
	
	//                                      Vo, Ba, Ga, Dx, Re, RT, PT
//	public static int[] preTestPauseDur  = {2,  5,  5,  2,  2, 5, 5};
//	public static int[] postTestPauseDur = {0,  0,  0,  2,  0, 0, 0};
//	public static int[] testCaptureDur   = {20, 20, 30, 20, 30, 45, 45};
//	public static int[] testCaptureDur   = {5, 5, 5, 5, 5};		// Short for tests
//	public static boolean[] preTestVibrate = {false, true, true, false, false, true, true};
//	public static boolean[] postTestVibrate = {false, true, true, false, false, true, true};
	
	public static int ZIP_BUFFER_SIZE = 65536;

	public static final int NUMBER_OF_TESTS = 7;
	public static int CHANGE_REACT_DUR = 1;
	public static int VIBRATE_DUR = 2;
	

	
	public static List<DataFileListItem> dataFilesList = null;
	public static class DataFileListItem
	{
		File f;
		boolean persist;
		public DataFileListItem(File f, boolean persist)
		{
			this.f = f;
			this.persist = persist;
		}
	}
	
	public static String MEDTRACKER_SUBDIR = "med";
	public static String TEST_UPLOAD_SUBDIR = "test";
	
	private PendingIntent uploadIntent;
	private AlarmManager am;
	private int brightness;
	private PendingIntent ntpIntent;
	private PendingIntent watchdogIntent;
	private Intent mainIntent;
	
	private static GlobalApp app;
	
	public static GlobalApp getApp(){
		return app;
	}
	
	// text size for survey system in small screens
	public static int QUESTION_FONT_SIZE = 18;
	public static int APPLICATION_FONT_SIZE = 15;
	public static int BUTTON_WIDTH = 100;
	public static int MARGIN_SIZE = 0;
	public static int ACTIVE_TESTS_FONT_SIZE = 30;
	@Override
	public void onCreate()
	{
		ACRA.init(this);
		CrashReportSender mySender = new CrashReportSender(CrashReportSender.Method.POST, CrashReportSender.Type.FORM, null);
		ACRA.getErrorReporter().setReportSender(mySender);
		super.onCreate();
		app = this;
		Log.v(TAG, "onCreate");
		
		setDefaultConfig();
		
		
		am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		
		getUniquePhoneID();
		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		@SuppressWarnings("deprecation")
		int width = display.getWidth();
		@SuppressWarnings("deprecation")
		int height = display.getHeight();
		Log.i(TAG, "screen size: " + width + " " + height);
		if(width + height > 1000){
			ACTIVE_TESTS_FONT_SIZE = 25;
			QUESTION_FONT_SIZE = 20;
			APPLICATION_FONT_SIZE = 17;
			BUTTON_WIDTH = 200;
			MARGIN_SIZE = 10;
		}else if(width + height < 600){
			ACTIVE_TESTS_FONT_SIZE = 22;
			Log.i(TAG, "small screen setting");
		}
		// initial upload service
		if(isUploadServiceOn())
        	startUploadService();
			
		else{
			String message = "uploadService is off";
			Log.v(TAG, message);
		}
		startWatchDogService();
		
		app.setStringPref(getString(R.string.language), Locale.getDefault().getLanguage());
	}
	
	/**
	 * set default config from config xml file after installation
	 */
	public void setDefaultConfig() {
		
		String currentVersion = getStringPref(PREF_KEY_VERISON);
		if(currentVersion.length()==0){
			// Initialize configuration at the 1st installation
			String versionName = getVersion();
			Log.i(TAG, "version updated from " +currentVersion+" to " + versionName);
			createDir();
			getUniquePhoneID();
			setStringPref(PREF_KEY_VERISON, versionName);
			initUserDefaultConfig();
			initConfigFile();
		}
		else{
			// app update
			String versionName = getVersion();
			Log.i(TAG, "version updated from " +currentVersion+" to " + versionName);
			setStringPref(PREF_KEY_VERISON, versionName);	
			updateConfig();
		}
	}

	
	
	public String getVersion(){
		try {
			String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			return versionName;
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getLocalizedMessage());
			return "unknown";
		}
	}


	public String getAPKUrl() {
		String url = URL_APK, usr = null, pwd = null;
		usr = app.getStringPref(GlobalApp.PREF_KEY_USERID);
		pwd = app.encrypt(
				app.getStringPref(GlobalApp.PREF_KEY_ENCRYPT_KEY));
		String project = app.getStringPref(GlobalApp.PREF_KEY_PROJECT, "default");
		String config = app.getStringPref(GlobalApp.PREF_KEY_CONFIG, "default");
//		AssetManager am = getAssets();
//		try{
//			InputStream inputStream = am.open(CONFIG_FILE);
//			BufferedReader br;
//			String         line;
//			br = new BufferedReader(new InputStreamReader(inputStream), 8192);
//			
//			while ((line = br.readLine()) != null) {
//			    // Deal with the line
//				String[] args = line.split(" ");
//				if(args[0].startsWith("#")){
//					continue;
//				}
//				else if(args[0].equals("PROJECT") && args.length == 2){
//					project = args[1];
//				}else if(args[0].equals("CONFIG") && args.length == 2){
//					config = args[1];
//				}
//			}
//
//			// Done with the file
//			br.close();
//			br = null;
//			inputStream = null;
//	
//		}catch (IOException e){
//		    //Logging exception
//			Log.e(TAG, e.getLocalizedMessage());
//		}
		if(usr != null){
			url += "/" +usr;
			if(pwd != null) url += "/" + pwd + "/" + project + "/" + config;
		}
		return url;
	}
	
	private void initConfigFile() {
		AssetManager am = getAssets();
		try{
			InputStream inputStream = am.open(CONFIG_FILE);
			BufferedReader br;
			String         line;
			br = new BufferedReader(new InputStreamReader(inputStream), 8192);
			FIXED_CONFIG_USERID = false;
			FIXED_CONFIG_USERPASSWORD = false;
			String project = "default";
			String config = "default";
			String userid = null;
			while ((line = br.readLine()) != null) {
			    // Read lines in the config file
				String[] args = line.split(" ");
				if(args[0].startsWith("#")){
					continue;
				}
				else if(args[0].equals("USERID") && args.length == 2){
					userid = args[1];
					setStringPref(GlobalApp.PREF_KEY_USERID, userid);
					FIXED_CONFIG_USERID = true;
				}else if(args[0].equals("USERPASSWORD") && args.length == 2){
					String userpwd = decrypt(args[1]);
					Log.d(TAG,"userpwd: " + userpwd);
					setStringPref(GlobalApp.PREF_KEY_ENCRYPT_KEY, userpwd);
					FIXED_CONFIG_USERPASSWORD = true;
				}else if(args[0].equals("PROJECT") && args.length == 2){
					project = args[1];
				}else if(args[0].equals("CONFIG") && args.length == 2){
					config = args[1];
					
				}
			}
			setStringPref(GlobalApp.PREF_KEY_PROJECT, project);
			// only need in older version (which don't have config parameter)
			if(config.equals("default") && userid != null){
				if(userid.contains("opdcsite")){
					config = "opdc_clinic";
				}else if(userid.contains("opdcuser")){
					config = "opdc_patient";
				}
			}
			
			
				
			reset(config, true);
			// Done with the file
			br.close();
			br = null;
			inputStream = null;
	
		}catch (IOException e){
		    //Logging exception
			Log.e(TAG, e.getLocalizedMessage());
		}
	}
	
	private void updateConfig(){
		String config = getStringPref(GlobalApp.PREF_KEY_CONFIG);
		if(config.length()>0){
			reset(config, false);
		}
	}
	
	private void initUserDefaultConfig(){
			reset("user_default", true);
	}
	
	public boolean reset(String setting, boolean update) {
		InputStream is = null;
		setStringPref(GlobalApp.PREF_KEY_CONFIG, setting);
		Log.i(TAG, "reset " + setting);
		try {
			is = app.getAssets().open("settings/"+setting+".xml");
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser xrp = factory.newPullParser();
			xrp.setInput(is, "UTF-8");
			int eventType = xrp.getEventType();
			while(eventType != XmlPullParser.END_DOCUMENT){
				if(eventType == XmlPullParser.START_TAG 
						&& xrp.getAttributeCount() == 1
						){
					String tag = xrp.getName();
					String name = xrp.getAttributeValue(0);
					String value = null;
					if(xrp.next() == XmlPullParser.TEXT)
						value = xrp.getText();
					
					if(tag.equals("string")){
						if(update)
						{	
							app.setStringPref(name, value);
							Log.i(TAG, tag +" "+ name + " " + value);
						}
					}else if(tag.equals("bool")){
						if(!update && name.equals("update") && Boolean.parseBoolean(value)){
							update = true;
							Log.i(TAG, "turn on update!");
						}
						else if(update && !name.equals("update"))
						{
							app.setBooleanPref(name, Boolean.parseBoolean(value));
							Log.i(TAG, tag +" "+ name + " " + value);
						}
					}
				}
				eventType = xrp.next();
			}
			return true;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		} catch (XmlPullParserException e) {
			Log.e(TAG, e.getMessage());
		} 
		return false;
	}

	private String decrypt(String userpwd) {
		
		encryptor = PADDING_ENCRYPTOR;
		return encryptor.decrypt(userpwd, PASSWORD);
	
	}
	
	public String encrypt(String userpwd) {
		
		encryptor = PADDING_ENCRYPTOR;
		return encryptor.encrypt(userpwd, PASSWORD);
	
	}

	public void getUniquePhoneID(){
		// Get unique phone hardware details
        TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String uniquePhoneID = Build.MANUFACTURER + " " + Build.PRODUCT + " " + tManager.getDeviceId();
        setStringPref(GlobalApp.PREF_KEY_UNIQUE_PHONE_ID, uniquePhoneID);
	}

	public void initActiveTests() {
		String rootPath = getStringPref(GlobalApp.PREF_KEY_ROOT_PATH);
		(new File(rootPath + "/" + TEST_UPLOAD_SUBDIR)).mkdir();
		
		// Initialize list of data files
		if(dataFilesList == null)
			dataFilesList = new ArrayList<DataFileListItem>();

	}
	
	public boolean isOnline() {
		 ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		 if(cm.getActiveNetworkInfo()==null)
			 return false;
		 return cm.getActiveNetworkInfo().isConnectedOrConnecting();

	}


	public  String getFormPath(){
		String rootPath = getStringPref(GlobalApp.PREF_KEY_ROOT_PATH);
		String formPath = rootPath + "/" + FORM_SUBDIR;
		String formName = formPath + "/" + FORM;
		return formName;
	}

	
	public void createSurveyForm() {
		AssetManager am = getAssets();
		String rootPath = getStringPref(GlobalApp.PREF_KEY_ROOT_PATH);
		String formPath = rootPath + "/" + FORM_SUBDIR;
		String formName = formPath + "/" + FORM;
		try{
			InputStream inputStream = am.open(FORM);
			File dir = new File(formPath);
			if(!dir.isDirectory())
				dir.mkdir();
			File form = new File(formName);
			OutputStream outputStream=new FileOutputStream(form);
			byte buffer[]=new byte[1024];
			int length=0;
			
			while((length=inputStream.read(buffer))>0) {
				outputStream.write(buffer,0,length);
			}
			outputStream.close();
		   	inputStream.close();
	
		}catch (IOException e){
		    //Logging exception
			Log.e(TAG, e.getLocalizedMessage());
		}
	}


	
	
	public String uploadFiles(String tag, File[] listZipFiles) {
		Log.v(tag,"uploadFiles");
		StringBuilder s = new StringBuilder();
		try{
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost(GlobalApp.uploadURL);
			MultipartEntity reqEntity = new MultipartEntity(
	                HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart(new FormBodyPart("UserID", 
					new StringBody(getStringPref(GlobalApp.PREF_KEY_USERID))));

			for(File file : listZipFiles){
				reqEntity.addPart("File", new FileBody(file));
				Log.v(tag,"uploading " + file.getName());
			}
			postRequest.setEntity(reqEntity);
			HttpResponse response = httpClient.execute(postRequest);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent(), "UTF-8"));
            String sResponse;
            
 
            while ((sResponse = reader.readLine()) != null) {
                s = s.append(sResponse);
            }
            Log.v(tag, "Response: " + s);
		}catch (Exception e){
			Log.e(tag, e.getMessage());
		}
		return s.toString();
	}
    
	
	public String getStringPref(String key, String defaultValue)
	{
		SharedPreferences preferences = getSharedPrefs();
		return preferences.getString(key, defaultValue);
	}
	
	private SharedPreferences getSharedPrefs()
	{
		return PreferenceManager.getDefaultSharedPreferences(this);
	}

	public void createDir() {
		String sdCardPath = Environment.getExternalStorageDirectory().toString();
        File subDir = new File(sdCardPath + "/" + getString(R.string.app_name));
        subDir.mkdir();
        File uploadDir = new File(sdCardPath + "/" + getString(R.string.app_name)
        		+ "/" + GlobalApp.UPLOAD_SUBDIR);
        uploadDir.mkdir();
        File statsDir = new File(sdCardPath + "/" + getString(R.string.app_name)
        		+ "/" + GlobalApp.STATS_SUBDIR);
        statsDir.mkdir();
        File streamsDir = new File(sdCardPath + "/" + getString(R.string.app_name)
        		+ "/" + GlobalApp.STREAMS_SUBDIR);
        streamsDir.mkdir();
        File medTrackerDir = new File(sdCardPath + "/" + getString(R.string.app_name)
        		+ "/" + GlobalApp.MEDTRACKER_SUBDIR);
        medTrackerDir.mkdir();
        
        File logsDir = new File(sdCardPath + "/" + getString(R.string.app_name)
        		+ "/" + GlobalApp.LOGS_SUBDIR);
        logsDir.mkdir();
        initActiveTests();
	}
	
	/**
	 * delete  files recursively (do not delete directories)
	 * @param fileOrDirectory
	 */
	public static void fileDeleteRecursive(File fileOrDirectory)
	{
	    if (fileOrDirectory.isDirectory())
	        for (File child : fileOrDirectory.listFiles())
	            fileDeleteRecursive(child);
	    else
	    	fileOrDirectory.delete();
	}
	
	public void clearData(){
		try{
			String sdCardPath = Environment.getExternalStorageDirectory().toString();
	        File subDir = new File(sdCardPath + "/" + getString(R.string.app_name));
	        fileDeleteRecursive(subDir);
//	        for(File f : subDir.listFiles()){
//	        	if(f!=null && f.isFile())
//	        		f.delete();
//	        }
//	        File uploadDir = new File(sdCardPath + "/" + getString(R.string.app_name)
//	        		+ "/" + GlobalApp.UPLOAD_SUBDIR);
//	        for(File f: uploadDir.listFiles())
//	        	if(f!=null && f.isFile())
//	        		f.delete();
//	        File testUploadDir = new File(sdCardPath + "/" + getString(R.string.app_name)
//	        		+ "/" + GlobalApp.TEST_UPLOAD_SUBDIR);
//	        
//	        for(File f: testUploadDir.listFiles())
//	        	if(f!=null && f.isFile())
//	        		f.delete();
//	        File formDir = new File(sdCardPath + "/" + getString(R.string.app_name)
//	        		+ "/" + GlobalApp.FORM_SUBDIR + "/instances");
//	        if(formDir.exists())
//		        for(File dir: formDir.listFiles())
//		        	if(dir!=null && dir.isDirectory())
//		        	{
//		        		for (File f: dir.listFiles()){
//		        			if(f != null && f.isFile())
//		        				f.delete();
//		        		}
//		        		dir.delete();
//		        	}
			
		}catch(NullPointerException ex){
			Log.i(TAG, "null pointer exception during clearing data");
			createDir();
		}

	}

	public boolean isUserInfoAvailable() {
		String userid = getStringPref(GlobalApp.PREF_KEY_USERID);
		String pwd = getStringPref(GlobalApp.PREF_KEY_ENCRYPT_KEY);
		Log.d(TAG, "userid = " + userid + " pwd = " + pwd);
		if(userid.equals("") || pwd.equals("") || userid.equals("default"))
		{
			
			return false;
		}
		return true;
	}
	
	public Date getLastModifiedDate() {
		String rootPath = getStringPref(GlobalApp.PREF_KEY_ROOT_PATH);
		String streamPath = rootPath + "/" + GlobalApp.STREAMS_SUBDIR;
		File streams = new File(streamPath);
		return new Date(streams.lastModified());
	}
	
	public BufferedWriter openLogTextFile(String logFileType)
    {
		String userID = getStringPref(GlobalApp.PREF_KEY_USERID);
		String rootPath = getStringPref(GlobalApp.PREF_KEY_ROOT_PATH);
		String pid = getStringPhoneID();
		if(pid == "")
			getUniquePhoneID();
//		String timestamp = timeString(new Date());
		String logTextFileName = rootPath + "/" + GlobalApp.LOGS_SUBDIR + "/" + PREFIX + "_" + LOG_FILE_PREFIX + "_"
				+ logFileType + "_" + userID + "_" + pid + ".log";
		BufferedWriter logTextStream = null;
		Log.i(TAG, "logFile:" + logTextFileName);
		File file = new File(logTextFileName);
		try
	    {
			if(!file.exists())
				file.createNewFile();
			logTextStream = new BufferedWriter(new FileWriter(file, true));
		}
	    catch (IOException e)
		{
			e.printStackTrace();
		}
		return logTextStream;
    }
	
	public String prettyDateString(Date time)
	{
		return DateFormat.format("yyyy/MM/dd kk:mm:ss", time).toString();
	}
	
	
	
    public void writeLogTextLine(BufferedWriter logTextStream,
    		String message, boolean toast)
    {
        try
        {
        	Date now = new Date();
        	String prettyDate = prettyDateString(now);
			logTextStream.write(prettyDate + ": " + message);
	        logTextStream.newLine();
	        logTextStream.flush();
	        if (toast)
	        {
	        	Toast.makeText(this, getString(R.string.app_name) + ": " + message, Toast.LENGTH_SHORT).show();
	        }
		}
        catch (IOException e)
		{
//			e.printStackTrace();
		}
    }
	
    public void setStringPref(String key, String value)
    {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	Editor ed = preferences.edit();
    	ed.putString(key, value);
    	ed.commit();
    }
    
    public String getStringPref(String key)
    {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	return preferences.getString(key, "");
    }
    
	private void clearPreferences() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Editor ed = preferences.edit();
		ed.clear();
		ed.commit();
	}
    
    public void setBooleanPref(String key, boolean value)
    {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	Editor ed = preferences.edit();
    	ed.putBoolean(key, value);
    	ed.commit();
    }
    
    public void setIntPref(String key, int value)
    {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	Editor ed = preferences.edit();
    	ed.putInt(key, value);
    	ed.commit();
    }

    public boolean getBooleanPref(String key)
    {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//    	Log.i("getBooleanPref", getStringPref(key));
    	return preferences.getBoolean(key, false);
    }
    
    public long getLongPref(String key)
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	return prefs.getLong(key, 0l);
    }
    
    public int getIntPref(String key)
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	return prefs.getInt(key, 0);
    }
    
    
    public Date getDatePref(String key)
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	return new Date(prefs.getLong(key, 0l));
    }
    
    public void setLongPref(String key, long value)
    {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	Editor ed = preferences.edit();
    	ed.putLong(key, value);
    	ed.commit();
    }
    
    public float getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float)level / (float)scale) * 100.0f; 
    }
    
	public String timeString(Date time)
	{
    	return DateFormat.format("yyyyMMdd_kkmmss", time).toString();
	}
    
	public boolean isBatteryOK(BufferedWriter logTextStream) {
		// Check is current battery level is higher than the threshold
		int batteryMinLevel = Integer.valueOf(getStringPref(GlobalApp.PREF_KEY_BATTERY_MIN_LEVEL));
		float level = getBatteryLevel();
		if( level > batteryMinLevel)
			return true;
		else{
			
			writeLogTextLine(logTextStream, getString(R.string.batteryLow) + ":" + level, false);
			return false;
		}
	}
	
	public boolean enableRecordingWhileCharging(){
		Log.d(TAG, getBooleanPref(getString(R.string.enableRecordingWhileCharging))+ " enable recording while charging");
		return getBooleanPref(getString(R.string.enableRecordingWhileCharging));
	}
	
	public boolean isBatteryCharging(BufferedWriter logTextStream) {
		Intent intent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return chargePlug != 0;

	}

	public boolean isZipServiceOn() {
		String key = getString(R.string.zipServiceOn);
		boolean zipServiceOn = getBooleanPref(key);
		return zipServiceOn;
	}

	public boolean isUploadServiceOn() {
		String key = getString(R.string.uploadServiceOn);
		return getBooleanPref(key);
	}

	public boolean isSDCardOK( BufferedWriter logTextStream, TextView promptText) {
		// Check SD card is available to write on
        String sdCardState = Environment.getExternalStorageState();
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() 
        		+ "/" + getString(R.string.app_name);
        
        // Store path in shared preferences accessible to all code
        setStringPref(GlobalApp.PREF_KEY_ROOT_PATH, rootPath);
        
        if(sdCardState.equals(Environment.MEDIA_MOUNTED)){
        	// Make output directory
	        String sdCardPath = Environment.getExternalStorageDirectory().toString();
	        File subDir = new File(sdCardPath + "/" + getString(R.string.app_name));
	        subDir.mkdir();
	        openLogTextFile(GlobalApp.LOG_FILE_NAME_UI);
	         
	        // Check available space
	        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
			@SuppressWarnings("deprecation")
			double sdAvailBytes = (double)stat.getAvailableBlocks() * (double)stat.getBlockSize();
			double sdAvailableGb = sdAvailBytes / 1073741824;
	        DecimalFormat df = new DecimalFormat("#.##");
	        if(logTextStream != null)
	        	writeLogTextLine(logTextStream, "SD card ready with " + df.format(sdAvailableGb) + "Gb free", false);
			return true;
        }else{
        	Toast.makeText(this, getString(R.string.app_name) + ": SD card not ready, prepare SD card and restart", Toast.LENGTH_SHORT).show();
	    	if(promptText != null)
	    		promptText.setText("SD card not ready\nPrepare card and restart.");
	    	return false;
        }
	}

	public void showUserSettings() {
		// Go to UserSettingActivity
		Intent i = new Intent(this, UserSettingActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}
	
	public void allocateStreamBuffer(int bufferLength, int entries)
	{
		streamBuffer = new double[bufferLength][entries];
	}
	
	public String getMedTrackerFilename(Date time)
	{
		Resources res = getResources();
		String userID = getStringPref(PREF_KEY_USERID, 
				res.getString(R.string.default_userID));
		String phoneID = getStringPhoneID();
		String rootPath = getStringPref(PREF_KEY_ROOT_PATH, "");
		return rootPath + "/" + TEST_UPLOAD_SUBDIR + "/" + PREFIX + "_med_" 
				+ userID + "_" + phoneID + "_" + timeString(time) + ".csv";
	}
	
//	public String getTestDataFilename(Date time, int testNumber, String type, 
//			String ext)
//	{
//		Resources res = getResources();
//		String userID = getStringPref(PREF_KEY_USERID, 
//				res.getString(R.string.default_userID));
//		String phoneID = getStringPhoneID();
//		String rootPath = getStringPref(PREF_KEY_ROOT_PATH, "");
//		return rootPath + "/" + TEST_UPLOAD_SUBDIR + "/" + PREFIX + "_test" 
//				+ testNumber + "_" + type + "_" + userID + "_" + phoneID + 
//			"_" + timeString(time) + "." + ext;
//	}
	

	public String getTestDataFilename(Date time, int testName, 
			String type, String ext)
	{
		Resources res = getResources();
		String userID = getStringPref(PREF_KEY_USERID, 
				res.getString(R.string.default_userID));
		String phoneID = getStringPhoneID();
		String rootPath = getStringPref(PREF_KEY_ROOT_PATH, "");
		return rootPath + "/" + TEST_UPLOAD_SUBDIR + "/" + PREFIX + "_" 
				+ getString(testName).replace('_', '-') + "_" 
				+ type + "_" + userID + "_" 
				+ phoneID +	"_" + timeString(time) + "." + ext;
	}	
	
	public String getTestZipPackageFilename(Date time)
	{
		Resources res = getResources();
		String userID = getStringPref(PREF_KEY_USERID, res.getString(R.string.default_userID));
		String phoneID = getStringPhoneID();
		String rootPath = getStringPref(PREF_KEY_ROOT_PATH, "");
		return rootPath + "/" + TEST_UPLOAD_SUBDIR + "/HopkinsPD_test_" + userID + "_" + phoneID + "_" + timeString(time) + "." + ZIP_FILE_EXTENSION;
	}
	
	public String getStringPhoneID() {
		return getStringPref(GlobalApp.PREF_KEY_UNIQUE_PHONE_ID).replaceAll(" ", "-")
				.replaceAll("_", "-");
	}

	public void zipAllDataFiles(String zipFilename)
	{
		try
		{
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(zipFilename);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

			byte data[] = new byte[ZIP_BUFFER_SIZE];
			for (DataFileListItem dfl: dataFilesList)
			{
				FileInputStream fi = new FileInputStream(dfl.f);
				String fileName = dfl.f.getAbsolutePath();
				origin = new BufferedInputStream(fi, ZIP_BUFFER_SIZE);
				ZipEntry entry = new ZipEntry(fileName.substring(fileName.lastIndexOf("/") + 1));
				out.putNextEntry(entry);
				int count;
				while ((count = origin.read(data, 0, ZIP_BUFFER_SIZE)) != -1)
				{
					out.write(data, 0, count);
				}
				out.closeEntry();
				origin.close();
			}
			out.flush();
			out.finish();
			out.close();
			
			clearAllDataFiles();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// Remove all non-persistent files
	public static void clearAllDataFiles()
	{
		for (Iterator<DataFileListItem> itr = dataFilesList.iterator(); itr.hasNext();)
        {
            DataFileListItem dfl = itr.next();  
            if (!dfl.persist)  
            {  
            	dfl.f.delete();
                itr.remove();  
            }
        }
	}
	
	public void encryptFile(String tag, String filename, String aesFilename)
	{
		try
		{
			Log.i(tag, "encrypting " + filename);
			
			
			AESCrypt aes = new AESCrypt(this);
			
			String pwd = getStringPref(PREF_KEY_ENCRYPT_KEY, "");
			if(pwd.equals(""))
				Log.e(tag, "user password is missing!");
			else
			{
				String aesStringKey = aes.generateAESStringKey(pwd);
				aes.encrypt(AES_FILE_VERSION, filename, aesFilename, aesStringKey);
				Log.i(tag, "encrypted " + aesFilename);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public String encryptTestUploadZipFile(String zipFilename)
	{
		String aesFilename = null;
		try
		{
			File srcZipFile = new File(zipFilename);
			String uploadPath = getStringPref(PREF_KEY_ROOT_PATH, "") + "/" + UPLOAD_SUBDIR;
			aesFilename = uploadPath + "/" + srcZipFile.getName() + "." + AES_FILE_EXTENSION;
			encryptFile("encryptTestUploadZipFile", zipFilename, aesFilename);
//			new File(zipFilename).delete();
//			AESCrypt aes = new AESCrypt(this);
//			String aesStringKey = getStringPref(PREF_KEY_ENCRYPT_KEY, "");
//			aes.encrypt(AES_FILE_VERSION, zipFilename, aesFilename, aesStringKey);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return aesFilename;
	}
	
	public DataOutputStream openTestStreamFile(String filename)
	{
		DataOutputStream testStreamFile = null;
		try
		{
			File newFile = new File(filename);
			testStreamFile = new DataOutputStream(new FileOutputStream(newFile));
			dataFilesList.add(new DataFileListItem(newFile, false));
		}
		catch (FileNotFoundException e)
		{
			Log.e(TAG, "FileNotFoundException");
		}
		return testStreamFile;
	}

	public void closeTestStreamFile(DataOutputStream testStreamFile)
	{
		if (testStreamFile != null)
		{
			try
			{
				testStreamFile.flush();
				testStreamFile.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void writeTestStreamFrames(DataOutputStream testStreamFile, int bufferCount, int outputFormat)
	{
		double item = 0;
		if (testStreamFile != null)
		{
			try
			{
				for (int j = 0; j < bufferCount; j ++)
				{
					for (int i = 0; i < streamBuffer[j].length; i ++)
					{
						item = streamBuffer[j][i];
						switch (outputFormat)
						{
						// Text strings in CSV format
						case OUTPUT_FORMAT_TXT:
							if (i < (streamBuffer[j].length - 1))
								testStreamFile.writeBytes(Double.toString(item) + ",");
							else
								testStreamFile.writeBytes(Double.toString(item));
							break;

							// Raw 64-bit, double big-endian format
						case OUTPUT_FORMAT_DOUBLE:
							testStreamFile.writeDouble(item);
							break;

							// Raw 32-bit, float big-endian format
						case OUTPUT_FORMAT_FLOAT:
							testStreamFile.writeFloat((float)item);
							break;
						}
					}

					// New line for CSV files
					if (outputFormat == OUTPUT_FORMAT_TXT)
					{
						testStreamFile.writeByte(10);
					}

				}

				testStreamFile.flush();
				
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public FileOutputStream openRawDataFile(String filename)
	{
		FileOutputStream os = null;
		try
		{
			File newFile = new File(filename);
			os = new FileOutputStream(newFile);
			GlobalApp.dataFilesList.add(new DataFileListItem(newFile, false));	// Raw data files are not persistent
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return os;
	}


	public int[] getActivityStats(int dateIdx) {
		int[] activities = new int[6];
		for(int i = 0;i < 6; i++)
			activities[i] = 0;
		String sdCardPath = Environment.getExternalStorageDirectory().toString();
        File subDir = new File(sdCardPath + "/" + getString(R.string.app_name) + 
        		"/" + STATS_SUBDIR);
        for(File f : subDir.listFiles()){
        	if(f!=null && f.isFile())
        	{
        		String[] items = f.getName().split("_");
        		try {
					Date date = new SimpleDateFormat("yyyyMMdd").parse(items[items.length-2]);
					Calendar cal = Calendar.getInstance();
				    cal.setTime(date);
				    int year = cal.get(Calendar.YEAR);
				    int month = cal.get(Calendar.MONTH);
				    int week = cal.get(Calendar.WEEK_OF_YEAR);
				    int day = cal.get(Calendar.DAY_OF_MONTH);
				    Calendar cal2 = Calendar.getInstance();
				    int thisyear = cal2.get(Calendar.YEAR);
				    int thismonth = cal2.get(Calendar.MONTH);
				    int thisweek = cal2.get(Calendar.WEEK_OF_YEAR);
				    int today = cal2.get(Calendar.DAY_OF_MONTH);
					int intervalSecs = Integer.parseInt(items[2]);
					if((dateIdx == 0 && year == thisyear && month == thismonth && day == today) 
							|| (dateIdx == 1 && year == thisyear && week == thisweek)
							|| (dateIdx == 2 &&  year == thisyear && month == thismonth)
							|| dateIdx == 3
							)
					{
						Log.i(TAG, "getActivityStats:" + items[items.length-2] + " " + intervalSecs);
						BufferedReader br = null;
						try {
							 
							br = new BufferedReader(new FileReader(f));
							String line;
							while ((line = br.readLine()) != null) {
					 
							        // use comma as separator
								String[] strs = line.split(",");
								Log.i(TAG, strs[1].trim());
								int type = Integer.parseInt(strs[1].trim());
								activities[type] += intervalSecs;
							}
					 
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							if (br != null) {
								try {
									br.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							
						}
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

        	}
        }
        return activities;
	}
	
	private void startWatchDogService() {
		Log.i(TAG, "start watch dog service");
		// create the pending intent
    	Intent intent = new Intent(this, WatchDogService.class);
		watchdogIntent = PendingIntent.getService(
				this, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		// setup alarm service to wake up and start service periodically		
		long intervalMillis =  Integer.parseInt(getStringPref(getString(R.string.zipInterval)));
		long currTime = System.currentTimeMillis();
		long triggerAtMillis = currTime/intervalMillis*intervalMillis + intervalMillis;
		
//		triggerAtMillis = System.currentTimeMillis();
//		intervalMillis = 120000;
		am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMillis, watchdogIntent);
		
		Log.i(TAG, "startWatchDogService. current " + currTime + " triggerAt " + triggerAtMillis 
				+ " interval "+ intervalMillis);
		
		Log.i(TAG, "start watch dog service " + intervalMillis);
	}

	
	public void startUploadService() {
		// create the pending intent
    	Intent intent = new Intent(this, SyncService.class);
		uploadIntent = PendingIntent.getService(
				this, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		// setup alarm service to wake up and start service periodically		
		long intervalMillis =  Integer.parseInt(getStringPref(getString(R.string.uploadInterval)));
//		intervalMillis = 30000;
		long currTime = System.currentTimeMillis();
		long triggerAtMillis = currTime/intervalMillis*intervalMillis + intervalMillis;
		
		boolean random = getBooleanPref(getString(R.string.uploadTimeRandom));
		if(random)
		{
			Random rand = new Random();
			int r =  rand.nextInt((int) intervalMillis);
			triggerAtMillis += r;
			Log.i(TAG, "random increment for triggerAtMills = " + r);
		}
		am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMillis, uploadIntent);
		Log.i(TAG, "startUploadService. current " + currTime + " triggerAt " + triggerAtMillis 
				+ " interval "+ intervalMillis);
		
		Log.i(TAG, "start upload service " + intervalMillis);
	}
	
	public void stopUploadService() {
		if(am != null && uploadIntent != null)
			am.cancel(uploadIntent);
	}

	public void saveScreenBirghtness(int prevScreenBrightness, int prevScreenTimeout) {
		Log.i(TAG,"save screen brightness " + prevScreenBrightness);
		setIntPref(SCREEN_KEY_BRIGHTNESS, prevScreenBrightness);
//		setIntPref(SCREEN_KEY_BRIGHTNESS_MODE, prevScreenBrightnessMode);
		setIntPref(SCREEN_KEY_TIMEOUT, prevScreenTimeout);
	}

	public int loadScreenBirghtness() {
		if(brightness < 20)
			return 20;
		return brightness;
	}

	public void startZip(Date begin, Date end)
	{
		Bundle params = new Bundle();
		String beginTime = timeString(begin);
		String endTime = timeString(end);
		params.putString("lastStartTimeStamp", beginTime);
		params.putString("lastStopTimeStamp", endTime);
		Intent zipperIntent = new Intent(this, ZipperService.class);
		zipperIntent.putExtras(params);
		startService(zipperIntent);
		String log = "startZip " + beginTime + " " + endTime;
		Log.v(TAG, log);
		
	}
	
//	public int getNumberOfRestTests(int curTestNum){
//		int rest = 0;
//		int test = curTestNum++;
//		while(test < GlobalApp.NUMBER_OF_TESTS){
//			switch(test){
//			case GlobalApp.TEST_VOICE:
//				if(app.getBooleanPref(getString(R.string.test_voice)))
//					rest++;
//				break;
//			case GlobalApp.TEST_BALANCE:
//				if(app.getBooleanPref(getString(R.string.test_balance)))
//					rest++;
//				break;
//			case GlobalApp.TEST_GAIT:
//				if(app.getBooleanPref(getString(R.string.test_gait)))
//					rest++;
//				break;
//			case GlobalApp.TEST_DEXTERITY:
//				if(app.getBooleanPref(getString(R.string.test_dexterity)))
//					rest++;
//				break;
//			case GlobalApp.TEST_REACTION:
//				if(app.getBooleanPref(getString(R.string.test_reaction)))
//					rest++;
//				break;
//			case GlobalApp.TEST_REST_TREMOR:
//				if(app.getBooleanPref(getString(R.string.test_rest_tremor)))
//					rest++;
//				break;
//			case GlobalApp.TEST_POSTURAL_TREMOR:
//				if(app.getBooleanPref(getString(R.string.test_postural_tremor)))
//					rest++;
//				break;
//			}
//			
//				
//			test ++;
//		}
//		return rest;
//	}

//	public int getNextTestNumber(int num) {
//		while(num < GlobalApp.NUMBER_OF_TESTS){
//			switch(num){
//			case GlobalApp.TEST_VOICE:
//				if(app.getBooleanPref(getString(R.string.test_voice)))
//					return num;
//				break;
//			case GlobalApp.TEST_BALANCE:
//				if(app.getBooleanPref(getString(R.string.test_balance)))
//					return num;
//				break;
//			case GlobalApp.TEST_GAIT:
//				if(app.getBooleanPref(getString(R.string.test_gait)))
//					return num;
//				break;
//			case GlobalApp.TEST_DEXTERITY:
//				if(app.getBooleanPref(getString(R.string.test_dexterity)))
//					return num;
//				break;
//			case GlobalApp.TEST_REACTION:
//				if(app.getBooleanPref(getString(R.string.test_reaction)))
//					return num;
//				break;
//			case GlobalApp.TEST_REST_TREMOR:
//				if(app.getBooleanPref(getString(R.string.test_rest_tremor)))
//					return num;
//				break;
//			case GlobalApp.TEST_POSTURAL_TREMOR:
//				if(app.getBooleanPref(getString(R.string.test_postural_tremor)))
//					return num;
//				break;
//			}
//			
//				
//			num ++;
//		}
//		return num;
//	}

	public void runLastUpload(Context ctx, BufferedWriter logTextStream) {
		app.writeLogTextLine(logTextStream, "run last upload", false);
		Intent in = new Intent(ctx, SyncService.class);
		startService(in);
	}


	


	public boolean isNTPSyncServiceOn() {
		return getBooleanPref(getString(R.string.ntpSyncOn));
	}

	
	
	public void startNTPSyncService() {
		// create the pending intent
    	Intent intent = new Intent(this, NTPService.class);
		ntpIntent = PendingIntent.getService(
				this, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		// setup alarm service to wake up and start service periodically		
		long intervalMillis =  Integer.parseInt(getStringPref(getString(R.string.ntpSyncInt)));
		long currTime = System.currentTimeMillis();
		long triggerAtMillis = currTime/intervalMillis*intervalMillis + intervalMillis;
		am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMillis, ntpIntent);
		Log.i(TAG, "startNTPSyncService. current " + currTime + " triggerAt " + triggerAtMillis
				+ " interval "+ intervalMillis);
		new NTPSyncTask().execute();
	}
	
	public void stopNTPSyncService() {
		if(am != null && ntpIntent != null)
			am.cancel(ntpIntent);
	}

	public void startMainService(boolean watchdog) {
		mainIntent = new Intent(app, MainService.class);
		mainIntent.putExtra("watchdog", watchdog);
		startService(mainIntent);
	}

	public void stopMainService() {
		if(mainIntent == null)
			mainIntent = new Intent(app, MainService.class);
		app.stopService(mainIntent);
	}

	public void closeLogTextStream(BufferedWriter logTextStream) {
		if(logTextStream != null){
			try {
				logTextStream.close();
			} catch (IOException e) {
				Log.e(TAG, e.getLocalizedMessage());
			}
		}
	}

	public void crashTest() {
		int i = Integer.parseInt("test");
		System.out.print(i);
	}


	
	 

	public boolean isInTestDemo(){
		return getBooleanPref(getString(R.string.test_demo));
	}

	public boolean haveConnectionType(int connectType, BufferedWriter logTextStream)
	{
		ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connMgr.getNetworkInfo(connectType);
		if (netInfo == null) 
		{
			writeLogTextLine(logTextStream, "netInfo is null", false);
			Log.d(TAG, "netinfo is null");
			return false;
		}else{
			boolean avail = netInfo.isAvailable();
			boolean conned = netInfo.isConnected();
			writeLogTextLine(logTextStream, "connectType:" + connectType + " avail:" + avail + " conned:" + conned, false);
			Log.d(TAG, "connectType:" + connectType + " avail:" + avail + " conned:" + conned );
			return avail && conned;
		}
		
	}

	public boolean isNextTestScreenOn() {
		return getBooleanPref(getString(R.string.next_button));
	}

	public void setLanguage(String lang) {
		Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
	}
}
