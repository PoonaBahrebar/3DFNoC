/*
File:           Main.java
Created:        2020/05/12
Last Changed:   2020/08/25
Author:         Jonathan D'Hoore
                University of Ghent

Part of Master's dissertation submitted in order to obtain the academic degree of
Master of Science in Electrical Engineering - main subject Electronic Circuits and Systems
Academic year 2019-2020

If you use our 3D NoC Emulator in your research, we would appreciate the following citation in any publications to which it has contributed:
Jonathan D'Hoore, Poona Bahrebar and Dirk Stroobandt, "3D NoC Emulation Model on a Single FPGA,"
In Proceedings of ACM/IEEE International Workshop on System-Level Interconnect Problems and Pathfinding (SLIPP'20), pp. 1-8, 2020.
*/

import ipCore.TrafficGenerator;
import myLogger.MyLogger;
import testing.MeshTest;

import javax.swing.*;
import java.util.logging.Level;

public class Main {

    public static void main(String[] args){
        /* Initialize loggers: debug and result */
        MyLogger loggers = new MyLogger(Level.INFO);

        boolean useGUI = true;

        /* Simulation CONSTANT parameters */
        int numVCs = 4;
        int bufferSize = 4;
        int flitsPerPacket = 8;
        int precision = 1000;
        int sourceQueueSize = 200;

        /* Cluster parameters */
        int sizeX = 3;
        int sizeY = 3;
        int sizeZ = 3;


        double hotSpotFactor = 1.0;
        double rentExponent = 1.0;
        int loadStep = 5;

        boolean adaptive = false;

        if(useGUI) {
            /* Use GUI to get cluster size */
            while (useGUI) {
                String inputString = JOptionPane.showInputDialog(null, "Give size of cluster:");
                String[] splitString = inputString.split("\\s+");
                if (splitString.length == 1) {
                    int input = Integer.parseInt(inputString);
                    System.out.println("Cluster size set to " + input + "x" + input + "x" + input);
                    sizeX = input;
                    sizeY = input;
                    sizeZ = input;
                    if (sizeX != 0 && sizeY != 0 && sizeZ != 0)
                        useGUI = false;
                } else if (splitString.length == 3) {
                    sizeX = Integer.parseInt(splitString[0]);
                    sizeY = Integer.parseInt(splitString[1]);
                    sizeZ = Integer.parseInt(splitString[2]);
                    System.out.println("Cluster size set to " + sizeZ + "x" + sizeY + "x" + sizeX);
                    if (sizeX != 0 && sizeY != 0 && sizeZ != 0)
                        useGUI = false;
                }
            }

            /* Use GUI to set hotspot factor */
            useGUI = true;

            while (useGUI) {
                String inputString = JOptionPane.showInputDialog(null, "Give hotspot factor:");
                if (inputString.length() != 0) {
                    hotSpotFactor = Double.parseDouble(inputString);
                    System.out.println("Hotspot factor set to " + hotSpotFactor);
                } else {
                    System.out.println("Invalid input, hotspot factor set to 1");
                }
                useGUI = false;
            }


            if (hotSpotFactor == 1.0) {
                /* Use GUI to set Rentian traffic */
                useGUI = true;

                while (useGUI) {
                    String inputString = JOptionPane.showInputDialog(null, "Give Rent exponent:");
                    if (inputString.length() != 0) {
                        rentExponent = Double.parseDouble(inputString);
                        System.out.println("Rent exponent set to: " + rentExponent);
                    } else {
                        System.out.println("Invalid input, uniform traffic used (p = 0).");
                    }
                    useGUI = false;
                }
            }


            /* Use GUI to set load step */
            useGUI = true;

            while (useGUI) {
                String inputString = JOptionPane.showInputDialog(null, "Give load step:");
                if (inputString.length() != 0) {
                    loadStep = Integer.parseInt(inputString);
                    System.out.println("Probability load step set to " + loadStep);
                } else {
                    System.out.println("Invalid input, load step set to 5.");
                }
                useGUI = false;
            }

            /* Set adaptive */
            String inputString = JOptionPane.showInputDialog(null, "Adaptive or XYZ routing?:");
            if (inputString.toLowerCase().equals("adaptive")) {
                System.out.println("Adaptive routing is used");
                adaptive = true;
            } else {
                System.out.println("XYZ routing is used");
            }
        }

        /* RADIX  parameters */
        int minRadix = 7;
        int radixStep = 1;
        int maxRadix = 7;

        /* TRAFFIC LOAD parameters */
        int minLoad = 5;
        int maxLoad = 200;

        int trafficPattern = TrafficGenerator.UNIFORM;
        if(hotSpotFactor != 1){
            trafficPattern = TrafficGenerator.HOTSPOT;
        } else if(rentExponent != 1){
            trafficPattern = TrafficGenerator.RENT;
        }



        /* Run test */
        MeshTest test  = new MeshTest(minRadix, numVCs, bufferSize, sourceQueueSize, adaptive, flitsPerPacket, precision, hotSpotFactor, rentExponent);
        test.setGUIClusterSize(sizeX, sizeY, sizeZ);
        test.updateTrafficPattern(trafficPattern);
        test.updateAdaptive(adaptive);
        test.radixTest(minRadix, radixStep, maxRadix, minLoad, loadStep, maxLoad, sizeX, sizeY, sizeZ);
        test.stopGUI();
    }

}
