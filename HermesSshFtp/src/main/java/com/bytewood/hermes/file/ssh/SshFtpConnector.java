package com.bytewood.hermes.file.ssh;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.bytewood.hermes.file.BaseFileSystemConnector;
import com.bytewood.hermes.model.SshConnection;

/**
 * The FtpConnector is the expert to handle classic FTP connections. It translates the API of org.apache.commons.net.ftp.FTPClient. 
 * <b>Shortcomings</b><br />
 * Since FTP is a stateful protocol which expects the client to know about the current working directory, there are some problems. Currently 
 * the most functional way is to use absolute paths everywhere.
 * In certain cases this can lead to a problem. E.g. if the root "/" directory is not mapped to the FTP user's root but to the system root.
 * In this case the mapping will yield wrong results and some operations will fail / return a FileNotFoundException.   
 * 
 * TODO:
 * <li> logging
 * <li> ftp mode handling (add handling of active remote etc.
 * <li> file mode handling
 * <li> add stateful mode
 * <li> verify integrity of the provided FTP Connection
 * @see org.apache.commons.net.ftp.FTPClient;
 * @author rainerkern
 */
public class SshFtpConnector extends BaseFileSystemConnector<SshConnection> {

	FTPClient ftpClient;
	private int maxConnectionRetries = 3;
	
	
	@Override
	protected void guard(String path) {
		this.guard();
		super.guard(path);
	}
	
	private void guard() {
		if (this.isConnected() == false)
			throw new UnsupportedOperationException(NOT_INITIALISED_EXCEPTION);
	}
	
	public boolean isConnected() {
		if (this.ftpClient == null)
			return false;
		//else
		return this.ftpClient.isConnected();
	}
	
	@Override
	public boolean connect(SshConnection arg) {
		//on connect(null) an IllegalArgumentException is thrown
		if (arg == null)
			throw new IllegalArgumentException(NO_CONNECTION_EXCEPTION);
		this.connection = arg;
		return this.connect();
	}
	
	/**
	 * tries to connect to the remote server defined in the connection
	 * @return true if connection was established successfully 
	 */
	public boolean connect() {
		if (super.connection == null)
			throw new UnsupportedOperationException(NO_CONNECTION_EXCEPTION);
			
		//if no FTP client was supplied, instantiate one
		if (this.ftpClient == null)
			this.ftpClient = new FTPClient();
		
		if (this.isConnected() == true)
			return true;

		for (int i=0; i <= this.maxConnectionRetries; i++) {
			this.doConnect(); 
			if (this.isConnected() == true)
				break;
		}
		return this.isConnected();
	}
	
	private void doConnect() {
		
	}
	
	/**
	 * has to be overridden because of the way that isFile() and isFolder() work on FTP
	 */
	@Override
	public List<String> listFilesInDirectory(String path) throws IOException {
		return this.doListDirectory(path, true, false);
	}
	
	/**
	 * has to be overridden because of the way that isFile() and isFolder() work on FTP
	 */
	@Override
	public List<String> listFoldersInDirectory(String path) throws IOException {
		return this.doListDirectory(path, false, true);
	}
	
	private List<String> doListDirectory(String path, boolean excludeFolders, boolean excludeFiles) throws IOException {
		this.guard(path);
		FTPFile [] ar = null;
		try {
//			if (logger.isTraceEnabled())
//				logger.trace(String.format("listing files of directory %s", pathname));
			if (this.isFolder(path) == false)
				throw new FileNotFoundException(String.format(NOT_A_FOLDER_EXCEPTION, path));
			ar = this.ftpClient.listFiles(path);
		} catch (Exception e) {
			throw new IOException(String.format(LIST_FILES_EXCEPTION, path, e.getMessage()), e);
		}
		
		List<String> ret = new ArrayList<String>(ar.length);
		for(FTPFile cur : ar) {
			//check if folders should be excluded
			if (excludeFolders && cur.isDirectory())
				continue;
			//check if files should be excluded
			if (excludeFiles && cur.isFile())
				continue;
			//add trailing "/" for folders
			String name = (cur.isFile()) ? cur.getName() : cur.getName()+File.separator; 
			
			ret.add( full(path, name) );
		}
		
		return ret;
	}
	
	public boolean exists(String path) throws FileNotFoundException, IOException {
		throw new UnsupportedOperationException();
	}
	
	
	public boolean isFolder(String path) throws FileNotFoundException, IOException {
		this.guard(path);
		//on FTP we can only try to enter the folder to see if 
		boolean success = this.ftpClient.changeWorkingDirectory(full(path));
		return success;
	}

	/**
	 * tries to create a file download stream for checking if a file is a file or a directory 
	 */
	public boolean isFile(String path) throws FileNotFoundException {
		this.guard(path);
		try (InputStream inputStream = this.ftpClient.retrieveFileStream(path)) {
			int returnCode = ftpClient.getReplyCode();
			if (inputStream == null || returnCode == 550)
				return false;
		} catch (IOException e) {
			this.postCommand();
			return false;
		}
	    return true;
	}

	public InputStream receive(String path) throws IOException {
		this.guard(path);
		InputStream is = this.ftpClient.retrieveFileStream(full(path));
		if (is == null)
			throw new FileNotFoundException(path);
		return is;
	}

	public void send(String path, OutputStream os) {
		throw new UnsupportedOperationException("sending is not yet implementd");
	}
	
	/**
	 * convenience method for directly downloading files
	 * @param remoteFilename
	 * @param localFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */
//	public void download(String path, OutputStream os) throws IOException {
//		this.guard(path);
//		if (os == null)
//			throw new IllegalArgumentException("provided output stream is null");
//		
////		if (logger.isTraceEnabled())
////			logger.trace(String.format("download(file %s -> %s): begin", remoteFilename, localFile.getAbsolutePath()));
//		
//		// create an input stream to the file content and use file output stream to write it
//		try (InputStream is = provideInputStream(path)) {
//			IOUtils.copy(is, os);
//		} catch (NullPointerException e) {
//			throw new FileNotFoundException(String.format(FILE_HAS_VANISHED_EXCEPTION, path));
//		} finally {
//			IOUtils.closeQuietly(os);
//		}
//						
//		this.postCommand();
//	}
	
	private void postCommand() {
		try {
			if (this.ftpClient.completePendingCommand() == false) {
//				logger.warn(String.format("unable to complete pending command"));
			}
		} catch (IOException e) {
//			logger.warn(String.format("unable to complete pending command"),e);
		}
	}

	/*
	 * Getters and Setters
	 */
	public FTPClient getFtpClient() {
		return ftpClient;
	}

	public void setFtpClient(FTPClient ftpClient) {
		this.ftpClient = ftpClient;
	}

	public void setMaxConnectionRetries(int arg) {
		this.maxConnectionRetries = arg;
	}
	
	public int getMaxConnectionRetries() {
		return this.maxConnectionRetries;
	}

	@Override
	public boolean disconnect() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> listDirectory(String directory) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
