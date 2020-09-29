package ru.sashasuper.utils;

public class MomentumStat {
    public MomentumStat(int countRight, int countWrong, double sumMSE, double sumCE) {
        this.countRight = countRight;
        this.countWrong = countWrong;
        this.sumMSE = sumMSE;
        this.sumCE = sumCE;
    }

    public int countRight;
    public int countWrong;
    public double sumMSE;
    public double sumCE;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("countRight=").append(countRight);
        sb.append(", countWrong=").append(countWrong);
        sb.append(", MSE=").append(sumMSE);
        sb.append(", CE=").append(sumCE);
        return sb.toString();
    }
}
