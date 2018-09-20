package com.ppu.fmc.local.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Embeddable
public class UrlLogIdentity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@NotNull
	@Column(name = "")
	private Long firstpacketsec;

	@NotNull
	@Column(length = 36)
	private String macaddr;

	@NotNull
	@Column(length = 255)
	private String keyurl;

	public Long getFirstpacketsec() {
		return firstpacketsec;
	}

	public void setFirstpacketsec(Long firstpacketsec) {
		this.firstpacketsec = firstpacketsec;
	}

	public String getMacaddr() {
		return macaddr;
	}

	public void setMacaddr(String macaddr) {
		this.macaddr = macaddr;
	}

	public String getKeyurl() {
		return keyurl;
	}

	public void setKeyurl(String keyurl) {
		this.keyurl = keyurl;
	}

	@Override
	public String toString() {
		return "UrlLogIdentity [firstpacketsec=" + firstpacketsec + ", macaddr=" + macaddr + ", keyurl=" + keyurl + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((firstpacketsec == null) ? 0 : firstpacketsec.hashCode());
		result = prime * result + ((keyurl == null) ? 0 : keyurl.hashCode());
		result = prime * result + ((macaddr == null) ? 0 : macaddr.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UrlLogIdentity other = (UrlLogIdentity) obj;
		if (firstpacketsec == null) {
			if (other.firstpacketsec != null)
				return false;
		} else if (!firstpacketsec.equals(other.firstpacketsec))
			return false;
		if (keyurl == null) {
			if (other.keyurl != null)
				return false;
		} else if (!keyurl.equals(other.keyurl))
			return false;
		if (macaddr == null) {
			if (other.macaddr != null)
				return false;
		} else if (!macaddr.equals(other.macaddr))
			return false;
		return true;
	}

}
