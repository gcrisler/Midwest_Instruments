package com.midwestinstruments.watermeter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MeterActivity extends Activity {

	static BluetoothDevice device;

	static BTDeviceConnection connection;

	private BTDeviceConnection.BTDeviceCallback callback = new BTDeviceConnection.BTDeviceCallback() {
		@Override
		public void onFlowRateUpdate(final int flowrate) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					TextView view = (TextView) findViewById(R.id.floatRate);
					view.setText(Integer.toString(flowrate));
				}
			});
		}

		@Override
		public void onTotalUpdate(final int floatRate) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					TextView view = (TextView)findViewById(R.id.totalizerFlowRate);
					view.setText(Integer.toString(floatRate));
				}
			});
		}

		@Override
		public void onResetTotalUpdate(final int resetRate) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					TextView view = (TextView)findViewById(R.id.resetFlowRate);
					view.setText(Integer.toString(resetRate));
				}
			});

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_meter);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		connection = new BTDeviceConnection(this);
		connection.setCallback(callback);
		findViewById(R.id.resetButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				connection.resetTotal();
			}
		});
		connection.connect(device);
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
				startActivity(new Intent(this, MeterSettingsActivity.class));
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
