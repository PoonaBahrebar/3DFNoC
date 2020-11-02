/*
File:           Flit.java
Created:        2020/05/09
Last Changed:   2020/07/22
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

import java.util.logging.Logger;

public class Flit {
    /* ********************************************************************************
     *                                  VARIABLES                                   *
     ******************************************************************************** */
    /* CONSTANTS */
    public static final int ZERO_FLIT = 0;
    public static final int HEADER_FLIT = 1;
    public static final int BODY_FLIT = 2;
    public static final int TAIL_FLIT = 3;

    /* Flit variables */
    private int type;                                       // Type of the flit: header, body or tail
    private int VC;                                         // VC to which this flit is allocated
    private int timestamp;                                  // Creation time of the packet
    private int payload;                                    // Actual data of the flit
    private int[] destination;                              // Destination router (z,y,x) of the flit
    private int[] source;                                   // Source router (z,y,x) of the flit
    private String ID;                                      // ID for debugging purposes

    private int hops;



    /* Loggers */
    private final static Logger debugLogger = Logger.getLogger("debugLogger");
    private final static Logger resultLogger = Logger.getLogger("resultLogger");


    /* ********************************************************************************
     *                                   CONSTRUCTORS                               *
     ******************************************************************************** */

    /**
     * Constructor for dummy flits.
     */
    public Flit(){
        this.type = Flit.ZERO_FLIT;
        this.ID = "ZERO_FLIT";

        // Set all variables to -1 to make sure that it will never be accidentally used.
        this.VC = -1;
        this.timestamp = -1;
        this.payload = -1;
        this.destination = new int[]{-1,-1,-1};
        this.source = new int[]{-1,-1,-1};

        this.hops = 0;
    }

    /**
     * Constructor for header, body or tail flit.
     * @param type: type of flit (header, body or tail)
     * @param timestamp: time of creation
     * @param payload: actual data
     * @param destination: destination router (z,y,x)
     * @param source: source router (z,y,x)
     */
    public Flit(int type, int timestamp, int payload, int[] destination, int[] source){
        this.type = type;
        this.VC = -1;
        this.timestamp = timestamp;
        this.payload = payload;
        this.destination = destination;
        this.source = source;

        this.ID = "Flit_S[" + source[0] + "," + source[1]  + "," + source[2]
                +"]D[" + destination[0] + "," + destination[1] + "," + destination[2] +"]_" + this.payload;
    }

    public void increaseHops(){
        this.hops += 1;
    }





    /* ********************************************************************************
     *                              GETTERS AND SETTERS                             *
     ******************************************************************************** */

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getVC() {
        return VC;
    }

    public void setVC(int VC) {
        this.VC = VC;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getPayload() {
        return payload;
    }

    public void setPayload(int payload) {
        this.payload = payload;
    }

    public int[] getDestination() {
        return destination;
    }

    public void setDestination(int[] destination) {
        this.destination = destination;
    }

    public int[] getSource() {
        return source;
    }

    public void setSource(int[] source) {
        this.source = source;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getHops() {
        return hops;
    }

    public void setHops(int hops) {
        this.hops = hops;
    }

    @Override
    public String toString() {
        return this.ID;
    }
}
