package com.bytewood.hermes.model;

public interface FtpConnection extends FileSystemConnection {
	public static final String host = "host";
	public static final String port = "port";
	public static final String userName = "userName";
	public static final String password = "password";
	
	String getUserName();
	void setUserName(String arg);

	String getPassword();
	void setPassword(String arg);
	
	int getPort();
	void setPort(int arg);
	
	String getHost();
	void setHost(String arg);
}
