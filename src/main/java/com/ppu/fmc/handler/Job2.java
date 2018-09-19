package com.ppu.fmc.handler;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import com.ppu.fmc.Fmc2ovo2Application;
import com.ppu.fmc.local.domain.MacAddr;
import com.ppu.fmc.local.domain.MacAddrUrl;
import com.ppu.fmc.local.repo.IBlacklistIpRepository;
import com.ppu.fmc.local.repo.IBlacklistMARepository;
import com.ppu.fmc.local.repo.IMacAddrRepository;
import com.ppu.fmc.local.repo.IMacAddrUrlRepository;
import com.ppu.fmc.ovo.model.RequestClient;
import com.ppu.fmc.ovo.model.ResponseMsg;
import com.ppu.fmc.ovo.ws.OVOUrlService;
import com.ppu.fmc.util.CSVUtils;
import com.ppu.fmc.util.StopWatch;
import com.ppu.fmc.util.StringUtils;
import com.ppu.fmc.util.Utils;

@Component
public class Job2 {
	static Logger log = LoggerFactory.getLogger(Job2.class);

	@Value("${fmc.fetch.rows:5}")
	private int fmcFetchRows;

	@Value("${ovo.ws.batch.rows:20}")
	private int ovoBatchRows;

	@Value("${local.data.ipclientmap}")
	private String ipclientmap;

	@Value("${fmc.job2.fast:false}")
	private boolean fastMode;

	@Value("${local.procbackdate.minutes:10}")
	private int backProcessedDateMinutes;

	@Value("${ovo.ws.url}")
	private String urlOVO;
	
	private List ipLocations;

	@Autowired
	@Qualifier("fmcEntityManagerFactory")
	EntityManager em;

	@Autowired
	IMacAddrRepository macAddrRepo;

	@Autowired
	IMacAddrUrlRepository macAddrUrlRepo;

	@Autowired
	IBlacklistIpRepository blacklistIpRepo;

	@Autowired
	IBlacklistMARepository blacklistMARepo;

	@Autowired
	OVOUrlService ovoUrlService;

	@Autowired
	Job2Service job2Service;

	private void methodSlow() throws RestClientException, KeyManagementException, NoSuchAlgorithmException,
			KeyStoreException, InterruptedException, IOException {

		// 1. cek semua macaddress di table MacAddr
		List<MacAddr> allMac = macAddrRepo.findAll();

		log.info("there are {} macaddresses", allMac.size());

		for (MacAddr action : allMac) {
			// blacklist check
			if (blacklistIpRepo.findOne(action.getIpaddr()) != null) {
				log.warn("BLACKLISTed IP {}", action.getIpaddr());
				continue;
			}

			if (blacklistMARepo.findOne(action.getMacaddr()) != null) {
				log.warn("BLACKLISTed MacAddress {}", action.getMacaddr());
				continue;
			}

			// sometimes krn prosesnya lama, bisa2 next macaddr udah ga terdaftar. so need
			// to check again
			if (macAddrRepo.findByMacaddr(action.getMacaddr()).isEmpty()) {
				log.warn("Missing Mac Address {}", action.getMacaddr());
				continue;
			}

			List<Object[]> items = searchAllUrlInFMCFor(action, fmcFetchRows);

			log.info("There are {} urls in connection_log for ip {}, mac {} since {}", items.size(), action.getIpaddr(),
					action.getMacaddr(), action.getLastprocesseddate());

			List<String> ipAddressesInHexa = new ArrayList<String>();

			// a. first, collect all ipaddress in hexa
			for (int i = 0; i < items.size(); i++) {
				Object[] fields = items.get(i);

				long firstPacketSec = Long.parseLong(String.valueOf(fields[0]));
				String url = String.valueOf(fields[1]);
				String ipAddressInHexa = String.valueOf(fields[2]);
				String ipAddress = StringUtils.fixIPAddress(String.valueOf(fields[3]));

				boolean found = false;

				for (String _s : ipAddressesInHexa) {
					if (_s.equals(ipAddressInHexa)) {
						found = true;
						break;
					}
				}

				if (!found)
					ipAddressesInHexa.add(ipAddressInHexa);
			}

			// dump into macaddrurl
			for (int i = 0; i < items.size(); i++) {
				Object[] fields = items.get(i);

				long firstPacketSec = Long.parseLong(String.valueOf(fields[0]));
				String url = String.valueOf(fields[1]);
				String ipAddressInHexa = String.valueOf(fields[2]);
				String ipAddress = StringUtils.fixIPAddress(String.valueOf(fields[3]));

//							String macAddress 		= "";
				String iplocation = "";

				try {
					iplocation = CSVUtils.getLocation(ipLocations, ipAddress);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}

				MacAddrUrl mau;

//							List<MacAddrUrl> findByUrl = macAddrUrlRepo.findByUrlAndIpaddr(url, ipAddress);
				List<MacAddrUrl> findByUrl = macAddrUrlRepo.findByUrlAndIpaddrAndFirstpacketsec(url, ipAddress,
						firstPacketSec);

				if (findByUrl.isEmpty()) {
					log.debug("new row[{}/{}] = " + StringUtils.objectsToString(fields, ", "), (i + 1), items.size());

					mau = new MacAddrUrl();
					mau.setCreateddate(LocalDateTime.now());
					mau.setFirstpacketsec(firstPacketSec);
//								mau.setLastpacketsec(lastpacketsec);
					mau.setMacaddr(action.getMacaddr());
					mau.setIpaddr(ipAddress);
					mau.setIplocation(iplocation);
					mau.setUrl(url);

					macAddrUrlRepo.save(mau);

				} else {
//								log.debug("exist row[{}/{}] = " + StringUtils.objectsToString(fields, ", "), (i + 1), items.size());

					mau = findByUrl.get(0);

					if (mau.getFirstpacketsec() < firstPacketSec) {
						mau.setFirstpacketsec(firstPacketSec);
						mau.setSentdate(null); // reset

						macAddrUrlRepo.save(mau);
					}

				}
			} // for (int i=

			boolean anyDataToSentToOvo = false;

			while (true) {
				List<MacAddrUrl> findUnsentData = macAddrUrlRepo.findUnsentUrl(action.getMacaddr(), ovoBatchRows);

				if (findUnsentData.isEmpty())
					break;

				log.debug("Sending {} data to {}", findUnsentData.size(), urlOVO);
				CompletableFuture<?>[] array = new CompletableFuture<?>[findUnsentData.size()];

				for (int j = 0; j < findUnsentData.size(); j++) {
					MacAddrUrl _obj = findUnsentData.get(j);

					RequestClient req = new RequestClient();
					req.setIpaddr(_obj.getIpaddr());
					req.setIplocation(_obj.getIplocation());
					req.setMacaddr(_obj.getMacaddr());
					req.setUrl(_obj.getUrl());
					req.setTime(_obj.getFirstpacketsec());

					req.setEventId(Fmc2ovo2Application.eventIdIncrementer);

					Fmc2ovo2Application.eventIdIncrementer += 1;

					array[j] = ovoUrlService.sendData(req);

				}

				anyDataToSentToOvo = true;

				// Wait until they are all done
				CompletableFuture.allOf(array).join();

				findUnsentData.forEach(_content -> {
					_content.setSentdate(LocalDateTime.now());
					macAddrUrlRepo.save(_content);
				});

			}

			// TODO update last dikurangi 10 menit
			if (anyDataToSentToOvo) {
				action.setLastprocesseddate(LocalDateTime.now().minusMinutes(backProcessedDateMinutes));
				macAddrRepo.save(action);
			}
		} // for (MacAddr a

	}

	private void methodFast() throws RestClientException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, InterruptedException, IOException {

		// 1. cek semua macaddress di table MacAddr
		List<MacAddr> allMac = macAddrRepo.findAll();

		List<MacAddr> cleanMac = new ArrayList<>();

		for (MacAddr action : allMac) {
			// blacklist check
			if (blacklistIpRepo.findOne(action.getIpaddr()) != null) {
				log.warn("BLACKLISTed IP {}", action.getIpaddr());
				continue;
			}

			if (blacklistMARepo.findOne(action.getMacaddr()) != null) {
				log.warn("BLACKLISTed MacAddress {}", action.getMacaddr());
				continue;
			}

			cleanMac.add(action);

		}

		log.info("PROCESSING {} macaddresses among {}", cleanMac.size(), allMac.size());

		StopWatch sw = StopWatch.AutoStart();

		CompletableFuture<?>[] array = new CompletableFuture<?>[cleanMac.size()];

		for (int i = 0; i < cleanMac.size(); i++) {

			array[i] = job2Service.collectUrl(ipLocations, cleanMac.get(i));
		}

		// Wait until they are all done
		CompletableFuture.allOf(array).join();
		
		log.info("Elapsed Fast Method {}", sw.stopAndGetAsString());

	}

	private List<Object[]> searchAllUrlInFMCFor(MacAddr action, int rowCount) {
		List<Object[]> rows = new ArrayList<>();

		if (StringUtils.isEmpty(action.getIpaddrhex()))
			return rows;

//		select a.url, a.first_packet_sec, a.last_packet_sec,hex(a.initiator_ipaddr) from connection_log a where a.url like 'https://kumparan%' order by a.first_packet_sec desc limit 10;

		StringBuffer sb = new StringBuffer(
				"SELECT a.first_packet_sec, a.url, hex(a.initiator_ipaddr), inet6_ntoa(a.initiator_ipaddr) FROM connection_log a");
		sb.append(" WHERE a.url <> ''");
		// sb.append(" WHERE char_length(a.url) <> 0");

		// tinggal diambil dari createddatenya
		sb.append(" AND a.first_packet_sec >= ");

		if (action.getLastprocesseddate() == null)
			sb.append(Utils.convertToSeconds(action.getCreateddate()));
		else
			sb.append(Utils.convertToSeconds(action.getLastprocesseddate()));

//		sb.append(" AND a.first_packet_sec >= ").append(action.getCreateddate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000);
		// sb.append(" AND a.url <> '").append(fromLastUrl).append("'");

		sb.append(" AND hex(a.initiator_ipaddr) = '").append(action.getIpaddrhex()).append("'");

		// sb.append(" ORDER BY a.first_packet_sec LIMIT ").append(rowCount);
		sb.append(" ORDER BY a.first_packet_sec ASC");
		if (rowCount > 0)
			sb.append(" LIMIT ").append(rowCount);

		Query q = em.createNativeQuery(sb.toString());
//		"select a.first_packet_sec, a.url, hex(a.initiator_ipaddr) from connection_log a where char_length(a.url) <> 0 and a.first_packet_sec > 1531987347 and a.url <> 'http://batsavcdn.ksmobile.net/bsi' order by a.first_packet_sec limit 5";

		try {
			List resultList = q.getResultList();

			if (resultList.size() < 1)
				return rows;

//		Object[] author = (Object[]) q.getSingleResult();

			rows.addAll(resultList);

			log.debug("findNextConnectionLogs return {} rows", resultList.size());

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			log.error("Error when ran {} -> {}", sb.toString(), action);
		}

		return rows;
	}

	public boolean execute() throws Exception {

		log.debug("Running {}", Job2.class.getSimpleName());

		// log.debug("fmc.fetch.rows :: {}", fmcFetchRows);
//		log.debug("local.data.ipclientmap :: {}", ipclientmap);
//		log.debug("local.data.mac.keep.days :: {}", keepOldMacAddrDays);
		log.debug("fmc.job2.fast :: {}", fastMode);
//		log.debug("lastSentFirstPacketSec :: {} -> {}", lastSentFirstPacketSec, Utils.converToLDT(lastSentFirstPacketSec));

		
		try {
			ipLocations = CSVUtils.loadIpLocationCsv(ipclientmap);
		} catch (IOException e1) {
			e1.printStackTrace();
			
			return false;
		}
		
		try {
			if (fastMode)
				methodFast();
			else
				methodSlow();
		} catch (Exception e) {
			throw e;
		}

		return true;
	}

}
