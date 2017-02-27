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
package edu.jhu.hopkinspd.test.conf;

import java.io.BufferedWriter;

import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;
import edu.jhu.hopkinspd.test.TapCapture;
import edu.jhu.hopkinspd.test.TestActivity;

public class TapTestConfig extends TestConfig{
    public TapTestConfig(){
        test_name = R.string.test_dexterity;
        pre_test_text = R.string.ins_dexterity;
        test_text = R.string.dir_dexterity;
        generalTapTestConfig();
    }
	
	public TapTestConfig(String hand) {
        if(hand.compareTo("left")==0){
	        test_name = R.string.test_dexterity_left;
	        pre_test_text = R.string.ins_dexterity_left;
	        test_text = R.string.dir_dexterity_left;
	        
	    }else if(hand.compareTo("right")==0){
	        test_name = R.string.test_dexterity_right;
	        pre_test_text = R.string.ins_dexterity_right;
	        test_text = R.string.dir_dexterity_right;
	        
        }
        generalTapTestConfig();
    }
	
	private void generalTapTestConfig(){
	    pre_icon = R.drawable.tap_test;
        test_view = R.layout.testtappage;
        test_disp_name = this.getDisplayName(test_name);
        postTestPauseDur = 2;
        help_link = "https://youtu.be/tJLqvKHn2XQ";
        audio_ins = R.raw.dexterity_test;
	}

    private TapCapture tapObj = null;
	private Button button1 = null, button2 = null;
	@Override
	public void runTest(TestActivity activity,BufferedWriter logWriter) {
		button1.setVisibility(View.VISIBLE);
		button2.setVisibility(View.VISIBLE);
		tapObj = new TapCapture(GlobalApp.getApp(), this);
		tapObj.startRecording();
	}
	@Override
	public void completeTest() {
		button1.setVisibility(View.INVISIBLE);
		button2.setVisibility(View.INVISIBLE);
		tapObj.stopRecording();
		tapObj.destroy();
	}
	@Override
	public void cancelTest() {
		button1.setVisibility(View.INVISIBLE);
		button2.setVisibility(View.INVISIBLE);
		if (tapObj != null){
			tapObj.stopRecording();
			tapObj.destroy();
		}
	}
	@Override
	public void createTest(TestActivity activity) {
		button1 = (Button)activity.findViewById(R.id.tap1_button);
		button1.setVisibility(View.INVISIBLE);
		button2 = (Button)activity.findViewById(R.id.tap2_button);
		button2.setVisibility(View.INVISIBLE);
	}
	@Override
	public void dispatchTouchEvent(MotionEvent me) {
		if (tapObj != null)
		{
			tapObj.handleTouchEvent(me);
		}
	}
}
