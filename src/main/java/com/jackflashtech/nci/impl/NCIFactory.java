package com.jackflashtech.nci.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import com.jackflashtech.nci.NCIDevice;
import com.jackflashtech.nci.NCIException;

/**
 * This class is the entry point for the library. Instantiate this class and
 * request devices by name. The names are defined in a properties file found in
 * the root of the classpath in a file called "nci.properties". For a device we
 * choose to refer to as "brecknell" that communications over COM6, we would set
 * up a properties file such as:
 * 
 * nci.brecknell.implclass=NCIDeviceRxtx
 * nci.brecknell.paritycheck=false
 * nci.brecknell.commport=COM6
 * 
 * Other than "implclass", the other properties should be documented in the
 * specific implementation of NCIDevice specified in "implclass".
 * 
 * @author Jonathan Card
 *
 */
public class NCIFactory {
	Properties settings;
	
	public NCIFactory() {
		try {
			this.settings = new Properties();
			InputStream inStream = NCIFactory.class.getClassLoader().getResourceAsStream("nci.properties");
			settings.load(inStream);
		} catch (IOException e) {
			this.settings = null;
		}
	}

	public NCIDevice getDevice(String name) throws NCIException {
		if (settings == null) throw new NCIException("This factory was not initialized correctly. This probably comes from not finding nci.properties.");
		Properties deviceProperties = new Properties();
		String deviceClassName = this.settings.getProperty("nci." + name + ".implclass");
		for (String propName : this.settings.stringPropertyNames()) {
			deviceProperties.put(propName, this.settings.get(propName));
		}
		try {
			Class<? extends NCIDevice> deviceClassObj =
					this.getClass().getClassLoader().loadClass(deviceClassName).asSubclass(NCIDevice.class);
			Constructor<? extends NCIDevice> deviceConstructor =
					deviceClassObj.getDeclaredConstructor(Properties.class, String.class);
			return deviceConstructor.newInstance(deviceProperties, name);
		} catch (ClassNotFoundException |
				NoSuchMethodException |
				SecurityException |
				InstantiationException |
				IllegalAccessException |
				IllegalArgumentException |
				InvocationTargetException e) {
			throw new NCIException("Exception constructing the device instance.", e);
		}
	}
}
