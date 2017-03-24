package com.midwestinstruments.watermeter;

/**
 * Created by byronh on 3/23/17.
 */
public class Display {
	public static String formatFlowValue(int value, int pipeSize) {
		if(pipeSize >= 0) {
			if (MWDevice.DECIMALS_FOR_PIPE_SIZE[pipeSize] == 1) {
				return String.format("%d.%01d", value / 10, value % 10);
			} else {
				return String.format("%d", value);
			}
		}
		return "";
	}
}
