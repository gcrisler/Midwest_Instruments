package com.midwestinstruments.watermeter.preferences;


import android.widget.NumberPicker;

/**
 * Created by byronh on 5/27/16.
 */
public class AdjustmentFormatter implements NumberPicker.Formatter {

	@Override
	public String format(int value) {
		return String.format("%d%%", value - 50);
	}

	public String[] buildValues() {
		String[] results = new String[101];
		for(int i=0; i<101; i++) {
			results[i] = format(i);
		}
		return results;
	}
}
