package org.firstinspires.ftc.teamcode.PracticeRobot;

import com.qualcomm.robotcore.hardware.DcMotor.RunMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

public class PracticeShooter {
    // Untuned estimates for a bare UltraPlanetary (2800 ticks/s with no load)
    // Replace these with the values from PracticeShooterTuner
    public static final double P = 30;
    public static final double F = 12;
    public static final double TARGET_VELOCITY = 2000; // ticks per second
//max = 2200 ticks / sec
    private DcMotorEx shooter;

    public void init(HardwareMap hwMap) {
        shooter = hwMap.get(DcMotorEx.class, "shooter");
        shooter.setDirection(Direction.FORWARD);
        setPIDF(P, F);
    }

    // PIDF values only last for the current OpMode, so init() has to set them every time
    public void setPIDF(double p, double f) {
        shooter.setPIDFCoefficients(RunMode.RUN_USING_ENCODER, new PIDFCoefficients(p, 0, 0, f));
    }

    // Closed loop, the hub keeps holding this speed between calls
    public void setVelocity(double ticksPerSecond) {
        setMode(RunMode.RUN_USING_ENCODER);
        shooter.setVelocity(ticksPerSecond);
    }

    // Open loop, for backing the shooter off and for letting it coast to a stop
    public void setPower(double power) {
        setMode(RunMode.RUN_WITHOUT_ENCODER);
        shooter.setPower(power);
    }

    public double getVelocity() {
        return shooter.getVelocity();
    }

    private void setMode(RunMode mode) {
        if (shooter.getMode() != mode) {
            shooter.setMode(mode);
        }
    }
}
