----------------------------------------------------------------------------------
--  File:           crossbar.vhd
--  Created:        12/06/2020
--  Last Changed:   14/07/2020
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
-- 
----------------------------------------------------------------------------------


library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use work.common.all;

entity crossbar is
    Port ( clk          : in    STD_ULOGIC;
           rst          : in    STD_ULOGIC;
           enable       : in    STD_ULOGIC;
           config       : in    STD_ULOGIC_VECTOR(0 to PORTS*PORTS-1);
           inputs       : in    FLIT_ARRAY(0 to PORTS-1);
           outputs      : out   FLIT_ARRAY(0 to PORTS-1) 
           );   
end crossbar;

architecture Behavioral of crossbar is

begin

process(clk, rst)
begin
    if rst = '1' then
        outputs <= (others => (others => '0'));
    elsif rising_edge(clk) and enable = '1' then
        outputs <= (others => (others => '0'));
        for I in 0 to PORTS-1 loop
            for J in 0 to PORTS-1 loop
                if(config(I*PORTS + J) = '1') then
                    outputs(J) <= inputs(I);
                end if;
            end loop;
        end loop;
    
    end if;
end process;


end Behavioral;
