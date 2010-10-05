package de.jw.cloud42.core.service;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * 
 * Helper class, provides methods to read application settings from property files.
 * Singleton!
 * 
 * @author fbitzer
 * 
 */
public class Cloud42Settings {
	/**
	 * Instance reference for Singleton, for each property file a new instance is created.
	 */
	private static HashMap<String, Cloud42Settings> instances = new HashMap<String, Cloud42Settings>();

	private final Logger LOG = Logger.getLogger(this.getClass().getName());

	/**
	 * the properties from where to load the Settings.
	 */
	private FileConfiguration properties;

	/**
	 * Private constructor, used to initialize the properties.
	 * 
	 */
	private Cloud42Settings(String filename) {
		try {
			// this is the PropertiesLoader of Apache Commons Configuration.
			// The properties file is looked up in:
			// - /etc/cloud42
			// - the user's home directory
			// - the classpath
			properties = new PropertiesConfiguration();

			properties.setBasePath("/etc/cloud42");
			properties.setFileName(filename);

			properties.load();
			
			LOG.info("Loaded settings from " + properties.getFile().getPath());
		} catch (Exception e) {
			LOG.log(Level.SEVERE,"Error reading properties file for Cloud42 (should be located at /etc/cloud42/" +  filename + ")!");
			
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method to get an instance of {@link Cloud42Settings} for the given filename according to Singleton pattern.
	 * 
	 * A {@link RuntimeException} is thrown if none of the property files are present or unreadable.
	 * 
	 * @param filename the name of the properties file. The algorithm tries to load the file from /etc/cloud42, then from the user's home directory and finally from the classpath.
	 * 
	 * @return object instance
	 */
	public static Cloud42Settings getInstance(String filename) {
		
		Cloud42Settings theInstance = null;
		
		if (instances.containsKey(filename)){
			
			theInstance = instances.get(filename);
		
		} else {
			
			theInstance = new Cloud42Settings(filename);
			
			instances.put(filename, theInstance);
			
		}
		
		return theInstance;
	}

	/**
	 * Reads the given property and returns its value as String.
	 * 
	 * @param propertyName
	 *            the name of the property to read
	 * @return the String value for the given propertyName
	 */
	public String getString(String propertyName) {
		
		LOG.log(Level.FINE, "Reading property " + propertyName + " in file " +  properties.getFile().getPath());
		
		return properties.getString(propertyName);
	}

	/**
	 * Reads the given property and returns its value as boolean.
	 * 
	 * @param propertyName
	 *            the name of the property to read
	 * @return the boolean value for the given propertyName
	 */
	public boolean getBoolean(String propertyName) {
		return properties.getBoolean(propertyName);
	}

	/**
	 * Reads the given property and returns its value as int.
	 * 
	 * @param propertyName
	 *            the name of the property to read
	 * @return Integer value of given property, 0 in case of error.
	 */
	public int getInteger(String propertyName) {
		return properties.getInt(propertyName);
	}

	/**
	 * Read all properties with the same key into a List.
	 * 
	 * specify multiple values as follows:
	 * 
	 * <pre>
	 * listen.port=1.2.3.4
	 * listen.port=127.0.0.1
	 * </pre>
	 * 
	 * @param propertyName
	 * @return A list of the properties
	 */
	public List<?> getList(String propertyName) {
		return properties.getList(propertyName);
	}

	/**
	 * Read all properties with the same key into a String[].
	 * 
	 * specify multiple values as follows:
	 * 
	 * <pre>
	 * listen.port=1.2.3.4
	 * listen.port=127.0.0.1
	 * </pre>
	 * 
	 * @param propertyName
	 * @return A String Array of the properties
	 */
	public String[] getStringArray(String propertyName) {
		return properties.getStringArray(propertyName);
	}
}

