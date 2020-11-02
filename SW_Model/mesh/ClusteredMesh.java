/*
File:           ClusteredMesh.java
Created:        2020/07/18
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
package mesh;

import ipCore.TrafficGenerator;
import memory.MemoryCluster;
import memory.MemoryInterCluster;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ClusteredMesh {
    /* - - - - - - - - - - -  - - - IMPORTANT - - - - - - - - - - - - - -
            Assumptions made with respect to the numbering of the ports:
            0 : EAST
            1 : SOUTH
            2 : WEST
            3 : NORTH
            4 : UP
            5 : DOWN
            6 : IPCORE
        - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */
    /* ********************************************************************************
     *                                  VARIABLES                                     *
     ******************************************************************************** */
    /* Used for creating Rentian traffic */
    public static List<List<Integer>> stepsPerDestination;
    public static int DEST_PREC;

    /* Clusters containing Routers, IPCores and channels */
    private List<List<List<MemoryCluster>>> memoryClusters;
    private List<List<List<MemoryInterCluster>>> memoryInterClusters;
    private int[] numClusters;


    private int sizeX;
    private int sizeY;
    private int sizeZ;

    private Cluster physicalCluster;

    private boolean adaptive;


    /* Network variables */
    private int radix;

    /* Stalling */
    private boolean stallNext;
    private boolean stallNetwork;



    /* Loggers */
    private final static Logger debugLogger = Logger.getLogger("debugLogger");
    private final static Logger resultLogger = Logger.getLogger("resultLogger");

    /* ********************************************************************************
     *                                   CONSTRUCTORS                               *
     ******************************************************************************** */

    public ClusteredMesh(int radix, int sizeX, int sizeY, int sizeZ, int numPorts, int numVCs, int bufferSize, int sourceQueueSize, boolean adaptive,
                         int flitsPerPacket, int prob, int precision, int[] hotspots, double hotSpotFactor, double rentExponent){
        /* Determine number of clusters in each direction*/
        this.numClusters = new int[3];
        this.numClusters[0] = (int) Math.ceil(radix/(1.0*sizeZ));
        this.numClusters[1] = (int) Math.ceil(radix/(1.0*sizeY));
        this.numClusters[2] = (int) Math.ceil(radix/(1.0*sizeX));

        if(hotSpotFactor != 1.0 || rentExponent != 1.0)
            this.DEST_PREC = 32768;
        else
            this.DEST_PREC = radix*radix*radix;


        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;

        this.adaptive = adaptive;

        /* Create cluster and inter-cluster memory */
        this.memoryClusters = new ArrayList<List<List<MemoryCluster>>>();
        this.memoryInterClusters = new ArrayList<List<List<MemoryInterCluster>>>();
        for(int i = 0; i < numClusters[0]; i++){
            this.memoryClusters.add(new ArrayList<List<MemoryCluster>>());
            this.memoryInterClusters.add(new ArrayList<List<MemoryInterCluster>>());
            for(int j = 0; j < numClusters[1]; j++){
                this.memoryClusters.get(i).add(new ArrayList<MemoryCluster>());
                this.memoryInterClusters.get(i).add(new ArrayList<MemoryInterCluster>());
                for(int k = 0; k < numClusters[2]; k++){
                    int[] startPosition = {i*sizeZ, j*sizeY, k*sizeX};
                    this.memoryClusters.get(i).get(j).add(new MemoryCluster(sizeX, sizeY, sizeZ, startPosition, adaptive, numPorts, numVCs, bufferSize));
                    this.memoryInterClusters.get(i).get(j).add(new MemoryInterCluster(sizeX, sizeY, sizeZ, startPosition, radix, adaptive));

                }
            }
        }

        /* Create physical cluster */
        this.physicalCluster = new Cluster(sizeX, sizeY, sizeZ, radix, adaptive,  numPorts, numVCs, bufferSize, sourceQueueSize, flitsPerPacket, prob, precision, hotspots, hotSpotFactor, rentExponent);

        /* Network variables */
        this.radix = radix;

        this.createTrafficDestinations(rentExponent, radix);

        /* Network stalling variables */
        stallNext = false;
        stallNetwork = false;
    }


    /* ********************************************************************************
     *                         CONSTRUCTOR HELP FUNCTIONS                           *
     ******************************************************************************** */

    /**
     * Help function used for creating local traffic at the Traffic Generators
     * @param rentExponent: rent exponenent
     * @param radix: radix of the network (size of the network along one direction)
     */
    private void createTrafficDestinations(double rentExponent, int radix){
        int N = radix*radix*radix;

        stepsPerDestination = new ArrayList<List<Integer>>();
        for(int i = 0; i < N; i++){
            stepsPerDestination.add(new ArrayList<Integer>());
        }

        int maxHops = 3*(radix-1);
        double[] P = new double[maxHops +1];
        double[] CPD = new double[maxHops +1];
        double CPD_total = 0.0;
        for(int d = 1; d < maxHops +1; d++){
            /* Compute P(d) */
            P[d] = 1.0/(4*d) * ( Math.pow(1+d*(d-1),rentExponent) - Math.pow(d*(d-1), rentExponent) + Math.pow(d*(d+1), rentExponent) - Math.pow(1+d*(d+1), rentExponent) );

            /* Compute CPD */
            double sum = 0.0;
            for(int i = 1; i < 2*Math.sqrt(N)-2; i++){
                if(Math.sqrt(N) + i - d <= Math.sqrt(N) && Math.sqrt(N) + i - d > 0){
                    sum += (Math.sqrt(N) - i) * (Math.sqrt(N) + i - d);
                }
            }
            CPD[d] = P[d] * sum;
            CPD_total += CPD[d];
        }

        /* Normalize CPD */
        for(int d = 0; d < CPD.length; d++){
            CPD[d] = CPD[d]/CPD_total;
        }

        /* Scale CPD such that prob is 1 for every source router */
        double[] factor = new double[N];
        for(int z_s = 0; z_s < radix; z_s ++){
            for(int y_s = 0; y_s < radix; y_s ++){
                for(int x_s = 0; x_s < radix; x_s ++){
                    int source = x_s + radix*y_s + radix*radix*z_s;
                    for(int z_d = 0; z_d < radix; z_d ++) {
                        for (int y_d = 0; y_d < radix; y_d++) {
                            for (int x_d = 0; x_d < radix; x_d++) {
                                int dest = x_d + radix*y_d + radix*radix*z_d;
                                int hops = Math.abs(x_d - x_s) + Math.abs(y_d - y_s) + Math.abs(z_d - z_s);
                                factor[source] += CPD[hops];
                            }
                        }
                    }
                    factor[source] = DEST_PREC / factor[source];
                }
            }
        }

        /* Create list of destinations */
        int[] CPD_SCALED = new int[maxHops +1];
        for(int z_s = 0; z_s < radix; z_s ++){
            for(int y_s = 0; y_s < radix; y_s ++){
                for(int x_s = 0; x_s < radix; x_s ++){
                    int source = x_s + radix*y_s + radix*radix*z_s;
                    int index = 0;
                    /* Create CPD scaled for this source */
                    for(int d = 0; d < CPD.length; d++){
                        CPD_SCALED[d] = (int) (CPD[d] * factor[source]);
                    }
                    for(int z_d = 0; z_d < radix; z_d ++) {
                        for (int y_d = 0; y_d < radix; y_d++) {
                            for (int x_d = 0; x_d < radix; x_d++) {
                                int dest = x_d + radix*y_d + radix*radix*z_d;
                                int hops = Math.abs(x_d - x_s) + Math.abs(y_d - y_s) + Math.abs(z_d - z_s);
                                int steps = Math.max(CPD_SCALED[hops],0);
                                for(int i = 0; i < steps; i++){
                                    stepsPerDestination.get(source).add(dest);
                                    index++;
                                }
                            }
                        }
                    }
                    /* Fill in extra slots at end of array with source, such that they are never used */
                    while(stepsPerDestination.get(source).size() < DEST_PREC){
                        stepsPerDestination.get(source).add(source);
                    }


                }
            }
        }
    }



    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                              *
     ******************************************************************************** */

    /**
     * Simulate the complete network (which is divided in several clusters).
     * This process is split in several steps:
     *  1) Load data from memory into the physical cluster
     *  2) Simualte the physical cluster
     *  3) Store data from physical cluster in memory
     *  4) After each cluster is simulated, update the inter-cluster memory
     */
    public void simulateMesh(){
        /* Network stalling variables */
        stallNetwork = stallNext;
        stallNext = false;

        /* Simulate network */
        for(int i = 0; i < numClusters[0]; i++){
            for(int j = 0; j < numClusters[1]; j++){
                for(int k = 0; k < numClusters[2]; k++){
                    /* Load state of the current cluster into the physical cluster */
                    this.physicalCluster.loadCluster(memoryClusters.get(i).get(j).get(k));
                    this.physicalCluster.loadInterClusterChannels(memoryInterClusters.get(i).get(j).get(k));

                    /* Unused routes need to be disabled */
                    this.disableEmptyRouters(i,j,k);

                    /* Simulate this cluster */
                    this.physicalCluster.simulateCluster(stallNetwork);

                    /* Store cluster state in memory (router, ipcore and channels inside cluster)
                        Store data on channels in between clusters
                    *   NOTE: channels in between clusters are not really updated yet!  */
                    this.memoryClusters.get(i).get(j).get(k).storeCluster(physicalCluster);
                    this.memoryInterClusters.get(i).get(j).get(k).storeInterCluster(physicalCluster);


                    /* Check if the network needs to be stalled, based on this cluster */
                    stallNext = stallNext || physicalCluster.checkNetworkStalling();


                }
            }
        }

        /* Update channels in between clusters */
        this.updateMemoryInterClusters();


    }

    /**
     * If the cluster dimension is not a multiple of the network dimension, there might be some 'unused nodes' in the physical cluster.
     * These need to be disabled to prevent them from creating new packets etc.
     * @param clusterZ: index of the cluster along the z-direction
     * @param clusterY: index of the cluster along the y-direction
     * @param clusterX: index of the cluster along the x-direction
     */
    private void disableEmptyRouters(int clusterZ, int clusterY, int clusterX){
        /* Set phase to DRAIN phase such that they do not create packages */
        int currentPhase = physicalCluster.getIpCores().get(0).get(0).get(0).getTrafficGenerator().getPhase();
        for(int i = 0; i < sizeZ; i++) {
            for (int j = 0; j < sizeY; j++) {
                for (int k = 0; k < sizeX; k++) {
                    int z = clusterZ*sizeZ + i;
                    int y = clusterY*sizeY + j;
                    int x = clusterX*sizeX + k;
                    if(z >= radix || y >= radix || x >= radix) {
                        physicalCluster.getIpCores().get(i).get(j).get(k).getTrafficGenerator().setPhase(TrafficGenerator.DRAIN_PHASE);
                    } else {
                        physicalCluster.getIpCores().get(i).get(j).get(k).getTrafficGenerator().setPhase(currentPhase);
                    }
                }
            }
        }
    }

    /**
     * Copy data from out to in buffer in the inter-cluster data
     */
    private void updateMemoryInterClusters(){
        for(int i = 0; i < numClusters[0]; i++) {
            for (int j = 0; j < numClusters[1]; j++) {
                for (int k = 0; k < numClusters[2]; k++) {
                    MemoryInterCluster currentMem = this.memoryInterClusters.get(i).get(j).get(k);


                    for(int m = 0; m < sizeZ; m++){
                        /* EAST and WEST CHANNELS */
                        for(int n = 0; n < sizeY; n++){
                            if(k != numClusters[2] -1) {
                                MemoryInterCluster eastMem = this.memoryInterClusters.get(i).get(j).get(k + 1);
                                currentMem.getInterClusterInputChannels().get(0).get(m).get(n).addFlit(eastMem.getInterClusterOutputChannels().get(2).get(m).get(n).removeFlit());
                                currentMem.getInterClusterOutputCreditChannels().get(0).get(m).get(n).addCredit(eastMem.getInterClusterInputCreditChannels().get(2).get(m).get(n).removeCredit());


                            }
                            if(k != 0) {
                                MemoryInterCluster westMem = this.memoryInterClusters.get(i).get(j).get(k - 1);
                                currentMem.getInterClusterInputChannels().get(2).get(m).get(n).addFlit(westMem.getInterClusterOutputChannels().get(0).get(m).get(n).removeFlit());
                                currentMem.getInterClusterOutputCreditChannels().get(2).get(m).get(n).addCredit(westMem.getInterClusterInputCreditChannels().get(0).get(m).get(n).removeCredit());
                            }
                        }
                        /* SOUTH and NORTH channels */
                        for(int n = 0; n < sizeX; n++){
                            if(j != 0) {
                                MemoryInterCluster southMem = this.memoryInterClusters.get(i).get(j - 1).get(k);
                                currentMem.getInterClusterInputChannels().get(1).get(m).get(n).addFlit(southMem.getInterClusterOutputChannels().get(3).get(m).get(n).removeFlit());
                                currentMem.getInterClusterOutputCreditChannels().get(1).get(m).get(n).addCredit(southMem.getInterClusterInputCreditChannels().get(3).get(m).get(n).removeCredit());
                            }
                            if( j != numClusters[1] -1) {
                                MemoryInterCluster northMem = this.memoryInterClusters.get(i).get(j + 1).get(k);
                                currentMem.getInterClusterInputChannels().get(3).get(m).get(n).addFlit(northMem.getInterClusterOutputChannels().get(1).get(m).get(n).removeFlit());
                                currentMem.getInterClusterOutputCreditChannels().get(3).get(m).get(n).addCredit(northMem.getInterClusterInputCreditChannels().get(1).get(m).get(n).removeCredit());
                            }
                        }
                    }

                    /* UP and DOWN channels */
                    for(int m = 0; m < sizeY; m++){
                        for(int n = 0; n < sizeX; n++){
                            if(i != numClusters[0] - 1) {
                                MemoryInterCluster upMem = this.memoryInterClusters.get(i + 1).get(j).get(k);
                                currentMem.getInterClusterInputChannels().get(4).get(m).get(n).addFlit(upMem.getInterClusterOutputChannels().get(5).get(m).get(n).removeFlit());
                                currentMem.getInterClusterOutputCreditChannels().get(4).get(m).get(n).addCredit(upMem.getInterClusterInputCreditChannels().get(5).get(m).get(n).removeCredit());
                            }
                            if(i != 0) {
                                MemoryInterCluster downMem = this.memoryInterClusters.get(i - 1).get(j).get(k);
                                currentMem.getInterClusterInputChannels().get(5).get(m).get(n).addFlit(downMem.getInterClusterOutputChannels().get(4).get(m).get(n).removeFlit());
                                currentMem.getInterClusterOutputCreditChannels().get(5).get(m).get(n).addCredit(downMem.getInterClusterInputCreditChannels().get(4).get(m).get(n).removeCredit());
                            }
                        }
                    }

                }
            }
        }
    }


    /**
     * Start measurement phase
     */
    public void startMeasurement(){
        for(int i = 0; i < sizeZ; i++){
            for(int j = 0; j < sizeY; j++){
                for(int k = 0; k < sizeX; k++) {
                    this.physicalCluster.getIpCores().get(i).get(j).get(k).startMeasurement();
                }
            }
        }

        /* Reset all Traffic Receiver memory values */
        for(int i = 0; i < numClusters[0]; i++){
            for(int j = 0; j < numClusters[1]; j++){
                for(int k = 0; k < numClusters[2]; k++){
                    MemoryCluster memoryCluster = memoryClusters.get(i).get(j).get(k);
                    for(int m = 0; m < sizeZ; m++){
                        for(int n = 0; n < sizeY; n++){
                            for(int p = 0; p < sizeX; p++) {
                                memoryCluster.getMemoryIPCore(m,n,p).getTrafficReceiver().reset();
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Start Drain phase
     */
    public void startDrain(){
        for(int i = 0; i < sizeZ; i++){
            for(int j = 0; j < sizeY; j++){
                for(int k = 0; k < sizeX; k++) {
                    this.physicalCluster.getIpCores().get(i).get(j).get(k).startDrain();
                }
            }
        }

    }

    /**
     * Check if the network is idle
     * @return true if all routers are idle
     */
    public boolean isIdle(){
        boolean idle = true;
        for(int i = 0; i < numClusters[0] && idle; i++){
            for(int j = 0; j < numClusters[1] && idle; j++){
                for(int k = 0; k < numClusters[2] && idle; k++){
                    MemoryCluster memoryCluster = memoryClusters.get(i).get(j).get(k);
                    for(int m = 0; m < sizeZ && idle; m++){
                        for(int n = 0; n < sizeY && idle; n++){
                            for(int p = 0; p < sizeX && idle; p++) {
                                idle = memoryCluster.getMemoryRouter(m,n,p).isIdle();
                            }
                        }
                    }
                }
            }
        }
        return idle;
    }


    /**
     * Calculates the average packet latency of all received packets at all nodes.
     * @return average packet latency
     */
    public double calculateAveragePacketLatency(){
        double totalPacketLatency = 0.0;
        double averagePacketLatency = 0.0;
        int totalReceivedPackets = 0;
        for(int i = 0; i < numClusters[0]; i++){
            for(int j = 0; j < numClusters[1]; j++){
                for(int k = 0; k < numClusters[2]; k++){
                    MemoryCluster memoryCluster = memoryClusters.get(i).get(j).get(k);
                    for(int m = 0; m < sizeZ; m++){
                        for(int n = 0; n < sizeY; n++){
                            for(int p = 0; p < sizeX; p++) {
                                int numReceivedPackets = memoryCluster.getMemoryIPCore(m,n,p).getTrafficReceiver().getNumReceivedPackets();
                                totalReceivedPackets += numReceivedPackets;
                                totalPacketLatency += memoryCluster.getMemoryIPCore(m,n,p).getTrafficReceiver().getAveragePacketLatency() * numReceivedPackets;
                            }
                        }
                    }
                }
            }
        }

        averagePacketLatency = totalPacketLatency / totalReceivedPackets;
        return averagePacketLatency;

    }

    /**
     * Calcualtes the avarege hop count of all received packets at all nodes.
     * @return: average hop count
     */
    public double calculateAverageHops(){
        double totalHops = 0.0;
        double averageHops = 0.0;
        int totalReceivedPackets = 0;
        for(int i = 0; i < numClusters[0]; i++){
            for(int j = 0; j < numClusters[1]; j++){
                for(int k = 0; k < numClusters[2]; k++){
                    MemoryCluster memoryCluster = memoryClusters.get(i).get(j).get(k);
                    for(int m = 0; m < sizeZ; m++){
                        for(int n = 0; n < sizeY; n++){
                            for(int p = 0; p < sizeX; p++) {
                                int numReceivedPackets = memoryCluster.getMemoryIPCore(m,n,p).getTrafficReceiver().getNumReceivedPackets();
                                totalReceivedPackets += numReceivedPackets;
                                totalHops += memoryCluster.getMemoryIPCore(m,n,p).getTrafficReceiver().getAverageHops() * numReceivedPackets;
                            }
                        }
                    }
                }
            }
        }
        averageHops = totalHops / totalReceivedPackets;
        return averageHops;
    }

    /**
     * Print out the number of received packets at each node.
     */
    public void printNumReceivedPackets(){
        for(int i = 0; i < numClusters[0]; i++){
            for(int j = 0; j < numClusters[1]; j++){
                for(int k = 0; k < numClusters[2]; k++){
                    MemoryCluster memoryCluster = memoryClusters.get(i).get(j).get(k);
                    for(int m = 0; m < sizeZ; m++){
                        for(int n = 0; n < sizeY; n++){
                            for(int p = 0; p < sizeX; p++) {
                                int numReceivedPackets = memoryCluster.getMemoryIPCore(m,n,p).getTrafficReceiver().getNumReceivedPackets();
                                int[] position = memoryCluster.getMemoryIPCore(m,n,p).getTrafficReceiver().getPosition();
                                System.out.println("R[" + position[0] + "," + position[1] + "," + position[2] + "]: " + numReceivedPackets);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Print the number of received packets at each node to the debugging file.
     */
    public void logNumReceivedPackets(){
        for(int i = 0; i < numClusters[0]; i++){
            for(int j = 0; j < numClusters[1]; j++){
                for(int k = 0; k < numClusters[2]; k++){
                    MemoryCluster memoryCluster = memoryClusters.get(i).get(j).get(k);
                    for(int m = 0; m < sizeZ; m++){
                        for(int n = 0; n < sizeY; n++){
                            for(int p = 0; p < sizeX; p++) {
                                int numReceivedPackets = memoryCluster.getMemoryIPCore(m,n,p).getTrafficReceiver().getNumReceivedPackets();
                                int[] position = memoryCluster.getMemoryIPCore(m,n,p).getTrafficReceiver().getPosition();
                                if(position[0] < radix && position[1] < radix && position[2] < radix) {
                                    // Print number of received packets for every router that should be simulated (not for routers on edge)
                                    String line = ("R[" + position[0] + "," + position[1] + "," + position[2] + "]: " + numReceivedPackets);
                                    debugLogger.log(Level.INFO, line);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /* ********************************************************************************
     *                              GETTERS AND SETTERS                             *
     ******************************************************************************** */

    public int getNetworkTime() {
        return this.memoryClusters.get(0).get(0).get(0).getMemoryIPCore(0,0,0).getTrafficReceiver().getNetworkTime();
    }

}
