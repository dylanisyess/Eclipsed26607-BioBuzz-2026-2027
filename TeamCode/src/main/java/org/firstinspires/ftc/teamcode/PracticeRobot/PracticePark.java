package org.firstinspires.ftc.teamcode.PracticeRobot;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="PracticePark", group="Linear OpMode")

public class PracticePark extends LinearOpMode {
    private final PracticeRobot Robot = new PracticeRobot();
    double timer;

    double vx, vy, omega;

    public void runOpMode() {
        Robot.init(hardwareMap);

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();

        sleep(500);

        Robot.driveDistance(this, 32);
        
    }
}