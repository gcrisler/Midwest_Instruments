package com.midwestinstruments.watermeter;


import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.midwestinstruments.watermeter.preferences.AdjustmentPreference;
import com.midwestinstruments.watermeter.preferences.MeterNamePreference;
import com.midwestinstruments.watermeter.preferences.PipeSizePreference;


public class MeterSettingsActivity extends PreferenceActivity {

	private BTDeviceConnection.BTSettingsDeviceCallback settingsCallback =
			new BTDeviceConnection.BTSettingsDeviceCallback() {
				@Override
				public void onPipeSizeUpdate(int pipeSize) {
					runOnUiThread(() -> {
						pipeSizePref.set(pipeSize);
						loadSettings();
					});
				}

				@Override
				public void onAdjustmentUpdate(int adjustment) {
					runOnUiThread(() -> {
						adjustmentPref.set(adjustment);
						loadSettings();
					});
				}

				@Override
				public void onIdUpdate(String id) {
					runOnUiThread(() -> {
						idPref.set(id);
						loadSettings();
					});
				}

				@Override
				public void onSerialUpdate(int serial) {

				}
			};

	private void loadSettings() {

		if(pipeSizePref.loaded && adjustmentPref.loaded && idPref.loaded && !settingsLoaded) {
			settingsLoaded = true;
			getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
		}
	}

	static class PrefValue<T> {
		T value;
		boolean loaded;

		public void set(T value) {
			this.value = value;
			loaded = true;
		}

		public void unload() {
			loaded = false;
		}
	}

	private boolean settingsLoaded = false;
	static PrefValue<Integer> pipeSizePref = new PrefValue<>();
	static PrefValue<Integer> adjustmentPref = new PrefValue<>();
	static PrefValue<String> idPref = new PrefValue<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().clear().commit();

		pipeSizePref.unload();
		adjustmentPref.unload();
		idPref.unload();

		MeterActivity.connection.setSettingsCallback(settingsCallback);
		MeterActivity.connection.read(MWDevice.PIPE_SIZE_INDEX_UUID_CHAR);
		MeterActivity.connection.read(MWDevice.ADJUSTMENT_FACTOR_UUID_CHAR);
		MeterActivity.connection.read(MWDevice.ID_UUID_CHAR);

		ActionBar bar = getActionBar();
		if(bar != null) {
			bar.setDisplayHomeAsUpEnabled(true);
		}

	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			goBack();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onBackPressed() {
		goBack();
	}

	private void goBack() {
		MeterActivity.connection.whenFinished(() -> {
			runOnUiThread(() -> {
				finish();
			});
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		MeterActivity.connection.setSettingsCallback(null);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MeterActivity.connection.setSettingsCallback(settingsCallback);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MeterActivity.connection.setSettingsCallback(null);
	}

	/**
	 * This method stops fragment injection in malicious applications.
	 */
	protected boolean isValidFragment(String fragmentName) {
		return PreferenceFragment.class.getName().equals(fragmentName) || SettingsFragment.class.getName().equals(fragmentName);
	}

	public static class SettingsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			addPreferencesFromResource(R.xml.pref_meter);

			MeterNamePreference meterName = (MeterNamePreference) findPreference("meter_name_pref");
			meterName.setDefaultValue(idPref.value);
			meterName.setSaveCallback((String val) -> {
				MeterActivity.connection.SETTINGS.setMeterId(val);
			});

			PipeSizePreference pipePref = (PipeSizePreference)findPreference("pipe_size_pref");
			pipePref.setValue(pipeSizePref.value);
			pipePref.setSaveCallback((Integer val)->{
				MeterActivity.connection.SETTINGS.setPipeSize(val);
			});

			AdjustmentPreference adjustPref = (AdjustmentPreference)findPreference("adjustment_factor_pref");
			adjustPref.setValue(adjustmentPref.value);
			adjustPref.setSaveCallback((Integer val)->{
				MeterActivity.connection.SETTINGS.setAdjustmentFactor(val);
			});

		}
	}


}
