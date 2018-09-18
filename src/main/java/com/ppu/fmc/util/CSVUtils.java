package com.ppu.fmc.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.ppu.fmc.exception.IpLocationNotFoundException;
import com.ppu.fmc.firepower.model.IpLocation;

public class CSVUtils {
	
	@SuppressWarnings("rawtypes")
	public static List loadIpLocationCsv(String csvFilename) throws JsonProcessingException, IOException {
		CsvSchema csvSchema = new CsvMapper().typedSchemaFor(IpLocation.class).withHeader();
		List list = new CsvMapper().readerFor(IpLocation.class)
				.with(csvSchema.withColumnSeparator(CsvSchema.DEFAULT_COLUMN_SEPARATOR))
				.readValues(new File(csvFilename)).readAll();

		/*
		 * for (int i = 0; i < list.size(); i++) { IpLocation _obj = (IpLocation)
		 * list.get(i);
		 * 
		 * System.out.println(_obj); }
		 */

		return list;

	}
	
	public static String getLocation(List<?> list, String ipAddress) throws IpLocationNotFoundException {
		if (StringUtils.isEmpty(ipAddress) || list == null || list.size() < 1)
			return "";

		// diambil
		StringTokenizer token = new StringTokenizer(ipAddress, ".");

		if (token.countTokens() != 4) {
			throw new IllegalArgumentException("IP address must be in the format 'xxx.xxx.xxx.xxx'");
		}

		int dots = 0;
		String byte1 = "";
		String byte2 = "";
		String byte3 = "";
		while (token.hasMoreTokens()) {
			++dots;

			if (dots == 1) {
				byte1 = token.nextToken();
			} else if (dots == 2) {
				byte2 = token.nextToken();
			} else if (dots == 3) {
				byte3 = token.nextToken();
			} else
				break;
		}

		String client3 = byte1 + "." + byte2 + "." + byte3;

		for (int i = 0; i < list.size(); i++) {
			IpLocation _obj = (IpLocation) list.get(i);

			StringTokenizer _token = new StringTokenizer(_obj.getIp(), ".");

			// segmen class A handler
			if (_token.countTokens() == 3) {
				if (_obj.getIp().equals(client3)) {
					return _obj.getLabel();
				}
			} else if (_token.countTokens() == 4) {
				if (_obj.getIp().equals(ipAddress)) {
					return _obj.getLabel();
				}
			}

		}

		throw new IpLocationNotFoundException(ipAddress);
	}
}
