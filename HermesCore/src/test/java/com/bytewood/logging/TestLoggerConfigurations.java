package com.bytewood.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import com.bytewood.filesystem.LogConfigurationEnabled;

public class TestLoggerConfigurations extends LogConfigurationEnabled {

	private static Logger logger = Logger.getLogger(TestLoggerConfigurations.class.toString());
	
	@Test
	public void displayAtDifferentLevels() {
		contextualizedTestLogMessage(Level.SEVERE);
		contextualizedTestLogMessage(Level.WARNING);
		contextualizedTestLogMessage(Level.INFO);
		contextualizedTestLogMessage(Level.FINE);
		contextualizedTestLogMessage(Level.FINER);
		contextualizedTestLogMessage(Level.FINEST);
	}
	
	private void contextualizedTestLogMessage(Level lvl){
		logger.log(lvl, String.format("logging statement at level %s", lvl));
	}


}
