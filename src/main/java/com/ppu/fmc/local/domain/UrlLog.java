package com.ppu.fmc.local.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;

import com.ppu.fmc.util.LocalDateTimeAttributeConverter;

@Entity
@Table(name = "url_log")
public class UrlLog {
	
	@EmbeddedId
    private UrlLogIdentity urlLogIdentity;
	
	@Column(length = 15)
	private String ipaddr;

	@Column(length = 200)
	private String location;

	@Convert(converter = LocalDateTimeAttributeConverter.class)
	private LocalDateTime sentdate;
	
	private Long sentId;
	
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	@CreatedDate
	private LocalDateTime createddate;

	public UrlLogIdentity getUrlLogIdentity() {
		return urlLogIdentity;
	}

	public void setUrlLogIdentity(UrlLogIdentity urlLogIdentity) {
		this.urlLogIdentity = urlLogIdentity;
	}

	public String getIpaddr() {
		return ipaddr;
	}

	public void setIpaddr(String ipaddr) {
		this.ipaddr = ipaddr;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public LocalDateTime getSentdate() {
		return sentdate;
	}

	public void setSentdate(LocalDateTime sentdate) {
		this.sentdate = sentdate;
	}

	public Long getSentId() {
		return sentId;
	}

	public void setSentId(Long sentId) {
		this.sentId = sentId;
	}

	public LocalDateTime getCreateddate() {
		return createddate;
	}

	public void setCreateddate(LocalDateTime createddate) {
		this.createddate = createddate;
	}

	@Override
	public String toString() {
		return "UrlLog [urlLogIdentity=" + urlLogIdentity + ", ipaddr=" + ipaddr + ", location=" + location
				+ ", sentdate=" + sentdate + ", sentId=" + sentId + ", createddate=" + createddate + "]";
	}
	
	
}
