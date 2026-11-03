"""Cut the user-supplied concept sheets into independent transparent inventory icons."""

from pathlib import Path
from PIL import Image
import json
import numpy as np
from scipy.ndimage import binary_dilation, label

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "art_sources/forging_0_3_5"
TEXTURES = ROOT / "src/main/resources/assets/xiuxian/textures/item"
MODELS = ROOT / "src/main/resources/assets/xiuxian/models/item"

MATERIALS = [
    "spirit_horn", "wolf_pelt", "azure_fang", "rockhide_plate", "curved_tusk",
    "viper_fang", "venom_sac", "frost_silk", "frost_gland", "wind_feather",
    "eagle_talon", "thunder_scale", "charged_spine", "thick_rock_plate",
    "king_tusk", "jade_scale", "python_sinew", "beast_marrow",
]
BLANKS = [
    "spirit_gold_sword", "spirit_gold_helmet", "spirit_gold_chestplate",
    "spirit_gold_leggings", "spirit_gold_boots",
]
ARMOR = [
    "spirit_horn_crown", "nightwing_hood", "frost_silk_headband",
    "thunder_pattern_headband", "frost_horn_helmet",
    "wolf_hide_hunting_chestplate", "rock_armor_chestplate",
    "sand_scorpion_chestplate", "king_armor", "jade_scale_water_armor",
    "wolf_pursuit_leggings", "marsh_walking_leggings", "vine_leggings",
    "tide_scale_leggings", "python_scale_leggings",
    "spirit_horn_light_boots", "sand_walking_boots", "rock_step_boots",
    "redtail_swift_boots", "falling_cloud_boots",
]


def background(rgb, sheet):
    red, green, blue = rgb
    if sheet in ("ingot", "blanks"):
        return max(rgb) - min(rgb) < 10 and 126 <= red <= 210
    if sheet == "materials":
        return max(rgb) - min(rgb) < 8 and 72 <= red <= 148
    return red <= 45 and -4 <= green - red <= 9 and 0 <= blue - red <= 17


def import_icon(sheet, name, box):
    source = Image.open(SOURCE / f"{sheet}_sheet.png").convert("RGB").crop(box)
    rgba = Image.new("RGBA", source.size)
    source_pixels = source.load()
    target_pixels = rgba.load()
    for y in range(source.height):
        for x in range(source.width):
            color = source_pixels[x, y]
            target_pixels[x, y] = (*color, 0 if background(color, sheet) else 255)
    pixels = np.array(rgba)
    connected, count = label(binary_dilation(pixels[:, :, 3] > 0, iterations=2))
    components = []
    for component in range(1, count + 1):
        ys, xs = np.where(connected == component)
        if len(xs) >= 25:
            components.append((component, len(xs), float(xs.mean()), float(ys.mean())))
    if components:
        center = source.width / 2
        focus = max(components, key=lambda part: part[1] / (1 + abs(part[2] - center) / 20))
        kept = [part[0] for part in components if part[0] == focus[0] or
                (part[1] > focus[1] * 0.2 and abs(part[2] - focus[2]) < source.width * 0.26
                 and abs(part[3] - focus[3]) < source.height * 0.26)]
        pixels[:, :, 3] *= np.isin(connected, kept)
        rgba = Image.fromarray(pixels, "RGBA")
    bounds = rgba.getbbox()
    if bounds is None:
        raise ValueError(f"No icon pixels found for {name}")
    rgba = rgba.crop(bounds)
    scale = min(56 / rgba.width, 56 / rgba.height)
    size = (max(1, round(rgba.width * scale)), max(1, round(rgba.height * scale)))
    rgba = rgba.resize(size, Image.Resampling.LANCZOS)
    output = Image.new("RGBA", (64, 64))
    output.alpha_composite(rgba, ((64 - size[0]) // 2, (64 - size[1]) // 2))
    output.save(TEXTURES / f"{name}.png")
    parent = "minecraft:item/handheld" if name.endswith("_sword") else "minecraft:item/generated"
    (MODELS / f"{name}.json").write_text(json.dumps({
        "parent": parent, "textures": {"layer0": f"xiuxian:item/{name}"}
    }, indent=2) + "\n", encoding="utf8")


def main():
    import_icon("ingot", "spirit_gold_ingot", (275, 105, 725, 465))
    blank_boxes = [
        (18, 160, 230, 412), (230, 160, 415, 410), (415, 160, 625, 414),
        (630, 155, 810, 415), (820, 165, 1008, 408),
    ]
    for name, box in zip(BLANKS, blank_boxes, strict=True):
        import_icon("blanks", name, box)

    material_columns = [
        [(0, 118), (120, 233), (238, 359), (363, 477), (480, 590),
         (595, 702), (707, 805), (809, 918), (922, 1024)],
        [(0, 123), (126, 242), (246, 355), (360, 477), (480, 592),
         (596, 707), (712, 814), (817, 924), (927, 1024)],
    ]
    for index, name in enumerate(MATERIALS):
        row, column = divmod(index, 9)
        left, right = material_columns[row][column]
        top, bottom = (122, 252) if row == 0 else (318, 405)
        import_icon("materials", name, (left, top, right, bottom))

    armor_columns = [
        [(191, 314), (330, 463), (493, 620), (650, 789), (869, 1023)],
        [(183, 314), (334, 468), (489, 621), (646, 796), (861, 1018)],
        [(196, 315), (334, 466), (489, 622), (652, 791), (855, 1017)],
        [(189, 316), (332, 467), (489, 625), (648, 795), (857, 1019)],
    ]
    armor_rows = [(5, 120), (151, 267), (308, 415), (453, 552)]
    for index, name in enumerate(ARMOR):
        row, column = divmod(index, 5)
        left, right = armor_columns[row][column]
        top, bottom = armor_rows[row]
        import_icon("armor", name, (left, top, right, bottom))

    # The revised serpent venom sac on the armor sheet supersedes its earlier icon.
    import_icon("armor", "venom_sac", (15, 5, 175, 255))


if __name__ == "__main__":
    main()
