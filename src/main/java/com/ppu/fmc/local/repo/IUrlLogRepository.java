package com.ppu.fmc.local.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ppu.fmc.local.domain.UrlLog;
import com.ppu.fmc.local.domain.UrlLogIdentity;

public interface IUrlLogRepository extends JpaRepository<UrlLog, UrlLogIdentity>{

}
