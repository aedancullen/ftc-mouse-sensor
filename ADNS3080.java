package org.firstinspires.ftc.teamcode;

import android.util.Log;

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

    DigitalChannel NCS_VER;
    DigitalChannel MOSI_VER;
    DigitalChannel SCLK_VER;


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


        NCS.setState(true);
        while (NCS_VER.getState() != true) {}
        SCLK.setState(true);
        while (SCLK_VER.getState() != true) {}


        reset();
        if (!verifySensor()) {
            throw new IOException("ADNS3080 product ID verification failed");
        }
    }

    public boolean verifySensor() {
        byte[] ret = spiRead(ADNS3080_PRODUCT_ID, 1);
        return ret[0] == ADNS3080_PRODUCT_ID_VALUE;
    }

    public void clearSensor() {
        spiWrite(ADNS3080_MOTION_CLEAR, new byte[]{(byte)(0xff)}, 1);
        this.dx = 0;
        this.dy = 0;
        this.squal = 0;
    }


    public void updateSensor() {
        byte[] ret;

        ret = spiRead(ADNS3080_DELTA_X, 1);
        byte x = ret[0];
        ret = spiRead(ADNS3080_DELTA_Y, 1);
        byte y = ret[0];

        this.dx = x;
        this.dy = y;
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

    private void delayMilliseconds(int millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {}
    }

    private void reset() {
        RST.setState(true);
        delayMilliseconds(500);
        RST.setState(false);
        delayMilliseconds(500);

    }


    // --------------------------------------------------------------------------------------------------
    // Software SPI implementation follows. Is currently only compatible with the ADNS3080's SPI Mode 3.
    // (clock is normally high, data bit assertions occur on the clock falling edge) i.e. CPOL=1, CPHA=1.
    // --------------------------------------------------------------------------------------------------



    private void spiWrite(byte reg, byte[] writeData, int length) {
        SCLK.setState(true);
        while (SCLK_VER.getState() != true) {}
        NCS.setState(false);
        while (NCS_VER.getState() != false) {}
        byte dataGoingOut;

        // ------------ REG ADDR WRITE -------------------
        dataGoingOut = (byte)(reg | 0x80);
        for (int i = 0; i < 8; i++) {

            SCLK.setState(false); // boom - clock the data out
            while (SCLK_VER.getState() != false) {}

            if ((dataGoingOut & 0xff) >> 7 == 1) { // if MSB is 1
                MOSI.setState(true);
                while (MOSI_VER.getState() != true) {}
                Log.d("spiWrite", "writing addr bit HIGH");
            }
            else {
                MOSI.setState(false);
                while (MOSI_VER.getState() != false) {}
                Log.d("spiWrite", "writing addr bit LOW");
            }
            dataGoingOut <<= 1; // Discard most-significant bit and proceed

            SCLK.setState(true); // (smaller boom)
            while (SCLK_VER.getState() != true) {}

        }

        // delayMicroseconds(75);

        // ------------ DATA BYTES WRITE -------------------
        for (int byteIndex = 0; byteIndex < length; byteIndex++) {
            dataGoingOut = writeData[byteIndex];
            for (int i = 0; i < 8; i++) {

                SCLK.setState(false); // boom - clock the data out
                while (SCLK_VER.getState() != false) {}

                if ((dataGoingOut & 0xff) >> 7 == 1) { // if MSB is 1
                    MOSI.setState(true);
                    while (MOSI_VER.getState() != true) {}
                    Log.d("spiWrite", "writing data bit HIGH");
                }
                else {
                    MOSI.setState(false);
                    while (MOSI_VER.getState() != false) {}
                    Log.d("spiWrite", "writing data bit LOW");
                }
                dataGoingOut <<= 1; // Discard most-significant bit and proceed

                SCLK.setState(true); // (smaller boom)
                while (SCLK_VER.getState() != true) {}

            }
        }

        NCS.setState(true);
        while (NCS_VER.getState() != true) {}

    }

    private byte[] spiRead(byte reg, int length) {
        SCLK.setState(true);
        while (SCLK_VER.getState() != true) {}
        NCS.setState(false);
        while (NCS_VER.getState() != false) {}
        byte dataGoingOut;
        byte dataComingIn;

        // ------------ REG ADDR WRITE -------------------
        dataGoingOut = reg; // note lack of a 0x80 bitwise or here
        for (int i = 0; i < 8; i++) {

            SCLK.setState(false); // boom - clock the data out
            while (SCLK_VER.getState() != false) {}

            if ((dataGoingOut & 0xff) >> 7 == 1) { // if MSB is 1
                MOSI.setState(true);
                while (MOSI_VER.getState() != true) {}
                Log.d("spiRead", "writing addr bit HIGH");
            }
            else {
                MOSI.setState(false);
                while (MOSI_VER.getState() != false) {}
                Log.d("spiRead", "writing addr bit LOW");
            }
            dataGoingOut <<= 1; // Discard most-significant bit and proceed

            SCLK.setState(true); // (smaller boom)
            while (SCLK_VER.getState() != true) {}

        }

        // delayMicroseconds(75);
        byte[] outputBuffer = new byte[length];

        // ------------ DATA BYTES READ -------------------
        for (int byteIndex = 0; byteIndex < length; byteIndex++) {
            dataComingIn = (byte)(0x00);
            for (int i = 0; i < 8; i++) {

                SCLK.setState(false);
                while (SCLK_VER.getState() != false) {}

                if (MISO.getState()) { // if received MSB is 1
                    dataComingIn |= 1; Log.d("spiRead", "got read bit HIGH");
                }
                else {Log.d("spiRead", "got read bit LOW");}
                dataComingIn <<= 1;

                SCLK.setState(true);
                while (SCLK_VER.getState() != true) {}

            }
            outputBuffer[byteIndex] = dataComingIn;
        }

        NCS.setState(true);
        while (NCS_VER.getState() != true) {}

        return outputBuffer;
    }


}
