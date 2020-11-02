----------------------------------------------------------------------------------
--  File:           vc_allocator_ext.vhd
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
----------------------------------------------------------------------------------


library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use work.common.all;

entity vc_allocator_ext is
    Port ( clk          : in    STD_ULOGIC;
           rst          : in    STD_ULOGIC;
           enable       : in    STD_ULOGIC;
           requests     : in    INT_PORT_ARRAY(0 to VCS*PORTS-1);
           free_vc      : in    STD_ULOGIC_VECTOR(0 to VCS*PORTS-1);
           grants       : out   INT_VC_ARRAY(0 to VCS*PORTS-1);
           output_ready : out   STD_ULOGIC; 
           
           -- Memory signals
           store        : in    STD_ULOGIC;
           load         : in    STD_ULOGIC;
           -- Inputs
           in_priorities_output : in    INT_ARRAY(0 to VCS*PORTS-1);
           in_priorities_input  : in    INT_ARRAY(0 to VCS*PORTS-1);
           in_states            : in    STD_ULOGIC_VECTOR(0 to VCS*PORTS-1);
           -- Outputs
           -- Inputs
           out_priorities_output : out    INT_ARRAY(0 to VCS*PORTS-1);
           out_priorities_input  : out    INT_ARRAY(0 to VCS*PORTS-1);
           out_states            : out    STD_ULOGIC_VECTOR(0 to VCS*PORTS-1)
           );   
end vc_allocator_ext;

architecture Behavioral of vc_allocator_ext is
signal states               : STD_ULOGIC_VECTOR(0 to VCS*PORTS-1);
signal internal_requests    : STD_ULOGIC_VECTOR(0 to VCS*PORTS*VCS*PORTS-1); 
signal internal_grants      : STD_ULOGIC_VECTOR(0 to VCS*PORTS*VCS*PORTS-1);

begin

ALLOCATOR:  entity work.allocator_ext(Behavioral)
    Generic map(
        NUM_INPUTS      =>  VCS*PORTS,
        NUM_OUTPUTS     =>  VCS*PORTS
    )
    Port map(
        clk             =>  clk,
        rst             =>  rst,
        enable          =>  enable,
        states          =>  states,
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
    for I in 0 to VCS*PORTS-1 loop
        -- If this input unit requests a VC:
        if(requests(I) /= PORTS) then
            for J in 0 to VCS loop
            -- Set request for every VC at the requested output port high
            internal_requests(I*VCS*PORTS + requests(I)*VCS + J) <= '1';
            end loop;
        end if;
           
    end loop;
end process;


-- Convert grants from allocator to higher level grants
process(internal_grants)
begin
    -- Initialize all grants to 'NO GRANT'
    grants <= (others => VCS);
    for I in 0 to VCS*PORTS-1 loop
        for J in 0 to PORTS -1 loop
            for K in 0 to VCS -1 loop
                if(internal_grants(I*VCS*PORTS + J*VCS + K) = '1') then
                    grants(I) <= K;     
                end if;
            end loop;
        end loop;
    end loop;
end process;


-- State updater
process(clk, rst)
begin
    if rst = '1' then
        states <= (others => '0');
    elsif rising_edge(clk) then
        if enable = '1' then
            for I in 0 to VCS*PORTS -1 loop
                if(free_vc(I) = '1') then
                    states(I) <= '0';
                else
                    for J in 0 to VCS*PORTS-1 loop
                        if(internal_grants(I+J) = '1') then
                            states(I) <= '1';
                        end if;
                    end loop;
                end if;
            end loop;
            
        elsif store = '1' then
            -- Store state into memory
            out_states <= states;
        elsif load = '1' then
            states <= in_states;
        end if;
    end if;
end process;



end Behavioral;
