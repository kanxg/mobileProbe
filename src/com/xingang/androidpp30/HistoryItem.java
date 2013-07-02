package com.xingang.androidpp30;

import java.text.DecimalFormat;
import java.util.Date;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.style.ForegroundColorSpan;

import java.util.EnumMap;


public class HistoryItem {
	final static String EOL = System.getProperty("line.separator"); 
	
	EnumMap<HistoryEnum, String> hi = new EnumMap<HistoryEnum, String>(HistoryEnum.class);
	
	public String getValue(HistoryEnum e ) {
		return hi.get(e);
	}
	public void setValue(HistoryEnum e, String s) {
		hi.put(e, s);
	}
	public EnumMap<HistoryEnum, String> getHistoryMap() {
		return hi;
	}
	
	public String toString( ) {
		return toSlaString( false );
	}
    
    public String toSlaString(boolean bIsSla) {
    	ServerSetup settings = null;
    	if ( bIsSla == true ) {
    		settings = ServerSetup.getInstance();
    	}
    	StringBuffer buf = new StringBuffer(); 
    	buf.append("--- Call Info ---" + EOL);
    	
		for ( EnumMap.Entry<HistoryEnum, String> entry : hi.entrySet() ) {
			String val=null;
			switch ( entry.getKey() ) {
			case START_TIME:
				val = entry.getKey().toString() + ": " + getFormattedStartTime(entry.getValue());
				break;
			case BATTERY:
				val = entry.getKey().toString() + ": " + entry.getValue() + "%";
				break;
			case UPLINK_METRICS:
				break;
			case DOWNLINK_METRICS:
				break;
			case MOS_POLQA:
				val = EOL +"--- Active Mobile Voice Quality Test Results ---" + EOL + entry.getKey().toString() + ": " + entry.getValue();
				break;
			default:
				val = entry.getKey().toString() + ": " + entry.getValue();
			}
			if(val!=null){
				if (settings != null &&  settings.validateSla(entry.getKey(), entry.getValue()) == false) {
	    			CharSequence str = UtilityBelt.setSpanBetweenTokens("##"+val + "##", "##", 
	    					new ForegroundColorSpan( Color.RED ) );
	    			val = str.toString();   
				}
				buf.append(val + EOL);
			}
		}       

    	return buf.toString();
    }
    
    public String getFormattedStartTime(String longTimeStr) {
    	Date date = new Date(Long.parseLong(longTimeStr));
    	return date.toLocaleString();
    }
    
    
	public void setData(SharedPreferences history, int i) {
		
    	for ( HistoryEnum he : HistoryEnum.values() ) {
    		String val = history.getString(he.toString() + Integer.toString(i), "Undefined" );
    		if (val != "Undefined") {
    			hi.put(he, val);
    		}
    	}
			
	}
	public void saveData(SharedPreferences.Editor history, int i) {
		for ( EnumMap.Entry<HistoryEnum, String> entry : hi.entrySet() ) {
			history.putString(entry.getKey()+Integer.toString(i), entry.getValue() );
		}
						
	}  
	
	
	public String buildUriPrams(  ) {
		StringBuffer  buf = new StringBuffer();	
		//params = "altitude=101&deviceID=201&gsmSignalStrength=301&latitude=401&longitude=501&phoneNumber=601&subscriberID=701&timestamp=date.struct&timestamp_day=16&timestamp_month=6&timestamp_year=2011&create=Create&globalCellID=999";
		
		// TODO:  All HTML params need to be URLEncoded just in case
		//        there are any special chars
		try {
			boolean b = false;
			for ( EnumMap.Entry<HistoryEnum, String> entry : hi.entrySet() ) {
				if ( b == true ) {
					buf.append("&");
				}
				if ( entry.getKey().getUrlName() != null ) {
					//String value=entry.getValue();
					//if(value!=null&"".equals(value)){
						buf.append( entry.getKey().getUrlName() + entry.getValue() );
						b = true;					
					//}
				}
			}			


	    	
		} catch ( Exception e) {
			System.out.println( "Error Building URI Params.\n\n" + e.toString() );	    	
	    }
		
		return buf.toString();

	}

}
