/*
File:           Router.java
Created:        2020/05/10
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

package router;

import building_blocks.Credit;
import building_blocks.CreditChannel;
import building_blocks.Flit;
import building_blocks.FlitChannel;
import iSLIP.Allocator;
import iSLIP.Arbiter;
import memory.MemoryInputUnit;
import memory.MemoryRouter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Router {
    /* - - - - - - - - - - -  - - - IMPORTANT - - - - - - - - - - - - - -
            Assumptions made with respect to the numbering of the ports:
            0 : EAST
            1 : SOUTH
            2 : WEST
            3 : NORTH
            4 : UP
            5 : DOWN
            6 : IPCORE

            The router is a 5-stage, input queued router. For more details see:
            "W. J. Dally and B. P. Towles, Principles and practices of interconnection networks. Elsevier, 2004."
        - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */
    /* ********************************************************************************
     *                                  VARIABLES                                   *
     ******************************************************************************** */

    /* Input and output ports */
    private List<InputPort> inputPorts;                         // List containing all the input ports
    private List<OutputPort> outputPorts;                       // List containing all the output ports
    private int numPorts;                                       // Number of input/output ports
    private int numVCs;                                         // Number of virtual channels per input port


    /* Router architecture elements */
    private RoutingUnit routingUnit;                            // Implements routing function
    private Allocator vcAllocator;                              // Implements VC allocation (iSLIP)
    private Allocator switchAllocator;                          // Implements Switch allocation (iSLIP)
    private List<Integer> inputUnitPriorities;                  // Priorities used for selecting an input unit during Switch Allocation
    private Crossbar crossbar;                                      // Implements switch


    /* Router and network variables */
    private int[] position;                                     // Position of the router: (z,y,x) coordinates
    private int radix;                                          // Radix of the mesh topology (n x n x n mesh has radix n)
    private String ID;                                          // Identifier for debugging purposes

    /* Adaptive routing variables */
    private boolean adaptive;
    private int[] routerLoads;


    /* Loggers */
    private final static Logger debugLogger = Logger.getLogger("debugLogger");
    private final static Logger resultLogger = Logger.getLogger("resultLogger");

    /* ********************************************************************************
     *                                   CONSTRUCTORS                               *
     ******************************************************************************** */

    /**
     * Constructor for creating one router
     * @param numPorts: number of input/output ports for this router
     * @param numVCs: number of virtual channels at each input port
     * @param bufferSize: size (in number of flits) of the buffer of each virtual channel
     * @param position: position (z,y,x) of this router
     * @param inputChannels: list that contains all the input channels (one for each input port)
     * @param inputCreditChannels: list that contains all the input credit channels (one for each input port)
     * @param outputChannels: list that contains all the output channels (one for each output port)
     * @param outputCreditChannels: list that contains all the output credit channels (one for each output port)
     */
    public Router(int numPorts, int numVCs, int bufferSize, int[] position, int radix, boolean adaptive,
                  List<FlitChannel> inputChannels, List<CreditChannel> inputCreditChannels, List<FlitChannel> outputChannels, List<CreditChannel> outputCreditChannels){
        /* Router variables */
        this.position = position;
        this.ID = "R[" + position[0] + "," + position[1] + "," + position[2] + "]";
        this.radix = radix;
        this.numPorts = numPorts;
        this.numPorts = numPorts;
        this.numVCs = numVCs;

        /* Adaptive routing variables */
        this.adaptive = adaptive;
        this.routerLoads = new int[numPorts-1];

        /* Add ports */
        this.inputPorts = new ArrayList<InputPort>();
        this.outputPorts = new ArrayList<OutputPort>();
        for(int i = 0; i < numPorts; i++){
            /* Add input port */
            FlitChannel inputChannel = inputChannels.get(i);
            CreditChannel inputCreditChannel = inputCreditChannels.get(i);
            String inputPortID = ID + "IP" + i;
            this.inputPorts.add(new InputPort(numVCs, bufferSize, inputChannel, inputCreditChannel, inputPortID));

            /* Add output port */
            FlitChannel outputChannel = outputChannels.get(i);
            CreditChannel outputCreditChannel = outputCreditChannels.get(i);
            String outputPortID = ID + "OP" + i;
            this.outputPorts.add(new OutputPort(numVCs, bufferSize, outputChannel, outputCreditChannel, outputPortID));
        }

        /* Initialize router architecture elements */
        this.routingUnit = new RoutingUnit(position, radix);
        this.vcAllocator = new Allocator(numPorts * numVCs, numPorts*numVCs);
        this.switchAllocator = new Allocator(numPorts, numPorts);
        this.inputUnitPriorities = new ArrayList<Integer>();
        for(int i = 0; i < numPorts; i++){
            this.inputUnitPriorities.add(0);
        }
        this.crossbar = new Crossbar(numPorts, outputChannels);

    }


    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                              *
     ******************************************************************************** */

    /**
     * Overall controlling function simulating the router behaviour.
     */
    public void simulateRouter(){
        /* Receiving of flits */
        this.flitReceiving();

        /* Sending of credits */
        this.creditSending();

        /* Load receiving, updating and sending */
        this.loadUpdater();

        /* Switch traversal */
        this.crossbar.traverseSwitch();

        /* Switch allocation */
        this.switchAllocation();

        /* VC allocation */
        this.vcAllocation();

        /* Route computation */
        this.routeComputation();

        /* Credit Receiving */
        this.creditReceiving();

        /* Update states */
        this.updateStates();


    }


    /**
     * Route computation for all input units who are currently in the ROUTING state.
     * Output port is saved as internal variable in the input unit.
     * State is updated to process to VC allocation + request for VC allocation is made.
     * Input units who are not in the ROUTING state will be ignored.
     */
    private void routeComputation(){
        for(int i = 0; i < numPorts; i++){
            for(int j = 0; j < numVCs; j++){
                InputUnit currentUnit = this.getInputUnit(i,j);

                /* Check if routing needs to be computed */
                if(currentUnit.getGlobalState() == InputUnit.ROUTING){
                    Flit headerFlit = currentUnit.getTopFlit();
                    debugLogger.log(Level.FINER, "ROUTING at " + currentUnit + " of " + headerFlit);

                    /* Compute output port */
                    int outputPort;
                    if(adaptive){
                        outputPort = this.routingUnit.minimalAdaptiveRouting(headerFlit, this.routerLoads);
                    } else {
                        outputPort = this.routingUnit.xyzRouting(headerFlit);
                    }

                    /* Update state fields */
                    currentUnit.setOutputPort(outputPort);
                    currentUnit.setNextGlobalState(InputUnit.WAITING_VC);

                    /* Add request for VC allocation: request all VCs at the output port */
                    int input = i*numVCs + j;
                    for(int k = 0; k < numVCs; k++){
                        int output = outputPort*numVCs + k;
                        this.vcAllocator.addRequest(input, output);
                    }

                }
            }
        }
    }

    /**
     * Controlling function for VC allocation. iSLIP scheme is used for allocation.
     * Input units that obtained grant will update their internal variables and go to ACTIVE state.
     * Allocated output units will go to ACTIVE state.
     */
    private void vcAllocation(){
        /*                                  NOTE:
         * NEW request for VC allocation are generated at the end of Route Computation.
         * If a request has resulted in a grant, the request needs to be removed.
         * If the request does not result in a grant, it needs to be applied again */

        /* First update the states for the output arbitration */
        for(int i = 0; i < numPorts; i++){
            for(int j = 0; j < numVCs; j++){
                int output = i*numVCs + j;
                int state = outputPorts.get(i).getGlobalStates()[j];
                this.vcAllocator.updateState(output,state);
            }
        }

        /* iSLIP allocation to allocate outputs to inputs */
        this.vcAllocator.simulate();

        List<List<Boolean>> grants = vcAllocator.getGrants();

        /* Update field variables of granted inputs */
        for(int i = 0; i < numPorts; i++){
            for(int j = 0; j < numVCs; j++){
                int input = i*numVCs + j;

                /* Check if this input is granted access to an output VC */
                int output = grants.get(input).indexOf(true);
                if(output != -1){
                    InputUnit inputUnit = this.getInputUnit(i,j);
                    int outputVC = output % numVCs;
                    int outputPortNumber = (output - outputVC) / numVCs;

                    /* Update input unit fields */
                    inputUnit.setAllocatedVC(outputVC);
                    inputUnit.setNextGlobalState(InputUnit.ACTIVE);

                    /* Update output unit state */
                    OutputPort outputPort = outputPorts.get(outputPortNumber);
                    outputPort.setNextGlobalState(outputVC, OutputPort.ACTIVE);

                    /* Reset requests */
                    vcAllocator.resetAllRequests(input);

                    /* Debug logging */
                    String outputUnit = outputPort.toString() + "U" + j;
                    debugLogger.log(Level.FINER, "VC ALLOCATION at " + inputUnit + " of " + inputUnit.getTopFlit() + ". Assigned unit: " + outputUnit);


                }
            }
        }
    }


    /**
     * Switch allocation stage.
     */
    private void switchAllocation(){
        /* Make requests */
        for(int i = 0; i < numPorts; i++){
            for(int j = 0; j < numVCs; j++){
                InputUnit currentUnit = this.getInputUnit(i,j);

                if(currentUnit.getGlobalState() == InputUnit.ACTIVE && currentUnit.getBuffer().size() > 0 && this.getCredits(currentUnit) > 0 ){
                        int input = i;      // This input PORT requests access to the switch
                        int output =  currentUnit.getOutputPort();
                        this.switchAllocator.addRequest(input, output);
                }
            }
        }

        /* NOTE: no need to update the states in switch allocation,
         * because the 'resources' are never allocated during multiple cycles,
         * so they are always IDLE at start of allocation */

        /* iSLIP allocation */
        this.switchAllocator.simulate();

        List<List<Boolean>> grants = this.switchAllocator.getGrants();

        /* Find input PORT that has been granted access to switch */
        for(int i = 0; i < numPorts; i++){
            int output = grants.get(i).indexOf(true);
            if(output != -1){
                /* Input port i is granted access to the switch, select 1 of the VCs to send a flit */
                for(int j = 0; j < numVCs; j++){
                    int VC = (inputUnitPriorities.get(i) + j) % numVCs;
                    InputUnit inputUnit = this.getInputUnit(i,VC);
                    if(inputUnit.getOutputPort() == output && inputUnit.getGlobalState() == InputUnit.ACTIVE && inputUnit.getBuffer().size() > 0 && this.getCredits(inputUnit) > 0){

                        /* Apply flit to the switch */
                        Flit flit = inputUnit.removeTopFlit();
                        flit.setVC(inputUnit.getAllocatedVC());
                        this.crossbar.addFlit(i, flit, output);

                        debugLogger.log(Level.FINER, "SWITCH ALLOCATION at " + inputUnit + " of " + flit);

                        /* Update credit counter for this output VC */
                        this.outputPorts.get(output).decreaseCreditCount(inputUnit.getAllocatedVC());

                        /* Send credit to upstream router */
                        inputPorts.get(i).storeCredit(new Credit(VC));

                        /* Update priority for selecting VC at input port */
                        int newPriority = (VC + 1) % numVCs;
                        this.inputUnitPriorities.set(i, newPriority);

                        /* If this flit is a TAIL flit, the resources need te be released again! */
                        if(flit.getType() == Flit.TAIL_FLIT){
                            /* Set output VC state back to idle */
                            outputPorts.get(output).setNextGlobalState(inputUnit.getAllocatedVC(), OutputPort.IDLE);

                            /* Debug logging */
                            String outputUnit = outputPorts.get(output).toString() + "U" + inputUnit.getAllocatedVC();
                            debugLogger.log(Level.FINER, "Tail flit releases " + outputUnit + " at " + inputUnit);

                            /* Reset input unit fields */
                            inputUnit.setOutputPort(-1);
                            inputUnit.setAllocatedVC(-1);


                            /* Set input unit to IDLE or ROUTING */
                            if(inputUnit.getBuffer().size() == 0){
                                inputUnit.setNextGlobalState(InputUnit.IDLE);
                            } else {
                                inputUnit.setNextGlobalState(InputUnit.ROUTING);
                            }
                        }
                        break;
                    }
                }
            }
        }

        /* Reset all requests */
        for(int i = 0; i < numPorts; i++) {
            this.switchAllocator.resetAllRequests(i);
        }


    }

    /**
     * Receiving of load signals and updating internal signal. Also sending of current load to neighbouring routers
     */
    private void loadUpdater(){
        // Simple load determination based on used VCs
        for(int i = 0; i < numPorts-1; i++){
            int load = 0;
            for(int j = 0; j < numVCs; j++) {
                load += this.getOutputPort(i).getGlobalStates()[j];
            }
            this.routerLoads[i] = load;
        }
    }


    /**
     * Updating of states of the input and output units.
     */
    private void updateStates(){
        for(int i = 0; i < numPorts; i++){
            /* Update input unit states */
            for(int j = 0; j < numVCs; j++){
                InputUnit inputUnit = this.getInputUnit(i,j);
                inputUnit.updateState();
            }

            /* Update output unit states */
            outputPorts.get(i).updateStates();
        }
    }

    /**
     * Determines whether or not this router is IDLE
     * @return boolean idle: indicates state of router
     */
    public boolean isIdle(){
        boolean idle = true;
        for(int i = 0; i < numPorts && idle; i++){
            for(int j = 0; j < numVCs; j++){
                /* As soon as 1 element is not idle, the router itself is not idle */
                if(getInputUnit(i,j).getGlobalState() != InputUnit.IDLE || outputPorts.get(i).getGlobalStates()[j] != OutputPort.IDLE) {
                    idle = false;
                    break;
                }
            }
        }
        return idle;
    }

    /**
     * Sending of credits (to the upstream router) at the input ports.
     */
    private void creditSending(){
        /* Sending of credits at input ports */
        for(int i = 0; i < numPorts; i++){
            inputPorts.get(i).sendCredit();
        }
    }

    /**
     * Receiving from credits (of the downstream router) at the output ports.
     */
    private void creditReceiving(){
        for(int i = 0; i < numPorts; i++){
            outputPorts.get(i).receiveCredit();
        }
    }

    /**
     * Receive flits at all input ports.
     */
    private void flitReceiving(){
        for(int i = 0; i < numPorts; i++){
            inputPorts.get(i).receiveFlit();
        }
    }



    /**
     * Load the state from memory into this router
     * @param memoryRouter: memory element containing information for this router
     */
    public void loadState(MemoryRouter memoryRouter){

        this.position = memoryRouter.getPosition();

        this.routingUnit.setPosition(this.position);

        /* Load states into input units */
        for(int i = 0; i < numPorts; i++){
            for(int j = 0; j < numVCs; j++){
                MemoryInputUnit loadUnit = memoryRouter.getInputUnits().get(i).get(j);
                InputUnit currentUnit = this.getInputUnit(i,j);
                currentUnit.setGlobalState(loadUnit.getGlobalState());
                currentUnit.setNextGlobalState(loadUnit.getNextGlobalState());
                currentUnit.setAllocatedVC(loadUnit.getAllocatedVC());
                currentUnit.setOutputPort(loadUnit.getOutputPort());
                currentUnit.setBuffer(loadUnit.getBuffer());
            }
        }

        /* Load Credit buffers at input ports */
        for(int i = 0; i < numPorts; i++){
            this.inputPorts.get(i).getCreditBuffer().clear();
            for(int j = 0; j < memoryRouter.getCreditBuffers().get(i).size(); j++){
                this.inputPorts.get(i).getCreditBuffer().add(memoryRouter.getCreditBuffers().get(i).get(j));
            }
        }

        /* Load VC allocator priorities */
        for(int i = 0; i < numPorts*numVCs; i++){
            int inputPriority = memoryRouter.getVcAllocatorInputPriorities().get(i);
            Arbiter currentInputArbiter = this.vcAllocator.getInputArbiters().get(i);
            currentInputArbiter.setPriority(inputPriority);

            int outputPriority = memoryRouter.getVcAllocatorOutputPriorities().get(i);
            Arbiter currentOutputArbiter = this.vcAllocator.getOutputArbiters().get(i);
            currentOutputArbiter.setPriority(outputPriority);
        }

        /* Load VC allocator requests */
        for(int i = 0; i < numPorts*numVCs; i++){
            vcAllocator.resetAllRequests(i);
            for(int j = 0; j < numPorts*numVCs; j++){
                if(memoryRouter.getVcAllocatorRequests().get(i).get(j)){
                    vcAllocator.addRequest(i,j);
                }
            }
        }

        /* Update states from output VCS */
        for (int i = 0; i < numPorts; i++){
            for(int j = 0; j < numVCs; j++){
                int state = memoryRouter.getOutputStates().get(i)[j];
                int creditCount = memoryRouter.getCreditCounters().get(i)[j];
                this.outputPorts.get(i).setCredits(j, creditCount);
                this.outputPorts.get(i).setGlobalState(j, state);
                this.outputPorts.get(i).setNextGlobalState(j, state);
            }
        }

        /* Load Switch Allocator priorities */
        for(int i = 0; i < numPorts; i++){
            int inputPriority = memoryRouter.getSwitchAllocatorInputPriorities().get(i);
            Arbiter currentInputArbiter = this.switchAllocator.getInputArbiters().get(i);
            currentInputArbiter.setPriority(inputPriority);

            int outputPriority = memoryRouter.getSwitchAllocatorOutputPriorities().get(i);
            Arbiter currentOutputArbiter = this.switchAllocator.getOutputArbiters().get(i);
            currentOutputArbiter.setPriority(outputPriority);
        }

        /* Load crossbar inputs and configuration */
        this.crossbar.setConfiguration(memoryRouter.getCrossbarConfiguration());
        this.crossbar.setInputFlits(memoryRouter.getCrossbarInputs());


        /* Set IDs */
        this.ID = "R[" + position[0] + "," + position[1] + "," + position[2] + "]";
        for(int i = 0; i < numPorts; i++){
            this.inputPorts.get(i).setID(ID + "IP" + i);
            this.outputPorts.get(i).setID(ID + "OP" + i);
            for(int j = 0; j < numVCs; j++){
                this.getInputUnit(i,j).setID(this.ID + "IP" + i +  "U" + j);
            }
        }
        this.routingUnit.setID("RoutingUnit@[" + position[0] + "," + position[1] + "," + position[2] + "]");
    }


    /* ********************************************************************************
     *                              GETTERS AND SETTERS                             *
     ******************************************************************************** */

    public InputUnit getInputUnit(int port, int  VC){
        return this.inputPorts.get(port).getInputUnits().get(VC);
    }

    public InputPort getInputPort(int port){
        return this.inputPorts.get(port);
    }

    public int getCredits(InputUnit inputUnit){
        OutputPort outputPort = outputPorts.get(inputUnit.getOutputPort());
        return outputPort.getCredits()[inputUnit.getAllocatedVC()];
    }

    public Allocator getVcAllocator(){
        return this.vcAllocator;
    }

    public Allocator getSwitchAllocator(){
        return this.switchAllocator;
    }

    public OutputPort getOutputPort(int i){
        return this.outputPorts.get(i);
    }

    public Crossbar getCrossbar() {
        return crossbar;
    }

    public void setCrossbar(Crossbar crossbar) {
        this.crossbar = crossbar;
    }

    public int getNumVCs(){
        return this.numVCs;
    }

    @Override
    public String toString() {
        return this.ID;
    }
}
