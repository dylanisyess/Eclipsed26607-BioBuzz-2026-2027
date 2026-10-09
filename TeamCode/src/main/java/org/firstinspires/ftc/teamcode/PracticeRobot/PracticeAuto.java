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

        Robot.left.setPower(0.6);
        Robot.right.setPower(0.6);
        sleep(2000);
        Robot.left.setPower(0);
        Robot.right.setPower(0);


    }
}