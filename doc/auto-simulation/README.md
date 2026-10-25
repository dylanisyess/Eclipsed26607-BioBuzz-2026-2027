# PracticeAuto path estimate

With the user's clarified orientation (the back of the robot leads), the
current code DOES turn left first and right second. The previous advice to
swap turn motor commands was based on the wrong orientation and is withdrawn.
The turn durations still produce large angles in the illustrative scenarios
below; the first turn is not a small left correction in this ideal model.

No robot Java files were changed. Shooting is treated as a stationary delay.
PracticePark.java was empty when inspected.

## What was run

Run from the repository root:

```powershell
python doc/auto-simulation/simulate_practice_auto.py
# Replace 14 with measured distance between left/right wheel contact centers:
python doc/auto-simulation/simulate_practice_auto.py --track-in 14
```

The script extracts literal left/right power commands and sleeps from the
current PracticeAuto.java. It integrates a differential-drive kinematic model
and writes `simulation-results.csv` and `practice-auto-path.svg` beside itself.
It is not an FTC SDK execution, a collision simulation, or a validated digital
model of this robot. It does not model acceleration, braking distance, battery
sag, wheel slip, motor imbalance, or tire scrub during turns.

Inputs and assumptions:

- User-supplied wheel diameter: 90 mm (circumference approximately 11.13 in).
- Nominal 20:1 UltraPlanetary: approximately 18.9:1 actual reduction for the
  4:1 and 5:1 cartridge combination. Assumed 1:1 drive after the gearbox.
- HD Hex bare-motor free speed: 6000 RPM at 12 V. This gives a theoretical
  no-load wheel speed of approximately 58.90 in/s, not a floor-speed measurement.
- Wheel spacing: **approximately 14 in**, supplied by the user.
- Front-to-back wheel spacing: **approximately 12 in**, supplied by the user.
  This is not necessarily the robot's full body length. It does not enter the
  ideal differential-drive equations, but affects tire scrub on a four-wheel
  tank drive. No scrub coefficient or measured turn rate is available.
- Floor speed scenarios: **60%, 70%, and 80% of theoretical free speed**.
  These are sensitivity examples, not measured values or confidence bounds.
- Wheel speed is proportional to command power, with instant speed changes.
  Zero power holds a wheel stationary. Actual BRAKE behavior is not a position
  servo, so that last assumption is especially approximate for pivot turns.
- The user confirms the robot's back leads and the current turn directions
  are correct. The simulator therefore maps named Robot.left to the right side
  relative to travel, and Robot.right to the left side. Positive commands move
  along the intended direction of travel. The default run uses this mapping;
  `--front-leading` switches back to the earlier orientation assumption.
  CSV power columns are explicitly travel-relative, not Java motor names.
- Coordinates describe the ideal drive center: x positive right of the initial
  heading, y positive forward, angle positive left. Start is (0, 0), facing +y.
  The origin is not the rear bumper or the wall.

For wheel velocities vL and vR and wheel spacing B:

```text
forward speed = (vL + vR) / 2
turn rate    = (vR - vL) / B
```

The integrator uses exact circular-arc motion for each constant-power interval.
The stationary-wheel turns move the robot center; they are not turns in place.

## Results with 14-inch wheel spacing

| Motion in current code | 60% speed scenario | 70% speed scenario | 80% speed scenario |
|---|---:|---:|---:|
| Forward, 0.5 power, 650 ms | 11.5 in | 13.4 in | 15.3 in |
| Named left motor only, 0.7 power, 800 ms | 81.0 deg left | 94.5 deg left | 108.0 deg left |
| Forward, full power, 750 ms | 26.5 in | 30.9 in | 35.3 in |
| Named right motor only, 0.7 power, 500 ms | 50.6 deg right | 59.1 deg right | 67.5 deg right |
| Forward, full power, 1500 ms | 53.0 in | 61.8 in | 70.7 in |
| Final x, right-positive coordinates | -63.8 in | -80.5 in | -96.2 in |
| Final y, forward of start | 71.7 in | 71.3 in | 66.9 in |
| Final heading, left of original travel direction | 30.4 deg | 35.4 deg | 40.5 deg |

Scheduled sleeps total 11.15 seconds, including shooting and startup delays.
Execution overhead is additional. The speed scenarios alone shift the endpoint
by over 32 inches laterally. This illustrates why a timer-only route needs
physical calibration and cannot be certified from motor specifications.

All three SVG curves now show the unchanged Java commands with the confirmed
rear-leading orientation. The middle-speed case ends 80.5 inches LEFT and 71.3
inches forward of the starting point. This is not a validated parking route.

## Changes to make

1. Keep the turn motor commands as written for the confirmed rear-leading
   orientation: first named left=0.7/right=0, then named left=0/right=0.7.
2. Choose target angles from measured waypoints and substantially shorten the
   first turn if the intention is a small correction. In the 70%/14-inch
   example, a 20-degree stationary-wheel pivot at 0.7 power takes about 169 ms;
   30 degrees takes about 254 ms. These are starting estimates, not tuned values.
   Both turn durations must be selected for their desired headings. The current
   unequal 800/500 ms turns leave a 35-degree net left heading in this example.
   If the final drive should be parallel to the initial drive, the two turn
   angles must cancel; an IMU can enforce that more reliably than equal times.
3. Measure each travel distance and use encoder targets for straight legs;
   use a configured IMU for turn heading. PracticeRobot currently selects
   RUN_WITHOUT_ENCODER, and the autonomous never reads encoder or IMU feedback.
   Changing the run mode alone will not add distance/heading control.
4. For this gearbox/wheel assumption, start calibration at approximately
   `28 * 18.9 / (PI * (90 / 25.4)) = 47.54 counts/inch`.
   Include any extra sprocket/gearing ratio. Command a measured 24-inch travel,
   then correct counts/inch by `old_value * commanded_distance / measured_distance`.
5. If keeping timing for initial practice, measure distance traveled at each
   actual power setting with the assembled robot. Measure turn angle at the
   actual turning power too; straight-line speed does not fully predict a
   skid-steer turn. Reduce speed for the final approach and retune duration to
   preserve the needed travel distance.
6. Add stop checks between steps, use opModeIsActive() in movement loops, and
   put all motor shutdowns in a finally block so later steps do not execute
   after a stop request or leave mechanisms powered after an early exit.

## What is still needed to check parking

The user says the route is the same on both alliance sides and supplies the
wheel spacings and rear-leading orientation. The exact start tile, robot's full
footprint, drive-center offset from the wall-contacting end, and target-zone
coordinates still have not been established. The left-wheel alignment identifies
an offset only once the tile and the meaning of wheel alignment (center or
outer edge, viewed along travel or along the nominal robot front) are known.

FIRST's BIOBUZZ V1 manual describes an approximately 23 by 11 inch loading
zone. No such zone or obstacle is placed in this relative simulation. There is
no collision or scoring verdict; an unobstructed kinematic curve can extend
through a wall or hive. A field test and measured geometry are needed to verify
the intended parking maneuver.

## Sources

- [REV UltraPlanetary manual, Appendix A: actual gear ratios](https://revrobotics.ca/content/docs/REV-41-1600-UM.pdf)
- [REV HD Hex motor: 6000 RPM and 28 encoder counts per motor revolution](https://revrobotics.global/REV-41-1291/)
- [FIRST BIOBUZZ Competition Manual V1: field zones](https://ftc-resources.firstinspires.org/ftc/archive/2027/game/cm-html/BIOBUZZ%20Competition%20Manual%20-%20V1.htm)

The FIRST manual's indexed text was available, but direct retrieval returned
HTTP 403. The field drawings and any subsequent manual changes were not
verified. The simulation does not depend on inferred field coordinates.
