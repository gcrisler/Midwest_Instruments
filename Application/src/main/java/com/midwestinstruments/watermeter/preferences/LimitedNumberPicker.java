package com.midwestinstruments.watermeter.preferences;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.NumberPicker;

/**
 * Custom-formatted number picker
 */
public class LimitedNumberPicker extends NumberPicker {


	public LimitedNumberPicker(Context c) {
		this(c, null);
	}

	public LimitedNumberPicker(Context c, AttributeSet attr) {
		super(c, attr);

		setMinValue(0);
		setMaxValue(100);
		super.setWrapSelectorWheel(false);
		setDisplayedValues(new AdjustmentFormatter().buildValues());
	}
}
