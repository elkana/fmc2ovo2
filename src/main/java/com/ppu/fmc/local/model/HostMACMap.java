package com.ppu.fmc.local.model;

public class HostMACMap {
	private String hostidhex;

	private String macaddr;
	
	private String macvendor;

	public String getHostidhex() {
		return hostidhex;
	}

	public void setHostidhex(String hostidhex) {
		this.hostidhex = hostidhex;
	}

	public String getMacaddr() {
		return macaddr;
	}

	public void setMacaddr(String macaddr) {
		this.macaddr = macaddr;
	}

	public String getMacvendor() {
		return macvendor;
	}

	public void setMacvendor(String macvendor) {
		this.macvendor = macvendor;
	}

	@Override
	public String toString() {
		return "HostMACMap [hostidhex=" + hostidhex + ", macaddr=" + macaddr + ", macvendor=" + macvendor + "]";
	}

	
}
