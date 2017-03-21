package com.midwestinstruments.watermeter.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

import com.midwestinstruments.watermeter.R;


public class AdjustmentPreference extends DialogPreference {

	private int value = 50;
	private LimitedNumberPicker picker;
	private final AdjustmentFormatter formatter = new AdjustmentFormatter();

	private Invoke<Integer> saveCallback;

	public AdjustmentPreference(Context context) {
		super(context);
		setDialogLayoutResource(R.layout.num_pref);
		setSummary(formatter.format(value));
	}

	public AdjustmentPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.num_pref);
		setSummary(formatter.format(value));
	}

	public void setSaveCallback(Invoke<Integer> setter) {
		saveCallback = setter;
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		this.picker = (LimitedNumberPicker) view.findViewById(R.id.pref_num_picker);
		picker.setDisplayedValues(formatter.buildValues());
		picker.setValue(value);
		setSummary(formatter.format(picker.getValue()));
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		if (which == DialogInterface.BUTTON_POSITIVE) {
			value = picker.getValue();
			setSummary(formatter.format(value));
			if(saveCallback != null) {
				saveCallback.invoke(value * 10 + 500);
			}
		}
	}

	public void setValue(int val) {
		value = (val - 500) / 10;
		if(picker != null) {
			picker.setValue(value);
		}
		setSummary(formatter.format(value));
		setDefaultValue(value);
	}

//	@Override
//	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
//
//		picker.setValue((Integer)defaultValue);
//		value = picker.getValue();
//
//		setSummary(formatter.format(value));
//	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, 0);
	}

	class AdjustmentFormatter implements NumberPicker.Formatter {

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
}
