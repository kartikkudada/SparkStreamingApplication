package com.upgrad.casestudy.solution;

import java.io.Serializable;

public class StockOpenClose implements Serializable {
	
	private static final long serialVersionUID = 2L;
	private Double open;
	private Double close;
	
	public StockOpenClose(Double open,Double close) {
		this.open = open;
		this.close = close;
	}
	
	public Double getOpen() {
		return open;
	}
	public void setOpen(Double open) {
		this.open = open;
	}
	public Double getClose() {
		return close;
	}
	public void setClose(Double close) {
		this.close = close;
	}
	
	public String toString() {
		return "open : " + open + " close : " + close;
	}

}
