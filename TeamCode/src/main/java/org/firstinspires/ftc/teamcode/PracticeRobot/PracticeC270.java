package org.firstinspires.ftc.teamcode.PracticeRobot;

import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;

import java.util.ArrayList;
import java.util.List;

public class PracticeC270 {
    // How far the camera is tilted up from level. Measure this on the robot
    public static final double CAMERA_TILT_DEG = 0;

    private AprilTagProcessor aprilTagProcessor;
    private VisionPortal visionPortal;
    private List<AprilTagSingleDetection> detectedTags = new ArrayList<>();
    private List<AprilTagClusterDetection> detectedClusters = new ArrayList<>();
    private Telemetry telemetry;

    public void init(HardwareMap hwMap, Telemetry telemetry) {
        this.telemetry = telemetry;

        aprilTagProcessor = new AprilTagProcessor.Builder()
                .setDrawTagID(true)
                .setDrawTagOutline(true)
                .setDrawAxes(true)
                .setDrawCubeProjection(true)
                .setOutputUnits(DistanceUnit.CM, AngleUnit.DEGREES)
                .build();

        VisionPortal.Builder builder = new VisionPortal.Builder();
        builder.setCamera(hwMap.get(WebcamName.class, "Webcam 1"));
        builder.setCameraResolution(new Size(640, 480));
        builder.addProcessor(aprilTagProcessor);

        visionPortal = builder.build();
    }

    public void update() {
        // SDK 12: each group of 4 hive tags comes back as one cluster detection, never as single tags
        detectedTags = new ArrayList<>();
        detectedClusters = new ArrayList<>();
        for (AprilTagDetection detection : aprilTagProcessor.getDetections()) {
            if (detection instanceof AprilTagSingleDetection) {
                detectedTags.add((AprilTagSingleDetection) detection);
            } else {
                detectedClusters.add((AprilTagClusterDetection) detection);
            }
        }
    }

    public void displayDetectionTelemetry(AprilTagSingleDetection detectedId) {
        if (detectedId == null) {return;}

        if (detectedId.metadata != null) {
            telemetry.addLine(String.format("\n==== (ID %d) %s", detectedId.id, detectedId.metadata.name));
            telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (cm)", detectedId.ftcPose.x, detectedId.ftcPose.y, detectedId.ftcPose.z));
            telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)", detectedId.ftcPose.pitch, detectedId.ftcPose.roll, detectedId.ftcPose.yaw));
            telemetry.addLine(String.format("RBE %6.1f %6.1f %6.1f  (cm, deg, deg)", detectedId.ftcPose.range, detectedId.ftcPose.bearing, detectedId.ftcPose.elevation));
        } else {
            telemetry.addLine(String.format("\n==== (ID %d) Unknown", detectedId.id));
            telemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", detectedId.center.x, detectedId.center.y));
        }
    }

    public void displayClusterTelemetry(AprilTagClusterDetection cluster) {
        telemetry.addLine(String.format("\n==== %s (%d%% of tags found)", cluster.metadata.name, cluster.percentClusterFound));
        telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (cm)", cluster.ftcPose.x, cluster.ftcPose.y, cluster.ftcPose.z));
        telemetry.addLine(String.format("Roll %6.1f  (deg)", cluster.ftcPose.roll));
        telemetry.addLine(String.format("Forward distance %6.1f  (cm)", getForwardDistance(cluster)));
    }

    public List<AprilTagSingleDetection> getDetectedTags() {
        return detectedTags;
    }

    public List<AprilTagClusterDetection> getDetectedClusters() {
        return detectedClusters;
    }

    // alliance is "RED" or "BLUE". Upside-down clusters (|roll| >= 90) are on cells that can't be scored in
    public AprilTagClusterDetection getScorableCluster(String alliance) {
        for (AprilTagClusterDetection cluster : detectedClusters) {
            if (cluster.metadata.name.startsWith(alliance) && Math.abs(cluster.ftcPose.roll) < 90) {
                return cluster;
            }
        }
        return null;
    }

    // Distance along the floor from the camera to the cell opening, in the direction the robot is facing
    public double getForwardDistance(AprilTagClusterDetection cluster) {
        double tilt = Math.toRadians(CAMERA_TILT_DEG);
        return cluster.ftcPose.y * Math.cos(tilt) - cluster.ftcPose.z * Math.sin(tilt);
    }

    public AprilTagSingleDetection getTagBySpecificId(int id) {
        for (AprilTagSingleDetection detection : detectedTags) {
            if (detection.id == id){
                return detection;
            }
        }
        return null;
    }

    public void stop() {
        if (visionPortal != null) {
            visionPortal.close();
        }
    }
}
