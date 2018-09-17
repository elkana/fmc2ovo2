package com.ppu.fmc.local.model;

public class HostMacMap {
	
	private String hostIdInHexa;
	private String macAddressInHexa;

	public String getHostIdInHexa() {
		return hostIdInHexa;
	}
	public void setHostIdInHexa(String hostIdInHexa) {
		this.hostIdInHexa = hostIdInHexa;
	}
	public String getMacAddressInHexa() {
		return macAddressInHexa;
	}
	public void setMacAddressInHexa(String macAddressInHexa) {
		this.macAddressInHexa = macAddressInHexa;
	}
	
	@Override
	public String toString() {
		return "HostMacMap [hostIdInHexa=" + hostIdInHexa + ", macAddressInHexa=" + macAddressInHexa + "]";
	}


}
