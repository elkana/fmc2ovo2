package com.ppu.fmc.ovo.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestClient implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long eventId;
	private String ipaddr;
	private String iplocation;
	private String macaddr;
	private String url;
	private long time;
	
	public RequestClient() {
	}
	
	public long getEventId() {
		return eventId;
	}
	public void setEventId(long eventId) {
		this.eventId = eventId;
	}
	public String getIpaddr() {
		return ipaddr;
	}
	public void setIpaddr(String ipaddr) {
		this.ipaddr = ipaddr;
	}
	
	public String getIplocation() {
		return iplocation;
	}
	public void setIplocation(String iplocation) {
		this.iplocation = iplocation;
	}
	public String getMacaddr() {
		return macaddr;
	}
	public void setMacaddr(String macaddr) {
		this.macaddr = macaddr;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	
	@Override
	public String toString() {
		return "RequestClient [eventId=" + eventId + ", ipaddr=" + ipaddr + ", iplocation=" + iplocation + ", macaddr="
				+ macaddr + ", url=" + url + ", time=" + time + "]";
	}
	
	
}
