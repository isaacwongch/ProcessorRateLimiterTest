package com.processortest.model;

import java.math.BigDecimal;

/**
 * Model for storing market data info
 */
public class MarketData {
	private String symbol;
	private BigDecimal bid;
	private BigDecimal ask;
	private BigDecimal last;
	private long updateTime;

	public String getSymbol() {
		return symbol;
	}

	public BigDecimal getBid() {
		return bid;
	}

	public BigDecimal getAsk() {
		return ask;
	}

	public BigDecimal getLast() {
		return last;
	}

	public long getUpdateTime() {
		return updateTime;
	}
}
