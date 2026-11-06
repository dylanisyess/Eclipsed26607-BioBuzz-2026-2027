package org.firstinspires.ftc.teamcode.PracticeRobot;;

import static java.lang.Thread.sleep;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection;
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
    public static final double ALIGN_DISTANCE = 120; // cm
    private static final double ALIGN_TOLERANCE = 2; // cm
    private static final double ALIGN_MAX_POWER = 0.4;
    // Teleop forward is negative power. Flip this if the robot drives away from the target
    private static final double ALIGN_DIRECTION = -1;

    private final PID alignPID = new PID(0.008, 0, 0);

    public DcMotor left, right, intake;

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

    // Drives straight forward or back only. The driver still has to aim the robot at the hive
    public void alignToDistance(double distance) {
        double error = distance - ALIGN_DISTANCE;
        double power = 0;

        if (Math.abs(error) > ALIGN_TOLERANCE) {
            power = Range.clip(alignPID.calculate(error), -ALIGN_MAX_POWER, ALIGN_MAX_POWER);
        }

        left.setPower(ALIGN_DIRECTION * -power);
        right.setPower(ALIGN_DIRECTION * -power);
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