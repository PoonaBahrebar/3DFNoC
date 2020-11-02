/*
File:           MemoryTG.java
Created:        2020/07/18
Last Changed:   2020/08/13
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

import building_blocks.PacketDescriptor;
import ipCore.TrafficGenerator;

import java.util.ArrayList;
import java.util.List;

public class MemoryTG {

    private int[] position;                                         // Position of the router: (z,y,x) coordinates
    private int numVCs;

    private int[] credits;                                          // Number of credits available for each VC
    private int[] states;                                           // States of the VC (ASSIGNED / IDLE)

    private List<PacketDescriptor> sourceQueue;                     // Buffer that contains packet descriptors, used to generate flits

    private int allocatePriority;                                   // Used for allocation of Virtual Channels
    private int packetToSend;                                       // Used for Round-Robin scheme in flits over VCs

    private int networkTime;
    private int localTime;


    /* ********************************************************************************
     *                                   CONSTRUCTORS                               *
     ******************************************************************************** */

    /**
     * Constructor
     * @param position: position of the router connected to the IP core
     * @param numVCs: number of VCs per port of the router
     * @param bufferSize: size of the buffer at each input unit (in number of flits)
     */
    public MemoryTG(int[] position, int numVCs, int bufferSize){
        this.numVCs = numVCs;
        this.position = position;
        this.credits = new int[numVCs];
        this.states = new int[numVCs];
        for(int i = 0; i < numVCs; i++){
            this.credits[i] = bufferSize;
            this.states[i] = TrafficGenerator.IDLE;
        }
        this.sourceQueue = new ArrayList<PacketDescriptor>();
        this.allocatePriority = 0;
        this.packetToSend = 0;
        this.networkTime = 0;
        this.localTime = 0;
    }


    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                                *
     ******************************************************************************** */

    /**
     * Stores the state variables of the TG into this memory element
     * @param trafficGenerator
     */
    public void storeTG(TrafficGenerator trafficGenerator){
        for(int i = 0; i < numVCs; i++){
            this.credits[i] = trafficGenerator.getCredits()[i];
            this.states[i] = trafficGenerator.getStates()[i];
        }

        this.sourceQueue = new ArrayList<PacketDescriptor>();
        for(int i = 0; i < trafficGenerator.getSourceQueue().size(); i++){
            this.sourceQueue.add(trafficGenerator.getSourceQueue().get(i));
        }
        this.allocatePriority = trafficGenerator.getAllocatePriority();
        this.packetToSend = trafficGenerator.getPacketToSend();
        this.networkTime = trafficGenerator.getNetworkTime();
        this.localTime = trafficGenerator.getLocalTime();

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

    public int[] getCredits() {
        return credits;
    }

    public void setCredits(int[] credits) {
        this.credits = credits;
    }

    public int[] getStates() {
        return states;
    }

    public void setStates(int[] states) {
        this.states = states;
    }

    public List<PacketDescriptor> getSourceQueue() {
        return sourceQueue;
    }

    public void setSourceQueue(List<PacketDescriptor> sourceQueue) {
        this.sourceQueue = sourceQueue;
    }

    public int getAllocatePriority() {
        return allocatePriority;
    }

    public void setAllocatePriority(int allocatePriority) {
        this.allocatePriority = allocatePriority;
    }

    public int getPacketToSend() {
        return packetToSend;
    }

    public void setPacketToSend(int packetToSend) {
        this.packetToSend = packetToSend;
    }

    public int getNetworkTime() {
        return networkTime;
    }

    public void setNetworkTime(int networkTime) {
        this.networkTime = networkTime;
    }

    public int getNumVCs() {
        return numVCs;
    }

    public void setNumVCs(int numVCs) {
        this.numVCs = numVCs;
    }

    public int getLocalTime() {
        return localTime;
    }

    public void setLocalTime(int localTime) {
        this.localTime = localTime;
    }
}
