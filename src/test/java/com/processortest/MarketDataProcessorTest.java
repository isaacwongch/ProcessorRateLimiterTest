package com.processortest;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javafaker.Faker;
import com.processortest.model.MarketData;
import com.processortest.processor.MarketDataProcessor;
import com.processortest.util.MyTimer;
import com.processortest.util.SlidingWindowRateLimiter;

@RunWith(MockitoJUnitRunner.class)
public final class MarketDataProcessorTest {

    private static final Logger LOG = LoggerFactory.getLogger(SlidingWindowRateLimiter.class);

    @Mock
    private MyTimer timer;

    @Spy
    @InjectMocks
    private SlidingWindowRateLimiter rateLimiter;

    @Spy
    @InjectMocks
    private MarketDataProcessor marketDataProcessor;

    @Before
    public void before() {
        Mockito.reset(rateLimiter,marketDataProcessor);
    }

    @Test
    public void testPublishNotMoreThanHundredPerSecond(){
        Faker faker = new Faker(new Random(9));
        when(timer.getCurrentTime()).thenReturn(1000L);
        for (int i = 0; i < 100; i++) {
            marketDataProcessor.onMessage(getDummyMarketData(faker.stock().nsdqSymbol(), 1000));
        }
        verify(marketDataProcessor, times(100)).publishAggregatedMarketData(any());
    }

    @Test
    public void testPublishMoreThanHundredPerSecond(){
        Faker faker = new Faker(new Random(9));
        when(timer.getCurrentTime()).thenReturn(500L);
        for (int i = 0; i < 1000; i++) {
            marketDataProcessor.onMessage(getDummyMarketData(faker.stock().nsdqSymbol(), 500));
        }
        when(timer.getCurrentTime()).thenReturn(1000L);
        for (int i = 0; i < 1000; i++) {
            marketDataProcessor.onMessage(getDummyMarketData(faker.stock().nsdqSymbol(), 1000));
        }
        when(timer.getCurrentTime()).thenReturn(1500L);
        for (int i = 0; i < 1000; i++) {
            marketDataProcessor.onMessage(getDummyMarketData(faker.stock().nsdqSymbol(), 1500));
        }
        when(timer.getCurrentTime()).thenReturn(2000L);
        for (int i = 0; i < 1000; i++) {
            marketDataProcessor.onMessage(getDummyMarketData(faker.stock().nsdqSymbol(), 2000));
        }
        verify(marketDataProcessor, times(200)).publishAggregatedMarketData(any());
    }

    @Test
    public void testSymbolNotHaveMoreThanOneUpdatePerSecond(){
        when(timer.getCurrentTime()).thenReturn(1000L);
        for (int i = 0; i < 100; i++) {
            marketDataProcessor.onMessage(getDummyMarketData("MSFT", 1000+i));
        }
        verify(marketDataProcessor, times(1)).publishAggregatedMarketData(any());
    }

	/**
	 * At an earlier point in time, processed MSFT with updated time as
	 * 1620664496540. 1s later and 2s later another 2 events for MSFT with
	 * updated time 1620664496440 and 1620664496430 (both are outdated) came
	 * respectively.
	 */
	@Test
	public void testSymbolAlwaysHaveTheLatestDataWhenPublishTwoOutdatedData() {
		when(timer.getCurrentTime()).thenReturn(1620664506540L);
		marketDataProcessor.onMessage(getDummyMarketData("MSFT", 1620664496540L));
		when(timer.getCurrentTime()).thenReturn(1620664507540L);
		marketDataProcessor.onMessage(getDummyMarketData("MSFT", 1620664496440L));
		when(timer.getCurrentTime()).thenReturn(1620664508540L);
		marketDataProcessor.onMessage(getDummyMarketData("MSFT", 1620664496430L));
		verify(marketDataProcessor, times(1)).publishAggregatedMarketData(any());
	}

    /**
     * At an earlier point in time, processed MSFT with updated time as
     * 1620664496540. 1s later and 2s later another 2 events for MSFT with
     * updated time 1620664496440 and 1620664496430 (one is outdated) came respectively.
     */
    @Test
    public void testSymbolAlwaysHaveTheLatestDataWhenPublishOneOutdatedData() {
        when(timer.getCurrentTime()).thenReturn(1620664506540L);
        marketDataProcessor.onMessage(getDummyMarketData("MSFT", 1620664496540L));
        when(timer.getCurrentTime()).thenReturn(1620664507540L);
        marketDataProcessor.onMessage(getDummyMarketData("MSFT", 1620664496440L));
        when(timer.getCurrentTime()).thenReturn(1620664508540L);
        marketDataProcessor.onMessage(getDummyMarketData("MSFT", 1620664496840L));
        verify(marketDataProcessor, times(2)).publishAggregatedMarketData(any());
    }

    private MarketData getDummyMarketData(final String symbol, final long updateTime){
        return new MarketData(symbol, BigDecimal.ONE, BigDecimal.ONE,BigDecimal.ONE, updateTime);
    }
}
