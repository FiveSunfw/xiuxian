package dev.xiuxian;

/** Realm progress is capped at the current manual's coverage, including a full ninth layer. */
public record CultivationProgress(int level, int points) {
    public static final int MAX_LEVEL = 9;
    public CultivationProgress {
        if (level < 0 || level > MAX_LEVEL || points < 0) throw new IllegalArgumentException("Invalid cultivation progress");
        if (level == 0) points = 0;
    }
    public int required(int base) {
        if (base < 1) throw new IllegalArgumentException("base must be positive");
        return level == 0 ? 0 : Math.multiplyExact(base, level);
    }
    public boolean full(int base) { return level > 0 && points >= required(base); }
    public CultivationProgress advance(int amount, int base) { return advance(amount, base, MAX_LEVEL); }
    public CultivationProgress advance(int amount, int base, int coverage) {
        if (base < 1 || amount < 0 || coverage < 1 || coverage > MAX_LEVEL) throw new IllegalArgumentException("Invalid progress input");
        if (level == 0) return this;
        int next = level;
        long remaining = (long) points + amount;
        int ceiling = Math.max(level, coverage);
        while (next < ceiling && remaining >= Math.multiplyExact(base, next)) {
            remaining -= Math.multiplyExact(base, next++);
        }
        return new CultivationProgress(next, (int)Math.min(remaining, Math.multiplyExact(base, next)));
    }
    public double bonus(double perLevel) { return level * perLevel; }
}
