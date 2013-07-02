package com.xingang.androidpp30;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.content.BroadcastReceiver;

public class ServiceReceiver extends BroadcastReceiver {
	 @Override
	  public void onReceive(Context context, Intent intent) {
	    MyPhoneStateListener phoneListener=new MyPhoneStateListener();
	    TelephonyManager telephony = (TelephonyManager) 
	    context.getSystemService(Context.TELEPHONY_SERVICE);
	    telephony.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);
	  }
}
