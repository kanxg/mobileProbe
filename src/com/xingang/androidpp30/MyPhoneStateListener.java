package com.xingang.androidpp30;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MyPhoneStateListener extends PhoneStateListener {

	public  static Handler 			m_handler = null; 
	private static int				m_state = 0; 
	
	
	public void onCallStateChanged(int state,String incomingNumber){
		switch(state){
		case TelephonyManager.CALL_STATE_IDLE:
			Log.d("PP30Lite", "IDLE");
			break;
		case TelephonyManager.CALL_STATE_OFFHOOK:
			Log.d("PP30Lite", "OFFHOOK");
			break;
		case TelephonyManager.CALL_STATE_RINGING:
			Log.d("PP30Lite", "RINGING");
			break;
		}
		if ( state != m_state ) {
			m_state = state;
			TelPhoneStateListener.m_callState = state;
			sendEventMsg();
		}
	} 
	
	
	private void sendEventMsg() {
		if ( m_handler != null ) {
			try {
				Message msg = m_handler.obtainMessage(); 
		        Bundle b = new Bundle(); 
		        b.putString("type", "collect");  
		        msg.setData(b); 
		        m_handler.handleMessage(msg); 

				Log.d("PP30Lite", "Completed sending the Collect Event");
			} catch (Exception e ) {
				Log.d("PP30Lite", "Failed sending the Collect Event");
				
			}
		}
	}
	
}
