package com.midwestinstruments.watermeter.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * Created by byronh on 5/27/16.
 */
public class MeterNamePreference extends EditTextPreference {

	public MeterNamePreference(Context c) {
		this(c, null);
	}

	public MeterNamePreference(Context c, AttributeSet attr) {
		super(c, attr);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		if (which == DialogInterface.BUTTON_POSITIVE) {
			setText(getEditText().getText().toString());
			setSummary(getText());
			// write value
		}
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		if(restoreValue) {
			setText("xxx"); // Get from somewhere
		} else {
			setText(defaultValue.toString());
		}
		setSummary(getText());
	}
}
