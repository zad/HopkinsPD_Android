package edu.jhu.hopkinspd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import edu.jhu.hopkinspd.test.TestPrepActivity;

public class SelectTestActivity extends Activity{

	private GlobalApp app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		app = GlobalApp.getApp();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_test);
		
	}
	
	public void singleTest(View v){
		String[] testArray = getResources().getStringArray(R.array.active_tests);    

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Make your selection");
        builder.setItems(testArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection
            	Intent takeTests = new Intent(app, TestPrepActivity.class);
            	TestPrepActivity.singleTest = true;
        		// Jump to specific test
        		takeTests.putExtra("TestNumber", item);
        		app.initActiveTests();
        		startActivity(takeTests);
        		finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

	}
	
	public void allTests(View v){
		Intent takeTests = new Intent(app, TestPrepActivity.class);

		// Jump to specific test
//		takeTests.putExtra("TestNumber", GlobalApp.TEST_REACTION);
		app.initActiveTests();
		startActivity(takeTests);
		finish();
	}

}
