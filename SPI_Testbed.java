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

    DigitalChannel NCS_VER;
    DigitalChannel MOSI_VER;
    DigitalChannel SCLK_VER;


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



        NCS_VER = hardwareMap.digitalChannel.get("NCS_VER");
        NCS_VER.setMode(DigitalChannelController.Mode.INPUT);

        MOSI_VER = hardwareMap.digitalChannel.get("MOSI_VER");
        MOSI_VER.setMode(DigitalChannelController.Mode.INPUT);

        SCLK_VER = hardwareMap.digitalChannel.get("SCLK_VER");
        SCLK_VER.setMode(DigitalChannelController.Mode.INPUT);



        telemetry.addData("SPI_Testbed", "Bopper bop bopping bopped, bop BOP to bop MISO/MOSI bopper");
        telemetry.update();

        waitForStart();

        telemetry.addData("SPI_Testbed", "Boppin, please bop...");
        telemetry.update();


        long start = System.currentTimeMillis();
        long count = 0;

        while (System.currentTimeMillis() < start + 1000) {

            MOSI.setState(true);
            while (MOSI_VER.getState() != true) {}

            MOSI.setState(false);
            while (MOSI_VER.getState() != false) {}

            count += 2;

        }

        telemetry.addData("SPI_Testbed", "Bop! Bopped " + count + " boppers in 1000ms");
        telemetry.update();

        while (true) {}

    }

}
