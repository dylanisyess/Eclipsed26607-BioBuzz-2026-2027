package org.firstinspires.ftc.teamcode.PracticeRobot;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name="Practiceteleop", group="Linear OpMode")

public class PracticeTeleop extends LinearOpMode {
    private final PracticeRobot Robot = new PracticeRobot();

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

            switch (currentState) {
                case idle:
                    Robot.shooter.setPower(0);
                    Robot.intake.setPower(0);
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
                    Robot.shooter.setPower(1);
                    if (time.seconds() >= 2.5) {
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

            Robot.intake.setPower(intakePower);

            if (gamepad1.dpad_down) {
                currentState = shooterState.idle;
            }

            telemetry.update();
        }
    }
}