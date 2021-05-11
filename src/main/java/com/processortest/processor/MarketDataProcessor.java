package com.processortest.processor;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.processortest.model.MarketData;
import com.processortest.model.SymbolLatestUpdateHistory;
import com.processortest.util.MyTimer;
import com.processortest.util.SlidingWindowRateLimiter;

/**
 * Implementation of Market Data Processor with the following requirements:
 * <p>
 * Ensure that the publishAggregatedMarketData method is not called any more
 * than 100 times/sec where this period is a sliding window.
 * Ensure each symbol will not have more than one update per second
 * Ensure each symbol will always have the latest market data when it is published
 *
 */
public class MarketDataProcessor {
	private static final Logger LOG = LoggerFactory.getLogger(MarketDataProcessor.class);

	private final Map<String, SymbolLatestUpdateHistory> symbolLastUpdateMap;

	private final SlidingWindowRateLimiter rateLimiter;

	private final MyTimer timer;

	public MarketDataProcessor(final MyTimer timer) {
		this.rateLimiter = new SlidingWindowRateLimiter(timer);
		this.timer = timer;
		this.symbolLastUpdateMap = new HashMap<>();
	}

	// Receive incoming market data
	public void onMessage(MarketData data) {

		if (rateLimiter.isAllowed()) {
			if (isSymbolAllowed(data)) {
				LOG.debug("Allowed {}", System.currentTimeMillis());
				publishAggregatedMarketData(data);
			}
		}
		// handle rejected cases, sent to dead letter queue?
	}

	/**
	 * Check whether symbol has been processed by the processor within [T:T-1]
	 * and if it carries the latest market data, which is dictated by the field
	 * * {@link MarketData#getUpdateTime()}
	 * 
	 * @param data
	 * @return
	 */
	public boolean isSymbolAllowed(final MarketData data) {
		synchronized (symbolLastUpdateMap) {
			SymbolLatestUpdateHistory hist = symbolLastUpdateMap.get(data.getSymbol());
			if (hist == null || (timer.getCurrentTime() - hist.getSystemProcessTime()  > 1000
					&& data.getUpdateTime() > hist.getMarketUpdateTime())) {
				symbolLastUpdateMap.put(data.getSymbol(),
						new SymbolLatestUpdateHistory(data.getSymbol(), data.getUpdateTime(), timer.getCurrentTime()));
				return true;
			}

			return false;
		}
	}

	// Publish aggregated and throttled market data
	public void publishAggregatedMarketData(MarketData data) {
		// Do Nothing, assume implemented.
	}
}
