package org.pdfclown.logging;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
	private static Logger LOGGER; 
	private final static int LOG_SIZE = 10024;
	
	public static void log(String msg) {
		if(LOGGER == null)
			init();
		
		LOGGER.log(Level.FINE, msg);
	}
	
	public static void init() {
		if(LOGGER != null)
			return;
		
		Handler handler = null;
		try {
			handler = new FileHandler("test.log");
		} catch(IOException e) {
			System.err.println(e);
		}
		
		LOGGER = Logger.getLogger("Clown");
		LOGGER.setLevel(Level.ALL);
		if(handler != null)
			LOGGER.addHandler(handler);
	}
}
