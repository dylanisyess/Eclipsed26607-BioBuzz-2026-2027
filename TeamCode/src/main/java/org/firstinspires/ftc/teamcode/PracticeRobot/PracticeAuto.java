package org.firstinspires.ftc.teamcode.PracticeRobot;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="PracticeAuto", group="Linear OpMode")

public class PracticeAuto extends LinearOpMode {
    private final PracticeRobot Robot = new PracticeRobot();
    double timer;

    double vx, vy, omega;

    public void runOpMode() {
        Robot.init(hardwareMap);

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();

        Robot.shooter.setPower(1);

        sleep(1000);

        Robot.intake.setPower(1);

        sleep(5000);

        Robot.shooter.setPower(0);
        Robot.intake.setPower(0);

        sleep(1000);

        Robot.left.setPower(0);
        Robot.right.setPower(0.5);

        sleep(500);

        Robot.left.setPower(1);
        Robot.right.setPower(1);

        sleep(4000);

        Robot.left.setPower(0);
        Robot.right.setPower(0);

    }
}