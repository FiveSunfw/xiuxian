"""Import the eleven swords from the user's transparent 4-by-3 icon sheet."""

from pathlib import Path
from PIL import Image
import json

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "art_sources/forging_0_3_6/swords_sheet.png"
TEXTURES = ROOT / "src/main/resources/assets/xiuxian/textures/item"
MODELS = ROOT / "src/main/resources/assets/xiuxian/models/item"
SWORDS = [
    "spirit_horn_light_sword",
    "azure_fang_hunting_sword",
    "rock_fang_armor_piercing_sword",
    "frost_silk_binding_sword",
    "windfeather_returning_sword",
    "viper_fang_poison_sword",
    "crystal_horn_rockbreaker_sword",
    "thunder_spine_shock_sword",
    "king_tusk_greatsword",
    "jade_python_binding_sword",
    "redtail_flame_sword",
]


def main():
    sheet = Image.open(SOURCE).convert("RGBA")
    if sheet.width % 4 or sheet.height % 3 or sheet.width // 4 != sheet.height // 3:
        raise ValueError("Sword sheet must contain twelve equal square cells")
    cell = sheet.width // 4
    for index, name in enumerate(SWORDS):
        row, column = divmod(index, 4)
        icon = sheet.crop((column * cell, row * cell, (column + 1) * cell, (row + 1) * cell))
        # The source already has real transparency. Discard faint off-cell glow only.
        alpha = icon.getchannel("A")
        bounds = alpha.point(lambda value: 255 if value > 32 else 0).getbbox()
        if bounds is None:
            raise ValueError(f"Sword is missing from cell {index + 1}: {name}")
        icon = icon.crop(bounds)
        scale = min(60 / icon.width, 60 / icon.height)
        size = (max(1, round(icon.width * scale)), max(1, round(icon.height * scale)))
        icon = icon.resize(size, Image.Resampling.LANCZOS)
        result = Image.new("RGBA", (64, 64))
        result.alpha_composite(icon, ((64 - size[0]) // 2, (64 - size[1]) // 2))
        result.save(TEXTURES / f"{name}.png")
        (MODELS / f"{name}.json").write_text(json.dumps({
            "parent": "minecraft:item/handheld",
            "textures": {"layer0": f"xiuxian:item/{name}"},
        }, indent=2) + "\n", encoding="utf8")


if __name__ == "__main__":
    main()
