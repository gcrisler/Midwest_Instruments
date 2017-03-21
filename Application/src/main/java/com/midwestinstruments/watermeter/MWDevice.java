package com.midwestinstruments.watermeter;

/**
 * Created by byronh on 5/9/16.
 */
public class MWDevice {
	/**
	 * Name of device as reported from BT scan
	 */
	public static final String NAME = "MIC Flow";

	/**
	 * Interval between BTLE scans.
	 */
	public static final long SCAN_INTERVAL_MS = 5000;

	public static final String SERVICE_UUID = "b1bf0001-1126-4d34-af90-1f1a1bf37fb9";
	public static final String FLOW_RATE_UUID_CHAR = "b1bf0002-1126-4d34-af90-1f1a1bf37fb9";
	public static final String TOTALIZED_FLOW_RATE_UUID_CHAR = "b1bf0003-1126-4d34-af90-1f1a1bf37fb9";
	public static final String RESET_FLOAT_RATE_UUID_CHAR = "b1bf0004-1126-4d34-af90-1f1a1bf37fb9";
	public static final String PIPE_SIZE_INDEX_UUID_CHAR = "b1bf0005-1126-4d34-af90-1f1a1bf37fb9";
	public static final String ADJUSTMENT_FACTOR_UUID_CHAR = "b1bf0006-1126-4d34-af90-1f1a1bf37fb9";
	public static final String SERIAL_NR_UUID_CHAR = "b1bf0007-1126-4d34-af90-1f1a1bf37fb9";
	//public static final String ID_UUID_CHAR = "b1bf000?-1126-4d34-af90-1f1a1bf37fb9";

	public static int[] DECIMALS_FOR_PIPE_SIZE = {
			0,
			0,
			0,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1
	};
//	#define BLE_UUID_FMS_SIZE_POINTER_CHARACTERISTIC (0x0005)    //Size pointer UUID
}
