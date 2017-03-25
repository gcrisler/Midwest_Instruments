package com.midwestinstruments.watermeter;

import java.math.BigInteger;

/**
 * Created by byronh on 3/23/17.
 */
public class Display {
	public static String formatFlowValue(int value, int pipeSize) {
		if(pipeSize >= 0 && pipeSize < MWDevice.DECIMALS_FOR_PIPE_SIZE.length) {
			if (MWDevice.DECIMALS_FOR_PIPE_SIZE[pipeSize] == 1) {
				return String.format("%d.%01d", value / 10, value % 10);
			} else {
				return String.format("%d", value);
			}
		}
		return "";
	}

	public static String asHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
 		for(byte b : bytes) {
			sb.append(String.format("%02x ", b));
		}
		return sb.toString();
	}
}
