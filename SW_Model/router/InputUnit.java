/*
File:           InputUnit.java
Created:        2020/05/09
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

package router;

import building_blocks.Flit;

import java.util.ArrayList;
import java.util.List;

public class InputUnit {
    /* ********************************************************************************
     *                                  VARIABLES                                   *
     ******************************************************************************** */
    /* CONSTANTS */
    public static final int IDLE = 0;
    public static final int ROUTING = 1;
    public static final int WAITING_VC = 2;
    public static final int ACTIVE = 3;
    public static final int WAITING_CREDITS = 4;
    public static final int REACHED_DESTINATION = 5;

    /* State Variables */
    private int globalState;                    // Represents the state of this inputUnit
    private int nextGlobalState;                // Value of globalState in next cycle
    private int outputPort;                     // Output port allocated to this inputUnit
    private int allocatedVC;                    // Virtual channel allocated (at downstream router)

    /* Buffer */
    private List<Flit> buffer;                  // Input buffer: stores incoming flits
    private int bufferSize;                     // Size (in number of flits) of the buffer

    /* Unit variables */
    String ID;                                   // Identifier for debugging purposes.


    /* ********************************************************************************
     *                                   CONSTRUCTORS                               *
     ******************************************************************************** */

    /**
     * Constructor for creating an input unit
     * @param bufferSize: size (in number of flits) of the buffer
     * @param ID: identifier for debugging purposes
     */
    public InputUnit(int bufferSize, String ID){
        this.globalState = IDLE;
        this.nextGlobalState = IDLE;
        this.outputPort = -1;
        this.allocatedVC = -1;
        this.buffer = new ArrayList<Flit>();
        this.bufferSize = bufferSize;
        this.ID = ID;
    }



    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                              *
     ******************************************************************************** */
    /**
     * Updates state of this input unit
     */
    public void updateState(){
        this.globalState = this.nextGlobalState;
    }

    /**
     * Return the first flit of the buffer (the flit is NOT removed from the buffer).
     * @return: first flit of buffer
     */
    public Flit getTopFlit(){
        return this.buffer.get(0);
    }

    /**
     * Return the first flit of the buffer (flit is removed from buffer).
     * @return: first flit of buffer
     */
    public Flit removeTopFlit(){
        return this.buffer.remove(0);
    }


    /**
     * Add a flit to the input buffer
     * @param flit: flit to be added to the buffer
     */
    public void addFlit(Flit flit){
        this.buffer.add(flit);

        // Check buffer overflow:
        if(this.buffer.size() > this.bufferSize){
            throw new RuntimeException("Buffer overflow at " + this.toString());
        }
    }



    /* ********************************************************************************
     *                              GETTERS AND SETTERS                             *
     ******************************************************************************** */

    public int getGlobalState() {
        return globalState;
    }

    public int getNextGlobalState() {
        return nextGlobalState;
    }

    public String getID() {
        return ID;
    }

    public int getOutputPort() {
        return outputPort;
    }

    public int getAllocatedVC() {
        return allocatedVC;
    }

    public List<Flit> getBuffer() {
        return buffer;
    }

    public void setBuffer(List<Flit> buffer) {
        this.buffer = buffer;
    }

    public void setNextGlobalState(int nextGlobalState) {
        this.nextGlobalState = nextGlobalState;
    }

    public void setGlobalState(int globalState) {
        this.globalState = globalState;
    }

    public void setOutputPort(int outputPort) {
        this.outputPort = outputPort;
    }

    public void setAllocatedVC(int allocatedVC) {
        this.allocatedVC = allocatedVC;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    @Override
    public String toString() {
        return this.ID;
    }


}
