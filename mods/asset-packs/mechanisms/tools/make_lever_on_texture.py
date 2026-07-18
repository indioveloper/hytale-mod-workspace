from __future__ import annotations

import argparse
from pathlib import Path

from PIL import Image


def make_on_texture(source: Path, output: Path) -> None:
    image = Image.open(source).convert("RGBA")
    pixels = image.load()

    # The black slot is the left texture island used by the lever base.
    for y in range(5, 15):
        for x in range(5, 7):
            red, green, blue, alpha = pixels[x, y]
            pixels[x, y] = (242, 130, 24, alpha)

    output.parent.mkdir(parents=True, exist_ok=True)
    image.save(output)


def main() -> None:
    parser = argparse.ArgumentParser(description="Create the active orange-center lever texture.")
    parser.add_argument("source", type=Path, help="Source Lever_Texture.png.")
    parser.add_argument("output", type=Path, help="Output PNG for the active lever state.")
    args = parser.parse_args()

    make_on_texture(args.source, args.output)


if __name__ == "__main__":
    main()
