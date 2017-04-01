package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannelController;


@Autonomous(name="SPI_Testbed")
public class SPI_Testbed extends LinearOpMode {


    DigitalChannel NCS;
    DigitalChannel MISO;
    DigitalChannel MOSI;
    DigitalChannel SCLK;
    DigitalChannel RST;

    public void runOpMode() {


        NCS = hardwareMap.digitalChannel.get("NCS");
        NCS.setMode(DigitalChannelController.Mode.OUTPUT);

        MISO = hardwareMap.digitalChannel.get("MISO");
        MISO.setMode(DigitalChannelController.Mode.INPUT);

        MOSI = hardwareMap.digitalChannel.get("MOSI");
        MOSI.setMode(DigitalChannelController.Mode.OUTPUT);

        SCLK = hardwareMap.digitalChannel.get("SCLK");
        SCLK.setMode(DigitalChannelController.Mode.OUTPUT);

        RST = hardwareMap.digitalChannel.get("RST");
        RST.setMode(DigitalChannelController.Mode.OUTPUT);


        telemetry.addData("SPI_Testbed", "Channel mode initialization completed, press START to begin MISO/MOSI speed test");
        telemetry.update();

        waitForStart();

        telemetry.addData("SPI_Testbed", "Running, please wait...");
        telemetry.update();


        long start = System.currentTimeMillis();
        long count = 0;

        while (System.currentTimeMillis() < start + 1000) {

            MOSI.setState(true);
            assert MISO.getState() == true;

            MOSI.setState(false);
            assert MISO.getState() == false;

            count += 2;

        }

        telemetry.addData("SPI_Testbed", "Done! Ran " + count + " write/read cycles in 1000ms");
        telemetry.update();

        while (true) {}

    }

}
