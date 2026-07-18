from __future__ import annotations

import argparse
from pathlib import Path

from PIL import Image


def make_outline(source: Path, output: Path, border: int, alpha: int) -> None:
    image = Image.open(source).convert("RGBA")
    width, height = image.size
    pixels = image.load()

    for y in range(height):
        for x in range(width):
            in_border = x < border or y < border or x >= width - border or y >= height - border
            r, g, b, a = pixels[x, y]
            pixels[x, y] = (r, g, b, min(a, alpha)) if in_border else (0, 0, 0, 0)

    output.parent.mkdir(parents=True, exist_ok=True)
    image.save(output)


def main() -> None:
    parser = argparse.ArgumentParser(description="Create a transparent block-face outline from a Hytale block texture.")
    parser.add_argument("source", type=Path, help="Source block texture PNG.")
    parser.add_argument("output", type=Path, help="Output outline texture PNG.")
    parser.add_argument("--border", type=int, default=2, help="Border thickness in pixels.")
    parser.add_argument("--alpha", type=int, default=115, help="Outline alpha, 0-255.")
    args = parser.parse_args()

    make_outline(args.source, args.output, args.border, args.alpha)


if __name__ == "__main__":
    main()
