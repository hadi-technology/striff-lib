package com.hadii.striff.metrics;

public interface Metric {

    double value(String cmpId);

    String acronym();
}
