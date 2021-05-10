package com.processortest.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of rate limiter using implementation of Sliding window
 */
public class SlidingWindowRateLimiter implements RateLimiter {
	private static final Logger LOG = LoggerFactory.getLogger(SlidingWindowRateLimiter.class);
	private static final int CALL_LIMIT_PER_SECOND = 100;
	private static final long ONE_SECOND = 1000L;
	private static final Object lock = new Object();
	/**
	 * TODO: Memory optimization
	 */
	private final Map<Long, Integer> timeHitCountMap;
	private final MyTimer timer;

	public SlidingWindowRateLimiter(final MyTimer timer) {
		this.timer = timer;
		this.timeHitCountMap = new ConcurrentHashMap<>();
	}

	public boolean isAllowed() {
		// TODO: Reduce lock contention

		long currentTime = timer.getCurrentTime();
		long curWindowKey = currentTime / 1000 * 1000;
		LOG.debug("Current window key: " + curWindowKey);

		// current window is empty
		if (timeHitCountMap.putIfAbsent(curWindowKey, 1) == null)
			return true;

		synchronized (lock) {
			Integer currentCount = timeHitCountMap.putIfAbsent(curWindowKey, 1);

			LOG.debug("Current Count: " + currentCount);
			Integer prevCount = timeHitCountMap.get(curWindowKey - ONE_SECOND);
			// Sliding window check
			if (prevCount == null) {
				if (currentCount < CALL_LIMIT_PER_SECOND) {
					timeHitCountMap.computeIfPresent(curWindowKey, (k, v) -> v + 1);
					return true;
				}
				return false;
			}

			// Approximate count
			double check = ((double) ONE_SECOND - currentTime + curWindowKey) / ONE_SECOND;
			double result = prevCount * (check) + currentCount;
			LOG.debug("{}*({}-{}+{})/{})+{} result: {}", prevCount, ONE_SECOND, currentTime, curWindowKey, ONE_SECOND,
					currentCount, result);
			if (result < CALL_LIMIT_PER_SECOND) {
				timeHitCountMap.computeIfPresent(curWindowKey, (k, v) -> v + 1);
				return true;
			}
			return false;
		}

	}
}
