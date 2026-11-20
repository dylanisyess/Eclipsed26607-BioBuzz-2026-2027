package org.firstinspires.ftc.teamcode.swerve;


import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Robot;

@TeleOp(name="SwerveDebug", group="Debug")
public class swervedebug extends LinearOpMode {

    private final Robot Robot = new Robot();

    @Override
    public void runOpMode() {
        Robot.init(hardwareMap);

        SwerveModule fl = new SwerveModule(Robot.frontLeft, Robot.frontLeftServo, Robot.frontLeftLamprey, 0.0025, 0, 0.0004, 0);
        SwerveModule fr = new SwerveModule(Robot.frontRight, Robot.frontRightServo, Robot.frontRightLamprey, 0.0025, 0, 0.0004, 0);
        SwerveModule bl = new SwerveModule(Robot.backLeft, Robot.backLeftServo, Robot.backLeftLamprey, 0.0025, 0, 0.0004, 0);
        SwerveModule br = new SwerveModule(Robot.backRight, Robot.backRightServo, Robot.backRightLamprey, 0.0025, 0, 0.0004, 0);

        waitForStart();

        while (opModeIsActive()) {

            double targetAngle = 0;
            if (gamepad1.a) targetAngle = 0;
            if (gamepad1.b) targetAngle = 90;
            if (gamepad1.x) targetAngle = 180;
            if (gamepad1.y) targetAngle = 270;

            SwerveModuleState state = new SwerveModuleState(0, targetAngle);

            fl.setDesiredState(state);
            fr.setDesiredState(state);
            bl.setDesiredState(state);
            br.setDesiredState(state);

            telemetry.addData("TargetAngle", targetAngle);
            telemetry.addData("module state speed", state.speed);
            telemetry.addData("module state angle", state.angle);
            telemetry.addData("FL angle", fl.getCurrentAngleDeg());
            telemetry.addData("FR angle", fr.getCurrentAngleDeg());
            telemetry.addData("BL angle", bl.getCurrentAngleDeg());
            telemetry.addData("BR angle", br.getCurrentAngleDeg());
            telemetry.addData("FL angle error", fl.angleErrorDeg(state.angle, fl.getCurrentAngleDeg()));
            telemetry.addData("FR angle error", fr.angleErrorDeg(state.angle, fr.getCurrentAngleDeg()));
            telemetry.addData("BL angle error", bl.angleErrorDeg(state.angle, bl.getCurrentAngleDeg()));
            telemetry.addData("BR angle error", br.angleErrorDeg(state.angle, br.getCurrentAngleDeg()));
            telemetry.update();
        }
    }
}
