package com.midwestinstruments.watermeter;

import android.app.ListActivity;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;

/**
 * Main activity for the device. The first screen that pops up. Starts scanning and
 * continues scanning until something happens.
 *
 * Created by byronh on 5/9/16.
 */
public class ScanActivity extends ListActivity {

	private BTScanner scanner;
	private ScanListAdapter activityData;

	private Handler handler;

	private int maxRssi;

	private Runnable listUpdate = new Runnable() {
		@Override
		public void run() {
			activityData.update(15000L);
			handler.postDelayed(listUpdate, 500);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		handler = new Handler();
		SharedPreferences prefs = getPreferences(0);
		maxRssi = prefs.getInt("MAX_RSSI", -64);
		activityData = new ScanListAdapter(this);
		activityData.setMaxRssi(maxRssi);
		scanner = new BTScanner(this);
		scanner.setCallback(new BTScanner.ScannerCallback() {
			@Override
			public void onScan(ScanResult result) {
				ScanData data = new ScanData();
				if(result.getRssi() > maxRssi) {
					maxRssi = result.getRssi();
					SharedPreferences.Editor edit = prefs.edit();
					edit.putInt("MAX_RSSI", maxRssi);
					edit.commit();
					activityData.setMaxRssi(maxRssi);

				}
				data.set(result);
				activityData.add(data);
			}
		});
		setListAdapter(activityData);
		setContentView(R.layout.activity_scan);

		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		// expire everything
		activityData.update(0);

		scanner.startScan();
		handler.post(listUpdate);
		super.onResume();
	}


	@Override
	protected void onPause() {
		scanner.stopScan();
		handler.removeCallbacks(listUpdate);
		super.onPause();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		MeterActivity.device = ((ScanData) activityData.getItem(position)).getDevice();

		startActivity(new Intent(this, MeterActivity.class));

	}
}
