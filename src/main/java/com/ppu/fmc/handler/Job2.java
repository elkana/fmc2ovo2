package com.ppu.fmc.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Job2 {
	static Logger log = LoggerFactory.getLogger(Job2.class);
	
	public boolean execute() throws Exception {

		log.debug("Hello Job#2");

		//		log.debug("fmc.fetch.rows :: {}", fmcFetchRows);
//		log.debug("local.data.ipclientmap :: {}", ipclientmap);
//		log.debug("local.data.mac.keep.days :: {}", keepOldMacAddrDays);
//		log.debug("local.data.mac.duplicate.keep :: {}", keepDuplicateMacAddr);
//		log.debug("lastSentFirstPacketSec :: {} -> {}", lastSentFirstPacketSec, Utils.converToLDT(lastSentFirstPacketSec));
		
		try {
//			method6();
		} catch (Exception e) {
			throw e;
		}

		return true;
	}

}
