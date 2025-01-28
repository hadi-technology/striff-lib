package com.hadii.striff.metrics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class MetricChange {
    private final String className;
    private final BigDecimal oldNOC;
    private final BigDecimal updatedNOC;
    private final BigDecimal oldDIT;
    private final BigDecimal updatedDIT;
    private final BigDecimal oldWMC;
    private final BigDecimal updatedWMC;
    private final BigDecimal oldAC;
    private final BigDecimal updatedAC;
    private final BigDecimal oldEC;
    private final BigDecimal updatedEC;
    private final BigDecimal oldEncapsulation;
    private final BigDecimal updatedEncapsulation;

    public MetricChange(String className,
            double oldNOC, double updatedNOC,
            double oldDIT, double updatedDIT,
            double oldWMC, double updatedWMC,
            double oldAC, double updatedAC,
            double oldEC, double updatedEC,
            double oldEncapsulation, double updatedEncapsulation) {
        this.className = className;
        this.oldNOC = BigDecimal.valueOf(oldNOC).setScale(2, RoundingMode.HALF_UP);
        this.updatedNOC = BigDecimal.valueOf(updatedNOC).setScale(2, RoundingMode.HALF_UP);
        this.oldDIT = BigDecimal.valueOf(oldDIT).setScale(2, RoundingMode.HALF_UP);
        this.updatedDIT = BigDecimal.valueOf(updatedDIT).setScale(2, RoundingMode.HALF_UP);
        this.oldWMC = BigDecimal.valueOf(oldWMC).setScale(2, RoundingMode.HALF_UP);
        this.updatedWMC = BigDecimal.valueOf(updatedWMC).setScale(2, RoundingMode.HALF_UP);
        this.oldAC = BigDecimal.valueOf(oldAC).setScale(2, RoundingMode.HALF_UP);
        this.updatedAC = BigDecimal.valueOf(updatedAC).setScale(2, RoundingMode.HALF_UP);
        this.oldEC = BigDecimal.valueOf(oldEC).setScale(2, RoundingMode.HALF_UP);
        this.updatedEC = BigDecimal.valueOf(updatedEC).setScale(2, RoundingMode.HALF_UP);
        this.oldEncapsulation = BigDecimal.valueOf(oldEncapsulation).setScale(2, RoundingMode.HALF_UP);
        this.updatedEncapsulation = BigDecimal.valueOf(updatedEncapsulation).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return String.format("Metric changes for class '%s':%n"
                + "- NOC: %.2f -> %.2f%n"
                + "- DIT: %.2f -> %.2f%n"
                + "- WMC: %.2f -> %.2f%n"
                + "- Afferent Coupling: %.2f -> %.2f%n"
                + "- Efferent Coupling: %.2f -> %.2f%n"
                + "- Encapsulation: %.2f -> %.2f%n",
                className, oldNOC, updatedNOC, oldDIT, updatedDIT, oldWMC, updatedWMC,
                oldAC, updatedAC, oldEC, updatedEC, oldEncapsulation, updatedEncapsulation);
    }

    @JsonIgnore
    public String className() {
        return className;
    }

    @JsonProperty("oldNOC")
    public double oldNOC() {
        return oldNOC.doubleValue();
    }

    @JsonProperty("updatedNOC")
    public double updatedNOC() {
        return updatedNOC.doubleValue();
    }

    @JsonProperty("oldDIT")
    public double oldDIT() {
        return oldDIT.doubleValue();
    }

    @JsonProperty("updatedDIT")
    public double updatedDIT() {
        return updatedDIT.doubleValue();
    }

    @JsonProperty("oldWMC")
    public double oldWMC() {
        return oldWMC.doubleValue();
    }

    @JsonProperty("updatedWMC")
    public double updatedWMC() {
        return updatedWMC.doubleValue();
    }

    @JsonProperty("oldAC")
    public double oldAC() {
        return oldAC.doubleValue();
    }

    @JsonProperty("updatedAC")
    public double updatedAC() {
        return updatedAC.doubleValue();
    }

    @JsonProperty("oldEC")
    public double oldEC() {
        return oldEC.doubleValue();
    }

    @JsonProperty("updatedEC")
    public double updatedEC() {
        return updatedEC.doubleValue();
    }

    @JsonProperty("oldEncapsulation")
    public double oldEncapsulation() {
        return oldEncapsulation.doubleValue();
    }

    @JsonProperty("updatedEncapsulation")
    public double updatedEncapsulation() {
        return updatedEncapsulation.doubleValue();
    }
}
