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

import com.bytewood.filesystem.MockFSConnection;
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
}
