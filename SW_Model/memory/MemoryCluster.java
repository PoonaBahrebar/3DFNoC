/*
File:           MemoryCluster.java
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

public class MemoryCluster {
    /* ********************************************************************************
     *                                  VARIABLES                                     *
     ******************************************************************************** */
    private List<List<List<MemoryRouter>>> routers;
    private List<List<List<MemoryIPCore>>> ipcores;

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



    /* ********************************************************************************
     *                                   CONSTRUCTORS                                 *
     ******************************************************************************** */

    /**
     * Constructor for memory of a cluster
     * @param sizeX: size of the cluster in x direction
     * @param sizeY: size of the cluster in y direction
     * @param sizeZ: size of the cluster in z direction
     * @param startPosition: position of the node with lowest coordinates
     * @param numPorts: Number of ports per router
     * @param numVCs: number of VCs per port
     * @param bufferSize: size of the buffer at each input unit (in number of flits)
     */
    public MemoryCluster(int sizeX, int sizeY, int sizeZ, int[] startPosition, boolean adaptive,
                         int numPorts, int numVCs, int bufferSize){
        this.routers = new ArrayList<List<List<MemoryRouter>>>();
        this.ipcores = new ArrayList<List<List<MemoryIPCore>>>();

        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;

        this.adaptive = adaptive;

        this.startPosition = startPosition;

        for(int i = 0; i < sizeZ; i++){
            this.routers.add(new ArrayList<List<MemoryRouter>>());
            this.ipcores.add(new ArrayList<List<MemoryIPCore>>());
            for(int j = 0; j < sizeY; j++){
                this.routers.get(i).add(new ArrayList<MemoryRouter>());
                this.ipcores.get(i).add(new ArrayList<MemoryIPCore>());
                for(int k = 0; k < sizeX; k++){
                    int[] position = new int[3];
                    position[0] = startPosition[0] + i;
                    position[1] = startPosition[1] + j;
                    position[2] = startPosition[2] + k;
                    this.routers.get(i).get(j).add(new MemoryRouter(position, numPorts, numVCs, bufferSize));
                    this.ipcores.get(i).get(j).add(new MemoryIPCore(position, numVCs, bufferSize));
                }
            }
        }

        this.createChannels(sizeX, sizeY, sizeZ);
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


    }



    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                                *
     ******************************************************************************** */

    /**
     * Store the state of the cluster into this memory element
     * @param cluster: cluster  of which the state needs to be stored into memory
     */
    public void storeCluster(Cluster cluster){
        /* Store router and ipcore states */
        for(int i = 0; i < sizeZ; i++){
            for(int j = 0; j < sizeY; j++){
                for(int k = 0; k < sizeX; k++){
                    this.routers.get(i).get(j).get(k).storeRouter(cluster.getRouters().get(i).get(j).get(k));
                    this.ipcores.get(i).get(j).get(k).storeIPCore(cluster.getIpCores().get(i).get(j).get(k));
                }
            }
        }

        /* Store channels into memory */
        for(int i = 0; i < sizeZ; i++){
            for(int j = 0; j < sizeY; j++){
                for(int k = 0; k < sizeX; k++){
                    /* Update buffers */
                    this.horizontalFlitChannels1.get(i).get(j).get(k).copyChannelBuffer(cluster.getHorizontalFlitChannels1().get(i).get(j).get(k).getChannelBuffer());
                    this.horizontalFlitChannels2.get(i).get(j).get(k).copyChannelBuffer(cluster.getHorizontalFlitChannels2().get(i).get(j).get(k).getChannelBuffer());
                    this.verticalFlitChannels1.get(i).get(j).get(k).copyChannelBuffer(cluster.getVerticalFlitChannels1().get(i).get(j).get(k).getChannelBuffer());
                    this.verticalFlitChannels2.get(i).get(j).get(k).copyChannelBuffer(cluster.getVerticalFlitChannels2().get(i).get(j).get(k).getChannelBuffer());
                    this.horizontalCreditChannels1.get(i).get(j).get(k).copyChannelBuffer(cluster.getHorizontalCreditChannels1().get(i).get(j).get(k).getChannelBuffer());
                    this.horizontalCreditChannels2.get(i).get(j).get(k).copyChannelBuffer(cluster.getHorizontalCreditChannels2().get(i).get(j).get(k).getChannelBuffer());
                    this.verticalCreditChannels1.get(i).get(j).get(k).copyChannelBuffer(cluster.getVerticalCreditChannels1().get(i).get(j).get(k).getChannelBuffer());
                    this.verticalCreditChannels2.get(i).get(j).get(k).copyChannelBuffer(cluster.getVerticalCreditChannels2().get(i).get(j).get(k).getChannelBuffer());
                    this.interPlaneFlitChannels1.get(i).get(j).get(k).copyChannelBuffer(cluster.getInterPlaneFlitChannels1().get(i).get(j).get(k).getChannelBuffer());
                    this.interPlaneFlitChannels2.get(i).get(j).get(k).copyChannelBuffer(cluster.getInterPlaneFlitChannels2().get(i).get(j).get(k).getChannelBuffer());
                    this.interPlaneCreditChannels1.get(i).get(j).get(k).copyChannelBuffer(cluster.getInterPlaneCreditChannels1().get(i).get(j).get(k).getChannelBuffer());
                    this.interPlaneCreditChannels2.get(i).get(j).get(k).copyChannelBuffer(cluster.getInterPlaneCreditChannels2().get(i).get(j).get(k).getChannelBuffer());
                    this.localInputChannels.get(i).get(j).get(k).copyChannelBuffer(cluster.getLocalInputChannels().get(i).get(j).get(k).getChannelBuffer());
                    this.localOutputChannels.get(i).get(j).get(k).copyChannelBuffer(cluster.getLocalOutputChannels().get(i).get(j).get(k).getChannelBuffer());
                    this.localInputCreditChannels.get(i).get(j).get(k).copyChannelBuffer(cluster.getLocalInputCreditChannels().get(i).get(j).get(k).getChannelBuffer());
                    this.localOutputCreditChannels.get(i).get(j).get(k).copyChannelBuffer(cluster.getLocalOutputCreditChannels().get(i).get(j).get(k).getChannelBuffer());

                }
            }
        }

    }

    /* ********************************************************************************
     *                              GETTERS AND SETTERS                               *
     ******************************************************************************** */

    public int[] getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int[] startPosition) {
        this.startPosition = startPosition;
    }

    public MemoryRouter getMemoryRouter(int z, int y, int x){
        return this.routers.get(z).get(y).get(x);
    }

    public MemoryIPCore getMemoryIPCore(int z, int y, int x){
        return this.ipcores.get(z).get(y).get(x);
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
}
