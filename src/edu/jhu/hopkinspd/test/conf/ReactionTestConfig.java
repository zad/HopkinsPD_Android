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
import edu.jhu.hopkinspd.test.ReactCapture;
import edu.jhu.hopkinspd.test.TestActivity;

public class ReactionTestConfig extends TestConfig{
    public ReactionTestConfig(){
        test_name = R.string.test_reaction;
        pre_test_text = R.string.ins_reaction;
        pre_icon = R.drawable.reaction_test;
        test_text = R.string.dir_reaction;
        test_view = R.layout.testreactpage;
        test_disp_name = this.getDisplayName(test_name);
        testCaptureDur = 30;
        help_link = "https://youtu.be/Brz2yZp_O7M";
        audio_ins = R.raw.reaction_test;
    }
    
	private ReactCapture reactObj = null;
	private boolean reactButtonOn = false;
	
	private Button button;

	@Override
	public void runTest(final TestActivity activity, BufferedWriter logWriter) {
		reactObj = new ReactCapture(GlobalApp.getApp(), this);
		reactObj.startRecording();
	}
	@Override
	public void completeTest() {
		reactObj.stopRecording();
		reactObj.destroy();
	}
	@Override
	public void cancelTest() {
		
		if(reactObj != null)
		{
			reactObj.stopRecording();
			reactObj.destroy();
		}
	}
	@Override
	public void createTest(TestActivity activity) {
		button = (Button)activity.findViewById(R.id.button_react);
		button.setVisibility(View.INVISIBLE);
	}
	@Override
	public void dispatchTouchEvent(MotionEvent me) {
		if (reactObj != null)
		{
			reactObj.handleTouchEvent(me, reactButtonOn);
		}
	}
	
	public void onInTestTimerTick(TestActivity activity) {
		// Choose new random tap button status
		int reactButtonVisible = View.INVISIBLE;
		boolean random = Math.random() > 0.5;
		if (random != reactButtonOn)
		{
			reactButtonOn = random;
			button = (Button)activity.findViewById(R.id.button_react);
			if (reactButtonOn)
			{
				reactButtonVisible = View.VISIBLE;
			}
			button.setVisibility(reactButtonVisible);
			reactObj.handleTouchEvent(null, reactButtonOn);
		}
	}
	
}
