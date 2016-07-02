package com.bytewood.hermes.file.ftp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.bytewood.hermes.file.BaseFileSystemConnector;
import com.bytewood.hermes.model.FtpConnection;

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
public class FtpConnector extends BaseFileSystemConnector<FtpConnection> {

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
	public boolean connect(FtpConnection arg) {
		//on connect(null) an IllegalArgumentException is thrown
		if (arg == null)
			throw new IllegalArgumentException(NO_CONNECTION_EXCEPTION);
		this.connection = arg;
		return this.connect();
	}
	
	/**
	 * tries to connect to the remote ftp-server defined in the connection
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
//		if (logger.isTraceEnabled())
//			logger.trace(String.format("connecting to %s", this.connection.toString()));
		
		try {
			// establish connection
//TODO		if (Log Trace)
//				String.format("connecting to server %s", this.connection.getServer);
			this.ftpClient.connect(this.connection.getHost(), this.connection.getPort());
			
			//check and switch to a specific client mode
//			establishClientMode(this.connection.getClientMode());
			
			//log into remote host
//TODO		if (Log Trace)
//				"Attempting Login with user %s to server %s"
			
			boolean loggedIn = this.ftpClient.login(this.connection.getUserName(), this.connection.getPassword());
			if (loggedIn == false)
				throw new ConnectException(String.format(LOGIN_FAILED_EXCEPTION, this.connection.getUserName(), this.ftpClient.getReplyString()));
			
			//check and setFile Type
//			this.establishFileType(this.connection.getFileType());

			return;
		} catch (ConnectException e) {
//			TODO Warn "unable to connect %s: %s" - this.connection.toString(), e.getMessage()
			return;
		} catch (SocketTimeoutException e) {
//			TODO Warn about socket timeout exception
			return;			
		} catch (Exception e) {
//			TODO Warn error while connecting %s: %s" - this.connection.toString(), e.getMessage()
			return;
		} 
	}

	public boolean disconnect() {
		try {
			this.ftpClient.disconnect();
		} catch (IOException e) {
			//basically ignore but we should print out a warning
//			logger.warn()
		}
		return true;
	}

	public List<String> listDirectory(String path) throws FileNotFoundException, IOException {
		return this.doListDirectory(path, false, false);
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

	public InputStream provideInputStream(String path) throws IOException {
		this.guard(path);
		InputStream is = this.ftpClient.retrieveFileStream(full(path));
		if (is == null)
			throw new FileNotFoundException(path);
		return is;
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
}
