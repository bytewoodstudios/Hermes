package com.bytewood.hermes.api;

import java.io.File;

public interface TargetFileProvider {
	
	/**
	 * provide a File object on the local file system for a given remote path
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public File provide(String path) throws Exception;
}
