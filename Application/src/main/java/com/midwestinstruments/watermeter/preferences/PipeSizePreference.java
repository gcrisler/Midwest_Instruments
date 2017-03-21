package com.midwestinstruments.watermeter.preferences;

import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;

/**
 * Created by byronh on 2/26/17.
 */
public class PipeSizePreference extends ListPreference {

	private Invoke<Integer> saveCallback;

	public PipeSizePreference(Context c) {
		this(c, null);
	}

	public PipeSizePreference(Context c, AttributeSet attr) {
		super(c, attr);
	}


	public void setSaveCallback(Invoke<Integer> setter) {
		saveCallback = setter;
	}

	public void setValue(int index) {
		super.setValueIndex(index);
		setSummary(index >= 0 ? getEntries()[index] : null);
		setDefaultValue(index);
	}

	@Override
	protected void notifyChanged() {
		super.notifyChanged();
		int index = findIndexOfValue(getValue());

		// Set the summary to reflect the new value.
		setSummary(index >= 0 ? getEntries()[index] : null);

		if(saveCallback != null) {
			saveCallback.invoke(index);
		}
	}
}
