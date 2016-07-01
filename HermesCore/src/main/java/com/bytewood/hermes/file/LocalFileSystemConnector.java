package com.bytewood.hermes.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.bytewood.hermes.model.FileSystemConnection;

/**
 * The local FS connector was invented as mock-connection which later was promoted to production code.
 * This can still be seen in that it extends the BaseFileSystemConnector as well as implementing FileSystemConnection.
 * 
 * @author rainerkern
 */
public class LocalFileSystemConnector extends BaseFileSystemConnector<FileSystemConnection> implements FileSystemConnection {
	
	public LocalFileSystemConnector() {
		super.isConnected = true;
	}
	
	public boolean connect() {
		return true;
	}

	public boolean connect(FileSystemConnection arg) {
		return true;
	}
	
	public boolean disconnect() {
		this.isConnected = false;
		return true;
	}

	public boolean isFolder(String path) throws FileNotFoundException {
		super.guard(path);
		return new File(full(path)).isDirectory();
	}

	public boolean isFile(String path) throws FileNotFoundException {
		super.guard(path);
		return new File(full(path)).isFile();
	}
	
	public List<String> listDirectory(String path) throws FileNotFoundException {
		super.guard(path);
		String fullPath = full(path);
		File folder = new File(fullPath);
		if (folder.exists() == false)
			throw new FileNotFoundException("could not find folder: "+fullPath);
		if (folder.isDirectory() == false)
			throw new FileNotFoundException(String.format(NOT_A_FOLDER_EXCEPTION,fullPath));
		
		return Arrays.asList( folder.list() );
	}
	
	/**
	 * @throws NullPointerException if path is null
	 * @throws FileNotFoundException if the path is invalid
	 */
	public InputStream provideInputStream(String path) throws FileNotFoundException {
		super.guard(path);
		File file = new File(full(path));
		return new FileInputStream(file);
	}
}
