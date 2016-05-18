package com.midwestinstruments.watermeter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;

/**
 * Data Container for scan results to be displayed on the screen
 * Created by byronh on 5/12/16.
 */
public class ScanData implements Comparable<ScanData> {
	private String name;
	private int rssi;
	private int flow;
	private BluetoothDevice device;

	public void set(ScanResult result) {
		device = result.getDevice();
		name = device.getName();
		rssi = result.getRssi();
		flow = -1;
	}

	public int getFlow() {
		return flow;
	}

	public int getRssi() {
		return rssi;
	}

	public String getName() {
		return name;
	}

	public BluetoothDevice getDevice() { return device; }

	@Override
	public boolean equals(Object o) {
		if(o instanceof ScanData) {
			return name.equals(((ScanData) o).getName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public int compareTo(ScanData another) {
		return Integer.compare(rssi, another.getRssi());
	}
}
