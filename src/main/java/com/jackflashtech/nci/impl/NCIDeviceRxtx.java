package com.jackflashtech.nci.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TooManyListenersException;

import com.jackflashtech.nci.NCIDevice;
import com.jackflashtech.nci.NCIDeviceListener;
import com.jackflashtech.nci.NCIException;
import com.jackflashtech.nci.Units;
import com.jackflashtech.nci.data.Status;
import com.jackflashtech.nci.data.Weight;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

// TODO: If a synchronous method is called, then times out, then another
// function is called, then the original function returns, the new state will be
// parsed as it if it is the response to the new call. I do not see a valid way
// to respond to this, with this architecture or any other.
public class NCIDeviceRxtx implements NCIDevice, SerialPortEventListener {
	/**
	 * This is the timeout when waiting to get the COM port.
	 */
	private static final int COMM_TIMEOUT = 2000;
	/**
	 * This is the timeout when waiting for a response from the device.
	 */
	private final static int TIMEOUT = 2000;
	/**
	 * The allocated length of the message from the device. This is almost
	 * certainly too small to support advanced messages like from Request About
	 * or Request Diag.
	 */
	private final static int INPUT_BUFFER_LENGTH = 30;
	
	/**
	 * This is a {@link Map} of the String representation of the units to the
	 * representation of the units in {@link Units}. It is cached here to speed
	 * up resolution. Representing units with an enum instead of a String makes
	 * the driver less stable to devices supporting units differently, but it
	 * solves issues of capitalization or other issues when working across
	 * different systems. This lookup can grow until it supports everything
	 * (which I anticipate happening easily and quicly), and then measurements
	 * across devices can be compared more easily on the part of the developer
	 * using this library.
	 */
	private final static Map<String, Units> UNITS_LOOKUP = new HashMap<String, Units>();
	{
		UNITS_LOOKUP.put("kg", Units.KG);
		UNITS_LOOKUP.put("lb", Units.LBS);
		UNITS_LOOKUP.put("g", Units.G);
		UNITS_LOOKUP.put("oz", Units.OZ);
	}
	
	// These are constructed on start-up because all of the states are supposed
	// to be stateless. Consider making instances of
	// SynchronousTransmissionState and AsynchronousTransmissionState cached
	// like this; I didn't only because having a cached version of
	// SynchronousTransmissionState and setting the internal state would be
	// technically stateful and it seemed like an opportunity for confusion.
	private final ITransmissionState STATUS_STATE = new SimpleStatusTransmissionState();
	private final ITransmissionState WEIGHT_STATE = new GeneralTransmissionState(new WeightTransmissionState(STATUS_STATE));
	private final ITransmissionState UNITS_STATE = new GeneralTransmissionState(new UnitsTransmissionState(STATUS_STATE));
	// TODO: I did not create a metrology state. I was going to reuse the WeightTransmissionState until I figured it out, and that never completed because my scale doesn't implement this.
	private final ITransmissionState ABOUT_STATE = new GeneralTransmissionState(new AboutTransmissionState(STATUS_STATE));
	private final ITransmissionState DIAGNOSTICS_STATE = new GeneralTransmissionState(new DiagnosticsTransmissionState(STATUS_STATE));
	
	private SerialPort port;
	private IPrimaryState currentState = null;
	boolean checkParity;
	NCIDeviceListener listener = null;

	// These are holding values for coordinating between the threads that call in and out of this class.
	volatile Weight weight = null;
	volatile Units units = null;
	volatile NCIException transmissionException;
	volatile Weight metrology = null;
	// TODO: Presumably, there will be other DTOs for the About, Diagnostic, Metrology states, etc.
	volatile Status status = null;
	
	/**
	 * Do not use this. It is only for running unit tests.
	 * 
	 * @throws NCIException
	 */
	NCIDeviceRxtx(boolean checkParity) throws NCIException {
		this.checkParity = checkParity;
	}
	
	NCIDeviceRxtx(Properties deviceProperties, String name) throws NCIException {
		String portName = null;
		try {
			portName = deviceProperties.getProperty("nci." + name + ".commport");
			String autoDiscovery = deviceProperties.getProperty("nci." + name + ".autodiscovery");
			// TODO: If autoDiscovery is provided but not parseable, then this is an error.
			// TODO: if autoDiscovery is provided but false, then this is nonsense.
			if (portName == null && autoDiscovery == null) throw new NCIException("No port name found.");
			String checkParityString = deviceProperties.getProperty("nci." + name + ".checkparity");
			if (checkParityString == null) throw new NCIException("Did not find a value for checkparity.");
			
			if (portName != null) {
				CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(portName);
				this.port = (SerialPort) portId.open("NCI Driver: " + name, COMM_TIMEOUT);
				port.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				port.setDTR(false);
				port.setRTS(false);
				port.addEventListener(this);
				port.notifyOnDataAvailable(true);
			} else if(Boolean.parseBoolean(autoDiscovery)) { // TODO: Realistically, there should be a better test that autoDiscovery is parseable and isn't an error.
				Enumeration<?> ids = CommPortIdentifier.getPortIdentifiers();
				while (ids.hasMoreElements()) {
					CommPortIdentifier id = (CommPortIdentifier)ids.nextElement();
					if (!id.isCurrentlyOwned()) {
						SerialPort port = (SerialPort)id.open("NCI Driver: " + name,  COMM_TIMEOUT);
						//port.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
						port.setDTR(false);
						port.setRTS(false);
						port.addEventListener(this);
						port.notifyOnDataAvailable(true);
						this.port = port;
						Status status;
						try {
							status = this.requestStatus();
							if (status == null) {
								this.port.close();
								this.port = null;
							} else {
								break;
							}
						} catch (NCIException e) {
							this.port.close();
							this.port = null;
						}
					}
				}
				if (this.port == null) throw new NCIException("No devices were found that correctly responded as NCI devices.");
			} else {
				// TODO: There should be an error condition here.
			}
			this.checkParity = Boolean.parseBoolean(checkParityString);	
		} catch (NoSuchPortException e) {
			throw new NCIException("There is no COM port named " + portName + ".", e);
		} catch (PortInUseException e) {
			throw new NCIException("COM port named " + portName + " is in use.", e);
		} catch (TooManyListenersException e) {
			this.port.close();
			throw new NCIException("The underlying port will not accept more listeners, so this device object will not work.", e);
		} catch (UnsupportedCommOperationException e) {
			this.port.close();
			throw new NCIException("Bonkers.");
		}
	}

	public void addDeviceListener(NCIDeviceListener listener) throws TooManyListenersException {
		if (listener != null) throw new TooManyListenersException();
		this.listener = listener;
	}

	public Weight getWeight() throws NCIException {
		Weight returnValue;
		this.weight = null;
		this.transmissionException = null;
		
		if (this.currentState != null) throw new NCIException("This device is in the middle of a communication and does not support concurrent operations.");
		synchronized (this) {
			byte[] outputMessage = {'W', 0x0d};
			try {
				this.port.getOutputStream().write(outputMessage);
				this.port.getOutputStream().flush();
				this.currentState = new SynchronousTransmissionState(WEIGHT_STATE);
				this.wait(TIMEOUT);
				if (this.transmissionException != null) throw this.transmissionException;
				if (weight == null) {
					throw new NCIException("There was a timeout or a failure to parse the response. No weight available.");
				}
				returnValue = this.weight;
			} catch (IOException | InterruptedException e) {
				throw new NCIException(e);
			} finally {
				this.weight = null;
				this.transmissionException = null;
				this.currentState = null;
			}
		}

		return returnValue;
	}
	
	@Override
	public Weight getHighResolutionWeight() throws NCIException {
		Weight returnValue;
		this.weight = null;
		
		if (this.currentState != null) throw new NCIException("This device is in the middle of a communication and does not support concurrent operations.");
		synchronized (this) {
			byte[] outputMessage = {'H', 0x0d};
			try {
				this.port.getOutputStream().write(outputMessage);
				this.port.getOutputStream().flush();
				this.currentState = new SynchronousTransmissionState(WEIGHT_STATE);
				this.wait(TIMEOUT);
				if (this.transmissionException != null) throw this.transmissionException;
				if (weight == null) {
					throw new NCIException("There was a timeout or a failure to parse the response. No weight available.");
				}
				returnValue = this.weight;
			} catch (IOException | InterruptedException e) {
				throw new NCIException(e);
			} finally {
				this.weight = null;
				this.transmissionException = null;
				this.currentState = null;
			}
		}

		return returnValue;
	}

	@Override
	public Units changeUnitsOfMeasure() throws NCIException {
		Units returnValue;
		this.units = null;
		
		if (this.currentState != null) throw new NCIException("This device is in the middle of a communication and does not support concurrent operations.");
		synchronized (this) {
			byte[] outputMessage = {'U', 0x0d};
			try {
				this.port.getOutputStream().write(outputMessage);
				this.port.getOutputStream().flush();
				this.currentState = new SynchronousTransmissionState(UNITS_STATE);
				this.wait(TIMEOUT);
				if (this.transmissionException != null) throw this.transmissionException;
				if (this.units == null) {
					throw new NCIException("There was a timeout or a failure to parse the response. The change of units is unknown.");
				}
				returnValue = this.units;
			} catch (IOException | InterruptedException e) {
				throw new NCIException(e);
			} finally {
				this.units = null;
				this.transmissionException = null;
				this.currentState = null;
			}
		}

		return returnValue;
	}

	// TODO: This is not right. It got half-implemented and it turns out this scale does not support it.
	@Override
	public void requestMetrologyRawCounts() throws NCIException {
		this.units = null;
		
		if (this.currentState != null) throw new NCIException("This device is in the middle of a communication and does not support concurrent operations.");
		synchronized (this) {
			byte[] outputMessage = {'M', 0x0d};
			try {
				OutputStream os = this.port.getOutputStream();
				os.write(outputMessage);
				os.flush();
				this.currentState = new SynchronousTransmissionState(WEIGHT_STATE);
				this.wait(TIMEOUT);
				if (this.transmissionException != null) throw this.transmissionException;
				if (this.metrology == null) {
					throw new NCIException("There was a timeout or a failure to parse the response. The request for metrology counts failed.");
				}
			} catch (IOException | InterruptedException e) {
				throw new NCIException(e);
			} finally {
				this.transmissionException = null;
				this.currentState = null;
			}
		}
		//Weight returnValue = this.metrology;
		//this.units = null;
		//return returnValue;
	}

	// This is not well tested. The return value in particular should be a collection of relevant strings. But the scale I'm developing this for doesn't support this function.
	@Override
	public void requestAbout() throws NCIException {
		this.units = null;
		
		if (this.currentState != null) throw new NCIException("This device is in the middle of a communication and does not support concurrent operations.");
		synchronized (this) {
			byte[] outputMessage = {'A', 0x0d};
			try {
				OutputStream os = this.port.getOutputStream();
				os.write(outputMessage);
				os.flush();
				this.currentState = new SynchronousTransmissionState(ABOUT_STATE);
				this.wait(TIMEOUT);
				if (this.transmissionException != null) throw this.transmissionException;
				if (this.metrology == null) {
					throw new NCIException("There was a timeout or a failure to parse the response. The request for metrology counts failed.");
				}
			} catch (IOException | InterruptedException e) {
				throw new NCIException(e);
			} finally {
				this.transmissionException = null;
				this.currentState = null;
			}
		}
		//Weight returnValue = this.metrology;
		//this.units = null;
		//return returnValue;
	}

	@Override
	public void requestDiagnostics() throws NCIException {
		this.units = null;
		
		if (this.currentState != null) throw new NCIException("This device is in the middle of a communication and does not support concurrent operations.");
		synchronized (this) {
			byte[] outputMessage = {'D', 0x0d};
			try {
				OutputStream os = this.port.getOutputStream();
				os.write(outputMessage);
				os.flush();
				this.currentState = new SynchronousTransmissionState(DIAGNOSTICS_STATE);
				this.wait(TIMEOUT);
				if (this.transmissionException != null) throw this.transmissionException;
				if (this.metrology == null) {
					throw new NCIException("There was a timeout or a failure to parse the response. The request for metrology counts failed.");
				}
			} catch (IOException | InterruptedException e) {
				throw new NCIException(e);
			} finally {
				this.transmissionException = null;
				this.currentState = null;
			}
		}
		//Weight returnValue = this.metrology;
		//this.units = null;
		//return returnValue;
	}
	
	@Override
	public Status requestStatus() throws NCIException {
		if (this.currentState != null) throw new NCIException("This device is in the middle of a communication and does not support concurrent operations.");
		synchronized (this) {
			this.status = null;
			byte[] outputMessage = {'S', 0x0d};
			try {
				OutputStream os = this.port.getOutputStream();
				os.write(outputMessage);
				os.flush();
				this.currentState = new SynchronousTransmissionState(STATUS_STATE);
				this.wait(TIMEOUT);
				if (this.transmissionException != null) throw this.transmissionException;
				if (this.status == null) {
					throw new NCIException("There was a timeout or a failure to parse the response. The request for a new status failed.");
				}
			} catch (IOException | InterruptedException e) {
				throw new NCIException(e);
			} finally {
				this.transmissionException = null;
				this.currentState = null;
			}
		}
		return this.status;
	}

	public void tare() throws NCIException {
		this.weight = null;
		
		if (this.currentState != null) throw new NCIException("This device is in the middle of a communication and does not support concurrent operations.");
		synchronized (this) {
			this.status = null;
			byte[] outputMessage = {'T', 0x0d};
			try {
				OutputStream os = this.port.getOutputStream();
				os.write(outputMessage);
				os.flush();
				this.currentState = new SynchronousTransmissionState(STATUS_STATE);
				this.wait(TIMEOUT);
				if (this.transmissionException != null) throw this.transmissionException;
				if (this.status == null) {
					throw new NCIException("There was a timeout or a failure to parse the response. The request for a new status failed.");
				}
			} catch (IOException | InterruptedException e) {
				throw new NCIException(e);
			} finally {
				this.transmissionException = null;
				this.currentState = null;
			}
		}
	}

	public void zero() throws NCIException {
		this.weight = null;
		
		if (this.currentState != null) throw new NCIException("This device is in the middle of a communication and does not support concurrent operations.");
		synchronized (this) {
			byte[] outputMessage = {'Z', 0x0d};
			try {
				OutputStream os = this.port.getOutputStream();
				os.write(outputMessage);
				os.flush();
				this.currentState = new SynchronousTransmissionState(STATUS_STATE);
				this.wait(TIMEOUT);
				if (this.transmissionException != null) throw this.transmissionException;
			} catch (IOException | InterruptedException e) {
				throw new NCIException(e);
			} finally {
				this.transmissionException = null;
				this.currentState = null;
			}
		}
	}
	
	public void closeDevice() throws NCIException {
		port.removeEventListener();
		this.port.close();	
	}

	public void serialEvent(SerialPortEvent event) {
		synchronized (this) {
			if (this.currentState != null) {
				switch (event.getEventType()) {
				case SerialPortEvent.BI:
				case SerialPortEvent.OE:
				case SerialPortEvent.FE:
				case SerialPortEvent.PE:
				case SerialPortEvent.CD:
				case SerialPortEvent.CTS:
				case SerialPortEvent.DSR:
				case SerialPortEvent.RI:
				case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
					break;
				case SerialPortEvent.DATA_AVAILABLE:
					this.currentState.parseInput();
				}

				this.currentState = null;
			} else {
				// Throwing away data. There doesn't seem to be anyway to know what
				// else to do; presumably this happened because the wait() call was
				// interrupted and I've already thrown an exception, but the data
				// came back anyway.
				try {
					InputStream is = NCIDeviceRxtx.this.port.getInputStream();
					while(is.read() > 0) {}
				} catch (IOException e) {
					// Nothing really to be done here. There is no state, so the
					// synchronous or asynchronous nature is unknown.
				}
			}
		}
	}
	
	/**
	 * The status of the device as reported by the last communication. This does
	 * not initiate a new connection to fetch the status. 
	 */
	public Status getStatus() {
		return status;
	}
		
	public interface ITransmissionState {
		void parseInput(int bytesRead, int startByte, byte[] inputMessage) throws NCIException;
	}
	
	public abstract class WrapperTransmissionState implements ITransmissionState {
		protected ITransmissionState internalState;
		
		public WrapperTransmissionState(ITransmissionState state) {
			this.internalState = state;
		}		
	}
	
	public class GeneralTransmissionState extends WrapperTransmissionState {
		public GeneralTransmissionState(ITransmissionState state) {
			super(state);
		}
		
		public void parseInput(int bytesRead, int startByte, byte[] inputMessage) throws NCIException {
			if (bytesRead <= startByte) {
				throw new NCIException("No bytes were read, though no error was thrown.");
			} 
			if (inputMessage[0] != 0x0a) {
				throw new NCIException("The first byte is supposed to be LF.");
			}
			if (inputMessage[1] == '?') {
				throw new NCIException("This function is not supported on this scale.");
			}
			this.internalState.parseInput(bytesRead, 1, inputMessage);
		}
	}
	
	public class WeightTransmissionState extends WrapperTransmissionState {
		public WeightTransmissionState(ITransmissionState state) {
			super(state);
		}

		public void parseInput(int bytesRead, int startByte, byte[] inputMessage) throws NCIException {
			int endByte = 0;
			for (int i = startByte; i < bytesRead; i++) {
				if (inputMessage[i] == 0x0d) {
					endByte = i - 1;
					break;
				}
			}
			if (endByte == 0) // This means there was no content between the <LF> and <CR>.
				throw new NCIException("Message was misformatted and no weight information was found.");
			// assert: endByte is the index of the last byte of the weight plus the units. It is not the index of the <CR>.
				
			int lastNumber = 0;
			for (int j = endByte; j > 0; j--) {
				if (inputMessage[j] >= 0x30 && inputMessage[j] <= 0x39) {
					lastNumber = j;
					break;
				}
			}
			if (lastNumber == 0)
				throw new NCIException("Message was misformatted and the weight was not reported with any numerals.");
			// assert: lastNumber is the index of the last digit found in the bytes. As the number starts with 1, it is also the length of the numerical content, and can be used to compute the coordinates of the units. In the exceptional case that there were no units, lastNumber + 1 will be the <CR> and the length will be 0, so unitsWeight will be "" and the error condition will be reported later.
			
			String unitsString = (new String(inputMessage, lastNumber + 1, endByte - lastNumber)).trim();
			
			boolean lbsOz = false;
			int additionalUnitsEndByte = 0;
			if (unitsString.equals("oz")) { // I think it's possible to be in oz, but not lbs-oz?
				for (int k = lastNumber; k > 0; k--) {
					if ((inputMessage[k] < 0x30 || inputMessage[k] > 0x39) && inputMessage[k] != '.' && inputMessage[k] != ' ' && inputMessage[k] != '-') {
						additionalUnitsEndByte = k;
						lbsOz = true;
						break;
					}
				}
			}
			
			// Not sure how worth it this is; this scale doesn't support lbs-oz, I think.
			if (lbsOz == true) {
				int additionalUnitsLastNumber = 0;
				for (int l = additionalUnitsEndByte; l > 0; l--) {
					if (inputMessage[l] >= 0x30 && inputMessage[l] <= 0x39) {
						additionalUnitsLastNumber = l;
						break;
					}
				}
				if (additionalUnitsLastNumber == 0) throw new NCIException("No numerical value provided for lbs in lbs-oz mode.");

				String lbsString = new String(inputMessage, 1, additionalUnitsLastNumber);
				String ozString = new String(inputMessage, additionalUnitsEndByte + 1, lastNumber - additionalUnitsEndByte);
				int lbs;
				double oz;
				try {
					lbs = Integer.parseInt(lbsString);
				} catch (NumberFormatException e) {
					throw new NCIException("Exception parsing the weight in lbs: " + lbsString, e);
				}
				try {
					oz = Double.parseDouble(ozString);
				} catch (NumberFormatException e) {
					throw new NCIException("Exception parsing the weight in oz: " + ozString, e);
				}
				
				NCIDeviceRxtx.this.weight = new Weight(lbs, oz);
			} else {
				Units units = UNITS_LOOKUP.get(unitsString);
				if (units == null) throw new NCIException("Units not recognized by this driver: " + unitsString);

				String weightString = new String(inputMessage, 1, lastNumber);
				double weight;
				try {
					weight = Double.parseDouble(weightString);
				} catch (NumberFormatException e) {
					throw new NCIException("Exception parsing weight: " + weightString, e);
				}
				
				NCIDeviceRxtx.this.weight = new Weight(weight, units);

			}
			
			if (this.internalState != null) {
				int newStartByte = endByte + 2; // The justification for this being +2 is that the <CR> was detected already, and the start of parsing should be at +2.
				if (newStartByte >= bytesRead) throw new NCIException("Parser configuration indicates more data should be expected, but not enough bytes were read. Aborting.");
				this.internalState.parseInput(bytesRead, newStartByte, inputMessage);
			}
		}
	}
	
	public class UnitsTransmissionState extends WrapperTransmissionState {
		public UnitsTransmissionState(ITransmissionState state) {
			super(state);
		}

		public void parseInput(int bytesRead, int startByte, byte[] inputMessage) throws NCIException {
			int endByte = 0;
			for (int i = startByte; i < bytesRead; i++) {
				if (inputMessage[i] == 0x0d) {
					endByte = i - 1;
					break;
				}
			}
			if (endByte == bytesRead) // This is suspicious; perhaps it should go back for more bytes, but we're going to throw an exception.
				throw new NCIException("Message was misformatted and the weight not parsed.");
			if (endByte == 0) // This means there was no content between the <LF> and <CR>.
				throw new NCIException("Message was misformatted and no weight information was found.");
			// assert: endByte is the index of the last byte of the units. It is not the index of the <CR>.
								
			String unitsString = (new String(inputMessage, 1, endByte)).trim();
			Units units = UNITS_LOOKUP.get(unitsString);
			if (units == null) throw new NCIException("Units not recognized by this driver: " + unitsString);
			NCIDeviceRxtx.this.units = units;

			if (this.internalState != null) {
				int newStartByte = endByte + 2; // The justification for this being +2 is that the <CR> was detected already, and the start of parsing should be at +2.
				if (newStartByte >= bytesRead) throw new NCIException("Parser configuration indicates more data should be expected, but not enough bytes were read. Aborting.");
				this.internalState.parseInput(bytesRead, newStartByte, inputMessage);
			}
		}
	}
	
	// TODO: Not bothering with metrology counts, as this isn't supported on the one I'm doing.
	
	public class AboutTransmissionState extends WrapperTransmissionState {
		public AboutTransmissionState(ITransmissionState state) {
			super(state);
		}

		public void parseInput(int bytesRead, int startByte, byte[] inputMessage) throws NCIException {
			
		}
	}
	
	public class DiagnosticsTransmissionState extends WrapperTransmissionState {
		public DiagnosticsTransmissionState(ITransmissionState state) {
			super(state);
		}

		public void parseInput(int bytesRead, int startByte, byte[] inputMessage) throws NCIException {

		}
	}
	
	public class SimpleStatusTransmissionState implements ITransmissionState {
		static final byte PARITY = -128;
		static final byte BYTE_FOLLOWS = 1 << 6;
		
		static final byte MOTION_INMOTION = 1 << 0;
		static final byte ZERO_ATZERO = 1 << 1;
		static final byte RAM_ERROR = 1 << 2;
		static final byte EEPROM_ERROR = 1 << 3;
		
		static final byte CAPACITY_UNDER = 1 << 0;
		static final byte CAPACITY_OVER = 1 << 1;
		static final byte ROM_ERROR = 1 << 2;
		static final byte CALIBRATION_FAULTY = 1 << 3;
		
		static final byte RANGE_HIGH = 0x03;
		static final byte WEIGHT_NET = 1 << 2;
		static final byte INITIALZERO_ERROR = 1 << 3;
		
		@Override
		public void parseInput(int bytesRead, int startByte, byte[] inputMessage) throws NCIException {
			boolean inMotion = false;
			boolean atZero = false;
			boolean ramError = false;
			boolean eepromError = false;
			boolean underCapacity = false;
			boolean overCapacity = false;
			boolean romError = false;
			boolean faultyCalibration = false;
			boolean highRange = false;
			boolean netWeight = false;
			boolean initialZeroError = false;
			
			if (startByte >= bytesRead) throw new NCIException("Parser configuration indicates more data should be expected, but not enough bytes were read. Aborting.");
			if (inputMessage[startByte] != 0x0a) throw new NCIException("Status bytes should start with \\r. Found: " + inputMessage[startByte]);
			
			int parityCheck = 0;
			if (startByte + 1 >= bytesRead) throw new NCIException("Misformatted status bytes. First status byte not included in message.");
			byte firstByte = inputMessage[startByte + 1];
			if ((firstByte & MOTION_INMOTION) != 0) {
				parityCheck++;
				inMotion = true;
			}
			if ((firstByte & ZERO_ATZERO) != 0) {
				parityCheck++;
				atZero = true;
			}
			if ((firstByte & RAM_ERROR) != 0) {
				parityCheck++;
				ramError = true;
			}
			if ((firstByte & EEPROM_ERROR) != 0) {
				parityCheck++;
				eepromError = true;
			}
			if ((firstByte & PARITY) != 0) {
				parityCheck++;
			}
			if (NCIDeviceRxtx.this.checkParity && (parityCheck % 2) > 0) {
				throw new NCIException("Parity failure on the first status byte.");
			}
			
			parityCheck = 0;
			boolean thirdByteExists = false;
			if (startByte + 2 >= bytesRead) {
				throw new NCIException("Misformatted status bytes. Second status byte not included in message.");
			}
			byte secondByte = inputMessage[startByte + 2];
			if ((secondByte & CAPACITY_UNDER) != 0) {
				parityCheck++;
				underCapacity = true;
			}
			if ((secondByte & CAPACITY_OVER) != 0) {
				parityCheck++;
				overCapacity = true;
			}
			if ((secondByte & ROM_ERROR) != 0) {
				parityCheck++;
				romError = true;
			}
			if ((secondByte & CALIBRATION_FAULTY) != 0) {
				parityCheck++;
				faultyCalibration = true;
			}
			if ((secondByte & BYTE_FOLLOWS) != 0) {
				parityCheck++;
				thirdByteExists = true;
			}
			if ((secondByte & PARITY) != 0) {
				parityCheck++;
			}
			if (NCIDeviceRxtx.this.checkParity && (parityCheck % 2) > 0) {
				throw new NCIException("Parity failure on the second status byte.");
			}
			
			if (thirdByteExists) {
				parityCheck = 0;
				if (startByte + 3 >= bytesRead) {
					throw new NCIException("Misformatted status bytes. Third status byte not included in message.");
				}
				byte thirdByte = inputMessage[startByte +3];
				if (thirdByte == 0x0d) throw new NCIException("0x0d is not a valid third byte. This probably indicates a faulty follows bit in the second byte.");
				if ((thirdByte & RANGE_HIGH) == RANGE_HIGH) {
					parityCheck += 2;
					highRange = true;
				} else if ((thirdByte & RANGE_HIGH) != 0) {
					throw new NCIException("Misformatted status bytes. Third status byte has unknown state with regard to range.");
				}
				if ((thirdByte & WEIGHT_NET) != 0) {
					parityCheck++;
					netWeight = true;
				}
				if ((thirdByte & INITIALZERO_ERROR) != 0) {
					parityCheck++;
					initialZeroError = true;
				}
				if ((thirdByte & BYTE_FOLLOWS) != 0) {
					parityCheck++;
					// Should I throw an error here? We do not parse a fourth byte, but we do not want to break if a fourth byte is added and we don't care.
				}
				if ((thirdByte & PARITY) != 0) {
					parityCheck++;
				}
				if (NCIDeviceRxtx.this.checkParity && (parityCheck % 2) > 0) {
					throw new NCIException("Parity failure on the third status byte.");
				}
			}
			
			// Not checking for <CR> or <EOT> at the end to maintain the extensibility of the status byte section.
			NCIDeviceRxtx.this.status = new Status(
					inMotion,
					atZero,
					ramError,
					eepromError,
					underCapacity,
					overCapacity,
					romError,
					faultyCalibration,
					highRange ? Status.Range.HIGH : Status.Range.LOW,
					netWeight ? Status.WeightType.NET : Status.WeightType.GROSS,
					initialZeroError);
		}

	}
	
	public interface IPrimaryState {
		void parseInput();
	}
	
	/**
	 * This is created when we are starting a state from a synchronous client
	 *  method, such as {@link NCIDeviceRxtx#getWeight()}.
	 * 
	 * @author Jonathan Card
	 *
	 */
	public class SynchronousTransmissionState implements IPrimaryState {
		private ITransmissionState internalState;
		
		public SynchronousTransmissionState(ITransmissionState state) {
			this.internalState = state;
		}

		@Override
		public void parseInput() {
			synchronized (NCIDeviceRxtx.this) {
				byte[] inputMessage = new byte[INPUT_BUFFER_LENGTH];
				try {
					int bytesRead = 0;
					int oldBytesRead = 0;
					InputStream is = NCIDeviceRxtx.this.port.getInputStream();
					do {
						oldBytesRead = bytesRead;
						bytesRead += is.read(inputMessage, bytesRead, INPUT_BUFFER_LENGTH - bytesRead);
					} while (bytesRead != oldBytesRead && bytesRead < INPUT_BUFFER_LENGTH && inputMessage[bytesRead - 1] != 0x03);
					this.internalState.parseInput(bytesRead, 0, inputMessage);
				} catch (NCIException e) {
					NCIDeviceRxtx.this.transmissionException = e;
				} catch (IOException e) {
					NCIDeviceRxtx.this.transmissionException = new NCIException("IOException retrieving data.", e);
				} finally {
					NCIDeviceRxtx.this.notify();
				}
			}
		}		
	}
	
	// DO NOT USE! This is a placeholder to demonstrate why I am doing what I did do. Asynchronous communication is not implemented yet.
	public class AsynchronousTransmissionState implements IPrimaryState {
		private ITransmissionState internalState;
		
		public AsynchronousTransmissionState(ITransmissionState state) {
			this.internalState = state;
		}

		@Override
		public void parseInput() {
			byte[] inputMessage = new byte[INPUT_BUFFER_LENGTH];
			try {
				int bytesRead = 0;
				int oldBytesRead = 0;
				InputStream is = NCIDeviceRxtx.this.port.getInputStream();
				do {
					oldBytesRead = bytesRead;
					bytesRead += is.read(inputMessage, bytesRead, INPUT_BUFFER_LENGTH - bytesRead);
				} while (bytesRead != oldBytesRead && bytesRead < INPUT_BUFFER_LENGTH && inputMessage[bytesRead - 1] != 0x03);
				this.internalState.parseInput(bytesRead, 0, inputMessage);
				// TODO: Notify the listener of results.
			} catch (NCIException e) {
				// TODO: Call the exception event on the listener
			} catch (IOException e) {
				// TODO: Call the exception event on the listener
			}
		}

	}

}
