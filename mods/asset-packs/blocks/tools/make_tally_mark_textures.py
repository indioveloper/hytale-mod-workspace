from __future__ import annotations

import json
import math
from pathlib import Path

from PIL import Image, ImageDraw


ICON_SIZE = 32
ICON_SCALE = 4
MARK_X = (3.5, 6.5, 9.5, 12.5)
MARK_ANGLES = (-2.0, 1.0, -1.0, 2.0)


def scaled(point: tuple[int, int]) -> tuple[int, int]:
    return point[0] * ICON_SCALE, point[1] * ICON_SCALE


def draw_scratch(draw: ImageDraw.ImageDraw, start: tuple[int, int], end: tuple[int, int]) -> None:
    sx, sy = scaled(start)
    ex, ey = scaled(end)
    draw.line((sx, sy, ex, ey), fill=(52, 47, 39, 190), width=4)


def make_tally(count: int) -> Image.Image:
    canvas = Image.new("RGBA", (ICON_SIZE * ICON_SCALE, ICON_SIZE * ICON_SCALE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(canvas)
    icon_x = (9, 13, 17, 21)
    for x in icon_x[: min(count, 4)]:
        draw_scratch(draw, (x, 10), (x, 22))
    if count == 5:
        draw_scratch(draw, (7, 20), (23, 12))
    return canvas.resize((ICON_SIZE, ICON_SIZE), Image.Resampling.LANCZOS)


def orientation(angle_degrees: float) -> dict[str, float]:
    half_angle = math.radians(angle_degrees) / 2.0
    return {"x": 0, "y": 0, "z": round(math.sin(half_angle), 6), "w": round(math.cos(half_angle), 6)}


def mark_node(node_id: int, x: float, angle: float, height: int) -> dict[str, object]:
    return {
        "id": str(node_id),
        "name": "Tally scratch",
        "children": [],
        "position": {"x": x, "y": 7.5, "z": -15.2},
        "orientation": orientation(angle),
        "shape": {
            "type": "quad",
            "offset": {"x": 0, "y": 0, "z": 0},
            "stretch": {"x": 1, "y": 1, "z": 1},
            "settings": {"size": {"x": 1, "y": height}, "normal": "+Z"},
            "visible": True,
            "doubleSided": True,
            "shadingMode": "standard",
            "unwrapMode": "custom",
            "textureLayout": {
                "front": {
                    "offset": {"x": 4, "y": 2},
                    "mirror": {"x": False, "y": False},
                    "angle": 0,
                }
            },
        },
    }


def make_model(count: int) -> dict[str, object]:
    nodes = [mark_node(index, MARK_X[index], MARK_ANGLES[index], 7) for index in range(min(count, 4))]
    if count == 5:
        nodes.append(mark_node(4, 8.0, -58.0, 12))
    return {"lod": "auto", "nodes": nodes}


def make_scratch_texture() -> Image.Image:
    image = Image.new("RGBA", (16, 16), (52, 47, 39, 105))
    pixels = image.load()
    for y in range(16):
        for x in range(16):
            variation = ((x * 3 + y * 5) % 5) - 2
            pixels[x, y] = (52 + variation, 47 + variation, 39 + variation, 105)
    return image


def main() -> None:
    pack = Path(__file__).resolve().parents[1]
    textures = pack / "Common" / "Blocks" / "OrbGenesis" / "TallyMarks"
    icons = pack / "Common" / "Icons" / "ItemsGenerated"
    textures.mkdir(parents=True, exist_ok=True)
    icons.mkdir(parents=True, exist_ok=True)

    for stale in textures.glob("Tally_Marks_[1-5].png"):
        stale.unlink()

    stale_model = textures / "Tally_Marks.blockymodel"
    if stale_model.exists():
        stale_model.unlink()

    make_scratch_texture().save(textures / "Tally_Marks_Scratch.png")

    images = []
    for count in range(1, 6):
        image = make_tally(count)
        images.append(image)
        model_path = textures / f"Tally_Marks_{count}.blockymodel"
        model_path.write_text(json.dumps(make_model(count), indent=2) + "\n", encoding="ascii")

    icon = images[-1].resize((64, 64), Image.Resampling.NEAREST)
    icon.save(icons / "OrbGenesis_Tally_Marks.png")


if __name__ == "__main__":
    main()
