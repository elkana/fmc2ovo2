package com.ppu.fmc.local.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;

import com.ppu.fmc.util.LocalDateTimeAttributeConverter;

import lombok.Data;

@Data
@Entity
@Table(name = "macaddrurl")
public class MacAddrUrl {
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(length = 36)
	private String macaddr;
	
	@Column(length = 15)
	private String ipaddr;

	@Column(length = 200)
	private String iplocation;

	@Column(length = 4096)
	private String url;
	private Long firstpacketsec;
	private Long lastpacketsec;
	
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	private LocalDateTime sentdate;
	
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@CreatedDate
	private LocalDateTime createddate;

}
