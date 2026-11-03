package dev.xiuxian;

/** Pure progression rules, independent of Minecraft persistence and networking. */
public record CultivationProgress(int level, int points) {
    public static final int MAX_LEVEL = 9;
    public CultivationProgress {
        if (level < 0 || level > MAX_LEVEL || points < 0) throw new IllegalArgumentException("Invalid cultivation progress");
        if (level == 0 || level == MAX_LEVEL) points = 0;
    }
    public int required(int basePoints) {
        if (basePoints < 1) throw new IllegalArgumentException("basePoints must be positive");
        return level == 0 || level == MAX_LEVEL ? 0 : Math.multiplyExact(basePoints, level);
    }
    public CultivationProgress advance(int amount, int basePoints) {
        if (basePoints < 1) throw new IllegalArgumentException("basePoints must be positive");
        if (amount < 0) throw new IllegalArgumentException("amount must not be negative");
        if (level == 0 || level == MAX_LEVEL) return this;
        int next = level;
        long remaining = (long) points + amount;
        while (next < MAX_LEVEL && remaining >= Math.multiplyExact(basePoints, next)) {
            remaining -= Math.multiplyExact(basePoints, next);
            next++;
        }
        return new CultivationProgress(next, next == MAX_LEVEL ? 0 : Math.toIntExact(remaining));
    }
    public double bonus(double perLevel) { return level * perLevel; }
}

