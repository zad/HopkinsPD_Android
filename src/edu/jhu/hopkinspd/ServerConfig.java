package edu.jhu.hopkinspd;

public class ServerConfig {
	// server_url needs to be the url of your hopkinspd server
	public static final String server_url = "http://mojo.cs.jhu.edu/tomcat/edu.jhu.pdserver/";
	public static final String rest_url = server_url + "rest/";
	public static final String upload_url = rest_url + "upload/multipleFiles";
	public static final String crashLog_url = rest_url + "upload/crashlog";
	public static final String apkVersion_url = rest_url + "apk/version";
	public static final String apk_url = rest_url + "apk";
}
