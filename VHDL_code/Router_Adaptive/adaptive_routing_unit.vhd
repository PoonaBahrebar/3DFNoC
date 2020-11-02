----------------------------------------------------------------------------------
--  File:           adaptive_routing_unit.vhd
--  Created:        30/07/2020
--  Last Changed:   25/08/2020
--  Author:         Jonathan D'Hoore
--                  University of Ghent
--
--  Part of Master's dissertation submitted in order to obtain the academic degree of
--  Master of Science in Electrical Engineering - main subject Electronic Circuits and Systems
--  Academic year 2019-2020
-- 
--
-- If you use our 3D NoC Emulator in your research, we would appreciate the following citation in any publications to which it has contributed:
-- Jonathan D'Hoore, Poona Bahrebar and Dirk Stroobandt, "3D NoC Emulation Model on a Single FPGA," 
-- In Proceedings of ACM/IEEE International Workshop on System-Level Interconnect Problems and Pathfinding (SLIPP'20), pp. 1-8, 2020.
----------------------------------------------------------------------------------

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use work.common.all;

entity adaptive_routing_unit is
    Port ( clk              : in    STD_ULOGIC;
           rst              : in    STD_ULOGIC;
           enable           : in    STD_ULOGIC;
           pos_x            : in    integer range 0 to RADIX-1;
           pos_y            : in    integer range 0 to RADIX-1;  
           pos_z            : in    integer range 0 to RADIX-1;    
           dest_x           : in    integer range 0 to RADIX-1;
           dest_y           : in    integer range 0 to RADIX-1;
           dest_z           : in    integer range 0 to RADIX-1;
           output_vc_states : in    STD_ULOGIC_VECTOR(0 to PORTS*VCS-1);  
           output           : out   integer range 0 to PORTS;
           output_ready     : out   STD_ULOGIC
           );
end adaptive_routing_unit;

architecture Behavioral of adaptive_routing_unit is

-- Procedure used for creating Hamiltonian label
procedure HamiltonianLabelS(signal   x   : in integer range 0 to RADIX -1;
                        signal   y   : in integer range 0 to RADIX -1;
                        signal   z   : in integer range 0 to RADIX -1;
                        variable   ham   : out integer range 0 to RADIX -1) is
begin
    if(z mod 2 = 0) then
        if(y mod 2 = 0) then
            -- z even and y even
            ham := RADIX*RADIX*z + RADIX*y + x + 1;
        else
            -- z even and y odd
            ham := RADIX*RADIX*z + RADIX*y + RADIX - x;        
        end if;
    else
        if(y mod 2 = 0) then
            -- z odd and y even
            ham := RADIX*RADIX*z + RADIX* (RADIX-y-1) + RADIX - x;
        else
            -- z odd and y odd
            ham := RADIX*RADIX*z + RADIX*(RADIX-y-1) + x + 1;
        end if;
    
    end if;
end procedure;

-- Procedure used for creating Hamiltonian label
procedure HamiltonianLabelV(variable   x   : in integer range 0 to RADIX -1;
                        variable   y   : in integer range 0 to RADIX -1;
                        variable   z   : in integer range 0 to RADIX -1;
                        variable   ham   : out integer range 0 to RADIX -1) is
begin
    if(z mod 2 = 0) then
        if(y mod 2 = 0) then
            -- z even and y even
            ham := RADIX*RADIX*z + RADIX*y + x + 1;
        else
            -- z even and y odd
            ham := RADIX*RADIX*z + RADIX*y + RADIX - x;        
        end if;
    else
        if(y mod 2 = 0) then
            -- z odd and y even
            ham := RADIX*RADIX*z + RADIX* (RADIX-y-1) + RADIX - x;
        else
            -- z odd and y odd
            ham := RADIX*RADIX*z + RADIX*(RADIX-y-1) + x + 1;
        end if;
    
    end if;
end procedure;

procedure onHamiltonianPath(variable   x   : in integer range 0 to RADIX -1;
                        variable   y   : in integer range 0 to RADIX -1;
                        variable   z   : in integer range 0 to RADIX -1;
                        variable   onPath   : out boolean) is
variable pos_label : integer range 0 to RADIX*RADIX*RADIX-1;
variable cand_label : integer range 0 to RADIX*RADIX*RADIX-1;
variable dest_label : integer range 0 to RADIX*RADIX*RADIX-1;
begin
    -- Determine labels
    HamiltonianLabelS(pos_x, pos_y, pos_z, pos_label);
    HamiltonianLabelV(x, y, z, cand_label);
    HamiltonianLabelS(dest_x, dest_y, dest_z, dest_label);
    
    -- Check if candidate is on Hamiltonian path
    if(pos_label < dest_label) then
        if(cand_label <= dest_label and cand_label > pos_label) then
            onPath := true;
        else
            onPath := false;
        end if;
    else
        if(cand_label >= dest_label and cand_label < pos_label) then
            onPath := true;
        else
            onPath := false;
        end if;
    end if;
    
    

end procedure;



begin

process(clk, rst)
variable minLoad : integer range 0 to VCS + 1 := VCS+1;
variable load   : integer range 0 to VCS := 0;
variable onPath : boolean := false;
variable x,y,z : integer range 0 to RADIX-1;
begin
    if rst = '1' then
        output <= PORTS;
        output_ready <= '0';
    elsif rising_edge(clk) then
        if (enable = '1') then
                    
            -- Flit has arrived at destination router: send to local port
            if (dest_x = pos_x) and (dest_y = pos_y) and (dest_z = pos_z) then 
                output <= 6;
            else       
                minLoad := VCS +1;
                
                if (dest_x > pos_x) then
                    -- Check if on Hamiltonian path
                    x := pos_x + 1;
                    y := pos_y;
                    z := pos_z;
                    onHamiltonianPath(x, y, z, onPath);
                    if(onPath) then
                        load := 0;
                        for I in 0 to VCS-1 loop
                            if(output_vc_states(0*VCS + I) = '1') then
                                load := load + 1;
                            end if;
                        end loop;
                        if(load < minLoad) then
                            minLoad := load;
                            output <= 0;
                        end if;
                    end if;
                    
                elsif (dest_x < pos_x) then
                    -- Check if on Hamiltonian path
                    x := pos_x - 1;
                    y := pos_y;
                    z := pos_z;
                    onHamiltonianPath(x, y, z, onPath);
                    if(onPath) then
                        load := 0;
                        for I in 0 to VCS-1 loop
                            if(output_vc_states(2*VCS + I) = '1') then
                                load := load + 1;
                            end if;
                        end loop;
                        if(load < minLoad) then
                            minLoad := load;
                            output <= 2;
                        end if;
                    end if;
                end if;
                    
                -- Routing along y-direction
                if (dest_y > pos_y) then
                    -- Check if on Hamiltonian path
                    x := pos_x;
                    y := pos_y+1;
                    z := pos_z;
                    onHamiltonianPath(x, y, z, onPath);
                    if(onPath) then
                        load := 0;
                        for I in 0 to VCS-1 loop
                            if(output_vc_states(3*VCS + I) = '1') then
                                load := load + 1;
                            end if;
                        end loop;
                        if(load < minLoad) then
                            minLoad := load;
                            output <= 3;
                        end if;
                    end if;
                elsif (dest_y < pos_y) then
                    -- Check if on Hamiltonian path
                    x := pos_x;
                    y := pos_y-1;
                    z := pos_z;
                    onHamiltonianPath(x, y, z, onPath);
                    if(onPath) then
                        load := 0;
                        for I in 0 to VCS-1 loop
                            if(output_vc_states(1*VCS + I) = '1') then
                                load := load + 1;
                            end if;
                        end loop;
                        if(load < minLoad) then
                            minLoad := load;
                            output <= 1;
                        end if;
                    end if;
                end if;
                    
                -- Routing along z-direction
                if (dest_z > pos_z) then
                    -- Check if on Hamiltonian path
                    x := pos_x;
                    y := pos_y;
                    z := pos_z+1;
                    onHamiltonianPath(x, y, z, onPath);
                    if(onPath) then
                        load := 0;
                        for I in 0 to VCS-1 loop
                            if(output_vc_states(4*VCS + I) = '1') then
                                load := load + 1;
                            end if;
                        end loop;
                        if(load < minLoad) then
                            minLoad := load;
                            output <= 4;
                        end if;
                    end if;
                elsif (dest_z < pos_z) then
                    -- Check if on Hamiltonian path
                    x := pos_x;
                    y := pos_y;
                    z := pos_z-1;
                    onHamiltonianPath(x, y, z, onPath);
                    if(onPath) then
                        load := 0;
                        for I in 0 to VCS-1 loop
                            if(output_vc_states(5*VCS + I) = '1') then
                                load := load + 1;
                            end if;
                        end loop;
                        if(load < minLoad) then
                            minLoad := load;
                            output <= 5;
                        end if;
                    end if;
                end if;
            end if;          
            
            -- Output is available in next cycle
            output_ready <= '1';
        
        else
            -- No routing needed: output not available
            output_ready <= '0';
        end if;    
    
    
    end if;

end process;


end Behavioral;
