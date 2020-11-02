/*
File:           MemoryTR.java
Created:        2020/07/18
Last Changed:   2020/07/19
Author:         Jonathan D'Hoore
                University of Ghent

Part of Master's dissertation submitted in order to obtain the academic degree of
Master of Science in Electrical Engineering - main subject Electronic Circuits and Systems
Academic year 2019-2020

If you use our 3D NoC Emulator in your research, we would appreciate the following citation in any publications to which it has contributed:
Jonathan D'Hoore, Poona Bahrebar and Dirk Stroobandt, "3D NoC Emulation Model on a Single FPGA,"
In Proceedings of ACM/IEEE International Workshop on System-Level Interconnect Problems and Pathfinding (SLIPP'20), pp. 1-8, 2020.
*/

package memory;

import ipCore.TrafficReceiver;

public class MemoryTR {
    /* Internal variables */
    int[] position;                                     // Position of the router: (y,x) coordinates
    String ID;


    /* - - - - - - - - - - - - - - Tracking variables - - - - - - - - - - - - - - */
    /* Number of received flits/packets */
    private int numReceivedFlits;                   // Total number of received flits at this receiver
    private int numReceivedPackets;                 // Total number of received packets at this receiver

    /* Latency */
    private double averagePacketLatency;            // Average packet latency
    private int minPacketLatency;                   // Minimum packet latency
    private int maxPacketLatency;                   // Maximum packet latency

    /* Number of hops */
    private double averageHops;                     // Average number of hops
    private int minHops;                            // Minimum number of hops
    private int maxHops;                            // Maximum number of hops

    /* Timing variables */
    private int networkTime;                            // Time of the network



    /* ********************************************************************************
     *                                   CONSTRUCTORS                               *
     ******************************************************************************** */

    /**
     * Constructor for memory element of traffic receiver
     * @param position: position of the connected router
     */
    public MemoryTR(int[] position){
        /* Internal variables */
        this.position = position;
        this.ID = "TR_node_" + position[0] + "." + position[1] + "." + position[2];

        /* Tracking variables */
        this.numReceivedFlits = 0;
        this.numReceivedPackets = 0;
        this.averagePacketLatency = 0.0;
        this.minPacketLatency = 1000000;
        this.maxPacketLatency = 0;
        this.averageHops = 0.0;
        this.minHops = 1000000;
        this.maxHops = 0;

        /* Time variables */
        this.networkTime = 0;
    }



    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                                *
     ******************************************************************************** */

    /**
     * Stores the state variables of the TR into this memory element
     * @param trafficReceiver
     */
    public void storeTR(TrafficReceiver trafficReceiver){
        this.numReceivedFlits = trafficReceiver.getNumReceivedFlits();
        this.numReceivedPackets = trafficReceiver.getNumReceivedPackets();
        this.averagePacketLatency = trafficReceiver.getAveragePacketLatency();
        this.minPacketLatency = trafficReceiver.getMinPacketLatency();
        this.maxPacketLatency = trafficReceiver.getMaxPacketLatency();
        this.averageHops = trafficReceiver.getAverageHops();
        this.minHops = trafficReceiver.getMinHops();
        this.maxHops = trafficReceiver.getMaxHops();
        this.networkTime = trafficReceiver.getNetworkTime();
    }

    /**
     * Reset all tracking variables
     */
    public void reset(){
        this.numReceivedFlits = 0;
        this.numReceivedPackets = 0;
        this.averagePacketLatency = 0.0;
        this.minPacketLatency = 1000000;
        this.maxPacketLatency = 0;
        this.averageHops = 0.0;
        this.minHops = 1000000;
        this.maxHops = 0;
    }



    /* ********************************************************************************
     *                              GETTERS AND SETTERS                             *
     ******************************************************************************** */

    public int[] getPosition() {
        return position;
    }

    public void setPosition(int[] position) {
        this.position = position;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getNumReceivedFlits() {
        return numReceivedFlits;
    }

    public void setNumReceivedFlits(int numReceivedFlits) {
        this.numReceivedFlits = numReceivedFlits;
    }

    public int getNumReceivedPackets() {
        return numReceivedPackets;
    }

    public void setNumReceivedPackets(int numReceivedPackets) {
        this.numReceivedPackets = numReceivedPackets;
    }

    public double getAveragePacketLatency() {
        return averagePacketLatency;
    }

    public void setAveragePacketLatency(double averagePacketLatency) {
        this.averagePacketLatency = averagePacketLatency;
    }

    public int getMinPacketLatency() {
        return minPacketLatency;
    }

    public void setMinPacketLatency(int minPacketLatency) {
        this.minPacketLatency = minPacketLatency;
    }

    public int getMaxPacketLatency() {
        return maxPacketLatency;
    }

    public void setMaxPacketLatency(int maxPacketLatency) {
        this.maxPacketLatency = maxPacketLatency;
    }

    public double getAverageHops() {
        return averageHops;
    }

    public void setAverageHops(double averageHops) {
        this.averageHops = averageHops;
    }

    public int getMinHops() {
        return minHops;
    }

    public void setMinHops(int minHops) {
        this.minHops = minHops;
    }

    public int getMaxHops() {
        return maxHops;
    }

    public void setMaxHops(int maxHops) {
        this.maxHops = maxHops;
    }

    public int getNetworkTime() {
        return networkTime;
    }

    public void setNetworkTime(int networkTime) {
        this.networkTime = networkTime;
    }
}
