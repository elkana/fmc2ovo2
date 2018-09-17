package com.ppu.fmc.local.domain;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;

import com.ppu.fmc.util.LocalDateTimeAttributeConverter;

import lombok.Data;

@Data
@Entity
@Table(name = "blacklistma")
public class BlacklistMA {
	
	@Id
	@Column(length = 36)
	private String macaddress;
	
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@CreatedDate
	private LocalDateTime createddate;
	
}
