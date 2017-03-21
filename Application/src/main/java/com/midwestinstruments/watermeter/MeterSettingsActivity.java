package com.midwestinstruments.watermeter;


import android.app.ActionBar;
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
	}

	private boolean settingsLoaded = false;
	static PrefValue<Integer> pipeSizePref = new PrefValue<>();
	static PrefValue<Integer> adjustmentPref = new PrefValue<>();
	static PrefValue<String> idPref = new PrefValue<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().clear().commit();

		MeterActivity.connection.setSettingsCallback(settingsCallback);
		MeterActivity.connection.read(MWDevice.PIPE_SIZE_INDEX_UUID_CHAR);
		MeterActivity.connection.read(MWDevice.ADJUSTMENT_FACTOR_UUID_CHAR);
		idPref.set("TODO");

		ActionBar bar = getActionBar();
		if(bar != null) {
			bar.setDisplayHomeAsUpEnabled(true);
		}

	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			if (!super.onMenuItemSelected(featureId, item)) {
				finish();
			}
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
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

//	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
//			(Preference preference, Object value) -> {
//		String stringValue = value != null? value.toString() : "";
//
//		if (preference instanceof ListPreference) {
//			// For list preferences, look up the correct display value in
//			// the preference's 'entries' list.
//			ListPreference listPreference = (ListPreference) preference;
//			int index = listPreference.findIndexOfValue(stringValue);
//
//			// Set the summary to reflect the new value.
//			preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
//
//		} else {
//			// For all other preferences, set the summary to the value's
//			// simple string representation.
//			preference.setSummary(stringValue);
//		}
//
//		return true;
//	};


//	private static <T> void bindPreferenceSummaryToValue(Preference preference, T data) {
//
//		//TODO - redo all this. pass a lambda that writes the value?
//
//		//preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
//
//		preference.setDefaultValue(data);
//		//sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, data);
//	}

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



//			bindPreferenceSummaryToValue(findPreference("meter_name_pref"), idPref.value);
//			bindPreferenceSummaryToValue(findPreference("pipe_size_pref"), pipeSizePref.value);
//			//bindPreferenceSummaryToValue(findPreference("flow_rate_pref"), adjustmentPref.value);
//			bindPreferenceSummaryToValue(findPreference("adjustment_factor_pref"), adjustmentPref.value);
		}
	}


}
