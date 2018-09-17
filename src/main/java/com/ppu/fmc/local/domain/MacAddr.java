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
import org.springframework.data.annotation.LastModifiedDate;

import com.ppu.fmc.util.LocalDateTimeAttributeConverter;

import lombok.Data;

@Data
@Entity
@Table(name = "macaddr")
public class MacAddr {
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(length = 36)
	private String macaddr;
	
	@Column(length = 40)
	private String ipaddrhex;
	
	@Column(length = 15)
	private String ipaddr;
	
	@Column(length = 200)
	private String location;
	
	private Long deltasec;
	
	private Long lowestsec;
	
	/**
	 * supaya ga perlu ngirim semuanya, akan diupdate setelah dikirim ke ovo ?
	 */
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	private LocalDateTime lastprocesseddate;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@LastModifiedDate
	private LocalDateTime updateddate;
	
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@CreatedDate
	private LocalDateTime createddate;

}
