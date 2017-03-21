package com.midwestinstruments.watermeter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MeterActivity extends Activity {

	private static final int FAIL_AFTER_MS = 10000;

	static BluetoothDevice device;

	static BTDeviceConnection connection;

	boolean hasData = false;
	Timer timer;

	private String id = "";
	private int serial = 0;
	private int pipeSize = -1;

	private class TimeoutTask extends TimerTask {
		@Override
		public void run() {
			// we couldn't find anything. Go back.
			Intent intent = new Intent(MeterActivity.this, ScanActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
	}

	private String formatValue(int value) {
		if(pipeSize >= 0) {
			if (MWDevice.DECIMALS_FOR_PIPE_SIZE[pipeSize] == 1) {
				return String.format("%d.%01d", value / 10, value % 10);
			} else {
				return String.format("%d", value);
			}
		}
		return "";
	}

	private BTDeviceConnection.BTFlowDeviceCallback callback = new BTDeviceConnection.BTFlowDeviceCallback() {
		@Override
		public void onFlowRateUpdate(final int flowrate) {
			runOnUiThread(() -> {
				setHasData();
				TextView view = (TextView) findViewById(R.id.floatRate);
				view.setText(formatValue(flowrate));
			});
		}

		@Override
		public void onTotalUpdate(final int floatRate) {
			runOnUiThread(() -> {
				setHasData();
				TextView view = (TextView)findViewById(R.id.totalizerFlowRate);
				view.setText(formatValue(floatRate));
			});
		}

		@Override
		public void onResetTotalUpdate(final int resetRate) {
			runOnUiThread(() -> {
				setHasData();
				TextView view = (TextView) findViewById(R.id.resetFlowRate);
				view.setText(formatValue(resetRate));
			});

		}
	};

	private BTDeviceConnection.BTSettingsDeviceCallback settingsCallback = new BTDeviceConnection.BTSettingsDeviceCallback() {
		@Override
		public void onPipeSizeUpdate(int pipeSize) {
			MeterActivity.this.pipeSize = pipeSize;
			runOnUiThread(() -> {
				setHasData();
			});
		}

		@Override
		public void onAdjustmentUpdate(int adjustment) {}

		@Override
		public void onIdUpdate(String id) {
			MeterActivity.this.id = id;
		}

		@Override
		public void onSerialUpdate(int serial) {
			MeterActivity.this.serial = serial;
		}
	};

	protected void setHasData() {
		if(pipeSize > -1) {
			hasData = true;
			timer.cancel();
			ProgressBar progress = (ProgressBar) findViewById(R.id.progressBar);
			progress.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		timer.cancel();
	}

	@Override
	protected void onResume() {
		super.onResume();
		hasData = false;
		timer.cancel();
		timer = new Timer();
		timer.schedule(new TimeoutTask(), FAIL_AFTER_MS);

		ProgressBar progress = (ProgressBar)findViewById(R.id.progressBar);
		progress.setVisibility(View.VISIBLE);
		readSettings();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_meter);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		timer = new Timer();
		timer.schedule(new TimeoutTask(), FAIL_AFTER_MS);

		connection = new BTDeviceConnection(this);
		connection.setCallback(callback);
		findViewById(R.id.resetButton).setOnClickListener((View v) -> {
			connection.resetTotal();
		});
		connection.connect(device);
	}

	private void readSettings() {
		connection.setSettingsCallback(settingsCallback);
		connection.read(MWDevice.PIPE_SIZE_INDEX_UUID_CHAR);
		connection.read(MWDevice.SERIAL_NR_UUID_CHAR);
		//connection.read(MWDevice.ID_UUID_CHAR);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.meter_config_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// app icon in action bar clicked; go home
				Intent intent = new Intent(this, ScanActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;
			case R.id.menu_config_action:
				Intent meterActivityIntent = new Intent(this, MeterSettingsActivity.class);
				startActivity(meterActivityIntent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {
		// This won't get run if we get killed, but if we do, the OS will recoup the resources anyway
		connection.disconnect();
		super.onDestroy();
	}
}
