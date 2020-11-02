/*
File:           Allocator.java
Created:        2020/05/10
Last Changed:   2020/05/12
Author:         Jonathan D'Hoore
                University of Ghent

Part of Master's dissertation submitted in order to obtain the academic degree of
Master of Science in Electrical Engineering - main subject Electronic Circuits and Systems
Academic year 2019-2020

If you use our 3D NoC Emulator in your research, we would appreciate the following citation in any publications to which it has contributed:
Jonathan D'Hoore, Poona Bahrebar and Dirk Stroobandt, "3D NoC Emulation Model on a Single FPGA,"
In Proceedings of ACM/IEEE International Workshop on System-Level Interconnect Problems and Pathfinding (SLIPP'20), pp. 1-8, 2020.
*/

/**
 * The implemented allocation scheme is iSLIP:
 * N. McKeown, “The islip scheduling algorithm for input-queued switches,” IEEE/ACM transactions on networking, vol. 7, no. 2, pp. 188–201, 1999.
 */

package iSLIP;

import router.OutputPort;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Allocator {
    /* ********************************************************************************
     *                                  VARIABLES                                   *
     ******************************************************************************** */
    /* Request and grants */
    private List<List<Boolean>> requests;                       // Requests: input requests output
    private List<List<Boolean>> grants;                         // Grants: input is granted access to output

    /* Internal variables */
    private int numInputs;                                      // Number of inputs (requesters)
    private int numOutputs;                                     // Number of outputs (resources)

    /* Arbiters */
    private List<Arbiter> outputArbiters;                       // List containing all the output arbiters
    private List<Arbiter> inputArbiters;                        // List containing all the input arbiters

    /* Output states */
    private List<Integer> outputStates;                         // States for every output resource indicating whether it is IDLE (0) or ACTIVE (1)



    /* Loggers */
    private final static Logger debugLogger = Logger.getLogger("debugLogger");
    private final static Logger resultLogger = Logger.getLogger("resultLogger");

    /* ********************************************************************************
     *                                   CONSTRUCTORS                               *
     ******************************************************************************** */

    /**
     * Constructor for creating an allocator (iSLIP scheme)
     * @param numInputs: number of inputs (requesters)
     * @param numOutputs: number of outputs (resources)
     */
    public Allocator(int numInputs, int numOutputs){
        this.numInputs = numInputs;
        this.numOutputs = numOutputs;

        /* Create input arbiters */
        this.inputArbiters = new ArrayList<Arbiter>();
        for(int i = 0; i < numInputs; i++){
            this.inputArbiters.add(new Arbiter());
        }

        /* Create output arbiters */
        this.outputArbiters = new ArrayList<Arbiter>();
        for(int i = 0; i < numOutputs; i++){
            this.outputArbiters.add(new Arbiter());
        }

        /* Initialize requests/grants */
        this.requests = new ArrayList<List<Boolean>>();
        this.grants = new ArrayList<List<Boolean>>();
        for(int i = 0; i < numInputs; i++){
            requests.add(new ArrayList<Boolean>());
            grants.add(new ArrayList<Boolean>());
            for(int j = 0; j < numOutputs; j++){
                requests.get(i).add(false);
                grants.get(i).add(false);
            }
        }

        this.outputStates = new ArrayList<Integer>();
        for(int i = 0; i < numOutputs; i++){
            outputStates.add(OutputPort.IDLE);
        }
    }


    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                              *
     ******************************************************************************** */

    /**
     * Simulate allocation according to the iSLIP scheme (output first)
     */
    public void simulate(){
        // Initialize intermediate requests (requests for input)
        List<List<Boolean>> inputArbiterRequests = new ArrayList<List<Boolean>>();
        for(int i = 0; i < numInputs; i++){
            inputArbiterRequests.add(new ArrayList<Boolean>());
            for(int j = 0; j < numOutputs; j++){
                inputArbiterRequests.get(i).add(false);
            }
        }

        /* Perform arbitration over output arbiters, taking into account the state of the resource */
        for(int i = 0; i < numOutputs; i++){
            // Create requests for each arbiter
            List<Boolean> arbiterRequests = new ArrayList<Boolean>();
            for(int j = 0; j < numInputs; j++){
                arbiterRequests.add(this.requests.get(j).get(i));
            }

            // Perform arbitration for this specific arbiter
            int state = outputStates.get(i);
            List<Boolean> arbiterGrants = this.outputArbiters.get(i).request(arbiterRequests, state);

            // Combine grants from this specific arbiter to corresponding request at input
            for(int j= 0; j < numInputs; j++){
                inputArbiterRequests.get(j).set(i, arbiterGrants.get(j));
            }
        }


        /* Perform arbitration over input arbiters */
        for(int i = 0; i < numInputs; i++){
            // Perform arbitration for this specific arbiter
            List<Boolean> arbiterGrants = inputArbiters.get(i).request(inputArbiterRequests.get(i));

            // Add grants from this specific arbiter to the overall grants
            for(int j = 0; j < numOutputs; j++){
                grants.get(i).set(j, arbiterGrants.get(j));
            }
        }

        /* Update priority of arbiters which resulted in a grant */
        for(int i = 0; i < numInputs; i++){
            for(int j = 0; j < numOutputs; j++){
                if(grants.get(i).get(j)){
                    // Update priority of input arbiter
                    int inputPriority = (j + 1) % numOutputs;
                    inputArbiters.get(i).setPriority(inputPriority);

                    // Update priority of output arbiter
                    int outputPriority = (i + 1) % numInputs;
                    outputArbiters.get(j).setPriority(outputPriority);
                }
            }
        }
    }

    /**
     * Add a requst: input requests output resource
     * @param input: input that requests a resource
     * @param output: output resource that is requested
     */
    public void addRequest(int input, int output){
        this.requests.get(input).set(output, true);
    }

    public void resetAllRequests(int input){
        for(int i = 0; i < numOutputs; i++) {
            this.requests.get(input).set(i, false);
        }
    }

    public void updateState(int output, int state){
        this.outputStates.set(output, state);
    }


    /* ********************************************************************************
     *                              GETTERS AND SETTERS                             *
     ******************************************************************************** */

    public List<List<Boolean>> getRequests() {
        return requests;
    }

    public void setRequests(List<List<Boolean>> requests) {
        this.requests = requests;
    }

    public List<List<Boolean>> getGrants() {
        return grants;
    }

    public void setGrants(List<List<Boolean>> grants) {
        this.grants = grants;
    }

    public int getNumInputs() {
        return numInputs;
    }

    public void setNumInputs(int numInputs) {
        this.numInputs = numInputs;
    }

    public int getNumOutputs() {
        return numOutputs;
    }

    public void setNumOutputs(int numOutputs) {
        this.numOutputs = numOutputs;
    }

    public List<Arbiter> getOutputArbiters() {
        return outputArbiters;
    }

    public void setOutputArbiters(List<Arbiter> outputArbiters) {
        this.outputArbiters = outputArbiters;
    }

    public List<Arbiter> getInputArbiters() {
        return inputArbiters;
    }

    public void setInputArbiters(List<Arbiter> inputArbiters) {
        this.inputArbiters = inputArbiters;
    }
}
