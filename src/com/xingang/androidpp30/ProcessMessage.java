package com.xingang.androidpp30;

import java.text.DecimalFormat;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import com.xingang.androidpp30.SvrMsg.Direction;
import com.xingang.androidpp30.SvrMsg.DurationType;
import com.xingang.androidpp30.SvrMsg.MetricType;
import com.xingang.androidpp30.SvrMsg.MosPolqaType;

import de.opticom.xingang.PolqaCalculator;



public class ProcessMessage {
	private Context 				m_context;
	private boolean					m_bActiveCall = false;
	private HistoryItem				m_sipCallHistoryItem	= null;
	private Date					m_startDate;
	private String					m_upMos = null;
	private String					m_downMos = null;
	private boolean					m_bWiFi = false;
	private WifiManager				m_wiFiManager = null;
	private TelPhoneStateListener	m_phoneState = null;
	private IPhoneService			m_iPhoneService = null;
	private LinkedBlockingQueue<HistoryItem>    m_transferQue = new LinkedBlockingQueue<HistoryItem>();
	private PolqaCalculator polqaCalculator = null;
	final static String EOL = System.getProperty("line.separator"); 
	final static int    SPEED_SIZE = 104857;  // 1/10th MB
	final static String SPEED_TEST = "Phone Status with Speed Test";

	public ProcessMessage ( Context context, IPhoneService iPS, Handler handler ) {
		m_context = context;
		m_phoneState = new TelPhoneStateListener( context, handler );
		m_iPhoneService = iPS;
		m_wiFiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		polqaCalculator= new PolqaCalculator(context.getSystemService(Context.TELEPHONY_SERVICE));
		
	}
	public boolean processStatusMessages(LinkedList<String> statusList ) {
		while ( statusList.isEmpty() == false )  {
			String	statusStr = statusList.removeFirst();
			Log.d("PP30 server return", statusStr);
			
			SvrMsg msg = new SvrMsg( statusStr );
			//debug use
			m_iPhoneService.addLogMsg(msg.getMsg(), LogMsg.TYPE.DEBUG, LogMsg.CAT.INFO);
			
			if ( msg.getType() != SvrMsg.Type.NONE ) {
				if ( msg.getType() == SvrMsg.Type.REGISTRATION ) {
					m_iPhoneService.addLogMsg( msg.getMsg(), LogMsg.TYPE.UA, LogMsg.CAT.INFO);
				} else if ( msg.getType() == SvrMsg.Type.SIGNALLING  ) {
					m_iPhoneService.addLogMsg( msg.getMsg(), LogMsg.TYPE.CALL, LogMsg.CAT.INFO);
				}
			}
			if ( msg.getType() == SvrMsg.Type.SIGNALLING ) {
				m_phoneState.pauseListening();
				m_phoneState.startListening();				
				processSignalling( msg );
			}
			if ( msg.getType() == SvrMsg.Type.MEDIA && m_bActiveCall == true ) {
				// We found the Metrics  We should get 2 of these messages DO & OD
				processMetrics( msg );
			}
			if ( msg.getType() == SvrMsg.Type.POLQA_RESULT && m_bActiveCall == true ) {
				// We found the Metrics  We should get 2 of these messages DO & OD
				processMosPOLQA( msg );
			}
			if ( msg.getType() == SvrMsg.Type.CALL_DURATIOIN && m_bActiveCall == true ) {
				// We found the Metrics  We should get 2 of these messages DO & OD
				processCallDuration( msg );
			}

			if ( msg.getType() == SvrMsg.Type.CONNECTION  ) {
				return processConnection( msg );
			}
			if ( msg.getType() == SvrMsg.Type.PHONE_STATUS  || 
				 msg.getType() == SvrMsg.Type.SERVICE_STATE ) {
				m_phoneState.pauseListening();
				m_phoneState.startListening();
			    Date dateNow = new Date ();
			    SimpleDateFormat dateformatYYYYMMDD = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			    StringBuilder nowYYYYMMDD = new StringBuilder( dateformatYYYYMMDD.format( dateNow ) );
				Log.d("PP30Lite", "*** " +nowYYYYMMDD + "Processing new Phone Status Request ***");
				
				processPhoneStatus( msg,  msg.getType() );
			}
			if ( msg.getType() == SvrMsg.Type.TRANSFER ) {
				new TransferRequest().start();
			}
		}  
		return true;
	}

	
	private class TransferRequest extends Thread {
		
		public void run( ) {
			String urlBase = ServerSetup.getInstance().getPhoneStatusURL();
			String urlSave = urlBase + "collect/save";
			String params;
	
			try {
				DisableSSLCertificateCheckUtil.disableChecks();
			} catch ( Exception e ) {
				Log.d("PP30Lite", "Failed to disable HTTPS Host Verification: " + e.toString() );
			}
			
			while (m_transferQue.size() > 0 ) {
				HistoryItem hi = m_transferQue.element(); // Retrieve but do not remove element
				
				params = hi.buildUriPrams(  );
				params += "&create=Create";
				Log.d("PP30Lite", params);
				
				if ( sendPost( urlSave, params ) == true ) {
					// Item was transferred so we can not remove the transferred element
					try { 
						m_transferQue.take(); 
					} catch ( InterruptedException e ) {
						Log.d("PP30Lite", "Transfer Request Failed to take history item off Queue");
					}
				} else {
					// We never removed item from Queue so it will be there next time
					// so we can try again.
					break;  
				}
			} // End run
		}  // End TransferRequest
		
		private boolean sendPost( String urlStr, String params ) {
			try {
	
				// Send data
				URL url = new URL(urlStr);
				
				HttpURLConnection conn;
				if ( urlStr.contains("https") == true) {
					conn = (HttpsURLConnection)url.openConnection();
				} else {
					conn = (HttpURLConnection)url.openConnection();
				}
				conn.setConnectTimeout(10000);
				conn.setReadTimeout(10000);
				conn.setRequestMethod("POST");
	
				// Basic HTTP Authentication
				String userPassword = "admin" + ":" + "admin";
				String encoding = Base64Coder.encodeString(userPassword);   		    
				conn.setRequestProperty ("Authorization", "Basic " + encoding);
	
				conn.setDoOutput(true);
				
				// TODO if the writer times out I have no way of knowing
				//      we should return false if it times out so we can try
				//      again later.
				OutputStream outStream = conn.getOutputStream();
				OutputStreamWriter wr = new OutputStreamWriter( outStream );
				wr.write(params);
				wr.flush();

				conn.setConnectTimeout(10000);
				conn.setReadTimeout(10000);

				// Get the response
				// TODO
				// Ignore the response
				// If there is no connection then it blocks for several minutes and 
				// setReadTimeout(10000); does not seem to work
				InputStream inStream = conn.getInputStream();
				BufferedReader rd = new BufferedReader(new InputStreamReader(inStream));
				String line;
				while ((line = rd.readLine()) != null) {
					Log.d("PP30Lite",  line );
				}
				
				wr.close();
				rd.close();
				conn.disconnect();
				
			} catch (Exception e) {
				Log.d("PP30Lite",  "Error Transferring data.\n\n" + e.toString() );
				return false;
			} 		
			return true;
		}		
	}
	

	
	
	private class SpeedTest extends Thread {
		private HistoryItem m_hi = null; 
		
		public SpeedTest( HistoryItem hi ) {
			m_hi = hi;
		}

		public void run() {
			String url = ServerSetup.getInstance().getPhoneStatusURL() + 
					"images/penny.jpg";
			String ftp = ServerSetup.getInstance().getSpeedTestFtpURL();
			String login = ServerSetup.getInstance().getSpeedTestFtpLogin();
			String pw = ServerSetup.getInstance().getSpeedTestFtpPW();
			String filename = ServerSetup.getInstance().getSpeedTestFtpFilename();
		
			if ( ftp != null && ftp.length() != 0 ) {
				if ( filename.length()== 0 ) 
					filename = "penny.jpg";
				ftpSpeedTest( ftp, login, pw, filename );
			} else {
				urlSpeedTest( url );
			}
			
		}
		
		private void ftpSpeedTest( String ftpSite, String user, String pw, String filename ) {
			FTPClient ftpClient = new FTPClient();
			try { 
			ftpClient.connect(InetAddress.getByName(ftpSite));
			if(!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				ftpClient.disconnect();
				Log.d("PP30Lite",  "Failed to connect to FTP Site. Site: "  + 
				                                    ftpSite + " Login: " + user + " PW: " + pw + "\n");				

			}
			ftpClient.enterLocalPassiveMode();
			ftpClient.login(user, pw);
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			ByteArrayOutputStream buff = new ByteArrayOutputStream(1024*1024*4);
			long timeDiff=0;
			long start = System.currentTimeMillis();
			ftpClient.retrieveFile(filename, buff);
			timeDiff = System.currentTimeMillis() - start;
			
			if (timeDiff != 0 &  buff.size() != 0 ) {
				DecimalFormat myFormatter = new DecimalFormat("#.#");
			    String sec = myFormatter.format( ((float)timeDiff)/1000.0 );
			    String mb = myFormatter.format( ((float)buff.size())/1024.0/1024.0 );
				String mbPerSec = myFormatter.format( Float.valueOf(mb) / Float.valueOf(sec) );
				
				m_hi.setValue(HistoryEnum.SPEED_TEST_MB, mb);
				m_hi.setValue(HistoryEnum.SPEED_TEST_MB_PER_SEC, mbPerSec);
				m_hi.setValue(HistoryEnum.SPEED_TEST_TIME_DIFF, sec);
			}
			
			ftpClient.logout();
			}catch (IOException e ) {
				Log.d("PP30Lite",  "Error Transferring SpeedTest data.\n\n" + e.toString() );				
			} finally {
				if(ftpClient.isConnected()) {
					try {
						ftpClient.disconnect();
					} catch(IOException ioe) {
						// do nothing
					}
				}				
			}
			addHistoryItem(m_hi);	
		}
		
		private void urlSpeedTest(String urlSite ) {

			try {
				DisableSSLCertificateCheckUtil.disableChecks();
			} catch ( Exception e ) {
				Log.d("PP30Lite",  "Failed to disable HTTPS Host Verification: " + e.toString() );
			}

			try {
				// TODO convert to use apache HTTPClent instead of java HttpsURLConnection
				// Send data
				URL url = new URL(urlSite);
				
				HttpURLConnection conn;
				if ( urlSite.contains("https") == true) {
					conn = (HttpsURLConnection)url.openConnection();
				} else {
					conn = (HttpURLConnection)url.openConnection();
				}
				
				conn.setConnectTimeout(10000);
				conn.setReadTimeout(10000);
				conn.setRequestMethod("GET");

				// Basic HTTP Authentication
				String userPassword = "admin" + ":" + "admin";
				String encoding = Base64Coder.encodeString(userPassword);   		    
				conn.setRequestProperty ("Authorization", "Basic " + encoding);

				conn.setDoOutput(true);
				
				// Read file and measure time of transfer
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));			
				
				char store[] = new char[SPEED_SIZE]; // 1MB test
				long timeDiff=0;
				long start = System.currentTimeMillis();
				int size=0;
				int cnt = 0;
				while ((cnt = rd.read(store, 0,  SPEED_SIZE)) != -1 ) {
					size += cnt;
				}
				timeDiff = System.currentTimeMillis() - start;

				rd.close();
				conn.disconnect();

				
				if (timeDiff != 0 &  size != 0 ) {
					DecimalFormat myFormatter = new DecimalFormat("#.#");
				    String sec = myFormatter.format( ((float)timeDiff)/1000.0 ); // convert from ms to sec
				    String mb = myFormatter.format( ((float)size)/1024.0/1024.0 ); //convert from bytes to MB
					String mbPerSec = myFormatter.format( Float.valueOf(mb) / Float.valueOf(sec) );
					
					m_hi.setValue(HistoryEnum.SPEED_TEST_MB, mb);
					m_hi.setValue(HistoryEnum.SPEED_TEST_MB_PER_SEC, mbPerSec);
					m_hi.setValue(HistoryEnum.SPEED_TEST_TIME_DIFF, sec);
				}				
				
			} catch (Exception e) {
				Log.d("PP30Lite",  "Error Transferring SpeedTest data.\n\n" + e.toString() );
			} 
					
			addHistoryItem(m_hi);				
		}

	}
	
	
	// On Demand Speed Test 
	public void processSpeedTest( ) {
		SvrMsg msg = new SvrMsg(PhoneStatusTimerTask.PHONE_STATUS + 
					                Calendar.getInstance().getTimeInMillis()/1000 + 
					                SPEED_TEST);
//		m_iPhoneService.addLogMsg(msg.toString(), LogMsg.TYPE.DEBUG, LogMsg.CAT.INFO);

		HistoryItem	historyPS = new HistoryItem();


		setPhoneStateInfo( historyPS, msg );
		ConnectivityManager conMgr = (ConnectivityManager) m_context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = conMgr.getActiveNetworkInfo();
		if ( ni != null && ni.isConnected() ) {
			new SpeedTest( historyPS ).start( );	
		} else {
			// Add history item without the speed test results
			addHistoryItem(historyPS);
		}
	}
	
	private void processPhoneStatus( SvrMsg msg, SvrMsg.Type type ) { 
//		m_iPhoneService.addLogMsg(msg.toString(), LogMsg.TYPE.DEBUG, LogMsg.CAT.INFO);
		
		if ( type == SvrMsg.Type.PHONE_STATUS && 
			 ServerSetup.getInstance().isPhoneStatusEnabled() == true &&
			 ServerSetup.getInstance().isSpeedTestEnabled() == true ) {
			// Speed test does all Phone Status stuff along with 
			// a Speed Test
			processSpeedTest();
			return;
		}

		HistoryItem				historyPS	= null;

		historyPS = new HistoryItem();

		setPhoneStateInfo( historyPS, msg );
	
		// Pass History Item to History Queue
		addHistoryItem( historyPS );
	}

	private void setPhoneStateInfo( HistoryItem hi, SvrMsg msg ) {
		hi.setValue(HistoryEnum.START_TIME, String.valueOf( msg.getTimeStampDate().getTime( ) ) );

		// Start building our Call Info String
		Location loc = m_phoneState.getLocation();
		if ( loc != null ) {
			hi.setValue(HistoryEnum.LATITUDE, String.valueOf(loc.getLatitude()));
			hi.setValue(HistoryEnum.LONGITUDE, String.valueOf(loc.getLongitude()));
			hi.setValue(HistoryEnum.ALTITUDE, String.valueOf(loc.getAltitude()));
		}

		ConnectivityManager conMgr = (ConnectivityManager) m_context.getSystemService(Context.CONNECTIVITY_SERVICE);
		m_bWiFi =  ( conMgr.getActiveNetworkInfo() != null &&
				     conMgr.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI );
		hi.setValue(HistoryEnum.WIFI_ENABLED,  m_bWiFi==true?"Yes":"No" );
		if ( m_bWiFi == true ) {
			int rssi = m_wiFiManager.getConnectionInfo().getRssi();
			hi.setValue(HistoryEnum.WIFI_SIGNAL_STRENGTH, Integer.toString(WifiManager.calculateSignalLevel(rssi, 6)));
			hi.setValue(HistoryEnum.WIFI_LINK_SPEED, Integer.toString(m_wiFiManager.getConnectionInfo().getLinkSpeed()));
			hi.setValue(HistoryEnum.WIFI_LINK_MAC, m_wiFiManager.getConnectionInfo().getMacAddress());
			hi.setValue(HistoryEnum.WIFI_LINK_SSID, m_wiFiManager.getConnectionInfo().getSSID());
		}
		

		
		if ( m_phoneState.isGsm() == true ) {
			// Convert the Android ASU Signal Strenght to dBm
			try {
				//99 represents invalid or no GSM Sig Registered  See Android API for details
				if (m_phoneState.getGsmSigStrength().equals("99") == false) {  
					int sig = Integer.parseInt(m_phoneState.getGsmSigStrength());
					//Convert to dBm
					hi.setValue(HistoryEnum.GSM_SIGNAL_STRENGTH,  Integer.toString(sig * 2 - 113));
				}
				else {
					hi.setValue(HistoryEnum.GSM_SIGNAL_STRENGTH, "99");
				}
			} catch (NumberFormatException e  ) {
				hi.setValue(HistoryEnum.GSM_SIGNAL_STRENGTH, "99");
			}
			if ( m_phoneState.getGssmErrorBitRate().equals("-1") == false  ) {
				hi.setValue(HistoryEnum.GSM_BIT_ERROR_RATE, m_phoneState.getGssmErrorBitRate());
			}
		} else if ( m_phoneState.getEvdoDbm() != "-1" ) {
			hi.setValue(HistoryEnum.EVDO_RSSI, m_phoneState.getEvdoDbm());
			hi.setValue(HistoryEnum.EVDO_SNR, m_phoneState.getEvdoSnr());			
		} else {
			// I hope it is CDMA
			hi.setValue(HistoryEnum.CDMA_SIGNAL_STRENGTH, m_phoneState.getCdmaSigStrength());
		}
		// TBD Need to implement LTE


		hi.setValue(HistoryEnum.GLOBAL_CELL_ID, m_phoneState.getGCID());
		hi.setValue(HistoryEnum.BATTERY, m_phoneState.getBatteryLife());

		// Memory
		ActivityManager mgr = (ActivityManager) m_context.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo memInfo = new ActivityManager.MemoryInfo();
		mgr.getMemoryInfo(memInfo);
		DecimalFormat myFormatter = new DecimalFormat("#.#");
		String val = myFormatter.format(  memInfo.availMem / 1024.0 / 1024.0  );
		hi.setValue(HistoryEnum.AVAILABLE_MEMEORY, String.valueOf(val));
		hi.setValue(HistoryEnum.IN_LOW_MEMORY_MODE, memInfo.lowMemory ? "Yes" : "No");
		
		


		// Device Info
		hi.setValue(HistoryEnum.PHONE_TYPE, m_phoneState.getPhoneType());
		hi.setValue(HistoryEnum.NETWORK_TYPE,m_phoneState.getNetworkType());
		hi.setValue(HistoryEnum.SIM_OPERATOR, m_phoneState.getSimOpName());
		hi.setValue(HistoryEnum.IMSI, m_phoneState.getSubscriberID());
		hi.setValue(HistoryEnum.IMEI, m_phoneState.getDeviceID());
		hi.setValue(HistoryEnum.MANUFACTURER, android.os.Build.MANUFACTURER);
		hi.setValue(HistoryEnum.MODEL, android.os.Build.MODEL);
		hi.setValue(HistoryEnum.IS_ROAMING, m_phoneState.getRoaming());
		hi.setValue(HistoryEnum.SOFTWARE_VERSION, m_phoneState.getSoftwareVer());
		hi.setValue(HistoryEnum.BUILD_VERSION, Build.VERSION.RELEASE);
		hi.setValue(HistoryEnum.PHONE_NUMBER, m_phoneState.getPhoneNumber());
		hi.setValue(HistoryEnum.DATA_SERVICE_STATE, m_phoneState.getDataState());
		hi.setValue(HistoryEnum.SERVICE_STATE, m_phoneState.getServiceState());
		hi.setValue(HistoryEnum.CALL_STATE, m_phoneState.getCallState());
		
	}
	



	private boolean processConnection( SvrMsg msg ) {
		if ( msg.getLevel() == SvrMsg.Level.ERROR ) {
			// Server lost connection so lets restart the server.	
			m_iPhoneService.addLogMsg("Lost Server Connection, so we will restart the server", LogMsg.TYPE.DEBUG, LogMsg.CAT.WARN);
			return false;
		} 
		return true;
	}


	private void processSignalling( SvrMsg msg ) {
		if ( msg.getLevel() == SvrMsg.Level.INFORMATION ) {
			if(msg.getMsg().contains( "Incoming call from") == true ){
				// We found the SIP From URL stick it in the Call Info
				processCallStart(msg);
			}else if(msg.getMsg().contains( "Outgoing call to") == true ){
				//Outgoing call to
				processCallStart(msg);
			}
			
//			int i = msg.getMsg().indexOf( "from" );
//			String uri = msg.getMsg().substring(i + 5);
//			uri = uri.trim();
			// TODO
			//m_callInfoStr.append("From: " + uri + EOL );
		} else if ( msg.getLevel() == SvrMsg.Level.SUCCESS || msg.getLevel() == SvrMsg.Level.ERROR) {
			// This should be the end of the call
			m_bActiveCall = false;
			
			addHistoryItem(m_sipCallHistoryItem);
		}    	
	}

	private void processCallStart( SvrMsg msg ) { 
		m_bActiveCall = true;
		m_upMos = null;
		m_downMos = null;
		m_startDate = new Date();
		m_startDate.setTime( msg.geteDate().getTime() );
		m_sipCallHistoryItem = new HistoryItem();
		
		setPhoneStateInfo( m_sipCallHistoryItem, msg );		
	}
	
	private void processCallDuration( SvrMsg msg ) {
		m_sipCallHistoryItem.setValue(HistoryEnum.PDD, msg.getDurationMapValue(DurationType.PDD.toString()));
		m_sipCallHistoryItem.setValue(HistoryEnum.CST, msg.getDurationMapValue(DurationType.CST.toString()));
		m_sipCallHistoryItem.setValue(HistoryEnum.Bill_Duration, msg.getDurationMapValue(DurationType.Bill_Dur.toString()));
		m_sipCallHistoryItem.setValue(HistoryEnum.Call_Duration, msg.getDurationMapValue(DurationType.Call_Dur.toString()));
	}
	
	private void processMosPOLQA( SvrMsg msg ) {
//		String MOS_POLQA=msg.getMosPolqaMapValue(MosPolqaType.MOS_POLQA.toString());
		String referenceFilename="/sdcard/xingang/androidpp30/"+msg.getMosPolqaMapValue(MosPolqaType.REF_FILE.toString());
		String testFilename=msg.getMosPolqaMapValue(MosPolqaType.TEST_FILE.toString());
		String POLQA_STPOS=msg.getMosPolqaMapValue(MosPolqaType.POLQA_STPOS.toString());
		String POLQA_LENGTH=msg.getMosPolqaMapValue(MosPolqaType.POLQA_LENGTH.toString());
		new CalMosValue(referenceFilename,testFilename,POLQA_STPOS,POLQA_LENGTH).start();
	}
	
	private void processMetrics( SvrMsg msg ) {
 
			if ( msg.getDir() == SvrMsg.Direction.DO ) {
				m_sipCallHistoryItem.setValue(HistoryEnum.DOWNLINK_METRICS, msg.getFormatedMetrics());
//				m_downMos = msg.getMos();
//				m_sipCallHistoryItem.setValue(HistoryEnum.MOS_DOWN, m_downMos);
			} else if ( msg.getDir() == SvrMsg.Direction.OD ) {
				m_sipCallHistoryItem.setValue(HistoryEnum.UPLINK_METRICS, msg.getFormatedMetrics());
//				m_upMos = msg.getMos();
//				m_sipCallHistoryItem.setValue(HistoryEnum.MOS_UP, m_upMos);
			} 
			//set active result
//			if(m_sipCallHistoryItem!=null){
//				m_sipCallHistoryItem.setValue(HistoryEnum.testid, msg.getMetricsValue(MetricType.testid));
//				m_sipCallHistoryItem.setValue(HistoryEnum.DIRECTION, msg.getDir().name());
//				m_sipCallHistoryItem.setValue(HistoryEnum.PKT_RCVD, msg.getMetricsValue(MetricType.PKT_RCVD));
//				m_sipCallHistoryItem.setValue(HistoryEnum.PKT_SENT, msg.getMetricsValue(MetricType.PKT_SENT));
//				m_sipCallHistoryItem.setValue(HistoryEnum.PKT_LOST, msg.getMetricsValue(MetricType.PKT_LOST));
//				m_sipCallHistoryItem.setValue(HistoryEnum.PKT_DISC, msg.getMetricsValue(MetricType.PKT_DISC));
//				m_sipCallHistoryItem.setValue(HistoryEnum.MOS_R, msg.getMetricsValue(MetricType.MOS_R));
//				m_sipCallHistoryItem.setValue(HistoryEnum.NL_XR, msg.getMetricsValue(MetricType.NL_XR));
//				m_sipCallHistoryItem.setValue(HistoryEnum.RSL_XR, msg.getMetricsValue(MetricType.RSL_XR));
//				m_sipCallHistoryItem.setValue(HistoryEnum.EPD, msg.getMetricsValue(MetricType.EPD));
//				m_sipCallHistoryItem.setValue(HistoryEnum.EPL, msg.getMetricsValue(MetricType.EPL));
//				m_sipCallHistoryItem.setValue(HistoryEnum.JTR_AVG, msg.getMetricsValue(MetricType.JTR_AVG));
//			}
		
	}
	
	public void setActiveCall(boolean b) {
		m_bActiveCall = false;
	}
	
	public void addHistoryItem( HistoryItem hi ) {
		if(hi!=null){
			m_transferQue.add( hi );
			m_iPhoneService.addHistoryItem(hi);
			Log.d("PP30Lite", "### Adding History Item to Phone Service List ###");
		}
		//add to history and set to null, if call this method again, not duplicate
		hi=null;
	}
	public void close() {
		if ( m_phoneState != null ) {
			m_phoneState.close();
		}
		
	}

	private class CalMosValue extends Thread{
		private String referenceFilename;
		private String testFilename;
		private String POLQA_STPOS;
		private String POLQA_LENGTH;
		public CalMosValue(String referenceFilename,String testFilename,String POLQA_STPOS,String POLQA_LENGTH){
			this.referenceFilename=referenceFilename;
			this.testFilename=testFilename;
			this.POLQA_STPOS=POLQA_STPOS;
			this.POLQA_LENGTH=POLQA_LENGTH;
		}
		public void run( ) {
			//set m_sipCallHistoryItem_DO,m_sipCallHistoryItem_OD to null, make sure 
			//processSignalling method didn't add historyitem again
			HistoryItem tmp_m_sipCallHistoryItem= m_sipCallHistoryItem;
			m_sipCallHistoryItem=null;
			long t1 = System.currentTimeMillis();
			int result= polqaCalculator.Calc(referenceFilename, testFilename,POLQA_STPOS,POLQA_LENGTH);
			long t2 = System.currentTimeMillis();
			double polqaRunDuration = (t2 - t1)/1000.0;
			if(result==0){
				float polqavalue= polqaCalculator.getResultData().mfMOSLQO;
				tmp_m_sipCallHistoryItem.setValue(HistoryEnum.MOS_POLQA, String.valueOf(polqavalue));
				m_iPhoneService.addLogMsg( "Polqa value:" + polqavalue + String.format(";POLQA Run Duration: %.3f s\n", polqaRunDuration), LogMsg.TYPE.DEBUG, LogMsg.CAT.INFO);

			}else{
				m_iPhoneService.addLogMsg("Error when calculate MOS POLQA value.", LogMsg.TYPE.DEBUG, LogMsg.CAT.CRITICAL);
			}
			addHistoryItem(tmp_m_sipCallHistoryItem);
		}
	}
	
	public void processQuickTest(Handler hm) {
		new QuickTest(hm).start();
	}

	private class QuickTest extends Thread {
		private Handler hm;
		
		public QuickTest(Handler hm){
			this.hm=hm;
		}
 
		public void run( ) {
			//call native method SipPP30Rtptest
			 SharedPreferences sipTab = m_context.getSharedPreferences(SettingsActivity.PREFS_NAME, 0);
			 	String phoneNumber=sipTab.getString("phoneNumberEdit", "");
		        String regIP = sipTab.getString("registrarIPEdit", "");
		        if (regIP.length() == 0 ) {
		        	regIP = sipTab.getString("proxyIPEdit", "");
		        }
		        String regPort = sipTab.getString("registrarPortEdit", "");
		        
		        String codec = sipTab.getString("codecEdit", "");
		        
		        StringBuffer	sXmlStr = new StringBuffer ( );
		        sXmlStr.append("-c mobile_voice_quality -a\"SIP:");
		        sXmlStr.append(phoneNumber+"@"+regIP+":"+regPort+"\"");
		        sXmlStr.append(" -L " + UtilityBelt.getLocalIpAddress());
		        sXmlStr.append(" --codec " + codec);
		        sXmlStr.append(" --test_id 0000");
		        
		        Log.d("PP30Lite", "QT:  send quick test command"+new String(sXmlStr));
		        String returnval=SipPP30Rtptest(new String(sXmlStr));  
		        String qtlogmsg="QT:send quick test command:"+new String(sXmlStr)+";return:"+returnval;
		        m_iPhoneService.addLogMsg(qtlogmsg, LogMsg.TYPE.DEBUG, LogMsg.CAT.INFO);
		        Message msg = hm.obtainMessage();
		        msg.obj = returnval;
		        hm.sendMessage(msg); 
		}
		
	}
	public native String SipPP30Rtptest (String quickTestCommand);

}
