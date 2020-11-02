/*
File:           Arbiter.java
Created:        2020/05/10
Last Changed:   2020/05/10
Author:         Jonathan D'Hoore
                University of Ghent

Part of Master's dissertation submitted in order to obtain the academic degree of
Master of Science in Electrical Engineering - main subject Electronic Circuits and Systems


Academic year 2019-2020
*/

package iSLIP;

import router.OutputPort;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Arbiter {
    /* ********************************************************************************
     *                                  VARIABLES                                   *
     ******************************************************************************** */
    private int priority;

    /* Loggers */
    private final static Logger debugLogger = Logger.getLogger("debugLogger");
    private final static Logger resultLogger = Logger.getLogger("resultLogger");

    /* ********************************************************************************
     *                                   CONSTRUCTORS                               *
     ******************************************************************************** */

    /**
     * Constructor for creating a new arbiter
     */
    public Arbiter(){
        this.priority = 0;
    }

    /* ********************************************************************************
     *                                 CLASS FUNCTIONS                              *
     ******************************************************************************** */

    /**
     * Implementation of arbiter functionality: grant access to one of the requesters.
     * Requester with highest priority will get first chance, priority decreases sequentially for following requesters.
     * State is not taken into account: resource is always available
     * @param requests: list of requests
     * @return grants: list of grants
     */
    public List<Boolean> request(List<Boolean> requests){
        return this.request(requests, OutputPort.IDLE);
    }

    /**
     * Implementation of arbiter functionality: grant access to one of the requesters.
     * Requester with highest priority will get first chance, priority decreases sequentially for following requesters.
     * If resource is busy, no grant is possible.
     * @param requests: list of requests
     * @param state: state of the resource
     * @return grants: list of grants
     */
    public List<Boolean> request(List<Boolean> requests, int state){
        /* Make grants array */
        List<Boolean> grants = new ArrayList<>();
        for(int i = 0; i < requests.size(); i++){
            grants.add(false);
        }

        /* Check if the resource is IDLE */
        if(state == OutputPort.IDLE){
            /* Starting from requester with highest priority, grant access to one requester */
            int p = this.priority;
            for(int i = 0; i < requests.size(); i++){
                if(requests.get(p)){
                    grants.set(p, true);
                    break;
                }
                p = (p + 1) % requests.size();
            }
        }

        return grants;
    }

    /* ********************************************************************************
     *                              GETTERS AND SETTERS                             *
     ******************************************************************************** */

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }


}
