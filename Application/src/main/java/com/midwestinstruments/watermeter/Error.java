package com.midwestinstruments.watermeter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by byronh on 5/9/16.
 */
public class Error {

	private final String message;

	public Error(String message) {
		this.message = message;
	}

	void execute(Context context) {
		new AlertDialog.Builder(context)
				.setMessage("Bluetooth LE not supported on this device")
				.create()
				.show();
	}

	public static final Error BTLE_NOT_SUPPORTED = new Error("Bluetooth LE not supported on this device");
	public static final Error RESTART_DEVICE = new Error("Something went wrong. Please restart your phone/tablet.");

	public static void check(boolean value, Activity activity, String devMsg, Error handler) {
		if(value) {
			Log.i(activity.getLocalClassName(), devMsg);
			handler.execute(activity);
		}
	}
}
