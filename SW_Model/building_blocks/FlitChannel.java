/*
File:           FlitChannel.java
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

package building_blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class FlitChannel {
    /* ********************************************************************************
     *                                  VARIABLES                                     *
     ******************************************************************************** */
    private List<Flit> channelBuffer;                   // Actual channel with delay of 1
    private boolean edge;                               // Indicates whether this channel is at the edge of the mesh or not

    /* Loggers */
    private final static Logger debugLogger = Logger.getLogger("debugLogger");
    private final static Logger resultLogger = Logger.getLogger("resultLogger");

    /* ********************************************************************************
     *                                   CONSTRUCTORS                                 *
     ******************************************************************************** */

    /**
     * Constructor for creating a flit channel.
     * @param edge: indicates whether this channel is at the edge of the mesh or not
     */
    public FlitChannel(boolean edge){
        if(!edge) {
            /* Create channel and initialize it with a dummy flit */
            this.channelBuffer = new ArrayList<Flit>();
            this.channelBuffer.add(new Flit());
            //Flitchannels start with extra flit on channel because flit receiving comes first in sim scheme (see Router)
            this.channelBuffer.add(new Flit());
        }

        this.edge = edge;
    }

    /**
     * Standard constructor with no arguments (assumes that this channel is not at an edge)
     */
    public FlitChannel(){
        this(false);
    }

    /**
     * Special constructor that creates channel with a certain amount of flits already on there (standard is 2)
     * @param size: number of flits with which the channel starts
     */
    public FlitChannel(int size){
        this.edge = false;
        this.channelBuffer = new ArrayList<Flit>();
        for(int i = 0; i < size; i++){
            this.channelBuffer.add(new Flit());
        }
    }



    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                                *
     ******************************************************************************** */

    /**
     * Adds a new flit to the channel (if this is not an edge channel)
     * @param flit: flit to be added to the channel
     */
    public void addFlit(Flit flit){
        if(!this.edge)
            this.channelBuffer.add(flit);
    }

    /**
     * Remove a flit from the channel (if this is not an edge channel)
     * @return: flit that was on the channel
     */
    public Flit removeFlit(){
        if(!this.edge)
            return this.channelBuffer.remove(0);
        else
            return new Flit();
    }

    public void copyChannelBuffer(List<Flit> channelBuffer){
        this.channelBuffer.clear();
        for(int i = 0; i < channelBuffer.size(); i++){
            this.channelBuffer.add(channelBuffer.get(i));
        }
    }


    /* ********************************************************************************
     *                              GETTERS AND SETTERS                               *
     ******************************************************************************** */

    public List<Flit> getChannelBuffer() {
        return channelBuffer;
    }

    public void setChannelBuffer(List<Flit> channelBuffer) {
        this.channelBuffer = channelBuffer;
    }

    public boolean isEdge() {
        return edge;
    }

    public void setEdge(boolean edge) {
        this.edge = edge;
    }
}
