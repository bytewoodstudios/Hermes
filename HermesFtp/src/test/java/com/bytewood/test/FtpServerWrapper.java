package com.bytewood.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import com.bytewood.hermes.ftp.model.impl.FtpConnectionImpl;
import com.bytewood.hermes.model.FtpConnection;

/**
 * This is a simple convenience class for quickly creating an FTP server for test purposes.
 * In no circumstances should this be used for any procuction environment.
 * You have been warned ;-)
 *  <li> default user name is "user"
 *  <li> default password is "pass"
 *  <li> default ftp port is "31337"
 *  <br/><br/>
 * @author rainerkern
 * @see FtpServerWrapper#ftpUserName
 * @see FtpServerWrapper#ftpUserPass
 * @see FtpServerWrapper#ftpPort
 */
public class FtpServerWrapper {
	public static String ftpHost = "localhost";
	public static int 	 ftpPort = 31337;
	public static String ftpUserName = "user";
	public static String ftpUserPass = "pass";
	
	public static final FtpConnection con = new FtpConnectionImpl();
	static {
		con.setHost(ftpHost);
		con.setPort(ftpPort);
		con.setUserName(ftpUserName);
		con.setPassword(ftpUserPass);
	}
	
	private static final List<BaseUser> users = new ArrayList<BaseUser>();
	static {
		BaseUser user = new BaseUser();
		user.setName(ftpUserName);
		user.setPassword(ftpUserPass);
		users.add(user);
	}
	public static final File ftpRoot = new File("src/test/resources/ftpRoot");
	
	public void start() throws FtpException {
		if (ftpRoot.exists() == false || ftpRoot.isDirectory() == false)
			System.out.println(new RuntimeException("ftpRoot folder could not be found"));
		
		FtpServerFactory serverFactory = new FtpServerFactory();
		ListenerFactory listenerFactory = new ListenerFactory();
		PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
		FtpServer server = serverFactory.createServer();
		
		UserManager um = userManagerFactory.createUserManager();
		serverFactory.setUserManager( um );
		
		List<Authority> auths = Arrays.asList(new Authority[]{new WritePermission()});
		for (BaseUser cur : users) {
			String fullUserHome = ftpRoot.getAbsolutePath() +"/";
			cur.setAuthorities(auths);
			cur.setHomeDirectory(fullUserHome);
			um.save(cur);
		}

		listenerFactory.setPort(ftpPort);
        // replace the default listener
        serverFactory.addListener("default", listenerFactory.createListener());
        //give write access to every user
        //start new server
        server.start();
	}
}
