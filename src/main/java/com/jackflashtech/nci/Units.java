package com.jackflashtech.nci;

/**
 * This enum represents the supported values of units used to report the weight.
 * It can be used in both the {@link com.jackflashtech.nci.data.Weight} class or
 * on its own reporting what units the scale is now reporting values after
 * calling {@link NCIDevice#changeUnitsOfMeasure()}.
 * 
 * @author Jonathan Card
 *
 */
public enum Units {
	LBS,
	LBS_OZ,
	OZ,
	KG,
	G,
	OTHER;
}
