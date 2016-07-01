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

	protected boolean isConnected = false;
	protected T connection;

	/**
	 * builds the full remote path which is supplied, including the configured remote root
	 * @param folder
	 * @param filename
	 * @return
	 */
	protected final String full(String folder, String filename) {
		if (folder.endsWith(File.separator) == false)
			folder = folder + File.separator;
		if (filename.startsWith(File.separator))
			filename = filename.substring(1);
		return full(folder + filename);
	}
	
	protected final String full(String path) {
		if (path.startsWith(File.separator) == false)
			return File.separator + path;
		return path;
	}
	
	protected void guard(String path) {
		if (this.isConnected == false)
			throw new UnsupportedOperationException("Cannot provide a file input stream because the file system has been actively disconnected");
		if (path == null)
			throw new IllegalArgumentException("Provided path is null");
	}
	
	public boolean isConnected() {
		return this.isConnected;
	}
	
	public List<String> listFilesInDirectory(String path) throws IOException {
		this.guard(path);
		List<String> src = this.listDirectory( full(path) );
		List<String> ret = new ArrayList<String>( src.size() );
		
		for (String cur : src)
			if( this.isFile(  full( cur )  ) == true)
					ret.add(cur);
		return ret;
	}
	
	public List<String> listFoldersInDirectory(String path) throws IOException {
		this.guard(path);
		List<String> src = this.listDirectory( full(path) );
		List<String> ret = new ArrayList<String>( src.size() );
		
		for (String cur : src)
			if( this.isFolder(  full( cur )  ) == true)
				ret.add(cur);
		return ret;
	}
	
	public boolean exists(String path) throws IOException {
		this.guard(path);
		
		try { 
			boolean a = this.isFile( full(path) );
			boolean b = this.isFolder( full(path) );
			if (a == false  && b == false)
				return false;
		} catch (FileNotFoundException e) {
			return false;
		}
		
		return true;
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
