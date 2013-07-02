package com.xingang.androidpp30;

import java.util.ArrayList;
import java.util.EnumMap;

import com.xingang.androidpp30.LogMsg.CAT;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.util.Log;

public class HistoryListActivity extends ListActivity implements TextWatcher {
    public static final String DEBUG_PREFS_NAME = "DebugPrefsFile";
    public static final String HISTORY_PREFS_NAME = "HistoryPrefsFile";
    private static final int   MAX_HISTORY_SIZE = 5000;
    
    private ArrayList<HistoryItem> m_historyList = null;
    private HistoryAdapter m_adapter;
    private TextView m_debug;
    private TextView m_call;
    private TextView m_ua;
    private TextView m_svr;
    private int m_currPos = 0; 
    private HistoryType m_historyType = HistoryType.LIST;
    

    
	
	final static String EOL = System.getProperty("line.separator"); 
	

   
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_list_layout);
        m_historyList = new ArrayList<HistoryItem>();
        m_adapter = new HistoryAdapter(this, R.layout.history_row_layout, m_historyList);
        setListAdapter(m_adapter);
 
        m_debug = (TextView)findViewById(R.id.DebugTxt);
        m_call = (TextView)findViewById(R.id.CurrentCallStatusTxt);
        m_ua = (TextView)findViewById(R.id.UAStatusTxt);
        
        // if SIP is not licensed then hide the Call and UA status
        if (getString(R.string.isSipLicensed).equals("yes") == false ) {
	        m_call.setVisibility(View.GONE);
	        m_ua.setVisibility(View.GONE);
        }
        m_svr = (TextView)findViewById(R.id.SvrStatusTxt);
        
        
        // Load Data
        setData();
       
        ColorDrawable divcolor = new ColorDrawable(Color.DKGRAY);
        getListView().setDivider(divcolor);
        getListView().setDividerHeight(2);
        
        registerForContextMenu(getListView());
        registerForContextMenu(m_debug);
        registerForContextMenu(m_svr);
        registerForContextMenu(findViewById(R.id.CallInfoTxtView));
           
        showHistory( ServerSetup.getInstance().getHistoryView() );
        
        m_debug.addTextChangedListener(this); 
        
        Log.d("PP30Lite", "OnCreate Complete");
    }
    
    public void onStop() {
    	saveData();
    	super.onStop();
    }
    
    public void onPause() {
    	saveData();
    	super.onPause();
    }
  
    // Save status to preference file
    private void saveData () {
    	SharedPreferences history = getSharedPreferences(HISTORY_PREFS_NAME, 0);
        SharedPreferences.Editor hisEditor = history.edit();
        hisEditor.clear();
        SharedPreferences.Editor debEditor = history.edit();
        
        debEditor.putString("debugTxt", m_debug.getText().toString());
        debEditor.putString("userAgent", m_ua.getText().toString());
        debEditor.putString("call", m_call.getText().toString());
        
        // Save History List Items
        hisEditor.putInt("historySize", m_historyList.size() );         
        for ( int i = 0; i < m_historyList.size(); i++ ) {
        	HistoryItem hi = m_historyList.get( i );
        	hi.saveData( hisEditor, i );
        }
        // Commit the edits!
        debEditor.commit();  
        hisEditor.commit();
    }
    
    // Retrieve status from preference file
    private void  setData() {
        SharedPreferences debug = getSharedPreferences(DEBUG_PREFS_NAME, 0);
        SharedPreferences history = getSharedPreferences(HISTORY_PREFS_NAME, 0);
        
        try {
        
	        m_call.setText(debug.getString("call", "Call Status"));
	        m_ua.setText(debug.getString("userAgent", "UA Status"));
	        m_debug.setText(debug.getString("debugTxt", getString(R.string.app_name_details))+EOL);
	        
	        // Read History List Items
	        int size = history.getInt("historySize", 0);
	        for( int i = 0; i < size; i++ ) {
	        	HistoryItem hi = new HistoryItem();
	        	hi.setData( history, i );
	        	m_historyList.add( hi );
	        
	        }
	        m_currPos = size;
	        m_adapter.notifyDataSetChanged();
	        updateHistoryDetailView(getHistoryItem(size-1));
        } catch (Exception e ) {
        	System.out.println( "Failed to Read History List Storage"+EOL);
        }
    }
       
    
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.history_context_menu, menu);
    }
    
    public boolean onContextItemSelected(MenuItem item) {
      if (item.getItemId() == R.id.showHistoryStatus ) {
    	showHistory( HistoryType.LIST );
        return true;
      }
      
      switch (item.getItemId()) {
      case R.id.showDebug:
    	showHistory( HistoryType.DEBUG );
        return true;
      case R.id.showHistory:
    	  showHistory( HistoryType.LIST );
        return true;
      case R.id.showHistoryDetail:
    	  showHistory(HistoryType.DETAIL );
    	  return true;
      default:
        return super.onContextItemSelected(item);
      }     
    }   
 
    private void showHistory( HistoryType type ) {
 	  ScrollView debug = (ScrollView)findViewById(R.id.ScrollDebug);
 	  ScrollView detail = (ScrollView)findViewById(R.id.ScrollHistoryDetails);
	  ListView history = (ListView)getListView();
  
	  m_historyType = type;
	  switch ( type ) {
	  case DEBUG:
			debug.setVisibility(View.VISIBLE);
	    	history.setVisibility(View.GONE);
	    	detail.setVisibility(View.GONE);
		  break;
	  case LIST:
	      	debug.setVisibility(View.GONE);
	      	history.setVisibility(View.VISIBLE);
	    	detail.setVisibility(View.GONE);
	    	m_adapter.notifyDataSetChanged();
		  break;
	  case DETAIL:
	      	debug.setVisibility(View.GONE);
	      	history.setVisibility(View.GONE);
	    	detail.setVisibility(View.VISIBLE);
		  break;
	  }
    }
    

        
    public int getNextPos() {
    	return m_currPos;
    }
    public void incrementNextPos() {
    	m_currPos++;
    }




	@Override 
	protected void onListItemClick(ListView l, View v, int position, long id) {
		HistoryItem hi = m_historyList.get(position);
		HistoryDetailsActivity.callInfo = hi.toString();
		HistoryDetailsActivity.upLinkMetrics = hi.getValue(HistoryEnum.UPLINK_METRICS);
		HistoryDetailsActivity.downLinkMetrics = hi.getValue(HistoryEnum.DOWNLINK_METRICS);
		HistoryDetailsActivity.latitude = hi.getValue(HistoryEnum.LATITUDE);
		HistoryDetailsActivity.longitude = hi.getValue(HistoryEnum.LONGITUDE);
		//HistoryDetailsActivity.passed = hi.getValue(HistoryEnum.PASSED);
		startActivity(new Intent(getApplicationContext(),
                HistoryDetailsActivity.class)); 

		super.onListItemClick(l, v, position, id);
	}

	public void setSvrState(boolean bIsRunning) {
		m_svr.setText( bIsRunning == true ? getString(R.string.svrRunning) : getString(R.string.svrStopped));
	}

	public void appendMsg( LogMsg msg ) {
		switch (msg.type){
		case DEBUG:
			m_debug.append(UtilityBelt.setSpanBetweenTokens("##" + msg.msg + EOL + "##", "##", new ForegroundColorSpan(getColor(msg.cat))) );
			trimDebug();
			break;
		case UA:
			m_ua.setText( msg.msg );
			break;
		case CALL:
			m_call.setText( msg.msg );
			break;
		}
	}
	
	private int getColor(CAT cat) {
		switch (cat) {
		case CRITICAL:  return Color.RED;
		case MAJOR:     return Color.MAGENTA;
		case WARN:      return Color.YELLOW;
		case INFO:      return Color.LTGRAY;
		case POP:       return Color.CYAN;
		case NONE:      return Color.WHITE;
		}
		return Color.WHITE;
	}

	private void trimDebug() {
		// We want to limit the size of status that we keep track of
		if ( m_debug.getLineCount() >  MAX_HISTORY_SIZE ) {
			StringBuffer newStatus = new StringBuffer(m_debug.getText());
			int index1 =  newStatus.indexOf("\n"); 
			
			// We will delete 10 lines at a time
			int index2 = index1+1;
			for ( int i = 0; i < 10; i++ ) {
				index2 =  newStatus.indexOf("\n", index2+1);
			}
			newStatus.replace(index1, index2, "");
			m_debug.setText(newStatus);	
		}
		m_adapter.trim();
	}
	
	public void clearCallStatus() {
		m_adapter.clear();
		m_adapter.notifyDataSetChanged();
		m_debug.setText( getString(R.string.app_name_details)+ EOL );
        m_call.setText(getString(R.string.stopCurrCall));
        m_ua.setText(getString(R.string.stopReg));
        updateHistoryDetailView(null);
		m_currPos = 0;
	}
	
	
	public int getHistorySize() {
		return m_historyList.size();
	}
	public HistoryItem getHistoryItem( int i ) {
		if ( i >= 0 && i < getHistorySize() ) {
			return m_historyList.get( i );
		}
		return null;
	}

	public void addHistoryItem(HistoryItem hi) {
		m_historyList.add(hi);
		trimDebug();
		m_adapter.notifyDataSetChanged();
        updateHistoryDetailView(hi);
	}
	
	private void updateHistoryDetailView(HistoryItem hi) {        
        ((TextView)findViewById(R.id.CallInfoTxtView)).setText(hi!=null ? hi.toSlaString(true) : "  --- Call Info ---\nNone Available at this time.");
        ((TextView)findViewById(R.id.DownLinkTxtView)).setText(hi!=null ? hi.getValue(HistoryEnum.DOWNLINK_METRICS) : "");
        ((TextView)findViewById(R.id.UpLinkTxtView)).setText(hi!=null ? hi.getValue(HistoryEnum.UPLINK_METRICS) : "");
	}
	
    // TextWatcher Interface
    // When text is changed we have to kick off a thread to force the scroll view 
    // to scroll to the last line.
    public  void afterTextChanged (Editable s) {
    	// TextWatcher Interface
    	//When text is changed we want to scroll to the bottom
        //final TextView textview = (TextView) findViewById(R.id.CallStatusTxt);
        final ScrollView scrollview = (ScrollView) findViewById(R.id.ScrollDebug);
 
        if ( scrollview == null ) {
        	return;
        }
        // scroll to last item
        scrollview.post(new Runnable() {
            @Override
            public void run() {
            	scrollview.fullScroll(View.FOCUS_DOWN);
            }
        });  
    }
    
    // TextWatcher Interface
    public void beforeTextChanged (CharSequence s, int start, int count, int after) {
    	// TextWatcher Interface
    }
    
    // TextWatcher Interface
    public  void onTextChanged (CharSequence s, int start, int before, int count) {
    	// TextWatcher Interface
    }
    
    private class HistoryAdapter extends ArrayAdapter<HistoryItem> {

        private ArrayList<HistoryItem> items;
        
        public void notifyDataSetChanged () {
        	if (m_historyType == HistoryType.LIST ) {
        		super.notifyDataSetChanged();
        	}
        }


        public HistoryAdapter(Context context, int textViewResourceId, ArrayList<HistoryItem> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	View v = convertView;
        	if (v == null) {
        		LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        		v = vi.inflate(R.layout.history_row_layout, null);
        	}
        	HistoryItem hi = items.get(position);
        	ServerSetup advSettings = ServerSetup.getInstance();
        	if (hi != null) {
        		TextView timeStamp = (TextView) v.findViewById(R.id.timestamp);
        		TextView signal = (TextView) v.findViewById(R.id.signal);
        		TextView item1 = (TextView) v.findViewById(R.id.item1);
        		TextView item2 = (TextView) v.findViewById(R.id.item2);
        		TextView item3 = (TextView) v.findViewById(R.id.item3);
        		ImageView iv = (ImageView) v.findViewById(R.id.signalIcon);
        		int textColor = getTextColor(advSettings, hi);
        		
         		if(timeStamp != null){
        			CharSequence str = UtilityBelt.setSpanBetweenTokens("##"+hi.getFormattedStartTime(hi.getValue(HistoryEnum.START_TIME)) + "##", "##", 
        					new ForegroundColorSpan( textColor ) );
        			timeStamp.setText( str );   
        		}
         		
         		

        		if (item1 != null) {
        			HistoryEnum he = advSettings.getHistoryListOp1();
        			String value =  hi.getValue(he) == null ? "N/A" : hi.getValue(he);
        			CharSequence str = UtilityBelt.setSpanBetweenTokens("##" + 
        			                                                    he.toString() + 
        			                                                    ": " + 
        			                                                    value + 
        			                                                    "##", "##", 
        			                                                    new ForegroundColorSpan( textColor ) );
        			item1.setText( str );  
        			
        		}
        		if (item2 != null) {
        			HistoryEnum he = advSettings.getHistoryListOp2();
        			String value =  hi.getValue(he) == null ? "N/A" : hi.getValue(he);
        			CharSequence str = UtilityBelt.setSpanBetweenTokens("##" + 
        			                                                    he.toString() + 
        			                                                    ": " + 
        			                                                    value + 
        			                                                    "##", "##", 
        			                                                    new ForegroundColorSpan( textColor ) );
        			item2.setText( str );  
        			
        		}
        		if (item3 != null) {
        			HistoryEnum he = advSettings.getHistoryListOp3();
        			String value =  hi.getValue(he) == null ? "N/A" : hi.getValue(he);
        			CharSequence str = UtilityBelt.setSpanBetweenTokens("##" + 
        			                                                    he.toString() + 
        			                                                    ": " + 
        			                                                    value + 
        			                                                    "##", "##", 
        			                                                    new ForegroundColorSpan( textColor ) );
        			item3.setText( str );  
        			
        		}
 
   

        		if(signal != null){
        			String value = "Unknown";
        			HistoryEnum he = HistoryEnum.GSM_SIGNAL_STRENGTH;
        			if ( hi.getValue(HistoryEnum.GSM_SIGNAL_STRENGTH) != null ) {
        				he = HistoryEnum.GSM_SIGNAL_STRENGTH;
        				value = hi.getValue(HistoryEnum.GSM_SIGNAL_STRENGTH);
        			} else if ( hi.getValue(HistoryEnum.CDMA_SIGNAL_STRENGTH) != null ) {
        				he = HistoryEnum.CDMA_SIGNAL_STRENGTH;
        				value = hi.getValue(HistoryEnum.CDMA_SIGNAL_STRENGTH);
        			} else if ( hi.getValue(HistoryEnum.LTE_SIGNAL_STRENGTH) != null ) {
        				he = HistoryEnum.LTE_SIGNAL_STRENGTH;
        				value = hi.getValue(HistoryEnum.LTE_SIGNAL_STRENGTH);
        			} 
        			CharSequence str = UtilityBelt.setSpanBetweenTokens("##" + 
        			                                                    he.toString() + 
        			                                                    ": " + 
        			                                                    value + 
        			                                                    "##", "##", 
        			                                                    new ForegroundColorSpan( textColor ) );
        			signal.setText( str );  
        		}        		

        		
        		
        		
        		
        		v.setBackgroundColor((position & 1) == 1 ? Color.WHITE : Color.LTGRAY); 
        		if( iv != null ) {
        			if ( hi.getValue(HistoryEnum.WIFI_ENABLED) == "Yes" ) {
        				iv.setImageResource(getWiFiSignalStrengthIconId(hi.getValue(HistoryEnum.WIFI_SIGNAL_STRENGTH)));
        			} else {
        				iv.setImageResource(getGsmSignalStrengthIconId(hi.getValue(HistoryEnum.GSM_SIGNAL_STRENGTH)));
        			}
        		}
        	}

        	return v;
        }

		private int getWiFiSignalStrengthIconId(String wifiSigStrength) {
			int sig = 0;
			try {
				sig = Integer.parseInt( wifiSigStrength );
			} catch (Exception e) {
				sig = 0;
			}
			if ( sig == 0 ) {
				return R.drawable.wifi0;
			} else if ( sig == 1) {
				return R.drawable.wifi1;
			} else if ( sig == 2 ) {
				return R.drawable.wifi2;
			} else if ( sig == 3 ) {
				return R.drawable.wifi3;
			} else if ( sig == 4 ) {
				return R.drawable.wifi4;
			} else if ( sig == 5 ) {
				return R.drawable.wifi5;
			} 
			return R.drawable.signal100;
		}
		
		private int getGsmSignalStrengthIconId(String gsmSigStrength) {
			int sig = 0;
			try {
				sig = Integer.parseInt( gsmSigStrength );
			} catch (Exception e) {
				sig = 0;
			}
			if ( sig == -113 ) {
				return R.drawable.signal0;
			} else if ( sig < -1105 ) {
				return R.drawable.signal20;
			} else if ( sig < -101 ) {
				return R.drawable.signal40;
			} else if ( sig <= -97 ) {
				return R.drawable.signal60;
			} else if ( sig <= -93 ) {
				return R.drawable.signal80;
			} 
			return R.drawable.signal100;
		}
		
		public void trim() {
			// We want to keep this list trimmed to MAX_HISTORY_SIZE rows
			while ( getCount() > MAX_HISTORY_SIZE ) {
				HistoryItem hi = getItem( 0 );
				remove( hi );
				m_currPos--;
			}
			m_adapter.notifyDataSetChanged();
		}
    }  // End Of HistoryAdapter    

	public int getTextColor(ServerSetup advSettings, HistoryItem hi) {
		for ( EnumMap.Entry<HistoryEnum, String> entry : hi.getHistoryMap().entrySet() ) {
			if ( advSettings.validateSla(entry.getKey(), entry.getValue()) == false ) {
				return Color.RED;
			}
		}
		return 0xFF006400;
	}
    
}  // End of HistoryListActivity