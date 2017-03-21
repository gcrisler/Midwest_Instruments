package com.midwestinstruments.watermeter;

import android.preference.Preference;

/**
 * Created by byronh on 2/26/17.
 */
public class BTPrefsManager {

	private BTDeviceConnection connection;

	private Preference idPref;
	private Preference adjustmentPref;
	private Preference pipeSizePref;

	void init(BTDeviceConnection con) {
		this.connection = con;
	}

	public void setup(Preference idPref, Preference adjustmentPref, Preference pipeSizePref) {
		this.idPref = idPref;
		this.adjustmentPref = adjustmentPref;
		this.pipeSizePref = pipeSizePref;
	}
}
