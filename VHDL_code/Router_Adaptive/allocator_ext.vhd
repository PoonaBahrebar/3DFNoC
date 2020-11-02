----------------------------------------------------------------------------------
--  File:           allocator_ext.vhd
--  Created:        26/07/2020
--  Last Changed:   27/07/2020
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

entity allocator_ext is
    Generic (
        NUM_INPUTS:     integer := VCS*PORTS;
        NUM_OUTPUTS :   integer := VCS*PORTS
        );
    Port ( clk          : in    STD_ULOGIC;
           rst          : in    STD_ULOGIC;
           enable       : in    STD_ULOGIC;
           states       : in    STD_ULOGIC_VECTOR(0 to NUM_OUTPUTS -1);
           requests     : in    STD_ULOGIC_VECTOR(0 to NUM_INPUTS*NUM_OUTPUTS - 1);
           grants       : out   STD_ULOGIC_VECTOR(0 to NUM_INPUTS*NUM_OUTPUTS - 1);
           output_ready : out   STD_ULOGIC; 
           
           -- Memory signals
           store        : in    STD_ULOGIC;
           load         : in    STD_ULOGIC;
           -- Inputs
           in_priorities_output : in    INT_ARRAY(0 to NUM_OUTPUTS-1);
           in_priorities_input  : in    INT_ARRAY(0 to NUM_OUTPUTS-1);
           -- Outputs
           -- Inputs
           out_priorities_output : out    INT_ARRAY(0 to NUM_OUTPUTS-1);
           out_priorities_input  : out    INT_ARRAY(0 to NUM_OUTPUTS-1)
           );   
end allocator_ext;

architecture Behavioral of allocator_ext is
signal priorities_output        :       INT_ARRAY(0 to NUM_OUTPUTS -1);  
signal priorities_input         :       INT_ARRAY(0 to NUM_INPUTS -1);                                    
signal reorganized_requests     :       STD_ULOGIC_VECTOR(0 to NUM_INPUTS*NUM_OUTPUTS-1);       
signal intermediate_grants      :       STD_ULOGIC_VECTOR(0 to NUM_INPUTS*NUM_OUTPUTS-1);
signal intermediate_requests    :       STD_ULOGIC_VECTOR(0 to NUM_INPUTS*NUM_OUTPUTS-1);
signal end_grants               :       STD_ULOGIC_VECTOR(0 to NUM_INPUTS*NUM_OUTPUTS-1);

signal clk2 : STD_ULOGIC;

begin

-- Create output arbiters
OUTPUT_ARBITERS:
    for I in 0 to NUM_OUTPUTS-1 generate
    ARBITER: entity work.arbiter(Behavioral)
        Generic map(
            NUM_REQS  =>  NUM_INPUTS
        )
        Port map(
            clk         =>  clk,
            rst         =>  rst,
            enable      =>  enable,
            state       =>  states(I),                      -- TODO state might have to be changed to internal variable
            priority    =>  priorities_output(I),              
            requests    =>  reorganized_requests(0 + I*NUM_INPUTS to (I+1) * NUM_INPUTS -1 ),        
            grants      =>  intermediate_grants(0 + I*NUM_INPUTS to (I+1) * NUM_INPUTS -1 )         
        );
    end generate;
    
    
-- Create input arbiters
INPUT_ARBITERS:
    for I in 0 to NUM_INPUTS-1 generate
    ARBITER: entity work.arbiter(Behavioral)
        Generic map(
            NUM_REQS  =>  NUM_OUTPUTS
        )
        Port map( 
            clk         =>  clk2,                    
            rst         =>  rst,
            enable      =>  enable,
            state       =>  '0',                    
            priority    =>  priorities_input(I),
            requests    =>  intermediate_requests(0 + I*NUM_OUTPUTS to (I+1) * NUM_OUTPUTS -1),
            grants      =>  end_grants(0 + I*NUM_OUTPUTS to (I+1) * NUM_OUTPUTS -1) 
        );
    end generate;
    
    
-- Connecting signals
process(requests, intermediate_grants)
begin
    for I in 0 to NUM_INPUTS -1 loop
        for J in 0 to NUM_OUTPUTS -1 loop
            -- Reorganize requetss
            reorganized_requests(I + J*NUM_INPUTS) <= requests(J + I*NUM_OUTPUTS);
            
            -- Connect grants of output arbiters with requests of input arbiters
            intermediate_requests(I + J*NUM_INPUTS) <= intermediate_grants(J + I*NUM_OUTPUTS);
        end loop;
    end loop;

end process;

-- Connect grants
process(end_grants)
begin
    grants <= end_grants;
end process;


process(clk)
begin
    clk2 <= not(clk);
end process;

--Update priority for each end grant
process(clk)
begin
    if rising_edge(clk) then            
        if store = '1' then
            for I in 0 to NUM_INPUTS-1 loop
                for J in 0 to NUM_OUTPUTS-1 loop
                    -- Update priority at input arbiters
                    if(end_grants(J + I*NUM_OUTPUTS) = '1') then
                        if(J = NUM_OUTPUTS -1) then
                            out_priorities_input(I) <= 0;
                        else                       
                        out_priorities_input(I) <= J + 1;
                        end if;
                        
                        -- Update priority at output arbiters
                        if(I = NUM_INPUTS-1) then
                            out_priorities_output(J) <= 0;
                        else
                            out_priorities_output(J) <= I + 1;
                        end if;
                    else
                        out_priorities_input(I) <= in_priorities_input(I);
                        out_priorities_output(J) <= in_priorities_output(J);
                    end if;
                    
                end loop;
            end loop;
        elsif load = '1' then
            priorities_input <= in_priorities_input;
            priorities_output <= in_priorities_output;
        end if;
    end if;
end process;


end Behavioral;
