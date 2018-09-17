package com.ppu.fmc.local.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.ppu.fmc.local.domain.MacAddrUrl;

public interface IMacAddrUrlRepository extends JpaRepository<MacAddrUrl, Long> {

	List<MacAddrUrl> findByUrlAndMacaddr(String url, String macAddr);
	List<MacAddrUrl> findByUrlAndIpaddr(String url, String ipAddr);
	List<MacAddrUrl> findByUrlAndIpaddrAndFirstpacketsec(String url, String ipAddr, long fps);
	
	@Query(value = "select * FROM macaddrurl c WHERE c.macaddr = ?1 and c.sentdate is null order by c.id limit ?2", nativeQuery=true)
	List<MacAddrUrl> findUnsentUrl(String macaddr, int limit);

	Page<MacAddrUrl> findByMacaddrAndSentdateIsNull(String macaddr, Pageable pageable);
//	Page<MacAddrUrl> findByMacaddrAndSentdateIsNullOrderById(String macaddr, Pageable pageable);
	
	List<MacAddrUrl> findBySentdateIsNotNullAndFirstpacketsecIsLessThanEqual(long fps);
	
	@Transactional
	@Modifying
	Long deleteBySentdateIsNotNullAndFirstpacketsecIsLessThanEqual(long fps);

}
