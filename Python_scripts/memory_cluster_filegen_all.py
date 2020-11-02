# This script is used to create the VHDL file for storing STATE MEMORY


import datetime

today = datetime.date.today()
date = str(today.day) + "/" + str(today.month) + "/" + str(today.year)

# PARAMETERS
PORTS = 7
VCS = 4
BUFFER_SIZE = 20
SQ_SIZE = 4

clusters = {"111" : "111"}

for sizeX in range(1,15):
    for sizeY in range(1,sizeX + 1):
        for sizeZ in range(1,sizeY + 1):
            test = str(sizeZ) + str(sizeY)+ str(sizeX)
            sortedString = "".join(sorted(test))
            total_routers = sizeX*sizeY*sizeZ
            if(total_routers < 28):
#                clusters[sortedString] = sortedString
                
                #signal_counter = 0
                
                file = open("cluster/gen_memory_cluster_" + str(sizeZ) + "x" + str(sizeY) +"x" + str(sizeX) + ".vhd", "w")
                
                
                entity_name = "memory_cluster_"+ str(sizeZ) + "x" + str(sizeY) +"x" + str(sizeX)
                prefix = ["           in_" , "           out_", "signal mem_"]
                
                
                # WRITE HEADER
                line = "----------------------------------------------------------------------------------"
                file.write(line + "\n")
                line = "--  File:           " + entity_name + ".vhd"
                file.write(line + "\n")
                line = "--  Created:        23/07/2020"
                file.write(line + "\n")
                line = "--  Last Changed:   " + date
                file.write(line + "\n")
                line = "--  Author:         Jonathan D'Hoore"
                file.write(line + "\n")
                line = "--                  University of Ghent"
                file.write(line + "\n")
                line = "--"
                file.write(line + "\n")
                line = "--  Part of Master's dissertation submitted in order to obtain the academic degree of"
                file.write(line + "\n")
                line = "--  Master of Science in Electrical Engineering - main subject Electronic Circuits and Systems"
                file.write(line + "\n")
                line = "--"
                file.write(line + "\n")
                line = "--  Academic year 2019-2020"
                file.write(line + "\n")
                line = "-- "
                file.write(line + "\n")
                line = "----------------------------------------------------------------------------------"
                file.write(line + "\n")
                
                
                file.write("\n")
                
                # Write libraries
                line = "library IEEE;"
                file.write(line + "\n")
                line = "use IEEE.STD_LOGIC_1164.ALL;"
                file.write(line + "\n")
                line = "use work.common.all;"
                file.write(line + "\n")
                
                
                
                file.write("\n")
                file.write("\n")
                
                
                # Write entity and port
                line = "entity " + entity_name + " is"
                file.write(line + "\n")
                line = "    Port ( clk                  : in    STD_ULOGIC;"
                file.write(line + "\n")
                line = "           wr_en                : in    STD_ULOGIC;"
                file.write(line + "\n")
                line = "           address              : in    integer range 0 to NUM_CLUSTERS-1;"
                file.write(line + "\n")
                
                # Write port inputs and outputs
                for a in range(len(prefix)):
                    if(a == 0):
                        file.write("\n")
                        file.write("\n")
                        file.write("           -- INPUTS: \n")
                    if(a == 1):
                        file.write("\n")
                        file.write("\n")
                        file.write("           -- OUTPUTS: \n")
                    if(a == 2):
                        # Start architecture 
                        line = "architecture Behavioral of " + entity_name + " is"
                        file.write(line + "\n")
                        # Define array types
                        # ROUTER:
                        line = "-- Array types for router memory ";
                        file.write(line + "\n")
                        line = "type global_state_array is array(0 to NUM_CLUSTERS-1) of global_state;"
                        file.write(line + "\n")
                        line = "type INT_VCP_ARRAY      is array(0 to NUM_CLUSTERS-1) of integer range 0 to PORTS*VCS-1;"
                        file.write(line + "\n")
                        line = "type INT_PORT_D_ARRAY   is array(0 to NUM_CLUSTERS-1) of integer range 0 to PORTS-1;"
                        file.write(line + "\n")
                        line = "type cb_config_array    is array(0 to NUM_CLUSTERS-1) of STD_ULOGIC_VECTOR(0 to PORTS*PORTS-1);"
                        file.write(line + "\n")
                        line = "type vc_state_array     is array(0 to NUM_CLUSTERS-1) of STD_ULOGIC_VECTOR(0 to PORTS*VCS-1); "
                        file.write(line + "\n")
                        
                        # IPCORE:
                        line = "-- Array types for IP Core memory ";
                        file.write(line + "\n")
                        line = "type lt_array       is array(0 to NUM_CLUSTERS-1) of STD_LOGIC_VECTOR(14 downto 0);"
                        file.write(line + "\n")
                        line = "type send_vc_array  is array(0 to NUM_CLUSTERS-1) of integer range 0 to VCS-1;"
                        file.write(line + "\n")
                        line = "type vc_reqs_array  is array(0 to NUM_CLUSTERS-1) of STD_LOGIC_VECTOR(0 to SQ_SIZE-1);"
                        file.write(line + "\n")
                        line = "type free_slots_array  is array(0 to NUM_CLUSTERS-1) of integer range 0 to SQ_SIZE;"
                        file.write(line + "\n")
                        
                        file.write("\n")
                        file.write("\n")
                        
                        line = "attribute ram_style: string;"
                        file.write(line + "\n")
                        
                        file.write("\n")
                        file.write("\n")
                        
                        
                    for i in range(sizeZ):
                        for j in range(sizeY):
                            for k in range(sizeX):
                                
                                # ROUTER MEMORY
                                RID = "R" + str(i) + "_" + str(j) + "_" + str(k)
                                
#                              
                                # Global state for each input unit
                                for m in range(PORTS):
                                    for n in range(VCS):
                                        VCID = "_P" + str(m) + "_VC" + str(n)
                                        type = "      : in    global_state;"
                                        #signal_counter+=1
                                        if(a == 1):
                                            type = "      : out    global_state;"
                                        elif(a == 2):
                                            type = "      :    global_state_array;"
                                        line = prefix[a] + "state_" + RID + VCID + type
                                        file.write(line + "\n")
                                        if( a == 2):
                                            line = "attribute ram_style of " + "mem_" + "state_" + RID + VCID +": signal is \"block\";"
                                            file.write(line + "\n")
                                        
                                file.write("\n")
                                        
                                # Allocated VC for each input unit
                                for m in range(PORTS):
                                    for n in range(VCS):
                                        VCID = "_P" + str(m) + "_VC" + str(n)
                                        type = "      : in    integer range 0 to VCS;"
                                        if(a == 1):
                                            type = "      : out    integer range 0 to VCS;"
                                        elif(a == 2):
                                            type = "      :    INT_VC_ARRAY(0 to NUM_CLUSTERS-1);"
                                        line = prefix[a] + "alloc_vc_" + RID + VCID + type
                                        file.write(line + "\n")
                                        if( a == 2):
                                            line = "attribute ram_style of " + "mem_" + "alloc_vc_" + RID + VCID +": signal is \"block\";"
                                            file.write(line + "\n")
                                
                                file.write("\n")
                                # Allocated Port for each input unit
                                for m in range(PORTS):
                                    for n in range(VCS):
                                        VCID = "_P" + str(m) + "_VC" + str(n)
                                        type = "      : in    integer range 0 to PORTS;"
                                        if(a == 1):
                                            type = "      : out    integer range 0 to PORTS;"
                                        elif(a == 2):
                                            type = "      :    INT_PORT_ARRAY(0 to NUM_CLUSTERS-1);"
                                        line = prefix[a] + "alloc_port_" + RID + VCID + type
                                        file.write(line + "\n")
                                        if( a == 2):
                                            line = "attribute ram_style of " + "mem_" + "alloc_port_" + RID + VCID +": signal is \"block\";"
                                            file.write(line + "\n")
                                        
                                        
                                                       
                                file.write("\n")
                                # Buffer for each input unit (each buffer is split in seperate flits)
                                for m in range(PORTS):
                                    for n in range(VCS):
                                        VCID = "_P" + str(m) + "_VC" + str(n)
                                        type = "      : in    FLIT;"
                                        if(a == 1):
                                            type = "      : out    FLIT;"
                                        elif(a == 2):
                                            type = "      :    FLIT_ARRAY(0 to NUM_CLUSTERS-1);"
                                        for b in range(BUFFER_SIZE):
                                            BID = "buf" + str(b) + "_"
                                            line = prefix[a] + BID + RID + VCID + type
                                            file.write(line + "\n")
                                            if( a == 2):
                                                line = "attribute ram_style of " + "mem_" + BID + RID + VCID +": signal is \"block\";"
                                                file.write(line + "\n")
                                            
                                file.write("\n")
                                 # Priorities of VC Allocator output and input arbiters
                                for m in range(PORTS):
                                    for n in range(VCS):
                                        VCID = "_P" + str(m) + "_VC" + str(n)
                                        type = "      : in    integer range 0 to PORTS*VCS-1;"
                                        if(a == 1):
                                            type = "      : out    integer range 0 to PORTS*VCS-1;"
                                        elif(a == 2):
                                            type = "      :    INT_VCP_ARRAY;"
                                        line = prefix[a] + "vc_out_prio_" + RID + VCID + type
                                        file.write(line + "\n")
                                        if( a == 2):
                                            line = "attribute ram_style of " + "mem_" + "vc_out_prio_" + RID + VCID +": signal is \"block\";"
                                            file.write(line + "\n")
                                
                                for m in range(PORTS):
                                    for n in range(VCS):
                                        VCID = "_P" + str(m) + "_VC" + str(n)
                                        type = "      : in    integer range 0 to PORTS*VCS-1;"
                                        if(a == 1):
                                            type = "      : out    integer range 0 to PORTS*VCS-1;"
                                        elif(a == 2):
                                            type = "      :    INT_VCP_ARRAY;"
                                        line = prefix[a] + "vc_in_prio_" + RID + VCID + type
                                        file.write(line + "\n")
                                        if( a == 2):
                                            line = "attribute ram_style of " + "mem_" + "vc_in_prio_" + RID + VCID +": signal is \"block\";"
                                            file.write(line + "\n")
                                        
                                file.write("\n")
                                # States of output VC (kept in VC allocator)
                                type = "      : in    STD_ULOGIC_VECTOR(0 to PORTS*VCS-1);"
                                if(a == 1):
                                    type = "      : out    STD_ULOGIC_VECTOR(0 to PORTS*VCS-1);"
                                elif(a == 2):
                                    type = "      :    vc_state_array;"
                                line = prefix[a] + "output_vc_states_" + RID + type
                                file.write(line + "\n")
                                if( a == 2):
                                    line = "attribute ram_style of " + "mem_" + "output_vc_states_" + RID +": signal is \"block\";"
                                    file.write(line + "\n")
                                
                                file.write("\n")
                                # Priorities of Switch Allocator output and input Arbiters
                                for m in range(PORTS):
                                    PID = "_P" + str(m)
                                    type = "      : in    integer range 0 to PORTS-1;"
                                    if(a == 1):
                                        type = "      : out    integer range 0 to PORTS-1;"
                                    elif(a == 2):
                                        type = "      :    INT_PORT_D_ARRAY;"
                                    line = prefix[a] + "sw_out_prio_" + RID + PID + type
                                    file.write(line + "\n")
                                    if( a == 2):
                                        line = "attribute ram_style of " + "mem_" + "sw_out_prio_" + RID + PID +": signal is \"block\";"
                                        file.write(line + "\n")
                                
                                for m in range(PORTS):
                                    PID = "_P" + str(m)
                                    type = "      : in    integer range 0 to PORTS-1;"
                                    if(a == 1):
                                        type = "      : out    integer range 0 to PORTS-1;"
                                    elif(a == 2):
                                        type = "      :    INT_PORT_D_ARRAY;"
                                    line = prefix[a] + "sw_in_prio_" + RID + PID + type
                                    file.write(line + "\n")
                                    if( a == 2):
                                        line = "attribute ram_style of " + "mem_" + "sw_in_prio_" + RID + PID +": signal is \"block\";"
                                        file.write(line + "\n")
                                
                                file.write("\n")
                                # Crossbar configuration
                                type = "      : in    STD_ULOGIC_VECTOR(0 to PORTS*PORTS-1);"
                                if(a == 1):
                                    type = "      : out    STD_ULOGIC_VECTOR(0 to PORTS*PORTS-1);"
                                elif(a == 2):
                                    type = "      :    cb_config_array;"
                                line = prefix[a] + "cb_config_" + RID + type
                                file.write(line + "\n")
                                if( a == 2):
                                    line = "attribute ram_style of " + "mem_" + "cb_config_" + RID +": signal is \"block\";"
                                    file.write(line + "\n")

                                    
                                file.write("\n")
                                # Credit counter for each output VC
                                for m in range(PORTS):
                                    for n in range(VCS):
                                        VCID = "_P" + str(m) + "_VC" + str(n)
                                        type = "      : in    integer range 0 to BUFFER_SIZE;"
                                        if(a == 1):
                                            type = "      : out    integer range 0 to BUFFER_SIZE;"
                                        elif(a == 2):
                                            type = "      :    INT_CC_ARRAY(0 to NUM_CLUSTERS-1);"
                                        line = prefix[a] + "credits_" + RID + VCID + type
                                        file.write(line + "\n")
                                        if( a == 2):
                                            line = "attribute ram_style of " + "mem_" + "credits_" + RID + VCID +": signal is \"block\";"
                                            file.write(line + "\n")
                                        
                                file.write("\n")
                                file.write("\n")
                                
                                # IP CORE MEMORY
                                IP_ID = "IP" + str(i) + "_" + str(j) + "_" + str(k)
                                
                                # Source queue
                                for m in range(SQ_SIZE):
                                    type = "      : in    PACKET_DESCR;"
                                    if(a == 1):
                                        type = "      : out    PACKET_DESCR;"
                                    elif(a == 2):
                                        type = "      :    PD_ARRAY(0 to NUM_CLUSTERS-1);"
                                    line = prefix[a] + "source_queue" + str(m) + "_" + IP_ID + type
                                    file.write(line + "\n")
                                    if( a == 2):
                                        line = "attribute ram_style of " + "mem_" + "source_queue" + str(m) + "_" + IP_ID +": signal is \"block\";"
                                        file.write(line + "\n")
                                    
                                # Local time
                                type = "      : in    STD_LOGIC_VECTOR(14 downto 0);"
                                if(a == 1):
                                    type = "      : out    STD_LOGIC_VECTOR(14 downto 0);"
                                elif(a == 2):
                                    type = "      :    lt_array;"
                                line = prefix[a] + "local_time_" + IP_ID + type
                                file.write(line + "\n")
                                if( a == 2):
                                    line = "attribute ram_style of " + "mem_" + "local_time_" + IP_ID +": signal is \"block\";"
                                    file.write(line + "\n")
                                
                            
                                # Sending VC
                                type = "      : in    integer range 0 to VCS-1;"
                                if(a == 1):
                                    type = "      : out    integer range 0 to VCS-1;"
                                elif(a == 2):
                                    type = "      :    send_vc_array;"
                                line = prefix[a] + "sending_vc_" + IP_ID + type
                                file.write(line + "\n")
                                if( a == 2):
                                    line = "attribute ram_style of " + "mem_" + "sending_vc_" + IP_ID +": signal is \"block\";"
                                    file.write(line + "\n")
                                
                                # Source queue write address
                                type = "      : in    integer range 0 to SQ_SIZE-1;"
                                if(a == 1):
                                    type = "      : out    integer range 0 to SQ_SIZE-1;"
                                elif(a == 2):
                                    type = "      :    INT_SQ_ARRAY(0 to NUM_CLUSTERS-1);"
                                line = prefix[a] + "sq_wr_address_" + IP_ID + type
                                file.write(line + "\n")
                                if( a == 2):
                                    line = "attribute ram_style of " + "mem_" + "sq_wr_address_" + IP_ID +": signal is \"block\";"
                                    file.write(line + "\n")
                                
                                # Credit counters
                                for m in range(VCS):
                                    VC_ID = "_VC_" + str(m)
                                    type = "      : in    integer range 0 to BUFFER_SIZE;"
                                    if(a == 1):
                                        type = "      : out    integer range 0 to BUFFER_SIZE;"
                                    elif(a == 2):
                                        type = "      :    INT_CC_ARRAY(0 to NUM_CLUSTERS-1);"
                                    line = prefix[a] + "credit_count_" + IP_ID + VC_ID + type
                                    file.write(line + "\n")
                                    if( a == 2):
                                        line = "attribute ram_style of " + "mem_" + "credit_count_" + IP_ID + VC_ID +": signal is \"block\";"
                                        file.write(line + "\n")
                                    
                                # VC allocation
                                for m in range(VCS):
                                    VC_ID = "_VC_" + str(m)
                                    type = "      : in    integer range 0 to SQ_SIZE-1;"
                                    if(a == 1):
                                        type = "      : out    integer range 0 to SQ_SIZE-1;"
                                    elif(a == 2):
                                        type = "      :    INT_SQ_ARRAY(0 to NUM_CLUSTERS-1);"
                                    line = prefix[a] + "vc_allocs_" + IP_ID + VC_ID + type
                                    file.write(line + "\n")
                                    if( a == 2):
                                        line = "attribute ram_style of " + "mem_" + "vc_allocs_" + IP_ID + VC_ID +": signal is \"block\";"
                                        file.write(line + "\n")
                                    
                                #Flits left
                                for m in range(VCS):
                                    VC_ID = "_VC_" + str(m)
                                    type = "      : in    integer range 0 to PACKET_SIZE;"
                                    if(a == 1):
                                        type = "      : out    integer range 0 to PACKET_SIZE;"
                                    elif(a == 2):
                                        type = "      :    INT_PS_ARRAY(0 to NUM_CLUSTERS-1);"
                                    line = prefix[a] + "flits_left_" + IP_ID + VC_ID + type
                                    file.write(line + "\n")
                                    if( a == 2):
                                        line = "attribute ram_style of " + "mem_" + "flits_left_" + IP_ID + VC_ID +": signal is \"block\";"
                                        file.write(line + "\n")
                                                
                                # VC REQS
                                type = "      : in    STD_LOGIC_VECTOR(0 to SQ_SIZE-1);"
                                if(a == 1):
                                    type = "      : out    STD_LOGIC_VECTOR(0 to SQ_SIZE-1);"
                                elif(a == 2):
                                    type = "      :    vc_reqs_array;"
                                line = prefix[a] + "vc_reqs_" + IP_ID + type
                                file.write(line + "\n")
                                if( a == 2):
                                    line = "attribute ram_style of " + "mem_" + "vc_reqs_" + IP_ID +": signal is \"block\";"
                                    file.write(line + "\n")
                                
                                # Free slots
                                type = "      : in    integer range 0 to SQ_SIZE;"
                                if(a == 1):
                                    type = "      : out    integer range 0 to SQ_SIZE;"
                                elif(a == 2):
                                    type = "      :    free_slots_array;"
                                line = prefix[a] + "free_slots_" + IP_ID + type
                                file.write(line + "\n")
                                if( a == 2):
                                    line = "attribute ram_style of " + "mem_" + "free_slots_" + IP_ID +": signal is \"block\";"
                                    file.write(line + "\n")
                                    
                                # Stalling
                                type = "      : in    STD_ULOGIC;"
                                if(a == 1):
                                    type = "      : out    STD_ULOGIC;"
                                    if(i == sizeZ-1 and j == sizeY-1 and k == sizeX -1 and m == VCS-1):
                                        type = "      : out    STD_ULOGIC"
                                elif(a == 2):
                                    type = "      :    STD_ULOGIC_VECTOR(0 to NUM_CLUSTERS-1);"
                                line = prefix[a] + "stalling_" + IP_ID + type
                                file.write(line + "\n")
                                if( a == 2):
                                    line = "attribute ram_style of " + "mem_" + "stalling_" + IP_ID +": signal is \"block\";"
                                    file.write(line + "\n")
                                    
                                  
                                file.write("\n")
                                file.write("\n")
                                
                    if(a == 1):
                        line = "    );"
                        file.write(line + "\n")
                        line = "end " + entity_name + ";"
                        file.write(line + "\n")
                        file.write("\n")
                        file.write("\n")
                                
#                print(entity_name + " : "  +str(signal_counter))
                # Start of architecture behavioral
                line = "begin"
                file.write(line + "\n")
                file.write("\n")
                file.write("\n")
                
                line = "-- Synchronous writing and reading (reading first)";
                file.write(line + "\n")
                
                # architecture implementation: process
                line = "process(clk)"
                file.write(line + "\n")
                line = "begin"
                file.write(line + "\n")
                line = "    if rising_edge(clk) then"
                file.write(line + "\n")
                line = "        if wr_en = '1' then"
                file.write(line + "\n")
                
                for i in range(sizeZ):
                    for j in range(sizeY):
                        for k in range(sizeX): 
                            # ROUTER MEMORY
                            RID = "R" + str(i) + "_" + str(j) + "_" + str(k)
                                                   
                            # Global state for each input unit
                            for m in range(PORTS):
                                for n in range(VCS):
                                    VCID = "_P" + str(m) + "_VC" + str(n)
                                    line = "            mem_state_" + RID + VCID + "(address)" + " <= " + "in_state_" + RID + VCID + ";"
                                    file.write(line + "\n")
                                    
                            file.write("\n")
                                    
                            # Allocated VC for each input unit
                            for m in range(PORTS):
                                for n in range(VCS):
                                    VCID = "_P" + str(m) + "_VC" + str(n)
                                    line = "            mem_alloc_vc_" + RID + VCID + "(address)" + " <= " + "in_alloc_vc_" + RID + VCID + ";"
                                    file.write(line + "\n")
                            
                            file.write("\n")
                            # Allocated Port for each input unit
                            for m in range(PORTS):
                                for n in range(VCS):
                                    VCID = "_P" + str(m) + "_VC" + str(n)
                                    line = "            mem_alloc_port_" + RID + VCID + "(address)" + " <= " + "in_alloc_port_" + RID + VCID + ";"
                                    file.write(line + "\n")
                                    
                                    
                            file.write("\n")
                            # Buffer for each input unit (each buffer is split in seperate flits)
                            for m in range(PORTS):
                                for n in range(VCS):
                                    VCID = "_P" + str(m) + "_VC" + str(n)
                                    for b in range(BUFFER_SIZE):
                                        BID = "buf" + str(b) + "_"
                                        line = "            mem_" + BID + RID + VCID + "(address)" + " <= " + "in_" + BID + RID + VCID + ";"
                                        file.write(line + "\n")
                                        
                            file.write("\n")
                             # Priorities of VC Allocator output and input arbiters
                            for m in range(PORTS):
                                for n in range(VCS):
                                    VCID = "_P" + str(m) + "_VC" + str(n)
                                    line = "            mem_vc_out_prio_" + RID + VCID + "(address)" + " <= " + "in_vc_out_prio_" + RID + VCID + ";"
                                    file.write(line + "\n")
                            
                            for m in range(PORTS):
                                for n in range(VCS):
                                    VCID = "_P" + str(m) + "_VC" + str(n)
                                    line = "            mem_vc_in_prio_" + RID + VCID + "(address)" + " <= " + "in_vc_in_prio_" + RID + VCID + ";"
                                    file.write(line + "\n")
                                    
                            file.write("\n")
                            # States of output VC (kept in VC allocator)
                            line = "            mem_output_vc_states_" + RID + "(address)" + " <= " + "in_output_vc_states_" + RID  + ";"
                            file.write(line + "\n")
                            
                            file.write("\n")
                            # Priorities of Switch Allocator output and input Arbiters
                            for m in range(PORTS):
                                PID = "_P" + str(m)
                                line = "            mem_sw_out_prio_" + RID + PID + "(address)" + " <= " + "in_sw_out_prio_" + RID + PID + ";"
                                file.write(line + "\n")
                            
                            for m in range(PORTS):
                                PID = "_P" + str(m)
                                line = "            mem_sw_in_prio_" + RID + PID + "(address)" + " <= " + "in_sw_in_prio_" + RID + PID  + ";"
                                file.write(line + "\n")
                            
                            file.write("\n")
                            # Crossbar configuration
                            line = "            mem_cb_config_" + RID + "(address)" + " <= " + "in_cb_config_" + RID + ";"
                            file.write(line + "\n")
                            
                                
                            file.write("\n")
                            # Credit counter for each output VC
                            for m in range(PORTS):
                                for n in range(VCS):
                                    VCID = "_P" + str(m) + "_VC" + str(n)
                                    line = "            mem_credits_" + RID + VCID + "(address)" + " <= " + "in_credits_" + RID + VCID + ";"
                                    file.write(line + "\n")
                            
                            # IP CORE MEMORY
                            IP_ID = "IP" + str(i) + "_" + str(j) + "_" + str(k)
                            
                            # Source queue
                            for m in range(SQ_SIZE):
                                line = "            mem_source_queue" + str(m) + "_" + IP_ID + "(address)" + " <= " + "in_source_queue" + str(m) + "_" + IP_ID  + ";"
                                file.write(line + "\n")
                                
                            # Local time
                            line = "            mem_local_time_" + IP_ID + "(address)" + " <= " + "in_local_time_" + IP_ID + ";"
                            file.write(line + "\n")
                            
                            # Sending VC
                            line = "            mem_sending_vc_" + IP_ID + "(address)" + " <= " + "in_sending_vc_" + IP_ID + ";"
                            file.write(line + "\n")
                            
                            # Source queue write address
                            line = "            mem_sq_wr_address_" + IP_ID + "(address)" + " <= " + "in_sq_wr_address_" + IP_ID + ";"
                            file.write(line + "\n")
                            
                            # Credit counters
                            for m in range(VCS):
                                VC_ID = "_VC_" + str(m)
                                line = "            mem_credit_count_" + IP_ID + VC_ID + "(address)" + " <= " + "in_credit_count_" + IP_ID + VC_ID + ";"
                                file.write(line + "\n")
                                
                            # VC allocation
                            for m in range(VCS):
                                VC_ID = "_VC_" + str(m)
                                line = "            mem_vc_allocs_" + IP_ID + VC_ID + "(address)" + " <= " + "in_vc_allocs_" + IP_ID + VC_ID + ";"
                                file.write(line + "\n")
                                
                            #Flits left
                            for m in range(VCS):
                                VC_ID = "_VC_" + str(m)
                                line = "            mem_flits_left_" + IP_ID + VC_ID + "(address)" + " <= " + "in_flits_left_" + IP_ID + VC_ID + ";"
                                file.write(line + "\n")
                                
                            # VC reqs
                            line = "            mem_vc_reqs_" + IP_ID + "(address)" + " <= " + "in_vc_reqs_" + IP_ID + ";"
                            file.write(line + "\n")
                            
                            # Free slots
                            line = "            mem_free_slots_" + IP_ID + "(address)" + " <= " + "in_free_slots_" + IP_ID + ";"
                            file.write(line + "\n")
                            
                            #  Stalling
                            line = "            mem_stalling_" + IP_ID + "(address)" + " <= " + "in_stalling_" + IP_ID + ";"
                            file.write(line + "\n")
                
                line = "        end if;"
                file.write(line + "\n")
                file.write("\n")
                
                # Reading part:
                line = "        -- Synchronous reading"
                file.write(line + "\n")
                for i in range(sizeZ):
                    for j in range(sizeY):
                        for k in range(sizeX): 
                            # ROUTER MEMORY
                            RID = "R" + str(i) + "_" + str(j) + "_" + str(k)
                        
                            # Global state for each input unit
                            for m in range(PORTS):
                                for n in range(VCS):
                                    VCID = "_P" + str(m) + "_VC" + str(n)
                                    line = "        out_state_" + RID + VCID + " <= " + "mem_state_" + RID + VCID + "(address)"  + ";"
                                    file.write(line + "\n")
                                    
                            file.write("\n")
                                    
                            # Allocated VC for each input unit
                            for m in range(PORTS):
                                for n in range(VCS):
                                    VCID = "_P" + str(m) + "_VC" + str(n)
                                    line = "        out_alloc_vc_" + RID + VCID + " <= " + "mem_alloc_vc_" + RID + VCID + "(address)" + ";"
                                    file.write(line + "\n")
                            
                            file.write("\n")
                            # Allocated Port for each input unit
                            for m in range(PORTS):
                                for n in range(VCS):
                                    VCID = "_P" + str(m) + "_VC" + str(n)
                                    line = "        out_alloc_port_" + RID + VCID + " <= " + "mem_alloc_port_" + RID + VCID + "(address)" + ";"
                                    file.write(line + "\n")
                                    
                            file.write("\n")
                            # Buffer for each input unit (each buffer is split in seperate flits)
                            for m in range(PORTS):
                                for n in range(VCS):
                                    VCID = "_P" + str(m) + "_VC" + str(n)
                                    for b in range(BUFFER_SIZE):
                                        BID = "buf" + str(b) + "_"
                                        line = "        out_" + BID + RID + VCID + " <= " + "mem_" + BID + RID + VCID + "(address)" + ";"
                                        file.write(line + "\n")
                                        
                            file.write("\n")
                             # Priorities of VC Allocator output and input arbiters
                            for m in range(PORTS):
                                for n in range(VCS):
                                    VCID = "_P" + str(m) + "_VC" + str(n)
                                    line = "        out_vc_out_prio_" + RID + VCID + " <= " + "mem_vc_out_prio_" + RID + VCID + "(address)" + ";"
                                    file.write(line + "\n")
                            
                            for m in range(PORTS):
                                for n in range(VCS):
                                    VCID = "_P" + str(m) + "_VC" + str(n)
                                    line = "        out_vc_in_prio_" + RID + VCID + " <= " +"mem_vc_in_prio_" + RID + VCID + "(address)" + ";"
                                    file.write(line + "\n")
                                    
                            file.write("\n")
                            # States of output VC (kept in VC allocator)
                            line = "        out_output_vc_states_" + RID + " <= " + "mem_output_vc_states_" + RID  + "(address)" + ";"
                            file.write(line + "\n")
                            
                            file.write("\n")
                            # Priorities of Switch Allocator output and input Arbiters
                            for m in range(PORTS):
                                PID = "_P" + str(m)
                                line = "        out_sw_out_prio_" + RID + PID + " <= " + "mem_sw_out_prio_" + RID + PID + "(address)" + ";"
                                file.write(line + "\n")
                            
                            for m in range(PORTS):
                                PID = "_P" + str(m)
                                line = "        out_sw_in_prio_" + RID + PID + " <= " + "mem_sw_in_prio_" + RID + PID  + "(address)" + ";"
                                file.write(line + "\n")
                            
                            file.write("\n")
                            # Crossbar configuration
                            line = "        out_cb_config_" + RID + " <= " + "mem_cb_config_" + RID + "(address)" + ";"
                            file.write(line + "\n")
                            
                                           
                            file.write("\n")
                            # Credit counter for each output VC
                            for m in range(PORTS):
                                for n in range(VCS):
                                    VCID = "_P" + str(m) + "_VC" + str(n)
                                    line = "        out_credits_" + RID + VCID + " <= " + "mem_credits_" + RID + VCID + "(address)" + ";"
                                    file.write(line + "\n")
                            
                            # IP CORE MEMORY
                            IP_ID = "IP" + str(i) + "_" + str(j) + "_" + str(k)
                            
                            # Source queue
                            for m in range(SQ_SIZE):
                                line = "        out_source_queue" + str(m) + "_" + IP_ID + " <= " + "mem_source_queue" + str(m) + "_" + IP_ID  + "(address)" + ";"
                                file.write(line + "\n")
                                
                            # Local time
                            line = "        out_local_time_" + IP_ID + " <= " + "mem_local_time_" + IP_ID + "(address)" + ";"
                            file.write(line + "\n")
                            
                            
                            # Sending VC
                            line = "        out_sending_vc_" + IP_ID + " <= " + "mem_sending_vc_" + IP_ID + "(address)" + ";"
                            file.write(line + "\n")
                            
                            # Source queue write address
                            line = "        out_sq_wr_address_" + IP_ID + " <= " + "mem_sq_wr_address_" + IP_ID + "(address)" + ";"
                            file.write(line + "\n")
                            
                            # Credit counters
                            for m in range(VCS):
                                VC_ID = "_VC_" + str(m)
                                line = "        out_credit_count_" + IP_ID + VC_ID + " <= " + "mem_credit_count_" + IP_ID + VC_ID + "(address)" + ";"
                                file.write(line + "\n")
                                
                            # VC allocation
                            for m in range(VCS):
                                VC_ID = "_VC_" + str(m)
                                line = "        out_vc_allocs_" + IP_ID + VC_ID + " <= " + "mem_vc_allocs_" + IP_ID + VC_ID + "(address)" + ";"
                                file.write(line + "\n")
                                
                            #Flits left
                            for m in range(VCS):
                                VC_ID = "_VC_" + str(m)
                                line = "        out_flits_left_" + IP_ID + VC_ID + " <= " + "mem_flits_left_" + IP_ID + VC_ID + "(address)" + ";"
                                file.write(line + "\n")
                                
                            # VC reqs
                            line = "        out_vc_reqs_" + IP_ID + " <= " + "mem_vc_reqs_" + IP_ID + "(address)" + ";"
                            file.write(line + "\n")
                            
                            # Free slots
                            line = "        out_free_slots_" + IP_ID + " <= " + "mem_free_slots_" + IP_ID + "(address)" + ";"
                            file.write(line + "\n")
                            
                            #  Stalling
                            line = "        out_stalling_" + IP_ID + " <= " + "mem_stalling_" + IP_ID + "(address)" + ";"
                            file.write(line + "\n")
                
                line = "    end if;"
                file.write(line + "\n")
                line = "end process;"
                file.write(line + "\n")          
                
                file.write("\n")
                file.write("\n")    
                
                line = "end Behavioral;"
                file.write(line + "\n");
                file.close()
                
                
                
                

            