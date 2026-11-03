package dev.xiuxian;

/** Keeps fractional cultivation so modest technique bonuses never disappear through rounding. */
public record CultivationGain(int points, double remainder) {
    public static CultivationGain accumulate(double remainder, double rate) {
        if (!Double.isFinite(remainder) || remainder < 0 || remainder >= 1 || !Double.isFinite(rate) || rate < 0)
            throw new IllegalArgumentException("Invalid cultivation rate or remainder");
        double total = remainder + rate;
        int points = Math.toIntExact((long) Math.floor(total));
        return new CultivationGain(points, total - points);
    }
}
