/**
 * This package is the main package for the interfaces represented in the NCI
 * driver library. The entry-point for the library is found in
 * {@link com.jackflashtech.nci.impl.NCIFactory}. The plan is for it to create
 * instances of the interfaces in this package, though as of now, it only
 * creates a single device on "COM6", and it uses the default settings you set
 * in Windows Device Manager.
 */
package com.jackflashtech.nci;