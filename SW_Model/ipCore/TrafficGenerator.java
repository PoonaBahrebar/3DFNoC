/*
File:           TrafficGenerator.java
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

import building_blocks.*;
import mesh.ClusteredMesh;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrafficGenerator {
    /* ********************************************************************************
     *                                  VARIABLES                                     *
     ******************************************************************************** */
    /* CONSTANTS */
    public static final int WARMUP_PHASE = 0;
    public static final int MEASUREMENT_PHASE = 1;
    public static final int DRAIN_PHASE = 2;
    public static final int IDLE = 0;
    public static final int ASSIGNED = 1;

    /* TRAFFIC PATTERNS */
    public static final int UNIFORM = 0;
    public static final int HOTSPOT = 1;
    public static final int RENT = 2;



    /* Traffic Generator variables */
    private int[] position;                                         // Position of the router: (z,y,x) coordinates
    private int radix;                                              // Radix of the mesh topology (n x n x n mesh has radix n)
    private int flitsPerPacket;                                     // Number of flits in each packet
    private int[] credits;                                          // Number of credits available for each VC
    private int[] states;                                           // States of the VC (ASSIGNED / IDLE)
    private int numVCs;                                             // Number of Virtual channels
    private int phase;                                              // Phase of the network: Warm-up, measurement or drain
    private String ID;                                              // Identifier for debugging purposes.

    /* Channels for communication with router */
    private FlitChannel flitQueue;                                  // Channel used for sending flit to the router
    private CreditChannel creditChannel;                            // Channel used for receiving credits from router

    /* Source Queue */
    private List<PacketDescriptor> sourceQueue;                     // Buffer that contains packet descriptors, used to generate flits

    /* Time variables */
    private int networkTime;                                        // Time of the complete network

    /* Traffic generation related variables */
    private int[] probArray;                                        // Array used to generate packet with certain probability
    private int precision;                                          // Precision of probability: (prob/precision)
    private int allocatePriority;                                   // Used for allocation of Virtual Channels
    private int packetToSend;                                       // Used for Round-Robin scheme in flits over VCs


    /* Debugging and analysis */
    private int numGeneratedPackets;                                // Number of generated packets

    private int[] destinations;
    private int[] hotspots;
    private double hotSpotFactor;

    private double rentExponent;

    private int trafficPattern;

    private int destPrec;

    /* Stalling */
    private int localTime;
    boolean stopTG;
    private int sourceQueuesize;


    /* Loggers */
    private final static Logger debugLogger = Logger.getLogger("debugLogger");
    private final static Logger resultLogger = Logger.getLogger("resultLogger");

    /* ********************************************************************************
     *                                   CONSTRUCTORS                               *
     ******************************************************************************** */

    /**
     * Constructor for creating traffic generator.
     * @param position: position (z,y,x) of the router
     * @param radix: radix of the mesh
     * @param numVCs: number of Virtual Channels per input port
     * @param bufferSize: size (in number of flits) of the buffer for each Virtual channel
     * @param sourceQueuesize: size of the source queue (in number of packets)
     * @param flitsPerPacket: number of flits per packet
     * @param prob: probability that a packet will be generated (prob/precision)
     * @param precision: determines precision of probability
     * @param hotspots: array containing number of routers that are hotspot routers
     * @param hotSpotFactor: factor indicating how much more traffic is sent to hotspot nodes
     * @param rentExponent: rent exponent
     * @param flitQueue: channel used for transmitting flits to the router
     * @param creditChannel: channel used for receiving credits from the router
     */
    public TrafficGenerator(int[] position, int radix, int numVCs, int bufferSize, int sourceQueuesize, int flitsPerPacket, int prob, int precision, int[] hotspots, double hotSpotFactor, double rentExponent,
                            FlitChannel flitQueue, CreditChannel creditChannel){
        /* Traffic generator variables */
        this.radix = radix;
        this.position = position;
        this.flitsPerPacket = flitsPerPacket;
        this.numVCs = numVCs;

        /* Source queue */
        this.sourceQueue = new ArrayList<PacketDescriptor>();

        /* Network time */
        this.networkTime = 0;

        /* Create prob array */
        this.precision = precision;
        this.probArray = new int[precision];
        for(int i = 0; i < prob; i++){
            probArray[i] = 1;
        }

        /* Internal variables */
        this.credits = new int[numVCs];
        this.states = new int[numVCs];
        for(int i = 0; i < numVCs; i++){
            this.credits[i] = bufferSize;
            this.states[i] = this.IDLE;
        }
        this.allocatePriority = 0;


        this.phase = WARMUP_PHASE;

        this.ID = "TG@[" + position[0] + "," + position[1] + "," + position[2] + "]";

        /* Channels */
        this.flitQueue = flitQueue;
        this.creditChannel = creditChannel;


        /* Debugging and analysis */
        this.numGeneratedPackets = 0;

        /* Destination array */
        if(hotSpotFactor != 1.0 || rentExponent != 1.0)
            this.destPrec = ClusteredMesh.DEST_PREC;
        else
            this.destPrec = radix*radix*radix-1;

        this.destinations = new int[this.destPrec];
        this.hotspots = hotspots;
        this.hotSpotFactor = hotSpotFactor;

        this.rentExponent = Math.max(rentExponent,1);

        if(hotSpotFactor == 1 && rentExponent == 1){
            this.trafficPattern = UNIFORM;
        } else if(hotSpotFactor != 1){
            this.trafficPattern = HOTSPOT;
        } else if(rentExponent != 1){
            this.trafficPattern = RENT;
        }

        /* Network stalling and SQ variables */
        this.localTime = 0;
        this.stopTG = false;
        this.sourceQueuesize = sourceQueuesize;

    }


    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                              *
     ******************************************************************************** */

    /**
     * Simulation process of Traffic Generator
     * @param stallNetwork: indicates whether or not the network is being stalled
     */
    public void simulateTrafficGenerator(boolean stallNetwork){

        if(!stallNetwork) {
            /* Send flits */
            this.generateFlits();
        }

        /* First check if TG can create a new packet */
        this.stopTG = !(this.sourceQueue.size() < sourceQueuesize && (!stallNetwork || this.localTime < this.networkTime));

        if (this.sourceQueue.size() < sourceQueuesize && !(stallNetwork && this.localTime == this.networkTime)) {
            /* Generate packet */
            this.generatePacket();
            /* Allocate a VC */
            this.allocateVC();

        }

        if(!stallNetwork) {
            /* Receive credits*/
            this.receiveCredit();
        }

    }


    /**
     * Uniform Random Packet generation. Create a packet descriptor and add it to the source queue.
     */
    private void generatePacket(){
        /* Randomly select element out of probArray: probability prob/precision to find 1 */
        int r = ThreadLocalRandom.current().nextInt(0, this.precision);

        if(probArray[r] == 1 && this.phase != DRAIN_PHASE ){
            int[] destination = createDestination();
            int numberOfFlits = this.flitsPerPacket;
            int generatedTime = this.localTime +1 + 1;
            PacketDescriptor packetDescriptor = new PacketDescriptor(destination, numberOfFlits, generatedTime);

            /* Add packet descriptor to source queue */
            this.sourceQueue.add(packetDescriptor);

            debugLogger.log(Level.FINER,"New packet created with destination R["+ destination[0] + "," + destination[1] + "," + destination[2] + "]");

            this.numGeneratedPackets++;
        }

    }

    /**
     * Try to allocate a Virtual channel to a packet.
     * This allocation scheme reduces to simple arbitration with Round-robin priority changing
     */
    private void allocateVC(){
        boolean allVCsAssigned = false;
        for(int i = 0; i < sourceQueue.size() && !allVCsAssigned; i++){
            PacketDescriptor packetDescriptor = sourceQueue.get(i);
            if(packetDescriptor.getAllocatedVC() == -1){
                /* Try to allocate a VC if possible */
                int start = allocatePriority;
                int end = allocatePriority + this.numVCs;
                for(int j = start; j < end; j++){
                    int index = j % numVCs;
                    if(states[index] == this.IDLE){
                        /* allocate VC */
                        packetDescriptor.setAllocatedVC(index);
                        states[index] = this.ASSIGNED;
                        allocatePriority = (index + 1) % numVCs;
                        break;
                    } else if(j == end-1){
                        allVCsAssigned = true;
                    }
                }
                break;
            }
        }
    }

    /**
     * Generate flits and add them to the flit channel if possible.
     */
    private void generateFlits(){
        /* Generate one flit */
        boolean flitSend = false;
        for(int i = 0; i < numVCs && i < sourceQueue.size() && !flitSend; i++){
            /* Round robin scheme: iterate over VCs to send flits */
            int packetNumber = (packetToSend + i) % Math.min(numVCs, sourceQueue.size());
            PacketDescriptor packetDescriptor = sourceQueue.get(packetNumber);

            int VC = packetDescriptor.getAllocatedVC();
            /* If this packet descriptor has a VC allocated: send a flit */
            if(packetDescriptor.getAllocatedVC() != -1 && this.credits[VC] > 0){
                int timestamp = packetDescriptor.getGenerationTime();
                int payload = packetDescriptor.getNumberOfFlits() - packetDescriptor.getRemainingFlits();
                int[] destination = packetDescriptor.getDestination();
                int type;

                /* Determine flit type and actions to take */
                if(packetDescriptor.getRemainingFlits() == packetDescriptor.getNumberOfFlits()){
                    type = Flit.HEADER_FLIT;
                    packetDescriptor.decreaseRemainingFlits();
                } else if(packetDescriptor.getRemainingFlits() == 1){
                    type = Flit.TAIL_FLIT;

                    /* Deallocate VC */
                    states[VC] = this.IDLE;

                    /* Remove packet descriptor from SourceQueue */
                    this.sourceQueue.remove(packetNumber);

                } else {
                    type = Flit.BODY_FLIT;
                    packetDescriptor.decreaseRemainingFlits();
                }

                Flit generatedFlit = new Flit(type, timestamp, payload, destination, this.position);
                generatedFlit.setVC(VC);
                this.credits[VC]--;
                this.flitQueue.addFlit(generatedFlit);
                debugLogger.log(Level.FINEST, generatedFlit + " added to channel at " + this.toString());
                flitSend = true;

                /* Update packetToSend such that next cycle other VC is used */
                this.packetToSend = (packetNumber + 1) % this.numVCs;
            }
        }
        if(!flitSend){
            /* If no flit is sent, add dummy flit. */
            this.flitQueue.addFlit(new Flit());
        }
    }

    /**
     * Receive a credit and update credit count
     */
    private void receiveCredit(){
        Credit credit = this.creditChannel.removeCredit();
        if(credit.getType() != Credit.ZERO_CREDIT){
            int VC = credit.getVC();
            this.credits[VC]++;
            debugLogger.log(Level.FINE, "Credit received at " + this.toString());
        }
    }

    /**
     * Creates a new destination node according to the currently used traffic pattern
     * @return: coordinates of the destination node
     */
    private int[] createDestination(){
        int[] destination = new int[3];
        int current = position[2] + radix * position[1] + radix * radix * position[0];

        /* Hotspot or uniform traffic pattern */
        if(trafficPattern == HOTSPOT || trafficPattern == UNIFORM) {
            /* Determine probability for hotspot and normal nodes of receiving a packet */
            int hotspotNodes = this.hotspots.length;
            int normalNodes = radix * radix * radix - 1 - hotspotNodes;
            double base_prob = 1.0 / (normalNodes + hotSpotFactor * hotspotNodes);
            double hs_prob = hotSpotFactor * base_prob;

            /* Make array containing possible destinations.
            More probable destinations have more entries in the array */
            int base = (int) (base_prob * this.destPrec);
            int hs = (int) (hs_prob * this.destPrec);
            int current_hs = 0;
            int index = 0;
            for (int i = 0; i < radix * radix * radix; i++) {
                if (i != current) {
                    if (current_hs < hotspots.length && i == hotspots[current_hs]) {
                        for (int j = 0; j < hs; j++) {
                            destinations[index] = i;
                            index++;
                        }
                        current_hs++;
                    } else {
                        for (int j = 0; j < base; j++) {
                            destinations[index] = i;
                            index++;
                        }
                    }
                }
            }
            for (int i = 0; i < this.destPrec - index; i++) {
                if (i != current)
                    destinations[index + i] = i % (radix * radix * radix);
                else
                    destinations[index + i] = i + 1;

            }

            /* Select one destination in the array */
            int rand_nr = ThreadLocalRandom.current().nextInt(0, this.destPrec);
            int router = this.destinations[rand_nr];
            int dest_x = router % radix;
            int dest_y = ((router - dest_x) / radix) % radix;
            int dest_z = (router - dest_x - radix * dest_y) / (radix * radix);

            destination = new int[]{dest_z, dest_y, dest_x};

            /* Local traffic pattern */
        } else if(trafficPattern == RENT) {
            int router = current;
            while(router == current) {
                int rand_nr = ThreadLocalRandom.current().nextInt(0, this.destPrec);
                router = ClusteredMesh.stepsPerDestination.get(current).get(rand_nr);
            }
            int dest_x = router % radix;
            int dest_y = ((router - dest_x) / radix) % radix;
            int dest_z = (router - dest_x - radix * dest_y) / (radix * radix);
            destination = new int[]{dest_z, dest_y, dest_x};
        }

        return destination;
    }



    /**
     * Update network time
     */
    public void updateNetworkTime(boolean stallNetwork){
            if(!stallNetwork){
                this.networkTime++;
            }
            if(!stopTG){
                this.localTime++;
            }
    }


    /**
     * Check if the network needs to be stalled
     * @return: true if network needs to be stalled
     */
    public boolean checkNetworkStalling(){
        return (this.sourceQueue.size() == 0 && this.localTime != this.networkTime && this.phase != DRAIN_PHASE);
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

    public int getRadix() {
        return radix;
    }

    public void setRadix(int radix) {
        this.radix = radix;
    }

    public int getFlitsPerPacket() {
        return flitsPerPacket;
    }

    public void setFlitsPerPacket(int flitsPerPacket) {
        this.flitsPerPacket = flitsPerPacket;
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

    public int getNumVCs() {
        return numVCs;
    }

    public void setNumVCs(int numVCs) {
        this.numVCs = numVCs;
    }

    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public FlitChannel getFlitQueue() {
        return flitQueue;
    }

    public void setFlitQueue(FlitChannel flitQueue) {
        this.flitQueue = flitQueue;
    }

    public CreditChannel getCreditChannel() {
        return creditChannel;
    }

    public void setCreditChannel(CreditChannel creditChannel) {
        this.creditChannel = creditChannel;
    }

    public List<PacketDescriptor> getSourceQueue() {
        return sourceQueue;
    }

    public void setSourceQueue(List<PacketDescriptor> sourceQueue) {
        this.sourceQueue = sourceQueue;
    }

    public int getNetworkTime() {
        return networkTime;
    }

    public void setNetworkTime(int networkTime) {
        this.networkTime = networkTime;
    }

    public int[] getProbArray() {
        return probArray;
    }

    public void setProbArray(int[] probArray) {
        this.probArray = probArray;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
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

    public int getNumGeneratedPackets() {
        return numGeneratedPackets;
    }

    public void setNumGeneratedPackets(int numGeneratedPackets) {
        this.numGeneratedPackets = numGeneratedPackets;
    }

    public int getLocalTime() {
        return localTime;
    }

    public void setLocalTime(int localTime) {
        this.localTime = localTime;
    }

    @Override
    public String toString() {
        return this.ID;
    }

}
