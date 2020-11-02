/*
File:           MeshTest.java
Created:        2020/05/12
Last Changed:   2020/08/30
Author:         Jonathan D'Hoore
                University of Ghent

Part of Master's dissertation submitted in order to obtain the academic degree of
Master of Science in Electrical Engineering - main subject Electronic Circuits and Systems
Academic year 2019-2020

If you use our 3D NoC Emulator in your research, we would appreciate the following citation in any publications to which it has contributed:
Jonathan D'Hoore, Poona Bahrebar and Dirk Stroobandt, "3D NoC Emulation Model on a Single FPGA,"
In Proceedings of ACM/IEEE International Workshop on System-Level Interconnect Problems and Pathfinding (SLIPP'20), pp. 1-8, 2020.
*/

package testing;

import mesh.ClusteredMesh;

import java.util.logging.Level;
import java.util.logging.Logger;


public class MeshTest {
    /* ********************************************************************************
     *                                  VARIABLES                                     *
     ******************************************************************************** */
    private int radix;
    private int numPorts;
    private int numVCs;
    private int bufferSize;
    private int flitsPerPacket;
    private int precision;

    private int minWarmupTime;
    private double convConst;
    private int measurementTime;

    private boolean adaptive;

    private int sourceQueueSize;


    private int sizeX;
    private int sizeY;
    private int sizeZ;

    private double hotSpotFactor;
    private int[] hotspots;

    private double rentExponent;

    private SimpleGUI gui;

    private final static Logger debugLogger = Logger.getLogger("debugLogger");
    private final static Logger resultLogger = Logger.getLogger("resultLogger");

    /* ********************************************************************************
     *                                   CONSTRUCTORS                               *
     ******************************************************************************** */

    public MeshTest(int radix, int numVCs, int bufferSize, int sourceQueueSize, boolean adaptive, int flitsPerPacket, int precision, double hotSpotFactor, double rentExponent){
        this.radix = radix;
        this.numPorts = 7;
        this.numVCs = numVCs;
        this.bufferSize = bufferSize;
        this.flitsPerPacket = flitsPerPacket;
        this.precision = precision;

        this.adaptive = adaptive;

        this.sourceQueueSize = sourceQueueSize;

        minWarmupTime = 1000;
        convConst = 0.001;
        measurementTime = 15000;

        this.hotSpotFactor = hotSpotFactor;
        this.rentExponent = rentExponent;

        gui = new SimpleGUI();
    }


    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                              *
     ******************************************************************************** */

    /**
     * Simulation run for several network sizes (radix)
     * Simulation is stopped if the average packet latency is higher than a certain treshold
     * @param minRadix: minimul radix
     * @param radixStep: Step with which radix is increased
     * @param maxRadix: maximum radix (included)
     * @param minLoad: minimum traffic load (i.e. PACKETS/cycle/core)
     * @param loadStep: step with which traffic load is increased)
     * @param maxLoad: maximum traffic load
     * @param sizeX: size of the cluster along x direction
     * @param sizeY: size of cluster along y direction
     * @param sizeZ: size of cluster along z direction
     */
    public void radixTest(int minRadix, int radixStep, int maxRadix, int minLoad, int loadStep, int maxLoad, int sizeX, int sizeY, int sizeZ){
        for(int i = minRadix; i <= maxRadix; i +=radixStep){
            gui.updateRadix(i);
            System.out.println("/ ---------------------- TEST FOR RADIX " + i + " ---------------------- /");
            this.radix = i;
            this.sizeX = Math.min(this.radix, sizeX);
            this.sizeY = Math.min(this.radix, sizeY);
            this.sizeZ = Math.min(this.radix, sizeZ);
            this.gui.setClusterLabel(this.sizeX, this.sizeY, this.sizeZ);
            if(hotSpotFactor != 1) {
                /* Create 1 hotspot at center of network */
                int middle = ((i * i * i) - 1) / 2;
                this.hotspots = new int[]{middle};

                // 4 hotspots created (static)
                this.hotspots = new int[]{8, middle, middle + 8, (radix*radix*radix)-5 };
            } else {
                this.hotspots = new int[]{};
            }
            this.variableLoadTest(minLoad, loadStep, maxLoad);
        }
    }


    /**
     * Function that varies the traffic load while keeping other simulation parameters constant
     * @param start: minimal traffic load
     * @param step: step in traffic load
     * @param stop: maximum traffic load
     */
    public void variableLoadTest(int start, int step, int stop){
        this.logParameters();
        resultLogger.log(Level.INFO, "p \t Packet latency \t avg_hops \t alpha \t time [s]");
        double avgHops = 0.0;

        boolean maxLatencyReached = false;
        for(int j = start; j <= stop && !maxLatencyReached; j+= step) {
            gui.updateProb(j);
            System.out.println("--- Simulation for p = " + j + "/" + precision);
            debugLogger.log(Level.FINER, "Simulation for p = " + j + "/" + precision);

            /* Create CLUSTERED mesh */
            ClusteredMesh mesh = new ClusteredMesh(radix, this.sizeX, this.sizeY, this.sizeZ, numPorts, numVCs, bufferSize, sourceQueueSize, adaptive,  flitsPerPacket, j, 1000, hotspots, hotSpotFactor, rentExponent);

            /* Simulation initial parameters */
            boolean idle = false;
            int phase = 0;
            int cycle = 0;
            double prevLatency = 1.0;
            int warmup = minWarmupTime;

            final long startTime =  System.currentTimeMillis();


            while (!idle) {
                debugLogger.log(Level.FINE, "/ ---------------------- SIMULATION CYCLE " + cycle + "  ---------------------- /");
                /* Simulate all routers */
                mesh.simulateMesh();

                /* After minimum warm up time, check if latency of mesh has already converged */
                if (mesh.getNetworkTime() > minWarmupTime && phase == 0 && mesh.getNetworkTime() % 100 == 0) {
                    /* Compute packet latency */
                    double latency = mesh.calculateAveragePacketLatency();


                    /* Check convergence */
                    if ((latency - prevLatency) / latency < convConst) {
                        System.out.println("Measurement started");
                        mesh.startMeasurement();
                        cycle = 0;
                        warmup = mesh.getNetworkTime();
                        phase = 1;
                    }
                    prevLatency = latency;
                }

                /* STOP CONDITION */
                if(mesh.getNetworkTime() % 500 == 0){
                    double latency = mesh.calculateAveragePacketLatency();
                    if(latency > 750){
                        idle = true;
                        System.out.println("Latency larger than 750, stopped simulation");
                        maxLatencyReached = true;
                    }
                }


                /* After measurement phase, start drain phase */
                if (mesh.getNetworkTime() > (warmup + measurementTime) && phase == 1) {
                    double latency = mesh.calculateAveragePacketLatency();
                    System.out.println("Drain started, avg packet latency is now: " + latency );
                    phase = 2;
                    mesh.startDrain();
                }

                if(mesh.getNetworkTime() % 500 == 0 && mesh.getNetworkTime() > (warmup + measurementTime) && mesh.isIdle()){
                    idle= true;
                }

                if(mesh.getNetworkTime() > 20*measurementTime){
                    System.out.println("Process stopped because taking too long, might have to check!");
                    idle = true;

                }

                cycle++;

            }

            double alpha = (1.0*(mesh.getNetworkTime() - warmup)) / cycle;
            System.out.println("alpha: " + alpha);


            final long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("Excecution took " + duration + " ms");


            double prob = 1.0*j / precision;
            double latency = mesh.calculateAveragePacketLatency();
            avgHops = mesh.calculateAverageHops();
            String msg = prob + "\t" + latency + "\t" + avgHops + "\t" + alpha + "\t" + Math.round(duration/1000);
            resultLogger.log(Level.INFO, msg);

            /* Print number of received packets test */
            //mesh.printNumReceivedPackets();

            debugLogger.log(Level.INFO, "/ - - - - - NUMBER OF RECEIVED PACKETS PER CORE - - - - - /");
            debugLogger.log(Level.INFO, "PARAMETERS:");
            debugLogger.log(Level.INFO, "Radix: " + this.radix);
            debugLogger.log(Level.INFO, "prob: " + prob);
            mesh.logNumReceivedPackets();


             System.out.println("Average hop count: " + avgHops);
            System.out.println("Latency: " + latency);

        }

        /* Report average hop count : */
        resultLogger.log(Level.INFO, "Average hop count : " + avgHops);

    }


    /**
     * Test used for debugging: testing the traffic pattern
     */
    public void trafficPatternTest(int load, int sizeX, int sizeY, int sizeZ){
        gui.updateProb(load);
        System.out.println("--- Simulation for p = " + load + "/" + precision);
        debugLogger.log(Level.FINER, "Simulation for p = " + load + "/" + precision);

        /* Create CLUSTERED mesh */
        ClusteredMesh mesh = new ClusteredMesh(radix, sizeX, sizeY, sizeZ, numPorts, numVCs, bufferSize, sourceQueueSize, adaptive,  flitsPerPacket, load, 1000, hotspots, hotSpotFactor, rentExponent);

        /* Simulation initial parameters */
        boolean idle = false;
        int phase = 0;
        int cycle = 0;
        double prevLatency = 1.0;
        int warmup = minWarmupTime;

        final long startTime =  System.currentTimeMillis();

        int cycles = 20000;
        // Little warm up just to be sure
        for(int i = 0; i < 100; i++) {
            mesh.simulateMesh();
        }

        mesh.startMeasurement();
        System.out.println("Started measurement");
        for(int i = 0; i < cycles; i++) {
            mesh.simulateMesh();
        }

        // Drain
        mesh.startDrain();
        System.out.println("Started drain");

        idle = false;
        while(!idle) {
            mesh.simulateMesh();

            if(mesh.getNetworkTime() % 500 == 0 && mesh.isIdle())
                idle = true;
        }


        debugLogger.log(Level.INFO, "/ - - - - - NUMBER OF RECEIVED PACKETS PER CORE - - - - - /");
        debugLogger.log(Level.INFO, "PARAMETERS:");
        debugLogger.log(Level.INFO, "Radix: " + this.radix);
        mesh.logNumReceivedPackets();


    }

    /**
     * Print header with parameters to result file
     */
    private void logParameters(){
        resultLogger.log(Level.INFO, "/ ****** SIMULATION PARAMETERS  ****** /");
        resultLogger.log(Level.INFO, "Radix: " + this.radix);
        resultLogger.log(Level.INFO, "VCs: " + this.numVCs);
        resultLogger.log(Level.INFO, "Buffer size: " + this.bufferSize);
        resultLogger.log(Level.INFO, "Flits per packet: " + this.flitsPerPacket);
        resultLogger.log(Level.INFO, "Cluster size: x = " + sizeX + ", y = " + sizeY + ", z = " + sizeZ);
        resultLogger.log(Level.INFO, "Hotspot factor: " + hotSpotFactor);
        resultLogger.log(Level.INFO, "Rent exponent: " + rentExponent);
        String routing = "XYZ";
        if(adaptive)
            routing = "Adaptive";
        resultLogger.log(Level.INFO, "Routing algorithm: " + routing);
        resultLogger.log(Level.INFO, "/ ****** START SIMULATION  ****** /");

    }

    public void stopGUI(){
        this.gui.finish();
    }

    public void setGUIClusterSize(int sizeX, int sizeY, int sizeZ){
        this.gui.setClusterLabel(sizeX, sizeY, sizeZ);
    }

    public void updateTrafficPattern(int trafficPattern){
        this.gui.updateTraffic(trafficPattern);
    }

    public void updateAdaptive(boolean adaptive){
        this.gui.updateAdaptive(adaptive);
    }
}
