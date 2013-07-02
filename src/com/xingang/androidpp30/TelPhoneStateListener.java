package com.xingang.androidpp30;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.telephony.PhoneStateListener;
import android.util.Log;

public class TelPhoneStateListener extends PhoneStateListener implements LocationListener {
	private int 					m_gsmSigStrength = 0;
	private int 					m_cdmaSigStrength = 0;
	private int 					m_lteSigStrength = 0;
	private int 					m_gsmErrorBitRate = 0;
	private int 					m_evdoDbm = 0;
	private int 					m_evdoSnr = 0;
	private int 					m_gcid = 0;
	private int 					m_serviceState = 0;
	private int 					m_dataState = 0;
	public static int 				m_callState = 0;
	private boolean 				m_isRoaming = false;
	private TelephonyManager 		m_telMgr = null;
	private int 					m_batteryLife = 0;
	private LocationManager 		m_locationManager = null;
	private Handler 				m_handler 		= null; 
	private boolean					m_bIsGsm = false;
	

	final static String EOL = System.getProperty("line.separator");
	
	public TelPhoneStateListener(Context context, Handler handler ) {
		m_handler = handler;
		m_telMgr = ( TelephonyManager )context.getSystemService(Context.TELEPHONY_SERVICE);

		// Register Battery Manager
		context.registerReceiver(this.mBatInfoReceiver,
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
		
		m_locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE); 
		m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, this); 
		m_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1, this); 
		startListening();	 	
	}

	// Battery Life Check
	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent intent) {
			m_batteryLife = intent.getIntExtra("level", 0);
		}
	}; // End Battery Life Check
	
	public String getBatteryLife() {
		return String.valueOf(m_batteryLife);
	}
	

	public void close() {
		m_locationManager.removeUpdates(this);
		
	}		

	public void startListening() {
		m_telMgr.listen(this ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);     
		m_telMgr.listen(this ,PhoneStateListener.LISTEN_CELL_LOCATION);      
		m_telMgr.listen(this ,PhoneStateListener.LISTEN_CALL_STATE);      
		m_telMgr.listen(this ,PhoneStateListener.LISTEN_SERVICE_STATE);     
		m_telMgr.listen(this ,PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
	}
	
	public void pauseListening() {
		m_telMgr.listen(this ,PhoneStateListener.LISTEN_NONE);
	}

		
	/* Get the Signal strength from the provider, each time there is an update */
	@Override
	public void onSignalStrengthsChanged(SignalStrength signalStrength) {
		super.onSignalStrengthsChanged(signalStrength);
		m_bIsGsm = signalStrength.isGsm();
		m_cdmaSigStrength = signalStrength.getCdmaDbm();
		m_gsmSigStrength = signalStrength.getGsmSignalStrength();
		m_gsmErrorBitRate = signalStrength.getGsmBitErrorRate();
		m_evdoDbm = signalStrength.getEvdoEcio();
		m_evdoSnr = signalStrength.getEvdoSnr();
	}
	public String getGsmSigStrength() {
		return String.valueOf( m_gsmSigStrength );
	}
	public String getGssmErrorBitRate() {
		return String.valueOf( m_gsmErrorBitRate );
	}
	public String getEvdoDbm() { 
		return String.valueOf( m_evdoDbm );
	}
	public String getEvdoSnr() {
		return String.valueOf( m_evdoSnr );
	}
	public boolean isGsm() {
		return m_bIsGsm;
	}
	public String getCdmaSigStrength() {
		return String.valueOf(m_cdmaSigStrength);
	}

	public void onCellLocationChanged (CellLocation location) {
		if ( location instanceof android.telephony.gsm.GsmCellLocation ) {
			m_gcid = ((GsmCellLocation)location).getCid() ;
		}
	} 
	public String getGCID() {
		return String.valueOf( m_gcid );
	}

	/**
	 * Callback invoked when device service state changes.
	 *
	 * @see ServiceState#STATE_EMERGENCY_ONLY
	 * @see ServiceState#STATE_IN_SERVICE
	 * @see ServiceState#STATE_OUT_OF_SERVICE
	 * @see ServiceState#STATE_POWER_OFF
	 */
	public void onServiceStateChanged(ServiceState serviceState) {
		if( m_serviceState != serviceState.getState() ) {
			m_serviceState = serviceState.getState();
			m_isRoaming = serviceState.getRoaming();
			Log.d("PP30Lite", "Service State: " + getServiceState());
			sendEventMsg();
		}
	}
	
	public void sendEventMsg() {
		Message msg = m_handler.obtainMessage(); 
        Bundle b = new Bundle(); 
        b.putString("type", "collect");  
        msg.setData(b); 
        m_handler.handleMessage(msg); 
	}
	
	public String getServiceState() {
		switch ( m_serviceState ) {
		case ServiceState.STATE_EMERGENCY_ONLY:
			return "Emergency Only";
		case ServiceState.STATE_IN_SERVICE:
			return "In Service";
		case ServiceState.STATE_POWER_OFF:
			return "Power Off";
		case ServiceState.STATE_OUT_OF_SERVICE:
			return "Out Of Service";				
		default:	
			return "Unknown";			
		}
	}
	
	public String getRoaming() {
		
		if (m_isRoaming == true) {
			return "Yes"; 
		} else {
			return "No";
		}
	}  

	/**
	 * Callback invoked when device call state changes.
	 *
	 * @see TelephonyManager#CALL_STATE_IDLE
	 * @see TelephonyManager#CALL_STATE_RINGING
	 * @see TelephonyManager#CALL_STATE_OFFHOOK
	 */
	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		if ( m_callState != state ) {
			m_callState = state;
			Log.d("PP30Lite", "Call State: " + getCallState() + "Phonenumber: " + ((incomingNumber==null)? "N/A" : incomingNumber) );
			sendEventMsg();
		}
	    super.onCallStateChanged(state, incomingNumber);

	}
	
	public String getCallState() {
		switch ( m_callState ) {
		case TelephonyManager.CALL_STATE_OFFHOOK:
			return "Off Hook";
		case TelephonyManager.CALL_STATE_RINGING:
			return "Ringing";
		case TelephonyManager.CALL_STATE_IDLE:
			return "Idle";				
		default:	
			return "Unknown";
		}
	}

	/**
	 * Callback invoked when connection state changes.
	 *
	 * @see TelephonyManager#DATA_DISCONNECTED
	 * @see TelephonyManager#DATA_CONNECTING
	 * @see TelephonyManager#DATA_CONNECTED
	 * @see TelephonyManager#DATA_SUSPENDED
	 */
	public void onDataConnectionStateChanged(int state) {
		m_dataState = state;
	}
	
	public String getDataState() {
		switch ( m_dataState ) {
		case TelephonyManager.DATA_DISCONNECTED:
			return "Disconnected";
		case TelephonyManager.DATA_CONNECTING:
			return "Connecting";
		case TelephonyManager.DATA_CONNECTED:
			return "Connected";
		case TelephonyManager.DATA_SUSPENDED:
			return "Suspended";
		default:
			return "Unknown";
		}
	}

	public String getPhoneNumber() {
		String pn = m_telMgr.getLine1Number();
		return (pn == null ? "UNKNOWN" : pn);
	}

	public String getSoftwareVer() {
		String swv = m_telMgr.getDeviceSoftwareVersion();
		return (swv == null ? "UNKNOWN" : swv);
	}


	public String getSubscriberID() {
		String sid = m_telMgr.getSubscriberId();
		return (sid == null ? "UNKNOWN" : sid);
	}


	public String getDeviceID() {
		String did = m_telMgr.getDeviceId();
		return (did == null ? "UNKNOWN" : did);
	}

	public String getSimOpName() {
		String sim = m_telMgr.getSimOperatorName();
		if (sim.equals("") == true ) {
			// Try network operator name
			sim = m_telMgr.getNetworkOperatorName();
		}		
		if ( sim != null ) {
			// This is a quick KLUDGE for AT&T
			// would not need this after we implement the RESTful interface or encode/decode http parameters but time is short
			sim = sim.replaceAll("&", "");
		}

		return (sim == null ? "UNKNOWN" : sim);
	}

	public String getNetworkType() {
		switch (m_telMgr.getNetworkType()) {
		case TelephonyManager.NETWORK_TYPE_UNKNOWN: return "UNKNOWN";
		case TelephonyManager.NETWORK_TYPE_GPRS: return "GPRS";
		case TelephonyManager.NETWORK_TYPE_EDGE: return "EDGE";
		case TelephonyManager.NETWORK_TYPE_UMTS: return "UMTS";
		case TelephonyManager.NETWORK_TYPE_HSDPA: return "HSDPA";
		case TelephonyManager.NETWORK_TYPE_HSUPA: return "HSUPA";
		case TelephonyManager.NETWORK_TYPE_HSPA: return "HSPA";
		case TelephonyManager.NETWORK_TYPE_CDMA: return "CDMA";
		case TelephonyManager.NETWORK_TYPE_EVDO_0: return "EVDO_0";
		case TelephonyManager.NETWORK_TYPE_EVDO_A: return "EVDO_A";
		case TelephonyManager.NETWORK_TYPE_1xRTT: return "1xRTT";
		}

		return "UNKNOWN";		
	}

	public String getPhoneType() {
		switch (m_telMgr.getPhoneType()) {
		case TelephonyManager.PHONE_TYPE_CDMA: return "CDMA";
		case TelephonyManager.PHONE_TYPE_GSM: return "GSM";
		case TelephonyManager.PHONE_TYPE_NONE: return "NONE";
		}

		return "UNKNOWN";
	}
	

	// Location Listener
	@Override
	public void onLocationChanged(Location location) {
		//printLocation(location);

	}

	// Location Listener
	@Override
	public void onProviderDisabled(String provider) {
	}

	// Location Listener
	@Override
	public void onProviderEnabled(String provider) {

	}

	// Location Listener
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	public Location getLocation() {
		if ( m_locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null ){
			return new Location(m_locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
		} else if ( m_locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null ) {
			return new Location(m_locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
		}
		return null;
	}
	
	public String printLocation(Location location) {
		String loc = "Location unknown" + EOL;
		if (location != null) {
			loc = "Latitude: " + location.getLatitude() + EOL + 
			"Longitude: " + location.getLongitude() + EOL +
			"Altitude: " + location.getAltitude() + EOL;
		}
		return new String (loc);
	}




}; // END TelPhoneStateListener

