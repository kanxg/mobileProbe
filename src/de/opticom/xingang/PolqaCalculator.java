package de.opticom.xingang;

import de.opticom.io.AudioDataMono;
import de.opticom.polqa.PolqaResultData;
import de.opticom.polqa.PolqaWrapper;


public class PolqaCalculator {
	
	private Object telephonyManager;
	private PolqaWrapper polqaWrapper = new PolqaWrapper();	
	private PolqaResultData resultData ;

	public PolqaCalculator(Object telephonyManager) {
		this.telephonyManager = telephonyManager;
	}
	
	public int Calc(String referenceFilename,String testFilename,String POLQA_STPOS,String POLQA_LENGTH) {
		int result;
		resultData = new PolqaResultData();
		AudioDataMono referenceFile = new AudioDataMono();
		AudioDataMono degradedFile = new AudioDataMono();
		//... fill the data vectors pfRefData and pfDegData with data
		result = referenceFile.read(referenceFilename, AudioDataMono.TYPE_WAVE, AudioDataMono.MODE_UNKNOWN, 0, 0);
		if(result != AudioDataMono.AUDIO_OK) {
			return -1;
		}
		result = degradedFile.read(testFilename, AudioDataMono.TYPE_WAVE, AudioDataMono.MODE_UNKNOWN, 0, 0);
		if(result != AudioDataMono.AUDIO_OK) {
			return -1;
		}
		//... Set ulSampleRate to the sample rate used.
		// Init library
		//Object tm = getSystemService(TELEPHONY_SERVICE); // on Android
		//Object tm = null; // on the Java virtual machine
		result = polqaWrapper.PolqaLibInit(
		"/sdcard/xingang/androidpp30/polqa/PolqaLicenseFile.txt",
		PolqaWrapper.POLQA_LC_STANDARD_IRS,
		telephonyManager);
		if(result != PolqaWrapper.POLQA_OK) {
			return -1;
		}
		// Calculate POLQA
		result = polqaWrapper.PolqaLibRun(
				referenceFile.samples, 0, referenceFile.samples.length, referenceFile.sampleRate,
				degradedFile.samples, degradedFile.sampleRate*(int)Float.parseFloat(POLQA_STPOS), degradedFile.sampleRate*(int)Float.parseFloat(POLQA_LENGTH), degradedFile.sampleRate);
		// Get and print the results
		result = polqaWrapper.PolqaLibGetResult(resultData);
		//Close library
		polqaWrapper.PolqaLibFree();
		if(result != PolqaWrapper.POLQA_OK) {
			return -1;
		}
		return result;
	}

	public PolqaResultData getResultData() {
		return resultData;
	}

	public void setResultData(PolqaResultData resultData) {
		this.resultData = resultData;
	}
	
}
