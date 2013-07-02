package com.xingang.androidpp30;

public enum HistoryEnum {
    START_TIME("Start Time", "timestamp="),
    LATITUDE("Latitude", "latitude="),
    LONGITUDE("Longitude", "longitude="),
    ALTITUDE("Altitude", "altitude="),
    NETWORK_TYPE("Network Type", "networkType="),
    SIM_OPERATOR("SIM Operator", "simOperator="),
    GLOBAL_CELL_ID("Global Cell ID", "globalCellID="),    
    GSM_SIGNAL_STRENGTH("GSM Signal Strength (dBm)", "gsmSignalStrength="),
    GSM_BIT_ERROR_RATE("GSM Bit Error Rate", "gsmBitErrorRate="),
    CDMA_SIGNAL_STRENGTH("CDMA Signal Strength", "cdmaSignalStrength"),
    EVDO_RSSI("EVDO RSSI", "evdoRssi="),
    EVDO_SNR("EVDO SNR", "evdoSnr="),  
    LTE_SIGNAL_STRENGTH("LTE Signal Strength", "lteSignalStrength="),
    IS_ROAMING("Is Network Roaming", "isRoaming="),
    SERVICE_STATE("Service State", "serviceState="),    
    DATA_SERVICE_STATE("Data Service State" , "dataServiceState="),
    SPEED_TEST_MB_PER_SEC("Speed Test MB/sec", "kbPerSec="),    
    SPEED_TEST_TIME_DIFF("Speed Test Download Time sec", "timeDiff="),
    SPEED_TEST_MB("MegaBytes Transferred", "bytesTransferred="),
    PHONE_TYPE("Phone Type", "phoneType="),
    CALL_STATE("Call Statue", "callState="),
    IMSI("IMSI", "subscriberID="),  // Subscriber ID
    PHONE_NUMBER("Phone Number", "phoneNumber="),
    IMEI("IMEI", "deviceID="),  // Device ID    
    MANUFACTURER("Manufacturer", "manufacturer="),
    MODEL("Model", "model="),    
    SOFTWARE_VERSION("Software Version", "swVersion="),
    BUILD_VERSION("Build Version", "buildVersion="),    
    BATTERY("Battery Level", "battery="),
    AVAILABLE_MEMEORY("Available Memory MB", "availMemory="),
    IN_LOW_MEMORY_MODE("In Low Memeory Mode", "inLowMemMode="),


    
 
 
    
    WIFI_ENABLED("WIFI Enabled", "wifiEnabled="),
    WIFI_SIGNAL_STRENGTH("WiFi Signal Strength", "wifiSignalStrength="),
    WIFI_LINK_SPEED("WiFi Link Speed", "wifiLinkSpeed="),
    WIFI_LINK_MAC("WiFi Link MAC", "wifiLinkMac="),
    WIFI_LINK_SSID("WiFi Link SSID", "wifiLinkSSID="),
    


 
 


    Number_Dialed("Number Dialed",null),
    CDC("CDC",null),
    Signaling_Return("Signaling Return Code",null),
    Return_Code("Return Code",null),
    MOS_POLQA("MOS POLQA",null),
    Negotiated_Media("Negotiated Media",null),
    
    PDD("PDD",null),
    CST("CST",null),
    Bill_Duration("Bill Duration",null),
    Call_Duration("Call Duration",null),


//    MOS_UP("MOS Up Link", null),
//    MOS_DOWN("MOS Down Link", null),
    UPLINK_METRICS("Uplink Metrics", null),
    DOWNLINK_METRICS("Downlink Metrics", null);
    
    //active result
//  DIRECTION("DIRECTION","direction="),
//  PKT_RCVD("PKT_RCVD","pktRcvd="),
//  PKT_SENT("PKT_SENT","pktSent="),
//  PKT_LOST("PKT_LOST","pktLost="),
//  PKT_DISC("PKT_DISC","pktDisc="),
//  MOS_R("MOS_R","mosR="),
//  NL_XR("NL_XR","nlXr="),
//  RSL_XR("RSL_XR","rslXr="),
//  EPD("EPD","epd="),
//  EPL("EPL","epl="),
//  JTR_AVG("JTR_AVG","jtrAvg="),
    
//    testid("test id","testid="),


    
    
    
    private String m_name;
    private String m_urlName;
    
    HistoryEnum(String name, String urlName) {
    	m_name = name;
    	m_urlName = urlName;
    }
  
    public String toString() {
    	return m_name;
    }
    
    public String getUrlName() {
    	return m_urlName;
    }
    
    public static HistoryEnum fromString(String text) {
    	if (text != null) {       
    		for (HistoryEnum b : HistoryEnum.values()) {
    			if (text.equalsIgnoreCase(b.toString())) {
    				return b; 
    				} 
    			}
    		}
    	return null;
    } 
}
