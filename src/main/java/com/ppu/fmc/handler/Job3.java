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

import com.ppu.fmc.local.domain.ConnectionLog;
import com.ppu.fmc.local.repo.IConnectionLogRepository;
import com.ppu.fmc.local.repo.IMacAddrRepository;
import com.ppu.fmc.util.CSVUtils;
import com.ppu.fmc.util.StopWatch;
import com.ppu.fmc.util.StringUtils;
import com.ppu.fmc.util.Utils;

/**
 * Job ini akan mendump data terbaru yg masuk terlebih dulu dari table
 * connection_log
 * 
 * Fyi, ada data cepat dan data lambat yg masuk ke table connection_log. dengan
 * menggunakan teknik pencatatan firstpacketsec dapat diketahui data yang baru
 * masuk milik IP mana.
 * 
 * This will result faster respon from OVO
 * 
 * @author perkasa
 *
 */
@Component
public class Job3 {
	static Logger log = LoggerFactory.getLogger(Job3.class);
	static long lastSentFirstPacketSec = 0;

	// 0 is unlimited
	@Value("${fmc.fetch.rows:0}")
	private int fmcFetchRows;

	@Value("${local.data.keep.days:4}")
	private int keepDataDays;

	@Value("${local.data.ipclientmap}")
	private String ipclientmap;

	@Autowired
	@Qualifier("fmcEntityManagerFactory")
	EntityManager em;

	@Autowired
	IConnectionLogRepository connLogRepo;

	@Autowired
	IMacAddrRepository macAddrRepo;

	private void method1() {

		cleanUpOldData();

		List listIpLocation = null;
		try {
			listIpLocation = CSVUtils.loadIpLocationCsv(ipclientmap);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		List<Object[]> connLogRows = findLastConnectionLogItem(lastSentFirstPacketSec);

		while (true) {
			log.debug("--> findLastConnectionLogItem = " + (connLogRows.size() < 1 ? "0"
					: connLogRows.size() + " rows --> " + StringUtils.objectsToString(connLogRows.get(0), ", ")));

			if (connLogRows.size() < 1)
				break;

			// final construct
			StopWatch sw = StopWatch.AutoStart();

			for (int i = 0; i < connLogRows.size(); i++) {
				Object[] fields = connLogRows.get(i);

				log.debug("row[{}/{}, fps>{}({})] = " + StringUtils.objectsToString(fields, ", "), (i + 1),
						connLogRows.size(), lastSentFirstPacketSec, Utils.converToLDT(lastSentFirstPacketSec));

				long firstPacketSec = Long.parseLong(String.valueOf(fields[0]));
				lastSentFirstPacketSec = firstPacketSec;

				String url = String.valueOf(fields[1]);
				String ipAddressInHexa = String.valueOf(fields[2]);
				String ipAddress = StringUtils.fixIPAddress(String.valueOf(fields[3]));
				String iplocation = "";
				try {
					iplocation = CSVUtils.getLocation(listIpLocation, ipAddress);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}

				// find macaddress
				String _hostIdInHexa = findHostIdByIP(ipAddressInHexa);
				String macAddress = Utils.convertHexToMacAddress(findMacAddressInHexaByHostId(_hostIdInHexa));

				// cari dulu di local,tp hanya yg ada macaddress yg akan disimpan
				if (StringUtils.isEmpty(macAddress)) {
					log.error("SKIPPED null macaddress for IP {}", ipAddress);
					continue;
				}

				List<ConnectionLog> _existingConnLog = connLogRepo.findByUrlAndFirstpacketsecAndIpaddrhex(url,
						firstPacketSec, ipAddressInHexa);

				if (_existingConnLog.isEmpty()) {
					ConnectionLog _connLog = new ConnectionLog();
					_connLog.setFirstpacketsec(firstPacketSec);
					_connLog.setCreateddate(LocalDateTime.now());
					_connLog.setIpaddr(ipAddress);
					_connLog.setIpaddrhex(ipAddressInHexa);
					_connLog.setIplocation(iplocation);
//					_connLog.setLastpacketsec(lastpacketsec);
					_connLog.setMacaddr(macAddress);
					_connLog.setUrl(url);
					_connLog.setFpsdate(Utils.converToLDT(firstPacketSec));

					connLogRepo.save(_connLog);
				}

			}

			log.info("Elapsed time: {}", sw.stopAndGetAsString());

			connLogRows = findNextConnectionLogs(lastSentFirstPacketSec, fmcFetchRows);

		}
	}

	private String findMacAddressInHexaByHostId(String hostIdInHexa) {
		StringBuffer sb = new StringBuffer("SELECT hex(c.host_id), hex(c.mac_address) FROM rna_host_mac_map c");
		sb.append(" WHERE hex(c.host_id) = '").append(hostIdInHexa).append("'");

		Query q = em.createNativeQuery(sb.toString());

		try {
			List resultList = q.getResultList();

			if (resultList.size() < 1)
				return null;

			for (int i = 0; i < resultList.size(); i++) {
				Object[] _fields = (Object[]) resultList.get(i);

//				HostMacMap _obj = new HostMacMap();
//				_obj.setHostIdInHexa(String.valueOf(_fields[0]));
//				_obj.setMacAddressInHexa(String.valueOf(_fields[1]));

				return String.valueOf(_fields[1]);
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}

	private String findHostIdByIP(String ipAddressInHexa) {

		StringBuffer sb = new StringBuffer("SELECT hex(b.host_id), hex(b.ipaddr) FROM rna_host_ip_map b");
		sb.append(" WHERE hex(b.ipaddr) = '").append(ipAddressInHexa).append("'");

		Query q = em.createNativeQuery(sb.toString());

		try {
			List resultList = q.getResultList();

			if (resultList.size() < 1)
				return null;

			for (int i = 0; i < resultList.size(); i++) {
				Object[] _fields = (Object[]) resultList.get(i);

//				HostIpMap _obj = new HostIpMap();
//				_obj.setHostIdInHexa(String.valueOf(_fields[0]));
//				_obj.setIpAddressInHexa(String.valueOf(_fields[1]));

				return String.valueOf(_fields[0]);
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}

	private void cleanUpOldData() {
		// mundur
		long deletePeriodInseconds = Utils.convertToSeconds(LocalDateTime.now().minusDays(keepDataDays));

		List<ConnectionLog> findOldData = connLogRepo.findByFirstpacketsecIsLessThanEqual(deletePeriodInseconds);

		if (findOldData.size() > 0) {
			log.warn("There are {}/{} data in table fmc.connection_log are {} days expired. preview[0]={}",
					findOldData.size(), connLogRepo.findAll().size(), keepDataDays, findOldData.get(0));
			Long deletedRows = connLogRepo.deleteByFirstpacketsecIsLessThanEqual(deletePeriodInseconds);
//			macAddrUrlRepo.deleteInBatch(findOldData);	
			log.warn("{} rows Cleanup from table fmc.connection_log", deletedRows);
		}

	}

	private List<Object[]> findLastConnectionLogItem(long lastFirstPacketId) {

		List<Object[]> rows = new ArrayList<>();

		StringBuffer sb = new StringBuffer(
				"select a.first_packet_sec, a.url, hex(a.initiator_ipaddr), inet6_ntoa(a.initiator_ipaddr) from connection_log a");
		sb.append(" where a.url <> ''");

		if (lastFirstPacketId > 0)
			sb.append(" and a.first_packet_sec = ").append(lastFirstPacketId);

		sb.append(" order by a.first_packet_sec DESC limit 1");

		// Query q = em.createNativeQuery("select a.first_packet_sec, a.url,
		// hex(a.initiator_ipaddr), inet6_ntoa(a.initiator_ipaddr) from connection_log a
		// where char_length(a.url) <> 0 order by a.first_packet_sec DESC limit 1");
		Query q = em.createNativeQuery(sb.toString());

		try {
			List resultList = q.getResultList();

			if (resultList.size() < 1)
				return rows;

//		Object[] author = (Object[]) q.getSingleResult();

			rows.addAll(resultList);

			log.debug("findLastConnectionLogItem return {} rows", resultList.size());

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return rows;

	}

	private List<Object[]> findNextConnectionLogs(long fromLastFirstPacketSec, int rowCount) {

		List<Object[]> rows = new ArrayList<>();

		StringBuffer sb = new StringBuffer(
				"SELECT a.first_packet_sec, a.url, hex(a.initiator_ipaddr), inet6_ntoa(a.initiator_ipaddr) FROM connection_log a");
		sb.append(" WHERE a.url <> ''");
		// sb.append(" WHERE char_length(a.url) <> 0");
		sb.append(" AND a.first_packet_sec > ").append(fromLastFirstPacketSec);
		// sb.append(" AND a.url <> '").append(fromLastUrl).append("'");
		// sb.append(" AND hex(a.initiator_ipaddr) <>
		// '").append(fromLastIpAddrInHexa).append("'");
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
		}

		return rows;

	}

	public boolean execute() throws Exception {

		log.debug("Running {}", Job3.class.getSimpleName());

		log.debug("fmc.fetch.rows :: {}", fmcFetchRows);
		log.debug("local.data.ipclientmap :: {}", ipclientmap);
		log.debug("local.data.keep.days :: {}", keepDataDays);
//		log.debug("local.data.mac.duplicate.keep :: {}", keepDuplicateMacAddr);
		log.debug("lastSentFirstPacketSec :: {} -> {}", lastSentFirstPacketSec,
				Utils.converToLDT(lastSentFirstPacketSec));

		try {
			method1();
		} catch (Exception e) {
			throw e;
		}

		return true;
	}

}
