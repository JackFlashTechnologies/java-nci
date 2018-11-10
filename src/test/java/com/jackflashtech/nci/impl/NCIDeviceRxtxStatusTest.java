package com.jackflashtech.nci.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import com.jackflashtech.nci.NCIException;
import com.jackflashtech.nci.data.Status;

public class NCIDeviceRxtxStatusTest {

	@Test
	public void testStatusSimple() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.SimpleStatusTransmissionState state = device.new SimpleStatusTransmissionState();
		byte firstByte = 1 << 4 | 1 << 5;
		byte secondByte = 1 << 4 | 1 << 5;
		byte[] inputMessage = 
			{0x0a, firstByte, secondByte, 0x0d, 0x03};
		state.parseInput(5, 0, inputMessage);
		Status status = device.status;
		assertFalse(status.isInMotion());
		assertFalse(status.isScaleAtZero());
		assertFalse(status.isRAMError());
		assertFalse(status.isEEPROMError());
		assertFalse(status.isUnderCapacity());
		assertFalse(status.isOverCapacity());
		assertFalse(status.isROMError());
		assertFalse(status.isFaultyCalibration());
		assertEquals(Status.Range.LOW, status.getRange());
		assertEquals(Status.WeightType.GROSS, status.getWeightType());
		assertFalse(status.isInitialZeroError());
	}
	
	@Test
	public void testStatusInMotion() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.SimpleStatusTransmissionState state = device.new SimpleStatusTransmissionState();
		byte firstByte = 1 << 0 | 1 << 4 | 1 << 5 | -128;
		byte secondByte = 1 << 4 | 1 << 5;
		byte[] inputMessage = 
			{0x0a, firstByte, secondByte, 0x0d, 0x03};
		state.parseInput(5, 0, inputMessage);
		Status status = device.status;
		assertTrue(status.isInMotion());
		assertFalse(status.isScaleAtZero());
		assertFalse(status.isRAMError());
		assertFalse(status.isEEPROMError());
		assertFalse(status.isUnderCapacity());
		assertFalse(status.isOverCapacity());
		assertFalse(status.isROMError());
		assertFalse(status.isFaultyCalibration());
		assertEquals(Status.Range.LOW, status.getRange());
		assertEquals(Status.WeightType.GROSS, status.getWeightType());
		assertFalse(status.isInitialZeroError());
	}
	
	@Test
	public void testStatusScaleAtZero() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.SimpleStatusTransmissionState state = device.new SimpleStatusTransmissionState();
		byte firstByte = 1 << 1 | 1 << 4 | 1 << 5 | -128;
		byte secondByte = 1 << 4 | 1 << 5;
		byte[] inputMessage = 
			{0x0a, firstByte, secondByte, 0x0d, 0x03};
		state.parseInput(5, 0, inputMessage);
		Status status = device.status;
		assertFalse(status.isInMotion());
		assertTrue(status.isScaleAtZero());
		assertFalse(status.isRAMError());
		assertFalse(status.isEEPROMError());
		assertFalse(status.isUnderCapacity());
		assertFalse(status.isOverCapacity());
		assertFalse(status.isROMError());
		assertFalse(status.isFaultyCalibration());
		assertEquals(Status.Range.LOW, status.getRange());
		assertEquals(Status.WeightType.GROSS, status.getWeightType());
		assertFalse(status.isInitialZeroError());
	}
	
	@Test
	public void testStatusRAMError() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.SimpleStatusTransmissionState state = device.new SimpleStatusTransmissionState();
		byte firstByte = 1 << 2 | 1 << 4 | 1 << 5 | -128;
		byte secondByte = 1 << 4 | 1 << 5;
		byte[] inputMessage = 
			{0x0a, firstByte, secondByte, 0x0d, 0x03};
		state.parseInput(5, 0, inputMessage);
		Status status = device.status;
		assertFalse(status.isInMotion());
		assertFalse(status.isScaleAtZero());
		assertTrue(status.isRAMError());
		assertFalse(status.isEEPROMError());
		assertFalse(status.isUnderCapacity());
		assertFalse(status.isOverCapacity());
		assertFalse(status.isROMError());
		assertFalse(status.isFaultyCalibration());
		assertEquals(Status.Range.LOW, status.getRange());
		assertEquals(Status.WeightType.GROSS, status.getWeightType());
		assertFalse(status.isInitialZeroError());
	}

	@Test
	public void testStatusEEPROMError() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.SimpleStatusTransmissionState state = device.new SimpleStatusTransmissionState();
		byte firstByte = 1 << 3 | 1 << 4 | 1 << 5 | -128;
		byte secondByte = 1 << 4 | 1 << 5;
		byte[] inputMessage = 
			{0x0a, firstByte, secondByte, 0x0d, 0x03};
		state.parseInput(5, 0, inputMessage);
		Status status = device.status;
		assertFalse(status.isInMotion());
		assertFalse(status.isScaleAtZero());
		assertFalse(status.isRAMError());
		assertTrue(status.isEEPROMError());
		assertFalse(status.isUnderCapacity());
		assertFalse(status.isOverCapacity());
		assertFalse(status.isROMError());
		assertFalse(status.isFaultyCalibration());
		assertEquals(Status.Range.LOW, status.getRange());
		assertEquals(Status.WeightType.GROSS, status.getWeightType());
		assertFalse(status.isInitialZeroError());
	}
	
	@Test
	public void testStatusUnderCapacity() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.SimpleStatusTransmissionState state = device.new SimpleStatusTransmissionState();
		byte firstByte = 1 << 4 | 1 << 5;
		byte secondByte = 1 << 0 | 1 << 4 | 1 << 5 | -128;
		byte[] inputMessage = 
			{0x0a, firstByte, secondByte, 0x0d, 0x03};
		state.parseInput(5, 0, inputMessage);
		Status status = device.status;
		assertFalse(status.isInMotion());
		assertFalse(status.isScaleAtZero());
		assertFalse(status.isRAMError());
		assertFalse(status.isEEPROMError());
		assertTrue(status.isUnderCapacity());
		assertFalse(status.isOverCapacity());
		assertFalse(status.isROMError());
		assertFalse(status.isFaultyCalibration());
		assertEquals(Status.Range.LOW, status.getRange());
		assertEquals(Status.WeightType.GROSS, status.getWeightType());
		assertFalse(status.isInitialZeroError());
	}

	@Test
	public void testStatusOverCapacity() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.SimpleStatusTransmissionState state = device.new SimpleStatusTransmissionState();
		byte firstByte = 1 << 4 | 1 << 5;
		byte secondByte = 1 << 1 | 1 << 4 | 1 << 5 | -128;
		byte[] inputMessage = 
			{0x0a, firstByte, secondByte, 0x0d, 0x03};
		state.parseInput(5, 0, inputMessage);
		Status status = device.status;
		assertFalse(status.isInMotion());
		assertFalse(status.isScaleAtZero());
		assertFalse(status.isRAMError());
		assertFalse(status.isEEPROMError());
		assertFalse(status.isUnderCapacity());
		assertTrue(status.isOverCapacity());
		assertFalse(status.isROMError());
		assertFalse(status.isFaultyCalibration());
		assertEquals(Status.Range.LOW, status.getRange());
		assertEquals(Status.WeightType.GROSS, status.getWeightType());
		assertFalse(status.isInitialZeroError());
	}
	
	@Test
	public void testStatusROMError() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.SimpleStatusTransmissionState state = device.new SimpleStatusTransmissionState();
		byte firstByte = 1 << 4 | 1 << 5;
		byte secondByte = 1 << 2 | 1 << 4 | 1 << 5 | -128;
		byte[] inputMessage = 
			{0x0a, firstByte, secondByte, 0x0d, 0x03};
		state.parseInput(5, 0, inputMessage);
		Status status = device.status;
		assertFalse(status.isInMotion());
		assertFalse(status.isScaleAtZero());
		assertFalse(status.isRAMError());
		assertFalse(status.isEEPROMError());
		assertFalse(status.isUnderCapacity());
		assertFalse(status.isOverCapacity());
		assertTrue(status.isROMError());
		assertFalse(status.isFaultyCalibration());
		assertEquals(Status.Range.LOW, status.getRange());
		assertEquals(Status.WeightType.GROSS, status.getWeightType());
		assertFalse(status.isInitialZeroError());
	}

	@Test
	public void testStatusFaultyCalibration() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.SimpleStatusTransmissionState state = device.new SimpleStatusTransmissionState();
		byte firstByte = 1 << 4 | 1 << 5;
		byte secondByte = 1 << 3 | 1 << 4 | 1 << 5 | -128;
		byte[] inputMessage = 
			{0x0a, firstByte, secondByte, 0x0d, 0x03};
		state.parseInput(5, 0, inputMessage);
		Status status = device.status;
		assertFalse(status.isInMotion());
		assertFalse(status.isScaleAtZero());
		assertFalse(status.isRAMError());
		assertFalse(status.isEEPROMError());
		assertFalse(status.isUnderCapacity());
		assertFalse(status.isOverCapacity());
		assertFalse(status.isROMError());
		assertTrue(status.isFaultyCalibration());
		assertEquals(Status.Range.LOW, status.getRange());
		assertEquals(Status.WeightType.GROSS, status.getWeightType());
		assertFalse(status.isInitialZeroError());
	}
	
	@Test
	public void testStatusHighRange() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.SimpleStatusTransmissionState state = device.new SimpleStatusTransmissionState();
		byte firstByte = 1 << 4 | 1 << 5;
		byte secondByte = 1 << 4 | 1 << 5 | 1 << 6 | -128;
		byte thirdByte = 3 | 1 << 4 | 1 << 5;
		byte[] inputMessage = 
			{0x0a, firstByte, secondByte, thirdByte, 0x0d, 0x03};
		state.parseInput(6, 0, inputMessage);
		Status status = device.status;
		assertFalse(status.isInMotion());
		assertFalse(status.isScaleAtZero());
		assertFalse(status.isRAMError());
		assertFalse(status.isEEPROMError());
		assertFalse(status.isUnderCapacity());
		assertFalse(status.isOverCapacity());
		assertFalse(status.isROMError());
		assertFalse(status.isFaultyCalibration());
		assertEquals(Status.Range.HIGH, status.getRange());
		assertEquals(Status.WeightType.GROSS, status.getWeightType());
		assertFalse(status.isInitialZeroError());
	}

	@Test
	public void testStatusNetWeight() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.SimpleStatusTransmissionState state = device.new SimpleStatusTransmissionState();
		byte firstByte = 1 << 4 | 1 << 5;
		byte secondByte = 1 << 4 | 1 << 5 | 1 << 6 | -128;
		byte thirdByte = 1 << 2 | 1 << 4 | 1 << 5 | -128;
		byte[] inputMessage = 
			{0x0a, firstByte, secondByte, thirdByte, 0x0d, 0x03};
		state.parseInput(6, 0, inputMessage);
		Status status = device.status;
		assertFalse(status.isInMotion());
		assertFalse(status.isScaleAtZero());
		assertFalse(status.isRAMError());
		assertFalse(status.isEEPROMError());
		assertFalse(status.isUnderCapacity());
		assertFalse(status.isOverCapacity());
		assertFalse(status.isROMError());
		assertFalse(status.isFaultyCalibration());
		assertEquals(Status.Range.LOW, status.getRange());
		assertEquals(Status.WeightType.NET, status.getWeightType());
		assertFalse(status.isInitialZeroError());
	}

	@Test
	public void testStatusInitialZeroError() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.SimpleStatusTransmissionState state = device.new SimpleStatusTransmissionState();
		byte firstByte = 1 << 4 | 1 << 5;
		byte secondByte = 1 << 4 | 1 << 5 | 1 << 6 | -128;
		byte thirdByte = 1 << 3 | 1 << 4 | 1 << 5 | -128;
		byte[] inputMessage = 
			{0x0a, firstByte, secondByte, thirdByte, 0x0d, 0x03};
		state.parseInput(6, 0, inputMessage);
		Status status = device.status;
		assertFalse(status.isInMotion());
		assertFalse(status.isScaleAtZero());
		assertFalse(status.isRAMError());
		assertFalse(status.isEEPROMError());
		assertFalse(status.isUnderCapacity());
		assertFalse(status.isOverCapacity());
		assertFalse(status.isROMError());
		assertFalse(status.isFaultyCalibration());
		assertEquals(Status.Range.LOW, status.getRange());
		assertEquals(Status.WeightType.GROSS, status.getWeightType());
		assertTrue(status.isInitialZeroError());
	}

	@Test
	public void testStatusRangeError1() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.SimpleStatusTransmissionState state = device.new SimpleStatusTransmissionState();
		byte firstByte = 1 << 4 | 1 << 5;
		byte secondByte = 1 << 4 | 1 << 5 | 1 << 6 | -128;
		byte thirdByte = 1 << 0 | 1 << 4 | 1 << 5 | -128;
		byte[] inputMessage = 
			{0x0a, firstByte, secondByte, thirdByte, 0x0d, 0x03};
		try {
			state.parseInput(6, 0, inputMessage);
			fail("Should have thrown an exception.");
		} catch (NCIException e) {
			System.out.println("Exception: " + e);
		}
		// TODO: Should this now have a status?
	}
	
	@Test
	public void testStatusRangeError2() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.SimpleStatusTransmissionState state = device.new SimpleStatusTransmissionState();
		byte firstByte = 1 << 4 | 1 << 5;
		byte secondByte = 1 << 4 | 1 << 5 | 1 << 6 | -128;
		byte thirdByte = 1 << 1 | 1 << 4 | 1 << 5 | -128;
		byte[] inputMessage = 
			{0x0a, firstByte, secondByte, thirdByte, 0x0d, 0x03};
		try {
			state.parseInput(6, 0, inputMessage);
			fail("Should have thrown an exception.");
		} catch (NCIException e) {
			System.out.println("Exception: " + e);
		}
	}

	@Test
	public void testMisformattedMessage() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.SimpleStatusTransmissionState state = device.new SimpleStatusTransmissionState();
		byte firstByte = 1 << 4 | 1 << 5;
		byte secondByte = 1 << 4 | 1 << 5 | 1 << 6 | -128;
		byte[] inputMessage = 
			{0x0a, firstByte, secondByte, 0x0d, 0x03};
		try {
			state.parseInput(5, 0, inputMessage);
			fail("Bad follows byte in second bit did not throw an exception.");
		} catch (NCIException e) {
			System.out.println("Exception: " + e);
		}
	}
}
