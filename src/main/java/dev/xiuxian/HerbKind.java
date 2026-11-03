package dev.xiuxian;

public enum HerbKind {
    QI("qi_herb", "qi_liquid"),
    WOOD("greenwood_fungus", "greenwood_elixir"),
    FROST("frost_herb", "frost_elixir"),
    STONE("stone_marrow_flower", "stone_marrow_elixir"),
    SUN("scarlet_sun_herb", "scarlet_sun_elixir"),
    MARSH("marsh_orchid", "marsh_elixir"),
    TIDE("tide_herb", "tide_elixir"),
    MOSS("glow_moss", "glow_elixir");

    public final String id;
    public final String elixirId;
    HerbKind(String id, String elixirId) { this.id = id; this.elixirId = elixirId; }
}
