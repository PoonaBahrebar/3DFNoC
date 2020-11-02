----------------------------------------------------------------------------------
--  File:           prng_15bit.vhd
--  Created:        30/06/2020
--  Last Changed:   30/06/2020
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

entity prng_15bit is
    Generic (
        SEED                : STD_LOGIC_VECTOR := "00000000000000000000000000000001"
        );
    Port ( clk              : in    STD_ULOGIC;
           rst              : in    STD_ULOGIC;
           rand             : out   STD_LOGIC_VECTOR(14 downto 0)
           );
end prng_15bit;

architecture Behavioral of prng_15bit is
signal rand_bits            : STD_LOGIC_VECTOR(31 downto 0);
begin

rand <= rand_bits(14 downto 0);

process(clk, rst)
begin
    if rst = '1' then
        rand_bits <= SEED;
    elsif rising_edge(clk) then
        rand_bits <= rand_bits(30 downto 0) & (rand_bits(0) xnor rand_bits(1) xnor rand_bits(21) xnor rand_bits(31) ) ;
    
    end if;
end process;


end Behavioral;
