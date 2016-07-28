package com.bytewood.filesystem;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LogConfigurationEnabled {
	private static Logger logger = Logger.getLogger(LogConfigurationEnabled.class.toString());
	
	static{
		try (final InputStream is = LogConfigurationEnabled.class.getResourceAsStream("/logging.properties")) {
			LogManager.getLogManager().readConfiguration(is);
			logger.log(Level.FINE, String.format("logging.properties from %s", LogConfigurationEnabled.class.getResource("/").toString()));
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, String.format("could not read logging.properties from %s", LogConfigurationEnabled.class.getResource("/").toString()), e);
		}

	}

}
