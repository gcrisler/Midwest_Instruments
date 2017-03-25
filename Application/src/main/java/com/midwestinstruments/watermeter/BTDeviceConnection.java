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
import android.util.Log;


import java.util.UUID;

/**
 * Created by byronh on 5/13/16.
 */
public class BTDeviceConnection {

	public static final String TAG = BTDeviceConnection.class.getSimpleName();

	public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

	/**
	 * The interface to all bluetooth operations
	 */
	private BluetoothAdapter bluetoothFacade;

	private Activity parentActivity;

	private volatile BluetoothGatt btGatt;

	private volatile BluetoothGattService btGattService;

	private volatile BTFlowDeviceCallback flowCallback = new DefaultBTDeviceCallback();
	private volatile BTSettingsDeviceCallback settingsCallback = new DefaultBTDeviceCallback();

	private BTOperationQueue queue;

	public interface BTFlowDeviceCallback {
		void onFlowRateUpdate(int flowrate);
		void onTotalUpdate(int floatRate);
		void onResetTotalUpdate(int resetRate);
		void onConnect();
	}

	public interface  BTSettingsDeviceCallback {
		void onPipeSizeUpdate(int pipeSize);
		void onAdjustmentUpdate(int adjustment);
		void onIdUpdate(String id);
		void onSerialUpdate(int serial);
	}

	public BTDeviceConnection(Activity parent) {
		parentActivity = parent;
		// populate the bluetooth adapter when the scanner is first created.
		BluetoothManager btMan = (BluetoothManager)parentActivity.getSystemService(Context.BLUETOOTH_SERVICE);
		Error.check(btMan == null, parentActivity, "Could not get BT Manager", Error.BTLE_NOT_SUPPORTED);
		bluetoothFacade = btMan.getAdapter();
	}

	public void setCallback(BTFlowDeviceCallback callback) {
		if(callback == null) {
			this.flowCallback = new DefaultBTDeviceCallback();
		} else {
			this.flowCallback = callback;
		}
	}

	public void setSettingsCallback(BTSettingsDeviceCallback callback) {
		if(callback == null) {
			this.settingsCallback = new DefaultBTDeviceCallback();
		} else {
			this.settingsCallback = callback;
		}
	}

	public void connect(BluetoothDevice device) {
		Log.d(TAG, "BT Connect");
		btGatt = device.connectGatt(parentActivity.getApplicationContext(), false, gattCallback);
		btGatt.connect();
		queue = new BTOperationQueue();
	}

	public void disconnect() {
		Log.d(TAG, "BT Disconnect");
		btGatt.disconnect();
		queue.stopQueue();
	}

	public void whenFinished(Runnable r) {
		Log.d(TAG, "schedule external operation");
		queue.scheduleOperation(() -> {
			Log.d(TAG, "execute external operation");
			r.run();
		}, false);
	}

	private interface BTCharacteristicOperation {
		void execute(BluetoothGattCharacteristic gattChar);
	}

	private void scheduleGattWrite(String gattUUID, BTCharacteristicOperation op) {
		queue.scheduleOperation(()->{
				BluetoothGattCharacteristic gattChar = btGattService.getCharacteristic(UUID.fromString(gattUUID));
				op.execute(gattChar);
				btGatt.writeCharacteristic(gattChar);
			}, true
		);
	}


	/**
	 * Reset Total
	 */
	public void resetTotal() {
		scheduleGattWrite(MWDevice.RESET_FLOAT_RATE_UUID_CHAR, (BluetoothGattCharacteristic gattChar) -> {
			gattChar.setValue(MWDevice.RESET_VALUE, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
		});
	}

	class Settings {
		public void setMeterId(String id) {
			if(id.length() < 10) {
				scheduleGattWrite(MWDevice.ID_UUID_CHAR, (BluetoothGattCharacteristic gattChar) -> {
					gattChar.setValue(id.getBytes());
				});
			}
		}

		public void setPipeSize(int index) {
			if(index >= 0 && index < 16) {
				scheduleGattWrite(MWDevice.PIPE_SIZE_INDEX_UUID_CHAR, (BluetoothGattCharacteristic gattChar) -> {
					gattChar.setValue(index, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
				});
			}
		}

		public void setAdjustmentFactor(int percentage) {
			if(percentage >= 500 && percentage <= 1500) {
				scheduleGattWrite(MWDevice.ADJUSTMENT_FACTOR_UUID_CHAR, (BluetoothGattCharacteristic gattChar) -> {
					gattChar.setValue(percentage, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
				});
			}
		}
	}

	public final Settings SETTINGS = new Settings();

	private void setupCharacteristic(BluetoothGattCharacteristic characteristic) {
		btGatt.setCharacteristicNotification(characteristic, true);
		final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
		descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		queue.scheduleOperation(() -> {
			btGatt.writeDescriptor(descriptor);
		}, true);

	}
//
//	public void setupSettings() {
//		btGattService = btGatt.getService(UUID.fromString(MWDevice.SERVICE_UUID));
//
//		setupCharacteristic(btGattService.getCharacteristic(UUID.fromString(MWDevice.ADJUSTMENT_FACTOR_UUID_CHAR)));
//		setupCharacteristic(btGattService.getCharacteristic(UUID.fromString(MWDevice.PIPE_SIZE_INDEX_UUID_CHAR)));
//		//setupCharacteristic(btGattService.getCharacteristic(UUID.fromString(MWDevice.ID_UUID_CHAR)));
//	}

	public void setupMeter() {
		Log.d(TAG, "meter setup");
		btGattService = btGatt.getService(UUID.fromString(MWDevice.SERVICE_UUID));

		setupCharacteristic(btGattService.getCharacteristic(UUID.fromString(MWDevice.FLOW_RATE_UUID_CHAR)));
		setupCharacteristic(btGattService.getCharacteristic(UUID.fromString(MWDevice.TOTALIZED_FLOW_RATE_UUID_CHAR)));
		setupCharacteristic(btGattService.getCharacteristic(UUID.fromString(MWDevice.RESET_FLOAT_RATE_UUID_CHAR)));

	}

	public void read(String uuid) {
		Log.d(TAG, "schedule "+uuid);
		queue.scheduleOperation(() -> {
			Log.d(TAG, "execute "+uuid);
			btGatt.readCharacteristic(btGattService.getCharacteristic(UUID.fromString(uuid)));
		}, true);
	}

	private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if(newState == BluetoothProfile.STATE_CONNECTED) {
				btGatt.discoverServices();
				Log.d(TAG, "BT Connected");
			} else {
				queue.setPaused(true);
				Log.d(TAG, "BT Disconnected");
			}
			super.onConnectionStateChange(gatt, status, newState);
		}


		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			setupMeter();
			queue.setPaused(false);
			Log.d(TAG, "BT Services Discovered");
			flowCallback.onConnect();
			super.onServicesDiscovered(gatt, status);
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			// we'll just cheat.
			onCharacteristicChanged(gatt, characteristic);

			super.onCharacteristicRead(gatt, characteristic, status);
			queue.markOperationComplete();
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			// called for notifications
			switch(characteristic.getUuid().toString()) {
				case MWDevice.FLOW_RATE_UUID_CHAR:
					Log.d(TAG, "Receive flow rate");
					flowCallback.onFlowRateUpdate(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0));
					break;
				case MWDevice.TOTALIZED_FLOW_RATE_UUID_CHAR:
					Log.d(TAG, "Receive Total");
					flowCallback.onTotalUpdate(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0));
					break;
				case MWDevice.RESET_FLOAT_RATE_UUID_CHAR:
					Log.d(TAG, "Receive reset total");
					flowCallback.onResetTotalUpdate(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0));
					break;
				case MWDevice.PIPE_SIZE_INDEX_UUID_CHAR:
					Log.d(TAG, "Receive pipe size");
					settingsCallback.onPipeSizeUpdate(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
					break;
				case MWDevice.ADJUSTMENT_FACTOR_UUID_CHAR:
					Log.d(TAG, "Receive adjustment");
					settingsCallback.onAdjustmentUpdate(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0));
					break;
				case MWDevice.ID_UUID_CHAR:
					Log.d(TAG, "Receive ID");
					settingsCallback.onIdUpdate(characteristic.getStringValue(0));
					break;
				//TODO serial? and Id
			}
			super.onCharacteristicChanged(gatt, characteristic);
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			queue.markOperationComplete();
			Log.d(TAG, "Descriptor written");
			super.onDescriptorWrite(gatt, descriptor, status);
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			queue.markOperationComplete();
			Log.d(TAG, "Characteristic written");
			super.onCharacteristicWrite(gatt, characteristic, status);
		}

		@Override
		public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
			queue.markOperationComplete();
			Log.d(TAG, "ReliableWrite written");
			super.onReliableWriteCompleted(gatt, status);
		}
	};
}
