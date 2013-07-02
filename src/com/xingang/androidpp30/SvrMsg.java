package com.xingang.androidpp30;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class SvrMsg {
	private Map<MetricType, Metric> m_metrics;
	
	public enum BackBone { WIFI, DATAPLAN };
	private BackBone m_backBone;
	
	public enum Type { 
		NONE("NONE"), 
		REGISTRATION("REG"), 
		CONNECTION("CON"), 
		SIGNALLING("SIG"),
		MEDIA("MED"), 
		QUIT("QT"), 
		GENERIC("GEN"),
		PHONE_STATUS("PS"),
		TRANSFER("TS"),
		SERVICE_STATE("SS"),
		POLQA_RESULT("RT"),
		CALL_DURATIOIN("DU");
		private final String s;
		Type( String str ) {
			s = str;
		}
		public String toString() {
			return new String(s);
		}
	};
	private Type    m_type = Type.NONE;
	
	public enum Level { 
		NONE("NONE"), 
		DEBUG("DEBUG"), 
		SUCCESS("SUC"), 
		INFORMATION("INFO"), 
		WARNING("WARN"), 
		ERROR("ERR");
		private final String s;
		Level ( String str ) {
			s = str;
		}
		public String toString() {
			return new String ( s );
		}
	};
	private Level	m_level = Level.NONE;
	
	public enum Direction { NONE, OD, DO };
	private Direction  m_direction = Direction.NONE;
	
	public enum MetricType {DIRECTION, PKT_RCVD, PKT_SENT, PKT_LOST, PKT_DISC, MOS_R, NL_XR, RSL_XR, EPD, EPL, JTR_AVG };
	
	public enum MosPolqaType { testid, MOS_POLQA , REF_FILE, POLQA_STPOS, POLQA_LENGTH ,TEST_FILE};
	private Map<String, String> mosPolqaMap;
	
	public enum DurationType { CST , PDD, Bill_Dur, Call_Dur};
	private Map<String, String> dorationMap;
	
	private String 	m_msg;
	private Date 	m_date;
	
	final static String EOL = System.getProperty("line.separator"); 
	
	public SvrMsg( String msg ) {
		buildMetricMap();
		parseMsg( msg );
	}
	
	public String toString() {
		return new String ( m_type.toString() + ":" + 
							m_level.toString() + ":" +
							UtilityBelt.getTimeStampStr(m_date) + ":" + 
							getMsg() + EOL );
	}
	
	public String getMos() {
		return new String (((Metric)m_metrics.get(MetricType.MOS_R)).val);
	}
	
	public Date geteDate() {
		return m_date;
	}

	public Date getTimeStampDate() {
		if ( m_date == null ) {
			m_date = new Date();
		}
		return  m_date; 
	}	
	public String getMsg() { 
		return m_msg;
	}
	public Direction getDir() {
		return m_direction;
	}
	public String getFormatedMetrics() {
		if ( getType() == Type.MEDIA ) {
			StringBuffer sb = new StringBuffer();
			for( MetricType mt : MetricType.values() ) {
				Metric m = (Metric)m_metrics.get(mt);
//				if ( m != null && !"DIRECTION".equals(m.fromSvr) ) {
					if ( m != null) {
					String s = m.toString();
					sb.append( s );
					if ( s.contains(EOL) != true ){
						sb.append( "NP" + EOL );
					}
				}
			}
			return new String(sb.toString());
		}
		return null;
	}
	
	private void buildMetricMap() {
		m_metrics = new HashMap<MetricType, Metric>();
		
		Metric m = new Metric ("DIRECTION", "DIRECTION: ");
		m_metrics.put( MetricType.DIRECTION, m );
		m = new Metric ("PKT_RCVD", "Packets Received: ");
		m_metrics.put( MetricType.PKT_RCVD, m );
		m = new Metric ("PKT_SENT", "Packets Sent: ");
		m_metrics.put( MetricType.PKT_SENT, m );
		m = new Metric ("PKT_LOST", "Packets Lost: ");
		m_metrics.put( MetricType.PKT_LOST, m );
		m = new Metric ("PKT_DISC", "Packets Discarded: ");
		m_metrics.put( MetricType.PKT_DISC, m );
		m = new Metric ("MOS_R", "MOS R: ");
		m_metrics.put( MetricType.MOS_R, m );
		m = new Metric ("NL_XR", "Noise Level(db): ");
		m_metrics.put( MetricType.NL_XR, m );
		m = new Metric ("RSL_XR", "Signal Level(db): ");
		m_metrics.put( MetricType.RSL_XR, m );
		m = new Metric ("EPD", "Echo Path Delay(ms): ");
		m_metrics.put( MetricType.EPD, m );
		m = new Metric ("EPL", "Echo Path Loss(db): ");
		m_metrics.put( MetricType.EPL, m );
		m = new Metric ("JTR_AVG", "Jitter Average(ms): ");
		m_metrics.put( MetricType.JTR_AVG, m );

	}

	public class Metric {
		public String fromSvr;
		public String formatted;
		public String val;
		public Metric ( String svr, String form ) {
			fromSvr = svr;
			formatted = form;
		}
		public String toString() {
			return new String(formatted);
		}
	}
	
	public void setBackBone( BackBone b ) {
		m_backBone = b;
	}
	
	public BackBone getBackBone() {
		return m_backBone;
	}
	
	public void setLevel( String level ) {
		if ( level.compareTo("DB") == 0 ) {
			m_level = Level.DEBUG;
		} else if ( level.compareTo("SC") == 0 ) {
			m_level = Level.SUCCESS;
		} else if ( level.compareTo("IF") == 0 ) {
			m_level = Level.INFORMATION;
		} else if ( level.compareTo("WN") == 0 ) {
			m_level = Level.WARNING;
		} else if ( level.compareTo("ER") == 0 ) {
			m_level = Level.ERROR;
		}
	}
	
	public Level getLevel() {
		return m_level;
	}
	
	public void setType( String type ) {
		if ( type.compareTo("RG") == 0 ) {
			m_type = Type.REGISTRATION;
		} else if ( type.compareTo("CN") == 0 ) {
			m_type = Type.CONNECTION;
		} else if ( type.compareTo("CS") == 0 ) {
			m_type = Type.SIGNALLING;
		} else if ( type.compareTo("MD") == 0 ) {
			m_type = Type.MEDIA;
		} else if ( type.compareTo("QT") == 0 ) {
			m_type = Type.QUIT;
		} else if ( type.compareTo("GN") == 0 ) {
			m_type = Type.GENERIC;
		} else if ( type.compareTo("PS") == 0 ) {
			m_type = Type.PHONE_STATUS;
		}else if ( type.compareTo("TS") == 0 ) {
			m_type = Type.TRANSFER;
		} else if ( type.compareTo("SS") == 0 ) {
			m_type = Type.SERVICE_STATE;
		}else if ( type.compareTo("RT") == 0 ) {
			m_type = Type.POLQA_RESULT;
		}else if ( type.compareTo("DU") == 0 ) {
			m_type = Type.CALL_DURATIOIN;
		}
	}
	
	public Type getType() {
		return m_type;
	}
	
	// parse status
	/*		new status string format:	 
			TYPE:LEVEL:TIME:MESSAGE
			 
			Each field is separated with a colon:
			 
			TYPE field is a two character field with the following values.
			 
			"RG"        //    Registration related
			"CN"        //    Connection (sip socket) related
			"CS"        //    Call Signalling Related
			"MD"        //    Media Related
			"QT"        //    Quit (Special Quit) status thread message
			"GN"        //    generic message
			"PS"		//    Phone Status
			"TS"		//    Transfer Status
			 
			Currently there are no "GN" or "MD" messages but I intend to add "MD" ones during and at the end of a call.
			 
			 
			LEVEL field is a two character field with following values
			 
			"DB"        //    debug (none used at the moment)
			"SC"        //    success
			"IF"        //    informational
			"WN"        //    warning (none used at the moment)
			"ER"        //    error
			 
			TIME  is a 10 character number field (leading zeros) which is the GMT time in seconds.  You need to convert it to local time and display the time and/or date in the format
			you want.
			 
			A "SC" (success) level message indicates a successful completion. 
			For a Registration Type message it means the registration completed successful. 
			For a Connection Type  message it means the socket is opened.
			For a Call Signalling Type message it means the call completed successfully.
			 
			A "ER" (error) level message indicates a failed completion.
			For a Registration Type message it means the registration failed.. 
			For a Connection Type  message it means the socket is failed (shouldn't happen with UDP).
			For a Call Signalling Type message it means the call ended with an error.
			 
			A "IF" (informational) indicates progression messages.
			 
			A "WN" (warning) indicates progression with a slight negative tent.
	*/
	private void parseMsg( String msg ) {
		if ( msg.charAt(2)== ':' && msg.charAt(5)  == ':' && msg.charAt(16) == ':' ) {
			String typeStr = msg.substring(0, 2);
			setType( typeStr );
			
			String levelStr = msg.substring(3, 5);
			setLevel( levelStr );
			
			String timeStr = msg.substring (6, 16);
			// convert time string (sec) into milliseconds and then to a date object
			m_date = new Date(Long.valueOf(timeStr)*1000);
			m_msg = msg.substring(17) + EOL;
			
			if ( getType() == Type.CONNECTION && getLevel() == Level.SUCCESS ) {
				// Find out if this is a WiFi or Wireless Connection
				if ( m_msg.contains("PPP") == true ) {
					// Data Plan Call
					setBackBone( BackBone.DATAPLAN );
				} else {  // ETH or WiFi
					// WiFi
					setBackBone( BackBone.WIFI );
				}
					
			} else if ( getType() == Type.PHONE_STATUS ) {
				setBackBone ( BackBone.DATAPLAN );
			}
			
			if (getType() == Type.MEDIA ) {
				parseMetrics( m_msg );
			}
			if (getType() == Type.POLQA_RESULT ) {
				parseMOSPOLQA( m_msg );
			}
			if (getType() == Type.CALL_DURATIOIN ) {
				parseDuration( m_msg );
			}
		}
	
	}
	private void parseDuration(String msg) {
		dorationMap=new HashMap<String,String>();
		String str[] = msg.split( " " );
		for ( int i = 0; i < str.length; i++) {
			String val[] = str[i].split("=");
			if ( val.length != 2 ) {
				continue;
			}
			dorationMap.put(val[0], val[1].trim());
		}
	}
	
	
	private void parseMOSPOLQA(String msg) {
		mosPolqaMap=new HashMap<String,String>();
		String str[] = msg.split( " " );
		for ( int i = 0; i < str.length; i++) {
			String val[] = str[i].split("=");
			if ( val.length != 2 ) {
				continue;
			}
			mosPolqaMap.put(val[0], val[1].trim());
		}
	}

	private void parseMetrics(String msg) {
		String metrics[] = msg.split( " " );
		for ( int i = 0; i < metrics.length; i++) {
			for( Iterator<MetricType> it = m_metrics.keySet().iterator(); it.hasNext(); ) {
				MetricType key = (MetricType)it.next();
				Metric m = (Metric)m_metrics.get(key);
				if ( metrics[i].contains( m.fromSvr+"=" ) ) {
					String val[] = metrics[i].split("=");
					if ( val.length != 2 ) {
						continue;
					}

					 if ( key == MetricType.MOS_R ) {
						if ( val[1].length() > 3 ) {
							m.val = val[1].substring(0, 3);  // 4.12345 -> 4.1
							m.formatted = m.formatted + m.val + EOL;
						} else {
							m.val = "NP";
							m.formatted = m.formatted + "NP" + EOL;
						}
					} else {
						if ( key == MetricType.DIRECTION ) { //Direction
							if ( val[1].contains("OD") ) {
								m_direction = Direction.OD;
							} else if ( val[1].contains("DO") ) {
								m_direction = Direction.DO;
							}
						}
						m.val = val[1].trim();
						m.formatted = m.formatted + m.val + EOL;
					}
				}
			}
		}
		
		// Post Processing
		// Packet Loss needs to show total and %
		// clac % = loss.val / received.val
		Metric lost = (Metric)m_metrics.get(MetricType.PKT_LOST);
		Metric rcv = (Metric)m_metrics.get(MetricType.PKT_RCVD);
		appendPercent( lost, rcv );
		
		// Packet Discarded needs to show total and %
		// clac % = discard.val / received.val
		Metric disc = (Metric)m_metrics.get(MetricType.PKT_DISC);
		appendPercent( disc, rcv );		

	}
	
	private void appendPercent(Metric appendTo, Metric denom ) {
		double percLost = 0;
		try {

			double numeratorVal = Double.parseDouble(appendTo.val);
			double denominatorVal = Double.parseDouble(denom.val);
			if ( numeratorVal != 0 ) {
				percLost = numeratorVal / denominatorVal;
			}
			
		} catch (Exception e ) {
			// Do not show % if calc failed.
			System.out.println("Failed to Calculate the percentage of Lost packets.");
		}
		NumberFormat form;
		form = NumberFormat.getPercentInstance(Locale.getDefault()); 
		if (form instanceof DecimalFormat) {
		    ((DecimalFormat) form).setDecimalSeparatorAlwaysShown(false);
		}
		appendTo.formatted = appendTo.formatted.trim();
	    appendTo.formatted = appendTo.formatted + ": " + form.format(percLost) + EOL;		
	}
	
	public String getMetricsValue(MetricType mt){
		Metric m = (Metric)m_metrics.get(mt);
		if(m!=null){
			return m.val;
		}
		return null;
	}
	
	public String getMosPolqaMapValue(String s){
		return mosPolqaMap.get(s);
	}
	public String getDurationMapValue(String s){
		return dorationMap.get(s);
	}

}
