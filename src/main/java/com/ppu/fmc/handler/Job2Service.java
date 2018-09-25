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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.ppu.fmc.local.domain.MacAddr;
import com.ppu.fmc.local.domain.UrlLog;
import com.ppu.fmc.local.domain.UrlLogIdentity;
import com.ppu.fmc.local.repo.IUrlLogRepository;
import com.ppu.fmc.ovo.ws.OVOUrlService;
import com.ppu.fmc.util.CSVUtils;
import com.ppu.fmc.util.StringUtils;
import com.ppu.fmc.util.Utils;

@Service
public class Job2Service {
	static Logger log = LoggerFactory.getLogger(Job2Service.class);

	@Value("${fmc.fetch.rows:5}")
	private int fmcFetchRows;

	// 0 for unlimited
	@Value("${local.data.url.sent.max:0}")
	private int maxUrlSent;

	@Autowired
	@Qualifier("fmcEntityManagerFactory")
	EntityManager em;

	@Autowired
	OVOUrlService ovoUrlService;

	@Autowired
	IUrlLogRepository urlLogRepo;

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

		// TODO: masih kurang harusnya ada pengenal hostId/macaddress sejenisnya
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

			log.debug("searchAllUrlInFMCFor return {} rows", resultList.size());

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			log.error("Error when ran {} -> {}", sb.toString(), action);
		}

		return rows;
	}

	@Async("specificTaskExecutor")
	public CompletableFuture<Void> collectUrl(List ipLocations, MacAddr macAddr) throws InterruptedException,
			RestClientException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException {

		List<Object[]> items = searchAllUrlInFMCFor(macAddr, fmcFetchRows);

		if (maxUrlSent > 0) {
			while (items.size() > maxUrlSent) {
				items.remove(0);
			}
		}

		log.info("COLLECTING {} urls in connection_log for ip {}, mac {} since {}", items.size(), macAddr.getIpaddr(),
				macAddr.getMacaddr(), macAddr.getLastprocesseddate() == null ? macAddr.getCreateddate() : macAddr.getLastprocesseddate() );

		List<String> ipAddressesInHexa = new ArrayList<String>();

		// a. first, collect all ipaddress in hexa
		for (int i = 0; i < items.size(); i++) {
			Object[] fields = items.get(i);

			String ipAddressInHexa = String.valueOf(fields[2]);

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

		// dump into url_log
		for (int i = 0; i < items.size(); i++) {
			Object[] fields = items.get(i);

			long firstPacketSec = Long.parseLong(String.valueOf(fields[0]));
			String url = String.valueOf(fields[1]);
			String ipAddressInHexa = String.valueOf(fields[2]);
			String ipAddress = StringUtils.fixIPAddress(String.valueOf(fields[3]));

			String iplocation = "";

			try {
				iplocation = CSVUtils.getLocation(ipLocations, ipAddress);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

			try {
				// just dump it
				UrlLogIdentity uli = new UrlLogIdentity();
				uli.setFirstpacketsec(firstPacketSec);
				uli.setMacaddr(macAddr.getMacaddr());
				uli.setKeyurl(url.substring(0, Math.min(url.length(), 255)));// sengaja dipotong

				if (urlLogRepo.findOne(uli) == null) {
					UrlLog entity = new UrlLog();
					entity.setUrlLogIdentity(uli);
					entity.setUrl(url);
					entity.setIpaddr(ipAddress);
					entity.setLocation(iplocation);
					entity.setCreateddate(LocalDateTime.now());
					urlLogRepo.save(entity);
					log.debug("new row[{}/{}] = " + StringUtils.objectsToString(fields, ", "), (i + 1), items.size());
				}

			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

		}
		
//		if (anyDataToSentToOvo) {
//			action.setLastprocesseddate(LocalDateTime.now().minusMinutes(backProcessedDateMinutes));
//			macAddrRepo.save(action);
//		}

		return CompletableFuture.completedFuture(null);
	}

}
