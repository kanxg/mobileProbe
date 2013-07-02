package com.xingang.androidpp30;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;

public class ServerSetup {
	private static int m_collectionRate;
	private static int m_transferRate;
	private static boolean m_isPhoneStatusEnabled;
	private static boolean m_isSpeedTestEnabled;
	private static String m_phoneStatusURL;
	private static boolean m_isTimeCollectEnabled;	
	private static boolean m_isServiceCollectEnabled;
	private static String m_speedTestFtpURL;
	private static String m_speedTestFtpLogin;
	private static String m_speedTestFtpPW;
	private static String m_speedTestFtpFilename;
	private static int m_stopServiceTime; // minutes
	private static int m_defaultHistoryView;
	private static String m_historyListViewOp1;
	private static String m_historyListViewOp2;
	private static String m_historyListViewOp3;
	private static String m_historySlaGsmSig;
	private static String m_historySlaBattery;
	private static String m_historySlaSpeedMBPS;
	private static String m_historySlaInLowMem;
	
	private static ServerSetup instance = null;
	
	protected ServerSetup() {
		// Exists only to defeat instantiation.
	}
	public static ServerSetup getInstance() {
		if(instance == null) {
			instance = new ServerSetup();
		}
		return instance;
	}

	public String getPhoneStatusURL() {
		return m_phoneStatusURL;
	}
	public void setPhoneStatusURL(String str ) {
		m_phoneStatusURL = str;
	}
	
	public int getCollectionRate() {
		return m_collectionRate;
	}
	public void setCollectionRate(int i ) {
		m_collectionRate = i;
	}
	
	public int getTransferRate() {
		return m_transferRate;
	}
	public void setTransferRate(int i ) {
		m_transferRate = i;
	}	
	
	public int getStopServiceTimeInMin() {
		return m_stopServiceTime;
	}
	public void setStopServiceTime(int i ) {
		m_stopServiceTime = i;
	}	
	
	public boolean isPhoneStatusEnabled() {
		return m_isPhoneStatusEnabled;
	}
	public void setIsPhoneStatusEnabled(boolean b) {
		m_isPhoneStatusEnabled = b;
	}
	
	public boolean isSpeedTestEnabled() {
		return m_isSpeedTestEnabled;
	}
	public void setIsSpeedTestEnabled(boolean b) {
		m_isSpeedTestEnabled = b;
	}
	
	public boolean isTimeStatusEnabled() {
		return m_isTimeCollectEnabled;
	}
	public void setIsTimeStatusEnabled(boolean b) {
		m_isTimeCollectEnabled = b;
	}	
	
	public boolean isServiceStatusEnabled() {
		return m_isServiceCollectEnabled;
	}
	public void setIsServiceStatusEnabled(boolean b) {
		m_isServiceCollectEnabled = b;
	}	
	
	public String getSpeedTestFtpURL() {
		return m_speedTestFtpURL;
	}	
	public void setSpeedTestFtpURL(String str ) {
		m_speedTestFtpURL = str;
	}
	
	public String getSpeedTestFtpLogin() {
		return m_speedTestFtpLogin;
	}	
	public void setSpeedTestFtpLogin(String str ) {
		m_speedTestFtpLogin = str;
	}	
	
	public String getSpeedTestFtpPW() {
		return m_speedTestFtpPW;
	}
	public void setSpeedTestFtpPW(String str ) {
		m_speedTestFtpPW = str;
	}	
	
	public String getSpeedTestFtpFilename() {
		return m_speedTestFtpFilename;
	}
	public void setSpeedTestFtpFilename(String str ) {
		m_speedTestFtpFilename = str;
	}		
	
	public HistoryType getHistoryView() {
		return m_defaultHistoryView == 0 ? HistoryType.LIST : HistoryType.DETAIL;
	}
	public void setHistoryView( int ht ) {
		m_defaultHistoryView = ht;
	}
	
	public HistoryEnum getHistoryListOp1() {
		return HistoryEnum.fromString(m_historyListViewOp1);
	}
	public void setHistoryListOp1( String str ) {
		m_historyListViewOp1 = str;
	}

	public HistoryEnum getHistoryListOp2() {
		return HistoryEnum.fromString(m_historyListViewOp2);
	}
	public void setHistoryListOp2( String str ) {
		m_historyListViewOp2 = str;
	}
	
	public HistoryEnum getHistoryListOp3() {
		return HistoryEnum.fromString(m_historyListViewOp3);
	}
	public void setHistoryListOp3( String str ) {
		m_historyListViewOp3 = str;
	}
	
	public void setHistorySlaGsmSig( String str ) {
		m_historySlaGsmSig = str;
	}
	public void setHistorySlaSpeedMBPS( String str ) {
		m_historySlaSpeedMBPS = str;
	}
	public void setHistorySlaInLowMem( String str ) {
		m_historySlaInLowMem = str;
	}
	public void setHistorySlaBattery( String str ) {
		m_historySlaBattery = str;
	}	
	
	public boolean validateSla(HistoryEnum he, String value ) {
		boolean bSla = true;
		int iVal;
		int iSla;
		double dVal;
		switch ( he ) {
		case BATTERY:
			iVal = Integer.parseInt(value);
			iSla = Integer.parseInt(m_historySlaBattery);
			if ( iVal < iSla ) {
				bSla = false;
			}
			break;
		case SPEED_TEST_MB_PER_SEC:
			dVal = Double.parseDouble(value);
			iSla = Integer.parseInt(m_historySlaSpeedMBPS);
			if ( dVal < iSla ) {
				bSla = false;
			}			
			break;
		case GSM_SIGNAL_STRENGTH:
			iVal = Integer.parseInt(value);
			iSla = Integer.parseInt(m_historySlaGsmSig);
			if ( iVal < iSla ) {
				bSla = false;
			}
			break;
		case IN_LOW_MEMORY_MODE:
			if ( m_historySlaInLowMem.equals(value )  == false ) {
				bSla = false;
			}
			break;
		default: 
			break;
		}
		return bSla;
	}
}
