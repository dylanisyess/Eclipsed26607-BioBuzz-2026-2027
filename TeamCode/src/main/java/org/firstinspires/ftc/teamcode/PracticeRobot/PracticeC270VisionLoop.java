package org.firstinspires.ftc.teamcode.PracticeRobot;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.PracticeRobot.PracticeC270;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;

@Autonomous(name="PracticeC270VisionLoops", group="OpMode")
public class PracticeC270VisionLoop extends OpMode {
    PracticeC270 C270 = new PracticeC270();

    @Override
    public void init(){
        C270.init(hardwareMap, telemetry);
    }

    @Override
    public void init_loop(){
        // Camera Stream on the DS only works during INIT, so show tag data here too
        telemetry.addData("DS preview", "3 dots -> Camera Stream (tap image to refresh)");
        loop();
    }

    @Override
    public void loop(){
        C270.update();
        AprilTagSingleDetection id35 = C270.getTagBySpecificId(35);
        telemetry.addData("Tags seen", C270.getDetectedTags().size());
        C270.displayDetectionTelemetry(id35);
    }

    @Override
    public void stop(){
        C270.stop();
    }
}
