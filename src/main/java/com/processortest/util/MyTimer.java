package com.processortest.util;

public class MyTimer implements ITimer{

    public long getCurrentTime() {
        return System.currentTimeMillis();
    }
}
