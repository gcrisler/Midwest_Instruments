package com.midwestinstruments.watermeter;

import android.app.ListActivity;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;

/**
 * Main activity for the device. The first screen that pops up. Starts scanning and
 * continues scanning until something happens.
 *
 * Created by byronh on 5/9/16.
 */
public class ScanActivity extends ListActivity {

	private BTScanner scanner;
	private ScanListAdapter activityData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
		super.onResume();
	}


	@Override
	protected void onPause() {
		scanner.stopScan();
		super.onPause();
	}

}
