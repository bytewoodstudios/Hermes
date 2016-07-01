package com.bytewood.filesystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.rules.TemporaryFolder;

import com.bytewood.hermes.file.LocalFileSystemConnector;

public class TestLocalConnector extends AbstractTestConnector {
	// Junit Temporary folder to hold our test cases
	private TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setup() throws IOException {
		//start with a new connection every time
		super.conn = new LocalFileSystemConnector();
		super.conn.connect();
		//create some folders
		tempFolder.create();
		//our test root
		File testRoot = tempFolder.newFolder();		
		super.remoteRoot = testRoot.getAbsolutePath();
		super.remotePathToADirectory = testRoot.getAbsolutePath();
		
		//create a folder
		new File(super.remoteRoot, "folder").mkdirs();
		
		String[] fileNames = {"test1", "test2", "test3", "test4"};
		Random rnd = new Random();
		for (String curName : fileNames) {
			String content = Integer.toString(rnd.nextInt());
			this.expectedContent.put(curName,content);
		}
		
		//create temporary files and write some content into them
		for (Entry<String,String> entry : this.expectedContent.entrySet()) {
			String curName = entry.getKey();
			String content = entry.getValue();
			File cur = new File(testRoot,curName);
			cur.createNewFile();
			
			FileOutputStream os = new FileOutputStream(cur);
			byte[] contentInBytes = content.getBytes();

			os.write(contentInBytes);
			os.flush();
			os.close();
			cur.deleteOnExit();
			
			super.remotePathToAFile = cur.getAbsolutePath();
			super.expectedContent.put(curName,content);
		}
	}
	
	
	@After
	public void tearDown() {
		tempFolder.delete();
	}
}
