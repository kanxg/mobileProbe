package de.opticom.io;

public class AudioDataMono {
	// error codes
	public static final int AUDIO_OK = 0;
	public static final int AUDIO_READ_ERROR = 1;
	public static final int AUDIO_FILEOPEN_ERROR = 2;
	public static final int AUDIO_MEMORY_ERROR = 3;
	public static final int AUDIO_UNKNOWN_TYPE = 4;
	public static final int AUDIO_INITIALISATON_ERROR = 5;

	// audio modes
	public static final int MODE_UNKNOWN = 0;
	public static final int MODE_IBM_ALAW = 1;
	public static final int MODE_IBM_MULAW = 2;
	public static final int MODE_INTEGER16_LITLE_ENDIAN = 3;
	public static final int MODE_INTEGER16_BIG_ENDIAN = 4;

	// audio types
	public static final int TYPE_UNKNOWN = 0;
	public static final int TYPE_WAVE = 1;
	public static final int TYPE_PCM = 2;

	public int read(
			String filename,
			int audioType,
			int audioMode,
			int sampleRate,
			int channels)
	{
		Reset();
		int result = internalGetAudioDataOneFile(
				filename,
				audioType,
				audioMode,
				sampleRate,
				channels);
		return result;
	}
	
	public float[] samples = null;
	public int sampleRate = 0;
//	public int audioType = 0;
//	public int audioMode = 0;
//	public int channels = 0;
	
	public void Reset() {
		samples = null;
		sampleRate = 0;
//		audioType = 0;
//		audioMode = 0;
//		channels = 0;
	}
	
	private native int internalGetAudioDataOneFile(
			String filename,
			int audioType,
			int audioMode,
			int sampleRate,
			int channels);
	
	static {
		System.loadLibrary("IoWrapperDll");
	}
}
