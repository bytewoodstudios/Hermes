package com.bytewood.filesystem;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
		if (super.remoteRoot.endsWith( File.separator) == false)
			super.remoteRoot += File.separator;
		super.remotePathToADirectory = testRoot.getAbsolutePath();
		
		//create a folder
		new File(super.remoteRoot, "folder").mkdirs();
		
		//cretate Files with content
		String[] fileNames = {"test1", "test2", "test3", "test4"};
		Random rnd = new Random();
		for (String curName : fileNames) {
			String content = Integer.toString(rnd.nextInt());

			File cur = new File(testRoot,curName);
			cur.createNewFile();
			
			//write content into files
			FileOutputStream os = new FileOutputStream(cur);
			byte[] contentInBytes = content.getBytes();

			os.write(contentInBytes);
			os.flush();
			os.close();
			cur.deleteOnExit();
			
		}
		super.remotePathToAFile = super.remoteRoot + File.separator + "test1";
	}
	

	@Override
	protected InputStream getExpectedFileContent(String path) {
		File expectedFile = new File(path);
		assertTrue("It is assumed that files to be able to provide content",expectedFile.exists());
		assertTrue("It is assumed that provided paths are files",expectedFile.isFile());

		try {
			return new FileInputStream( expectedFile );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}


	@Override
	protected Set<String> getExpectedFolderContent(String path) {
		File folder = new File(path);
		assertTrue("It is assumed that files to be able to provide content",folder.exists());
		assertTrue("It is assumed that files provided paths are folders",folder.isDirectory());

		//construct fully qualified paths
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
	
	@After
	public void tearDown() {
		tempFolder.delete();
	}
}
