/*
File:           Credit.java
Created:        2020/05/09
Last Changed:   2020/05/09
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

public class Credit {
    /* ********************************************************************************
     *                                  VARIABLES                                   *
     ******************************************************************************** */
    /* CONSTANTS */
    public static final int ZERO_CREDIT = 0;
    public static final int NORMAL_CREDIT = 1;

    /* Credit variables */
    private int type;               // Indicates whether the credit is a dummy or not
    private int VC;                 // Virtual Channel of unit that sent this credit
    private String ID;              // ID for debugging purposes


    /* Loggers */
    private final static Logger debugLogger = Logger.getLogger("debugLogger");
    private final static Logger resultLogger = Logger.getLogger("resultLogger");




    /* ********************************************************************************
     *                                   CONSTRUCTORS                               *
     ******************************************************************************** */

    /**
     * Constructor for dummy credits
     */
    public Credit(){
        this.VC = -1;
        this.type = ZERO_CREDIT;
        this.ID = "ZERO_CREDIT";
    }

    /**
     * Constructor for normal credits.
     * @param VC: Virtual Channel to which the credit needs to be send.
     */
    public Credit(int VC){
        this.VC = VC;
        this.type = NORMAL_CREDIT;
        this.ID = "Credit_VC" + VC;
    }


    /* ********************************************************************************
     *                              GETTERS AND SETTERS                             *
     ******************************************************************************** */
    public void setVC(int VC) {
        this.VC = VC;
    }

    public int getVC() {
        return VC;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.ID;
    }
}
