package com.bytewood.hermes.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.bytewood.hermes.api.FileSystemConnector;

public abstract class BaseFileSystemConnector<T> implements FileSystemConnector<T> {
	public static final String NOT_INITIALISED_EXCEPTION	= "Has not yet been initialised";
	public static final String NO_CONNECTION_EXCEPTION 		= "no connection was supplied";
	public static final String NOT_CONNECTED_EXCEPTION 		= "Is not connected to a remote host";
	public static final String LOGIN_FAILED_EXCEPTION 		= "Login to ftp server as user %s failed. The reason returned was: \"%s\"";
	public static final String ROOT_PATH_NULL_EXCEPTION 	= "Cannot set a remote root folder of \"null\"";
	public static final String FOLDER_NOT_FOUND_EXCEPTION	= "Could not find directory %s";
	public static final String NOT_A_FOLDER_EXCEPTION 		= "the provided path is not a folder: %s";
	public static final String LIST_FILES_EXCEPTION			= "Could not list files in directory %s: %s";
	public static final String FILE_HAS_VANISHED_EXCEPTION	= "The remote file '%s' could not be found.";
	public static final String PATH_IS_NULL					= "The path provided is null";
	public static final String FOLDER_PATH_IS_NULL			= "The folder path provided is null";
	public static final String FILE_PATH_IS_NULL			= "The file path provided is null";
	

	protected T connection;

	/**
	 * builds the full remote path which is supplied, including the configured remote root
	 * @param folder
	 * @param filename
	 * @return
	 */
	protected final String full(String folder, String filename) {
		if (folder == null)
			throw new IllegalArgumentException(FOLDER_PATH_IS_NULL);
		if (filename == null)
			throw new IllegalArgumentException(FILE_PATH_IS_NULL);
		if (folder.startsWith(File.separator) == false)
			folder = File.separator + folder;
		if (folder.endsWith(File.separator) == false)
			folder = folder + File.separator;
		if (filename.startsWith(File.separator))
			filename = filename.substring(1);
		return full(folder + filename);
	}
	
	protected final String full(String path) {
		if (path == null)
			throw new IllegalArgumentException(PATH_IS_NULL);
		if (path.startsWith(File.separator) == false)
			return File.separator + path;
		return path;
	}
	
	protected void guard(String path) {
		if (this.isConnected() == false)
			throw new UnsupportedOperationException("Cannot provide a file input stream because the file system has been actively disconnected");
		if (path == null)
			throw new IllegalArgumentException("Provided path is null");
	}
	
	public List<String> listFilesInDirectory(String path) throws IOException {
		return this.doListFilesOrFoldersInDirectory(path, true, false);
	}
	
	public List<String> listFoldersInDirectory(String path) throws IOException {
		return this.doListFilesOrFoldersInDirectory(path, false, true);
	}
	
	private List<String> doListFilesOrFoldersInDirectory(String path, boolean excludeFolders, boolean excludeFiles) throws FileNotFoundException, IOException {
		this.guard(path);
		List<String> src = this.listDirectory( full(path) );
		List<String> ret = new ArrayList<String>( src.size() );
		
		for (String cur : src) {
			//check if folders should be excluded
			if (excludeFolders && this.isFolder( cur ))
				continue;
			//check if files should be excluded
			if (excludeFiles && this.isFile( cur ))
				continue;
			
			ret.add(cur);
		}
		return ret;
	}
	
	/*
	 * Getters and Setters
	 */
	public T getConnection() {
		return this.connection;
	}
	
	public void setConnection(T arg) {
		this.connection = arg;
	}
}
