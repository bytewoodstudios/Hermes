package com.bytewood.hermes.file;

import java.io.File;
import java.io.IOException;

import com.bytewood.hermes.api.TargetFileProvider;

public class TempFileProvider implements TargetFileProvider {

	private File rootFolder = null;
	private String prefix = "bw-";
	private String suffix = "tempfile";
	private boolean deleteOnExit = true;
	
	public File provide(String arg) throws IOException {
		//create a temp file with the provided string
		String pre = (arg == null) ? prefix : prefix+arg;
		File ret = File.createTempFile(pre, suffix, this.rootFolder);
		if (this.deleteOnExit)
			ret.deleteOnExit();
		return ret;
	}

	public String getFilePrefix() {
		return prefix;
	}

	public void setFileSuffix(String suffix) {
		this.suffix = suffix;
	}
	
	public void setFilePrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getFileSuffix() {
		return suffix;
	}
	
	
	public void setFileDeleteOnExit(boolean arg) {
		this.deleteOnExit = arg;
	}

	public boolean getFileDeleteOnExit() {
		return this.deleteOnExit;
	}
}
