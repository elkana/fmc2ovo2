package com.ppu.fmc.local.model;

public class HostIPMap {

	private String hostidhex;

	private String ipaddrhex;
	
	private String ipaddr;
	
	private String iplocation;
	
	public String getHostidhex() {
		return hostidhex;
	}

	public void setHostidhex(String hostidhex) {
		this.hostidhex = hostidhex;
	}

	public String getIpaddrhex() {
		return ipaddrhex;
	}

	public void setIpaddrhex(String ipaddrhex) {
		this.ipaddrhex = ipaddrhex;
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

	@Override
	public String toString() {
		return "HostIPMap [hostidhex=" + hostidhex + ", ipaddrhex=" + ipaddrhex + ", ipaddr=" + ipaddr + ", iplocation="
				+ iplocation + "]";
	}
	
	
}

