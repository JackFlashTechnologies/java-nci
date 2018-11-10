package com.jackflashtech.nci;

import java.util.TooManyListenersException;

import com.jackflashtech.nci.data.Status;
import com.jackflashtech.nci.data.Weight;

/**
 * The abstract representation of a device, such as the Brecknell 6710U.
 * Instances of this are constructed through the
 * {@link com.jackflashtech.nci.impl.NCIFactory}.
 * 
 * @author Jonathan Card
 *
 */
public interface NCIDevice {
	/**
	 * Use this method to add a listener for asynchronous methods.
	 * 
	 * @param listener
	 */
	public void addDeviceListener(NCIDeviceListener listener) throws TooManyListenersException;
	
	/**
	 * A synchronous method to retrieve the weight from the NCI device.
	 * 
	 * @return The weight currently being registered by the device.
	 * 
	 */
	public Weight getWeight() throws NCIException;
	
	public Weight getHighResolutionWeight() throws NCIException;
	
	public Units changeUnitsOfMeasure() throws NCIException;
	
	// This is not well tested. The return value in particular should be a collection of relevant strings. But the scale I'm developing this for doesn't support this function.
	public void requestMetrologyRawCounts() throws NCIException;

	// This is not well tested. The return value in particular should be a collection of relevant strings. But the scale I'm developing this for doesn't support this function.
	public void requestAbout() throws NCIException;
	
	public void requestDiagnostics() throws NCIException;
	
	/**
	 * This requests a new status from the device and returns it. The status is
	 * also stored to be retrieved by {@link #getStatus()}, like other
	 * communications.
	 * 
	 * @return
	 * @throws NCIException
	 */
	public Status requestStatus() throws NCIException;
	
	/**
	 * Sets the tare on the device, making the returned weight net rather than gross.
	 * 
	 * @throws NCIException
	 */
	public void tare() throws NCIException;
	
	/**
	 * Reset the device to using the natural 0, making the returned weight gross rather than net.
	 */
	public void zero() throws NCIException;
	
	/**
	 * This returns the status as retrieved from the last successful
	 * communication with the device. This does not supply a new status. If you
	 * require a new status update, use {@link #requestState()}.
	 *
	 * @return
	 */
	public Status getStatus();
	
	public void closeDevice() throws NCIException;
}
