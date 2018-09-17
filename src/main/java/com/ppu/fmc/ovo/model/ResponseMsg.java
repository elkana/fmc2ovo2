package com.ppu.fmc.ovo.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseMsg implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -57346746464131542L;
	
	@JsonProperty
	private String message;
	
	@JsonProperty
	private Long status;
	
	public ResponseMsg() {
		
	}
	
	@JsonCreator
	public ResponseMsg(@JsonProperty("message")String message, @JsonProperty("status")Long status) {
//		System.err.println("constructore terpanggil");
		this.message = message;
		this.status = status;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Long getStatus() {
		return status;
	}
	public void setStatus(Long status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "ResponseMsg [message=" + message + ", status=" + status + "]";
	}

}

