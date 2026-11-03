package dev.xiuxian;

public class CultivationProgressTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        var mortal = new CultivationProgress(0, 0);
        check(mortal.advance(10000, 300).equals(mortal), "Mortals must not cultivate");
        var first = new CultivationProgress(1, 0);
        check(first.required(300) == 300, "First threshold");
        check(first.advance(299, 300).equals(new CultivationProgress(1, 299)), "Below threshold");
        check(first.advance(300, 300).equals(new CultivationProgress(2, 0)), "Exact threshold");
        check(new CultivationProgress(1, 299).advance(2, 300).equals(new CultivationProgress(2, 1)), "Carry excess progress");
        check(first.advance(1000, 300).equals(new CultivationProgress(3, 100)), "Cross multiple levels");
        check(new CultivationProgress(8, 2399).advance(1, 300).equals(new CultivationProgress(9, 0)), "Complete ninth level");
        check(first.advance(Integer.MAX_VALUE, 300).equals(new CultivationProgress(9, 2700)), "Large gain must cap without overflow");
        check(new CultivationProgress(9, 0).advance(100, 300).points() == 100, "Ninth layer must still accumulate");
        check(new CultivationProgress(2, 200).advance(0, 100).equals(new CultivationProgress(3, 0)), "Reconcile changed threshold");
        check(20 + first.bonus(2) == 22 && 1 + first.bonus(1) == 2, "First-level default bonuses");
        check(20 + new CultivationProgress(2, 0).bonus(2) == 24 && 1 + new CultivationProgress(2, 0).bonus(1) == 3, "Second-level default bonuses");
        check(20 + new CultivationProgress(9, 0).bonus(2) == 38 && 1 + new CultivationProgress(9, 0).bonus(1) == 10, "Ninth-level default bonuses");
        try { first.advance(1, 0); throw new AssertionError("Expected invalid threshold rejection"); }
        catch (IllegalArgumentException expected) { }
        double fraction = 0; int points = 0;
        for (int i=0; i<4; i++) { var gain = CultivationGain.accumulate(fraction, 1.25); fraction = gain.remainder(); points += gain.points(); }
        check(points == 5 && fraction == 0, "Fractional technique gains must not be lost");
        var boosted = CultivationGain.accumulate(0, 1.25 * 1.5);
        check(boosted.points() == 1 && boosted.remainder() == 0.875, "Multiplicative food gain");
        check(CultivationGain.accumulate(0.5, 0).remainder() == 0.5, "Pausing preserves the fraction");
        check(first.advance(1000,300,1).equals(new CultivationProgress(1,300)), "No manual caps first layer without overflow");
        check(new CultivationProgress(1,300).advance(0,300,9).equals(new CultivationProgress(2,0)), "Learning unlocks the next layer");
        check(first.advance(100000,300,3).equals(new CultivationProgress(3,900)), "Fragment caps at third layer");
        check(new CultivationProgress(9,2699).advance(100,300).equals(new CultivationProgress(9,2700)), "Ninth layer caps at full");
        System.out.println("20 progression checks passed.");
    }
}

