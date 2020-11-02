/*
File:           CreditChannel.java
Created:        2020/05/09
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

package building_blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CreditChannel {
    /* ********************************************************************************
     *                                  VARIABLES                                     *
     ******************************************************************************** */
    private List<Credit> channelBuffer;                       // Actual channel: contains 1 credit
    private boolean edge;                                    // Indicates whether this channel is at the edge of the mesh or not



    /* Loggers */
    private final static Logger debugLogger = Logger.getLogger("debugLogger");
    private final static Logger resultLogger = Logger.getLogger("resultLogger");

    /* ********************************************************************************
     *                                   CONSTRUCTORS                                 *
     ******************************************************************************** */

    /**
     * Constructor for creating a credit channel.
     * @param edge: indicates whether this channel is at the edge of the mesh or not
     */
    public CreditChannel(boolean edge){
        if(!edge){
            /* Create channel and initialize it with a dummy credit */
            this.channelBuffer = new ArrayList<Credit>();
            this.channelBuffer.add(new Credit());
            this.channelBuffer.add(new Credit());
        }

        this.edge = edge;
    }

    /**
     * Standard constructor with no arguments (assumes that this channel is not at an edge)
     */
    public CreditChannel(){
        this(false);
    }

    /**
     * Special constructor that creates channel with a certain amount of credits already on there (standard is 2)
     * @param size: number of credits with which the channel starts
     */
    public CreditChannel(int size){
        this.edge = false;
        this.channelBuffer = new ArrayList<Credit>();
        for(int i = 0; i < size; i++){
            channelBuffer.add(new Credit());
        }
    }


    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                                *
     ******************************************************************************** */

    /**
     * Adds a credit to the channel (if this is not an edge channel)
     * @param credit: credit to be added to the channel.
     */
    public void addCredit(Credit credit){
        if(!this.edge)
            this.channelBuffer.add(credit);
    }

    /**
     * Remove a credit from the channel (if this is not an edge channel)
     * @return credit that was on the channel
     */
    public Credit removeCredit(){
        if(!this.edge)
            return this.channelBuffer.remove(0);
        else
            return new Credit();
    }

    public void copyChannelBuffer(List<Credit> channelBuffer){
        this.channelBuffer.clear();
        for(int i = 0; i < channelBuffer.size(); i++){
            this.channelBuffer.add(channelBuffer.get(i));
        }
    }




    /* ********************************************************************************
     *                              GETTERS AND SETTERS                               *
     ******************************************************************************** */

    public List<Credit> getChannelBuffer() {
        return channelBuffer;
    }

    public void setChannelBuffer(List<Credit> channelBuffer) {
        this.channelBuffer = channelBuffer;
    }

    public boolean isEdge() {
        return edge;
    }

    public void setEdge(boolean edge) {
        this.edge = edge;
    }
}
