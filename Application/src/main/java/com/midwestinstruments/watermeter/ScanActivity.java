package com.midwestinstruments.watermeter;

import android.app.ListActivity;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
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

	private Runnable listUpdate = new Runnable() {
		@Override
		public void run() {
			activityData.update(10000L);
			handler.postDelayed(listUpdate, 1000);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		handler = new Handler();
		activityData = new ScanListAdapter(this);
		scanner = new BTScanner(this);
		scanner.setCallback(new BTScanner.ScannerCallback() {
			@Override
			public void onScan(ScanResult result) {
				ScanData data = new ScanData();
				data.set(result);
				activityData.add(data);
			}
		});
		setListAdapter(activityData);

		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
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

		Intent intent = new Intent(this, MeterActivity.class);
		startActivity(intent);

	}
}
