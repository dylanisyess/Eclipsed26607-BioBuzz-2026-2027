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
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

import java.util.Arrays;
import java.util.List;

public class PracticeRobot {
    public DcMotor left, right, intake;

    public DcMotorEx shooter;

    private List<DcMotor> motors;

    public ElapsedTime runtime = new ElapsedTime();
    boolean intaking;

    public void init(HardwareMap hardwareMap) {
        left = hardwareMap.get(DcMotor.class, "Left");
        right = hardwareMap.get(DcMotor.class, "Right");
        intake = hardwareMap.get(DcMotor.class, "Intake");
        shooter = hardwareMap.get(DcMotorEx.class, "shooter");
        motors = Arrays.asList(left, right);

        left.setDirection(Direction.REVERSE);
        right.setDirection(Direction.FORWARD);
        intake.setDirection(Direction.REVERSE);
        shooter.setDirection(Direction.REVERSE);

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

    public void shootHard() {
        shooter.setTargetPosition(96);
        shooter.setPower(1);
        while (shooter.isBusy()) {

        }
        shooter.setPower(0);
        shooter.setTargetPosition(0);
        shooter.setPower(0.5);
        while (shooter.isBusy()) {

        }
        shooter.setPower(0);
    }

    public void shootSoft() {
        shooter.setTargetPosition(96);
        shooter.setPower(0.5);
        while (shooter.isBusy()) {

        }
        shooter.setPower(0);
        shooter.setTargetPosition(0);
        shooter.setPower(0.5);
        while (shooter.isBusy()) {

        }
        shooter.setPower(0);
    }

}

//