"""Regenerates the nua item textures.

The storm helmet icon is the vanilla copper helmet with a lightning rod drawn on
top of it, so the palette and shading match the vanilla armor exactly. The
vanilla helmet pixels are embedded below so this script needs nothing but Pillow.

Run from the project root:

    python tools/generate_textures.py
"""

import os
from PIL import Image

SHADOW = (66, 28, 17, 255)
DARK = (95, 44, 28, 255)
MID = (128, 57, 33, 255)
LIGHT = (156, 78, 49, 255)
BRIGHT = (214, 109, 72, 255)
GLOW = (252, 153, 130, 255)

# 16x16 grid of minecraft:item/copper_helmet, copied verbatim.
COPPER_HELMET = [
    [None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None],
    [None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None],
    [None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None],
    [None, None, None, None, None, DARK, DARK, DARK, DARK, DARK, DARK, None, None, None, None, None],
    [None, None, None, None, DARK, LIGHT, BRIGHT, BRIGHT, BRIGHT, BRIGHT, MID, SHADOW, None, None, None, None],
    [None, None, None, DARK, LIGHT, GLOW, (253, 212, 203, 255), GLOW, BRIGHT, BRIGHT, LIGHT, MID, SHADOW, None, None, None],
    [None, None, None, DARK, BRIGHT, GLOW, GLOW, BRIGHT, BRIGHT, LIGHT, LIGHT, LIGHT, SHADOW, None, None, None],
    [None, None, None, DARK, BRIGHT, BRIGHT, DARK, DARK, DARK, SHADOW, MID, LIGHT, SHADOW, None, None, None],
    [None, None, None, DARK, BRIGHT, DARK, SHADOW, SHADOW, SHADOW, SHADOW, SHADOW, LIGHT, SHADOW, None, None, None],
    [None, None, None, DARK, BRIGHT, DARK, SHADOW, SHADOW, SHADOW, SHADOW, SHADOW, MID, SHADOW, None, None, None],
    [None, None, None, DARK, LIGHT, SHADOW, SHADOW, SHADOW, SHADOW, SHADOW, SHADOW, MID, SHADOW, None, None, None],
    [None, None, None, None, DARK, SHADOW, None, None, None, None, SHADOW, SHADOW, None, None, None, None],
    [None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None],
    [None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None],
    [None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None],
    [None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None],
]

# Lightning rod sitting on the crown of the helmet.
ROD = {
    (0, 7): BRIGHT,
    (1, 7): GLOW,
    (1, 8): MID,
    (2, 6): DARK,
    (2, 7): BRIGHT,
    (2, 8): LIGHT,
    (2, 9): DARK,
}

OUT = os.path.join("src", "main", "resources", "assets", "nua", "textures", "item")
ARMOR_OUT = os.path.join("src", "main", "resources", "assets", "nua", "textures", "armor")


def build(pixels):
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(pixels):
        for x, color in enumerate(row):
            if color is not None:
                image.putpixel((x, y), color)
    return image


def main():
    os.makedirs(OUT, exist_ok=True)
    os.makedirs(ARMOR_OUT, exist_ok=True)

    storm = [list(row) for row in COPPER_HELMET]
    for (y, x), color in ROD.items():
        storm[y][x] = color

    build(storm).save(os.path.join(OUT, "storm_helmet.png"))
    print("wrote", os.path.join(OUT, "storm_helmet.png"))

    # GeckoLib armour texture. The placeholder model samples uv [0,0] on every cube, so a flat
    # copper field with a lit top edge is enough until the real model replaces it.
    rod = Image.new("RGBA", (16, 16), BRIGHT)
    px = rod.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = GLOW if y < 3 else (BRIGHT if y < 11 else MID)
    rod.save(os.path.join(ARMOR_OUT, "storm_helmet.png"))
    print("wrote", os.path.join(ARMOR_OUT, "storm_helmet.png"))


if __name__ == "__main__":
    main()
