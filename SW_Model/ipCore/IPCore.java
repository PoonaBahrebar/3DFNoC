/*
File:           IPCore.java
Created:        2020/05/12
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

package ipCore;

import building_blocks.Credit;
import building_blocks.CreditChannel;
import building_blocks.Flit;
import building_blocks.FlitChannel;
import memory.MemoryIPCore;
import memory.MemoryTG;
import memory.MemoryTR;

import java.util.logging.Logger;

public class IPCore {
    /* ********************************************************************************
     *                                  VARIABLES                                     *
     ******************************************************************************** */
    private TrafficGenerator trafficGenerator;
    private TrafficReceiver trafficReceiver;

    private int numVCs;

    /* Flit and credit channel */
    private FlitChannel inputChannel;            // Channel used for receiving flit from upstream router
    private CreditChannel inputCreditChannel;         // Channel used for sending credit to upstream router

    private String ID;

    /* Loggers */
    private final static Logger debugLogger = Logger.getLogger("debugLogger");
    private final static Logger resultLogger = Logger.getLogger("resultLogger");

    /* ********************************************************************************
     *                                   CONSTRUCTORS                               *
     ******************************************************************************** */

    /**
     * Constructor for IP core and underlying elements (TG and TR)
     * @param position: coordinates of the connected router
     * @param radix: radix of the network (size of the network in one dimension)
     * @param numVCs: number of VCs per port
     * @param bufferSize: size of the buffer at each input unit (in number of flits)
     * @param sourceQueueSize: size of the source queue (in number of packets)
     * @param flitsPerPacket: number of flits per packet
     * @param prob: probability that a packet will be generated (prob/precision)
     * @param precision: determines precision of probability
     * @param hotspots: array containing number of routers that are hotspot routers
     * @param hotSpotFactor: factor indicating how much more traffic is sent to hotspot nodes
     * @param rentExponent: rent exponent
     * @param outputChannel: channel used for sending flits from the IP core to the router
     * @param outputCreditChannel: channel used for sending credits from IP core to router
     * @param inputChannel: channel used for sending flits from router to IP core
     * @param inputCreditChannel: channel used for sending credits from router to IP core
     */
    public IPCore(int[] position, int radix, int numVCs, int bufferSize, int sourceQueueSize, int flitsPerPacket, int prob, int precision, int[] hotspots, double hotSpotFactor, double rentExponent,
                  FlitChannel outputChannel, CreditChannel outputCreditChannel, FlitChannel inputChannel, CreditChannel inputCreditChannel){

        /* Create Traffic Generator */
        this.trafficGenerator = new TrafficGenerator(position, radix, numVCs, bufferSize, sourceQueueSize, flitsPerPacket, prob, precision, hotspots, hotSpotFactor, rentExponent, outputChannel, outputCreditChannel);

        /* Create Traffic Receiver */
        this.trafficReceiver = new TrafficReceiver(position);

        this.numVCs = numVCs;

        /* Connect input channels */
        this.inputChannel = inputChannel;
        this.inputCreditChannel = inputCreditChannel;

        this.ID = "node_" + position[0] + "." + position[1];
    }

    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                              *
     ******************************************************************************** */


    /**
     * Simulation process of the IP core
     * @param stallNetwork: indicates whether or not the network has to be stalled.
     */
    public void simulateIPCore(boolean stallNetwork){
        /* Simulate traffic generator */
        this.trafficGenerator.simulateTrafficGenerator(stallNetwork);

        /* If the network is not stalled, a flit can be received */
        if(!stallNetwork)
            this.receiveFlit();
    }

    /**
     * Receive a flit from the input flit channel.
     * Analyze the content and send credit back if needed.
     */
    public void receiveFlit(){
        Flit receivedFlit = this.inputChannel.removeFlit();

        if(receivedFlit.getType() != Flit.ZERO_FLIT){
            this.trafficReceiver.receiveFlit(receivedFlit);

            /* Send credit to upstream router */
            Credit credit = new Credit(receivedFlit.getVC());
            //storeCredit(credit);
            this.inputCreditChannel.addCredit(credit);
        } else {
            this.inputCreditChannel.addCredit(new Credit());
        }
    }

    /**
     * Update network time for all ipCore elements
     */
    public void updateNetworkTime(boolean stallNetwork){
        this.trafficGenerator.updateNetworkTime(stallNetwork);
        if(!stallNetwork)
            this.trafficReceiver.updateNetworkTime();
    }

    /**
     * Load state from memory into this IP Core
     * @param memoryIPCore: memory element containing all information about this IP core
     */
    public void loadState(MemoryIPCore memoryIPCore){
        /* Traffic Generator fields */
        MemoryTG memTG = memoryIPCore.getTrafficGenerator();
        this.trafficGenerator.setPosition(memTG.getPosition());
        for(int i = 0; i < numVCs; i++){
            this.trafficGenerator.getCredits()[i] = memTG.getCredits()[i];
            this.trafficGenerator.getStates()[i] = memTG.getStates()[i];

        }
        this.trafficGenerator.getSourceQueue().clear();
        for(int i = 0; i < memTG.getSourceQueue().size(); i++){
            this.trafficGenerator.getSourceQueue().add(memTG.getSourceQueue().get(i));
        }
        this.trafficGenerator.setAllocatePriority(memTG.getAllocatePriority());
        this.trafficGenerator.setPacketToSend(memTG.getPacketToSend());
        this.trafficGenerator.setNetworkTime(memTG.getNetworkTime());
        this.trafficGenerator.setLocalTime(memTG.getLocalTime());

        /* Traffic Receiver fields */
        MemoryTR memTR = memoryIPCore.getTrafficReceiver();
        this.trafficReceiver.setPosition(memTR.getPosition());
        this.trafficReceiver.setID(memTR.getID());
        this.trafficReceiver.setNumReceivedPackets(memTR.getNumReceivedPackets());
        this.trafficReceiver.setNumReceivedFlits(memTR.getNumReceivedFlits());
        this.trafficReceiver.setAveragePacketLatency(memTR.getAveragePacketLatency());
        this.trafficReceiver.setMinPacketLatency(memTR.getMinPacketLatency());
        this.trafficReceiver.setMaxPacketLatency(memTR.getMaxPacketLatency());
        this.trafficReceiver.setAverageHops(memTR.getAverageHops());
        this.trafficReceiver.setMinHops(memTR.getMinHops());
        this.trafficReceiver.setMaxHops(memTR.getMaxHops());
        this.trafficReceiver.setNetworkTime(memTR.getNetworkTime());

        /* Update ID */
        int[] position = memoryIPCore.getPosition();
        this.ID = "node_" + position[0] + "." + position[1];
        this.trafficGenerator.setID("TG@[" + position[0] + "," + position[1] + "," + position[2] + "]");
        this.trafficReceiver.setID("TR_node_" + position[0] + "." + position[1] + "." + position[2]);

    }

    /**
     * Check if the network needs to be stalled
     * @return: true if network needs to be stalled
     */
    public boolean checkNetworkStalling(){
        return this.trafficGenerator.checkNetworkStalling();
    }



    /* ********************************************************************************
     *                      SIMULATION CONTROL AND ANALYSIS                          *
     ******************************************************************************** */
    public double getAveragePacketLatency(){
        return this.trafficReceiver.getAveragePacketLatency();
    }

    public int getNumReceivedPackets(){
        return this.trafficReceiver.getNumReceivedPackets();
    }

    public double getAverageHops(){
        return this.trafficReceiver.getAverageHops();
    }

    public void startMeasurement(){
        /* Reset traffic receiver */
        this.trafficReceiver.reset();

        /* Set traffic generator in measurement phase */
        this.trafficGenerator.setPhase(TrafficGenerator.MEASUREMENT_PHASE);

        /* Reset number of generated packets */
        this.trafficGenerator.setNumGeneratedPackets(0);
    }

    public void startDrain(){
        /* Set traffic generator in drain phase */
        this.trafficGenerator.setPhase(TrafficGenerator.DRAIN_PHASE);
    }

    /* ********************************************************************************
     *                              GETTERS AND SETTERS                             *
     ******************************************************************************** */

    public TrafficGenerator getTrafficGenerator() {
        return trafficGenerator;
    }

    public void setTrafficGenerator(TrafficGenerator trafficGenerator) {
        this.trafficGenerator = trafficGenerator;
    }

    public TrafficReceiver getTrafficReceiver() {
        return trafficReceiver;
    }

    public void setTrafficReceiver(TrafficReceiver trafficReceiver) {
        this.trafficReceiver = trafficReceiver;
    }

    public FlitChannel getInputChannel() {
        return inputChannel;
    }

    public void setInputChannel(FlitChannel inputChannel) {
        this.inputChannel = inputChannel;
    }

    public CreditChannel getInputCreditChannel() {
        return inputCreditChannel;
    }

    public void setInputCreditChannel(CreditChannel inputCreditChannel) {
        this.inputCreditChannel = inputCreditChannel;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }


    @Override
    public String toString() {
        return this.ID;
    }
}
