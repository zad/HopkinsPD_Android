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

import edu.jhu.hopkinspd.GlobalApp;
import edu.jhu.hopkinspd.R;
import edu.jhu.hopkinspd.test.AccelCapture;
import edu.jhu.hopkinspd.test.GyroCapture;
import edu.jhu.hopkinspd.test.TestActivity;

public class RestTremorTestConfig extends TestConfig{
    public RestTremorTestConfig(){
        test_name = R.string.test_rest_tremor;
        pre_test_text = R.string.ins_rest_tremor;
        test_text = R.string.dir_rest_tremor;
        
        audio_ins = R.raw.rest_tremor_test;
        generalRestTremorTestConfig();
        
          
        
    }
	
	public RestTremorTestConfig(String hand) {
        if(hand.compareTo("left")==0){
            test_name = R.string.test_rest_tremor_left;
            pre_test_text = R.string.ins_rest_tremor_left;
            test_text = R.string.dir_rest_tremor_left;
        }else if(hand.compareTo("right")==0){
            test_name = R.string.test_rest_tremor_right;
            pre_test_text = R.string.ins_rest_tremor_right;
            test_text = R.string.dir_rest_tremor_right;
        }
        generalRestTremorTestConfig();
    }
	
	private void generalRestTremorTestConfig(){
	    pre_icon = R.drawable.rest_tremor_test;
        test_view = R.layout.testpage;
        test_disp_name = this.getDisplayName(test_name);
        preTestPauseDur = 5;
        testCaptureDur = 45;
        preTestVibrate = true;
        postTestVibrate = true;   
        help_link = "https://youtu.be/cPd1Ct0xOCg";
	}

    private AccelCapture accelObj = null;
	private GyroCapture gyroObj = null;
	
	@Override
	public void runTest(TestActivity activity, BufferedWriter logWriter) {
		GlobalApp app = GlobalApp.getApp();
		accelObj = new AccelCapture(app, this);
		accelObj.startRecording(null);
		
		if(gyro_on){
			gyroObj = new GyroCapture(app, this);
			gyroObj.startRecording();
		}
	}
	@Override
	public void completeTest() {
		if(accelObj != null){
			accelObj.stopRecording();
			accelObj.destroy();	
		}
		if(gyro_on && gyroObj != null){
			gyroObj.stopRecording();
			gyroObj.destroy();
		}
	}
	@Override
	public void cancelTest() {
		if(accelObj != null){
			accelObj.stopRecording();
			accelObj.destroy();	
		}
		if(gyro_on && gyroObj != null){
			gyroObj.stopRecording();
			gyroObj.destroy();
		}
	}

}
