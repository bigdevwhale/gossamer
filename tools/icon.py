"""Draws the launcher icon (an orb web with one firefly caught in it) as vector drawables, and docs/logo.png.

python tools/icon.py
"""
import math
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DRAW = ROOT / "app/src/main/res/drawable"

BG = "#FF141B26"
SILK = "#FFEDE8DC"
AMBER = "#FFF2B85B"
GLOW = "#55F2B85B"
SPOKES = 12
CX, CY, R = 54.0, 52.0, 30.0


def rim(i):
    return 1.0 + 0.06 * math.sin(i * 1.9 + 0.5)


def pt(i, r):
    a = i * 2 * math.pi / SPOKES - math.pi / 2 + 0.2
    return CX + math.cos(a) * r * R, CY + math.sin(a) * r * R


def web_paths():
    spokes = " ".join(f"M{CX:.2f},{CY:.2f}L{x:.2f},{y:.2f}" for x, y in (pt(i, rim(i)) for i in range(SPOKES)))
    rings = []
    k = 0.22
    while k < 1.0:
        pts = [pt(i, k * rim(i)) for i in range(SPOKES)]
        rings.append("M" + " L".join(f"{x:.2f},{y:.2f}" for x, y in pts) + "Z")
        k += 0.17
    rimp = "M" + " L".join(f"{x:.2f},{y:.2f}" for x, y in (pt(i, rim(i)) for i in range(SPOKES))) + "Z"
    return spokes, " ".join(rings), rimp


def firefly():
    x, y = pt(2, 0.62)
    return x, y


def circle(x, y, r):
    return f"M{x - r:.2f},{y:.2f}a{r:.2f},{r:.2f} 0,1 1,{2 * r:.2f},0a{r:.2f},{r:.2f} 0,1 1,{-2 * r:.2f},0z"


def main():
    DRAW.mkdir(parents=True, exist_ok=True)
    head = ('<?xml version="1.0" encoding="utf-8"?>\n<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
            '    android:width="108dp"\n    android:height="108dp"\n    android:viewportWidth="108"\n    android:viewportHeight="108">\n')
    spokes, rings, rimp = web_paths()
    fx, fy = firefly()

    def fg(mono=False):
        silk = "#FFFFFFFF" if mono else SILK
        out = [
            f'    <path android:strokeColor="{silk}" android:strokeAlpha="0.85" android:strokeWidth="1.1" android:pathData="{spokes}" />',
            f'    <path android:strokeColor="{silk}" android:strokeAlpha="0.7" android:strokeWidth="0.9" android:fillColor="#00000000" android:pathData="{rings}" />',
            f'    <path android:strokeColor="{silk}" android:strokeWidth="1.2" android:fillColor="#00000000" android:pathData="{rimp}" />',
        ]
        if not mono:
            out.append(f'    <path android:fillColor="{GLOW}" android:pathData="{circle(fx, fy, 7)}" />')
        out.append(f'    <path android:fillColor="{"#FFFFFFFF" if mono else AMBER}" android:pathData="{circle(fx, fy, 3.2)}" />')
        return "\n".join(out)

    (DRAW / "ic_launcher_foreground.xml").write_text(head + fg() + "\n</vector>\n", encoding="utf-8", newline="\n")
    (DRAW / "ic_launcher_monochrome.xml").write_text(head + fg(True) + "\n</vector>\n", encoding="utf-8", newline="\n")
    (DRAW / "ic_launcher_background.xml").write_text(head + f'    <path android:fillColor="{BG}" android:pathData="M0,0h108v108h-108z" />\n</vector>\n', encoding="utf-8", newline="\n")

    try:
        from PIL import Image, ImageDraw, ImageFilter
        s = 512 / 72  # the visible part of the adaptive icon is the middle 72 dp
        off = 18

        def m(x, y):
            return (x - off) * s, (y - off) * s
        img = Image.new("RGBA", (512, 512), (20, 27, 38, 255))
        d = ImageDraw.Draw(img)
        silk = (237, 232, 220, 220)
        for i in range(SPOKES):
            d.line([m(CX, CY), m(*pt(i, rim(i)))], fill=silk, width=6)
        k = 0.22
        while k < 1.0:
            pts = [m(*pt(i, k * rim(i))) for i in range(SPOKES)]
            d.line(pts + [pts[0]], fill=(237, 232, 220, 180), width=5)
            k += 0.17
        pts = [m(*pt(i, rim(i))) for i in range(SPOKES)]
        d.line(pts + [pts[0]], fill=silk, width=7)
        glow = Image.new("RGBA", (512, 512), (0, 0, 0, 0))
        gx, gy = m(fx, fy)
        ImageDraw.Draw(glow).ellipse([gx - 60, gy - 60, gx + 60, gy + 60], fill=(242, 184, 91, 150))
        img = Image.alpha_composite(img, glow.filter(ImageFilter.GaussianBlur(26)))
        d = ImageDraw.Draw(img)
        d.ellipse([gx - 23, gy - 23, gx + 23, gy + 23], fill=(242, 184, 91, 255))
        (ROOT / "docs").mkdir(exist_ok=True)
        img.save(ROOT / "docs/logo.png")
    except ImportError:
        pass


if __name__ == "__main__":
    main()
