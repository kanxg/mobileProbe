package com.xingang.androidpp30;

import android.app.Activity;
import android.app.TabActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class SettingsActivity extends Activity implements OnClickListener,  OnFocusChangeListener  {
    public static final String PREFS_NAME = "SettingsPrefsFile";
    private static final String TOS_STR = "01100000";
    private Handler hm;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        hm = new Handler() {
            public void handleMessage(Message m) {
                // toast code
            	Toast.makeText(getApplicationContext(), 
        			       m.obj.toString(),
        			       Toast.LENGTH_LONG).show();
//        		Button b=(Button)findViewById(R.id.startQuickTest);
//        		b.setEnabled(true);
            }
        };
        
        setContentView(R.layout.phone_layout);  
        ((Button)findViewById(R.id.AddAdditionalProxy)).setOnClickListener(this);
        ((Button)findViewById(R.id.RemoveLastProxy)).setOnClickListener(this);     
        ((EditText)findViewById(R.id.ProxyPortEdit)).setOnFocusChangeListener(this);    
        ((EditText)findViewById(R.id.ProxyPortEdit1)).setOnFocusChangeListener(this);    
        ((EditText)findViewById(R.id.ProxyPortEdit2)).setOnFocusChangeListener(this);    
        ((EditText)findViewById(R.id.ProxyPortEdit)).setOnFocusChangeListener(this);    
        ((EditText)findViewById(R.id.ProxyPortEdit)).setOnFocusChangeListener(this);
        
        ((EditText)findViewById(R.id.TOSEdit)).setOnFocusChangeListener(this);  
        ((EditText)findViewById(R.id.OutboundPortEdit)).setOnFocusChangeListener(this);  
        ((EditText)findViewById(R.id.RegIntEdit)).setOnFocusChangeListener(this);  
        ((EditText)findViewById(R.id.MediaMinPortEdit)).setOnFocusChangeListener(this);  
        ((EditText)findViewById(R.id.MediaMaxPortEdit)).setOnFocusChangeListener(this);  
        ((EditText)findViewById(R.id.RegistrarPortEdit)).setOnFocusChangeListener(this);   

        setData();

    }
    
    
    private void setData () {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        //Quick Test Phone Number
        ((EditText)findViewById(R.id.PhoneNumEdit)).setText(settings.getString("phoneNumberEdit", ""));
        
        // SIP UA Settings
        ((EditText)findViewById(R.id.UserNameEdit)).setText(settings.getString("userNameEdit", ""));
        ((EditText)findViewById(R.id.AuthPasswordEdit)).setText(settings.getString("authPasswordEdit", ""));

    
        // Proxy 0 Settings
        ((EditText)findViewById(R.id.ProxyIPEdit)).setText(settings.getString("proxyIPEdit", ""));
        ((EditText)findViewById(R.id.ProxyPortEdit)).setText(settings.getString("proxyPortEdit", "5060"));

        
        // Proxy 1 Settings
        ((EditText)findViewById(R.id.ProxyIPEdit1)).setText(settings.getString("proxyIPEdit1", ""));
        ((EditText)findViewById(R.id.ProxyPortEdit1)).setText(settings.getString("proxyPortEdit1", "5060"));
        if ( ((EditText)findViewById(R.id.ProxyIPEdit1)).getText().toString().length() !=0 ) {
        	addProxy();
        }
        // Proxy 2 Settings
        ((EditText)findViewById(R.id.ProxyIPEdit2)).setText(settings.getString("proxyIPEdit2", ""));
        ((EditText)findViewById(R.id.ProxyPortEdit2)).setText(settings.getString("proxyPortEdit2", "5060"));
        if ( ((EditText)findViewById(R.id.ProxyIPEdit2)).getText().toString().length() != 0 ) {
        	addProxy();
        } 
 

        // General SIP
        ((CheckBox)findViewById(R.id.EnableSIPAgentChkBox)).setChecked(settings.getBoolean("sipEnabled", false));
        ((EditText)findViewById(R.id.MediaMinPortEdit)).setText(settings.getString("minMediaPort", "5000"));
        ((EditText)findViewById(R.id.MediaMaxPortEdit)).setText(settings.getString("maxMediaPort", "13000"));
        ((EditText)findViewById(R.id.OutboundPortEdit)).setText(settings.getString("outboundPort", "5060"));
        ((EditText)findViewById(R.id.TOSEdit)).setText(settings.getString("tos", "01100000"));
        ((EditText)findViewById(R.id.RegIntEdit)).setText(settings.getString("regInt", "3600"));
        ((Spinner)findViewById(R.id.ReferenceFileSpin)).setSelection(settings.getInt("referenceFile", 0));
        ((CheckBox)findViewById(R.id.AlwaysIncludePortChkBox)).setChecked(settings.getBoolean("alwaysIncludePort", false));
        ((CheckBox)findViewById(R.id.IcludeRPortChkBox)).setChecked(settings.getBoolean("includeRPort", true));

        //Registrar Settings
        ((EditText)findViewById(R.id.RegistrarIPEdit)).setText(settings.getString("registrarIPEdit", ""));
        ((EditText)findViewById(R.id.RegistrarPortEdit)).setText(settings.getString("registrarPortEdit", "5060"));
        ((EditText)findViewById(R.id.DisplayNameEdit)).setText(settings.getString("displayNameEdit", ""));
        ((EditText)findViewById(R.id.DomainEdit)).setText(settings.getString("domainEdit", ""));
        ((EditText)findViewById(R.id.RealmEdit)).setText(settings.getString("realmEdit", ""));
        ((EditText)findViewById(R.id.AuthUserNameEdit)).setText(settings.getString("authUserNameEdit", ""));
        ((Spinner)findViewById(R.id.RecordRoutingSpin)).setSelection(settings.getInt("recordRoutingSpin", 0));

        //codec
        Spinner codecSpin=(Spinner)findViewById(R.id.CodecSpin);
        ArrayAdapter<String> codecSpinAdap=(ArrayAdapter)codecSpin.getAdapter();
        int codecSpinnerPosition= codecSpinAdap.getPosition(settings.getString("codecEdit","AMR-WB"));
        codecSpin.setSelection(codecSpinnerPosition);
        
        ((EditText)findViewById(R.id.AmrPayloadEdit)).setText(settings.getString("amrPayloadEdit", "102"));
        ((EditText)findViewById(R.id.AmrWbPayloadEdit)).setText(settings.getString("amrWbPayloadEdit", "104"));
        

       
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
        
        //Quick Test Phone Number
        editor.putString("phoneNumberEdit", ((EditText)findViewById(R.id.PhoneNumEdit)).getText().toString());

        // SIP User Agent Settings
        editor.putString("userNameEdit", ((EditText)findViewById(R.id.UserNameEdit)).getText().toString());
        editor.putString("authPasswordEdit", ((EditText)findViewById(R.id.AuthPasswordEdit)).getText().toString());
        
        
        // Proxy 0 Settings
        editor.putString("proxyIPEdit", ((EditText)findViewById(R.id.ProxyIPEdit)).getText().toString());
        editor.putString("proxyPortEdit", ((EditText)findViewById(R.id.ProxyPortEdit)).getText().toString());
        
        // Proxy 1 Settings
        editor.putString("proxyIPEdit1", ((EditText)findViewById(R.id.ProxyIPEdit1)).getText().toString());
        editor.putString("proxyPortEdit1", ((EditText)findViewById(R.id.ProxyPortEdit1)).getText().toString());
        
        // Proxy 2 Settings
        editor.putString("proxyIPEdit2", ((EditText)findViewById(R.id.ProxyIPEdit2)).getText().toString());
        editor.putString("proxyPortEdit2", ((EditText)findViewById(R.id.ProxyPortEdit2)).getText().toString());

        
        // General SIP
        editor.putBoolean("sipEnabled", ((CheckBox)findViewById(R.id.EnableSIPAgentChkBox)).isChecked());
        editor.putString("minMediaPort", ((EditText)findViewById(R.id.MediaMinPortEdit)).getText().toString());
        editor.putString("maxMediaPort", ((EditText)findViewById(R.id.MediaMaxPortEdit)).getText().toString());
        editor.putString("outboundPort", ((EditText)findViewById(R.id.OutboundPortEdit)).getText().toString());
        editor.putString("tos", ((EditText)findViewById(R.id.TOSEdit)).getText().toString());
        editor.putString("regInt", ((EditText)findViewById(R.id.RegIntEdit)).getText().toString());
        editor.putInt("referenceFile", ((Spinner)findViewById(R.id.ReferenceFileSpin)).getSelectedItemPosition());
        editor.putBoolean("alwaysIncludePort", ((CheckBox)findViewById(R.id.AlwaysIncludePortChkBox)).isChecked());
        editor.putBoolean("includeRPort", ((CheckBox)findViewById(R.id.IcludeRPortChkBox)).isChecked());
 
        
        // Registrar Settings
        editor.putString("registrarIPEdit", ((EditText)findViewById(R.id.RegistrarIPEdit)).getText().toString());
        editor.putString("registrarPortEdit", ((EditText)findViewById(R.id.RegistrarPortEdit)).getText().toString());
        editor.putString("displayNameEdit", ((EditText)findViewById(R.id.DisplayNameEdit)).getText().toString());
        editor.putInt("recordRoutingSpin", ((Spinner)findViewById(R.id.RecordRoutingSpin)).getSelectedItemPosition());
        editor.putString("domainEdit", ((EditText)findViewById(R.id.DomainEdit)).getText().toString());
        editor.putString("realmEdit", ((EditText)findViewById(R.id.RealmEdit)).getText().toString());
        editor.putString("authUserNameEdit", ((EditText)findViewById(R.id.AuthUserNameEdit)).getText().toString());
        
        
        editor.putString("codecEdit", ((Spinner)findViewById(R.id.CodecSpin)).getSelectedItem().toString());
        editor.putString("amrPayloadEdit", ((EditText)findViewById(R.id.AmrPayloadEdit)).getText().toString());
        editor.putString("amrWbPayloadEdit", ((EditText)findViewById(R.id.AmrWbPayloadEdit)).getText().toString());
        
        // Commit the edits!
        editor.commit();   	
    }
    
	@Override
	public void onFocusChange(View arg0, boolean arg1) {
		int min = 1;
		int max = 1;
		
		switch ( arg0.getId() ) {
		case R.id.ProxyPortEdit: // Range 1000-90000
			validateEditTextRange(
					(EditText)findViewById(R.id.ProxyPortEdit),
					1000,
					90000,
					R.string.proxyPortInvalid,
					"5060" );
			return; 
		case R.id.ProxyPortEdit1: // Range 1000-90000
			validateEditTextRange(
					(EditText)findViewById(R.id.ProxyPortEdit1),
					1000,
					90000,
					R.string.proxyPortInvalid,
					"5060" );
			return; 
		case R.id.ProxyPortEdit2: // Range 1000-90000
			validateEditTextRange(
					(EditText)findViewById(R.id.ProxyPortEdit2),
					1000,
					90000,
					R.string.proxyPortInvalid,
					"5060" );
			return;
		case R.id.TOSEdit:
	        EditText tosEdit = (EditText) findViewById(R.id.TOSEdit);
			String tos = tosEdit.getText().toString();
			int len = tos.length();
			// TOS is an 8 bit binary number
			if ( len != 8 ) {
				Toast.makeText(getApplicationContext(), 
						       R.string.tosInvalidLen, 
						       Toast.LENGTH_LONG).show();
				tosEdit.setText(TOS_STR);
				return;
			}
			for ( int i = 0; i < len; i++ ) {
				if ( tos.charAt(i) != '0' && tos.charAt(i) != '1') {
					Toast.makeText(getApplicationContext(), 
							       R.string.tosInvalidLen, 
							       Toast.LENGTH_LONG).show();
					tosEdit.setText(TOS_STR);
					return;
					
				}
			}			
			return;
		case R.id.OutboundPortEdit: // Range 1000-90000
			validateEditTextRange(
					(EditText)findViewById(R.id.OutboundPortEdit),
					1000,
					90000,
					R.string.outboundPortInvalid,
					"5060" );
			return; 
		case R.id.RegIntEdit: 
			validateEditTextRange(
					(EditText)findViewById(R.id.RegIntEdit),
					1000,
					90000,
					R.string.registrationIntervalInvalid,
					"3600" );			
			return; 
		case R.id.MediaMinPortEdit: 
			if ( validateEditTextRange(
					(EditText)findViewById(R.id.MediaMinPortEdit),
					1000,
					90000,
					R.string.mediaPortInvalid,
					"12000" ) == true ) {
				((EditText)findViewById(R.id.MediaMaxPortEdit)).setText("20000");
			}
			// Validate MIN < MAX
			min = Integer.parseInt(((EditText)findViewById(R.id.MediaMinPortEdit)).getText().toString());
			max = Integer.parseInt(((EditText)findViewById(R.id.MediaMaxPortEdit)).getText().toString());
			if ( min >= max ) {
				((EditText)findViewById(R.id.MediaMaxPortEdit)).setText(Integer.toString(min+1000));
			}
			return;	
		case R.id.MediaMaxPortEdit: // Range 1000-90000 and > MediaMinPortEdit
			if ( validateEditTextRange(
					(EditText)findViewById(R.id.MediaMaxPortEdit),
					1000,
					90000,
					R.string.mediaPortInvalid,
					"20000" ) == true ) {
				((EditText)findViewById(R.id.MediaMinPortEdit)).setText("12000");
			}	
			// Validate MIN < MAX
			min = Integer.parseInt(((EditText)findViewById(R.id.MediaMinPortEdit)).getText().toString());
			max = Integer.parseInt(((EditText)findViewById(R.id.MediaMaxPortEdit)).getText().toString());
			if ( min >= max ) {
				((EditText)findViewById(R.id.MediaMinPortEdit)).setText(Integer.toString(max-1000));
			}
			return;
		case R.id.RegistrarPortEdit:	
			validateEditTextRange(
					(EditText)findViewById(R.id.RegistrarPortEdit),
					1000,
					90000,
					R.string.registrationPortInvalid,
					"5060" );			
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
			return false;
		}
		return true;
	}
    
    // Implement the OnClickListener callback
    public void onClick(View v) {
      switch ( v.getId() ) {
      case R.id.AddAdditionalProxy:
          addProxy();
          return;
      case R.id.RemoveLastProxy:
          removeProxy();
          return;
      default:
          return;      
      }
    }
    
    private void addProxy() {
    	if( ((TextView)findViewById(R.id.ProxiesTxt1)).getVisibility() == View.GONE ) {
    		((TextView)findViewById(R.id.ProxiesTxt1)).setVisibility( View.VISIBLE );
    		((TextView)findViewById(R.id.ProxyIPTxt1)).setVisibility( View.VISIBLE );
    		((TextView)findViewById(R.id.ProxyIPEdit1)).setVisibility( View.VISIBLE );
    		((TextView)findViewById(R.id.ProxyPortTxt1)).setVisibility( View.VISIBLE );
    		((TextView)findViewById(R.id.ProxyPortEdit1)).setVisibility( View.VISIBLE );
    	} else if ( findViewById(R.id.ProxiesTxt2).getVisibility() == View.GONE ) {
    		((TextView)findViewById(R.id.ProxiesTxt2)).setVisibility( View.VISIBLE );
    		((TextView)findViewById(R.id.ProxyIPTxt2)).setVisibility( View.VISIBLE );
    		((TextView)findViewById(R.id.ProxyIPEdit2)).setVisibility( View.VISIBLE );
    		((TextView)findViewById(R.id.ProxyPortTxt2)).setVisibility( View.VISIBLE );
    		((TextView)findViewById(R.id.ProxyPortEdit2)).setVisibility( View.VISIBLE );	
    	}
    }
    
    private void removeProxy() {
    	if( ((TextView)findViewById(R.id.ProxiesTxt2)).getVisibility() == View.VISIBLE ) {
    		((TextView)findViewById(R.id.ProxiesTxt2)).setVisibility( View.GONE );
    		((TextView)findViewById(R.id.ProxyIPTxt2)).setVisibility( View.GONE );
    		((TextView)findViewById(R.id.ProxyIPEdit2)).setVisibility( View.GONE );
    		((TextView)findViewById(R.id.ProxyIPEdit2)).setText( "" );
    		((TextView)findViewById(R.id.ProxyPortTxt2)).setVisibility( View.GONE );
    		((TextView)findViewById(R.id.ProxyPortEdit2)).setVisibility( View.GONE );
    	} else if ( findViewById(R.id.ProxiesTxt1).getVisibility() == View.VISIBLE ) {
    		((TextView)findViewById(R.id.ProxiesTxt1)).setVisibility( View.GONE );
    		((TextView)findViewById(R.id.ProxyIPTxt1)).setVisibility( View.GONE );
    		((TextView)findViewById(R.id.ProxyIPEdit1)).setText( "" );
    		((TextView)findViewById(R.id.ProxyIPEdit1)).setVisibility( View.GONE );
    		((TextView)findViewById(R.id.ProxyPortTxt1)).setVisibility( View.GONE );
    		((TextView)findViewById(R.id.ProxyPortEdit1)).setVisibility( View.GONE );	
    	}
    }
   
    /** Called when the user click the button */
    public void startQuickTest(View view) {
        // Do something in response to button click
        	String phoneName=((EditText)findViewById(R.id.PhoneNumEdit)).getText().toString();
        	if("".equals(phoneName)){
        		Toast.makeText(getApplicationContext(), 
       			       "phone Number can't be null! ", 
       			       Toast.LENGTH_LONG).show();
        	}else{
            		saveData();
//            		Button b=(Button)findViewById(R.id.startQuickTest);
//            		b.setEnabled(false);
            		AndroidPP30 pp30=(AndroidPP30)getParent();
                	pp30.processQuickTest(hm);	
        	}
    }
    
    /** Called when the user click the button */
    public void stopQuickTest(View view) {
        // Do something in response to button click
        	Toast.makeText(getApplicationContext(), 
       			       "Not implemented now.", 
       			       Toast.LENGTH_SHORT).show();
//    		Button b=(Button)findViewById(R.id.startQuickTest);
//    		b.setEnabled(true);
    }
}

