package com.ppu.fmc.handler;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class Job1 {
	static Logger log = LoggerFactory.getLogger(Job1.class);
	
	@Autowired
	@Qualifier("fmcEntityManagerFactory")
	EntityManager em;
	
	private void method1() {
		StringBuffer sb = new StringBuffer("SELECT hex(c.host_id), hex(c.mac_address), c.mac_vendor, hex(b.ipaddr), inet6_ntoa(b.ipaddr) FROM rna_host_mac_map c INNER JOIN rna_host_ip_map b ON hex(c.host_id)=hex(b.host_id)");
		Query q = em.createNativeQuery(sb.toString());
		try {
			List resultList = q.getResultList();

			if (resultList.size() < 1)
				return;

			for (int i = 0; i < resultList.size(); i++) {
				Object[] _fields = (Object[]) resultList.get(i);

				log.debug((String) _fields[0]);
//				HostIPMap _obj = new HostIPMap();
//				_obj.setHostidhex(String.valueOf(_fields[0]));
//				_obj.setIpaddrhex(String.valueOf(_fields[1]));
//				_obj.setIpaddr(StringUtils.fixIPAddress(String.valueOf(_fields[2])));
//				_obj.setCreateddate(LocalDateTime.now());


			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public boolean execute() throws Exception {

		log.debug("Hello Job#1");

//		log.debug("fmc.fetch.rows :: {}", fmcFetchRows);
//		log.debug("local.data.ipclientmap :: {}", ipclientmap);
//		log.debug("local.data.mac.keep.days :: {}", keepOldMacAddrDays);
//		log.debug("local.data.mac.duplicate.keep :: {}", keepDuplicateMacAddr);
//		log.debug("lastSentFirstPacketSec :: {} -> {}", lastSentFirstPacketSec, Utils.converToLDT(lastSentFirstPacketSec));
		
		try {
			method1();
		} catch (Exception e) {
			throw e;
		}

		return true;
	}

}
