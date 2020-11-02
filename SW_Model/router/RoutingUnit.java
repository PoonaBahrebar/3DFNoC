/*
File:           RoutingUnit.java
Created:        2020/05/09
Last Changed:   2020/08/08
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

import building_blocks.Flit;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoutingUnit {
    /* - - - - - - - - - - -  - - - IMPORTANT - - - - - - - - - - - - - -
            Assumptions made with respect to the numbering of the ports:
            0 : EAST
            1 : SOUTH
            2 : WEST
            3 : NORTH
            4 : UP
            5 : DOWN
            6 : IPCORE
        - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */

    /* ********************************************************************************
     *                                  VARIABLES                                   *
     ******************************************************************************** */
    private int[] position;                         // Position of the current router: (z,y,x) coordinates
    private int radix;                              // Radix of the mesh topology (n x n x n mesh has radix n)

    private String ID;                              // ID for debugging purposes


    /* Loggers */
    private final static Logger debugLogger = Logger.getLogger("debugLogger");
    private final static Logger resultLogger = Logger.getLogger("resultLogger");

    /* ********************************************************************************
     *                                   CONSTRUCTORS                               *
     ******************************************************************************** */
    /**
     * @param position: position of the current router: (y,x) coordinates
     */
    public RoutingUnit(int[] position, int radix){
        this.position = position;
        this.radix = radix;
        this.ID = "RoutingUnit@[" + position[0] + "," + position[1] + "," + position[2] + "]";
    }

    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                              *
     ******************************************************************************** */
    /**
     * Dimension order (xyz) routing; using the destination field in the header Flit, the flit is routed to correct output port.
     * @param headerFlit: header flit containing information about destination router
     * @return output port to which the packet needs to be routed
     */
    public int xyzRouting(Flit headerFlit){
        int outputPort = -1;

        /* Safety check */
        if(headerFlit.getType() == Flit.HEADER_FLIT){
            int[] destination = headerFlit.getDestination();
            int zd = destination[0];
            int yd = destination[1];
            int xd = destination[2];
            int zp = this.position[0];
            int yp = this.position[1];
            int xp = this.position[2];

            /* First Routing in x-direction */
            if(xd > xp){                                /* Destination to the EAST */
                outputPort = 0;
            } else if(xd < xp){                         /* Destination to the WEST */
                outputPort = 2;
            }
            /* Routing in y-direction */
            else if(yd > yp){                           /* Destination NORTH  */
                outputPort = 3;
            } else if(yd < yp){                         /* Destination SOUTH  */
                outputPort = 1;
            }
            /* Routing in z-direction */
            else if(zd > zp){                           /* Destination UP (in 3D plane) */
                outputPort = 4;
            } else if(zd < zp){                         /* Destination DOWN (in 3D plane) */
                outputPort = 5;
            }
            /* Current router is destination */
            else {
                outputPort = 6;                         /* Destination is ipCore */
            }


        } else {    /* Not a header flit */
            String errorMsg = "Error: non-header flit being routed by " + this.ID;
            System.out.println(errorMsg);
            debugLogger.log(Level.INFO, errorMsg);
        }


        return outputPort;
    }


    /**
     * Minimal adaptive routing algorithm using Two-Block Partitioning
     * Based on the work of:
     *      M. Ebrahimi, M. Daneshtalab, P. Liljeberg, J. Plosila, J. Flich, and H. Tenhunen,
     *      “Path-based partitioning methods for 3d networks-on-chip with minimal adaptive routing,”
     *      IEEE Transactions on Computers, vol. 63, no. 3, pp. 718–733, 2012.
     * @param headerFlit
     * @param routerLoads
     * @return
     */
    public int minimalAdaptiveRouting(Flit headerFlit, int[] routerLoads){
        int outputPort = -1;

        List<Integer> candidates = new ArrayList<Integer>();

        int[] destination = headerFlit.getDestination();
        int zd = destination[0];
        int yd = destination[1];
        int xd = destination[2];
        int zp = this.position[0];
        int yp = this.position[1];
        int xp = this.position[2];

        /* If current router is destination, send to IP core */
        if(xd == xp && yd == yp && zd == zp){
            return 6;
        }

        /* Determine candidate output ports on minimal path */
        if(xd > xp){                                /* Destination to the EAST */
            if(isOnHamiltonianPath(position, destination, new int[]{zp,yp,xp+1}))
                candidates.add(0);
        } else if(xd < xp){                         /* Destination to the WEST */
            if(isOnHamiltonianPath(position, destination, new int[]{zp,yp,xp-1}))
                candidates.add(2);
        }
        if(yd > yp){                                /* Destination NORTH  */
            if(isOnHamiltonianPath(position, destination, new int[]{zp,yp+1,xp}))
                candidates.add(3);
        } else if(yd < yp){                         /* Destination SOUTH  */
            if(isOnHamiltonianPath(position, destination, new int[]{zp,yp-1,xp}))
                candidates.add(1);
        }
       if(zd > zp){                                 /* Destination UP (in 3D plane) */
           if(isOnHamiltonianPath(position, destination, new int[]{zp+1,yp,xp}))
               candidates.add(4);
        } else if(zd < zp){                         /* Destination DOWN (in 3D plane) */
           if(isOnHamiltonianPath(position, destination, new int[]{zp-1,yp,xp}))
               candidates.add(5);
        }


       /* Check network loads at each output port and select candidate with lowest load */
        int minLoad = 1000;
        for(int i = 0; i < candidates.size(); i++){
            if(routerLoads[candidates.get(i)] < minLoad){
                minLoad = routerLoads[candidates.get(i)];
                outputPort = candidates.get(i);
            }
        }
        return outputPort;
    }

    /**
     * Checks if the candidate node is on the HamiltonianPath between source and destination.
     * @param source: coordinates of the source node
     * @param destination: coordinates of the destination node
     * @param candidate: coordinates of the candidate node
     * @return: true if candidate is on hamiltonian path
     */
    private boolean isOnHamiltonianPath(int[] source, int[] destination, int[] candidate){
        int sourceLabel = hamiltonianLabel(source);
        int destLabel = hamiltonianLabel(destination);
        int candLabel = hamiltonianLabel(candidate);

        if(sourceLabel < destLabel){
            return (candLabel <= destLabel && candLabel > sourceLabel);
        }
        else{
            return (candLabel >= destLabel && candLabel < sourceLabel);
        }
    }

    /**
     * Determines the hamiltonian label of a node
     * @param position: coordinates of the node
     * @return: Hamiltonian label
     */
    private int hamiltonianLabel(int[] position){
        int label = 0;

        int z = position[0];
        int y = position[1];
        int x = position[2];

        if(z % 2 == 0){
            if(y % 2 == 0){
                // z even and y even
                label = radix*radix*z + radix*y + x + 1;
            } else {
                // z even and y odd
                label = radix*radix*z + radix*y + radix - x;
            }
        } else{
            if(y % 2 == 0){
                // z odd and y even
                label = radix*radix*z + radix * (radix - y - 1) + radix - x;
            } else {
                // z odd and y odd
                label = radix*radix*z + radix * (radix - y - 1) + x + 1;
            }

        }


        return label;

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
