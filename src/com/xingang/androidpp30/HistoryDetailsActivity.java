package com.xingang.androidpp30;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class HistoryDetailsActivity extends Activity implements OnClickListener  {
    public static String callInfo;
    public static String upLinkMetrics;
    public static String downLinkMetrics; 
    public static String latitude;
    public static String longitude;
    public static String passed;
    
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        setContentView(R.layout.history_details_layout);
      
        Button mapIt = (Button)findViewById(R.id.MapItBtn);
        mapIt.setOnClickListener(this);
        
        TextView call = (TextView)findViewById(R.id.CallInfoTxtView);       
        TextView up = (TextView)findViewById(R.id.UpLinkTxtView);       
        TextView down = (TextView)findViewById(R.id.DownLinkTxtView);
        down.setBackgroundColor(Color.DKGRAY);
                
        call.setText( callInfo );
        up.setText( upLinkMetrics );
        down.setText( downLinkMetrics );

    }
    
    public void onClick(View v) {  
    	switch(v.getId()){    
    	case R.id.MapItBtn: 
     		Intent intent = new Intent(getApplicationContext(),
                    MapItActivity.class);   		
    		intent.putExtra("Latitude", latitude);
    		intent.putExtra("Longitude", longitude);
    		intent.putExtra("CallInfo", callInfo);
    		intent.putExtra("Passed", passed);
    		startActivity(intent);
    		break;  
    	}  
    }     
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
