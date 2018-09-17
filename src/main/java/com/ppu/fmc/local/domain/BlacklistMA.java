package com.ppu.fmc.local.domain;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;

import com.ppu.fmc.util.LocalDateTimeAttributeConverter;

@Entity
@Table(name = "blacklistma")
public class BlacklistMA {
	
	@Id
	@Column(length = 36)
	private String macaddress;
	
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@CreatedDate
	private LocalDateTime createddate;

	
	public String getMacaddress() {
		return macaddress;
	}

	public void setMacaddress(String macaddress) {
		this.macaddress = macaddress;
	}

	public LocalDateTime getCreateddate() {
		return createddate;
	}

	public void setCreateddate(LocalDateTime createddate) {
		this.createddate = createddate;
	}

	@Override
	public String toString() {
		return "BlacklistMA [macaddress=" + macaddress + ", createddate=" + createddate + "]";
	}
	
	
}
