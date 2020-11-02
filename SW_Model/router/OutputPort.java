/*
File:           OutputPort.java
Created:        2020/05/09
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
import building_blocks.FlitChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OutputPort {
    /* ********************************************************************************
     *                                  VARIABLES                                   *
     ******************************************************************************** */
    /* CONSTANTS */
    public static final int IDLE = 0;
    public static final int ACTIVE = 1;


    /* Variables to keep track of downstream router virtual channels */
    private int[] globalStates;                      // State variable (IDLE or ACTIVE) at the current cycle for every VC at the downstream router
    private int[] nextGlobalStates;                  // State variable (IDLE or ACTIVE) at the next cycle for every VC at the downstream router

    /* Credit flow */
    private int[] credits;                          // Credit counters for every VC at the downstream router
    private int[] nextCredits;
    private List<Credit> creditBuffer;              // Buffer that temporarily stores credits
    private int creditToUpdate;                     // Variable used for updating correct credit counter


    /* Output and credit channels */
    private FlitChannel outputChannel;               // Channel used for sending flit to downstream router
    private CreditChannel creditChannel;             // Channel used for receiving credits from downstream router

    /* Output Port variables */
    String ID;                                      // Identifier for debugging purposes.


    /* Loggers */
    private final static Logger debugLogger = Logger.getLogger("debugLogger");
    private final static Logger resultLogger = Logger.getLogger("resultLogger");

    /* ********************************************************************************
     *                                   CONSTRUCTORS                               *
     ******************************************************************************** */
    /**
     * @param bufferSize: size (in number of flits) of the buffer at downstream router
     * @param numVCs: number of virtual channels at the input port of the downstream router
     * @param outputChannel: channel used for sending flit to downstream router
     * @param creditChannel: channel used for receiving credits from downstream router
     * @param ID: identifier for debugging purposes.
     */
    public OutputPort(int numVCs, int bufferSize, FlitChannel outputChannel, CreditChannel creditChannel, String ID){
        /* State variables */
        this.globalStates = new int[numVCs];
        this.nextGlobalStates = new int[numVCs];
        this.credits = new int[numVCs];
        this.nextCredits = new int[numVCs];
        for(int i = 0; i < numVCs; i++){
            this.credits[i] = bufferSize;
            this.nextCredits[i] = bufferSize;
        }

        /* Credit variables */
        this.creditBuffer = new ArrayList<Credit>();
        this.creditToUpdate = -1;

        /* Flit and credit channels */
        this.outputChannel = outputChannel;
        this.creditChannel = creditChannel;

        /* ID variable */
        this.ID = ID;
    }



    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                              *
     ******************************************************************************** */
      /**
     * Update state of this output port
     */
    public void updateStates(){
        for(int i = 0; i < globalStates.length; i++){
            this.globalStates[i] = this.nextGlobalStates[i];
        }
    }

    /**
     * Set state of the output unit in the next cycle
     * @param VC: Virtual channel that needs to be updated
     * @param state: new state
     */
    public void setNextGlobalState(int VC, int state){
        this.nextGlobalStates[VC] = state;
    }

    public void setGlobalState(int VC, int state){
        this.globalStates[VC] = state;
    }




    /**
     * Decreases the credit count of the output VC with one.
     * @param VC: Virtual Channel of which the credit count needs to be reduced.
     */
    public void decreaseCreditCount(int VC){
        this.credits[VC]--;
    }

    /**
     * Receiving of credit and updating credit variables
     */
    public void receiveCredit(){
        Credit credit =  this.creditChannel.removeCredit();
        if(credit.getType() != Credit.ZERO_CREDIT){
            int VC = credit.getVC();
            this.credits[VC]++;
            debugLogger.log(Level.FINE, "Received credit at " + this.toString() + " VC = " + VC);

        }
    }

    public void setCredits(int VC, int creditCount){
        this.credits[VC] = creditCount;
    }




    /* ********************************************************************************
     *                              GETTERS AND SETTERS                             *
     ******************************************************************************** */

        public int[] getGlobalStates() {
        return globalStates;
    }

    public void setGlobalStates(int[] globalStates) {
        this.globalStates = globalStates;
    }

    public int[] getNextGlobalStates() {
        return nextGlobalStates;
    }

    public void setNextGlobalStates(int[] nextGlobalStates) {
        this.nextGlobalStates = nextGlobalStates;
    }

    public int[] getCredits() {
        return credits;
    }

    public void setCredits(int[] credits) {
        this.credits = credits;
    }

    public int[] getNextCredits() {
        return nextCredits;
    }

    public void setNextCredits(int[] nextCredits) {
        this.nextCredits = nextCredits;
    }

    public List<Credit> getCreditBuffer() {
        return creditBuffer;
    }

    public void setCreditBuffer(List<Credit> creditBuffer) {
        this.creditBuffer = creditBuffer;
    }

    public int getCreditToUpdate() {
        return creditToUpdate;
    }

    public void setCreditToUpdate(int creditToUpdate) {
        this.creditToUpdate = creditToUpdate;
    }

    public FlitChannel getOutputChannel() {
        return outputChannel;
    }

    public void setOutputChannel(FlitChannel outputChannel) {
        this.outputChannel = outputChannel;
    }

    public CreditChannel getCreditChannel() {
        return creditChannel;
    }

    public void setCreditChannel(CreditChannel creditChannel) {
        this.creditChannel = creditChannel;
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
