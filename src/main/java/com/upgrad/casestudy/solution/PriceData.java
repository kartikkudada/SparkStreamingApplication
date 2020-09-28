package com.upgrad.casestudy.solution;

import java.io.Serializable;

public class PriceData implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Double close;
	private String high;
	private String low;
	private Double open;
	private Double volume;
	
	
	public Double getClose() {
		return close;
	}
	public void setClose(Double close) {
		this.close = close;
	}
	public String getHigh() {
		return high;
	}
	public void setHigh(String high) {
		this.high = high;
	}
	public String getLow() {
		return low;
	}
	public void setLow(String low) {
		this.low = low;
	}
	public Double getOpen() {
		return open;
	}
	public void setOpen(Double open) {
		this.open = open;
	}
	public Double getVolume() {
		return volume;
	}
	public void setVolume(Double volume) {
		this.volume = volume;
	}
	
	
	@Override
	public String toString() {
		return "priceData {close=" + close + ", high=" + high + ", low=" + low +"}";
				}
		
		
	}


