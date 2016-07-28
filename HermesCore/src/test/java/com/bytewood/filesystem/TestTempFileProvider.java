package com.bytewood.filesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Test;

import com.bytewood.hermes.api.TargetFileProvider;
import com.bytewood.hermes.file.TempFileProvider;

public class TestTempFileProvider {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(TestTempFileProvider.class.toString());
	
	private File tmp;
	
	@After
	public void tearDown() {
		if (tmp == null)
			return;
		tmp.delete();
		assertFalse(tmp.exists());
	}
	
	@Test
	public void testDefault() throws Exception {
		TargetFileProvider p = new TempFileProvider();
		String identifier = "foo";
		tmp = p.provide(identifier);
		assertNotNull(tmp);
		assertTrue(tmp.exists());
		assertTrue(tmp.canRead());
		assertTrue(tmp.canWrite());
		//check if the identifier is part of the filename
		assertTrue(tmp.getName().contains(identifier));
	}
	
	@Test
	public void testNullArg() throws Exception {
		TargetFileProvider p = new TempFileProvider();
		tmp = p.provide(null);
		assertNotNull(tmp);
		assertTrue(tmp.exists());
		assertTrue(tmp.canRead());
		assertTrue(tmp.canWrite());
	}
	
	@Test
	public void testNullNoAutomaticDelete() throws Exception {
		TempFileProvider p = new TempFileProvider();
		p.setFileDeleteOnExit(false);
		String identifier = "foo";
		tmp = p.provide(identifier);
		assertNotNull(tmp);
		assertTrue(tmp.exists());
		assertTrue(tmp.canRead());
		assertTrue(tmp.canWrite());
		tmp.delete();
		assertFalse(tmp.exists());
	}

	
	@Test
	public void testGettersAndSetters() {
		TempFileProvider p = new TempFileProvider();
		final String s = "aaa";
		p.setFilePrefix(s);
		assertEquals(s, p.getFilePrefix());
		p.setFileSuffix(s);
		assertEquals(s, p.getFileSuffix());
		
		//default is true
		assertTrue(p.getFileDeleteOnExit());
		p.setFileDeleteOnExit(false);
		assertFalse(p.getFileDeleteOnExit());
	}

}
