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
	 * @param listener	A listener to add to this device to support asynchronous methods.
	 * @throws	TooManyListenersException An exception that indicates that this device does not support more listeners.
	 */
	public void addDeviceListener(NCIDeviceListener listener) throws TooManyListenersException;
	
	/**
	 * A synchronous method to retrieve the weight from the NCI device.
	 * 
	 * @return The weight currently being registered by the device.
	 * @throws	NCIException	This is thrown in the event that some exception happened during the request for the weight.
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
	 * @return	The status requested from the device.
	 * @throws NCIException	This is thrown in the event that some exception happened during the request for the status.
	 */
	public Status requestStatus() throws NCIException;
	
	/**
	 * Sets the tare on the device, making the returned weight net rather than gross.
	 * 
	 * @throws NCIException	This is thrown in the event that some exception happened during the request that the scale sets the tare.
	 */
	public void tare() throws NCIException;
	
	/**
	 * Reset the device to using the natural 0, making the returned weight gross rather than net.
	 * 
	 * @throws NCIException This is thrown in the event that some exception happened during the request that the scale sets the zero.
	 */
	public void zero() throws NCIException;
	
	/**
	 * This returns the status as retrieved from the last successful
	 * communication with the device. This does not supply a new status. If you
	 * require a new status update, use {@link #requestStatus()}.
	 *
	 * @return	The status reported during the most recent communication with the device. This may be null if there was an exception in the most recent communication.
	 */
	public Status getStatus();
	
	public void closeDevice() throws NCIException;
}
