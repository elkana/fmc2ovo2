package com.ppu.fmc.local.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ppu.fmc.local.domain.MacAddr;

public interface IMacAddrRepository extends JpaRepository<MacAddr, Long>{

	List<MacAddr> findByMacaddr(String macAddress);
	List<MacAddr> findByIpaddrhex(String ipaddrhex);
	List<MacAddr> findByIpaddr(String ipaddr);
}
