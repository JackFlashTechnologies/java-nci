package com.jackflashtech.nci.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jackflashtech.nci.NCIDevice;
import com.jackflashtech.nci.Units;
import com.jackflashtech.nci.data.Status;
import com.jackflashtech.nci.data.Weight;

public class NCIDeviceRxtxTestSystem {
	private NCIFactory factory = new NCIFactory();
	private NCIDevice device;
	
	@Before
	public void createDevice() throws Exception {
		device = factory.getDevice("test");
	}
	
	@After
	public void destroyDevice() throws Exception {
		device.closeDevice();
	}
	
	@Test
	public void retreieveWeightTest() throws Exception {		
		Weight weight = device.getWeight();
		System.out.println("Weight: " + weight.getWeight());
		System.out.println("Units: " + weight.getUnits());
		System.out.println("Status: " + device.getStatus());
		System.out.println();
	}
	
	@Test
	public void changeUnitsTest() throws Exception {
		Units units = device.changeUnitsOfMeasure();
		
		System.out.println("Units: " + units);
		System.out.println("Status: " + device.getStatus());
		System.out.println();
	}
	
	/*
	@Test
	public void requestMetrologyCountsTest() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(port);
		device.requestMetrologyRawCounts();
		
		fail("Don't know what to test for right now.");
	}*/
	
	/*
	@Test
	public void requestAboutTest() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(port);
		device.requestAbout();
		
		fail("Don't know what to ttest for right now.");
	}
	*/
	
	/*
	@Test
	public void requestDiagnosticsTest() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(port);
		device.requestDiagnostics();
		
		fail("Don't know what to test for right now.");
	}
	*/
	
	@Test
	public void tareTest() throws Exception {
		device.tare();
		Status status = device.getStatus();
		assertNotNull(status);
	}
	
	@Test
	public void zeroTest() throws Exception {
		device.zero();
		Status status = device.getStatus();
		assertNotNull(status);
	}
	
	@Test
	public void statusTest() throws Exception {
		Status status = device.requestStatus();
		assertNotNull(status);
		assertSame(status, device.getStatus());
	}
}
