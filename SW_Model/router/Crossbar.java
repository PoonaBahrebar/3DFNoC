/*
File:           Crossbar.java
Created:        2020/05/11
Last Changed:   2020/07/18
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
import building_blocks.FlitChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Crossbar {
    /* ********************************************************************************
     *                                  VARIABLES                                   *
     ******************************************************************************** */
    private List<Flit> inputFlits;                    // List of buffers containing flits that need to traverse the switch
    private List<FlitChannel> outputChannels;           // List containing all the output channels
    private List<Integer> configuration;                // Current configuration of the switch
    private int numPorts;                               // Number of input/output ports connected to the switch


    /* Loggers */
    private final static Logger debugLogger = Logger.getLogger("debugLogger");
    private final static Logger resultLogger = Logger.getLogger("resultLogger");


    /* ********************************************************************************
     *                                   CONSTRUCTORS                               *
     ******************************************************************************** */

    /**
     * Constructor for creating crossbar. Input buffers are initialized with dummy flits.
     * @param numPorts: number of input/output ports connected to the switch
     * @param channels: list containing the output channels
     */
    public Crossbar(int numPorts, List<FlitChannel> channels){
        this.numPorts = numPorts;

        this.inputFlits = new ArrayList<Flit>();
        this.outputChannels = new ArrayList<FlitChannel>();
        this.configuration = new ArrayList<Integer>();
        for(int i = 0; i < numPorts; i++){
            this.inputFlits.add(new Flit());
            this.outputChannels.add(channels.get(i));
            this.configuration.add(-1);
        }

    }



    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                              *
     ******************************************************************************** */

    /**
     * Add flit to the buffer of the corresponding input port
     * @param input: number of the input port
     * @param flit: flit that needs to traverse switch
     */
    public void addFlit(int input, Flit flit, int output){
        this.inputFlits.set(input, flit);
        this.configuration.set(output, input);
    }

    /**
     * Traverse flits from input buffers to the output channel.
     * If no input is connected to the output channel, a zero flit is added to the channel
     */
    public void traverseSwitch(){
        for(int output = 0; output < numPorts; output++){
            int input = configuration.get(output);
            Flit flit = new Flit();
            if(input != -1){
                flit = inputFlits.get(input);
                debugLogger.log(Level.FINER, flit.toString() + " is added to channel at output " + output);
            }
            this.outputChannels.get(output).addFlit(flit);



            /* Reset configuration and input buffer */
            this.configuration.set(output, -1);
        }
    }


    /* ********************************************************************************
     *                              GETTERS AND SETTERS                             *
     ******************************************************************************** */

    public List<Flit> getInputFlits() {
        return inputFlits;
    }

    public void setInputFlits(List<Flit> inputFlits) {
        this.inputFlits = inputFlits;
    }

    public List<FlitChannel> getOutputChannels() {
        return outputChannels;
    }

    public void setOutputChannels(List<FlitChannel> outputChannels) {
        this.outputChannels = outputChannels;
    }

    public List<Integer> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(List<Integer> configuration) {
        this.configuration = configuration;
    }

    public int getNumPorts() {
        return numPorts;
    }

    public void setNumPorts(int numPorts) {
        this.numPorts = numPorts;
    }
}
