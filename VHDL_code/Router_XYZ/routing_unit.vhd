----------------------------------------------------------------------------------
--  File:           routing_unit.vhd
--  Created:        11/06/2020
--  Last Changed:   11/06/2020
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

entity routing_unit is
    Port ( clk          : in    STD_ULOGIC;
           rst          : in    STD_ULOGIC;
           enable       : in    STD_ULOGIC;
           pos_x        : in    integer range 0 to RADIX-1;
           pos_y        : in    integer range 0 to RADIX-1;  
           pos_z        : in    integer range 0 to RADIX-1;
           dest_x       : in    integer range 0 to RADIX-1;
           dest_y       : in    integer range 0 to RADIX-1;
           dest_z       : in    integer range 0 to RADIX-1;
           output       : out   integer range 0 to PORTS;
           output_ready : out   STD_ULOGIC
           );
end routing_unit;

architecture Behavioral of routing_unit is
begin

process(clk, rst)
begin
    if (rst = '1') then        
        output  <=  PORTS;
        output_ready <= '0';
    
    elsif (rising_edge(clk)) then
        -- Determine output port according to XYZ routing scheme            
        if (enable = '1') then
            
            -- Routing along x-direction
            if (dest_x > pos_x) then
                output <= 0;
            elsif (dest_x < pos_x) then
                output <= 2;
                
            -- Routing along y-direction
            elsif (dest_y > pos_y) then
                output <= 3;
            elsif (dest_y < pos_y) then
                output <= 1;
                
            -- Routing along z-direction
            elsif (dest_z > pos_z) then
                output <= 4;
            elsif (dest_z < pos_z) then
                output <= 5;
            
            -- Flit has arrived at destination router: send to local port
            else 
                output <= 6;
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
