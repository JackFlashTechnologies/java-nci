package com.jackflashtech.nci;

import com.jackflashtech.nci.data.Status;
import com.jackflashtech.nci.data.Weight;

public interface NCIDeviceListener {
	void deviceClosed();
	void receivedWeight(Weight weight);
	void receivedAbout();
	void receivedDiagnostics();
	void receviedMetrologyRawCounts();
	void updatedUnits(Units units);
	void updatedStatus(Status status);
}
