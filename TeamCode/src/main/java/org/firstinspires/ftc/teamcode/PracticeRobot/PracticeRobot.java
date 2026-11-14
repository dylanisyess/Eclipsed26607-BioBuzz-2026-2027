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
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.PID;

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

    // The last number is the lowest power that still moves the robot. Too low and it stops short,
    // too high and it rocks back and forth at the end
    private final PID alignPID = new PID(0.025, 0, 0, 0.35);
    private final PID turnPID = new PID(0.012, 0, 0, 0.35);
    private final PID drivePID = new PID(0.015, 0, 0, 0.3);

    public DcMotor left, right, intake;
    private IMU imu;
    private double targetHeading;

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
        left.setPower(forward + TURN_DIRECTION * turn);
        right.setPower(forward - TURN_DIRECTION * turn);
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