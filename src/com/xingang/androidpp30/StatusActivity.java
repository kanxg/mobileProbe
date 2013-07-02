package com.xingang.androidpp30;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ScrollView;
import android.widget.TextView; 
import android.text.style.ForegroundColorSpan;


public class StatusActivity extends Activity implements TextWatcher {
    public static final String PREFS_NAME = "StatusPrefsFile";
    private TextView m_callStatus;
    private TextView m_uaStatus;
    private TextView m_currentCallStatus;
    private TextView m_historyStatus;

    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_layout);
        m_uaStatus = (TextView)findViewById(R.id.UAStatusTxt);
        m_callStatus = (TextView)findViewById(R.id.CallStatusTxt);
        m_currentCallStatus = (TextView)findViewById(R.id.CurrentCallStatusTxt);
        m_historyStatus = (TextView)findViewById(R.id.HistoryTxt);
        setData(); 
        ((TextView)findViewById(R.id.CallStatusTxt)).addTextChangedListener(this); 
 
        registerForContextMenu(m_callStatus);
        registerForContextMenu(m_historyStatus);
        showHistory( true );
                
       
    }
    
    private void showHistory( boolean bShowHistory ) {
 	  ScrollView debug = (ScrollView)findViewById(R.id.ScrollDebug);
	  ScrollView history = (ScrollView)findViewById(R.id.ScrollHistory);
  
	  if ( bShowHistory ) {
      	debug.setVisibility(View.GONE);
      	history.setVisibility(View.VISIBLE);       
		  
	  } else {
		debug.setVisibility(View.VISIBLE);
    	history.setVisibility(View.GONE);		  
	  }
    }
    
  
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.status_context_menu, menu);
    }
    
    public boolean onContextItemSelected(MenuItem item) {  
      switch (item.getItemId()) {
      case R.id.showDebugStatus:
    	showHistory( false );
        return true;
      case R.id.showHistoryStatus:
    	  showHistory( true );
        return true;
      default:
        return super.onContextItemSelected(item);
      }
    }   
    
    
    public void onStop() {
    	saveData();
    	super.onStop();
    }
  
    // Save status to preference file
    private void saveData () {
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        
        editor.putString("uaStatus", m_uaStatus.getText().toString());
        editor.putString("callStatus", m_callStatus.getText().toString());
 
        // Commit the edits!
        editor.commit();   	
    }
    
    // Retrieve status from preference file
    private void setData () {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        m_uaStatus.setText(settings.getString("uaStatus", "UA Status"));
        m_callStatus.setText(settings.getString("callStatus", getString(R.string.app_name_details))+"\n");

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
                //int y = textview.getLayout().getLineCount(); // e.g. I want to scroll to the end
                //scrollview.scrollTo(0, y);
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
  
 
    //  Clears the call status text view and sets the text back to 
    //  the default splash text
    public void clearCallStatus() {
    	m_callStatus.setText(getString(R.string.app_name_details) + "\n");
    }

    // Append a string to the call status test view
	public void appendCallStatus(CharSequence status, int color ) {
		

		if ( color == 0 ) {
			m_callStatus.append( status );
		}
		else {
			m_callStatus.append( UtilityBelt.setSpanBetweenTokens( "##" + status + "##", "##", new ForegroundColorSpan(color) ) );
		}
		
		// We want to limit the size of status that we keep track of
		if ( m_callStatus.getLineCount() > 500 ) {
			StringBuffer newStatus = new StringBuffer(m_callStatus.getText());
			int index1 =  newStatus.indexOf("\n"); 
			
			// We will delete 10 lines at a time
			int index2 = index1+1;
			for ( int i = 0; i < 10; i++ ) {
				index2 =  newStatus.indexOf("\n", index2+1);
			}
			newStatus.replace(index1, index2, "");
			m_callStatus.setText(newStatus);
			
			
		}
	}


	public void appendRegistrationStatus(String statusTxt, Date timestamp) {   
		m_uaStatus.setText( formateDate(timestamp) + ": " + statusTxt );
	}
	
	private String formateDate ( Date timestamp ) {
	    SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.getDefault());	
		return  sdf.format(timestamp); 
	}


	public void appendCurrentCallStatus(String statusTxt, Date timestamp) {
		m_currentCallStatus.setText( formateDate(timestamp) + ": " + statusTxt );
		
	}
	
}

