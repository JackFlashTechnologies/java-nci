package com.jackflashtech.nci.data;

import com.jackflashtech.nci.NCIException;
import com.jackflashtech.nci.Units;

/**
 * This represents the weight returned from the device. Generally, this means a
 * simple encapsulation of a value and the relevant units, and arithmetic
 * operations to combine instances of Weight with each other, potentially in
 * other units, may be called for in other versions. However, the lbs-oz units
 * represent a value in multiple parts (not a single number representing the
 * entire value, like using hours and minutes). These values are returned in
 * {@link #getLbs()} and {@link #getOz()} instead of {@link #getWeight()}.
 * Accessing these properties is an exception when the units are not
 * {@link Units#LBS_OZ}, and vice-versa. You can distinguish between these modes
 * with {@link #isLbsOz()}.
 * 
 * @author Jonathan Card
 *
 */
public class Weight {
	private double weight;
	private int lbs;
	private double oz;
	private Units units;
	
	/**
	 * The constructor for most systems, where the weight is represented by a
	 * single double value.
	 * 
	 * @param weight
	 * @param units
	 */
	public Weight(double weight, Units units) {
		this.weight = weight;
		this.units = units;
	}

	/**
	 * The constructor for the lbs-oz system. The weight is in two values, the
	 * lbs value must be an integer, and the units are assumed to be
	 * {@link Units#LBS_OZ}.
	 * 
	 * @param lbs
	 * @param oz
	 */
	public Weight(int lbs, double oz) {
		this.lbs = lbs;
		this.oz = oz;
		this.units = Units.LBS_OZ;
	}
	
	/**
	 * Call the function to distinguish between the modes in which the values in
	 * this object can be accessed. If the units are {@link Units#LBS_OZ}, use
	 * the accessors {@link #getLbs()} and {@link #getOz()}. Otherwise, use
	 * {@link #getWeight()}.
	 * 
	 * @return
	 */
	public boolean isLbsOz() {
		if (units == Units.LBS_OZ) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * The accessor for the weight represented by this class, in any units other
	 * than {@link Units#LBS_OZ}.
	 * 
	 * @return
	 * @throws NCIException	Thrown when the units are {@link Units#LBS_OZ}.
	 */
	public double getWeight() throws NCIException {
		if (units == Units.LBS_OZ) throw new NCIException("The weight is in lbs-oz. Retrieve the weight with the proper accessors.");
		return weight;
	}
	
	/**
	 * The accessor for the weight in pounds, when the units are
	 * {@link Units#LBS_OZ}. This does not include the remainder of the weight
	 * in ounces.
	 * 
	 * @return
	 * @throws NCIException	Thrown when the units are not {@link Units#LBS_OZ}
	 */
	public int getLbs() throws NCIException {
		if (units != Units.LBS_OZ) throw new NCIException("The weight is not in lbs-oz. Retrieve the weight with the proper accessors.");
		return lbs;
	}
	
	/**
	 * The access for the weight in ounces, when the units are
	 * {@link Units#LBS_OZ}. This does not include the majority of the weight in
	 * lbs.
	 * 
	 * @return
	 * @throws NCIException	Thrown when the units are not in {@link Units#LBS_OZ}
	 */
	public double getOz() throws NCIException {
		if (units != Units.LBS_OZ) throw new NCIException("The weight is not in lbs-oz. Retrieve the weight with the proper accessors.");
		return oz;
	}
	
	/**
	 * The units in which the weight is represented.
	 * 
	 * @return
	 */
	public Units getUnits() {
		return units;
	}
}
