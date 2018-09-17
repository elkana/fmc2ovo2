package com.ppu.fmc.local.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ppu.fmc.local.domain.BlacklistIp;

public interface IBlacklistIpRepository extends JpaRepository<BlacklistIp, String>{

}
