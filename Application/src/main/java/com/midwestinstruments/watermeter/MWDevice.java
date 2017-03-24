package com.midwestinstruments.watermeter;

/**
 * Created by byronh on 5/9/16.
 */
public class MWDevice {

	public static final int MANUFACTURER_ID = 0x03b3;

	/**
	 * Interval between BTLE scans.
	 */
	public static final long SCAN_INTERVAL_MS = 7000;

	public static final String SERVICE_UUID = "b1bf0001-1126-4d34-af90-1f1a1bf37fb9";
	public static final String FLOW_RATE_UUID_CHAR = "b1bf0002-1126-4d34-af90-1f1a1bf37fb9";
	public static final String TOTALIZED_FLOW_RATE_UUID_CHAR = "b1bf0003-1126-4d34-af90-1f1a1bf37fb9";
	public static final String RESET_FLOAT_RATE_UUID_CHAR = "b1bf0004-1126-4d34-af90-1f1a1bf37fb9";
	public static final String PIPE_SIZE_INDEX_UUID_CHAR = "b1bf0005-1126-4d34-af90-1f1a1bf37fb9";
	public static final String ADJUSTMENT_FACTOR_UUID_CHAR = "b1bf0006-1126-4d34-af90-1f1a1bf37fb9";
	public static final String SERIAL_NR_UUID_CHAR = "b1bf0007-1126-4d34-af90-1f1a1bf37fb9";
	public static final String ID_UUID_CHAR = "b1bf0008-1126-4d34-af90-1f1a1bf37fb9";

	public static final int RESET_VALUE = 128;

	public static int[] DECIMALS_FOR_PIPE_SIZE = {
			1, // 1/2
			1,
			1,
			1,
			1, // 1.5 40
			0,
			0,
			0, // 3
			0,
			0, // 4 80
			0,
			0, // 6
			0,
			0, // 8 80
			0
	};
//	#define BLE_UUID_FMS_SIZE_POINTER_CHARACTERISTIC (0x0005)    //Size pointer UUID
}
