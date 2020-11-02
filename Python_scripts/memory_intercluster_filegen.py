# This script is used to create the VHDL file for storing STATE MEMORY

import datetime

today = datetime.date.today()
date = str(today.day) + "/" + str(today.month) + "/" + str(today.year)

clusters = {"111" : "111"}

for sizeX in range(1,15):
    for sizeY in range(1,sizeX + 1):
        for sizeZ in range(1,sizeY + 1):
            test = str(sizeZ) + str(sizeY)+ str(sizeX)
            sortedString = "".join(sorted(test))
            total_routers = sizeX*sizeY*sizeZ
            if(total_routers < 25):
                
                file = open("intercluster/gen_memory_intercluster_" + str(sizeZ) + "x" + str(sizeY) +"x" + str(sizeX) + ".vhd", "w")
                
                
                entity_name = "memory_intercluster_"+ str(sizeZ) + "x" + str(sizeY) +"x" + str(sizeX)
                prefix = ["           in_" , "           out_", "signal mem_"]
                
                
                # WRITE HEADER
                line = "----------------------------------------------------------------------------------"
                file.write(line + "\n")
                line = "--  File:           " + entity_name + ".vhd"
                file.write(line + "\n")
                line = "--  Created:        31/07/2020"
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
                        
                        
                        line = "attribute ram_style: string;"
                        file.write(line + "\n")
                        
                        file.write("\n")
                        file.write("\n")
                        
                        
                   
                    type = "      : in    FLIT;"
                    if(a == 1):
                        type = "      : out    FLIT;"
                    elif(a == 2):
                        type = "      :    FLIT_ARRAY(0 to NUM_CLUSTERS-1);"
                    # EAST and WEST channels
                    for i in range(sizeZ):
                        for j in range(sizeY):
                            RID = "E_" + str(i) + "_" + str(j)
                            line = prefix[a] + "flit_in_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "flit_in_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                            RID = "W_" + str(i) + "_" + str(j)
                            line = prefix[a] + "flit_in_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "flit_in_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                                
                                
                    # SOUTH and NORTH channels
                    for i in range(sizeZ):
                        for j in range(sizeX):
                            RID = "S_" + str(i) + "_" + str(j)
                            line = prefix[a] + "flit_in_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "flit_in_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                            RID = "N_" + str(i) + "_" + str(j)
                            line = prefix[a] + "flit_in_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "flit_in_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                                
                    # UP and DOWN channels
                    for i in range(sizeY):
                        for j in range(sizeX):
                            RID = "U_" + str(i) + "_" + str(j)
                            line = prefix[a] + "flit_in_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "flit_in_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                            RID = "D_" + str(i) + "_" + str(j)
                            line = prefix[a] + "flit_in_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "flit_in_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                            
                    # EAST and WEST channels
                    for i in range(sizeZ):
                        for j in range(sizeY):
                            RID = "E_" + str(i) + "_" + str(j)
                            line = prefix[a] + "flit_out_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "flit_out_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                            RID = "W_" + str(i) + "_" + str(j)
                            line = prefix[a] + "flit_out_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "flit_out_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                                
                                
                    # SOUTH and NORTH channels
                    for i in range(sizeZ):
                        for j in range(sizeX):
                            RID = "S_" + str(i) + "_" + str(j)
                            line = prefix[a] + "flit_out_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "flit_out_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                            RID = "N_" + str(i) + "_" + str(j)
                            line = prefix[a] + "flit_out_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "flit_out_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                                
                    # UP and DOWN channels
                    for i in range(sizeY):
                        for j in range(sizeX):
                            RID = "U_" + str(i) + "_" + str(j)
                            line = prefix[a] + "flit_out_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "flit_out_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                            RID = "D_" + str(i) + "_" + str(j)
                            line = prefix[a] + "flit_out_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "flit_out_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                            
                            
                    type = "      : in    CREDIT;"
                    if(a == 1):
                        type = "      : out    CREDIT;"
                    elif(a == 2):
                        type = "      :    CREDIT_ARRAY(0 to NUM_CLUSTERS-1);"
                    # EAST and WEST channels
                    for i in range(sizeZ):
                        for j in range(sizeY):
                            RID = "E_" + str(i) + "_" + str(j)
                            line = prefix[a] + "credit_in_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "credit_in_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                            RID = "W_" + str(i) + "_" + str(j)
                            line = prefix[a] + "credit_in_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "credit_in_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                                
                                
                    # SOUTH and NORTH channels
                    for i in range(sizeZ):
                        for j in range(sizeX):
                            RID = "S_" + str(i) + "_" + str(j)
                            line = prefix[a] + "credit_in_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "credit_in_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                            RID = "N_" + str(i) + "_" + str(j)
                            line = prefix[a] + "credit_in_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "credit_in_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                                
                    # UP and DOWN channels
                    for i in range(sizeY):
                        for j in range(sizeX):
                            RID = "U_" + str(i) + "_" + str(j)
                            line = prefix[a] + "credit_in_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "credit_in_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                            RID = "D_" + str(i) + "_" + str(j)
                            line = prefix[a] + "credit_in_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "credit_in_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                            
                    # EAST and WEST channels
                    for i in range(sizeZ):
                        for j in range(sizeY):
                            RID = "E_" + str(i) + "_" + str(j)
                            line = prefix[a] + "credit_out_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "credit_out_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                            RID = "W_" + str(i) + "_" + str(j)
                            line = prefix[a] + "credit_out_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "credit_out_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                                
                                
                    # SOUTH and NORTH channels
                    for i in range(sizeZ):
                        for j in range(sizeX):
                            RID = "S_" + str(i) + "_" + str(j)
                            line = prefix[a] + "credit_out_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "credit_out_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                            RID = "N_" + str(i) + "_" + str(j)
                            line = prefix[a] + "credit_out_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "credit_out_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                                
                    # UP and DOWN channels
                    for i in range(sizeY):
                        for j in range(sizeX):
                            RID = "U_" + str(i) + "_" + str(j)
                            line = prefix[a] + "credit_out_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "credit_out_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")
                                
                            if(a == 1 and i == sizeY-1 and j == sizeX-1):
                                type = "      : out    CREDIT"                               
                            RID = "D_" + str(i) + "_" + str(j)
                            line = prefix[a] + "credit_out_" + RID + type;
                            file.write(line + "\n")
                            if( a == 2):
                                line = "attribute ram_style of " + "mem_" + "credit_out_" + RID + ": signal is \"block\";"
                                file.write(line + "\n")    
                            
                            
                        
                    if(a == 1):
                        line = "    );"
                        file.write(line + "\n")
                        line = "end " + entity_name + ";"
                        file.write(line + "\n")
                        file.write("\n")
                        file.write("\n")
                                
                
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
                
                # EAST and WEST channels
                for i in range(sizeZ):
                    for j in range(sizeY):
                        RID = "E_" + str(i) + "_" + str(j)
                        line = "            mem_flit_in_" + RID + "(address)" + " <= " + "in_flit_in_" + RID + ";"
                        file.write(line + "\n")
                        RID = "W_" + str(i) + "_" + str(j)
                        line = "            mem_flit_in_" + RID + "(address)" + " <= " + "in_flit_in_" + RID + ";"
                        file.write(line + "\n")
                # SOUTH and NORTH channels
                for i in range(sizeZ):
                    for j in range(sizeX):
                        RID = "S_" + str(i) + "_" + str(j)
                        line = "            mem_flit_in_" + RID + "(address)" + " <= " + "in_flit_in_" + RID + ";"
                        file.write(line + "\n")
                        RID = "N_" + str(i) + "_" + str(j)
                        line = "            mem_flit_in_" + RID + "(address)" + " <= " + "in_flit_in_" + RID + ";"
                        file.write(line + "\n")
                # UP and DOWN channels
                for i in range(sizeY):
                    for j in range(sizeX):
                        RID = "U_" + str(i) + "_" + str(j)
                        line = "            mem_flit_in_" + RID + "(address)" + " <= " + "in_flit_in_" + RID + ";"
                        file.write(line + "\n")
                        RID = "D_" + str(i) + "_" + str(j)
                        line = "            mem_flit_in_" + RID + "(address)" + " <= " + "in_flit_in_" + RID + ";"
                        file.write(line + "\n")
                # EAST and WEST channels
                for i in range(sizeZ):
                    for j in range(sizeY):
                        RID = "E_" + str(i) + "_" + str(j)
                        line = "            mem_flit_out_" + RID + "(address)" + " <= " + "in_flit_out_" + RID + ";"
                        file.write(line + "\n")
                        RID = "W_" + str(i) + "_" + str(j)
                        line = "            mem_flit_out_" + RID + "(address)" + " <= " + "in_flit_out_" + RID + ";"
                        file.write(line + "\n")
                # SOUTH and NORTH channels
                for i in range(sizeZ):
                    for j in range(sizeX):
                        RID = "S_" + str(i) + "_" + str(j)
                        line = "            mem_flit_out_" + RID + "(address)" + " <= " + "in_flit_out_" + RID + ";"
                        file.write(line + "\n")
                        RID = "N_" + str(i) + "_" + str(j)
                        line = "            mem_flit_out_" + RID + "(address)" + " <= " + "in_flit_out_" + RID + ";"
                        file.write(line + "\n")
                # UP and DOWN channels
                for i in range(sizeY):
                    for j in range(sizeX):
                        RID = "U_" + str(i) + "_" + str(j)
                        line = "            mem_flit_out_" + RID + "(address)" + " <= " + "in_flit_out_" + RID + ";"
                        file.write(line + "\n")
                        RID = "D_" + str(i) + "_" + str(j)
                        line = "            mem_flit_out_" + RID + "(address)" + " <= " + "in_flit_out_" + RID + ";"
                        file.write(line + "\n")
                        
                file.write("\n")        
                        
                # EAST and WEST channels
                for i in range(sizeZ):
                    for j in range(sizeY):
                        RID = "E_" + str(i) + "_" + str(j)
                        line = "            mem_credit_in_" + RID + "(address)" + " <= " + "in_credit_in_" + RID + ";"
                        file.write(line + "\n")
                        RID = "W_" + str(i) + "_" + str(j)
                        line = "            mem_credit_in_" + RID + "(address)" + " <= " + "in_credit_in_" + RID + ";"
                        file.write(line + "\n")
                # SOUTH and NORTH channels
                for i in range(sizeZ):
                    for j in range(sizeX):
                        RID = "S_" + str(i) + "_" + str(j)
                        line = "            mem_credit_in_" + RID + "(address)" + " <= " + "in_credit_in_" + RID + ";"
                        file.write(line + "\n")
                        RID = "N_" + str(i) + "_" + str(j)
                        line = "            mem_credit_in_" + RID + "(address)" + " <= " + "in_credit_in_" + RID + ";"
                        file.write(line + "\n")
                # UP and DOWN channels
                for i in range(sizeY):
                    for j in range(sizeX):
                        RID = "U_" + str(i) + "_" + str(j)
                        line = "            mem_credit_in_" + RID + "(address)" + " <= " + "in_credit_in_" + RID + ";"
                        file.write(line + "\n")
                        RID = "D_" + str(i) + "_" + str(j)
                        line = "            mem_credit_in_" + RID + "(address)" + " <= " + "in_credit_in_" + RID + ";"
                        file.write(line + "\n")
                # EAST and WEST channels
                for i in range(sizeZ):
                    for j in range(sizeY):
                        RID = "E_" + str(i) + "_" + str(j)
                        line = "            mem_credit_out_" + RID + "(address)" + " <= " + "in_credit_out_" + RID + ";"
                        file.write(line + "\n")
                        RID = "W_" + str(i) + "_" + str(j)
                        line = "            mem_credit_out_" + RID + "(address)" + " <= " + "in_credit_out_" + RID + ";"
                        file.write(line + "\n")
                # SOUTH and NORTH channels
                for i in range(sizeZ):
                    for j in range(sizeX):
                        RID = "S_" + str(i) + "_" + str(j)
                        line = "            mem_credit_out_" + RID + "(address)" + " <= " + "in_credit_out_" + RID + ";"
                        file.write(line + "\n")
                        RID = "N_" + str(i) + "_" + str(j)
                        line = "            mem_credit_out_" + RID + "(address)" + " <= " + "in_credit_out_" + RID + ";"
                        file.write(line + "\n")
                # UP and DOWN channels
                for i in range(sizeY):
                    for j in range(sizeX):
                        RID = "U_" + str(i) + "_" + str(j)
                        line = "            mem_credit_out_" + RID + "(address)" + " <= " + "in_credit_out_" + RID + ";"
                        file.write(line + "\n")
                        RID = "D_" + str(i) + "_" + str(j)
                        line = "            mem_credit_out_" + RID + "(address)" + " <= " + "in_credit_out_" + RID + ";"
                        file.write(line + "\n")
                             
                        
                            
                
                line = "        end if;"
                file.write(line + "\n")
                file.write("\n")
                
                # Reading part:
                line = "        -- Synchronous reading"
                file.write(line + "\n")
                
                # EAST and WEST channels
                for i in range(sizeZ):
                    for j in range(sizeY):
                        RID = "E_" + str(i) + "_" + str(j)
                        line = "        out_flit_in_" + RID  + " <= " + "mem_flit_in_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                        RID = "W_" + str(i) + "_" + str(j)
                        line = "        out_flit_in_" + RID  + " <= " + "mem_flit_in_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                # SOUTH and NORTH channels
                for i in range(sizeZ):
                    for j in range(sizeX):
                        RID = "S_" + str(i) + "_" + str(j)
                        line = "        out_flit_in_" + RID  + " <= " + "mem_flit_in_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                        RID = "N_" + str(i) + "_" + str(j)
                        line = "        out_flit_in_" + RID  + " <= " + "mem_flit_in_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                # UP and DOWN channels
                for i in range(sizeY):
                    for j in range(sizeX):
                        RID = "U_" + str(i) + "_" + str(j)
                        line = "        out_flit_in_" + RID  + " <= " + "mem_flit_in_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                        RID = "D_" + str(i) + "_" + str(j)
                        line = "        out_flit_in_" + RID  + " <= " + "mem_flit_in_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                # EAST and WEST channels
                for i in range(sizeZ):
                    for j in range(sizeY):
                        RID = "E_" + str(i) + "_" + str(j)
                        line = "        out_flit_out_" + RID  + " <= " + "mem_flit_out_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                        RID = "W_" + str(i) + "_" + str(j)
                        line = "        out_flit_out_" + RID  + " <= " + "mem_flit_out_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                # SOUTH and NORTH channels
                for i in range(sizeZ):
                    for j in range(sizeX):
                        RID = "S_" + str(i) + "_" + str(j)
                        line = "        out_flit_out_" + RID  + " <= " + "mem_flit_out_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                        RID = "N_" + str(i) + "_" + str(j)
                        line = "        out_flit_out_" + RID  + " <= " + "mem_flit_out_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                # UP and DOWN channels
                for i in range(sizeY):
                    for j in range(sizeX):
                        RID = "U_" + str(i) + "_" + str(j)
                        line = "        out_flit_out_" + RID  + " <= " + "mem_flit_out_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                        RID = "D_" + str(i) + "_" + str(j)
                        line = "        out_flit_out_" + RID  + " <= " + "mem_flit_out_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                        
                file.write("\n")        
                        
                # EAST and WEST channels
                for i in range(sizeZ):
                    for j in range(sizeY):
                        RID = "E_" + str(i) + "_" + str(j)
                        line = "        out_credit_in_" + RID  + " <= " + "mem_credit_in_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                        RID = "W_" + str(i) + "_" + str(j)
                        line = "        out_credit_in_" + RID  + " <= " + "mem_credit_in_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                # SOUTH and NORTH channels
                for i in range(sizeZ):
                    for j in range(sizeX):
                        RID = "S_" + str(i) + "_" + str(j)
                        line = "        out_credit_in_" + RID  + " <= " + "mem_credit_in_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                        RID = "N_" + str(i) + "_" + str(j)
                        line = "        out_credit_in_" + RID  + " <= " + "mem_credit_in_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                # UP and DOWN channels
                for i in range(sizeY):
                    for j in range(sizeX):
                        RID = "U_" + str(i) + "_" + str(j)
                        line = "        out_credit_in_" + RID  + " <= " + "mem_credit_in_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                        RID = "D_" + str(i) + "_" + str(j)
                        line = "        out_credit_in_" + RID  + " <= " + "mem_credit_in_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                # EAST and WEST channels
                for i in range(sizeZ):
                    for j in range(sizeY):
                        RID = "E_" + str(i) + "_" + str(j)
                        line = "        out_credit_out_" + RID  + " <= " + "mem_credit_out_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                        RID = "W_" + str(i) + "_" + str(j)
                        line = "        out_credit_out_" + RID  + " <= " + "mem_credit_out_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                # SOUTH and NORTH channels
                for i in range(sizeZ):
                    for j in range(sizeX):
                        RID = "S_" + str(i) + "_" + str(j)
                        line = "        out_credit_out_" + RID  + " <= " + "mem_credit_out_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                        RID = "N_" + str(i) + "_" + str(j)
                        line = "        out_credit_out_" + RID  + " <= " + "mem_credit_out_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                # UP and DOWN channels
                for i in range(sizeY):
                    for j in range(sizeX):
                        RID = "U_" + str(i) + "_" + str(j)
                        line = "        out_credit_out_" + RID  + " <= " + "mem_credit_out_"  + RID + "(address)" + ";"
                        file.write(line + "\n")
                        RID = "D_" + str(i) + "_" + str(j)
                        line = "        out_credit_out_" + RID  + " <= " + "mem_credit_out_"  + RID + "(address)" + ";"
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
                
                
                
                

            