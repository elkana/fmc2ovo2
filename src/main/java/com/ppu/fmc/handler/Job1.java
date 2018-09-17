package com.ppu.fmc.handler;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.ppu.fmc.local.domain.MacAddr;
import com.ppu.fmc.local.model.HostIPMap;
import com.ppu.fmc.local.model.HostMACMap;
import com.ppu.fmc.util.StringUtils;

@Component
public class Job1 {
	static Logger log = LoggerFactory.getLogger(Job1.class);

	@Autowired
	@Qualifier("fmcEntityManagerFactory")
	EntityManager em;

	private void method1() {
		// get all active host
		List<HostMACMap> macs = new ArrayList<>();

		Query q = em
				.createNativeQuery("SELECT hex(c.host_id), hex(c.mac_address), c.mac_vendor FROM rna_host_mac_map c");
		try {
			List resultList = q.getResultList();

			if (resultList.size() > 0) {
				for (int i = 0; i < resultList.size(); i++) {
					Object[] _fields = (Object[]) resultList.get(i);

					HostMACMap _obj = new HostMACMap();
					_obj.setHostidhex(String.valueOf(_fields[0]));
					_obj.setMacaddr(String.valueOf(_fields[1]));
					_obj.setMacvendor(String.valueOf(_fields[2]));

					macs.add(_obj);
				}
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		// get all related IPs
		List<HostIPMap> ips = new ArrayList<>();

		q = em.createNativeQuery("SELECT hex(b.host_id), hex(b.ipaddr), inet6_ntoa(b.ipaddr) FROM rna_host_ip_map b");
		try {
			List resultList = q.getResultList();

			if (resultList.size() > 0) {
				for (int i = 0; i < resultList.size(); i++) {
					Object[] _fields = (Object[]) resultList.get(i);

					HostIPMap _obj = new HostIPMap();
					_obj.setHostidhex(String.valueOf(_fields[0]));
					_obj.setIpaddrhex(String.valueOf(_fields[1]));
					_obj.setIpaddr(StringUtils.fixIPAddress(String.valueOf(_fields[2])));

					ips.add(_obj);

				}

			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		if (macs.size() < 1 || ips.size() < 1)
			return;

		List<MacAddr> maList = new ArrayList<>();
		
		for (HostMACMap hmm: macs) {
			for (HostIPMap him : ips) {
				if (hmm.getHostidhex().equals(him.getHostidhex())) {
					MacAddr ma = new MacAddr();
					ma.setIpaddr(him.getIpaddr());
					ma.setIpaddrhex(him.getIpaddrhex());
					ma.setMacaddr(hmm.getMacaddr());
//					ma.setLocation(location);
//					ma.setCreateddate(createddate);
					
					maList.add(ma);
					break;
				}
			}
		}
		
		log.info("Recent MacAddress: {} devices", maList.size());
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
