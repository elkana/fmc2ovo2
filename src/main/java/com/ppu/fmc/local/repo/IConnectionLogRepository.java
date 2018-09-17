package com.ppu.fmc.local.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ppu.fmc.local.domain.ConnectionLog;

public interface IConnectionLogRepository extends JpaRepository<ConnectionLog, Long>{

	List<ConnectionLog> findByUrlAndFirstpacketsecAndIpaddrhex(String url, long fps, String ipAddrHex);
	
	
	@Query(value = "FROM ConnectionLog c WHERE c.firstpacketsec >= ?1 and c.ipaddrhex = ?2 and c.sentdate is null")
	Page<ConnectionLog> findByFps(long minFps, String ipAddrHex, Pageable pageable);
}
