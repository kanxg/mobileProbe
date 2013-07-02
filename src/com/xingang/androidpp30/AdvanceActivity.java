package com.xingang.androidpp30;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


public class AdvanceActivity extends Activity implements OnFocusChangeListener {
    public static final String PREFS_NAME = "AdvancedPrefsFile";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.adv_layout);  

        ((EditText)findViewById(R.id.PhoneStatusCollectRateEdit)).setOnFocusChangeListener(this);   
        ((EditText)findViewById(R.id.PhoneStatusTransferRateEdit)).setOnFocusChangeListener(this);  
        ((EditText)findViewById(R.id.PhoneStatusStopServiceEdit)).setOnFocusChangeListener(this);
        
        
        ((EditText)findViewById(R.id.HistorySlaGsmSigEdit)).setOnFocusChangeListener(this);   
        ((EditText)findViewById(R.id.HistorySlaSpeedKBPSEdit)).setOnFocusChangeListener(this);   
        ((EditText)findViewById(R.id.HistorySlaInLowMemEdit)).setOnFocusChangeListener(this);   
        ((EditText)findViewById(R.id.HistorySlaBatteryEdit)).setOnFocusChangeListener(this); 
    	
    	createHistoryListOptionSpinner(R.id.HistoryListViewOption1Spinner);
    	createHistoryListOptionSpinner(R.id.HistoryListViewOption2Spinner);
    	createHistoryListOptionSpinner(R.id.HistoryListViewOption3Spinner);
 
        
        setData();
    }
    
    private void createHistoryListOptionSpinner( int spinnerId ) {
      	Spinner spinner = (Spinner) findViewById(spinnerId);
    	List<String> list = new ArrayList<String>();
    	for ( HistoryEnum he : HistoryEnum.values() ) {
    		list.add(he.toString());
    	}
    	ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
        		android.R.layout.simple_spinner_item, list);
        	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	spinner.setAdapter(dataAdapter);     	
    }
    
    public void onStop() {
    	saveData();
    	super.onStop();
    }
    
    public void onPause() {
    	saveData();
    	super.onPause();
    }
    
    private void saveData () {
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        String value;
        boolean bVal;
        int iVal;
 
        // Phone Status Settings
        value = ((EditText)findViewById(R.id.PhoneStatusCollectRateEdit)).getText().toString();
        editor.putString("collectRate", value);
        
        value = ((EditText)findViewById(R.id.PhoneStatusTransferRateEdit)).getText().toString();
        editor.putString("transferRate", value);
        
        value = ((EditText)findViewById(R.id.PhoneStatusStopServiceEdit)).getText().toString();
        editor.putString("stopServiceTime", value);
        
        bVal = ((CheckBox)findViewById(R.id.EnablePhoneStatusAgentChkBox)).isChecked();
        editor.putBoolean("phoneStatusEnabled", bVal);
        
        value = ((EditText)findViewById(R.id.PhoneStatusURIEdit)).getText().toString();
        editor.putString("phoneStatusUri", value);
        
        bVal = ((CheckBox)findViewById(R.id.EnablePhoneStatusSpeedTestChkBox)).isChecked();
        editor.putBoolean("speedTestEnabled", bVal);
 
        value = ((EditText)findViewById(R.id.PhoneStatusFtpSiteEdit)).getText().toString();
        editor.putString("ftpUri", value);
        
        value = ((EditText)findViewById(R.id.PhoneStatusFtpLoginEdit)).getText().toString();
        editor.putString("ftpLogin", value);
        
        value = ((EditText)findViewById(R.id.PhoneStatusFtpPWEdit)).getText().toString();
        editor.putString("ftpPW", value);
        
        value = ((EditText)findViewById(R.id.PhoneStatusFtpFileNameEdit)).getText().toString();
        editor.putString("ftpFile", value);
        
        bVal = ((CheckBox)findViewById(R.id.EnablePhoneStatusTimeCollectChkBox)).isChecked();
        editor.putBoolean("enableTimeStatus", bVal);
        
        bVal = ((CheckBox)findViewById(R.id.EnablePhoneStatusServiceCollectChkBox)).isChecked();
        editor.putBoolean("enableServiceStatus", bVal);

        // History View Options
        iVal = ((Spinner)findViewById(R.id.HistoryDefaultViewSpinner)).getSelectedItemPosition();
        editor.putInt("defaultHistoryView", iVal);
        
        String selItem = ((Spinner)findViewById(R.id.HistoryListViewOption1Spinner)).getSelectedItem().toString();
        editor.putString("defaultHistoryListViewOp1", selItem);
        
        selItem = ((Spinner)findViewById(R.id.HistoryListViewOption2Spinner)).getSelectedItem().toString();
        editor.putString("defaultHistoryListViewOp2", selItem);
        
        selItem = ((Spinner)findViewById(R.id.HistoryListViewOption3Spinner)).getSelectedItem().toString();
        editor.putString("defaultHistoryListViewOp3", selItem);
 
        value = ((EditText)findViewById(R.id.HistorySlaGsmSigEdit)).getText().toString();
        editor.putString("historySlaGsmSigEdit", value);
        
        value = ((EditText)findViewById(R.id.HistorySlaSpeedKBPSEdit)).getText().toString();
        editor.putString("historySlaSpeedKBPSEdit", value);
        
        value = ((EditText)findViewById(R.id.HistorySlaInLowMemEdit)).getText().toString();
        editor.putString("historySlaInLowMemEdit", value);
        
        value = ((EditText)findViewById(R.id.HistorySlaBatteryEdit)).getText().toString();
        editor.putString("historySlaBatteryEdit", value);

        // Commit the edits!
        editor.commit();   	
        
        setServerSetup();
    }
    
    private void setData () {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        // Phone Status Settings
        ((EditText)findViewById(R.id.PhoneStatusCollectRateEdit)).setText(settings.getString("collectRate", "90"));
        ((EditText)findViewById(R.id.PhoneStatusTransferRateEdit)).setText(settings.getString("transferRate", "300"));
        ((EditText)findViewById(R.id.PhoneStatusStopServiceEdit)).setText(settings.getString("stopServiceTime", "90"));        
        ((CheckBox)findViewById(R.id.EnablePhoneStatusAgentChkBox)).setChecked(settings.getBoolean("phoneStatusEnabled", true));
        ((EditText)findViewById(R.id.PhoneStatusURIEdit)).setText(settings.getString("phoneStatusUri", "http://134.64.202.98:8080/MobileManager/"));
        ((CheckBox)findViewById(R.id.EnablePhoneStatusSpeedTestChkBox)).setChecked(settings.getBoolean("speedTestEnabled", false));
 
        ((EditText)findViewById(R.id.PhoneStatusFtpSiteEdit)).setText(settings.getString("ftpUri", ""));
        ((EditText)findViewById(R.id.PhoneStatusFtpLoginEdit)).setText(settings.getString("ftpLogin", ""));
        ((EditText)findViewById(R.id.PhoneStatusFtpPWEdit)).setText(settings.getString("ftpPW", ""));
        ((EditText)findViewById(R.id.PhoneStatusFtpFileNameEdit)).setText(settings.getString("ftpFile", ""));

        ((CheckBox)findViewById(R.id.EnablePhoneStatusTimeCollectChkBox)).setChecked(settings.getBoolean("enableTimeStatus", true));
        ((CheckBox)findViewById(R.id.EnablePhoneStatusServiceCollectChkBox)).setChecked(settings.getBoolean("enableServiceStatus", true));

        // History View Options
        ((Spinner)findViewById(R.id.HistoryDefaultViewSpinner)).setSelection(settings.getInt("defaultHistoryView", 0));
        
        String op1 = settings.getString("defaultHistoryListViewOp1", HistoryEnum.GLOBAL_CELL_ID.toString());
        String op2 = settings.getString("defaultHistoryListViewOp2", HistoryEnum.BATTERY.toString());
        String op3 = settings.getString("defaultHistoryListViewOp3", HistoryEnum.SPEED_TEST_MB_PER_SEC.toString());
    	int i = 0;
        for ( HistoryEnum he : HistoryEnum.values() ) {
        	String str = he.toString();
    		if ( str.equals(op1) == true) {
    			((Spinner)findViewById(R.id.HistoryListViewOption1Spinner)).setSelection(i);
    		}
    		if ( str.equals(op2) == true ) {
    			((Spinner)findViewById(R.id.HistoryListViewOption2Spinner)).setSelection(i);
    		}   
    		if ( str.equals(op3) == true ) {
    			((Spinner)findViewById(R.id.HistoryListViewOption3Spinner)).setSelection(i);
    		}    		
    		i++;
    	}
        ((EditText)findViewById(R.id.HistorySlaGsmSigEdit)).setText(settings.getString("historySlaGsmSigEdit", "-103"));
        ((EditText)findViewById(R.id.HistorySlaSpeedKBPSEdit)).setText(settings.getString("historySlaSpeedKBPSEdit", "15"));
        ((EditText)findViewById(R.id.HistorySlaInLowMemEdit)).setText(settings.getString("historySlaInLowMemEdit", "No"));
        ((EditText)findViewById(R.id.HistorySlaBatteryEdit)).setText(settings.getString("historySlaBatteryEdit", "7"));
      
        setServerSetup();
    }
    
    private void setServerSetup() {
        // Phone Status Settings
    	String value;
    	boolean bVal;
    	int iVal; 
       	ServerSetup svrSetup = ServerSetup.getInstance();
       	
        value = ((EditText)findViewById(R.id.PhoneStatusCollectRateEdit)).getText().toString();
        svrSetup.setCollectionRate(Integer.parseInt(value));
 
        value = ((EditText)findViewById(R.id.PhoneStatusTransferRateEdit)).getText().toString();
        svrSetup.setTransferRate(Integer.parseInt(value));
        
        value = ((EditText)findViewById(R.id.PhoneStatusStopServiceEdit)).getText().toString();
        svrSetup.setStopServiceTime(Integer.parseInt(value));
        
        bVal = ((CheckBox)findViewById(R.id.EnablePhoneStatusAgentChkBox)).isChecked();
        svrSetup.setIsPhoneStatusEnabled(bVal);
        
        value = ((EditText)findViewById(R.id.PhoneStatusURIEdit)).getText().toString();
        svrSetup.setPhoneStatusURL(value);
        
        bVal = ((CheckBox)findViewById(R.id.EnablePhoneStatusSpeedTestChkBox)).isChecked();
        svrSetup.setIsSpeedTestEnabled(bVal);
 
        value = ((EditText)findViewById(R.id.PhoneStatusFtpSiteEdit)).getText().toString();
        svrSetup.setSpeedTestFtpURL(value);
        
        value = ((EditText)findViewById(R.id.PhoneStatusFtpLoginEdit)).getText().toString();
        svrSetup.setSpeedTestFtpLogin(value);
        
        value = ((EditText)findViewById(R.id.PhoneStatusFtpPWEdit)).getText().toString();
        svrSetup.setSpeedTestFtpPW(value);
        
        value = ((EditText)findViewById(R.id.PhoneStatusFtpFileNameEdit)).getText().toString();
        svrSetup.setSpeedTestFtpFilename(value);
        
        bVal = ((CheckBox)findViewById(R.id.EnablePhoneStatusTimeCollectChkBox)).isChecked();
        svrSetup.setIsTimeStatusEnabled(bVal);
        
        bVal = ((CheckBox)findViewById(R.id.EnablePhoneStatusServiceCollectChkBox)).isChecked();
        svrSetup.setIsServiceStatusEnabled(bVal);

        // History View Options
        iVal = ((Spinner)findViewById(R.id.HistoryDefaultViewSpinner)).getSelectedItemPosition();
        svrSetup.setHistoryView(iVal);
        
        String selItem = ((Spinner)findViewById(R.id.HistoryListViewOption1Spinner)).getSelectedItem().toString();
        svrSetup.setHistoryListOp1(selItem);
        
        selItem = ((Spinner)findViewById(R.id.HistoryListViewOption2Spinner)).getSelectedItem().toString();
        svrSetup.setHistoryListOp2(selItem);
        
        selItem = ((Spinner)findViewById(R.id.HistoryListViewOption3Spinner)).getSelectedItem().toString();
        svrSetup.setHistoryListOp3(selItem);
 
        value = ((EditText)findViewById(R.id.HistorySlaGsmSigEdit)).getText().toString();
        svrSetup.setHistorySlaGsmSig(value);
        
        value = ((EditText)findViewById(R.id.HistorySlaSpeedKBPSEdit)).getText().toString();
        svrSetup.setHistorySlaSpeedMBPS(value);
        
        value = ((EditText)findViewById(R.id.HistorySlaInLowMemEdit)).getText().toString();
        svrSetup.setHistorySlaInLowMem(value);
        
        value = ((EditText)findViewById(R.id.HistorySlaBatteryEdit)).getText().toString();
        svrSetup.setHistorySlaBattery(value);    	
    }

	@Override
	public void onFocusChange(View arg0, boolean arg1) {
		
		switch ( arg0.getId() ) {
			case R.id.PhoneStatusCollectRateEdit:	
				validateEditTextRange(
						(EditText)findViewById(R.id.PhoneStatusCollectRateEdit),
						5,
						3600,
						R.string.collectRateInvalid,
						"90" );			
				return;	
			case R.id.PhoneStatusTransferRateEdit:	
				validateEditTextRange(
						(EditText)findViewById(R.id.PhoneStatusTransferRateEdit),
						30,
						3600,
						R.string.transferRateInvalid,
						"300" );			
				return;
			case R.id.PhoneStatusStopServiceEdit:	
				validateEditTextRange(
						(EditText)findViewById(R.id.PhoneStatusStopServiceEdit),
						0,
						525600,
						R.string.transferRateInvalid,
						"90" );			
				return;	
			case R.id.HistorySlaGsmSigEdit:
				validateEditTextRange(
						(EditText)findViewById(R.id.HistorySlaGsmSigEdit),
						-113,
						-89,
						R.string.gsmSlaSigInvalid,
						"-103" );		
				return;
			case R.id.HistorySlaSpeedKBPSEdit:
				validateEditTextRange(
						(EditText)findViewById(R.id.HistorySlaSpeedKBPSEdit),
						1,
						10000,
						R.string.speedKBPSInvalid,
						"15" );					
				return;
			case R.id.HistorySlaInLowMemEdit:
				EditText e = (EditText)findViewById(R.id.HistorySlaInLowMemEdit);
				if ( ( e.getText().equals("Yes") || e.getText().equals("No") ) == false ) {
					e.setText("No");
				}
				return;
			case R.id.HistorySlaBatteryEdit:
				validateEditTextRange(
						(EditText)findViewById(R.id.HistorySlaBatteryEdit),
						1,
						100,
						R.string.batteryInvalid,
						"15" );					
				return;
		 
		}		
	}
	
	private boolean validateEditTextRange( EditText e, int minVal, int maxVal, int strId, String defaultVal) {
		int val = Integer.parseInt(e.getText().toString());
		if ( val < minVal || val > maxVal) {
			Toast.makeText(getApplicationContext(), 
				       strId, 
				       Toast.LENGTH_LONG).show();				
			e.setText(defaultVal);
			return true;
		}
		return false;
	}
}