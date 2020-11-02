----------------------------------------------------------------------------------
--  File:           arbiter.vhd
--  Created:        11/06/2020
--  Last Changed:   20/06/2020 
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


entity arbiter is
    Generic (
        NUM_REQS : integer := VCS*PORTS
        );
    Port ( clk          : in    STD_ULOGIC;
           rst          : in    STD_ULOGIC;
           enable       : in    STD_ULOGIC;
           state        : in    STD_ULOGIC;                 -- TODO state might have to be changed to internal variable
           priority     : in    integer range 0 to NUM_REQS-1;
           requests     : in    STD_ULOGIC_VECTOR(0 to NUM_REQS-1);
           grants       : out   STD_ULOGIC_VECTOR(0 to NUM_REQS-1)
           );
end arbiter;

architecture Behavioral of arbiter is
begin

process(clk, rst)
variable granted : STD_LOGIC;
variable index : integer range 0 to NUM_REQS-1;
begin
    if (rst = '1') then
        grants <= (others => '0');
        
    elsif (rising_edge(clk)) then
        
        -- If resource is already in use, it can not be allocated again
        if(state = '1' or enable = '0') then
            grants <= (others => '0');
            
        -- Resource available: grant access to one requester
        else
            index := priority;
            granted := '0';
            for I in 0 to NUM_REQS -1 loop
            -- Grant access to first requester
            if (requests(index) = '1') and granted = '0' then
                granted := '1';
                grants(index) <= '1';
            else
                grants(index) <= '0';
            end if;

            
            -- Update index
            if(index = NUM_REQS-1) then
                index := 0;
            else
                index := index + 1;
            end if;
            
            end loop;
        end if;
    end if;
end process;


end Behavioral;
