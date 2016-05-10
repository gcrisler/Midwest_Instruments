package com.midwestinstruments.watermeter;

import android.app.ListActivity;
import android.os.Bundle;

/**
 * Main activity for the device. The first screen that pops up. Starts scanning and
 * continues scanning until something happens.
 *
 * Created by byronh on 5/9/16.
 */
public class ScanActivity extends ListActivity {

	private BTScanner scanner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		scanner = new BTScanner(this);

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
