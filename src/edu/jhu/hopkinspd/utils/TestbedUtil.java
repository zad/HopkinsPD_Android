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
package edu.jhu.hopkinspd.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

import edu.jhu.hopkinspd.GlobalApp;

public class TestbedUtil {
	private static final String TAG = GlobalApp.TAG + "|" + "TestbedUtil";
	private static TestbedUtil testbed;
	private String server_ip = "192.168.1.122";
	private int server_port = 5001;
	private OutputStream output;
	private boolean connected;
	
	
	public static TestbedUtil getInstance(){
		if(testbed == null)
			testbed = new TestbedUtil();
		return testbed;
	}
	
	public boolean connect(){
		try {
			InetAddress serverAddr = InetAddress.getByName(server_ip);
			Log.v(TAG, "C: Connecting..." + serverAddr.getHostAddress() + " " + server_port);
			
			Socket socket = new Socket(serverAddr, server_port);
			
			Log.v(TAG, "C: Connected: " + serverAddr.getHostAddress() + " " + server_port);
		    
			output = socket.getOutputStream();
			connected = true;
		} catch (UnknownHostException e) {
			Log.e(TAG, e.getLocalizedMessage());
			connected = false;
		} catch (IOException e) {
			Log.e(TAG, e.getLocalizedMessage());
			connected = false;
		}
		return connected;
	}
	
	public void disconnect(){
		if(output !=  null)
		{
			try {
				output.close();
			} catch (IOException e) {
				Log.e(TAG, e.getLocalizedMessage());
			}
		}
	}
	
	public void sendBytes(byte[] bytes, int len){
		if(output != null){
			Log.i(TAG, "sendBytes " + len);
			try {
				output.write(bytes);
				output.flush();
			} catch (IOException e) {
				Log.e(TAG, e.getLocalizedMessage());
				output = null;
			}
		}
	}

	public void sendBytes(byte[] bytes) {
		sendBytes(bytes, bytes.length);
	}
	
}
