package com.bytewood.hermes.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.bytewood.hermes.api.FileReceiver;
import com.bytewood.hermes.api.FileSystemConnector;
import com.bytewood.hermes.api.TargetFileProvider;

public class FileSystemDownloader {
	// Exceptoion Messages
	public static final String PATH_IS_NULL				= "Provided path is null.";
	public static final String FOLDER_DOWNLOAD_FAILED	= "Could not dowload %s from %s."; 
	
	// create a dummy receiver if none is provided
	private static final FileReceiver	nopReceiver	= new FileReceiver() {
		public void receive(File arg) {/* NOP */}
	};
	
	private FileSystemConnector			connector;
	private TargetFileProvider			targetFileProvider	= new TempFileProvider();
	private boolean						disconnectOnExit	= true;

	public void downloadFolder(String sourceFolder) throws Exception {
		downloadFolder(sourceFolder, null);
	}

	public void downloadFolder(String sourceFolder, FileReceiver receiver) throws Exception {
		if (sourceFolder == null)
			throw new IllegalArgumentException(PATH_IS_NULL);
		if (this.connector.isConnected() == false)
			this.connector.connect();
		if (receiver == null)
			receiver = nopReceiver;

		this.recursiveDownloadFolder(sourceFolder, receiver);

		// close connection
		if (this.disconnectOnExit)
			this.connector.disconnect();
	}

	private void recursiveDownloadFolder(String currentFolder, FileReceiver receiver) throws Exception {
		// enumerate all files
		List<String> files = this.connector.listDirectory(currentFolder);
		for (String curName : files) {
			// is the current item a folder?
			if (this.connector.isFolder(curName)) {
				this.recursiveDownloadFolder(currentFolder + "/" + curName, receiver);
				continue;
			}

			// else ... well it is a file then => download
			try {
				File localFile = this.download(currentFolder, curName);
				receiver.receive(localFile);
			} catch (Exception e) {
				throw new Exception(String.format(FOLDER_DOWNLOAD_FAILED, curName, currentFolder), e);
			}
		}
	}

	/**
	 * downloads the file depicted by _path_ to the local download directory
	 * 
	 * @param path
	 * @return a File object referencing the downloaded file
	 * @throws Exception
	 */
	private File download(String folder, String file) throws Exception, IOException, FileNotFoundException {
		/* You can get Path from file also: file.toPath() */
		InputStream in = this.connector.provideInputStream(folder + "/" + file);
		File target = this.targetFileProvider.provide(file);
		Path path = Paths.get(target.getAbsolutePath());
		Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
		return target;
	}

	/*
	 * Getters and Setters
	 */
	public FileSystemConnector getConnector() {
		return this.connector;
	}

	public void setConnector(FileSystemConnector arg) {
		this.connector = arg;
	}

	public boolean getDisconnectOnExit() {
		return this.disconnectOnExit;
	}

	public void setDisconnectOnExit(boolean arg) {
		this.disconnectOnExit = arg;
	}

	public TargetFileProvider getTargetFileProvider() {
		return targetFileProvider;
	}

	public void setTargetFileProvider(TargetFileProvider targetFileProvider) {
		this.targetFileProvider = targetFileProvider;
	}
}
