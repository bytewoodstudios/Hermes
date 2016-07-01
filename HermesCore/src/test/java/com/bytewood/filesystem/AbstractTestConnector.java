package com.bytewood.filesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.bytewood.hermes.api.FileSystemConnector;
import com.bytewood.hermes.model.FileSystemConnection;

public abstract class AbstractTestConnector {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/* 
	 * #####
	 * These properties have to be filed by implementing subclasses
	 * ####
	 */
	protected FileSystemConnector<? extends FileSystemConnection> conn;
	protected String remoteRoot;
	protected String remotePathToADirectory;
	protected int	 minimumFilesInRemoteDirectory = -1;
	protected String remotePathToAFile;
	protected HashMap<String,String> expectedContent = new HashMap<String,String>();

	/*
	 * Test Subclass Setup
	 */
	@Test
	public void testSubclassSetup() {
		assertNotNull("remote root was not set by implementing subclass", remoteRoot);
		assertNotNull("remote test dirtectory was not set by implementing subclass", remotePathToADirectory);
		assertFalse("number of files in remote test directory was not set by implementing subclass", minimumFilesInRemoteDirectory <= -1);
		assertNotNull("remote test file was not set by implementing subclass", remotePathToAFile);
	}
	
	/*
	 * Connect and Disconnect Tests
	 */
	@Test
	public void testConnectAndDisconnect() {
		//connect first
		assertTrue( conn.connect() );
		//check for connection status
		assertTrue( conn.isConnected() );
		//then disconnect again
		assertTrue( conn.disconnect() );
		//check again for connection status
		assertFalse( conn.isConnected() );
	}


	@Test
	public void testFailConnectNullConnection() {
		thrown.expect(UnsupportedOperationException.class);
		this.conn.setConnection(null);
		this.conn.connect();
		fail("expected exception was not thrown");
	}
	
	@Test
	public void testFailConnectNullConnection2() {
		thrown.expect(IllegalArgumentException.class);
		this.conn.setConnection(null);
		this.conn.connect(null);
		fail("an expected exception was not thrown");
	}
	
	@Test
	public <T extends FileSystemConnection> void testConnectWithProvidedConnection() {
		//do some tricks with generics ;-)
		@SuppressWarnings("unchecked")
		FileSystemConnector<T> tmp = (FileSystemConnector<T>) this.conn;
		
		//cache the connection
		T cache = tmp.getConnection();
		this.conn.setConnection(null);
		assertNull(this.conn.getConnection());

		tmp.connect(cache);
		assertTrue(this.conn.isConnected());
	}
	
	/*
	 * Test exists, isFile and isFolder
	 */
	@Test
	public void testExists() throws NullPointerException, FileNotFoundException, IOException {
		//positive
		assertTrue( "exists() returned falseeven tough the directory should exist", this.conn.exists(remotePathToADirectory) );
		assertTrue( "exists() returned true even tough the file should exist", this.conn.exists(remotePathToAFile) );
		//negative
		assertFalse( "exists() returned true even tough the file should not exist", this.conn.exists("foo/bar/really/does/not/exist"));
	}
	
	@Test
	public void testIsFolder() throws NullPointerException, IOException {
		//positive
		assertTrue( "Did not find folder at: "+remotePathToADirectory, this.conn.isFolder(remotePathToADirectory) );
		//negative
		assertFalse( "Did find folder at:"+remotePathToADirectory, this.conn.isFolder(remotePathToAFile) );
	}
	
	@Test 
	public void testIsFile() throws NullPointerException, IOException {
		//positive
		assertTrue( this.conn.isFile(remotePathToAFile) );
		//negative
		assertFalse( this.conn.isFile(remotePathToADirectory) );
	}
	
	/*
	 * Test File and Directory Listings 
	 */
	
	@Test //positive
	public void testListDirectory() throws NullPointerException, IOException {
		List<String> files = this.conn.listDirectory(this.remoteRoot);
		assertTrue("Did not list a minimum of " + this.minimumFilesInRemoteDirectory+" files.", files.size() >= this.minimumFilesInRemoteDirectory);
	}
	
	@Test //negative
	public void testListDirectoryOnFile() throws IOException {
		thrown.expect(IOException.class);
		this.conn.listDirectory(this.remotePathToAFile);
		fail("no exception was raised");
	}
	
	@Test //positive
	public void testListAllFilesInDirectory() throws IOException {
		List<String>files = this.conn.listFilesInDirectory(this.remoteRoot);
		assertTrue("No Files found in remote directory, even tough there should be some.", files.size() > 0);
	}

	@Test //positive
	public void testListAllFoldersInDirectory() throws IOException {
		List<String>files = this.conn.listFoldersInDirectory(this.remoteRoot);
		assertTrue("No Files found in remote directory, even tough there should be some.", files.size() > 0);
	}
	
	
	/*
	 * Test downloading
	 */
	@Test //positive
	public void testDownloadSingleFile() throws IOException {
		InputStream is = this.conn.provideInputStream(remotePathToAFile);
		assertNotNull(is);
	}

	@Test //positive
	public void testDownloadAllFilesInExistingFolder() throws IOException {
		List<String> files = this.conn.listDirectory(this.remoteRoot);
		int counter = 0;
		for (String cur : files) {
			//check for folders
			if (this.conn.isFolder(this.remoteRoot + "/" + cur))
				continue;
			
			//download file
			InputStream in = this.conn.provideInputStream(this.remoteRoot+"/"+cur);
			@SuppressWarnings("resource")
			java.util.Scanner s = new java.util.Scanner(in).useDelimiter("\\A");
			String content =  s.hasNext() ? s.next() : "";
			
			//compare content with expected content
			String expected = this.expectedContent.get(cur);
			assertNotNull(content);
			assertNotNull(expected);
			assertEquals(expected, content);
			counter++;
		}
		assertTrue(counter > 0);
	}

	@Test
	public void testFailOnListNonExistingFolder() throws IOException {
		thrown.expect(IOException.class);
		this.conn.listDirectory("does/not/exist");
		fail("no exception was raised");
	}

	@Test
	public void testFailOnListNullFolder() throws IOException {
		thrown.expect(IllegalArgumentException.class);
		this.conn.listDirectory(null);
		fail("no exception was raised");
	}
	
	@Test
	public void testFailOnDownloadNonExistingFile() throws IOException {
		thrown.expect(FileNotFoundException.class);
		this.conn.provideInputStream("does/not/exist");
		fail("no exception was raised");
	}

	@Test
	public void testFailOnDownloadNullFile() throws IOException {
		thrown.expect(IllegalArgumentException.class);
		this.conn.provideInputStream(null);
		fail("no exception was raised");
	}
	
	@Test
	public void testFailOnDownloadWhileDisconnected() throws IOException {
		thrown.expect(UnsupportedOperationException.class);
		this.conn.disconnect();
		this.conn.provideInputStream("foo");
		fail("no exception was raised");
	}
	
	
	/*
	 * Utility Methods
	 */
	static String convertStreamToString(java.io.InputStream is) {
		@SuppressWarnings("resource")
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

}
