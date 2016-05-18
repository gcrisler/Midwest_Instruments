package com.midwestinstruments.watermeter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import com.example.android.bluetoothlegatt.SampleGattAttributes;

import java.util.UUID;

/**
 * Created by byronh on 5/13/16.
 */
public class BTDeviceConnection {

	/**
	 * The interface to all bluetooth operations
	 */
	private BluetoothAdapter bluetoothFacade;

	private Activity parentActivity;

	private BluetoothGatt btGatt;

	private BluetoothGattService btGattService;

	private BTDeviceCallback operationCallback;

	private BTOperationQueue queue;

	public interface BTDeviceCallback {
		void onFlowRateUpdate(int flowrate);
		void onTotalUpdate(int floatRate);
		void onResetTotalUpdate(int resetRate);
	}

	public BTDeviceConnection(Activity parent) {
		parentActivity = parent;
		// populate the bluetooth adapter when the scanner is first created.
		BluetoothManager btMan = (BluetoothManager)parentActivity.getSystemService(Context.BLUETOOTH_SERVICE);
		Error.check(btMan == null, parentActivity, "Could not get BT Manager", Error.BTLE_NOT_SUPPORTED);
		bluetoothFacade = btMan.getAdapter();
	}

	public void setCallback(BTDeviceCallback callback) {
		this.operationCallback = callback;
	}

	public void connect(BluetoothDevice device) {
		btGatt = device.connectGatt(parentActivity.getApplicationContext(), false, callback);
		btGatt.connect();
		queue = new BTOperationQueue();
	}

	public void disconnect() {
		btGatt.disconnect();
		queue.stopQueue();
	}

	/**
	 * Reset Total
	 */
	public void resetTotal() {
		queue.scheduleOperation(new Runnable() {
			@Override
			public void run() {
				BluetoothGattCharacteristic reset = btGattService.getCharacteristic(UUID.fromString(MWDevice
						.RESET_FLOAT_RATE_UUID_CHAR));
				reset.setValue(0, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
				btGatt.writeCharacteristic(reset);
			}
		});
	}

	BluetoothGattCallback callback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if(newState == BluetoothProfile.STATE_CONNECTED) {
				btGatt.discoverServices();
			}
			super.onConnectionStateChange(gatt, status, newState);
		}

		private void setupCharacteristic(BluetoothGattCharacteristic characteristic) {
			btGatt.setCharacteristicNotification(characteristic, true);
			final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes
					.CLIENT_CHARACTERISTIC_CONFIG));
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			queue.scheduleOperation(new Runnable() {
				@Override
				public void run() {
					btGatt.writeDescriptor(descriptor);
				}
			});

		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			btGattService = btGatt.getService(UUID.fromString(MWDevice.SERVICE_UUID));

			setupCharacteristic(btGattService.getCharacteristic(UUID.fromString(MWDevice.FLOAT_RATE_UUID_CHAR)));
			setupCharacteristic(btGattService.getCharacteristic(UUID.fromString(MWDevice.TOTALIZED_FLOAT_RATE_UUID_CHAR)));
			setupCharacteristic(btGattService.getCharacteristic(UUID.fromString(MWDevice.RESET_FLOAT_RATE_UUID_CHAR)));

			super.onServicesDiscovered(gatt, status);
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			// we'll just cheat.
			onCharacteristicChanged(gatt, characteristic);

			super.onCharacteristicRead(gatt, characteristic, status);
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			// called for notifications
			if(operationCallback != null) {
				if(characteristic.getUuid().equals(UUID.fromString(MWDevice.FLOAT_RATE_UUID_CHAR))) {
					operationCallback.onFlowRateUpdate(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0));
				} else if (characteristic.getUuid().equals(UUID.fromString(MWDevice.TOTALIZED_FLOAT_RATE_UUID_CHAR))) {
					operationCallback.onTotalUpdate(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0));
				} else if (characteristic.getUuid().equals(UUID.fromString(MWDevice.RESET_FLOAT_RATE_UUID_CHAR))) {
					operationCallback.onResetTotalUpdate(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0));
				}
			}
			super.onCharacteristicChanged(gatt, characteristic);
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			queue.markOperationComplete();
			super.onDescriptorWrite(gatt, descriptor, status);
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			queue.markOperationComplete();
			super.onCharacteristicWrite(gatt, characteristic, status);
		}

		@Override
		public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
			queue.markOperationComplete();
			super.onReliableWriteCompleted(gatt, status);
		}
	};
}
