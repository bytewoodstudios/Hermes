package com.bytewood.hermes.api;

import java.io.File;

public interface FileReceiver {
	
	/**
	 * receive a file for processing
	 * @param arg
	 */
	public void receive(File arg);
}
