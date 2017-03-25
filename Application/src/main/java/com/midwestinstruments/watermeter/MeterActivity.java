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
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MeterActivity extends Activity {

	private static final int FAIL_AFTER_MS = 10000;

	static BluetoothDevice device;

	static BTDeviceConnection connection;

	boolean hasData = false;
	Timer timer;
	private boolean previouslyConnected = false;

	private String id = "";
	private int serial = 0;
	private int pipeSize = -1;
	private int flowrate = -1;

	private class TimeoutTask extends TimerTask {
		@Override
		public void run() {
			// we couldn't find anything. Go back.
			Intent intent = new Intent(MeterActivity.this, ScanActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
	}

	private BTDeviceConnection.BTFlowDeviceCallback callback = new BTDeviceConnection.BTFlowDeviceCallback() {
		@Override
		public void onFlowRateUpdate(final int flowrate) {
			runOnUiThread(() -> {
				MeterActivity.this.flowrate = flowrate;
				updateGui();
				TextView view = (TextView) findViewById(R.id.floatRate);
				view.setText(Display.formatFlowValue(flowrate, pipeSize));
			});
		}

		@Override
		public void onTotalUpdate(final int floatRate) {
			runOnUiThread(() -> {
				updateGui();
				TextView view = (TextView)findViewById(R.id.totalizerFlowRate);
				view.setText(Display.formatFlowValue(floatRate, pipeSize));
			});
		}

		@Override
		public void onResetTotalUpdate(final int resetRate) {
			runOnUiThread(() -> {
				updateGui();
				TextView view = (TextView) findViewById(R.id.resetFlowRate);
				view.setText(Display.formatFlowValue(resetRate, pipeSize));
			});

		}

		@Override
		public void onConnect() {
			readSettings();
			previouslyConnected = true;
		}
	};

	private BTDeviceConnection.BTSettingsDeviceCallback settingsCallback = new BTDeviceConnection.BTSettingsDeviceCallback() {
		@Override
		public void onPipeSizeUpdate(int pipeSize) {
			MeterActivity.this.pipeSize = pipeSize;
			runOnUiThread(() -> {
				updateGui();
			});
		}

		@Override
		public void onAdjustmentUpdate(int adjustment) {}

		@Override
		public void onIdUpdate(String id) {
			MeterActivity.this.id = id;
			runOnUiThread(() -> {
				updateGui();
			});
		}

		@Override
		public void onSerialUpdate(int serial) {
			MeterActivity.this.serial = serial;
			runOnUiThread(() -> {
				updateGui();
			});

		}
	};

	protected void updateGui() {
		if(!hasData && pipeSize > -1 && flowrate > -1) {
			hasData = true;
			timer.cancel();
			ProgressBar progress = (ProgressBar) findViewById(R.id.progressBar);
			progress.setVisibility(View.INVISIBLE);
		}
		if(this.id != null && !this.id.isEmpty() && this.serial > 0) {
			getActionBar().setTitle("Meter: " + this.id + " - #" + this.serial);
		}
	}

	@Override
	protected void onPause() {
		pipeSize = -1;
		flowrate = -1;
		super.onPause();
		timer.cancel();
	}

	@Override
	protected void onResume() {
		hasData = false;
		pipeSize = -1;
		flowrate = -1;
		timer.cancel();
		timer = new Timer();
		timer.schedule(new TimeoutTask(), FAIL_AFTER_MS);

		ProgressBar progress = (ProgressBar)findViewById(R.id.progressBar);
		progress.setVisibility(View.VISIBLE);
		if(previouslyConnected) {
			readSettings();
		}
		super.onResume();
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
			Toast.makeText(getApplicationContext(), "Totalizer Reset", Toast.LENGTH_LONG).show();

			// we cheat. just set the value onscreen to zero immediately so the user gets feedback
			TextView view = (TextView) findViewById(R.id.resetFlowRate);
			view.setText(Display.formatFlowValue(0, pipeSize));

		});
		connection.connect(device);
	}

	private void readSettings() {
		connection.setSettingsCallback(settingsCallback);
		connection.read(MWDevice.PIPE_SIZE_INDEX_UUID_CHAR);
		connection.read(MWDevice.SERIAL_NR_UUID_CHAR);
		connection.read(MWDevice.ID_UUID_CHAR);
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
				goBack();
				return true;
			case R.id.menu_config_action:
				connection.whenFinished(() -> {
					runOnUiThread(() -> {
						Intent meterActivityIntent = new Intent(this, MeterSettingsActivity.class);
						startActivity(meterActivityIntent);
					});
				});
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		goBack();
	}

	private void goBack() {
		ProgressBar progress = (ProgressBar)findViewById(R.id.progressBar);
		progress.setVisibility(View.VISIBLE);

		// wait for BT queue to finish. If we don't, we might miss updates
		connection.whenFinished(() -> {
			runOnUiThread(() -> {
				Intent intent = new Intent(this, ScanActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			});
		});
	}
	@Override
	protected void onDestroy() {
		// This won't get run if we get killed, but if we do, the OS will recoup the resources anyway
		connection.disconnect();
		super.onDestroy();
	}
}
