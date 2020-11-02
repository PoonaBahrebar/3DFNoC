/*
File:           MemoryRouter.java
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

import building_blocks.Credit;
import building_blocks.Flit;
import iSLIP.Arbiter;
import router.InputUnit;
import router.OutputPort;
import router.Router;

import java.util.ArrayList;
import java.util.List;

public class MemoryRouter {

    private int numPorts;
    private int numVCs;
    private int bufferSize;

    private List<List<MemoryInputUnit>> inputUnits;
    private List<List<Credit>> creditBuffers;

    private List<Integer> vcAllocatorOutputPriorities;
    private List<Integer> vcAllocatorInputPriorities;
    private List<Integer> switchAllocatorOutputPriorities;
    private List<Integer> switchAllocatorInputPriorities;

    private List<List<Boolean>> vcAllocatorRequests;

    private List<Flit> crossbarInputs;
    private List<Integer> crossbarConfiguration;

    private List<int[]> outputStates;
    private List<int[]> creditCounters;

    private int[] position;


    /* ********************************************************************************
     *                                   CONSTRUCTORS                               *
     ******************************************************************************** */

    /**
     * Constructor for router memory element
     * @param position: position of the router in the network
     * @param numPorts: number of ports in each router
     * @param numVCs: number of VCs at every port
     * @param bufferSize: size of the buffer (in number of flits) at each input unit
     */
    public MemoryRouter(int[] position, int numPorts, int numVCs, int bufferSize){
        this.numPorts = numPorts;
        this.numVCs = numVCs;
        this.bufferSize = bufferSize;

        this.position = position;

        /* Create input unit memory elements */
        this.inputUnits = new ArrayList<List<MemoryInputUnit>>();
        for(int i = 0; i < numPorts; i++){
            this.inputUnits.add(new ArrayList<MemoryInputUnit>());
            for(int j = 0; j < numVCs; j++) {
                this.inputUnits.get(i).add(new MemoryInputUnit(bufferSize));
            }
        }

        this.creditBuffers = new ArrayList<List<Credit>>();
        for(int i = 0; i < numPorts; i++){
            creditBuffers.add(new ArrayList<Credit>());

        }

        /* Priorities of VC allocator */
        this.vcAllocatorInputPriorities = new ArrayList<Integer>();
        this.vcAllocatorOutputPriorities = new ArrayList<Integer>();
        for(int i = 0; i < numPorts*numVCs; i++){
            this.vcAllocatorInputPriorities.add(0);
            this.vcAllocatorOutputPriorities.add(0);
        }

        /* VC allocator requests */
        this.vcAllocatorRequests = new ArrayList<List<Boolean>>();
        for(int i = 0; i < numPorts*numVCs; i++){
            vcAllocatorRequests.add(new ArrayList<Boolean>());
            for(int j = 0; j < numPorts*numVCs; j++){
                vcAllocatorRequests.get(i).add(false);
            }
        }

        /* Priorities of Switch allocator */
        this.switchAllocatorInputPriorities = new ArrayList<Integer>();
        this.switchAllocatorOutputPriorities = new ArrayList<Integer>();
        for(int i = 0; i < numPorts; i++){
            this.switchAllocatorInputPriorities.add(0);
            this.switchAllocatorOutputPriorities.add(0);
        }

        /* States of output VCs for every output port */
        this.outputStates = new ArrayList<int[]>();
        this.creditCounters = new ArrayList<int[]>();
        for(int i = 0; i < numPorts; i++){
            this.outputStates.add(new int[numVCs]);
            this.creditCounters.add(new int[numVCs]);
            for(int j = 0; j < numVCs; j++){
                creditCounters.get(i)[j] = bufferSize;
            }
        }

        this.crossbarInputs = new ArrayList<Flit>();
        this.crossbarConfiguration = new ArrayList<Integer>();
        for(int i = 0; i < numPorts; i++){
            crossbarInputs.add(new Flit());
            this.crossbarConfiguration.add(-1);
        }


    }


    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                                *
     ******************************************************************************** */

    /**
     * Store the variables of the router into this memory element
     * @param router
     */
    public void storeRouter(Router router){
        /* Store input units */
        for(int i = 0; i < numPorts; i++){
            for(int j = 0; j < numVCs; j++){
                this.inputUnits.get(i).get(j).storeInputUnit(router.getInputUnit(i,j));
            }
        }

        /* Store credit buffer at each input port */
        for(int i = 0; i < numPorts; i++){
            this.creditBuffers.get(i).clear();
            for(int j = 0; j < router.getInputPort(i).getCreditBuffer().size(); j++){
                this.creditBuffers.get(i).add(router.getInputPort(i).getCreditBuffer().get(j));
            }
        }

        /* Store VC allocator priorities */
        for(int i = 0; i < numPorts*numVCs; i++){
            Arbiter currentInputArbiter = router.getVcAllocator().getInputArbiters().get(i);
            this.vcAllocatorInputPriorities.set(i,currentInputArbiter.getPriority());

            Arbiter currentOutputArbiter = router.getVcAllocator().getOutputArbiters().get(i);
            this.vcAllocatorOutputPriorities.set(i, currentOutputArbiter.getPriority());
        }

        /* Store VC allocator requests */
        this.vcAllocatorRequests = new ArrayList<List<Boolean>>();
        for(int i = 0; i < numPorts*numVCs; i++){
            vcAllocatorRequests.add(new ArrayList<Boolean>());
            for(int j = 0; j < numPorts*numVCs; j++){
                vcAllocatorRequests.get(i).add(router.getVcAllocator().getRequests().get(i).get(j));
            }
        }

        /* Store states from output VCS */
        this.outputStates = new ArrayList<int[]>();
        this.creditCounters = new ArrayList<int[]>();
        for (int i = 0; i < numPorts; i++){
            this.outputStates.add(new int[numVCs]);
            this.creditCounters.add(new int[numVCs]);
            for(int j = 0; j < numVCs; j++){
                this.outputStates.get(i)[j] = router.getOutputPort(i).getGlobalStates()[j];
                this.creditCounters.get(i)[j] = router.getOutputPort(i).getCredits()[j];
            }
        }

        /* Store Switch Allocator priorities */
        for(int i = 0; i < numPorts; i++){
            Arbiter currentInputArbiter = router.getSwitchAllocator().getInputArbiters().get(i);
            this.switchAllocatorInputPriorities.set(i, currentInputArbiter.getPriority());

            Arbiter currentOutputArbiter =  router.getSwitchAllocator().getOutputArbiters().get(i);
            this.switchAllocatorOutputPriorities.set(i, currentOutputArbiter.getPriority());
        }

        /* Store crossbar inputs and configuration */
        this.crossbarConfiguration = new ArrayList<Integer>();
        for(int i = 0; i < router.getCrossbar().getConfiguration().size(); i++){
            this.crossbarConfiguration.add(router.getCrossbar().getConfiguration().get(i));
        }
        this.crossbarInputs = new ArrayList<Flit>();
        for(int i = 0; i < router.getCrossbar().getInputFlits().size(); i++){
            this.crossbarInputs.add(router.getCrossbar().getInputFlits().get(i));
        }

    }


    /**
     * Checks if the router is currently idle
     * @return
     */
    public boolean isIdle(){
        boolean idle = true;
        for(int i = 0; i < numPorts && idle; i++){
            for(int j = 0; j < numVCs; j++){
                /* As soon as 1 element is not idle, the router itself is not idle */
                if(inputUnits.get(i).get(j).getGlobalState() != InputUnit.IDLE || outputStates.get(i)[j] != OutputPort.IDLE) {
                    idle = false;
                    break;
                }
            }
        }
        return idle;
    }

    /* ********************************************************************************
     *                              GETTERS AND SETTERS                             *
     ******************************************************************************** */

    public List<List<MemoryInputUnit>> getInputUnits() {
        return inputUnits;
    }

    public void setInputUnits(List<List<MemoryInputUnit>> inputUnits) {
        this.inputUnits = inputUnits;
    }

    public List<Integer> getVcAllocatorOutputPriorities() {
        return vcAllocatorOutputPriorities;
    }

    public void setVcAllocatorOutputPriorities(List<Integer> vcAllocatorOutputPriorities) {
        this.vcAllocatorOutputPriorities = vcAllocatorOutputPriorities;
    }

    public List<Integer> getVcAllocatorInputPriorities() {
        return vcAllocatorInputPriorities;
    }

    public void setVcAllocatorInputPriorities(List<Integer> vcAllocatorInputPriorities) {
        this.vcAllocatorInputPriorities = vcAllocatorInputPriorities;
    }

    public List<Integer> getSwitchAllocatorOutputPriorities() {
        return switchAllocatorOutputPriorities;
    }

    public void setSwitchAllocatorOutputPriorities(List<Integer> switchAllocatorOutputPriorities) {
        this.switchAllocatorOutputPriorities = switchAllocatorOutputPriorities;
    }

    public List<Integer> getSwitchAllocatorInputPriorities() {
        return switchAllocatorInputPriorities;
    }

    public void setSwitchAllocatorInputPriorities(List<Integer> switchAllocatorInputPriorities) {
        this.switchAllocatorInputPriorities = switchAllocatorInputPriorities;
    }

    public List<int[]> getOutputStates() {
        return outputStates;
    }

    public void setOutputStates(List<int[]> outputStates) {
        this.outputStates = outputStates;
    }

    public int[] getPosition() {
        return position;
    }

    public void setPosition(int[] position) {
        this.position = position;
    }

    public List<Flit> getCrossbarInputs() {
        return crossbarInputs;
    }

    public void setCrossbarInputs(List<Flit> crossbarInputs) {
        this.crossbarInputs = crossbarInputs;
    }

    public List<Integer> getCrossbarConfiguration() {
        return crossbarConfiguration;
    }

    public void setCrossbarConfiguration(List<Integer> crossbarConfiguration) {
        this.crossbarConfiguration = crossbarConfiguration;
    }

    public List<List<Boolean>> getVcAllocatorRequests() {
        return vcAllocatorRequests;
    }

    public void setVcAllocatorRequests(List<List<Boolean>> vcAllocatorRequests) {
        this.vcAllocatorRequests = vcAllocatorRequests;
    }

    public List<List<Credit>> getCreditBuffers() {
        return creditBuffers;
    }

    public void setCreditBuffers(List<List<Credit>> creditBuffers) {
        this.creditBuffers = creditBuffers;
    }

    public List<int[]> getCreditCounters() {
        return creditCounters;
    }

    public void setCreditCounters(List<int[]> creditCounters) {
        this.creditCounters = creditCounters;
    }
}
