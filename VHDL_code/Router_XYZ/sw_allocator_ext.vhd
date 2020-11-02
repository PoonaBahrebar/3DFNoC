----------------------------------------------------------------------------------
--  File:           sw_allocator_ext.vhd
--  Created:        26/07/2020
--  Last Changed:   26/07/2020
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

entity sw_allocator_ext is
    Port ( clk          : in    STD_ULOGIC;
           rst          : in    STD_ULOGIC;
           enable       : in    STD_ULOGIC;
           requests     : in    INT_PORT_ARRAY(0 to VCS*PORTS-1);
           grants       : out   STD_ULOGIC_VECTOR(0 to VCS*PORTS-1);
           cb_config    : out   STD_ULOGIC_VECTOR(0 to PORTS*PORTS-1);
           output_ready : out   STD_ULOGIC; 
           
           -- Memory signals
           store        : in    STD_ULOGIC;
           load         : in    STD_ULOGIC;
           -- Inputs
           in_priorities_output : in    INT_ARRAY(0 to PORTS-1);
           in_priorities_input  : in    INT_ARRAY(0 to PORTS-1);
           -- Outputs
           out_priorities_output : out    INT_ARRAY(0 to PORTS-1);
           out_priorities_input  : out    INT_ARRAY(0 to PORTS-1)
           ); 
end sw_allocator_ext;

architecture Behavioral of sw_allocator_ext is
signal internal_requests    :   STD_ULOGIC_VECTOR(0 to PORTS*PORTS-1);
signal internal_grants      :   STD_ULOGIC_VECTOR(0 to PORTS*PORTS-1);

begin

ALLOCATOR:  entity work.allocator_ext(Behavioral)
    Generic map(
        NUM_INPUTS      =>  PORTS,
        NUM_OUTPUTS     =>  PORTS
    )
    Port map(
        clk             =>  clk,
        rst             =>  rst,
        enable          =>  enable,
        states          =>  (others => '0'),
        requests        =>  internal_requests,
        grants          =>  internal_grants,
        output_ready    =>  output_ready,
        
        -- memory signals
        store                   =>  store,
        load                    =>  load,
        in_priorities_output    =>  in_priorities_output,
        in_priorities_input     =>  in_priorities_input,
        out_priorities_output   =>  out_priorities_output,
        out_priorities_input    =>  out_priorities_input       
    );


-- Convert higher level requests to requests for the allocator
process(requests)
begin
    internal_requests <= (others => '0');
    for I in 0 to PORTS-1 loop
        for J in 0 to VCS -1 loop
            -- Combine requests from input units per input port
            if(requests(I*VCS + J) /= PORTS) then
                internal_requests(I*PORTS + requests(I*VCS + J)) <= '1';
            end if; 
        end loop;
    end loop;
end process;
    
    
-- Convert grants from allocator to higher level grants
process(internal_grants)
begin
    cb_config <= internal_grants;
    
    grants <= (others => '0');
    for I in 0 to PORTS-1 loop
        for J in 0 to PORTS-1 loop
            if(internal_grants(I*PORTS + J) = '1') then
                -- input I has acces to output J: find corresponding input unit
                for K in 0 to VCS-1 loop
                    if(requests(I*VCS + K) = J) then
                        grants(I*VCS+K) <= '1';
                    end if;
                end loop;
            end if;
        end loop;
    end loop;

end process;

end Behavioral;
