/*
File:           Cluster.java
Created:        2020/07/18
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

package mesh;

import building_blocks.CreditChannel;
import building_blocks.FlitChannel;
import ipCore.IPCore;
import memory.MemoryCluster;
import memory.MemoryInterCluster;
import router.Router;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cluster {
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
     *                                  VARIABLES                                     *
     ******************************************************************************** */
    /* Network elements */
    private List<List<List<Router>>> routers;
    private List<List<List<IPCore>>> ipCores;

    /* Cluster elements */
    private int sizeX;
    private int sizeY;
    private int sizeZ;
    private int[] startPosition;

    private boolean adaptive;

    /* - - - - - - - - - - - - - - CHANNELS - - - - - - - - - - - - - - */
    /* Channels used for creating network (in 2D planes)*/
    private List<List<List<FlitChannel>>> horizontalFlitChannels1;
    private List<List<List<FlitChannel>>> horizontalFlitChannels2;
    private List<List<List<FlitChannel>>> verticalFlitChannels1;
    private List<List<List<FlitChannel>>> verticalFlitChannels2;
    private List<List<List<CreditChannel>>> horizontalCreditChannels1;
    private List<List<List<CreditChannel>>> horizontalCreditChannels2;
    private List<List<List<CreditChannel>>> verticalCreditChannels1;
    private List<List<List<CreditChannel>>> verticalCreditChannels2;

    /* Channels used for creating network (in between planes) */
    private List<List<List<FlitChannel>>> interPlaneFlitChannels1;
    private List<List<List<FlitChannel>>> interPlaneFlitChannels2;
    private List<List<List<CreditChannel>>> interPlaneCreditChannels1;
    private List<List<List<CreditChannel>>> interPlaneCreditChannels2;

    /* Local channels */
    private List<List<List<FlitChannel>>> localInputChannels;
    private  List<List<List<FlitChannel>>> localOutputChannels;
    private  List<List<List<CreditChannel>>> localInputCreditChannels;
    private  List<List<List<CreditChannel>>> localOutputCreditChannels;

    /* Inter cluster channels
                NOTE:
                The inter cluster channels are grouped: all channels on EAST, SOUTH, WEST, NORTH, UP and DOWN directions are grouped together. (Upper level of list: InterClusterChannels.get(i))
                For each direction, the channels are saved in a 2D plane.
                    e.g. for    EAST plane, the channel connected to router at z = 0 and y = 1 is: InterClusterChannels.get(0).get(0).get(1)
                                UPPER plane, channel connected to router @ x = 2, y = 1:    InterClusterChannels.get(4).get(1).get(2)   */
    private List<List<List<FlitChannel>>> interClusterInputChannels;
    private List<List<List<FlitChannel>>> interClusterOutputChannels;
    private List<List<List<CreditChannel>>> interClusterInputCreditChannels;
    private List<List<List<CreditChannel>>> interClusterOutputCreditChannels;




    /* Loggers */
    private final static Logger debugLogger = Logger.getLogger("debugLogger");
    private final static Logger resultLogger = Logger.getLogger("resultLogger");

    /* ********************************************************************************
     *                                   CONSTRUCTORS                               *
     ******************************************************************************** */

    public Cluster(int sizeX, int sizeY, int sizeZ, int radix, boolean adaptive,
                   int numPorts, int numVCs, int bufferSize, int sourceQueueSize, int flitsPerPacket, int prob, int precision, int[] hotspots, double hotSpotFactor, double rentExponent){
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.startPosition = new int[]{0,0,0};

        this.adaptive = adaptive;

        this.routers = new ArrayList<List<List<Router>>>();
        this.ipCores = new ArrayList<List<List<IPCore>>>();

        /* Create channels used for interconnecting all routers */
        this.createChannels(sizeX, sizeY, sizeZ);

        /* Create routers and ipCores */
        for(int i = 0; i < sizeZ; i++){
            this.routers.add(new ArrayList<List<Router>>());
            this.ipCores.add(new ArrayList<List<IPCore>>());
            for(int j = 0; j < sizeY; j++){
                this.routers.get(i).add(new ArrayList<Router>());
                this.ipCores.get(i).add(new ArrayList<IPCore>());
                for(int k = 0; k < sizeX; k++){
                    int[] position = new int[]{i,j,k};

                    /* Connect corresponding channel to router */
                    List<FlitChannel> inputChannels = new ArrayList<FlitChannel>();
                    List<CreditChannel> inputCreditChannels = new ArrayList<CreditChannel>();
                    List<FlitChannel> outputChannels = new ArrayList<FlitChannel>();
                    List<CreditChannel> outputCreditChannels = new ArrayList<CreditChannel>();
                    this.addChannels(inputChannels, inputCreditChannels, outputChannels, outputCreditChannels, position, radix);

                    /* Create router itself */
                    Router router = new Router(numPorts, numVCs, bufferSize, position, radix, adaptive, inputChannels, inputCreditChannels, outputChannels, outputCreditChannels);
                    this.routers.get(i).get(j).add(router);

                    /* Create ipCore */
                    FlitChannel ipCoreOutput = localInputChannels.get(i).get(j).get(k);
                    FlitChannel ipCoreInput = localOutputChannels.get(i).get(j).get(k);
                    CreditChannel ipCoreOutputCredit = localInputCreditChannels.get(i).get(j).get(k);
                    CreditChannel ipCoreInputCredit = localOutputCreditChannels.get(i).get(j).get(k);
                    IPCore ipCore = new IPCore(position, radix, numVCs, bufferSize, sourceQueueSize, flitsPerPacket, prob, precision, hotspots, hotSpotFactor, rentExponent, ipCoreOutput, ipCoreOutputCredit, ipCoreInput, ipCoreInputCredit);
                    this.ipCores.get(i).get(j).add(ipCore);
                }
            }
        }
    }

    /* ********************************************************************************
     *                         CONSTRUCTOR HELP FUNCTIONS                           *
     ******************************************************************************** */

    /**
     * This function creates all the channels and add them to the corresponding lists.
     * Only non-edge channels are created.
     */
    private void createChannels(int sizeX, int sizeY, int sizeZ){
        /* Create horizontal channels */
        this.horizontalFlitChannels1 = new ArrayList<List<List<FlitChannel>>>();
        this.horizontalFlitChannels2 = new ArrayList<List<List<FlitChannel>>>();
        this.horizontalCreditChannels1 = new ArrayList<List<List<CreditChannel>>>();
        this.horizontalCreditChannels2 = new ArrayList<List<List<CreditChannel>>>();
        for(int i = 0; i < sizeZ; i++){
            this.horizontalFlitChannels1.add(new ArrayList<List<FlitChannel>>());
            this.horizontalFlitChannels2.add(new ArrayList<List<FlitChannel>>());
            this.horizontalCreditChannels1.add(new ArrayList<List<CreditChannel>>());
            this.horizontalCreditChannels2.add(new ArrayList<List<CreditChannel>>());
            for(int j = 0; j < sizeY; j++){
                this.horizontalFlitChannels1.get(i).add(new ArrayList<FlitChannel>());
                this.horizontalFlitChannels2.get(i).add(new ArrayList<FlitChannel>());
                this.horizontalCreditChannels1.get(i).add(new ArrayList<CreditChannel>());
                this.horizontalCreditChannels2.get(i).add(new ArrayList<CreditChannel>());
                for(int k = 0; k < sizeX; k++){
                    this.horizontalFlitChannels1.get(i).get(j).add(new FlitChannel());
                    this.horizontalFlitChannels2.get(i).get(j).add(new FlitChannel());
                    this.horizontalCreditChannels1.get(i).get(j).add(new CreditChannel());
                    this.horizontalCreditChannels2.get(i).get(j).add(new CreditChannel());
                }
            }
        }

        /* Create vertical channels */
        this.verticalFlitChannels1 = new ArrayList<List<List<FlitChannel>>>();
        this.verticalFlitChannels2 = new ArrayList<List<List<FlitChannel>>>();
        this.verticalCreditChannels1 = new ArrayList<List<List<CreditChannel>>>();
        this.verticalCreditChannels2 = new ArrayList<List<List<CreditChannel>>>();
        for(int i = 0; i < sizeZ; i++){
            this.verticalFlitChannels1.add(new ArrayList<List<FlitChannel>>());
            this.verticalFlitChannels2.add(new ArrayList<List<FlitChannel>>());
            this.verticalCreditChannels1.add(new ArrayList<List<CreditChannel>>());
            this.verticalCreditChannels2.add(new ArrayList<List<CreditChannel>>());
            for(int j = 0; j < sizeY; j++){
                this.verticalFlitChannels1.get(i).add(new ArrayList<FlitChannel>());
                this.verticalFlitChannels2.get(i).add(new ArrayList<FlitChannel>());
                this.verticalCreditChannels1.get(i).add(new ArrayList<CreditChannel>());
                this.verticalCreditChannels2.get(i).add(new ArrayList<CreditChannel>());
                for(int k = 0; k < sizeX; k++){
                    this.verticalFlitChannels1.get(i).get(j).add(new FlitChannel());
                    this.verticalFlitChannels2.get(i).get(j).add(new FlitChannel());
                    this.verticalCreditChannels1.get(i).get(j).add(new CreditChannel());
                    this.verticalCreditChannels2.get(i).get(j).add(new CreditChannel());
                }
            }
        }

        /* Create local channels */
        this.localInputChannels = new ArrayList<List<List<FlitChannel>>>();
        this.localOutputChannels = new ArrayList<List<List<FlitChannel>>>();
        this.localInputCreditChannels = new ArrayList<List<List<CreditChannel>>>();
        this.localOutputCreditChannels = new ArrayList<List<List<CreditChannel>>>();
        for(int i = 0; i < sizeZ; i++){
            localInputChannels.add(new ArrayList<List<FlitChannel>>());
            localOutputChannels.add(new ArrayList<List<FlitChannel>>());
            localInputCreditChannels.add(new ArrayList<List<CreditChannel>>());
            localOutputCreditChannels.add(new ArrayList<List<CreditChannel>>());
            for(int j = 0; j < sizeY; j++){
                localInputChannels.get(i).add(new ArrayList<FlitChannel>());
                localOutputChannels.get(i).add(new ArrayList<FlitChannel>());
                localInputCreditChannels.get(i).add(new ArrayList<CreditChannel>());
                localOutputCreditChannels.get(i).add(new ArrayList<CreditChannel>());
                for(int k = 0 ; k < sizeX; k++){
                    localInputChannels.get(i).get(j).add(new FlitChannel());
                    localOutputChannels.get(i).get(j).add(new FlitChannel());
                    localInputCreditChannels.get(i).get(j).add(new CreditChannel());
                    localOutputCreditChannels.get(i).get(j).add(new CreditChannel());
                }
            }
        }

        /* Create inter-plane channels */
        this.interPlaneFlitChannels1 = new ArrayList<List<List<FlitChannel>>>();
        this.interPlaneFlitChannels2 = new ArrayList<List<List<FlitChannel>>>();
        this.interPlaneCreditChannels1 = new ArrayList<List<List<CreditChannel>>>();
        this.interPlaneCreditChannels2 = new ArrayList<List<List<CreditChannel>>>();
        for(int i = 0; i < sizeZ; i++){
            this.interPlaneFlitChannels1.add(new ArrayList<List<FlitChannel>>());
            this.interPlaneFlitChannels2.add(new ArrayList<List<FlitChannel>>());
            this.interPlaneCreditChannels1.add(new ArrayList<List<CreditChannel>>());
            this.interPlaneCreditChannels2.add(new ArrayList<List<CreditChannel>>());
            for(int j = 0; j < sizeY; j++){
                this.interPlaneFlitChannels1.get(i).add(new ArrayList<FlitChannel>());
                this.interPlaneFlitChannels2.get(i).add(new ArrayList<FlitChannel>());
                this.interPlaneCreditChannels1.get(i).add(new ArrayList<CreditChannel>());
                this.interPlaneCreditChannels2.get(i).add(new ArrayList<CreditChannel>());
                for(int k = 0; k < sizeX; k++){
                    this.interPlaneFlitChannels1.get(i).get(j).add(new FlitChannel());
                    this.interPlaneFlitChannels2.get(i).get(j).add(new FlitChannel());
                    this.interPlaneCreditChannels1.get(i).get(j).add(new CreditChannel());
                    this.interPlaneCreditChannels2.get(i).get(j).add(new CreditChannel());
                }
            }
        }

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
                this.interClusterOutputChannels.get(0).get(i).add(new FlitChannel());           // EAST
                this.interClusterInputCreditChannels.get(0).get(i).add(new CreditChannel());    // EAST
                this.interClusterOutputCreditChannels.get(0).get(i).add(new CreditChannel());   // EAST
                this.interClusterInputChannels.get(2).get(i).add(new FlitChannel());            // WEST
                this.interClusterOutputChannels.get(2).get(i).add(new FlitChannel());           // WEST
                this.interClusterInputCreditChannels.get(2).get(i).add(new CreditChannel());    // WEST
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
                this.interClusterOutputChannels.get(1).get(i).add(new FlitChannel());           // SOUTH
                this.interClusterInputCreditChannels.get(1).get(i).add(new CreditChannel());    // SOUTH
                this.interClusterOutputCreditChannels.get(1).get(i).add(new CreditChannel());   // SOUTH
                this.interClusterInputChannels.get(3).get(i).add(new FlitChannel());            // NORTH
                this.interClusterOutputChannels.get(3).get(i).add(new FlitChannel());           // NORTH
                this.interClusterInputCreditChannels.get(3).get(i).add(new CreditChannel());    // NORTH
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
                this.interClusterOutputChannels.get(4).get(i).add(new FlitChannel());           // UP
                this.interClusterInputCreditChannels.get(4).get(i).add(new CreditChannel());    // UP
                this.interClusterOutputCreditChannels.get(4).get(i).add(new CreditChannel());   // UP
                this.interClusterInputChannels.get(5).get(i).add(new FlitChannel());            // DOWN
                this.interClusterOutputChannels.get(5).get(i).add(new FlitChannel());           // DOWN
                this.interClusterInputCreditChannels.get(5).get(i).add(new CreditChannel());    // DOWN
                this.interClusterOutputCreditChannels.get(5).get(i).add(new CreditChannel());   // DOWN

            }
        }


    }



    /**
     * This function adds the correct channels to the lists such that they can be used to create a router.
     * @param inputFlitChannels: list containing all the input flit channels of this router
     * @param inputCreditChannels: list containing all the input credit channels of this router
     * @param outputFlitChannels: list containing all the output flit channels of this router
     * @param outputCreditChannels: list contining all the output credit channels of this router
     * @param position: position (z,y,x) in the network of this router
     */
    private void addChannels(List<FlitChannel> inputFlitChannels, List<CreditChannel> inputCreditChannels, List<FlitChannel> outputFlitChannels, List<CreditChannel> outputCreditChannels, int[] position, int radix){
        int z = position[0];
        int y = position[1];
        int x = position[2];
        int xMod = x % sizeX;
        int yMod = y % sizeY;
        int zMod = z % sizeZ;

        /* EAST */
        if( xMod < this.sizeX -1 ){     /* Channel inside cluster */
            inputFlitChannels.add(this.horizontalFlitChannels2.get(z).get(y).get(x));
            inputCreditChannels.add(this.horizontalCreditChannels2.get(z).get(y).get(x));
            outputFlitChannels.add(this.horizontalFlitChannels1.get(z).get(y).get(x));
            outputCreditChannels.add(this.horizontalCreditChannels1.get(z).get(y).get(x));
        }
        else {                          /* This channel is at the edge of cluster */
            inputFlitChannels.add(this.interClusterInputChannels.get(0).get(z).get(y));
            inputCreditChannels.add(this.interClusterInputCreditChannels.get(0).get(z).get(y));
            outputFlitChannels.add(this.interClusterOutputChannels.get(0).get(z).get(y));
            outputCreditChannels.add(this.interClusterOutputCreditChannels.get(0).get(z).get(y));
        }
        /* SOUTH */
        if( yMod > 0 ){                 /* Channel inside cluster */
            inputFlitChannels.add(this.verticalFlitChannels1.get(z).get(y-1).get(x));
            inputCreditChannels.add(this.verticalCreditChannels1.get(z).get(y-1).get(x));
            outputFlitChannels.add(this.verticalFlitChannels2.get(z).get(y-1).get(x));
            outputCreditChannels.add(this.verticalCreditChannels2.get(z).get(y-1).get(x));
        }
        else {                          /* This channel is at the edge of cluster */
            inputFlitChannels.add(this.interClusterInputChannels.get(1).get(z).get(x));
            inputCreditChannels.add(this.interClusterInputCreditChannels.get(1).get(z).get(x));
            outputFlitChannels.add(this.interClusterOutputChannels.get(1).get(z).get(x));
            outputCreditChannels.add(this.interClusterOutputCreditChannels.get(1).get(z).get(x));
        }
        /* WEST */
        if( xMod > 0 ){                 /* Channel inside cluster */
            inputFlitChannels.add(this.horizontalFlitChannels1.get(z).get(y).get(x-1));
            inputCreditChannels.add(this.horizontalCreditChannels1.get(z).get(y).get(x-1));
            outputFlitChannels.add(this.horizontalFlitChannels2.get(z).get(y).get(x-1));
            outputCreditChannels.add(this.horizontalCreditChannels2.get(z).get(y).get(x-1));
        }
        else {                          /* This channel is at the edge of cluster */
            inputFlitChannels.add(this.interClusterInputChannels.get(2).get(z).get(y));
            inputCreditChannels.add(this.interClusterInputCreditChannels.get(2).get(z).get(y));
            outputFlitChannels.add(this.interClusterOutputChannels.get(2).get(z).get(y));
            outputCreditChannels.add(this.interClusterOutputCreditChannels.get(2).get(z).get(y));
        }
        /* NORTH */
        if( yMod < this.sizeY -1 ){     /* Channel inside cluster */
            inputFlitChannels.add(this.verticalFlitChannels2.get(z).get(y).get(x));
            inputCreditChannels.add(this.verticalCreditChannels2.get(z).get(y).get(x));
            outputFlitChannels.add(this.verticalFlitChannels1.get(z).get(y).get(x));
            outputCreditChannels.add(this.verticalCreditChannels1.get(z).get(y).get(x));
        }
        else {                           /* This channel is at the edge of cluster */
            inputFlitChannels.add(this.interClusterInputChannels.get(3).get(z).get(x));
            inputCreditChannels.add(this.interClusterInputCreditChannels.get(3).get(z).get(x));
            outputFlitChannels.add(this.interClusterOutputChannels.get(3).get(z).get(x));
            outputCreditChannels.add(this.interClusterOutputCreditChannels.get(3).get(z).get(x));
        }
        /* UP */
        if( zMod < this.sizeZ -1 ){     /* Channel inside cluster */
            inputFlitChannels.add(this.interPlaneFlitChannels2.get(y).get(z).get(x));
            inputCreditChannels.add(this.interPlaneCreditChannels2.get(y).get(z).get(x));
            outputFlitChannels.add(this.interPlaneFlitChannels1.get(y).get(z).get(x));
            outputCreditChannels.add(this.interPlaneCreditChannels1.get(y).get(z).get(x));
        }
        else {                          /* This channel is at the edge of cluster */
            inputFlitChannels.add(this.interClusterInputChannels.get(4).get(y).get(x));
            inputCreditChannels.add(this.interClusterInputCreditChannels.get(4).get(y).get(x));
            outputFlitChannels.add(this.interClusterOutputChannels.get(4).get(y).get(x));
            outputCreditChannels.add(this.interClusterOutputCreditChannels.get(4).get(y).get(x));
        }
        /* DOWN */
        if( zMod > 0 ){                 /* Channel inside cluster */
            inputFlitChannels.add(this.interPlaneFlitChannels1.get(y).get(z-1).get(x));
            inputCreditChannels.add(this.interPlaneCreditChannels1.get(y).get(z-1).get(x));
            outputFlitChannels.add(this.interPlaneFlitChannels2.get(y).get(z-1).get(x));
            outputCreditChannels.add(this.interPlaneCreditChannels2.get(y).get(z-1).get(x));
        }
        else {                          /* This channel is at the edge of cluster */
            inputFlitChannels.add(this.interClusterInputChannels.get(5).get(y).get(x));
            inputCreditChannels.add(this.interClusterInputCreditChannels.get(5).get(y).get(x));
            outputFlitChannels.add(this.interClusterOutputChannels.get(5).get(y).get(x));
            outputCreditChannels.add(this.interClusterOutputCreditChannels.get(5).get(y).get(x));
        }

        /* IPCORE */
        inputFlitChannels.add(localInputChannels.get(z).get(y).get(x));
        inputCreditChannels.add(localInputCreditChannels.get(z).get(y).get(x));
        outputFlitChannels.add(localOutputChannels.get(z).get(y).get(x));
        outputCreditChannels.add(localOutputCreditChannels.get(z).get(y).get(x));

    }


    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                              *
     ******************************************************************************** */

    /**
     * Simulate this cluster
     */
    public void simulateCluster(boolean stallNetwork){
        for(int i = 0; i < this.sizeZ; i++){
            for(int j = 0; j < this.sizeY; j++){
                for(int k = 0; k < this.sizeX; k++){
                    /* Simulating routers and ipcores */
                    debugLogger.log(Level.FINE, " ------ " + routers.get(i).get(j).get(k));
                    if(!stallNetwork) {
                        routers.get(i).get(j).get(k).simulateRouter();
                    } else {
                        debugLogger.log(Level.FINE, "Network stalled: router not simulated");

                    }

                    ipCores.get(i).get(j).get(k).simulateIPCore(stallNetwork);
                }
            }
        }

        this.updateNetworkTime(stallNetwork);
    }

    /**
     * Update the network time
     * @param stallNetwork: indicates whether or not the network is being stalled.
     */
    private void updateNetworkTime(boolean stallNetwork){
        for(int i = 0; i < this.sizeZ; i++){
            for(int j = 0; j < this.sizeY; j++){
                for(int k = 0; k < this.sizeX; k++){
                    ipCores.get(i).get(j).get(k).updateNetworkTime(stallNetwork);
                }
            }
        }

    }

    /**
     * Check if the network needs to be stalled
     * @return true if network needs to be stalled
     */
    public boolean checkNetworkStalling(){
        boolean stall = false;
        for(int i = 0; i < this.sizeZ && !stall; i++) {
            for (int j = 0; j < this.sizeY && !stall; j++) {
                for (int k = 0; k < this.sizeX && !stall; k++) {
                    stall = stall || ipCores.get(i).get(j).get(k).checkNetworkStalling();
                }
            }
        }

        return stall;
    }



    /* ********************************************************************************
     *                                 LOADING AND STORING                            *
     ******************************************************************************** */

    /**
     * Load variables from memory into this cluster
     * @param memoryCluster: memory element containing all information about this cluster
     */
    public void loadCluster(MemoryCluster memoryCluster){
        /* Load routers and IP cores */
        for(int i = 0; i < sizeZ; i++){
            for(int j = 0; j < sizeY; j++){
                for(int k = 0; k < sizeX; k++){
                    this.routers.get(i).get(j).get(k).loadState(memoryCluster.getMemoryRouter(i,j,k));
                    this.ipCores.get(i).get(j).get(k).loadState(memoryCluster.getMemoryIPCore(i,j,k));
                }
            }
        }

        /* Load position */
        this.startPosition = memoryCluster.getStartPosition();

        /* Load channels inside cluster */
        for(int i = 0; i < sizeZ; i++){
            for(int j = 0; j < sizeY; j++){
                for(int k = 0; k < sizeX; k++){
                    /* Update buffers */
                    this.horizontalFlitChannels1.get(i).get(j).get(k).copyChannelBuffer(memoryCluster.getHorizontalFlitChannels1().get(i).get(j).get(k).getChannelBuffer());
                    this.horizontalFlitChannels2.get(i).get(j).get(k).copyChannelBuffer(memoryCluster.getHorizontalFlitChannels2().get(i).get(j).get(k).getChannelBuffer());
                    this.verticalFlitChannels1.get(i).get(j).get(k).copyChannelBuffer(memoryCluster.getVerticalFlitChannels1().get(i).get(j).get(k).getChannelBuffer());
                    this.verticalFlitChannels2.get(i).get(j).get(k).copyChannelBuffer(memoryCluster.getVerticalFlitChannels2().get(i).get(j).get(k).getChannelBuffer());
                    this.horizontalCreditChannels1.get(i).get(j).get(k).copyChannelBuffer(memoryCluster.getHorizontalCreditChannels1().get(i).get(j).get(k).getChannelBuffer());
                    this.horizontalCreditChannels2.get(i).get(j).get(k).copyChannelBuffer(memoryCluster.getHorizontalCreditChannels2().get(i).get(j).get(k).getChannelBuffer());
                    this.verticalCreditChannels1.get(i).get(j).get(k).copyChannelBuffer(memoryCluster.getVerticalCreditChannels1().get(i).get(j).get(k).getChannelBuffer());
                    this.verticalCreditChannels2.get(i).get(j).get(k).copyChannelBuffer(memoryCluster.getVerticalCreditChannels2().get(i).get(j).get(k).getChannelBuffer());
                    this.interPlaneFlitChannels1.get(i).get(j).get(k).copyChannelBuffer(memoryCluster.getInterPlaneFlitChannels1().get(i).get(j).get(k).getChannelBuffer());
                    this.interPlaneFlitChannels2.get(i).get(j).get(k).copyChannelBuffer(memoryCluster.getInterPlaneFlitChannels2().get(i).get(j).get(k).getChannelBuffer());
                    this.interPlaneCreditChannels1.get(i).get(j).get(k).copyChannelBuffer(memoryCluster.getInterPlaneCreditChannels1().get(i).get(j).get(k).getChannelBuffer());
                    this.interPlaneCreditChannels2.get(i).get(j).get(k).copyChannelBuffer(memoryCluster.getInterPlaneCreditChannels2().get(i).get(j).get(k).getChannelBuffer());
                    this.localInputChannels.get(i).get(j).get(k).copyChannelBuffer(memoryCluster.getLocalInputChannels().get(i).get(j).get(k).getChannelBuffer());
                    this.localOutputChannels.get(i).get(j).get(k).copyChannelBuffer(memoryCluster.getLocalOutputChannels().get(i).get(j).get(k).getChannelBuffer());
                    this.localInputCreditChannels.get(i).get(j).get(k).copyChannelBuffer(memoryCluster.getLocalInputCreditChannels().get(i).get(j).get(k).getChannelBuffer());
                    this.localOutputCreditChannels.get(i).get(j).get(k).copyChannelBuffer(memoryCluster.getLocalOutputCreditChannels().get(i).get(j).get(k).getChannelBuffer());

                    /* Update edge variable */
                    this.horizontalFlitChannels1.get(i).get(j).get(k).setEdge(memoryCluster.getHorizontalFlitChannels1().get(i).get(j).get(k).isEdge());
                    this.horizontalFlitChannels2.get(i).get(j).get(k).setEdge(memoryCluster.getHorizontalFlitChannels2().get(i).get(j).get(k).isEdge());
                    this.verticalFlitChannels1.get(i).get(j).get(k).setEdge(memoryCluster.getVerticalFlitChannels1().get(i).get(j).get(k).isEdge());
                    this.verticalFlitChannels2.get(i).get(j).get(k).setEdge(memoryCluster.getVerticalFlitChannels2().get(i).get(j).get(k).isEdge());
                    this.horizontalCreditChannels1.get(i).get(j).get(k).setEdge(memoryCluster.getHorizontalCreditChannels1().get(i).get(j).get(k).isEdge());
                    this.horizontalCreditChannels2.get(i).get(j).get(k).setEdge(memoryCluster.getHorizontalCreditChannels2().get(i).get(j).get(k).isEdge());
                    this.verticalCreditChannels1.get(i).get(j).get(k).setEdge(memoryCluster.getVerticalCreditChannels1().get(i).get(j).get(k).isEdge());
                    this.verticalCreditChannels2.get(i).get(j).get(k).setEdge(memoryCluster.getVerticalCreditChannels2().get(i).get(j).get(k).isEdge());
                    this.interPlaneFlitChannels1.get(i).get(j).get(k).setEdge(memoryCluster.getInterPlaneFlitChannels1().get(i).get(j).get(k).isEdge());
                    this.interPlaneFlitChannels2.get(i).get(j).get(k).setEdge(memoryCluster.getInterPlaneFlitChannels2().get(i).get(j).get(k).isEdge());
                    this.interPlaneCreditChannels1.get(i).get(j).get(k).setEdge(memoryCluster.getInterPlaneCreditChannels1().get(i).get(j).get(k).isEdge());
                    this.interPlaneCreditChannels2.get(i).get(j).get(k).setEdge(memoryCluster.getInterPlaneCreditChannels2().get(i).get(j).get(k).isEdge());
                    this.localInputChannels.get(i).get(j).get(k).setEdge(memoryCluster.getLocalInputChannels().get(i).get(j).get(k).isEdge());
                    this.localOutputChannels.get(i).get(j).get(k).setEdge(memoryCluster.getLocalOutputChannels().get(i).get(j).get(k).isEdge());
                    this.localInputCreditChannels.get(i).get(j).get(k).setEdge(memoryCluster.getLocalInputCreditChannels().get(i).get(j).get(k).isEdge());
                    this.localOutputCreditChannels.get(i).get(j).get(k).setEdge(memoryCluster.getLocalOutputCreditChannels().get(i).get(j).get(k).isEdge());


                }
            }
        }

    }


    /**
     * Load variables from memory to update the channels inbetween clusters
     * @param memoryInterCluster: memory element containing all information about inter cluster channels
     */
    public void loadInterClusterChannels(MemoryInterCluster memoryInterCluster){

        /* Horizontal plane channels */
        for(int i = 0; i < sizeZ; i++){
            for(int j = 0; j < sizeY; j++){
                /* EAST */
                this.interClusterInputChannels.get(0).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterInputChannels().get(0).get(i).get(j).getChannelBuffer());
                this.interClusterOutputChannels.get(0).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterOutputChannels().get(0).get(i).get(j).getChannelBuffer());
                this.interClusterInputCreditChannels.get(0).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterInputCreditChannels().get(0).get(i).get(j).getChannelBuffer());
                this.interClusterOutputCreditChannels.get(0).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterOutputCreditChannels().get(0).get(i).get(j).getChannelBuffer());
                this.interClusterInputChannels.get(0).get(i).get(j).setEdge(memoryInterCluster.getInterClusterInputChannels().get(0).get(i).get(j).isEdge());
                this.interClusterOutputChannels.get(0).get(i).get(j).setEdge(memoryInterCluster.getInterClusterOutputChannels().get(0).get(i).get(j).isEdge());
                this.interClusterInputCreditChannels.get(0).get(i).get(j).setEdge(memoryInterCluster.getInterClusterInputCreditChannels().get(0).get(i).get(j).isEdge());
                this.interClusterOutputCreditChannels.get(0).get(i).get(j).setEdge(memoryInterCluster.getInterClusterOutputCreditChannels().get(0).get(i).get(j).isEdge());

                /* WEST */
                this.interClusterInputChannels.get(2).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterInputChannels().get(2).get(i).get(j).getChannelBuffer());
                this.interClusterOutputChannels.get(2).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterOutputChannels().get(2).get(i).get(j).getChannelBuffer());
                this.interClusterInputCreditChannels.get(2).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterInputCreditChannels().get(2).get(i).get(j).getChannelBuffer());
                this.interClusterOutputCreditChannels.get(2).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterOutputCreditChannels().get(2).get(i).get(j).getChannelBuffer());
                this.interClusterInputChannels.get(2).get(i).get(j).setEdge(memoryInterCluster.getInterClusterInputChannels().get(2).get(i).get(j).isEdge());
                this.interClusterOutputChannels.get(2).get(i).get(j).setEdge(memoryInterCluster.getInterClusterOutputChannels().get(2).get(i).get(j).isEdge());
                this.interClusterInputCreditChannels.get(2).get(i).get(j).setEdge(memoryInterCluster.getInterClusterInputCreditChannels().get(2).get(i).get(j).isEdge());
                this.interClusterOutputCreditChannels.get(2).get(i).get(j).setEdge(memoryInterCluster.getInterClusterOutputCreditChannels().get(2).get(i).get(j).isEdge());

            }
            for(int j = 0; j < sizeX; j++){
                /* SOUTH */
                this.interClusterInputChannels.get(1).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterInputChannels().get(1).get(i).get(j).getChannelBuffer());
                this.interClusterOutputChannels.get(1).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterOutputChannels().get(1).get(i).get(j).getChannelBuffer());
                this.interClusterInputCreditChannels.get(1).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterInputCreditChannels().get(1).get(i).get(j).getChannelBuffer());
                this.interClusterOutputCreditChannels.get(1).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterOutputCreditChannels().get(1).get(i).get(j).getChannelBuffer());
                this.interClusterInputChannels.get(1).get(i).get(j).setEdge(memoryInterCluster.getInterClusterInputChannels().get(1).get(i).get(j).isEdge());
                this.interClusterOutputChannels.get(1).get(i).get(j).setEdge(memoryInterCluster.getInterClusterOutputChannels().get(1).get(i).get(j).isEdge());
                this.interClusterInputCreditChannels.get(1).get(i).get(j).setEdge(memoryInterCluster.getInterClusterInputCreditChannels().get(1).get(i).get(j).isEdge());
                this.interClusterOutputCreditChannels.get(1).get(i).get(j).setEdge(memoryInterCluster.getInterClusterOutputCreditChannels().get(1).get(i).get(j).isEdge());

                /* NORTH */
                this.interClusterInputChannels.get(3).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterInputChannels().get(3).get(i).get(j).getChannelBuffer());
                this.interClusterOutputChannels.get(3).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterOutputChannels().get(3).get(i).get(j).getChannelBuffer());
                this.interClusterInputCreditChannels.get(3).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterInputCreditChannels().get(3).get(i).get(j).getChannelBuffer());
                this.interClusterOutputCreditChannels.get(3).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterOutputCreditChannels().get(3).get(i).get(j).getChannelBuffer());
                this.interClusterInputChannels.get(3).get(i).get(j).setEdge(memoryInterCluster.getInterClusterInputChannels().get(3).get(i).get(j).isEdge());
                this.interClusterOutputChannels.get(3).get(i).get(j).setEdge(memoryInterCluster.getInterClusterOutputChannels().get(3).get(i).get(j).isEdge());
                this.interClusterInputCreditChannels.get(3).get(i).get(j).setEdge(memoryInterCluster.getInterClusterInputCreditChannels().get(3).get(i).get(j).isEdge());
                this.interClusterOutputCreditChannels.get(3).get(i).get(j).setEdge(memoryInterCluster.getInterClusterOutputCreditChannels().get(3).get(i).get(j).isEdge());


            }
        }
        /* UP and DOWN channels */
        for(int i = 0; i < sizeY; i++){
            for(int j = 0; j < sizeX; j++){
                /* UP */
                this.interClusterInputChannels.get(4).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterInputChannels().get(4).get(i).get(j).getChannelBuffer());
                this.interClusterOutputChannels.get(4).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterOutputChannels().get(4).get(i).get(j).getChannelBuffer());
                this.interClusterInputCreditChannels.get(4).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterInputCreditChannels().get(4).get(i).get(j).getChannelBuffer());
                this.interClusterOutputCreditChannels.get(4).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterOutputCreditChannels().get(4).get(i).get(j).getChannelBuffer());
                this.interClusterInputChannels.get(4).get(i).get(j).setEdge(memoryInterCluster.getInterClusterInputChannels().get(4).get(i).get(j).isEdge());
                this.interClusterOutputChannels.get(4).get(i).get(j).setEdge(memoryInterCluster.getInterClusterOutputChannels().get(4).get(i).get(j).isEdge());
                this.interClusterInputCreditChannels.get(4).get(i).get(j).setEdge(memoryInterCluster.getInterClusterInputCreditChannels().get(4).get(i).get(j).isEdge());
                this.interClusterOutputCreditChannels.get(4).get(i).get(j).setEdge(memoryInterCluster.getInterClusterOutputCreditChannels().get(4).get(i).get(j).isEdge());

                /* DOWN */
                this.interClusterInputChannels.get(5).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterInputChannels().get(5).get(i).get(j).getChannelBuffer());
                this.interClusterOutputChannels.get(5).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterOutputChannels().get(5).get(i).get(j).getChannelBuffer());
                this.interClusterInputCreditChannels.get(5).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterInputCreditChannels().get(5).get(i).get(j).getChannelBuffer());
                this.interClusterOutputCreditChannels.get(5).get(i).get(j).copyChannelBuffer(memoryInterCluster.getInterClusterOutputCreditChannels().get(5).get(i).get(j).getChannelBuffer());
                this.interClusterInputChannels.get(5).get(i).get(j).setEdge(memoryInterCluster.getInterClusterInputChannels().get(5).get(i).get(j).isEdge());
                this.interClusterOutputChannels.get(5).get(i).get(j).setEdge(memoryInterCluster.getInterClusterOutputChannels().get(5).get(i).get(j).isEdge());
                this.interClusterInputCreditChannels.get(5).get(i).get(j).setEdge(memoryInterCluster.getInterClusterInputCreditChannels().get(5).get(i).get(j).isEdge());
                this.interClusterOutputCreditChannels.get(5).get(i).get(j).setEdge(memoryInterCluster.getInterClusterOutputCreditChannels().get(5).get(i).get(j).isEdge());


            }
        }

    }




    /* ********************************************************************************
     *                              GETTERS AND SETTERS                             *
     ******************************************************************************** */

    public List<List<List<Router>>> getRouters() {
        return routers;
    }

    public void setRouters(List<List<List<Router>>> routers) {
        this.routers = routers;
    }

    public List<List<List<IPCore>>> getIpCores() {
        return ipCores;
    }

    public void setIpCores(List<List<List<IPCore>>> ipCores) {
        this.ipCores = ipCores;
    }

    public int getSizeX() {
        return sizeX;
    }

    public void setSizeX(int sizeX) {
        this.sizeX = sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public void setSizeY(int sizeY) {
        this.sizeY = sizeY;
    }

    public int getSizeZ() {
        return sizeZ;
    }

    public void setSizeZ(int sizeZ) {
        this.sizeZ = sizeZ;
    }

    public int[] getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int[] startPosition) {
        this.startPosition = startPosition;
    }

    public List<List<List<FlitChannel>>> getHorizontalFlitChannels1() {
        return horizontalFlitChannels1;
    }

    public void setHorizontalFlitChannels1(List<List<List<FlitChannel>>> horizontalFlitChannels1) {
        this.horizontalFlitChannels1 = horizontalFlitChannels1;
    }

    public List<List<List<FlitChannel>>> getHorizontalFlitChannels2() {
        return horizontalFlitChannels2;
    }

    public void setHorizontalFlitChannels2(List<List<List<FlitChannel>>> horizontalFlitChannels2) {
        this.horizontalFlitChannels2 = horizontalFlitChannels2;
    }

    public List<List<List<FlitChannel>>> getVerticalFlitChannels1() {
        return verticalFlitChannels1;
    }

    public void setVerticalFlitChannels1(List<List<List<FlitChannel>>> verticalFlitChannels1) {
        this.verticalFlitChannels1 = verticalFlitChannels1;
    }

    public List<List<List<FlitChannel>>> getVerticalFlitChannels2() {
        return verticalFlitChannels2;
    }

    public void setVerticalFlitChannels2(List<List<List<FlitChannel>>> verticalFlitChannels2) {
        this.verticalFlitChannels2 = verticalFlitChannels2;
    }

    public List<List<List<CreditChannel>>> getHorizontalCreditChannels1() {
        return horizontalCreditChannels1;
    }

    public void setHorizontalCreditChannels1(List<List<List<CreditChannel>>> horizontalCreditChannels1) {
        this.horizontalCreditChannels1 = horizontalCreditChannels1;
    }

    public List<List<List<CreditChannel>>> getHorizontalCreditChannels2() {
        return horizontalCreditChannels2;
    }

    public void setHorizontalCreditChannels2(List<List<List<CreditChannel>>> horizontalCreditChannels2) {
        this.horizontalCreditChannels2 = horizontalCreditChannels2;
    }

    public List<List<List<CreditChannel>>> getVerticalCreditChannels1() {
        return verticalCreditChannels1;
    }

    public void setVerticalCreditChannels1(List<List<List<CreditChannel>>> verticalCreditChannels1) {
        this.verticalCreditChannels1 = verticalCreditChannels1;
    }

    public List<List<List<CreditChannel>>> getVerticalCreditChannels2() {
        return verticalCreditChannels2;
    }

    public void setVerticalCreditChannels2(List<List<List<CreditChannel>>> verticalCreditChannels2) {
        this.verticalCreditChannels2 = verticalCreditChannels2;
    }

    public List<List<List<FlitChannel>>> getInterPlaneFlitChannels1() {
        return interPlaneFlitChannels1;
    }

    public void setInterPlaneFlitChannels1(List<List<List<FlitChannel>>> interPlaneFlitChannels1) {
        this.interPlaneFlitChannels1 = interPlaneFlitChannels1;
    }

    public List<List<List<FlitChannel>>> getInterPlaneFlitChannels2() {
        return interPlaneFlitChannels2;
    }

    public void setInterPlaneFlitChannels2(List<List<List<FlitChannel>>> interPlaneFlitChannels2) {
        this.interPlaneFlitChannels2 = interPlaneFlitChannels2;
    }

    public List<List<List<CreditChannel>>> getInterPlaneCreditChannels1() {
        return interPlaneCreditChannels1;
    }

    public void setInterPlaneCreditChannels1(List<List<List<CreditChannel>>> interPlaneCreditChannels1) {
        this.interPlaneCreditChannels1 = interPlaneCreditChannels1;
    }

    public List<List<List<CreditChannel>>> getInterPlaneCreditChannels2() {
        return interPlaneCreditChannels2;
    }

    public void setInterPlaneCreditChannels2(List<List<List<CreditChannel>>> interPlaneCreditChannels2) {
        this.interPlaneCreditChannels2 = interPlaneCreditChannels2;
    }

    public List<List<List<FlitChannel>>> getLocalInputChannels() {
        return localInputChannels;
    }

    public void setLocalInputChannels(List<List<List<FlitChannel>>> localInputChannels) {
        this.localInputChannels = localInputChannels;
    }

    public List<List<List<FlitChannel>>> getLocalOutputChannels() {
        return localOutputChannels;
    }

    public void setLocalOutputChannels(List<List<List<FlitChannel>>> localOutputChannels) {
        this.localOutputChannels = localOutputChannels;
    }

    public List<List<List<CreditChannel>>> getLocalInputCreditChannels() {
        return localInputCreditChannels;
    }

    public void setLocalInputCreditChannels(List<List<List<CreditChannel>>> localInputCreditChannels) {
        this.localInputCreditChannels = localInputCreditChannels;
    }

    public List<List<List<CreditChannel>>> getLocalOutputCreditChannels() {
        return localOutputCreditChannels;
    }

    public void setLocalOutputCreditChannels(List<List<List<CreditChannel>>> localOutputCreditChannels) {
        this.localOutputCreditChannels = localOutputCreditChannels;
    }

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
