/*
File:           PacketDescriptor.java
Created:        2020/05/09
Last Changed:   2020/05/09 23:29
Author:         Jonathan D'Hoore
                University of Ghent

Part of Master's dissertation submitted in order to obtain the academic degree of
Master of Science in Electrical Engineering - main subject Electronic Circuits and Systems
Academic year 2019-2020

If you use our 3D NoC Emulator in your research, we would appreciate the following citation in any publications to which it has contributed:
Jonathan D'Hoore, Poona Bahrebar and Dirk Stroobandt, "3D NoC Emulation Model on a Single FPGA,"
In Proceedings of ACM/IEEE International Workshop on System-Level Interconnect Problems and Pathfinding (SLIPP'20), pp. 1-8, 2020.
*/

package building_blocks;

import java.util.logging.Logger;

public class PacketDescriptor {
    /* ********************************************************************************
     *                                  VARIABLES                                   *
     ******************************************************************************** */
    private int[] destination;                      // Destination (y,x) coordinates of the packet
    private int numberOfFlits;                      // Number of flits in this packet
    private int generationTime;                     // Generation time of this packet
    private int allocatedVC;                        // VC allocated to this packet
    private int remainingFlits;                     // Number of flits that still need to be sent
    private String ID;                              // ID used for debugging purposes

    /* Loggers */
    private final static Logger debugLogger = Logger.getLogger("debugLogger");
    private final static Logger resultLogger = Logger.getLogger("resultLogger");

    /* ********************************************************************************
     *                                   CONSTRUCTORS                               *
     ******************************************************************************** */
    /**
     * @param destination: destination (y,x) coordinates of the packet
     * @param numberOfFlits: umber of flits in this packet
     * @param generationTime: generation time of this packet
     */
    public PacketDescriptor(int[] destination, int numberOfFlits, int generationTime){
        this.destination = destination;
        this.numberOfFlits = numberOfFlits;
        this.generationTime = generationTime;
        this.allocatedVC = -1;
        this.remainingFlits = numberOfFlits;
        this.ID = "Packet_D[" + destination[0] + "," + destination[1] + "]_T:" + generationTime;
    }



    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                              *
     ********************************************************************************  */

    /**
     * Decreases the number of remaining flits that need to be sendby one.
     */
    public void decreaseRemainingFlits(){
        this.remainingFlits--;
    }

    /* ********************************************************************************
     *                              GETTERS AND SETTERS                             *
     ******************************************************************************** */
    public int[] getDestination() {
        return destination;
    }

    public int getNumberOfFlits() {
        return numberOfFlits;
    }

    public int getGenerationTime() {
        return generationTime;
    }

    public String getID() {
        return ID;
    }

    public void setDestination(int[] destination) {
        this.destination = destination;
    }

    public void setNumberOfFlits(int numberOfFlits) {
        this.numberOfFlits = numberOfFlits;
    }

    public void setGenerationTime(int generationTime) {
        this.generationTime = generationTime;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getAllocatedVC() {
        return allocatedVC;
    }

    public void setAllocatedVC(int allocatedVC) {
        this.allocatedVC = allocatedVC;
    }

    public int getRemainingFlits() {
        return remainingFlits;
    }

    public void setRemainingFlits(int remainingFlits) {
        this.remainingFlits = remainingFlits;
    }

    @Override
    public String toString() {
        return this.ID;
    }
}
