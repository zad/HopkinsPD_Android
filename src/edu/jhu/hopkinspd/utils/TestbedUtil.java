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
