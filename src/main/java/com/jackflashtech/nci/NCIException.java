package com.jackflashtech.nci;

// TODO: Consider a sub-class of NCIException for parsing exceptions
public class NCIException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8839110621674741393L;

	public NCIException(String string) {
		super(string);
	}
	
	public NCIException(Throwable e) {
		super(e);
	}

	public NCIException(String string, Throwable e) {
		super(string, e);
	}

}
