package com.midwestinstruments.watermeter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.Collections;

/**
 * Created by byronh on 5/9/16.
 */
public class BTScanner {

	/**
	 * Tag for debug logging
	 */
	private static final String SCANNER = "BTScanner";

	/**
	 * Keep the activity that handles the scanner
	 */
	private Activity parentActivity;

	/**
	 * If the scanner was stopped. We need this because we have to continuously
	 * restart the scan to get new data.
	 */
	private boolean stopped = false;

	/**
	 * The interface to all bluetooth operations
	 */
	private BluetoothAdapter bluetoothFacade;

	/**
	 * Filter for unwanted devices from scan
	 */
	private final ScanFilter filter = new ScanFilter.Builder()
			.setDeviceName(MWDevice.NAME).build();

	/**
	 * Settings related to the scan
	 */
	private final ScanSettings scanSettings = new ScanSettings.Builder()
			//.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
			//.setMatchMode(ScanSettings.MATCH_MODE_STICKY)
			//.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT) // needs 23.0
			.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

	/**
	 * Handler for timer operations. We have to restart the bt scan after getting the results.
	 */
	private final Handler handler;

	/**
	 * Create a new Scanner Object
	 * @param parentActivity the parent activity
	 */
	public BTScanner(Activity parentActivity) {
		this.parentActivity = parentActivity;
		handler = new Handler();

		// populate the bluetooth adapter when the scanner is first created. Doesn't actually start scanning
		BluetoothManager btMan = (BluetoothManager)parentActivity.getSystemService(Context.BLUETOOTH_SERVICE);
		Error.check(btMan == null, parentActivity, "Could not get BT Manager", Error.BTLE_NOT_SUPPORTED);
		bluetoothFacade = btMan.getAdapter();
		Error.check(bluetoothFacade == null, parentActivity, "Could not get BT Adapter", Error.BTLE_NOT_SUPPORTED);
	}

	/**
	 * Start the scan and continue scanning until stopScan is called.
	 */
	public void startScan() {
		stopped = false;
		internalStart();
	}

	/**
	 * Do one iteration of scanning
	 */
	private void internalStart() {

		checkPermissions();

		// when our activity comes to the front, start scanning
		bluetoothFacade.getBluetoothLeScanner().startScan(Collections.singletonList(filter), scanSettings, scanResult);

		// schedule the next iteration
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				internalStop();
				if (!stopped) {
					internalStart();
				}
			}
		}, MWDevice.SCAN_INTERVAL_MS);
	}

	/**
	 * Stop the current scan and do not restart it
	 */
	public void stopScan() {
		stopped = true;
		internalStop();
	}

	/**
	 * stop the current iteration of the scan
	 */
	private void internalStop() {
		// this activity went to the background suspend scanning
		bluetoothFacade.getBluetoothLeScanner().stopScan(scanResult);
	}


	/**
	 * Scan callback. This is called when any result from a scan is found.
	 */
	private final ScanCallback scanResult = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			//called when each scan result comes through
			Log.d(SCANNER, String.format("scan result %s rssi:%d", result.getDevice().getName(), result.getRssi()));
			super.onScanResult(callbackType, result);
		}

		@Override
		public void onScanFailed(int errorCode) {
			// An error occurred in scanning. Display the error.
			Error.check(errorCode == ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED,
					parentActivity, "Scan reported as unsupported", Error.BTLE_NOT_SUPPORTED);
			Error.check(true, parentActivity, String.format("Scan error %d", errorCode), Error.RESTART_DEVICE);
		}
	};

	/**
	 * Starting in Android 6, you MUST ask the user when attempting a "dangerous" operation for the first time.
	 * The BT LE scan can give the app a rough location which is considered dangerous.
	 */
	private void checkPermissions() {

		if(ContextCompat.checkSelfPermission(parentActivity.getApplicationContext(),
				android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(parentActivity, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
		}

		// check that the user approved. We're dead in the water if they don't.
		Error.check(ContextCompat.checkSelfPermission(
				parentActivity.getApplicationContext(),
				android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED,
			parentActivity, "User did not accept permissions",
				new Error("Course Location Permissions are required for Bluetooth Low Energy scans");
	}
}
