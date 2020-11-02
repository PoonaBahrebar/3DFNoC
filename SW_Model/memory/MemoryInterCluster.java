/*
File:           MemoryInterCluster.java
Created:        2020/07/18
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

package memory;

import building_blocks.CreditChannel;
import building_blocks.FlitChannel;
import mesh.Cluster;

import java.util.ArrayList;
import java.util.List;

public class MemoryInterCluster {
    /* ********************************************************************************
     *                                  VARIABLES                                     *
     ******************************************************************************** */

    private List<List<List<FlitChannel>>> interClusterInputChannels;
    private List<List<List<FlitChannel>>> interClusterOutputChannels;
    private List<List<List<CreditChannel>>> interClusterInputCreditChannels;
    private List<List<List<CreditChannel>>> interClusterOutputCreditChannels;

    private int sizeX;
    private int sizeY;
    private int sizeZ;

    private boolean adaptive;


    /* ********************************************************************************
     *                                   CONSTRUCTORS                                 *
     ******************************************************************************** */

    /**
     Constructor for memory of the inter cluster memory
     * @param sizeX: size of the cluster in x direction
     * @param sizeY: size of the cluster in y direction
     * @param sizeZ: size of the cluster in z direction
     * @param startPosition: position of the node with lowest coordinates
     * @param radix: radix of the network
     * @param adaptive: indicates whether or not the routing is adaptive or not
     */
    public MemoryInterCluster(int sizeX, int sizeY, int sizeZ, int[] startPosition, int radix, boolean adaptive){
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;

        this.adaptive = adaptive;

        /* Create inter-cluster channels */
        this.interClusterInputChannels = new ArrayList<List<List<FlitChannel>>>();
        this.interClusterOutputChannels = new ArrayList<List<List<FlitChannel>>>();
        this.interClusterInputCreditChannels = new ArrayList<List<List<CreditChannel>>>();
        this.interClusterOutputCreditChannels = new ArrayList<List<List<CreditChannel>>>();
        for(int i = 0; i < 6; i++){
            this.interClusterInputChannels.add(new ArrayList<List<FlitChannel>>());
            this.interClusterOutputChannels.add(new ArrayList<List<FlitChannel>>());
            this.interClusterInputCreditChannels.add(new ArrayList<List<CreditChannel>>());
            this.interClusterOutputCreditChannels.add(new ArrayList<List<CreditChannel>>());
        }

        /* Horizontal plane channels */
        for(int i = 0; i < sizeZ; i++){
            /* EAST and WEST channels */
            this.interClusterInputChannels.get(0).add(new ArrayList<FlitChannel>());            // EAST
            this.interClusterOutputChannels.get(0).add(new ArrayList<FlitChannel>());           // EAST
            this.interClusterInputCreditChannels.get(0).add(new ArrayList<CreditChannel>());    // EAST
            this.interClusterOutputCreditChannels.get(0).add(new ArrayList<CreditChannel>());   // EAST
            this.interClusterInputChannels.get(2).add(new ArrayList<FlitChannel>());            // WEST
            this.interClusterOutputChannels.get(2).add(new ArrayList<FlitChannel>());           // WEST
            this.interClusterInputCreditChannels.get(2).add(new ArrayList<CreditChannel>());    // WEST
            this.interClusterOutputCreditChannels.get(2).add(new ArrayList<CreditChannel>());   // WEST
            for(int j = 0; j < sizeY; j++){
                this.interClusterInputChannels.get(0).get(i).add(new FlitChannel());            // EAST
                this.interClusterOutputChannels.get(0).get(i).add(new FlitChannel(0));           // EAST
                this.interClusterInputCreditChannels.get(0).get(i).add(new CreditChannel(0));    // EAST
                this.interClusterOutputCreditChannels.get(0).get(i).add(new CreditChannel());   // EAST
                this.interClusterInputChannels.get(2).get(i).add(new FlitChannel());            // WEST
                this.interClusterOutputChannels.get(2).get(i).add(new FlitChannel(0));           // WEST
                this.interClusterInputCreditChannels.get(2).get(i).add(new CreditChannel(0));    // WEST
                this.interClusterOutputCreditChannels.get(2).get(i).add(new CreditChannel());   // WEST
            }

            /* SOUTH and NORTH channels */
            this.interClusterInputChannels.get(1).add(new ArrayList<FlitChannel>());            // SOUTH
            this.interClusterOutputChannels.get(1).add(new ArrayList<FlitChannel>());           // SOUTH
            this.interClusterInputCreditChannels.get(1).add(new ArrayList<CreditChannel>());    // SOUTH
            this.interClusterOutputCreditChannels.get(1).add(new ArrayList<CreditChannel>());   // SOUTH
            this.interClusterInputChannels.get(3).add(new ArrayList<FlitChannel>());            // NORTH
            this.interClusterOutputChannels.get(3).add(new ArrayList<FlitChannel>());           // NORTH
            this.interClusterInputCreditChannels.get(3).add(new ArrayList<CreditChannel>());    // NORTH
            this.interClusterOutputCreditChannels.get(3).add(new ArrayList<CreditChannel>());   // NORTH
            for(int j = 0; j < sizeX; j++){
                this.interClusterInputChannels.get(1).get(i).add(new FlitChannel());            // SOUTH
                this.interClusterOutputChannels.get(1).get(i).add(new FlitChannel(0));           // SOUTH
                this.interClusterInputCreditChannels.get(1).get(i).add(new CreditChannel(0));    // SOUTH
                this.interClusterOutputCreditChannels.get(1).get(i).add(new CreditChannel());   // SOUTH
                this.interClusterInputChannels.get(3).get(i).add(new FlitChannel());            // NORTH
                this.interClusterOutputChannels.get(3).get(i).add(new FlitChannel(0));           // NORTH
                this.interClusterInputCreditChannels.get(3).get(i).add(new CreditChannel(0));    // NORTH
                this.interClusterOutputCreditChannels.get(3).get(i).add(new CreditChannel());   // NORTH
            }
        }

        /* UP and DOWN channels */
        for(int i = 0; i < sizeY; i++){
            this.interClusterInputChannels.get(4).add(new ArrayList<FlitChannel>());            // UP
            this.interClusterOutputChannels.get(4).add(new ArrayList<FlitChannel>());           // UP
            this.interClusterInputCreditChannels.get(4).add(new ArrayList<CreditChannel>());    // UP
            this.interClusterOutputCreditChannels.get(4).add(new ArrayList<CreditChannel>());   // UP
            this.interClusterInputChannels.get(5).add(new ArrayList<FlitChannel>());            // DOWN
            this.interClusterOutputChannels.get(5).add(new ArrayList<FlitChannel>());           // DOWN
            this.interClusterInputCreditChannels.get(5).add(new ArrayList<CreditChannel>());    // DOWN
            this.interClusterOutputCreditChannels.get(5).add(new ArrayList<CreditChannel>());   // DOWN
            for(int j = 0; j < sizeX; j++){
                this.interClusterInputChannels.get(4).get(i).add(new FlitChannel());            // UP
                this.interClusterOutputChannels.get(4).get(i).add(new FlitChannel(0));           // UP
                this.interClusterInputCreditChannels.get(4).get(i).add(new CreditChannel(0));    // UP
                this.interClusterOutputCreditChannels.get(4).get(i).add(new CreditChannel());   // UP
                this.interClusterInputChannels.get(5).get(i).add(new FlitChannel());            // DOWN
                this.interClusterOutputChannels.get(5).get(i).add(new FlitChannel(0));           // DOWN
                this.interClusterInputCreditChannels.get(5).get(i).add(new CreditChannel(0));    // DOWN
                this.interClusterOutputCreditChannels.get(5).get(i).add(new CreditChannel());   // DOWN

            }
        }

        this.updateEdges(startPosition, radix);


    }

    public void updateEdges(int[] startPosition, int radix){
        int z = startPosition[0];
        int y = startPosition[1];
        int x = startPosition[2];

        for(int i = 0 ; i < sizeZ; i++){
            for(int j = 0; j < sizeY; j++){
                /* EAST */
                this.interClusterInputChannels.get(0).get(i).get(j).setEdge(x + sizeX >= radix);
                this.interClusterOutputChannels.get(0).get(i).get(j).setEdge(x + sizeX >= radix);
                this.interClusterInputCreditChannels.get(0).get(i).get(j).setEdge(x + sizeX >= radix);
                this.interClusterOutputCreditChannels.get(0).get(i).get(j).setEdge(x + sizeX >= radix);
                /* WEST */
                this.interClusterInputChannels.get(2).get(i).get(j).setEdge(x == 0);
                this.interClusterOutputChannels.get(2).get(i).get(j).setEdge(x == 0);
                this.interClusterInputCreditChannels.get(2).get(i).get(j).setEdge(x == 0);
                this.interClusterOutputCreditChannels.get(2).get(i).get(j).setEdge(x == 0);
            }
        }

        for(int i = 0; i < sizeZ; i++){
            for(int j = 0; j < sizeX; j++){
                /* SOUTH */
                this.interClusterInputChannels.get(1).get(i).get(j).setEdge(y == 0);
                this.interClusterOutputChannels.get(1).get(i).get(j).setEdge(y == 0);
                this.interClusterInputCreditChannels.get(1).get(i).get(j).setEdge(y == 0);
                this.interClusterOutputCreditChannels.get(1).get(i).get(j).setEdge(y == 0);
                /* NORTH */
                this.interClusterInputChannels.get(3).get(i).get(j).setEdge(y + sizeY >= radix);
                this.interClusterOutputChannels.get(3).get(i).get(j).setEdge(y + sizeY >= radix);
                this.interClusterInputCreditChannels.get(3).get(i).get(j).setEdge(y + sizeY >= radix);
                this.interClusterOutputCreditChannels.get(3).get(i).get(j).setEdge(y + sizeY >= radix);
            }
        }

        for(int i = 0; i < sizeY; i++){
            for(int j = 0; j < sizeX; j++){
                /* UP */
                this.interClusterInputChannels.get(4).get(i).get(j).setEdge(z + sizeZ >= radix);
                this.interClusterOutputChannels.get(4).get(i).get(j).setEdge(z + sizeZ >= radix);
                this.interClusterInputCreditChannels.get(4).get(i).get(j).setEdge(z + sizeZ >= radix);
                this.interClusterOutputCreditChannels.get(4).get(i).get(j).setEdge(z + sizeZ >= radix);
                /* DOWN */
                this.interClusterInputChannels.get(5).get(i).get(j).setEdge(z == 0);
                this.interClusterOutputChannels.get(5).get(i).get(j).setEdge(z == 0);
                this.interClusterInputCreditChannels.get(5).get(i).get(j).setEdge(z == 0);
                this.interClusterOutputCreditChannels.get(5).get(i).get(j).setEdge(z == 0);

            }
        }

    }


    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                                *
     ******************************************************************************** */

    /**
     * Store the inter cluster data (flits/credits) of the physical cluster into memory
     * @param physicalCluster
     */
    public void storeInterCluster(Cluster physicalCluster){
        /* Horizontal plane channels */
        for(int i = 0; i < sizeZ; i++){
            for(int j = 0; j < sizeY; j++){
                /* EAST */
                this.interClusterInputChannels.get(0).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterInputChannels().get(0).get(i).get(j).getChannelBuffer());
                this.interClusterOutputChannels.get(0).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterOutputChannels().get(0).get(i).get(j).getChannelBuffer());
                this.interClusterInputCreditChannels.get(0).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterInputCreditChannels().get(0).get(i).get(j).getChannelBuffer());
                this.interClusterOutputCreditChannels.get(0).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterOutputCreditChannels().get(0).get(i).get(j).getChannelBuffer());

                /* WEST */
                this.interClusterInputChannels.get(2).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterInputChannels().get(2).get(i).get(j).getChannelBuffer());
                this.interClusterOutputChannels.get(2).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterOutputChannels().get(2).get(i).get(j).getChannelBuffer());
                this.interClusterInputCreditChannels.get(2).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterInputCreditChannels().get(2).get(i).get(j).getChannelBuffer());
                this.interClusterOutputCreditChannels.get(2).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterOutputCreditChannels().get(2).get(i).get(j).getChannelBuffer());

            }
            for(int j = 0; j < sizeX; j++){
                /* SOUTH */
                this.interClusterInputChannels.get(1).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterInputChannels().get(1).get(i).get(j).getChannelBuffer());
                this.interClusterOutputChannels.get(1).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterOutputChannels().get(1).get(i).get(j).getChannelBuffer());
                this.interClusterInputCreditChannels.get(1).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterInputCreditChannels().get(1).get(i).get(j).getChannelBuffer());
                this.interClusterOutputCreditChannels.get(1).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterOutputCreditChannels().get(1).get(i).get(j).getChannelBuffer());

                /* NORTH */
                this.interClusterInputChannels.get(3).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterInputChannels().get(3).get(i).get(j).getChannelBuffer());
                this.interClusterOutputChannels.get(3).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterOutputChannels().get(3).get(i).get(j).getChannelBuffer());
                this.interClusterInputCreditChannels.get(3).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterInputCreditChannels().get(3).get(i).get(j).getChannelBuffer());
                this.interClusterOutputCreditChannels.get(3).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterOutputCreditChannels().get(3).get(i).get(j).getChannelBuffer());


            }
        }
        /* UP and DOWN channels */
        for(int i = 0; i < sizeY; i++){
            for(int j = 0; j < sizeX; j++){
                /* UP */
                this.interClusterInputChannels.get(4).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterInputChannels().get(4).get(i).get(j).getChannelBuffer());
                this.interClusterOutputChannels.get(4).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterOutputChannels().get(4).get(i).get(j).getChannelBuffer());
                this.interClusterInputCreditChannels.get(4).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterInputCreditChannels().get(4).get(i).get(j).getChannelBuffer());
                this.interClusterOutputCreditChannels.get(4).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterOutputCreditChannels().get(4).get(i).get(j).getChannelBuffer());

                /* DOWN */
                this.interClusterInputChannels.get(5).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterInputChannels().get(5).get(i).get(j).getChannelBuffer());
                this.interClusterOutputChannels.get(5).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterOutputChannels().get(5).get(i).get(j).getChannelBuffer());
                this.interClusterInputCreditChannels.get(5).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterInputCreditChannels().get(5).get(i).get(j).getChannelBuffer());
                this.interClusterOutputCreditChannels.get(5).get(i).get(j).copyChannelBuffer(physicalCluster.getInterClusterOutputCreditChannels().get(5).get(i).get(j).getChannelBuffer());

            }
        }
    }


    /* ********************************************************************************
     *                              GETTERS AND SETTERS                               *
     ******************************************************************************** */

    public List<List<List<FlitChannel>>> getInterClusterInputChannels() {
        return interClusterInputChannels;
    }

    public void setInterClusterInputChannels(List<List<List<FlitChannel>>> interClusterInputChannels) {
        this.interClusterInputChannels = interClusterInputChannels;
    }

    public List<List<List<FlitChannel>>> getInterClusterOutputChannels() {
        return interClusterOutputChannels;
    }

    public void setInterClusterOutputChannels(List<List<List<FlitChannel>>> interClusterOutputChannels) {
        this.interClusterOutputChannels = interClusterOutputChannels;
    }

    public List<List<List<CreditChannel>>> getInterClusterInputCreditChannels() {
        return interClusterInputCreditChannels;
    }

    public void setInterClusterInputCreditChannels(List<List<List<CreditChannel>>> interClusterInputCreditChannels) {
        this.interClusterInputCreditChannels = interClusterInputCreditChannels;
    }

    public List<List<List<CreditChannel>>> getInterClusterOutputCreditChannels() {
        return interClusterOutputCreditChannels;
    }

    public void setInterClusterOutputCreditChannels(List<List<List<CreditChannel>>> interClusterOutputCreditChannels) {
        this.interClusterOutputCreditChannels = interClusterOutputCreditChannels;
    }
}
