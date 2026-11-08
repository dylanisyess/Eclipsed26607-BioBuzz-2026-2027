package org.firstinspires.ftc.teamcode.PracticeRobot;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;

@TeleOp(name="Practiceteleop", group="Linear OpMode")

public class PracticeTeleop extends LinearOpMode {
    private static final String ALLIANCE = "RED"; // "RED" or "BLUE"

    private final PracticeRobot Robot = new PracticeRobot();
    private final PracticeC270 C270 = new PracticeC270();

    double left, right, omega, intakePower;
    boolean reverse;


    enum shooterState {
        idle,
        backwards,
        shooter,
        intake
    }

    enum driveState {
        forward,
        reverse
    }

    ElapsedTime time = new ElapsedTime();
    shooterState currentState = shooterState.idle;
    driveState driverState = driveState.forward;

    public void runOpMode() {
        Robot.init(hardwareMap);
        C270.init(hardwareMap, telemetry);
        telemetry.update();

        Robot.intaking = false;
        reverse = false;

        telemetry.addData("Status", "Initialized");
        telemetry.update();
        waitForStart();

        Robot.runtime.reset();

        while (opModeIsActive()) {
            left = gamepad1.left_stick_y;
            right = gamepad1.right_stick_y;
            omega = gamepad1.right_stick_x;

            C270.update();
            AprilTagClusterDetection target = C270.getScorableCluster(ALLIANCE);

            // Hold X to align. Drops back to manual driving if the target is lost
            if (gamepad1.x && target != null) {
                Robot.alignToDistance(C270.getForwardDistance(target));
            } else {
                switch (driverState) {
                    case forward:
                        Robot.left.setPower(left);
                        Robot.right.setPower(right);
                        if (gamepad1.right_bumper) {
                            driverState = driveState.reverse;
                        }
                        break;
                    case reverse:
                        Robot.left.setPower(-right);
                        Robot.right.setPower(-left);
                        if (gamepad1.left_bumper) {
                            driverState = driveState.forward;
                        }
                        break;
                }
            }

            if (target != null) {
                C270.displayClusterTelemetry(target);
                telemetry.addData("Align target", "%.1f cm", PracticeRobot.ALIGN_DISTANCE);
            } else {
                telemetry.addData("Align target", "not found");
            }

            switch (currentState) {
                case idle:
                    Robot.shooter.setPower(0);
                    if (gamepad1.dpad_up) {
                        time.reset();
                        currentState = shooterState.backwards;
                    }
                    break;
                case backwards:
                    Robot.shooter.setPower(-0.4);
                    if (time.seconds() >= 1) {
                        time.reset();
                        currentState = shooterState.shooter;
                    }
                    break;
                case shooter:
//                    Robot.shooter.setVelocity(PracticeShooter.TARGET_VELOCITY);
                    Robot.shooter.setPower(1);
                    if (time.seconds() >= 3) {
                        time.reset();
                        currentState = shooterState.intake;
                    }
                    break;
                case intake:
                    Robot.intake.setPower(0.4);
                    if (time.seconds() >= 3) {
                        time.reset();
                        currentState = shooterState.idle;
                    }
                    break;
            }

            if (gamepad1.y) {
                intakePower = 1;
            }

            if (gamepad1.a) {
                intakePower = 0;
            }

            // The intake state controls the intake while it's feeding
            if (currentState != shooterState.intake) {
                Robot.intake.setPower(intakePower);
            }

            if (gamepad1.dpad_down) {
                currentState = shooterState.idle;
            }

            telemetry.update();
        }

        C270.stop();
    }
}