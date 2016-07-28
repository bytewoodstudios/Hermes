package com.bytewood.filesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.bytewood.hermes.file.BaseFileSystemConnector;

/**
 * Mock Connection Class for faster testing. Even simulates folders
 * This class also performs JUnit Tests on the {@link BaseFileSystemConnector}
 */
@SuppressWarnings("rawtypes")
public class MockFSConnection extends BaseFileSystemConnector {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(MockFSConnection.class.toString());
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	HashMap<String, String> files;
	String directoryPath = "/directory";
	
	public MockFSConnection() {
		this.files = new HashMap<String,String>();
		String[] fileNames = {"test1", "test2", "test3", "test4"};
		Random rnd = new Random();
		for (String curName : fileNames) {
			String content = Integer.toString(rnd.nextInt());
			this.files.put(curName,content);
		}
	}
	
	@Override
	public boolean isConnected() {
		return true;
	}
	
	@Override
	public boolean connect() {
		return true;
	}

	@Override
	public boolean connect(Object connection) {
		return true;
	}

	@Override
	public boolean disconnect() {
		return true;
	}

	@Override
	public Object getConnection() {
		return null;
	}
	
	@Override
	public void setConnection(Object arg) {
	}

	public List<String> listDirectory(String directory) throws FileNotFoundException {
		//enumerate "files"
		List<String> ret = new ArrayList<String>( this.files.keySet() );
		
		//if we are not currently "inside the directory" ... add it to the list
		if (directory.endsWith(this.directoryPath) == false)
			ret.add(directoryPath);
		
		return ret;
	}

	public InputStream receive(String path) throws FileNotFoundException {
		String[] parts = path.split("/");
		String filename = parts[parts.length-1];
		String content = this.files.get(filename);
		InputStream is = new ByteArrayInputStream( content.getBytes() );
		return is;
	}

	public void send(String path, OutputStream os) {
		throw new UnsupportedOperationException("sending is not yet implementd");
	}
	
	@Override
	public boolean exists(String path) throws IOException {
		return true;
	}
	
	public boolean isFolder(String path) throws FileNotFoundException {
		return path.equals(directoryPath);
	}

	public boolean isFile(String path) throws FileNotFoundException {
		return path.equals(directoryPath) == false;
	}
	
	@Test
	public void testFullPathCreation() {
		String[] tests = {"", "/", "/file", "/folder/", "folder1/file"};
		
		for(String cur : tests) {
			String res = super.full(cur);
			testPathResults(cur,res);
		}
		
		thrown.expect(IllegalArgumentException.class);
		super.full(null);
		fail("expected exception was not thrown");
	}
	
	@Test
	public void testFullPathCreation2() {
		String[] folders = {"", "/", "/folder", "/folder/"};
		String[] files = {"", "/", "/file", "/folder/", "folder1/file"};
		
		
		for(String folder: folders) {
			for(String file : files) {
				String res = super.full(folder,file);
				testPathResults(file,res);
			}
		}

		try {
			super.full(null,null);
			fail("expected exception was not thrown");
		} catch (IllegalArgumentException e) {
			/* expected */
		}
		try {
			super.full("",null);
			fail("expected exception was not thrown");
		} catch (IllegalArgumentException e) {
			/* expected */
		}
		try {
			super.full(null,"");
			fail("expected exception was not thrown");
		} catch (IllegalArgumentException e) {
			/* expected */
		}
	}
	
	private void testPathResults(String cur, String res) {
		assertNotNull(res);
		//check beginning of the path
		assertTrue(res.startsWith(File.separator));
		assertTrue(res.contains(cur));
		
		//special case for empty strings
		if (cur.length() == 0)
			return;
		
		//check the end of the path
		boolean expected = cur.endsWith(File.separator);
		boolean actual = res.endsWith(File.separator);
		assertEquals(expected, actual);
	}

}