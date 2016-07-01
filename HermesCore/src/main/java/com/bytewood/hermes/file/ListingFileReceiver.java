package com.bytewood.hermes.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.bytewood.hermes.api.FileReceiver;

/**
 * a small utility class which adds all received files to a simple ArrayList which be accessed can later.,
 */
public class ListingFileReceiver implements FileReceiver {
	private List<File> list = new ArrayList<File>();

	public void receive(File arg) {
		this.list.add(arg);
	}
	
	public List<File> getFileList() {
		return this.list;
	}
}