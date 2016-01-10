package com.midwestinstruments.watermeter.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.NumberPicker;

/**
 * Custom-formatted number picker
 */
public class LimitedNumberPicker extends NumberPicker {

	/**
	 * NumberPicker only supports positive numbers. We have negative numbers, so offset the values by this amount so the numbers positive under the covers
	 */
	private int offset = 0;

	public LimitedNumberPicker(Context c) {
		super(c);
	}

	public LimitedNumberPicker(Context c, AttributeSet attributes) {
		super(c, attributes);
		setup(attributes);
	}

	public LimitedNumberPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup(attrs);
	}

	/**
	 * Read the min and max from the xml instead of hardcoding.
	 * @param attrs attributes from the xml
	 */
	private void setup(AttributeSet attrs) {
		int min = attrs.getAttributeIntValue(null, "minValue", -50);
		int max = attrs.getAttributeIntValue(null, "maxValue", 50);

		offset = min;

		setMinValue(0);
		setMaxValue(max - min);
		setFormatter(new Formatter() {
			@Override
			public String format(int value) {
				return String.format("%d%%", value + offset);
			}
		});
	}

	@Override
	public int getValue() {
		return super.getValue() + offset;
	}
}
