package com.midwestinstruments.watermeter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Data Container for scan results to be displayed on the screen
 * Created by byronh on 5/12/16.
 */
public class ScanData implements Comparable<ScanData> {
	private String name;
	private int rssi;
	private int flow;

	private int totalizer;
	private int pipeIndex;
	private int resettableTotalizer;


	private BluetoothDevice device;

	public void set(ScanResult result) {
		device = result.getDevice();
		name = device.getName();
		rssi = result.getRssi();
		byte[] data = result.getScanRecord().getManufacturerSpecificData(MWDevice.MANUFACTURER_ID);
		if(data != null) {
			ByteBuffer buff = ByteBuffer.wrap(data);
			buff.order(ByteOrder.LITTLE_ENDIAN);
			flow = buff.getShort(0);
			totalizer = buff.getInt(2);
			pipeIndex = data[6] & 0x1F;
			resettableTotalizer = buff.getInt(7);
		}
		else {
			Log.w(ScanData.class.getSimpleName(), "null manufacturer data");
		}
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

	public int getPipeIndex() { return pipeIndex; }

	public int getResettableTotalizer() { return resettableTotalizer; }

	public int getTotalizer() { return totalizer; }

	@Override
	public boolean equals(Object o) {
		if(o instanceof ScanData) {

			return device.getAddress().equals(((ScanData) o).getDevice().getAddress());
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
