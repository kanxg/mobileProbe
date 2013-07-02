package com.xingang.androidpp30;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.graphics.drawable.Drawable;
import android.os.Bundle;


public class MapItActivity extends MapActivity  {
	private MapItItemizedOverlay m_itemizedoverlayGreen = null;
	private MapItItemizedOverlay m_itemizedoverlayRed = null;
	private MapView m_mapView;
	private boolean m_bIsFirst=true;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_layout);
		

		
		m_mapView = (MapView) findViewById(R.id.mapview);
		m_mapView.setBuiltInZoomControls(true);
		// redraw map / overlay
		m_mapView.invalidate();	
		
		Bundle extras = getIntent().getExtras();
		if (extras != null ) {
			String lat = extras.getString("Latitude");
			String lng = extras.getString("Longitude");
			String info = extras.getString("CallInfo");
			String pass = extras.getString("Passed");
			if ( lat != null && lng != null ) {
				addMarker(lat, lng, info, pass );
			}
		}
		

	}

	private void addMarker(String latitude, String longitude, String info, String pass) {
		setupOverlays();
		
		int latitudeInt = (int)(Double.parseDouble(latitude) * 1E6);
		int longitudeInt = (int)(Double.parseDouble(longitude) * 1E6);
		GeoPoint point = new GeoPoint( latitudeInt, longitudeInt );
		String tag = info;
		OverlayItem overlayitem = new OverlayItem(point, "Mapped Call", tag);
		if( pass == "Yes" ) {
			m_itemizedoverlayGreen.addOverlay(overlayitem);
		} else {
			m_itemizedoverlayRed.addOverlay(overlayitem);
		}		
		m_mapView.invalidate();	
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void addMarker(HistoryItem hi ) {
		setupOverlays();
		
		int latitudeInt = (int)(Double.parseDouble(hi.getValue(HistoryEnum.LATITUDE)) * 1E6);
		int longitudeInt = (int)(Double.parseDouble(hi.getValue(HistoryEnum.LONGITUDE)) * 1E6);

		GeoPoint point = new GeoPoint( latitudeInt, longitudeInt );
		String tag = hi.toString();
		OverlayItem overlayitem = new OverlayItem(point, "Mapped Call", tag);
//		if( hi.getValue(HistoryEnum.PASSED) == "Yes" ) {
			m_itemizedoverlayGreen.addOverlay(overlayitem);
//		} else {
//			m_itemizedoverlayRed.addOverlay(overlayitem);
//		}
		m_mapView.invalidate();
	}

	private void setupOverlays() {
		if ( m_bIsFirst ) {
			List<Overlay> mapOverlays = m_mapView.getOverlays();
			Drawable drawable = this.getResources().getDrawable(R.drawable.pin);
			m_itemizedoverlayGreen = new MapItItemizedOverlay(drawable, this);
			mapOverlays.add(m_itemizedoverlayGreen);
			drawable = this.getResources().getDrawable(R.drawable.pin_red);
			m_itemizedoverlayRed = new MapItItemizedOverlay(drawable, this);
			mapOverlays.add(m_itemizedoverlayRed);
			m_bIsFirst = false;
		}
	}

	public void clearMap() {
		if ( m_itemizedoverlayGreen != null  ) {
			m_itemizedoverlayGreen.clearAll();
		}
		if ( m_itemizedoverlayRed != null  ) {
			m_itemizedoverlayRed.clearAll();
		}
		m_mapView.invalidate();
	}
}
