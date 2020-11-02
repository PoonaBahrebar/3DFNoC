/*
File:           MemoryInputUnit.java
Created:        2020/07/18
Last Changed:   2020/07/20
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

import building_blocks.Flit;
import router.InputUnit;

import java.util.ArrayList;
import java.util.List;

public class MemoryInputUnit {/* CONSTANTS */
    public static final int IDLE = 0;
    public static final int ROUTING = 1;
    public static final int WAITING_VC = 2;
    public static final int ACTIVE = 3;

    /* State Variables */
    private int globalState;                    // Represents the state of this inputUnit
    private int nextGlobalState;                // Value of globalState in next cycle
    private int outputPort;                     // Output port allocated to this inputUnit
    private int allocatedVC;                    // Virtual channel allocated (at downstream router)

    /* Buffer */
    private List<Flit> buffer;                  // Input buffer: stores incoming flits


    /* ********************************************************************************
     *                                   CONSTRUCTORS                               *
     ******************************************************************************** */

    /**
     * Constructor for input unit memory element
     * @param bufferSize: size of the buffer in number of flits
     */
    public MemoryInputUnit(int bufferSize){
        this.globalState = IDLE;
        this.nextGlobalState = IDLE;
        this.outputPort = -1;
        this.allocatedVC = -1;
        this.buffer = new ArrayList<Flit>();

    }



    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                                *
     ******************************************************************************** */

    /**
     * Stores the states variables of the input unit into memory
     * @param inputUnit: unit from which the state variables need to be stored in memory
     */
    public void storeInputUnit(InputUnit inputUnit){
        this.globalState = inputUnit.getGlobalState();
        this.nextGlobalState = inputUnit.getNextGlobalState();
        this.outputPort = inputUnit.getOutputPort();
        this.allocatedVC = inputUnit.getAllocatedVC();
        this.buffer = new ArrayList<Flit>();
        for(int i = 0; i < inputUnit.getBuffer().size(); i++){
            buffer.add(inputUnit.getBuffer().get(i));
        }
    }


    /* ********************************************************************************
     *                              GETTERS AND SETTERS                             *
     ******************************************************************************** */


    public int getGlobalState() {
        return globalState;
    }

    public void setGlobalState(int globalState) {
        this.globalState = globalState;
    }

    public int getNextGlobalState() {
        return nextGlobalState;
    }

    public void setNextGlobalState(int nextGlobalState) {
        this.nextGlobalState = nextGlobalState;
    }

    public int getOutputPort() {
        return outputPort;
    }

    public void setOutputPort(int outputPort) {
        this.outputPort = outputPort;
    }

    public int getAllocatedVC() {
        return allocatedVC;
    }

    public void setAllocatedVC(int allocatedVC) {
        this.allocatedVC = allocatedVC;
    }

    public List<Flit> getBuffer() {
        return buffer;
    }

    public void setBuffer(List<Flit> buffer) {
        this.buffer = buffer;
    }
}
