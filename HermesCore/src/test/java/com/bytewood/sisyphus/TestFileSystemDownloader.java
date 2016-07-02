package com.bytewood.sisyphus;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.bytewood.hermes.api.FileSystemConnector;
import com.bytewood.hermes.api.TargetFileProvider;
import com.bytewood.hermes.file.BaseFileSystemConnector;
import com.bytewood.hermes.file.FileSystemDownloader;
import com.bytewood.hermes.file.ListingFileReceiver;
import com.bytewood.hermes.file.TempFileProvider;

public class TestFileSystemDownloader {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private FileSystemDownloader downloader;
	@SuppressWarnings("rawtypes")
	private FileSystemConnector conn = new MockFSConnection();
	
	@Before
	public void init() {
		this.downloader = new FileSystemDownloader();
		this.downloader.setConnector(this.conn);
		//quick sanity check
		assertTrue(this.conn == this.downloader.getConnector());
	}

	@Test //positive
	public void testdownload() throws Exception {
		//the folder name is actually not important because of the mock connection
		TargetFileProvider ttf = new TargetFileProvider() {
			public File provide(String arg) throws Exception {
				File ret = new File(arg);
				ret.createNewFile();
				ret.deleteOnExit();
				return ret;
			}
		}; 
		
		this.downloader.setTargetFileProvider(ttf);
		this.downloader.downloadFolder("/");
	}
	
	@Test //positive
	public void testdownloadWithFileReciever() throws Exception {
		ListingFileReceiver receiver = new ListingFileReceiver(); 
		//the folder name is actually not important because of the mock connection
		this.downloader.downloadFolder("/", receiver);
	}
	
	
	@Test //negative
	public void testDownloadNullPath() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		this.downloader.downloadFolder(null);
		fail("no exception was thrown");
	}
	
	@Test //negative
	public void testGettersAndSetters() throws Exception {
		//get default target file provider
		TargetFileProvider provider = this.downloader.getTargetFileProvider();
		assertNotNull(provider);
		assertTrue(provider instanceof TempFileProvider);
		
		//set a new provider
		TargetFileProvider nanProvider = new TargetFileProvider() {
			public File provide(String arg) throws Exception {
				return null;
			}
		};
		this.downloader.setTargetFileProvider(nanProvider);
		assertTrue( nanProvider == this.downloader.getTargetFileProvider() );
		
		//disconnect on exit
		this.downloader.setDisconnectOnExit(false);
		assertFalse(this.downloader.getDisconnectOnExit());
	}
	
	
	/**
	 * Mock Connection Class for faster testing. Even simulates folders
	 */
	@SuppressWarnings("rawtypes")
	private class MockFSConnection extends BaseFileSystemConnector {
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

		public InputStream provideInputStream(String path) throws FileNotFoundException {
			String[] parts = path.split("/");
			String filename = parts[parts.length-1];
			String content = this.files.get(filename);
			InputStream is = new ByteArrayInputStream( content.getBytes() );
			return is;
		}

		public boolean isFolder(String path) throws FileNotFoundException {
			return path.equals(directoryPath);
		}

		public boolean isFile(String path) throws FileNotFoundException {
			return path.equals(directoryPath) == false;
		}

	}
}
