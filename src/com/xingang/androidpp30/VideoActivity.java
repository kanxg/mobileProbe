package com.xingang.androidpp30;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

public class VideoActivity extends Activity {
	   public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.video_layout); 
	        ((ImageView)findViewById(R.id.ImageView01)).setImageResource(R.drawable.video1);
	        ((ImageView)findViewById(R.id.ImageView02)).setImageResource(R.drawable.video2);
	   }
}
