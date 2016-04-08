package edu.jhu.hopkinspd.test;

import java.io.File;
import java.util.Date;

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.*;
import android.widget.*;
import android.os.AsyncTask;

public class TestEndActivity extends Activity
{
	private static final String TAG = GlobalApp.TAG + "|TestEndActivity";
	private GlobalApp app;
	private long lastBackClick;
	private ProgressDialog pd;
	Button finish;
	private TextView ins;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		app = (GlobalApp) getApplication();
		setContentView(R.layout.endpage);
		ins = (TextView)findViewById(R.id.text_ins);
		

		finish = (Button)findViewById(R.id.button_finish);
		finish.setEnabled(false);
		finish.setVisibility(View.INVISIBLE);
		finish.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				finish();
			}
		});


		LongOperation task = new LongOperation();
		task.execute();
		
	}
	

	
	class LongOperation extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(TestEndActivity.this);
			pd.setTitle("Processing...");
			pd.setMessage("Please wait.");
			pd.setCancelable(false);
			pd.setIndeterminate(true);
			pd.show();
		}

		@Override
		protected Void doInBackground(Void... voids)
		{
			// Zip up all existing data files
			if(!app.isInTestDemo())
			{
				String zipFilename = app.getTestZipPackageFilename(new Date());
				app.zipAllDataFiles(zipFilename);
				app.encryptTestUploadZipFile(zipFilename);
					
			}
			

			return null;
		}

		@Override
		protected void onPostExecute(Void voids)
		{
			TextView ins = (TextView)findViewById(R.id.text_ins);
			ins.setText(
					Html.fromHtml(
							//"<b>Tests successfully completed!</b> Press the button below to exit."
							getString(R.string.ins_finish)
							));
			
			Button finish = (Button)findViewById(R.id.button_finish);
			finish.setEnabled(true);
			finish.setVisibility(View.VISIBLE);
			if (pd!=null) 
				pd.dismiss();
		}
	}

	@Override
	public void onBackPressed()
	{
		long current = System.currentTimeMillis();
		if(current - lastBackClick > 15*1000 ){
			lastBackClick = current;
			Toast.makeText(app, "click again to exit current test", Toast.LENGTH_LONG).show();
		}else{
			super.onBackPressed();
		}
	}    
}
