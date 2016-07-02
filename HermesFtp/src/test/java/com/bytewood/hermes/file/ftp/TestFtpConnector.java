package com.bytewood.hermes.file.ftp;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
		super.remotePathToAFile = "/file1";
	}

	@After
	public void tearDown() {
		this.conn.disconnect();
	}

	/** 
	 * Provides a guaranteed correct input stream by accessing files via the local file system.
	 * It is assumed that these files exist
	 * @throws  
	 */
	@Override
	protected InputStream getExpectedFileContent(String path) {
		File expectedFile = new File(FtpServerWrapper.ftpRoot.getAbsolutePath() + File.separator + path);
		assertTrue("It is assumed that files to be able to provide content",expectedFile.exists());
		assertTrue("It is assumed that provided paths are files",expectedFile.isFile());
		try {
			return new FileInputStream(expectedFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/** 
	 * Provides a guaranteed correct list of all folder contents by accessing the ftp folder via the local file system.
	 * It is assumed that the folder exist
	 * @throws  
	 */
	@Override
	protected Set<String> getExpectedFolderContent(String path) {
		File folder = new File(FtpServerWrapper.ftpRoot.getAbsolutePath() + File.separator + path);
		assertTrue("It is assumed that files to be able to provide content",folder.exists());
		assertTrue("It is assumed that files provided paths are folders",folder.isDirectory());
		
		File[] ls = folder.listFiles();
		Set<String> ret = new HashSet<String>(ls.length);
		for(File cur : ls) {
			String name = path + cur.getName();
			if (cur.isDirectory())
				name += File.separator;
			ret.add(name);
		}
		return ret;
	}
	
	/*
	 * Tests
	 */
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
