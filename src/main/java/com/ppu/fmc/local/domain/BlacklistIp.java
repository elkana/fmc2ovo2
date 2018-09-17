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
@Table(name = "blacklistip")
public class BlacklistIp {
	
	@Id
	@Column(length = 15)
	private String ip;
	
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@CreatedDate
	private LocalDateTime createddate;
	
}
