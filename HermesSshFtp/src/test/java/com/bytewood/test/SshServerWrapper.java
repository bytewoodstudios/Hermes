package com.bytewood.test;

import java.io.File;
import java.io.IOException;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.scp.ScpCommandFactory;
import org.apache.sshd.server.session.ServerSession;

import com.bytewood.hermes.ftp.model.impl.SshConnectionImpl;
import com.bytewood.hermes.model.SshConnection;

/**
 * This is a simple convenience class for quickly creating an FTP server for test purposes.
 * In no circumstances should this be used for any procuction environment.
 * You have been warned ;-)
 *  <li> default user name is "user"
 *  <li> default password is "pass"
 *  <li> default port is "31337"
 *  <br/><br/>
 * @author rainerkern
 * @see FtpServerWrapper#ftpUserName
 * @see FtpServerWrapper#ftpUserPass
 * @see FtpServerWrapper#ftpPort
 */
public class SshServerWrapper {
	public static String host = "localhost";
	public static int 	 port = 31337;
	public static String userName = "user";
	public static String userPass = "pass";
	
	public static final SshConnection con = new SshConnectionImpl();
	static {
		con.setHost(host);
		con.setPort(port);
		con.setUserName(userName);
		con.setPassword(userPass);
	}

	private class MockAuthenticator implements PasswordAuthenticator {
		@Override
		public boolean authenticate(String user, String pass, ServerSession session) throws PasswordChangeRequiredException {
			if (SshServerWrapper.userName.equals(user) && SshServerWrapper.userPass.equals(pass))
				return true;
			//else
			return false;
		}
	}
	
	public static final File ftpRoot = new File("src/test/resources/ftpRoot");
	
	public static void main(String[] args) throws IOException {
		SshServerWrapper foo = new SshServerWrapper();
		foo.start();
		System.out.println("started");
	}

	private SshServer sshd;
	
	public void start() throws IOException {
		this.sshd = SshServer.setUpDefaultServer();
		this.sshd.setPort(port);
		this.sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
		this.sshd.setPasswordAuthenticator(new MockAuthenticator());
		this.sshd.setCommandFactory(new ScpCommandFactory());
		this.sshd.start();
	}
}
