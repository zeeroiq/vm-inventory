package com.shri.vminventory.dto;

public record ChartDataPoint(
    String label,
    long value,
    String secondaryLabel
) {
    public ChartDataPoint(String label, long value) {
        this(label, value, null);
    }
}
