package org.firstinspires.ftc.teamcode.PracticeRobot;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;

@Autonomous(name="PracticeAuto", group="Linear OpMode")

public class PracticeAuto extends LinearOpMode {
    private final PracticeRobot Robot = new PracticeRobot();
    private final PracticeC270 C270 = new PracticeC270();
    private static final String ALLIANCE = "RED"; // "RED" or "BLUE"
    private static final double ALIGN_TIMEOUT = 3; // seconds
    private static final double ALIGN_SETTLE = 0.3; // seconds
    double timer;

    double vx, vy, omega;

    public void runOpMode() {
        Robot.init(hardwareMap);
        C270.init(hardwareMap, telemetry);

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();

        Robot.resetHeading();

        sleep(500);

        Robot.driveDistance(this, 32);

        sleep(200);

        alignWithCamera();

        sleep(200);

        shoot();

        sleep(500);

        Robot.turnToHeading(this, 45);

        sleep(200);

        Robot.driveDistance(this, 143);

        sleep(300);

        Robot.turnToHeading(this, 0);

        sleep(300);

        Robot.driveDistance(this, 110);

        C270.stop();
    }

    // Drives until the camera reads ALIGN_DISTANCE. If the target isn't seen, waits for it
    // until the timeout, then carries on from wherever the encoders put the robot
    private void alignWithCamera() {
        ElapsedTime timeout = new ElapsedTime();
        ElapsedTime settled = new ElapsedTime();

        while (opModeIsActive() && timeout.seconds() < ALIGN_TIMEOUT && settled.seconds() < ALIGN_SETTLE) {
            C270.update();
            AprilTagClusterDetection target = C270.getScorableCluster(ALLIANCE);

            if (target == null) {
                settled.reset();
                Robot.left.setPower(0);
                Robot.right.setPower(0);
                telemetry.addData("Align", "target not found");
            } else {
                double distance = C270.getForwardDistance(target);
                if (!Robot.alignToDistance(distance)) {
                    settled.reset();
                }
                telemetry.addData("Align", "%.1f / %.1f cm", distance, PracticeRobot.ALIGN_DISTANCE);
            }
            telemetry.update();
        }

        Robot.left.setPower(0);
        Robot.right.setPower(0);
    }

    // Same steps as the teleop shooter states: back off, spin up, then feed
    private void shoot() {
        Robot.shooter.setPower(-0.4);
        sleep(1000);

        Robot.shooter.setVelocity(PracticeShooter.TARGET_VELOCITY);
        sleep(2500);

        Robot.intake.setPower(0.3);
        sleep(4000);

        Robot.intake.setPower(0);
        Robot.shooter.setPower(0);
    }
}
