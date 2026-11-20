package org.firstinspires.ftc.teamcode.Robot.swerve;

import static java.lang.Math.sqrt;

public class SwerveKinematics {

    private final double L; // length
    private final double W; //width

    public SwerveKinematics(double trackLength, double trackWidth) {
        this.L = trackLength;
        this.W = trackWidth;
    }

    public SwerveModuleState[] toModuleStates(double vx, double vy, double omega) {

        double R = sqrt(L*L + W*W);
        double A = vx - omega * (L / R);
        double B = vx + omega * (L / R);
        double C = vy - omega * (W / R);
        double D = vy + omega * (W / R);

        SwerveModuleState fl = new SwerveModuleState(
                Math.hypot(B, D),
                Math.toDegrees(Math.atan2(B, D))
        );
        SwerveModuleState fr = new SwerveModuleState(
                Math.hypot(B, C),
                Math.toDegrees(Math.atan2(B, C))
        );
        SwerveModuleState bl = new SwerveModuleState(
                Math.hypot(A, D),
                Math.toDegrees(Math.atan2(A, D))
        );
        SwerveModuleState br = new SwerveModuleState(
                Math.hypot(A, C),
                Math.toDegrees(Math.atan2(A, C))
        );

        double max = Math.max(
                Math.max(fl.speed, fr.speed),
                Math.max(bl.speed, br.speed)
        );

        if (max > 1.0) {
            fl.speed /= max;
            fr.speed /= max;
            bl.speed /= max;
            br.speed /= max;
        }

        return new SwerveModuleState[] { fl, fr, bl, br };
    }
}
