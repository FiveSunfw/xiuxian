"""Generate the first-pass 16px crop, food and workstation assets and worldgen data."""
from pathlib import Path
from PIL import Image, ImageDraw
import json

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources"
A = ROOT / "assets/xiuxian"
D = ROOT / "data/xiuxian"
CROPS = {
    "jade_rice": ((126, 211, 197), ["plains", "sunflower_plains", "meadow", "river"]),
    "green_spirit_wheat": ((171, 217, 96), ["forest", "flower_forest", "birch_forest", "old_growth_birch_forest", "plains"]),
    "dragon_blood_pepper": ((221, 63, 42), ["savanna", "savanna_plateau", "windswept_savanna", "badlands", "wooded_badlands", "eroded_badlands", "desert"]),
    "purple_jade_shoot": ((168, 100, 197), ["bamboo_jungle", "jungle", "sparse_jungle"]),
    "armor_potato": ((180, 148, 87), ["windswept_hills", "windswept_gravelly_hills", "windswept_forest", "stony_shore", "stony_peaks"]),
    "dew_melon": ((104, 206, 165), ["swamp", "mangrove_swamp"]),
    "star_flower": ((176, 182, 255), ["meadow", "flower_forest", "jagged_peaks", "frozen_peaks", "stony_peaks", "snowy_slopes", "grove"]),
    "earthfire_ginger": ((220, 143, 61), "#minecraft:is_overworld"),
}
FOODS = {
    "jade_rice_meal": (126, 211, 197), "green_spirit_flatbread": (171, 217, 96),
    "dragon_pepper_steak": (221, 63, 42), "earthfire_mutton_stew": (220, 143, 61),
    "armor_baked_potato": (180, 148, 87), "purple_shoot_fish_soup": (168, 100, 197),
    "dew_melon_drink": (104, 206, 165), "starlight_honey_cake": (176, 182, 255),
}


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf8")


def texture(path, image):
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)


def crop_texture(index, color, age):
    image = Image.new("RGBA", (16, 16)); draw = ImageDraw.Draw(image)
    dark = (44, 91, 58, 255); green = (77, 144, 76, 255); pale = (*color, 255)
    height = 4 + age * 3
    for dx in (-3, 0, 3):
        x = 8 + dx
        draw.line((x, 15, x + (1 if dx < 0 else -1), 15 - height), fill=dark, width=1)
        draw.line((x, 13, x + (3 if dx <= 0 else -3), 11), fill=green, width=1)
    if age >= 1:
        top = max(1, 15 - height)
        if index in (0, 1):
            for x in (4, 7, 10):
                draw.rectangle((x, top, x + 1, top + 2 + age), fill=pale)
        elif index in (2, 5):
            draw.rectangle((5, top + 1, 7, top + 4), fill=pale); draw.rectangle((9, top + 3, 11, top + 6), fill=pale)
        elif index in (3, 7):
            draw.polygon([(5, 14), (6, top + 2), (8, top), (10, top + 2), (11, 14)], fill=pale)
            draw.line((6, 12, 10, 10), fill=dark)
        elif index == 4:
            draw.ellipse((5, 10, 11, 15), fill=pale, outline=dark)
            if age >= 2: draw.ellipse((3, 12, 6, 15), fill=pale, outline=dark)
        else:
            draw.line((8, top, 8, top + 6), fill=pale, width=2)
            draw.line((5, top + 2, 11, top + 2), fill=pale, width=2)
            draw.point((8, top + 2), fill=(255, 241, 170, 255))
    return image


def food_texture(index, color):
    image = Image.new("RGBA", (16, 16)); d = ImageDraw.Draw(image)
    brown = (104, 66, 39, 255); light = (224, 192, 137, 255); color = (*color, 255)
    if index in (0, 3, 5):
        d.polygon([(2, 7), (14, 7), (12, 13), (4, 13)], fill=brown, outline=(61, 39, 27, 255))
        d.ellipse((2, 4, 14, 10), fill=light, outline=brown); d.ellipse((4, 5, 12, 9), fill=color)
        d.point((7, 6), fill=(246, 237, 190, 255)); d.point((10, 8), fill=(246, 237, 190, 255))
    elif index == 6:
        d.rectangle((6, 1, 10, 3), fill=(145, 193, 200, 255)); d.polygon([(5, 3), (11, 3), (13, 6), (12, 14), (4, 14), (3, 6)], fill=color, outline=(78, 130, 135, 255))
        d.line((6, 5, 6, 11), fill=(224, 255, 239, 255))
    elif index == 7:
        d.rectangle((3, 7, 13, 13), fill=(176, 120, 62, 255), outline=brown); d.rectangle((3, 5, 13, 8), fill=light, outline=brown)
        d.line((8, 2, 8, 7), fill=color, width=2); d.line((5, 4, 11, 4), fill=color, width=2)
    else:
        d.polygon([(2, 9), (5, 4), (12, 3), (14, 7), (11, 12), (4, 13)], fill=light, outline=brown)
        d.line((5, 8, 11, 6), fill=color, width=2); d.point((7, 10), fill=brown)
    return image


def block_textures():
    for side in ("top", "bottom", "north", "south", "east", "west"):
        image = Image.new("RGBA", (16, 16), (105, 73, 49, 255)); d = ImageDraw.Draw(image)
        for x in range(0, 16, 4):
            for y in range(0, 16, 4): d.point((x + (y % 3), y + 1), fill=(143, 105, 69, 255))
        if side == "top":
            d.line((3, 3, 12, 12), fill=(119, 211, 198, 255)); d.line((12, 3, 3, 12), fill=(119, 211, 198, 255)); d.rectangle((6, 6, 9, 9), fill=(179, 236, 218, 255))
        texture(A / f"textures/block/spirit_farmland_{side}.png", image)
    for side in ("side", "top", "front", "front_lit"):
        image = Image.new("RGBA", (16, 16), (102, 80, 62, 255)); d = ImageDraw.Draw(image)
        d.rectangle((0, 0, 15, 15), outline=(59, 48, 42, 255)); d.line((0, 5, 15, 5), fill=(63, 53, 45, 255)); d.line((0, 11, 15, 11), fill=(63, 53, 45, 255))
        if side.startswith("front"):
            d.rectangle((3, 7, 12, 14), fill=(38, 34, 31, 255), outline=(161, 113, 64, 255))
            if side.endswith("lit"): d.polygon([(5, 13), (7, 9), (8, 12), (10, 8), (11, 13)], fill=(255, 151, 54, 255))
        if side == "top": d.rectangle((4, 4, 11, 11), fill=(57, 60, 58, 255), outline=(190, 150, 91, 255))
        texture(A / f"textures/block/spirit_kitchen_{side}.png", image)


def generate():
    block_textures()
    for name, color in {
        "spirit_food_mana_regen": (101, 211, 209), "spirit_food_attack": (199, 71, 45),
        "spirit_food_vitality": (198, 123, 65), "spirit_food_defense": (157, 138, 92),
        "spirit_food_speed": (112, 181, 165), "spirit_food_starlight": (138, 154, 241),
    }.items():
        icon = Image.new("RGBA", (18, 18)); draw = ImageDraw.Draw(icon)
        draw.ellipse((2, 2, 15, 15), fill=(*color, 255), outline=(38, 46, 48, 255))
        draw.line((8, 4, 8, 13), fill=(247, 240, 198, 255), width=2)
        draw.line((4, 8, 13, 8), fill=(247, 240, 198, 255), width=2)
        texture(A / f"textures/mob_effect/{name}.png", icon)
    for index, (crop, (color, biomes)) in enumerate(CROPS.items()):
        for age in range(4):
            texture(A / f"textures/block/{crop}_{age}.png", crop_texture(index, color, age))
            write_json(A / f"models/block/{crop}_{age}.json", {"parent": "minecraft:block/cross", "render_type": "minecraft:cutout", "textures": {"cross": f"xiuxian:block/{crop}_{age}"}})
        write_json(A / f"blockstates/{crop}.json", {"variants": {f"age={age}": {"model": f"xiuxian:block/{crop}_{age}"} for age in range(4)}})
        write_json(A / f"models/item/{crop}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"xiuxian:block/{crop}_3"}})
        write_json(D / f"loot_tables/blocks/{crop}.json", {"type":"minecraft:block", "pools":[
            {"rolls":1, "entries":[{"type":"minecraft:item","name":f"xiuxian:{crop}"}], "conditions":[{"condition":"minecraft:survives_explosion"}]},
            {"rolls":1, "entries":[{"type":"minecraft:item","name":f"xiuxian:{crop}"}], "conditions":[{"condition":"minecraft:survives_explosion"},{"condition":"minecraft:block_state_property","block":f"xiuxian:{crop}","properties":{"age":"3"}}]}]})
        write_json(D / f"worldgen/configured_feature/wild_{crop}.json", {"type": f"xiuxian:wild_{crop}", "config": {}})
        if crop == "earthfire_ginger":
            placement = [{"type":"minecraft:count","count":32},{"type":"minecraft:in_square"},{"type":"minecraft:height_range","height":{"type":"minecraft:uniform","min_inclusive":{"absolute":-64},"max_inclusive":{"absolute":-1}}},{"type":"minecraft:biome"}]
        else:
            placement = [{"type":"minecraft:rarity_filter","chance":6},{"type":"minecraft:in_square"},{"type":"minecraft:heightmap","heightmap":"WORLD_SURFACE_WG"},{"type":"minecraft:biome"}]
        write_json(D / f"worldgen/placed_feature/wild_{crop}.json", {"feature":f"xiuxian:wild_{crop}","placement":placement})
        value = biomes if isinstance(biomes, str) else [f"minecraft:{biome}" for biome in biomes]
        write_json(D / f"forge/biome_modifier/wild_{crop}.json", {"type":"forge:add_features","biomes":value,"features":f"xiuxian:wild_{crop}","step":"vegetal_decoration"})
    for index, (food, color) in enumerate(FOODS.items()):
        texture(A / f"textures/item/{food}.png", food_texture(index, color))
        write_json(A / f"models/item/{food}.json", {"parent":"minecraft:item/generated","textures":{"layer0":f"xiuxian:item/{food}"}})
    textures = {side:f"xiuxian:block/spirit_farmland_{'bottom' if side == 'down' else 'top' if side == 'up' else side}" for side in ("down","up","north","south","east","west")}
    textures["particle"] = textures["north"]
    write_json(A / "models/block/spirit_farmland.json", {"parent":"minecraft:block/cube","textures":textures})
    write_json(A / "models/item/spirit_farmland.json", {"parent":"xiuxian:block/spirit_farmland"})
    write_json(A / "blockstates/spirit_farmland.json", {"variants":{"":{"model":"xiuxian:block/spirit_farmland"}}})
    for lit in (False, True):
        suffix = "_lit" if lit else ""
        write_json(A / f"models/block/spirit_kitchen{suffix}.json", {"parent":"minecraft:block/orientable","textures":{"top":"xiuxian:block/spirit_kitchen_top","side":"xiuxian:block/spirit_kitchen_side","front":f"xiuxian:block/spirit_kitchen_front{suffix}"}})
    variants = {}
    for facing, rotation in (("north",0),("east",90),("south",180),("west",270)):
        for lit in (False, True): variants[f"facing={facing},lit={str(lit).lower()}"] = {"model":f"xiuxian:block/spirit_kitchen{'_lit' if lit else ''}","y":rotation}
    write_json(A / "blockstates/spirit_kitchen.json", {"variants":variants})
    write_json(A / "models/item/spirit_kitchen.json", {"parent":"xiuxian:block/spirit_kitchen"})
    for block in ("spirit_farmland", "spirit_kitchen"):
        write_json(D / f"loot_tables/blocks/{block}.json", {"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":f"xiuxian:{block}"}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]})
    write_json(D / "recipes/spirit_farmland.json", {"type":"minecraft:crafting_shaped","pattern":["DBD","BSB","DBD"],"key":{"D":{"item":"minecraft:dirt"},"B":{"item":"minecraft:bone_meal"},"S":{"item":"xiuxian:spirit_stone"}},"result":{"item":"xiuxian:spirit_farmland","count":4}})
    bricks = [{"item":"minecraft:bricks"},{"item":"minecraft:stone_bricks"}]
    heat = [{"item":"minecraft:campfire"},{"item":"minecraft:furnace"}]
    write_json(D / "recipes/spirit_kitchen.json", {"type":"minecraft:crafting_shapeless","ingredients":[bricks,bricks,bricks,bricks,{"item":"minecraft:iron_ingot"},{"item":"minecraft:iron_ingot"},{"item":"xiuxian:spirit_stone"},heat],"result":{"item":"xiuxian:spirit_kitchen"}})


if __name__ == "__main__": generate()
