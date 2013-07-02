package com.xingang.androidpp30;

import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;


interface IPhoneService {
	public int getLogMsgs( Collection<LogMsg> c );
	public void addLogMsg( String s, LogMsg.TYPE t, LogMsg.CAT c );
	
	
	public int getHistoryItems( Collection<HistoryItem> c );
	public void addHistoryItem( HistoryItem hi );	
	
}

public class PhoneService extends Service implements Runnable, IPhoneService {
	private final IBinder 		m_binder = new PhoneServiceBinder();
    private NotificationManager m_NM;
    private int NOTIFICATION = R.string.phoneServiceStarted;  
    
	private PhoneService			m_phoneService;
	private PowerManager 			m_powerMgr;
	private PowerManager.WakeLock 	m_wakeLock;
	private SipPP30StatusThread		m_statusThread 	= null;
	private MsgHandler 				m_handler 		= null; 
	private Timer					m_phoneStatusTimer = null;
	private Timer					m_transferTimer = null;
	private PhoneStatusTimerTask	m_phoneStatusTimerTask = null;
	private TransferTimerTask		m_transferTimerTask = null;
	private	ProcessMessage  		m_processMessage = null;
	
	public final static int         MAX_LOG_MSGS = 50;
	public final static int			MAX_HISTORY_MSGS = 50;
	private LinkedBlockingQueue<LogMsg>     	m_logQue = new LinkedBlockingQueue<LogMsg>();
	private LinkedBlockingQueue<HistoryItem>    m_historyQue = new LinkedBlockingQueue<HistoryItem>();
	private boolean					m_bProcessInProgress = false;
	private final Handler 			m_stopTimeHandler = new Handler();
	private Timer 					m_stopTimeTimer = new Timer();
	private TimerTask				m_stopTimeTimerTask;

	final static String EOL = System.getProperty("line.separator"); 

    @Override
    public void onCreate() {
        m_NM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
        
 		m_powerMgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		m_wakeLock = m_powerMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PP30Lite(a)");	

 		m_processMessage= new ProcessMessage( getApplicationContext(), (IPhoneService)this, getHandler());       
    }

	@Override
	public IBinder onBind(Intent arg0) {
		return m_binder;
	}
	

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("PP30Lite", "Phone Service onStartCommand");
		runSvr();
        return START_STICKY;
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		stopSvr();
		// Cancel the persistent notification.
        m_NM.cancel(NOTIFICATION);

		Log.d("PP30Lite", "Phone Service onDestroy");
	}
	
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.phoneServiceStarted);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.icon, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, AndroidPP30.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.phoneServiceStarted),
                       text, contentIntent);

        // Send the notification.
        //m_NM.notify(NOTIFICATION, notification);
        
        startForeground(4321, notification);
    }


    
	
	public class PhoneServiceBinder extends Binder {
		PhoneService getService() {
			Log.d("PP30Lite", "Phone Service Bound");

			return PhoneService.this;
		}
	}
	
	class MsgHandler extends Handler {
		@Override
		public void handleMessage(Message msg ) {
			try { 
				if (msg.getData().get("type") == "collect" &&
					ServerSetup.getInstance().isServiceStatusEnabled() == true) {
					Log.d("PP30Lite", "Call State Update:  Collect now");
					LinkedList<String> statusList = new LinkedList<String>();
					long time = Calendar.getInstance().getTimeInMillis();
					statusList.add( "SS:SC:" + time/1000 +
							        ":Service State Status Test" );				
					m_processMessage.processStatusMessages( statusList );
				}
			} catch (Exception e ) {
				Log.d("PP30Lite", "Call State Update:  Failed to Collect now");
			}
		}
	}
	
	private Handler getHandler() {
		if ( m_handler == null ) {
			m_handler =  new MsgHandler();
			MyPhoneStateListener.m_handler = m_handler;
		}
		return m_handler;
	}
	
	public int getLogMsgs( Collection<LogMsg> c ) {
		int size = m_logQue.size();
		if ( size > 0 ) {
			m_logQue.drainTo(c);
		}
		return size;
	}


	public void addLogMsg( String s, LogMsg.TYPE t, LogMsg.CAT c ) {
		try {
			while ( m_logQue.size() >= MAX_LOG_MSGS ) {
				m_logQue.take();
			}
			m_logQue.put(new LogMsg(s, t, c));
		} catch ( Exception e ) {
			Log.d("PP30Lite", "Adding message to Log failed");
		}		
	}


	public void processSpeedTest() {
		m_processMessage.processSpeedTest();
	}
	
	public void processQuickTest(Handler hm) {
		// Check if sip init
		if ( m_statusThread == null ) {
			Toast.makeText(getApplicationContext(),
					"SIP Server is not running.",
					Toast.LENGTH_LONG).show();
		}else{
			m_processMessage.processQuickTest(hm);	
		}
		
	}
	
	public int getHistoryItems( Collection<HistoryItem> c ) {
		int size = m_historyQue.size();
		if ( size > 0 ) {
			m_historyQue.drainTo(c);
		}
		return size;
	}
	
	public void addHistoryItem( HistoryItem hi ) {
		m_bProcessInProgress = false;
		try {
			while ( m_historyQue.size() >= MAX_LOG_MSGS ) {
				m_historyQue.take();
			}
			m_historyQue.put(hi);
			Log.d("PP30Lite", "^^^ Phone Service History Que Size = " + String.valueOf(m_historyQue.size()) );
		} catch ( Exception e ) {
			Log.d("PP30Lite", "Adding history item to Log failed");
		}
	}	


	private void runSvr() {
		runStopInXMinTimer();
		runSipSvr();
		runPhoneSvr();
	}
	
	// Stops service at user configured interval
	private void runStopInXMinTimer() {
		int stopTime = ServerSetup.getInstance().getStopServiceTimeInMin() * 60 * 1000;

		if (stopTime <= 0 ) {
			// Value of 0 means run forever
			return;
		}
		m_stopTimeTimerTask = new TimerTask() {
			public void run() {
				m_stopTimeHandler.post( new Runnable() {
					public void run() {
						// stop the service
						Log.d("PP30Lite", "Stop Service Timer Executed.");
						
						// service may not be able to stop itself if another 
						// process is bound to it (needs to be tested)
						stopSelf();
					}
				});
			}
		};
		m_stopTimeTimer.schedule(m_stopTimeTimerTask, stopTime);
	}
	

	// Start the SIP server thread
	private boolean runSipSvr()  {
		int	nLogLevel 		= 31; // 31;  //= 15;	//	info and below
		int	nSipMsgLogLevel = 16; //16;  //= 16;	//	debug level
		String xml =  UtilityBelt.buildSipConfigXml(getApplicationContext());
		if ( xml == null ) {
			return false;
		}
		// Check if we are already running
		if ( m_statusThread != null ) {
			Toast.makeText(getApplicationContext(), 
				       "SIP Server is already running.  Request ignored.", 
				       Toast.LENGTH_LONG).show();	
			return false;
		}
		String	response = new String();

		if (( response = SipPP30Init ( nLogLevel, nSipMsgLogLevel, xml ) ).equals("OK") ) {
			m_statusThread = new SipPP30StatusThread ( getHandler(), this );
			m_statusThread.start();
			addLogMsg(getString(R.string.startSvr) + EOL, LogMsg.TYPE.DEBUG, LogMsg.CAT.POP);
			updatePP30StatusString();
			m_wakeLock.acquire();
			return true; 
		}

		addLogMsg("Failed to start sip server. " + response + EOL, LogMsg.TYPE.DEBUG, LogMsg.CAT.CRITICAL);
		return false;
	}   

	private void runPhoneSvr() {
		if ( ServerSetup.getInstance().isPhoneStatusEnabled() == true ) {
			
			// Check if we are already running
			if ( m_phoneStatusTimer != null ) {
				Toast.makeText(getApplicationContext(), 
					       "Mobile Event Tracker is already running.  Request ignored.", 
					       Toast.LENGTH_LONG).show();	
				return;
			}
			
			if ( ServerSetup.getInstance().isTimeStatusEnabled() == true ) {
				m_phoneStatusTimer = new Timer( );
				m_phoneStatusTimerTask = new PhoneStatusTimerTask( getHandler(), this );
				m_phoneStatusTimer.scheduleAtFixedRate( m_phoneStatusTimerTask, 1000, 
						ServerSetup.getInstance().getCollectionRate()*1000 );  // Convert Sec to MS
			}

			m_transferTimer = new Timer( );
			m_transferTimerTask = new TransferTimerTask( getHandler(), this );
			m_transferTimer.scheduleAtFixedRate( m_transferTimerTask, 1000, 
					ServerSetup.getInstance().getTransferRate()*1000 ); //Convert Sec to MS

		}

	}   


	// Stop the SIP server thread
	private boolean stopSvr()  {
		// Check is we are already running
		if ( m_statusThread == null && m_phoneStatusTimer == null ) {
			Toast.makeText(getApplicationContext(), 
				       "Server is already stopped.  Request ignored.", 
				       Toast.LENGTH_LONG).show();	
			return false;
		}
		SipPP30Uninit();
		m_statusThread = null; 
		if ( m_phoneStatusTimer != null ) {
			m_phoneStatusTimer.cancel();
			m_phoneStatusTimer = null;
		}
		if ( m_transferTimer != null ) {
			m_transferTimer.cancel();
			m_transferTimer = null;
		}
		m_handler = null;
		addLogMsg(getString(R.string.stopSvr) + EOL, LogMsg.TYPE.DEBUG, LogMsg.CAT.POP);
		addLogMsg(getString(R.string.stopCurrCall), LogMsg.TYPE.CALL, LogMsg.CAT.INFO);
		addLogMsg(getString(R.string.stopReg), LogMsg.TYPE.UA, LogMsg.CAT.INFO);
		m_processMessage.setActiveCall( false );
		clearWakeLock(); 
		m_processMessage.close();
		return true;
	}

	private void clearWakeLock() {
		if (m_wakeLock != null && m_wakeLock.isHeld() == true) {
			try {
				m_wakeLock.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	// Poll for status updates from SIP server and post them to status activity
	private void updatePP30StatusString ( ) {
		LinkedList<String> statusList = new LinkedList<String>();

		if ( m_statusThread != null &&
				m_statusThread.GetStatus( statusList ) > 0 )  {
			
			// SIP server can loose tcp connection and if it does we are dead
			// In this special case we need to restart the server.
			if ( m_processMessage.processStatusMessages( statusList ) == false ) {
				stopSvr();
				runSvr();
			}			
		} 
		if ( m_phoneStatusTimerTask != null &&
				m_phoneStatusTimerTask.GetStatus(statusList) > 0 ) {
			if ( m_bProcessInProgress == false ) {
				m_bProcessInProgress = true;
				m_processMessage.processStatusMessages( statusList );
			}
		}  
		if ( m_transferTimerTask != null && 
				m_transferTimerTask.GetStatus(statusList) > 0 ) {
			m_processMessage.processStatusMessages( statusList );
		} 
	}


	//	Runnable methods

	// @Override
	public void run ( )  {
		updatePP30StatusString ( );
	}

	//	Utilities

	/* A native method that is implemented by the
	 * 'SipPP30-jni' native library, which is packaged with this application.
	 */
	public native String  SipPP30Init   ( int logLevel, int sipMsgLogLevel, String sSipXml );

	public native void    SipPP30Uninit ( );
	

	/* this is used to load the 'SipPP30-jni' library on application
	 * startup. The library has already been unpacked into
	 * /data/data/com.xingang.androidpp30/lib/libSipPP30-jni.so at
	 * installation time by the package manager.
	 */
	static 
	{
		System.loadLibrary ( "SipPP30" );
	}

}
