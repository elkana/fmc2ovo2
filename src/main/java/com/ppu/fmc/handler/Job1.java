package com.ppu.fmc.handler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ppu.fmc.exception.IpLocationNotFoundException;
import com.ppu.fmc.local.domain.MacAddr;
import com.ppu.fmc.local.model.HostIPMap;
import com.ppu.fmc.local.model.HostMACMap;
import com.ppu.fmc.local.repo.IMacAddrRepository;
import com.ppu.fmc.util.CSVUtils;
import com.ppu.fmc.util.StringUtils;
import com.ppu.fmc.util.Utils;

@Component
public class Job1 {
	static Logger log = LoggerFactory.getLogger(Job1.class);

	@Value("${local.data.ipclientmap}")
	private String ipclientmap;

	@Autowired
	@Qualifier("fmcEntityManagerFactory")
	EntityManager em;

	@Autowired
	IMacAddrRepository macAddrRepo;

	private void method1() {
		// get all active host
		List<HostMACMap> macs = new ArrayList<>();

		Query q = em
				.createNativeQuery("SELECT hex(c.host_id), hex(c.mac_address), c.mac_vendor FROM rna_host_mac_map c");
		try {
			List resultList = q.getResultList();

			for (int i = 0; i < resultList.size(); i++) {
				Object[] _fields = (Object[]) resultList.get(i);

				HostMACMap _obj = new HostMACMap();

				_obj.setHostidhex(String.valueOf(_fields[0]));
				_obj.setMacaddr(Utils.convertHexToMacAddress(String.valueOf(_fields[1])));
				_obj.setMacvendor(String.valueOf(_fields[2]));

				macs.add(_obj);
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		if (macs.size() < 1)
			return;

		List listIpLocation = null;
		try {
			listIpLocation = CSVUtils.loadIpLocationCsv(ipclientmap);
		} catch (IOException e1) {
			// TO DO Auto-generated catch block
			e1.printStackTrace();
		}

		// get all related IPs
		List<HostIPMap> ips = new ArrayList<>();

		q = em.createNativeQuery("SELECT hex(b.host_id), hex(b.ipaddr), inet6_ntoa(b.ipaddr) FROM rna_host_ip_map b");
		try {
			List resultList = q.getResultList();

			for (int i = 0; i < resultList.size(); i++) {

				Object[] _fields = (Object[]) resultList.get(i);

				HostIPMap _obj = new HostIPMap();

				_obj.setHostidhex(String.valueOf(_fields[0]));
				_obj.setIpaddrhex(String.valueOf(_fields[1]));
				_obj.setIpaddr(StringUtils.fixIPAddress(String.valueOf(_fields[2])));

				try {
					_obj.setIplocation(CSVUtils.getLocation(listIpLocation, _obj.getIpaddr()));
				} catch (IpLocationNotFoundException e) {
					// sementara kalo ga terdaftar lokasinya ga bisa diproses
					log.warn(e.getMessage());
					continue;

				} catch (Exception e) {
					log.error(e.getMessage(), e);
					continue;
				}

				ips.add(_obj);

			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		if (ips.size() < 1)
			return;

		List<MacAddr> maActiveDevices = new ArrayList<>();

		for (HostMACMap hmm : macs) {
			for (HostIPMap him : ips) {
				if (hmm.getHostidhex().equals(him.getHostidhex())) {

					// sementara kalo ga terdaftar lokasinya ga bisa diproses
					if (him.getIplocation() == null)
						continue;

					MacAddr ma = new MacAddr();

					ma.setIpaddr(him.getIpaddr());
					ma.setIpaddrhex(him.getIpaddrhex());
					ma.setMacaddr(hmm.getMacaddr());
					ma.setLocation(him.getIplocation());
					ma.setCreateddate(LocalDateTime.now());

					maActiveDevices.add(ma);

					break;
				}
			}
		}

		List<MacAddr> getAllLocalMac = macAddrRepo.findAll();

		log.info("Active MacAddress: {} devices, {} in local", maActiveDevices.size(), getAllLocalMac.size());

		int updateCounter = 0;
		int deleteCounter = 0;
		for (MacAddr localMac : getAllLocalMac) {

			// kalo masih ada update IPnya, kalo ga ada di delete saja
			MacAddr _active = null;
			int _row = -1;
			
			for (int i = 0; i < maActiveDevices.size(); i++) {
				MacAddr mrl = maActiveDevices.get(i);
				
				if (localMac.getMacaddr().equals(mrl.getMacaddr())) {
					_active = mrl;
					_row = i;
					break;
				}
			}
			
			if (_active != null) {

				// perbarui IPnya
				if (!_active.getIpaddrhex().equals(localMac.getIpaddrhex())) {
					
					String _previousIP = localMac.getIpaddr();
					
					localMac.setIpaddr(_active.getIpaddr());
					localMac.setIpaddrhex(_active.getIpaddrhex());
					localMac.setUpdateddate(LocalDateTime.now());

					updateCounter += 1;
					macAddrRepo.save(localMac);
					
					log.warn("UPDATED MacAddress {} {}. Before was {}", localMac.getMacaddr(), localMac.getIpaddr(), _previousIP);
				}
				
			} else {

				// karena ga aktif maka dihapus dr local
				deleteCounter += 1;
				macAddrRepo.delete(localMac);
				log.warn("DELETED MacAddress {} {}", localMac.getMacaddr(), localMac.getIpaddr());
			}
			
			// should remove from recentList, any residue will be inserted
			maActiveDevices.remove(_row);
		}
		
		for (MacAddr ma : maActiveDevices) {
			macAddrRepo.save(ma);
			log.warn("NEW MacAddress {} {}", ma.getMacaddr(), ma.getIpaddr());
		}

		log.info("{} Refreshed local MacAddress, {} deleted - {} updated - {} inserted", macAddrRepo.findAll().size(), deleteCounter, updateCounter, maActiveDevices.size());

	}

	public boolean execute() throws Exception {

		log.debug("Running {}", Job1.class.getSimpleName());

//		log.debug("fmc.fetch.rows :: {}", fmcFetchRows);
		log.debug("local.data.ipclientmap :: {}", ipclientmap);
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
