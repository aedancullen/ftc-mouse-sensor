package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.io.IOException;

/* Written by Aedan Cullen. */


public class ADNS3080 {

    HardwareMap hardwareMap;

    DigitalChannel NCS;
    DigitalChannel MISO;
    DigitalChannel MOSI;
    DigitalChannel SCLK;
    DigitalChannel RST;


    private byte ADNS3080_PRODUCT_ID =          0x00;
    private byte ADNS3080_MOTION =              0x02;
    private byte ADNS3080_DELTA_X =             0x03;
    private byte ADNS3080_DELTA_Y =             0x04;
    private byte ADNS3080_SQUAL =               0x05;
    private byte ADNS3080_CONFIGURATION_BITS =  0x0A;
    private byte ADNS3080_MOTION_CLEAR =        0x12;
    private byte ADNS3080_FRAME_CAPTURE =       0x13;
    private byte ADNS3080_MOTION_BURST =        0x50;


    private byte ADNS3080_PRODUCT_ID_VALUE =    0x17;


    private int dx;
    private int dy;
    private int squal;


    public ADNS3080(HardwareMap hardwareMap) throws IOException {
        this.hardwareMap = hardwareMap;

        NCS = hardwareMap.digitalChannel.get("NCS");
        NCS.setMode(DigitalChannelController.Mode.OUTPUT);
        NCS.setState(true);

        MISO = hardwareMap.digitalChannel.get("MISO");
        MISO.setMode(DigitalChannelController.Mode.INPUT);

        MOSI = hardwareMap.digitalChannel.get("MOSI");
        MOSI.setMode(DigitalChannelController.Mode.OUTPUT);

        SCLK = hardwareMap.digitalChannel.get("SCLK");
        SCLK.setMode(DigitalChannelController.Mode.OUTPUT);
        SCLK.setState(true);

        RST = hardwareMap.digitalChannel.get("RST");
        RST.setMode(DigitalChannelController.Mode.OUTPUT);
        RST.setState(false);

        reset();
        if (!verifySensor()) {
            throw new IOException("ADNS3080 product ID verification failed");
        }
    }

    public boolean verifySensor() {
        byte[] ret = spiRead(ADNS3080_PRODUCT_ID, 1);
        return ret[0] == ADNS3080_PRODUCT_ID_VALUE;
    }


    public void updateSensor() {
        byte[] ret = spiRead(ADNS3080_MOTION_BURST, 4);
        byte motion = ret[0];
        this.dx = ret[1];
        this.dy = ret[2];
        this.squal = ret[3];
    }



    public int getDx() {
        return this.dx;
    }

    public int getDy() {
        return this.dy;
    }

    public int getSqual() {
        return this.squal;
    }



    private void delayMicroseconds(int micros) {
        try {
            Thread.sleep(0, micros * 1000);
        }
        catch (InterruptedException e) {}
    }

    private void reset() {
        RST.setState(true);
        delayMicroseconds(10);
        RST.setState(false);
        delayMicroseconds(500);

    }


    // --------------------------------------------------------------------------------------------------
    // Software SPI implementation follows. Is currently only compatible with the ADNS3080's SPI Mode 3.
    // (clock is normally high, data bit assertions occur on the clock falling edge) i.e. CPOL=1, CPHA=1.
    // --------------------------------------------------------------------------------------------------



    private void spiWrite(byte reg, byte[] writeData, int length) {
        SCLK.setState(true);
        NCS.setState(false);
        byte dataGoingOut;

        // ------------ REG ADDR WRITE -------------------
        dataGoingOut = (byte)(reg | 0x80);
        for (int i = 0; i < 8; i++) {
            SCLK.setState(false); // boom - clock the data out
            if ((dataGoingOut & 0xff) >> 7 == 1) { // if MSB is 1
                MOSI.setState(true);
            }
            else {
                MOSI.setState(false);
            }
            SCLK.setState(true); // (smaller boom)
            dataGoingOut = (byte)(dataGoingOut << 1); // Discard most-significant bit and proceed
        }

        delayMicroseconds(75);

        // ------------ DATA BYTES WRITE -------------------
        for (int byteIndex = 0; byteIndex < length; byteIndex++) {
            dataGoingOut = writeData[byteIndex];
            for (int i = 0; i < 8; i++) {
                SCLK.setState(false); // boom - clock the data out
                if ((dataGoingOut & 0xff) >> 7 == 1) { // if MSB is 1
                    MOSI.setState(true);
                }
                else {
                    MOSI.setState(false);
                }
                SCLK.setState(true); // (smaller boom)
                dataGoingOut = (byte)(dataGoingOut << 1); // Discard most-significant bit and proceed
            }
        }

        NCS.setState(true);

    }

    private byte[] spiRead(byte reg, int length) {
        SCLK.setState(true);
        NCS.setState(false);
        byte dataGoingOut;
        byte dataComingIn;

        // ------------ REG ADDR WRITE -------------------
        dataGoingOut = reg; // note lack of a 0x80 bitwise or here
        for (int i = 0; i < 8; i++) {
            SCLK.setState(false); // boom - clock the data out
            if ((dataGoingOut & 0xff) >> 7 == 1) { // if MSB is 1
                MOSI.setState(true);
            }
            else {
                MOSI.setState(false);
            }
            SCLK.setState(true); // (smaller boom)
            dataGoingOut = (byte)(dataGoingOut << 1); // Discard most-significant bit and proceed
        }

        delayMicroseconds(75);
        byte[] outputBuffer = new byte[length];

        // ------------ DATA BYTES READ -------------------
        for (int byteIndex = 0; byteIndex < length; byteIndex++) {
            dataComingIn = (byte)(0x00);
            for (int i = 0; i < 8; i++) {
                SCLK.setState(false);
                if (MISO.getState()) { // if received MSB is 1
                    dataComingIn |= 1;
                }
                SCLK.setState(true);
                dataComingIn = (byte)(dataComingIn << 1);
            }
            outputBuffer[byteIndex] = dataComingIn;
        }

        NCS.setState(true);

        return outputBuffer;
    }


}
