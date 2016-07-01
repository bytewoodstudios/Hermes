package com.bytewood.hermes.file.ftp;

import static org.junit.Assert.*;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.ftpserver.ftplet.FtpException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bytewood.filesystem.AbstractTestConnector;
import com.bytewood.test.FtpServerWrapper;

public class TestFtpConnector extends AbstractTestConnector {

	static FtpServerWrapper wrapper = new FtpServerWrapper();

	/**
	 * Before the actual test can start we need to fire up an ftp-server
	 * @throws FtpException 
	 */
	@BeforeClass
	public static void setupClass() throws FtpException {
		wrapper.start();
	}
	
	@Before
	public void setup() {
		FtpConnector ftp = new FtpConnector();
		ftp.setConnection(FtpServerWrapper.con);
		ftp.connect();
		assertTrue("Connecting to the Test FTP Failed",ftp.isConnected());
		super.conn = ftp;
		super.remoteRoot = "/";
		super.remotePathToADirectory = "/folder1";
		super.minimumFilesInRemoteDirectory = 2;
		super.remotePathToAFile = "/file1";
	}

	@After
	public void tearDown() {
		this.conn.disconnect();
	}

	@Test
	public void testUseCorrectFtpClient() {
		FtpConnector ftp = new FtpConnector();
		ftp.setFtpClient(null);
		assertNull(ftp.getFtpClient());
		
		ftp.setConnection(FtpServerWrapper.con);
		FTPClient expected = new FTPClient();
		ftp.setFtpClient(expected);
		ftp.connect();
		assertTrue("FtpConnector did not return the provided FTPClient after init", expected == ftp.getFtpClient());
	}
	
	/*
	 * test the Guard
	 */
	@Test
	public void testGuardNullPath() {
		super.thrown.expect(IllegalArgumentException.class);
		FtpConnector ftp = (FtpConnector) super.conn;
		//create new FtpConnection which is not initialised
		ftp.guard(null);
		fail("expected exception was not thrown");
	}

	@Test
	public void testGuardBlockUninitialised() {
		super.thrown.expect(UnsupportedOperationException.class);
		//create new FtpConnection which is not initialised
		FtpConnector ftp = new FtpConnector();
		ftp.guard("path/to/some/File");
		fail("expected exception was not thrown");
	}

	/*
	 * Test Getters and Setters
	 */
	@Test
	public void testGettersAndSetters() {
		FtpConnector ftp = new FtpConnector();
		ftp.setConnection(FtpServerWrapper.con);
		assertEquals(FtpServerWrapper.con, ftp.getConnection());
		
		//test for ftpClient
		FTPClient expected = new FTPClient();
		ftp.setFtpClient(expected);
		assertEquals(expected, ftp.getFtpClient());
		
		//test for connectRetry
		ftp.setMaxConnectionRetries(5);
		assertEquals(5, ftp.getMaxConnectionRetries());
		
	}
	
}
