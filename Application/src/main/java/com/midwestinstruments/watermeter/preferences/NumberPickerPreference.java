package com.midwestinstruments.watermeter.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

import com.midwestinstruments.watermeter.R;

public class NumberPickerPreference extends DialogPreference {

	private int value = 50;
	private LimitedNumberPicker picker;
	private final AdjustmentFormatter formatter = new AdjustmentFormatter();

	public NumberPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.num_pref);
		setSummary(formatter.format(value));
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		this.picker = (LimitedNumberPicker) view.findViewById(R.id.pref_num_picker);
		picker.setValue(value);
		setSummary(formatter.format(picker.getValue()));
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		if (which == DialogInterface.BUTTON_POSITIVE) {
			value = picker.getValue();
			setSummary(formatter.format(picker.getValue()));
			callChangeListener(value);
		}
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {

		if(restorePersistedValue) {
			this.value = 50; // todo
		} else {
			this.value = 50;
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, 0);
	}
}
