package com.bytewood.hermes.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface FileSystemConnector<T> {
	
	/**
	 * @throws UnsupportedOperationException if the FileSystemConnector's connection is null
	 * 
	 */
	public boolean connect();
	
	/**
	 * @throws IllegalArgumentException if the provided connection is null
	 */
	public boolean connect(T connection);
	public boolean isConnected();
	public boolean disconnect();
	
	public T getConnection();
	public void setConnection(T arg);
	
	/**
	 * @throws IllegalArgumentException if path is null
	 * @throws FileNotFoundException if the path is invalid
	 * @throws IOException if any exception occurs while communicating with the remote file system
	 * @param path the fully qualified remote path e.g.: "/folder1/folder2" or "/folder1/folder2/"
	 * @return a list of all the files and folders in a given directory identified by their fully qualified path relative to the remote root
	 */
	public List<String> listDirectory(String directory) throws FileNotFoundException, IOException;

	/**
	 * @throws IllegalArgumentException if path is null
	 * @throws FileNotFoundException if the path is invalid
	 * @throws IOException if any exception occurs while communicating with the remote file system
	 * @param path the fully qualified remote path e.g.: "/folder1/folder2" or "/folder1/folder2/" 
	 * @return a list of all the files in a given directory identified by their fully qualified path relative to the remote root
	 */
	public List<String> listFilesInDirectory(String path) throws FileNotFoundException, IOException;

	/**
	 * @throws IllegalArgumentException if path is null
	 * @throws FileNotFoundException if the path is invalid
	 * @throws IOException if any exception occurs while communicating with the remote file system
	 * @param path the fully qualified remote path e.g.: "/folder1/folder2" or "/folder1/folder2/"
	 * @return a list of all the folders in a given directory identified by their fully qualified path relative to the remote root
	 */
	public List<String> listFoldersInDirectory(String path) throws FileNotFoundException, IOException;

	/**
	 * convenience method to find out if a file exists
	 * 
	 * @throws IllegalArgumentException if path is null
	 * @throws FileNotFoundException if the path is invalid
	 * @throws IOException if any exception occurs while communicating with the remote file system
	 */
	public boolean exists(String path) throws IOException;

	
	/**
	 * @throws IllegalArgumentException if path is null
	 * @throws FileNotFoundException if the path is invalid
	 * @throws IOException if any exception occurs while communicating with the remote file system
	 */
	public boolean isFolder(String path) throws FileNotFoundException, IOException;

	/**
	 * @throws IllegalArgumentException if path is null
	 * @throws FileNotFoundException if the path is invalid
	 * @throws IOException if any exception occurs while communicating with the remote file system
	 */
	public boolean isFile(String path) throws FileNotFoundException, IOException;
	
	/**
	 * @throws NullPointerException if path is null
	 * @throws FileNotFoundException if the path is invalid
	 * @throws IOException if any exception occurs while communicating with the remote file system
	 */
	public InputStream provideInputStream(String path) throws FileNotFoundException, IOException;
	
}