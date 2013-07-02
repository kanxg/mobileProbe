package com.xingang.androidpp30;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.TabActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

public class AndroidPP30 extends TabActivity {
	private PhoneService m_phoneService;
	private HistoryListActivity m_historyActivity = null;
	private MapItActivity m_mapActivity = null;
	private Handler m_Handler = new Handler();

	final static String EOL = System.getProperty("line.separator");

	// Called when the activity is first created.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Resources res = getResources(); // Resource object to get Drawables
		final TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		// History Tab
		intent = new Intent().setClass(this, HistoryListActivity.class);
		spec = tabHost
				.newTabSpec("history")
				.setIndicator("Home",
						res.getDrawable(R.drawable.ic_tab_status))
				.setContent(intent);
		tabHost.addTab(spec);

		// Map Tab
		intent = new Intent().setClass(this, MapItActivity.class);
		spec = tabHost.newTabSpec("mapit")
				.setIndicator("Map", res.getDrawable(R.drawable.ic_tab_map))
				.setContent(intent);
		tabHost.addTab(spec);

		// Advanced Settings Tab
		intent = new Intent().setClass(this, AdvanceActivity.class);
		spec = tabHost
				.newTabSpec("advance")
				.setIndicator("Advanced",
						res.getDrawable(R.drawable.ic_tab_adv))
				.setContent(intent);
		tabHost.addTab(spec);

		if (getString(R.string.isSipLicensed).equals("yes") == true) {
			// Setting Tab
			intent = new Intent().setClass(this, SettingsActivity.class);
			spec = tabHost
					.newTabSpec("sip")
					.setIndicator("SIP",
							res.getDrawable(R.drawable.ic_tab_phone))
					.setContent(intent);
			tabHost.addTab(spec);
		}
		// Set Active as current tab so that it can get a chance to read in its
		// data
		tabHost.setCurrentTab(2);

		// Set Map as current tab so that it is created and we can save
		// a reference to it
		tabHost.setCurrentTab(1);
		m_mapActivity = (MapItActivity) getLocalActivityManager().getActivity(
				"mapit");

		// Set History as current tab so that it is created and we can save
		// a reference to it
		tabHost.setCurrentTab(0);
		m_historyActivity = (HistoryListActivity) getLocalActivityManager()
				.getCurrentActivity();

		mapHistory();

		// Turn off the keypad when user swaps tabs
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(
						tabHost.getApplicationWindowToken(), 0);
			}
		});

	} // End onCreate

	protected void onResume() {
		super.onResume();
		if (isMyServiceRunning() == true) {
			m_historyActivity.setSvrState(true);
			m_Handler.postDelayed(m_BindServiceTimeTask, 100);
		}
	}

	protected void onPause() {
		super.onPause();
		m_Handler.removeCallbacks(m_UpdateTimeTask);
		try {
			unbindService(m_Connection);
		} catch (Exception e) {
			Log.d("PP30Lite",
					"Service was not running so unbindService Failed.");
		}
		m_phoneService = null;
		m_historyActivity.setSvrState(false);
	}

	private ServiceConnection m_Connection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			m_phoneService = ((PhoneService.PhoneServiceBinder) binder)
					.getService();
			Toast.makeText(
					AndroidPP30.this,
					"Connected to PP30 Lite(a) Service -- History list will update shortly.",
					Toast.LENGTH_SHORT).show();
		}

		public void onServiceDisconnected(ComponentName className) {
			m_phoneService = null;
			stopService();
		}
	};

	void doBindService() {
		bindService(new Intent(this, PhoneService.class), m_Connection, 0);
		// Context.BIND_AUTO_CREATE);
	}

	// @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	// @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.stop:
			stopService();
			return true;
		case R.id.run:
			runService();
			return true;
		case R.id.clearStatus:
			m_historyActivity.clearCallStatus();
			m_mapActivity.clearMap();
			return true;
		case R.id.speedTest:
			if (m_phoneService != null) {
				m_phoneService.processSpeedTest();
			} else {
				Toast.makeText(
						getApplicationContext(),
						"Must have Mobile Event Tracker enabled and be running",
						Toast.LENGTH_LONG).show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void processQuickTest(Handler hm) {
		if (m_phoneService != null) {
			m_phoneService.processQuickTest(hm);
		} else {
			Toast.makeText(getApplicationContext(),
					"Must have Mobile Event Tracker enabled and be running",
					Toast.LENGTH_LONG).show();
		}
	}

	private void runService() {
		if (UtilityBelt.isLicensed(getApplicationContext()) == true) {
			m_historyActivity.setSvrState(true);
			startService(new Intent(this, PhoneService.class));
			m_Handler.postDelayed(m_BindServiceTimeTask, 1000);
		}
	}

	private void stopService() {
		m_historyActivity.setSvrState(false);
		m_Handler.removeCallbacks(m_UpdateTimeTask);
		try {
			unbindService(m_Connection);
		} catch (Exception e) {
			Log.d("PP30Lite",
					"Service was not running so unbindService Failed.");
		}
		stopService(new Intent(this, PhoneService.class));
		m_phoneService = null;
	}

	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.xingang.androidpp30.PhoneService".equals(service.service
					.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private void mapHistory() {
		for (int i = 0; i < m_historyActivity.getHistorySize(); i++) {
			HistoryItem hi = m_historyActivity.getHistoryItem(i);
			if (hi != null && hi.getValue(HistoryEnum.LATITUDE) != null) {
				m_mapActivity.addMarker(hi);
			}
		}
	}

	private class GrabLogsFromService extends
			AsyncTask<Void, Void, ArrayList<LogMsg>> {
		protected ArrayList<LogMsg> doInBackground(Void... unused) {
			ArrayList<LogMsg> logs = new ArrayList<LogMsg>();
			m_phoneService.getLogMsgs(logs);
			return logs;
		}

		protected void onPostExecute(ArrayList<LogMsg> logs) {
			Iterator<LogMsg> logItr = logs.iterator();
			while (logItr.hasNext()) {
				m_historyActivity.appendMsg(logItr.next());
			}
		}
	}

	private class GrabHistoryFromService extends
			AsyncTask<Void, Void, ArrayList<HistoryItem>> {
		protected ArrayList<HistoryItem> doInBackground(Void... unused) {
			ArrayList<HistoryItem> hiList = new ArrayList<HistoryItem>();
			m_phoneService.getHistoryItems(hiList);
			Log.d("PP30Lite", " >>> AndroidPP30 Aquired History Items: "
					+ String.valueOf(hiList.size()));
			return hiList;
		}

		protected void onPostExecute(ArrayList<HistoryItem> hiList) {
			Iterator<HistoryItem> hiItr = hiList.iterator();
			while (hiItr.hasNext()) {
				HistoryItem hi = hiItr.next();
				m_historyActivity.addHistoryItem(hi);
				if (hi.getValue(HistoryEnum.LATITUDE) != null) {
					m_mapActivity.addMarker(hi);
				}
			}
		}
	}

	private Runnable m_UpdateTimeTask = new Runnable() {
		public void run() {
			if (m_phoneService != null) {

				new GrabLogsFromService().execute();

				// Grab all history items from service and update UI
				ArrayList<HistoryItem> hiList = new ArrayList<HistoryItem>();
				m_phoneService.getHistoryItems(hiList);
				Log.d("PP30Lite", " >>> AndroidPP30 Aquired History Items: "
						+ String.valueOf(hiList.size()));
				Iterator<HistoryItem> hiItr = hiList.iterator();
				while (hiItr.hasNext()) {
					HistoryItem hi = hiItr.next();
					m_historyActivity.addHistoryItem(hi);
					if (hi.getValue(HistoryEnum.LATITUDE) != null) {
						m_mapActivity.addMarker(hi);
					}
				}
			}
			m_Handler.postDelayed(this, 15000);

		}
	};

	private Runnable m_BindServiceTimeTask = new Runnable() {
		public void run() {
			doBindService();
			m_Handler.removeCallbacks(m_UpdateTimeTask);
			m_Handler.postDelayed(m_UpdateTimeTask, 5000);
		}
	};

}
