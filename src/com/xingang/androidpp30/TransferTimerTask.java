package com.xingang.androidpp30;

import java.util.Calendar;
import java.util.LinkedList;
import android.os.Handler;
import java.util.TimerTask;

public class TransferTimerTask extends TimerTask {
	Handler				m_statusHandler = null;
	Runnable			m_runnable		= null;
	LinkedList<String>	m_statusQueue	= null;
	static String		TRANSFER_STATUS	="TS:SC:";
	
	public TransferTimerTask ( Handler aHandler, Runnable aRunnable  ) {

		m_statusQueue 	= new LinkedList<String> ( );
		m_statusHandler = aHandler;
		m_runnable 		= aRunnable;
	}
	
	public void run() {
		long time = Calendar.getInstance().getTimeInMillis();
		m_statusQueue.add(TRANSFER_STATUS + 
				          time/1000 +
				          ":Transfer Results");
		m_statusHandler.post ( m_runnable );
	}
	
	public 
	int	GetStatus ( LinkedList<String> returnedStatus ) {
		int	count = 0;

		synchronized ( m_statusQueue )
			{
			while ( !m_statusQueue.isEmpty() )
				{
				returnedStatus.addLast ( m_statusQueue.removeFirst() );
				count++;
				}
			}
		
		return count;
		}

}
