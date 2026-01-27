package com.hadii.striff.metrics.spi;

import com.hadii.striff.metrics.MetricChange;

public final class OopMetricsAugmentation {

    public static final String KEY = "oopMetrics";

    private final MetricChange metricChange;
    private final boolean added;
    private final boolean deleted;

    public OopMetricsAugmentation(MetricChange metricChange, boolean added, boolean deleted) {
        this.metricChange = metricChange;
        this.added = added;
        this.deleted = deleted;
    }

    public MetricChange metricChange() {
        return metricChange;
    }

    public boolean added() {
        return added;
    }

    public boolean deleted() {
        return deleted;
    }
}
