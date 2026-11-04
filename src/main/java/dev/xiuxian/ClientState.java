package dev.xiuxian;

// Only the local player's server-synchronized display state.
public final class ClientState {
    public static CultivationFoundation.State foundation;
    public static int level, points, required, technique;
    public static double rate;
    public static double formation=1;
    public static double attackDamage = 1;
    public static void clear() { foundation = null; SpellManuals.client = null; level = 0; points = 0; required = 0; attackDamage = 1; technique = 0; rate = 0; formation = 1; }
    private ClientState() {}
}

