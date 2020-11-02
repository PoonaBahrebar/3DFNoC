/*
File:           InputPort.java
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
import building_blocks.Flit;
import building_blocks.FlitChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InputPort {
    /* ********************************************************************************
     *                                  VARIABLES                                   *
     ******************************************************************************** */
    /* Virtual channel units: */
    private List<InputUnit> inputUnits;         // List of all the input VC units
    private int numInputUnits;                  // Number of input VC units

    private List<Credit> creditBuffer;          // Buffer used for storing credits

    /* Flit and credit channel */
    private FlitChannel inputChannel;            // Channel used for receiving flit from upstream router
    private CreditChannel creditChannel;         // Channel used for sending credit to upstream router

    /* Input Port variables */
    String ID;                                  // Identifier for debugging purposes.

    /* Loggers */
    private final static Logger debugLogger = Logger.getLogger("debugLogger");
    private final static Logger resultLogger = Logger.getLogger("resultLogger");


    /* ********************************************************************************
     *                                   CONSTRUCTORS                               *
     ******************************************************************************** */
    /**
     * @param numInputUnits: number of input VC units at this input port
     * @param bufferSize: size of the buffer of each VC unit.
     * @param inputChannel: channel used for receiving flit from upstream router
     * @param creditChannel: channel used for sending credit to upstream router
     * @param ID: identifier for debugging purposes.
     */
    public InputPort(int numInputUnits, int bufferSize, FlitChannel inputChannel, CreditChannel creditChannel, String ID){
        /* ID variable */
        this.ID = ID;

        /* Create input units */
        this.inputUnits = new ArrayList<InputUnit>();
        this.numInputUnits = numInputUnits;
        for(int i = 0; i < numInputUnits; i++){
            String unitID = this.ID + "U" + i;
            this.inputUnits.add(new InputUnit(bufferSize, unitID));
        }

        /* Channels */
        this.inputChannel = inputChannel;
        this.creditChannel = creditChannel;

        /* Credit buffer */
        this.creditBuffer = new ArrayList<Credit>();

    }


    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                              *
     ******************************************************************************** */

    /**
     * Store created credit in a 'buffer' at the input port. This credit will be sent in the next cycle.
     * @param credit: credit to be stored
     */
    public void storeCredit(Credit credit){
        this.creditBuffer.add(credit);
        debugLogger.log(Level.FINE,"Credit (VC = " + credit.getVC() + ") added to credit BUFFER at " + this.toString() );
    }

    /**
     * Put the credit form the credit buffer onto the channel to the upstream router.
     * If the credit buffer is empty, a dummy credit is added.
     */
    public void sendCredit(){
        Credit credit = new Credit();
        if(this.creditBuffer.size() > 0){
            credit = creditBuffer.remove(0);
            debugLogger.log(Level.FINE, "Credit (VC = " + credit.getVC() + ") added to credit CHANNEL at " + this.toString());
        }
        this.creditChannel.addCredit(credit);

    }

    /**
     * Receive flit from the input channel and store it in the corresponding buffer if needed
     */
    public void receiveFlit(){
        if(!inputChannel.isEdge() && inputChannel.getChannelBuffer().size() == 0){
            System.out.println("No flits to receive on this channel!");
        }

        /* Remove flit from channel */
        Flit receivedFlit = inputChannel.removeFlit();

        /* Check if the received flit is an actual flit from upstream router */
        if(receivedFlit.getType() != Flit.ZERO_FLIT){
            /* Add flit to corresponding VC buffer */
            int VC = receivedFlit.getVC();
            InputUnit inputUnit = this.inputUnits.get(VC);
            inputUnit.addFlit(receivedFlit);

            /* Update hop count */
            receivedFlit.increaseHops();

            // Log to debug file
            debugLogger.log(Level.FINER, "Received " + receivedFlit + " at " + inputUnit);

            /* If the unit was idle, update the state */
            if(inputUnit.getGlobalState() == InputUnit.IDLE){
                inputUnit.setNextGlobalState(InputUnit.ROUTING);
                // quick fix
                inputUnit.setGlobalState(InputUnit.ROUTING);
            }
        }
    }

    /* ********************************************************************************
     *                              GETTERS AND SETTERS                             *
     ******************************************************************************** */
    public String getID() {
        return ID;
    }

    public int getNumInputUnits() {
        return numInputUnits;
    }

    public CreditChannel getCreditChannel() {
        return creditChannel;
    }

    public FlitChannel getInputChannel() {
        return inputChannel;
    }

    public List<InputUnit> getInputUnits() {
        return inputUnits;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setCreditChannel(CreditChannel creditChannel) {
        this.creditChannel = creditChannel;
    }

    public void setInputChannel(FlitChannel inputChannel) {
        this.inputChannel = inputChannel;
    }

    public void setInputUnits(List<InputUnit> inputUnits) {
        this.inputUnits = inputUnits;
    }

    public void setNumInputUnits(int numInputUnits) {
        this.numInputUnits = numInputUnits;
    }

    public List<Credit> getCreditBuffer() {
        return creditBuffer;
    }

    public void setCreditBuffer(List<Credit> creditBuffer) {
        this.creditBuffer = creditBuffer;
    }

    @Override
    public String toString() {
        return this.ID;
    }

}
