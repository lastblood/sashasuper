package ru.sashasuper.logic;

public class MomentumStat {
    public MomentumStat(int countRight, int countWrong, double minMetric, double maxMetric, double sumMetric) {
        this.countRight = countRight;
        this.countWrong = countWrong;
        this.minMetric = minMetric;
        this.maxMetric = maxMetric;
        this.sumMetric = sumMetric;
    }

    public int countRight;
    public int countWrong;
    public double minMetric;
    public double maxMetric;
    public double sumMetric;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MomentumStat{");
        sb.append("countRight=").append(countRight);
        sb.append(", countWrong=").append(countWrong);
        sb.append(", minMetric=").append(minMetric);
        sb.append(", maxMetric=").append(maxMetric);
        sb.append(", sumMetric=").append(sumMetric);
        sb.append('}');
        return sb.toString();
    }

    public boolean betterThan(MomentumStat other) {
        return countRight > other.countRight;
    }
}
