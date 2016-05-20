package com.midwestinstruments.watermeter;


import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

import com.midwestinstruments.watermeter.widget.LimitedNumberPicker;

public class NumberPickerPreference extends DialogPreference {

	LimitedNumberPicker picker;
	Integer initialValue;

	public NumberPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.num_pref);
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		this.picker = (LimitedNumberPicker) view.findViewById(R.id.pref_num_picker);

		if (this.initialValue != null) {
			picker.setValue(initialValue);
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		if (which == DialogInterface.BUTTON_POSITIVE) {
			this.initialValue = picker.getValue();
			persistInt(initialValue);
			callChangeListener(initialValue);
		}
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		int def = (defaultValue instanceof Number) ? (Integer) defaultValue :
				(defaultValue != null) ? Integer.parseInt(defaultValue.toString()) : 0;
		if (restorePersistedValue) {
			this.initialValue = getPersistedInt(def);
		} else {
			this.initialValue = (Integer) defaultValue;
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, 0);
	}
}
