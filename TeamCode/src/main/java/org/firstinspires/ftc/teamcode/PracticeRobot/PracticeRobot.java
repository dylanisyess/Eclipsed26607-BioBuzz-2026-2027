package org.firstinspires.ftc.teamcode.PracticeRobot;;

import static java.lang.Thread.sleep;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotor.RunMode;
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior;
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.util.PID;

import java.util.Arrays;
import java.util.List;

public class PracticeRobot {
    // Park where shots score, read "Forward distance" from telemetry, and put it here
    public static final double ALIGN_DISTANCE = 58; // cm
    private static final double ALIGN_TOLERANCE = 2; // cm
    private static final double ALIGN_MAX_POWER = 0.5;
    // Teleop forward is negative power. Flip this if the robot drives away from the target
    private static final double ALIGN_DIRECTION = -1;

    // HD Hex 28 ticks/rev through the real ratios of the "4:1" (76/21) and "5:1" (68/13) cartridges, then 1:1 chain
    private static final double TICKS_PER_WHEEL_REV = 28 * (76.0 / 21) * (68.0 / 13);
    private static final double WHEEL_DIAMETER = 9; // cm
    private static final double CM_PER_TICK = Math.PI * WHEEL_DIAMETER / TICKS_PER_WHEEL_REV;

    // The left encoder counts down on positive power. Flip this if driveDistance runs until it times out
    private static final double LEFT_ENCODER_DIRECTION = -1;

    // Flip this if the robot spins away from the target heading
    private static final double TURN_DIRECTION = 1;

    private static final double TURN_TOLERANCE = 2; // deg
    private static final double TURN_MAX_POWER = 0.5;
    private static final double TURN_TIMEOUT = 3; // seconds
    private static final double DRIVE_TOLERANCE = 1; // cm
    private static final double DRIVE_MAX_POWER = 0.6;
    private static final double DRIVE_TIMEOUT = 5; // seconds
    private static final double SETTLE_TIME = 0.15; // seconds
    private static final double HEADING_HOLD_KP = 0.02;
    private static final double HEADING_HOLD_MAX_POWER = 0.2;

    // Field centric teleop. Raise the kP if it's slow to point where the stick says, lower it if it wobbles
    private static final double FIELD_TURN_KP = 0.02;
    private static final double FIELD_TURN_MAX_POWER = 0.6;
    private static final double FIELD_STICK_DEADBAND = 0.1;
    // How far past 90 deg the stick has to be before the robot switches between driving front first and back first.
    // Stops it flipping back and forth when the stick points straight out the robot's side
    private static final double FIELD_REVERSE_HYSTERESIS = 20; // deg

    // The last number is the lowest power that still moves the robot. Too low and it stops short,
    // too high and it rocks back and forth at the end
    private final PID alignPID = new PID(0.025, 0, 0, 0.35);
    private final PID turnPID = new PID(0.012, 0, 0, 0.35);
    private final PID drivePID = new PID(0.015, 0, 0, 0.3);

    public DcMotor left, right, intake;
    private IMU imu;
    private double targetHeading;
    private boolean fieldReversed;
    private boolean fieldStickIdle = true;

    public PracticeShooter shooter = new PracticeShooter();

    private List<DcMotor> motors;

    public ElapsedTime runtime = new ElapsedTime();
    boolean intaking;

    public void init(HardwareMap hardwareMap) {
        left = hardwareMap.get(DcMotor.class, "left");
        right = hardwareMap.get(DcMotor.class, "right");
        intake = hardwareMap.get(DcMotor.class, "intake");
        shooter.init(hardwareMap);
        motors = Arrays.asList(left, right);

        left.setDirection(Direction.REVERSE);
        right.setDirection(Direction.FORWARD);
        intake.setDirection(Direction.REVERSE);

        for (DcMotor motor : motors) {
            motor.setZeroPowerBehavior(ZeroPowerBehavior.BRAKE);
            motor.setMode(RunMode.RUN_WITHOUT_ENCODER);
        }

        // With the logo up, yaw is the same whichever side the USB ports face
        imu = hardwareMap.get(IMU.class, "imu");
        imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(LogoFacingDirection.UP, UsbFacingDirection.LEFT)));
    }

    // Call right after waitForStart so heading 0 is the way the robot starts
    public void resetHeading() {
        imu.resetYaw();
        targetHeading = 0;
    }

    // Degrees, counterclockwise is positive
    public double getHeading() {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
    }

    public double getLeftDistance() {
        return LEFT_ENCODER_DIRECTION * left.getCurrentPosition() * CM_PER_TICK;
    }

    public double getRightDistance() {
        return right.getCurrentPosition() * CM_PER_TICK;
    }

    private double getHeadingError() {
        return AngleUnit.normalizeDegrees(targetHeading - getHeading());
    }

    // Positive turn spins the robot counterclockwise
    private void setDrivePower(double forward, double turn) {
        double leftPower = forward + TURN_DIRECTION * turn;
        double rightPower = forward - TURN_DIRECTION * turn;

        // Scale down together so a hard turn at full speed still turns instead of both sides clipping at 1
        double max = Math.max(1, Math.max(Math.abs(leftPower), Math.abs(rightPower)));
        left.setPower(leftPower / max);
        right.setPower(rightPower / max);
    }

    // Field centric teleop for a tank chassis. It can't strafe, so it turns to face the way the stick points
    // and drives that way, front first or back first, whichever is closer. Driving slows down the further off
    // it's pointing so it doesn't swing wide. x and y are the stick with right and up positive, away from the
    // driver is the way the robot faced at resetHeading. turn spins in place (counterclockwise positive)
    // and only works while the stick is centered
    public void driveFieldCentric(double x, double y, double turn) {
        double magnitude = Math.min(1, Math.hypot(x, y));

        if (magnitude < FIELD_STICK_DEADBAND) {
            fieldStickIdle = true;
            setDrivePower(0, turn);
            return;
        }

        // Same as the IMU, 0 is away from the driver and counterclockwise is positive
        double stickHeading = Math.toDegrees(Math.atan2(-x, y));
        double frontError = AngleUnit.normalizeDegrees(stickHeading - getHeading());
        double backError = AngleUnit.normalizeDegrees(frontError + 180);

        if (fieldStickIdle) {
            // Fresh push, use whichever end is closer
            fieldReversed = Math.abs(frontError) > 90;
            fieldStickIdle = false;
        } else if (fieldReversed && Math.abs(backError) > 90 + FIELD_REVERSE_HYSTERESIS) {
            fieldReversed = false;
        } else if (!fieldReversed && Math.abs(frontError) > 90 + FIELD_REVERSE_HYSTERESIS) {
            fieldReversed = true;
        }

        double error = fieldReversed ? backError : frontError;
        double turnPower = Range.clip(FIELD_TURN_KP * error, -FIELD_TURN_MAX_POWER, FIELD_TURN_MAX_POWER);
        double speed = magnitude * Math.max(0, Math.cos(Math.toRadians(error)));
        if (fieldReversed) {
            speed = -speed;
        }

        // Forward for the driver is negative motor power
        setDrivePower(-speed, turnPower);
    }

    // Blocks until the robot reaches the heading or times out
    public void turnToHeading(LinearOpMode opMode, double heading) {
        targetHeading = heading;
        turnPID.reset();

        ElapsedTime timeout = new ElapsedTime();
        ElapsedTime settled = new ElapsedTime();

        while (opMode.opModeIsActive() && timeout.seconds() < TURN_TIMEOUT && settled.seconds() < SETTLE_TIME) {
            double error = getHeadingError();
            double power = Range.clip(turnPID.calculate(error), -TURN_MAX_POWER, TURN_MAX_POWER);

            if (Math.abs(error) > TURN_TOLERANCE) {
                settled.reset();
                setDrivePower(0, power);
            } else {
                setDrivePower(0, 0);
            }
        }

        setDrivePower(0, 0);
    }

    // Blocks until the robot has gone the distance or times out. Positive distance drives
    // the way positive motor power does. Holds the last heading given to turnToHeading
    public void driveDistance(LinearOpMode opMode, double distance) {
        drivePID.reset();

        // Left encoder only, the right motor's encoder is broken. The IMU keeps both sides going the same distance.
        // Once it's replaced, go back to averaging (left + right) / 2
        double leftStart = getLeftDistance();
        ElapsedTime timeout = new ElapsedTime();
        ElapsedTime settled = new ElapsedTime();

        while (opMode.opModeIsActive() && timeout.seconds() < DRIVE_TIMEOUT && settled.seconds() < SETTLE_TIME) {
            double traveled = getLeftDistance() - leftStart;
            double error = distance - traveled;
            double power = Range.clip(drivePID.calculate(error), -DRIVE_MAX_POWER, DRIVE_MAX_POWER);

            if (Math.abs(error) > DRIVE_TOLERANCE) {
                settled.reset();
                // Not the PID class, its minimum power would make the robot wobble when nearly straight
                double correction = Range.clip(HEADING_HOLD_KP * getHeadingError(), -HEADING_HOLD_MAX_POWER, HEADING_HOLD_MAX_POWER);
                setDrivePower(power, correction);
            } else {
                setDrivePower(0, 0);
            }
        }

        setDrivePower(0, 0);
    }

    public void intake() {
        if (intaking == false)  {
            intake.setPower(1);
            intaking = true;
        }
        else  {
            intake.setPower(0);
            intaking = false;
        }
    }

    // Drives straight forward or back only. The driver still has to aim the robot at the hive.
    // Call it every loop. Returns true once the robot is within tolerance
    public boolean alignToDistance(double distance) {
        double error = distance - ALIGN_DISTANCE;
        double power = 0;

        if (Math.abs(error) > ALIGN_TOLERANCE) {
            power = Range.clip(alignPID.calculate(error), -ALIGN_MAX_POWER, ALIGN_MAX_POWER);
        }

        left.setPower(ALIGN_DIRECTION * -power);
        right.setPower(ALIGN_DIRECTION * -power);
        return power == 0;
    }

    public void shootHard() {
        shooter.setPower(1);
    }

    public void shootSoft() {
        shooter.setPower(0.5);
    }

public void shootZero() {
        shooter.setPower(0);
    }


}

//