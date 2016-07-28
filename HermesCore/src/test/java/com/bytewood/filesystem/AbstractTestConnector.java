package com.bytewood.filesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.bytewood.hermes.api.FileSystemConnector;
import com.bytewood.hermes.model.FileSystemConnection;

public abstract class AbstractTestConnector extends LogConfigurationEnabled{
	private static Logger logger = Logger.getLogger(AbstractTestConnector.class.toString());
	/**
	 * This method is to be used by the implementing test class to provide a guaranteed correct stream of a file on the remote file system.
	 * This is not always possible in which case null can be returned to skip the comparison
	 * @param path
	 * @return null if no stream can be provided at all
	 * @return a stream of the expected content of a remote file
	 */
	protected abstract InputStream getExpectedFileContent(String path);

	/**
	 * This method provides the capability of checking that all contents were provided by the remote file system to the unit test.
	 * It is expected that this method returns fully qualified file and directory paths relative to the remote file system root.<br>
	 * example
	 * <li> /folder1/folder2/file1
	 * <li> /folder1/folder2/file2
	 * <li> ...
	 * 
	 * @param path full remote path to the folder under test. E.g. "/folder1/folder2"
	 * @return a set of all fully qualified file- and folder-names in the given directory
	 */
	protected abstract Set<String> getExpectedFolderContent(String path);
	
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
	protected String remotePathToAFile;

	/*
	 * Test Subclass Setup
	 */
	@Test
	public void testSubclassSetup() {
		assertNotNull("remote root was not set by implementing subclass", remoteRoot);
		assertNotNull("remote test dirtectory was not set by implementing subclass", remotePathToADirectory);
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
		List<String> ls = this.conn.listDirectory(this.remoteRoot);
		int actual = ls.size();
		int expected = this.getExpectedFolderContent(this.remoteRoot).size();
		assertEquals("Expected "+expected+" number of files but found "+actual+" in remote directory", expected, actual);
		for (String cur : ls) {
			assertNotNull(cur);
			assertTrue("paths have to start with \"/\"", cur.startsWith(File.separator));
		}
	}
	
	@Test //negative
	public void testListDirectoryOnFile() throws IOException {
		thrown.expect(IOException.class);
		this.conn.listDirectory(this.remotePathToAFile);
		fail("no exception was raised");
	}
	
	@Test //positive
	public void testListAllFilesInDirectory() throws IOException {
		List<String> ls = this.conn.listFilesInDirectory(this.remoteRoot);
		assertTrue("No Files found in remote directory, even tough there should be some.", ls.size() > 0);
		for (String cur : ls) {
			assertNotNull(cur);
			assertTrue("paths have to start with \"/\"", cur.startsWith(File.separator));
			assertTrue("file paths must not end with \"/\"", cur.endsWith(File.separator) == false);
		}
	}

	@Test //positive
	public void testListAllFoldersInDirectory() throws IOException {
		List<String>ls = this.conn.listFoldersInDirectory(this.remoteRoot);
		assertTrue("No Folders found in remote directory, even tough there should be some.", ls.size() > 0);
		for (String cur : ls) {
			assertNotNull(cur);
			assertTrue("paths have to start with \"/\"", cur.startsWith(File.separator));
			assertTrue("folder paths have to end with \"/\"", cur.endsWith(File.separator));
		}
	}
	
	
	/*
	 * Test downloading
	 */
	@Test //positive
	public void testDownloadSingleFile() throws IOException {
		InputStream is = this.conn.receive(remotePathToAFile);
		assertNotNull(is);
	}

	@Test //positive
	public void testDownloadAllFilesInExistingFolder() throws IOException {
		logger.log(Level.FINE, String.format("testing download of all files from existing folder..."));
		
		//first we get all files and folders which are expected in the remote folder
		Set<String> expectedFolderContent = this.getExpectedFolderContent(this.remoteRoot);
		List<String> files = this.conn.listDirectory(this.remoteRoot);
		for (String cur : files) {
			//check for folders
			if (this.conn.isFolder(cur)) {
				//remove folders
				expectedFolderContent.remove(cur);
				continue;
			}
			
			//download file
			InputStream content = this.conn.receive(cur);
			assertNotNull(content);
			//remove the file which has been downloaded
			expectedFolderContent.remove(cur);
			
			
			InputStream expected = this.getExpectedFileContent(cur);
			if (expected == null)
				continue;
			//compare content with expected content
			assertTrue(IOUtils.contentEquals(expected, content ));
		}
		//in the end no file should remain in the set because all have been downloaded
		assertTrue(expectedFolderContent.toString(), expectedFolderContent.isEmpty());
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
		this.conn.receive("does/not/exist");
		fail("no exception was raised");
	}

	@Test
	public void testFailOnDownloadNullFile() throws IOException {
		thrown.expect(IllegalArgumentException.class);
		this.conn.receive(null);
		fail("no exception was raised");
	}
	
	@Test
	public void testFailOnDownloadWhileDisconnected() throws IOException {
		thrown.expect(UnsupportedOperationException.class);
		this.conn.disconnect();
		this.conn.receive("foo");
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
