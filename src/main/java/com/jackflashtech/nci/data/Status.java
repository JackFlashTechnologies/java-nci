package com.jackflashtech.nci.data;

import com.jackflashtech.nci.NCIDevice;

/**
 * This class represents the status of the device. It encapsulates a variety of
 * values reported from the device according to SCP-01 from Avery Weigh-tronix.
 * It supports a third status byte, and as of now does not distinguish between a
 * third byte not being reported (it is optional) and the values being sent. The
 * values {@link #getRange()}, {@link #getWeightType()}, and
 * {@link #isInitialZeroError()} are the values that may not have been reported.
 * 
 * There are no setters, as these values to not represent set-able values. The
 * scale's status can only be changed through the operations in
 * {@link NCIDevice}.
 * 
 * I do not include documentation for what the values individually mean, because
 * I do not entirely know. See the documentation of your device to see what your
 * device may mean by a given value.
 * 
 * @author Jonathan Card
 *
 */
public class Status {
	private boolean inMotion;
	private boolean scaleAtZero;
	private boolean RAMError;
	private boolean EEPROMError;
	private boolean underCapacity;
	private boolean overCapacity;
	private boolean ROMError;
	private boolean faultyCalibration;
	private Range range;
	private WeightType weightType;
	private boolean initialZeroError;
	
	/**
	 * This enum represents the possible values of {@link Status#getRange()}.
	 * 
	 * @author Jonathan Card
	 *
	 */
	public enum Range {
		LOW,
		HIGH;
	}
	
	/**
	 * This enum represents the possible values of {@link Status#getWeightType()}.
	 * 
	 * @author Jonathan Card
	 *
	 */
	public enum WeightType {
		NET,
		GROSS
	}

	/**
	 * Constructor for this data transport object (DTO). There is currently no
	 * support for some values not being provided.
	 * 
	 * @param inMotion
	 * @param scaleAtZero
	 * @param ramError
	 * @param eepromError
	 * @param underCapacity
	 * @param overCapacity
	 * @param romError
	 * @param faultyCalibration
	 * @param range
	 * @param weightType
	 * @param initialZeroError
	 */
	public Status(boolean inMotion, boolean scaleAtZero, boolean ramError, boolean eepromError, boolean underCapacity,
			boolean overCapacity, boolean romError, boolean faultyCalibration, Range range, WeightType weightType,
			boolean initialZeroError) {
		this.inMotion = inMotion;
		this.scaleAtZero = scaleAtZero;
		RAMError = ramError;
		EEPROMError = eepromError;
		this.underCapacity = underCapacity;
		this.overCapacity = overCapacity;
		ROMError = romError;
		this.faultyCalibration = faultyCalibration;
		this.range = range;
		this.weightType = weightType;
		this.initialZeroError = initialZeroError;
	}

	public boolean isInMotion() {
		return inMotion;
	}

	public boolean isScaleAtZero() {
		return scaleAtZero;
	}

	public boolean isRAMError() {
		return RAMError;
	}

	public boolean isEEPROMError() {
		return EEPROMError;
	}

	public boolean isUnderCapacity() {
		return underCapacity;
	}

	public boolean isOverCapacity() {
		return overCapacity;
	}

	public boolean isROMError() {
		return ROMError;
	}

	public boolean isFaultyCalibration() {
		return faultyCalibration;
	}

	public Range getRange() {
		return range;
	}

	public WeightType getWeightType() {
		return weightType;
	}

	public boolean isInitialZeroError() {
		return initialZeroError;
	}

	@Override
	public String toString() {
		return "In Motion: " + this.inMotion + "; At Zero: " + this.scaleAtZero + "; RAM Error: " + this.RAMError + "; EEPROM Error: " + this.EEPROMError + "; Under capacity: " + this.underCapacity + "; Over capacity: " + this.overCapacity + "; ROM Error: " + this.ROMError + "; Faulty calibration: " + this.faultyCalibration + "; Range: " + this.range + "; Weight Type: " + this.weightType + "; Initial zero error: " + this.initialZeroError;
	}
}
