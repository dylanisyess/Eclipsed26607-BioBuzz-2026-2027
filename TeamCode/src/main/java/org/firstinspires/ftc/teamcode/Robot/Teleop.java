package org.firstinspires.ftc.teamcode.Robot;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Robot.swerve.SwerveDrive;
import org.firstinspires.ftc.teamcode.Robot.swerve.SwerveModule;
import org.firstinspires.ftc.teamcode.util.C270;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;

@TeleOp(name="teleop", group="Linear OpMode")

public class Teleop extends LinearOpMode {
    private static final String ALLIANCE = "RED"; // "RED" or "BLUE"

    private final Robot Robot = new Robot();
    private final C270 C270 = new C270();

    double vx, vy, omega, intakePower;
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

        final SwerveModule fl = new SwerveModule(Robot.frontLeft, Robot.frontLeftServo, Robot.frontLeftEncoder, 0.0025, 0, 0.0004, 0);
        final SwerveModule fr = new SwerveModule(Robot.frontRight, Robot.frontRightServo, Robot.frontRightEncoder, 0.0025, 0, 0.0004, 0);
        final SwerveModule bl = new SwerveModule(Robot.backLeft, Robot.backLeftServo, Robot.backLeftEncoder, 0.0025, 0, 0.0004, 0);
        final SwerveModule br = new SwerveModule(Robot.backRight, Robot.backRightServo, Robot.backRightEncoder, 0.0025, 0, 0.0004, 0);
        private final SwerveDrive SwerveDrive = new SwerveDrive(fl, fr, bl, br, 1, 1);

        telemetry.update();

        Robot.intaking = false;
        reverse = false;

        telemetry.addData("Status", "Initialized");
        telemetry.update();
        waitForStart();

        Robot.runtime.reset();

        while (opModeIsActive()) {
            vx = gamepad1.left_stick_x;
            -vy = gamepad1.left_stick_y;
            omega = gamepad1.right_stick_x;

            C270.update();
            AprilTagClusterDetection target = C270.getScorableCluster(ALLIANCE);

            if (gamepad1.x && target != null) {
//                Robot.alignToDistance(C270.getForwardDistance(target));
            } else {
                switch (driverState) {
                    case forward:
                        SwerveDrive.drive(vx, vy, omega);
                        break;
                }
            }

            if (target != null) {
                C270.displayClusterTelemetry(target);
                telemetry.addData("Align target", "%.1f cm", Robot.AlignDist);
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
                    Robot.shooter.setVelocity(Shooter.TARGET_VELOCITY);
//                    Robot.shooter.setPower(0.9);
                    if (time.seconds() >= 2.5) {
                        time.reset();
                        currentState = shooterState.intake;
                    }
                    break;
                case intake:
                    Robot.intake.setPower(0.3);
                    if (time.seconds() >= 4) {
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

            if (currentState != shooterState.intake) {
                Robot.intake.setPower(intakePower);
            }

            if (gamepad1.dpad_down) {
                currentState = shooterState.idle;
            }

            telemetry.addData("Shooter velocity", "%.0f / %.0f ticks/s", Robot.shooter.getVelocity(), Shooter.TARGET_VELOCITY);
            telemetry.update();
        }

        C270.stop();
    }
}