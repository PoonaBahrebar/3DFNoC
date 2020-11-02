/*
File:           MemoryIPCore.java
Created:        2020/07/18
Last Changed:   2020/08/08
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

import ipCore.IPCore;

public class MemoryIPCore {
    private MemoryTG trafficGenerator;
    private MemoryTR trafficReceiver;
    private int[] position;


    /* ********************************************************************************
     *                                   CONSTRUCTORS                               *
     ******************************************************************************** */

    /**
     * Constructor of memory element for IP core.
     * @param position: position of the router
     * @param numVCs: number of virtual channels at each port
     */
    public MemoryIPCore(int[] position, int numVCs, int bufferSize){
        this.position = position;
        this.trafficGenerator = new MemoryTG(position, numVCs, bufferSize);
        this.trafficReceiver = new MemoryTR(position);
    }



    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                                *
     ******************************************************************************** */

    /**
     * Store state variables into this memory element
     * @param ipCore: IP core of which the state variables need to be stored.
     */
    public void storeIPCore(IPCore ipCore){
        this.trafficGenerator.storeTG(ipCore.getTrafficGenerator());
        this.trafficReceiver.storeTR(ipCore.getTrafficReceiver());
    }




    /* ********************************************************************************
     *                              GETTERS AND SETTERS                             *
     ******************************************************************************** */

    public MemoryTG getTrafficGenerator() {
        return trafficGenerator;
    }

    public MemoryTR getTrafficReceiver() {
        return trafficReceiver;
    }

    public int[] getPosition() {
        return position;
    }

    public void setPosition(int[] position) {
        this.position = position;
    }
}
