package com.jackflashtech.nci.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import com.jackflashtech.nci.NCIException;
import com.jackflashtech.nci.Units;

// The tests here are illegal messages, as I do not send status messages. Status
//  messages are tested elsewhere.
public class NCIDeviceRxtxWeightTest {

	@Test
	public void testWeightParsingSimple() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.WeightTransmissionState state = device.new WeightTransmissionState(null);
		byte[] inputMessage = 
			{0x0a, '-', '1', '0', '0', 'k', 'g', 0x0d};
		state.parseInput(8, 0, inputMessage);
		assertEquals("Weight was parsed wrong.", -100.0, device.weight.getWeight(), 0.01);
		assertEquals("Units were parsed wrong.", Units.KG, device.weight.getUnits());
	}
	
	@Test
	public void testWeightMissingUnits() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.WeightTransmissionState state = device.new WeightTransmissionState(null);
		byte [] inputMessage =
			{0x0a, '-', '1', '0', '0', 0x0d};
		try {
			state.parseInput(6, 0, inputMessage);
			fail("Did not throw an exception.");
		} catch (NCIException e) {
			System.out.println(e);
		}
	}
	
	@Test
	public void testWeightMissingValue() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.WeightTransmissionState state = device.new WeightTransmissionState(null);
		byte[] inputMessage =
			{0x0a, '-', 'k', 'g', 0x0d};
		try {
			state.parseInput(5, 0, inputMessage);
			fail("Did not throw an exception.");
		} catch (NCIException e) {
			System.out.println(e);
		}
	}

	@Test
	public void testWeightNoContent() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.WeightTransmissionState state = device.new WeightTransmissionState(null);
		byte[] inputMessage =
			{0x0a, 0x0d};
		try {
			state.parseInput(2, 0, inputMessage);
			fail("Did not throw an exception.");
		} catch (NCIException e) {
			System.out.println(e);
		}
	}
	
	@Test
	public void testWeightNoCR() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.WeightTransmissionState state = device.new WeightTransmissionState(null);
		byte[] inputMessage =
			{0x0a, '-', '1', '0', '0', 'k', 'g', 0x03};
		try {
			state.parseInput(8, 0, inputMessage);
			fail("Did not throw an exception.");
		} catch (NCIException e) {
			System.out.println(e);
		}
	}
	
	@Test
	public void testWeightGarbage() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.WeightTransmissionState state = device.new WeightTransmissionState(null);
		byte[] inputMessage =
			{0x0a, 'A', '1', 'F', '7', 'k', 'g'};
		try {
			state.parseInput(7, 0, inputMessage);
			fail("Did not throw an exception.");
		} catch (NCIException e) {
			System.out.println(e);
		}
	}
	
	@Test
	public void testWeightSpaceNoMinus() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.WeightTransmissionState state = device.new WeightTransmissionState(null);
		byte[] inputMessage =
			{0x0a, ' ', '1', '0', '0', 'k', 'g', 0x0d};
		state.parseInput(8, 0, inputMessage);
		assertEquals("Weight was parsed wrong.", 100.0, device.weight.getWeight(), 0.01);
		assertEquals("Units were parsed wrong.", Units.KG, device.weight.getUnits());
	}
	
	@Test
	public void testWeightSpaceAfterGrams() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.WeightTransmissionState state = device.new WeightTransmissionState(null);
		byte[] inputMessage =
			{0x0a, '1', '0', '0', 'g', ' ', 0x0d};
		state.parseInput(8, 0, inputMessage);
		assertEquals("Weight was parsed wrong.", 100.0, device.weight.getWeight(), 0.01);
		assertEquals("Units were parsed wrong.", Units.G, device.weight.getUnits());
	}
	
	@Test
	public void testWeightSpaceBeforeGrams() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.WeightTransmissionState state = device.new WeightTransmissionState(null);
		byte[] inputMessage =
			{0x0a, '1', '0', '0', ' ', 'g', 0x0d};
		state.parseInput(8, 0, inputMessage);
		assertEquals("Weight was parsed wrong.", 100.0, device.weight.getWeight(), 0.01);
		assertEquals("Units were parsed wrong.", Units.G, device.weight.getUnits());
	}
	
	@Test
	public void testWeightNoSpaceAroundGrams() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.WeightTransmissionState state = device.new WeightTransmissionState(null);
		byte[] inputMessage =
			{0x0a, '1', '0', '0', 'g', 0x0d};
		state.parseInput(8, 0, inputMessage);
		assertEquals("Weight was parsed wrong.", 100.0, device.weight.getWeight(), 0.01);
		assertEquals("Units were parsed wrong.", Units.G, device.weight.getUnits());
	}
	
	@Test
	public void testWeightLbsOzWithSpace() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.WeightTransmissionState state = device.new WeightTransmissionState(null);
		byte[] inputMessage =
			{0x0a, '1', '0', '0', 'l', 'b', ' ', '5', '2', '.', '6', '0', 'o', 'z', 0x0d};
		state.parseInput(15, 0, inputMessage);
		assertTrue("Weight was in the wrong units.", device.weight.isLbsOz());
		assertEquals("Pounds was parsed wrong.", 100, device.weight.getLbs());
		assertEquals("Oz was parsed wrong.", 52.60, device.weight.getOz(), 0.01);
		assertEquals("Units were parsed wrong.", Units.LBS_OZ, device.weight.getUnits());
	}
	
	@Test
	public void testWeightLbsOzNoSpace() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.WeightTransmissionState state = device.new WeightTransmissionState(null);
		byte[] inputMessage =
			{0x0a, '1', '0', '0', 'l', 'b', '5', '2', '.', '6', '0', 'o', 'z', 0x0d};
		state.parseInput(14, 0, inputMessage);
		assertTrue("Weight was in the wrong units.", device.weight.isLbsOz());
		assertEquals("Pounds was parsed wrong.", 100, device.weight.getLbs());
		assertEquals("Oz was parsed wrong.", 52.60, device.weight.getOz(), 0.01);
		assertEquals("Units were parsed wrong.", Units.LBS_OZ, device.weight.getUnits());
	}
	
	@Test
	public void testWeightOzOnly() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.WeightTransmissionState state = device.new WeightTransmissionState(null);
		byte[] inputMessage =
			{0x0a, '5', '2', '.', '6', '0', 'o', 'z', 0x0d};
		state.parseInput(9, 0, inputMessage);
		assertEquals("Weight was parsed wrong.", 52.6, device.weight.getWeight(), 0.01);
		assertEquals("Units were parsed wrong.", Units.OZ, device.weight.getUnits());
	}
	
	@Test
	public void testWeightLbsOzPsychOut1() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.WeightTransmissionState state = device.new WeightTransmissionState(null);
		byte[] inputMessage =
			{0x0a, '1', '0', '0', 'f', 'o', 'o', '5', '2', '.', '6', '0', 'o', 'z', 0x0d};
		state.parseInput(15, 0, inputMessage);
		// TODO: This may be wrong, but it seems like an awfully specific scenario.
		assertTrue("Weight was in the wrong units.", device.weight.isLbsOz());
		assertEquals("Pounds was parsed wrong.", 100, device.weight.getLbs());
		assertEquals("Oz was parsed wrong.", 52.60, device.weight.getOz(), 0.01);
		assertEquals("Units were parsed wrong.", Units.LBS_OZ, device.weight.getUnits());
	}
	
	@Test
	public void testWeightLbsOzPsychOut2() throws Exception {
		NCIDeviceRxtx device = new NCIDeviceRxtx(true);
		NCIDeviceRxtx.WeightTransmissionState state = device.new WeightTransmissionState(null);
		byte[] inputMessage =
			{0x0a, 'f', 'o', 'o', '5', '2', '.', '6', '0', 'o', 'z', 0x0d};
		try {
			state.parseInput(15, 0, inputMessage);
			fail("Should have thrown an exception.");
		} catch (NCIException e) {
			System.out.println(e);
		}
	}

}
