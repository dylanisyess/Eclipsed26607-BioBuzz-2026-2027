"""Estimate PracticeAuto's differential-drive path; Python standard library only.

Not an FTC runtime/physics simulator. Defaults to the user's rear-leading
orientation: named left motor is on the right when looking along travel.
Assumes positive commands move toward +y initially, speed proportional to power, no slip, direct
1:1 gearbox-to-wheel drive, and a stationary braked wheel at zero power.
Parses literal setPower/sleep statements in this simple sequential Java opmode.
Does not support loops, branches, variables, or arbitrary Java programs.
"""

import argparse
import csv
import math
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "TeamCode/src/main/java/org/firstinspires/ftc/teamcode/PracticeRobot/PracticeAuto.java"


def read_commands():
    source = SOURCE.read_text()
    source = re.sub(r"/\*.*?\*/|//[^\n]*", "", source, flags=re.S)
    pattern = r"Robot\.(left|right)\.setPower\(([\d.]+)\)|sleep\((\d+)\)"
    left = right = 0.0
    commands = []
    for match in re.finditer(pattern, source):
        side, power, milliseconds = match.groups()
        if side == "left":
            left = float(power)
        elif side == "right":
            right = float(power)
        else:
            commands.append((left, right, float(milliseconds) / 1000))
    return commands


def advance(x, y, heading, left_distance, right_distance, track):
    """x right, y forward from start; heading positive counterclockwise/left."""
    angle = (right_distance - left_distance) / track
    distance = (right_distance + left_distance) / 2
    if abs(angle) < 1e-12:
        x -= distance * math.sin(heading)
        y += distance * math.cos(heading)
    else:
        radius = distance / angle
        x += radius * (math.cos(heading + angle) - math.cos(heading))
        y += radius * (math.sin(heading + angle) - math.sin(heading))
    return x, y, heading + angle


def simulate(commands, speed, track, swap=False):
    """swap maps named motor sides to travel-relative sides; not a code edit."""
    x = y = heading = elapsed = 0.0
    path = [(x, y)]
    rows = []
    for left, right, duration in commands:
        if swap:
            left, right = right, left
        previous_heading = heading
        steps = max(1, math.ceil(duration / 0.01))
        for _ in range(steps):
            x, y, heading = advance(x, y, heading,
                                    left * speed * duration / steps,
                                    right * speed * duration / steps, track)
            path.append((x, y))
        elapsed += duration
        if left or right:
            rows.append(dict(end_s=elapsed, travel_left_power=left, travel_right_power=right,
                             duration_s=duration, x_right_in=x / .0254,
                             y_forward_in=y / .0254,
                             heading_left_deg=math.degrees(heading),
                             turn_left_deg=math.degrees(heading - previous_heading),
                             center_travel_in=(left + right) / 2 * speed * duration / .0254))
    return path, rows


def plot_svg(cases, destination, track_in, orientation):
    # A relative path diagram, deliberately omitting unverified field geometry.
    points = [(x / .0254, y / .0254) for _, _, path in cases for x, y in path]
    xmin = math.floor((min(p[0] for p in points) - 12) / 24) * 24
    xmax = math.ceil((max(p[0] for p in points) + 12) / 24) * 24
    ymin = math.floor((min(p[1] for p in points) - 12) / 24) * 24
    ymax = math.ceil((max(p[1] for p in points) + 12) / 24) * 24
    scale = min(800 / (xmax - xmin), 500 / (ymax - ymin))
    width, height = (xmax - xmin) * scale, (ymax - ymin) * scale
    def xy(x, y):
        return 90 + (x / .0254 - xmin) * scale, 100 + (ymax - y / .0254) * scale
    svg = ['<svg xmlns="http://www.w3.org/2000/svg" width="1000" height="820" viewBox="0 0 1000 820">',
           '<rect width="1000" height="820" fill="#fafbfc"/>',
           '<g font-family="Arial, sans-serif" fill="#172b4d">',
           '<text x="40" y="35" font-size="24">PracticeAuto: estimated path from starting axle center</text>',
           f'<text x="40" y="65" font-size="15">90 mm wheels | 18.9:1 | approx. {track_in:g} in track | {orientation} | no slip or acceleration</text>']
    for xx in range(xmin, xmax + 1, 24):
        px, _ = xy(xx * .0254, 0)
        svg.append(f'<path d="M {px} 100 v {height}" stroke="#dbe2ea"/>')
        svg.append(f'<text x="{px}" y="{height+125}" text-anchor="middle" font-size="12">{xx}</text>')
    for yy in range(ymin, ymax + 1, 24):
        _, py = xy(0, yy * .0254)
        svg.append(f'<path d="M 90 {py} h {width}" stroke="#dbe2ea"/>')
        svg.append(f'<text x="78" y="{py+4}" text-anchor="end" font-size="12">{yy}</text>')
    for label, color, path in cases:
        coords = ' '.join(f'{a:.2f},{b:.2f}' for a, b in (xy(x, y) for x, y in path))
        svg.append(f'<polyline points="{coords}" fill="none" stroke="{color}" stroke-width="3"/>')
        a, b = xy(*path[-1])
        svg.append(f'<circle cx="{a}" cy="{b}" r="5" fill="{color}"/>')
    sx, sy = xy(0, 0)
    svg.append(f'<circle cx="{sx}" cy="{sy}" r="6" fill="#172b4d"/><text x="{sx+10}" y="{sy-9}" font-size="14">Start; facing up</text>')
    svg.append(f'<text x="{90+width/2}" y="{height+150}" text-anchor="middle" font-size="15">Left (-) / right (+) of start, inches</text>')
    svg.append('<text x="25" y="370" transform="rotate(-90 25 370)" text-anchor="middle" font-size="15">Forward from start, inches</text>')
    for index, (label, color, _) in enumerate(cases):
        y = 675 + index * 25
        svg.append(f'<path d="M 55 {y-5} h 30" stroke="{color}" stroke-width="4"/><text x="100" y="{y}" font-size="15">{label}</text>')
    svg.append('<text x="40" y="795" font-size="14">Grid spacing: 24 in; not a field map. No wall, hive, robot footprint, or parking checks.</text></g></svg>')
    destination.write_text('\n'.join(svg), encoding='utf-8')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--track-in', type=float, default=14)
    parser.add_argument('--front-leading', action='store_true',
                        help='Use named left/right as travel-relative sides instead of the confirmed rear-leading setup')
    args = parser.parse_args()
    if args.track_in <= 0:
        parser.error('--track-in must be positive')
    commands = read_commands()
    free_speed = 6000 / 18.9 / 60 * math.pi * .090
    destination = Path(__file__).resolve().parent
    cases, all_rows = [], []
    orientation = 'front leading' if args.front_leading else 'back leading (confirmed)'
    print(f'Orientation: {orientation}; positive heading = left relative to travel')
    for factor, color in [(0.6, '#e69b16'), (0.7, '#d94841'), (0.8, '#993eab')]:
        path, rows = simulate(commands, free_speed * factor, args.track_in * .0254,
                              swap=not args.front_leading)
        cases.append((f'Current code: {factor:.0%} of theoretical free speed (assumed)', color, path))
        for row in rows:
            all_rows.append(dict(case=f'current_{factor:.1f}', **row))
        print(f'\nSpeed factor {factor:.0%}: {free_speed*factor/.0254:.2f} in/s at full power')
        for number, row in enumerate(rows, 1):
            print(f"  Move {number}: travel {row['center_travel_in']:.1f} in; turn {row['turn_left_deg']:+.1f} deg; "
                  f"position ({row['x_right_in']:.1f}, {row['y_forward_in']:.1f}) in; heading {row['heading_left_deg']:+.1f} deg")
    plot_svg(cases, destination / 'practice-auto-path.svg', args.track_in, orientation)
    with (destination / 'simulation-results.csv').open('w', newline='') as handle:
        writer = csv.DictWriter(handle, fieldnames=list(all_rows[0]))
        writer.writeheader()
        writer.writerows(all_rows)
    print(f'\nTotal scheduled time: {sum(c[2] for c in commands):.2f} s')
    print(f'Theoretical free speed: {free_speed/.0254:.2f} in/s')
    print(f'Approximate encoder counts/in: {28*18.9/(math.pi*.090/.0254):.2f}')


if __name__ == '__main__':
    main()
