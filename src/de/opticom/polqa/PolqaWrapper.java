package de.opticom.polqa;

public class PolqaWrapper {
	
	// POLQA mode
    public final static int POLQA_LC_STANDARD_IRS 	= 0x0002;
    public final static int POLQA_LC_SWIDE_H 		= 0x0003;

    // Result verbosity
    public final static int POLQA_DEF     		= 0x000;	/* Default level - use internal preset (currently POLQA_RLEVEL1 | POLQA_RLEVEL2 | POLQA_RLEVEL3) */
    public final static int POLQA_RLEVEL1 		= 0x100;	/* Lowest informational level - highest computational speed */
    public final static int POLQA_RLEVEL2 		= 0x200;	/* Medium informational level - lower computational speed */
    public final static int POLQA_RLEVEL3 		= 0x400;	/* High informational level - low computational speed */
    public final static int POLQA_RLEVEL_ALL 	= POLQA_RLEVEL1 | POLQA_RLEVEL2 | POLQA_RLEVEL3;

    /* Specify whether to use automatic level alignment or not */
    /* Note: Using the automatuic mode is non-standard! */
    public final static int POLQA_LEVEL_ALIGN = 0x1000;  /* Switch on automatic level alignment between reference and test signal */
    								 /* Note: If this is set, POLQA_RLEVEL2 will be used automatically!) */

    /* Specify whether to use automatic samplerate conversion */
    /* Note: Using the automatic mode is non-standard! */
    public final static int POLQA_AUTO_SR_CONVERSION_OFF = 0x2000; /* NOTE: default is on */
    
    /* Specify if the acurate processing time shall be measured (adds up to 2s processing time per run) */
    public final static int POLQA_CHECK_PROCESSING_TIME = 0x40;

    /* Some helpers to mask off bits of ulMode that specify ....       */
    public final static int POLQA_LC_MASK 		= 0x000F;	/* ... the listening condition */
    public final static int POLQA_RLEVEL_MASK 	= 0x0700;	/* ... the result level        */
    
    // Error codes
	public final static int	POLQA_OK=0;
	public final static int POLQA_MEMORY_ALLOCATION_FAILED=1;
	public final static int POLQA_REGISTRATION_FAILED=2;
	public final static int POLQA_INITIALISATION_FAILED=3;
	public final static int POLQA_CALCULATION_FAILED=4;
	public final static int POLQA_CREATE_LICENCE_INFO_FAILED=5;
	public final static int POLQA_INPUT_SIGNALS_TOO_LONG=6;
	public final static int POLQA_SAMPLE_RATE_NOT_SUPPORTED=7;
	public final static int POLQA_WRONG_HANDLE=8;
	public final static int POLQA_FILE_OPEN_FAILED=9;
	public final static int POLQA_RESULT_LEVEL_TOO_LOW=10;
	public final static int POLQA_LAST_ERROR=11;
		
	public int PolqaLibInit(String regFileName, int ulMode, Object telephonyManager)
	{
		if(handle != 0) return POLQA_WRONG_HANDLE;
		// Attention: internalPolqaLibInit() modifies handle!
		int result = internalPolqaLibInit(regFileName, ulMode, telephonyManager);
		return result;
	}

	public int PolqaLibRun(
			float[] refData, int refOffset, int refCount, int refSampleRate,
			float[] degData, int degOffset, int degCount, int degSampleRate)
	{
		return internalPolqaLibRun(
				refData, refOffset, refCount, refSampleRate,
				degData, degOffset, degCount, degSampleRate);
	}
	
	public int PolqaLibGetResult(PolqaResultData result)
	{
		return internalPolqaLibGetResult(result);
	}
	
	/**
	 * not use in PP30
	public int PolqaLibGetDelayHistogram(int FirstSample, int NumSamples, PolqaDelayHistogram histogram)
	{
		return internalPolqaLibGetDelayHistogram(FirstSample, NumSamples, histogram);
	}
	*/
	
	public int PolqaLibFree() {
		if (handle == 0) return POLQA_WRONG_HANDLE;
		// Attention: internalPolqaLibFree() modifies handle!
		int result = internalPolqaLibFree();
		return result;
	}
	
	public int PolqaCreateLicenseKeyInfo(String licenseInfoFilename, String licenseFileName, Object telephonyManager)
	{
		int result = internalPolqaCreateLicenseKeyInfo(licenseInfoFilename, licenseFileName, telephonyManager);
		return result;
	}

	public long getHandle() { return handle; }
	private long handle = 0;

	private static native void initIDs();
	private native int internalPolqaLibInit(String regFileName, int ulMode, Object telephonyManager);
	private native int internalPolqaLibRun(
			float[] refData, int refOffset, int refCount, int refSampleRate,
			float[] degData, int degOffset, int degCount, int degSampleRate);
	private native int internalPolqaLibGetResult(PolqaResultData result);
	//not use
//	private native int internalPolqaLibGetDelayHistogram(int FirstSample, int NumSamples, PolqaDelayHistogram histogram);

	private native int internalPolqaLibFree();
	private native int internalPolqaCreateLicenseKeyInfo(String licenseInfoFilename, String licenseFileName, Object telephonyManager);
	
	static {
		System.loadLibrary("PolqaOemJava");
		initIDs();
	}
}
