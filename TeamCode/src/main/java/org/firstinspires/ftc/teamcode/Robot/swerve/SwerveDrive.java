package org.firstinspires.ftc.teamcode.swerve;

public class SwerveDrive {
    private SwerveModule fl, fr, bl, br;
    private SwerveKinematics kinematics;
    private boolean field_centric = true;
    private java.util.function.DoubleSupplier headingSupplier;

    public SwerveDrive(SwerveModule fl,
                       SwerveModule fr,
                       SwerveModule bl,
                       SwerveModule br,
                       double trackLength,
                       double trackWidth) {

        this.fl = fl;
        this.fr = fr;
        this.bl = bl;
        this.br = br;

        this.kinematics = new SwerveKinematics(trackLength, trackWidth);
    }

    public void drive (double vx, double vy, double omega) {
        SwerveModuleState[] states = kinematics.toModuleStates(vx, vy, omega);
        fl.setDesiredState(states[0]);
        fr.setDesiredState(states[1]);
        bl.setDesiredState(states[2]);
        br.setDesiredState(states[3]);

    }
}
