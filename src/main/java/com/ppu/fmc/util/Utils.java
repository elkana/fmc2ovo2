package com.ppu.fmc.util;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

	private static final Logger log = LoggerFactory.getLogger(Utils.class);

	public static boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

	public static <T> T nvl(T a, T b) {
		return (a == null) ? b : a;
	}

	public static void main(String[] args) {

		String[] arrays = { " eric" };

		System.err.println(StringUtils.joinString(",", arrays));
	}

	public static long setMinimumValueAsLong(String value, long minValue) {
		long val = value == null ? minValue : Long.parseLong(value);
		if (val < minValue)
			val = minValue;

		return val;

	}

	public static void printStat(String appInfo) {

		double max = Runtime.getRuntime().maxMemory() / 1024;
		double used = (Runtime.getRuntime().totalMemory() / 1024) - (Runtime.getRuntime().freeMemory() / 1024);
		DecimalFormat df = new DecimalFormat(" (0.0000'%')");
		DecimalFormat df2 = new DecimalFormat(" # 'KB'");

		log.info("AppInfo={}, UsedMemory={}, UsableMemory={}, cpu={}", appInfo, df2.format(used) + df.format(used / max * 100)
				,df2.format(max - used) + df.format((max - used) / max * 100)
				,Runtime.getRuntime().availableProcessors());

	}

	public static String convertHexToMacAddress(String hexa) {
		String[] arrays = hexa.split("(?<=\\G.{2})");
		return String.join(":", arrays);
	}

	public static long convertToSeconds(LocalDateTime ldt) {
		return ldt.atZone(ZoneId.systemDefault()).toEpochSecond();
	}

	public static LocalDateTime converToLDT(long seconds) {
		return Instant.ofEpochSecond(seconds).atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

}
