package org.firstinspires.ftc.teamcode;

public class PID {

    private double kP, kI, kD;
    private double integral;
    private double prevError;
    private double prevTime;
    private double integralLim = 0.25;
    private double PIDMin = 0.1;
    private double PID;

    public PID(double kP, double kI, double kD) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;

        this.integral = 0;
        this.prevError = 0;
        this.prevTime = System.nanoTime() / 1e9;
    }

    public double calculate(double error) {
        double currentTime = System.nanoTime() / 1e9;
        double dt = currentTime - prevTime;
        prevTime = currentTime;

        integral += error * dt;

        double derivative = (dt > 0) ? (error - prevError) / dt : 0;

        prevError = error;

        double integralSum = kI * integral;

        if (integralSum > integralLim) {
            integralSum = integralLim;
        }
        if (integralSum < -integralLim) {
            integralSum = -integralLim;
        }

        PID = kP * error + integralSum + kD * derivative;

        if (PID < PIDMin && PID > -PIDMin) {
            if (PID > 0) {
                PID = PIDMin;
            } else if (PID < 0) {
                PID = -PIDMin;
            }
        }

        return PID;
    }

    public void reset() {
        integral = 0;
        prevError = 0;
        prevTime = System.nanoTime() / 1e9;
    }
}