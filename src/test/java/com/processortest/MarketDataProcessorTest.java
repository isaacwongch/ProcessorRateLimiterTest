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

import com.github.javafaker.Faker;
import com.processortest.model.MarketData;
import com.processortest.processor.MarketDataProcessor;
import com.processortest.util.MyTimer;
import com.processortest.util.SlidingWindowRateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public final class MarketDataProcessorTest {

    private static final Logger LOG = LoggerFactory.getLogger(SlidingWindowRateLimiter.class);

    @Mock
    private MyTimer timer;

    @Spy
    @InjectMocks
    private SlidingWindowRateLimiter rateLimiter;

    @Spy
    private MarketDataProcessor marketDataProcessor;

    @Before
    public void before() {
        Mockito.reset(rateLimiter);
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
    public void testSymbolNotHaveMoreThanOneUpdatePerSecond(){
        when(timer.getCurrentTime()).thenReturn(1000L);
        for (int i = 0; i < 100; i++) {
            marketDataProcessor.onMessage(getDummyMarketData("MSFT", 1000+i));
        }
        verify(marketDataProcessor, times(1)).publishAggregatedMarketData(any());
    }

    @Test
    public void testSymbolAlwaysHaveTheLatestDataWhenPublish(){

    }

    private MarketData getDummyMarketData(final String symbol, final long updateTime){
        return new MarketData(symbol, BigDecimal.ONE, BigDecimal.ONE,BigDecimal.ONE, updateTime);
    }
}
