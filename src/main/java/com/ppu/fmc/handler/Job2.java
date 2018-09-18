package com.ppu.fmc.handler;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Job2 {
	static Logger log = LoggerFactory.getLogger(Job2.class);
	
	@Value("${fmc.fast:false}")
	private boolean fastMode;

	@Autowired
	@Qualifier("fmcEntityManagerFactory")
	EntityManager em;
	private void method1() {
		
	}
	
	public boolean execute() throws Exception {

		log.debug("Running {}", Job2.class.getSimpleName());

		//		log.debug("fmc.fetch.rows :: {}", fmcFetchRows);
//		log.debug("local.data.ipclientmap :: {}", ipclientmap);
//		log.debug("local.data.mac.keep.days :: {}", keepOldMacAddrDays);
		log.debug("fmc.fast :: {}", fastMode);
//		log.debug("lastSentFirstPacketSec :: {} -> {}", lastSentFirstPacketSec, Utils.converToLDT(lastSentFirstPacketSec));
		
		try {
			method1();
		} catch (Exception e) {
			throw e;
		}

		return true;
	}

}
