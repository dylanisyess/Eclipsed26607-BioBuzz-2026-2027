package org.firstinspires.ftc.teamcode.Robot;;

import static java.lang.Thread.sleep;

import android.content.Context;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotor.RunMode;
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior;
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.hardware.AnalogInput;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

import java.util.Arrays;
import java.util.List;

public class Robot {
    public DcMotor frontLeft, frontRight, backLeft, backRight;
    public CRServo frontLeftServo, frontRightServo, backLeftServo, backRightServo;
    public AnalogInput frontLeftEncoder, frontRightEncoder, backLeftEncoder, backRightEncoder;
    double flOffsetDeg, frOffsetDeg, blOffsetDeg, brOffsetDeg;
    public DcMotor intake;
    public DcMotorEx shooter;
    public DigitalChannel limitSwitch;
    private List<DcMotor> motors;
    private Context _appContext;
    public ElapsedTime runtime = new ElapsedTime();
    double L = 200;
    double W = 200;
    double R = 282.84;
    double FWD, STR, RCW, A, B, C, D, FRA, FLA, BLA, BRA, FRS, FLS, BLS, BRS, max;
    boolean neg, intaking, shooting;
    public static final double AlignDist = 58;
    public static final double IntakePower = 58;



    IMU imu;
    private static final double MAX_VELOCITY = 2800d;
    private static final double COUNTS_PER_MOTOR_REV = 146.44d;    // eg: HD Hex Motor 20:1 560, core hex 288, 40:1 1120
    private static final double DRIVE_GEAR_REDUCTION = 1.33d;     // This is < 1.0 if geared UP, eg. 26d/10d
    private static final double WHEEL_DIAMETER_INCHES = 2.3622d;     // For figuring circumference
    static final double COUNTS_PER_INCH = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * 3.14159265359d);
    double gripPosition, handPosition;
    int handMode;

    public void init(HardwareMap hardwareMap) {
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");
        motors = Arrays.asList(frontLeft, frontRight, backLeft, backRight);

        frontLeftServo = hardwareMap.get(CRServo.class, "frontLeftServo");
        frontRightServo = hardwareMap.get(CRServo.class, "frontRightServo");
        backLeftServo = hardwareMap.get(CRServo.class, "backLeftServo");
        backRightServo = hardwareMap.get(CRServo.class, "backRightServo");

        frontLeftEncoder = hardwareMap.get(AnalogInput.class, "frontLeftLamprey");
        frontRightEncoder = hardwareMap.get(AnalogInput.class, "frontRightLamprey");
        backLeftEncoder = hardwareMap.get(AnalogInput.class, "backLeftLamprey");
        backRightEncoder = hardwareMap.get(AnalogInput.class, "backRightLamprey");

        intake = hardwareMap.get(DcMotor.class, "intake");
        shooter = hardwareMap.get(DcMotorEx.class, "shooter");

        frontLeft.setDirection(Direction.REVERSE);
        frontRight.setDirection(Direction.FORWARD);
        backLeft.setDirection(Direction.REVERSE);
        backRight.setDirection(Direction.FORWARD);
        intake.setDirection(Direction.REVERSE);
        shooter.setDirection(Direction.REVERSE);

        frontLeft.setZeroPowerBehavior(ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(ZeroPowerBehavior.BRAKE);

        imu = hardwareMap.get(IMU.class, "imu");
        imu.initialize(new IMU.Parameters(
                new RevHubOrientationOnRobot(LogoFacingDirection.LEFT, UsbFacingDirection.UP)));
        imu.resetYaw();

        _appContext = hardwareMap.appContext;

        for (DcMotor motor : motors) {
            motor.setZeroPowerBehavior(ZeroPowerBehavior.BRAKE);
            motor.setMode(RunMode.RUN_WITHOUT_ENCODER);
        }

    }


    double getHeading() {
        YawPitchRollAngles orientation = imu.getRobotYawPitchRollAngles();
        return orientation.getYaw(AngleUnit.DEGREES);
    }


}

