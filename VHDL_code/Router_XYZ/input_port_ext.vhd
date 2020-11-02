----------------------------------------------------------------------------------
--  File:           input_port_ext.vhd
--  Created:        26/07/2020
--  Last Changed:   08/08/2020
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
use IEEE.numeric_std.ALL;
use work.common.all;

entity input_port_ext is
    Port ( clk              : in    STD_ULOGIC;
           rst              : in    STD_ULOGIC;
           enable           : in    STD_ULOGIC;
           config           : in    STD_ULOGIC;
           input_flit       : in    FLIT;
           grants_vc        : in    INT_VC_ARRAY(0 to VCS-1);
           grants_sw        : in    STD_ULOGIC_VECTOR(0 to VCS-1);
           credit_counters  : in    INT_CC_ARRAY(0 to PORTS*VCS-1);
           output_credit    : out   CREDIT;
           reqs_vc          : out   INT_PORT_ARRAY(0 to VCS-1);
           reqs_sw          : out   INT_PORT_ARRAY(0 to VCS-1);
           allocated_ports  : out   INT_PORT_ARRAY(0 to VCS-1);
           allocated_vcs    : out   INT_VC_ARRAY(0 to VCS-1);
           free_vcs         : out   STD_ULOGIC_VECTOR(0 to VCS-1);
           
           -- Position of this router
           pos_x            : in    integer range 0 to RADIX-1;
           pos_y            : in    integer range 0 to RADIX-1;  
           pos_z            : in    integer range 0 to RADIX-1;
           
           -- MEMORY SIGNALS
           store            : in    STD_ULOGIC;
           load             : in    STD_ULOGIC;
           -- INPUTS:            
           -- Global state for each input unit
           in_states        : in    global_state_array(0 to VCS-1);
           -- Allocated VC for each input unit
           in_alloc_vcs     : in    INT_VC_ARRAY(0 to VCS-1);
           -- Allocated port for each input unit
           in_alloc_ports   : in    INT_PORT_ARRAY(0 to VCS-1);
           -- Buffer for each input unit (each buffer is split in seperate flits)
           in_buf_array     : in    FLIT_ARRAY_t(0 to VCS-1);
           --OUTPUTS:            
           -- Global state for each input unit
           out_states        : out    global_state_array(0 to VCS-1);
           -- Buffer for each input unit 
           out_buf_array     : out    FLIT_ARRAY_t(0 to VCS-1)
           );
end input_port_ext;

architecture Behavioral of input_port_ext is
type global_state_array is array(integer range <>) of global_state;

signal input_flits      : FLIT_ARRAY(0 to VCS-1);
signal output_flits     : FLIT_ARRAY(0 to VCS-1);
signal output_credits   : CREDIT_ARRAY(0 to VCS-1);

begin

INPUT_UNITS:
for I in 0 to VCS-1 generate
    INPUT_UNIT: entity work.input_unit_ext(Behavioral)
        Port map(
            clk             =>      clk,
            rst             =>      rst,
            enable          =>      enable,
            config          =>      config,
            input_flit      =>      input_flits(I),
            grant_vc        =>      grants_vc(I),
            grant_sw        =>      grants_sw(I),
            credit_counters =>      credit_counters,
            req_vc          =>      reqs_vc(I),
            req_sw          =>      reqs_sw(I),
            port_allocated  =>      allocated_ports(I),
            vc_allocated    =>      allocated_vcs(I),
            free_vc         =>      free_vcs(I),
            credit          =>      output_credits(I),
            
            pos_x           =>      pos_x,
            pos_y           =>      pos_y,
            pos_z           =>      pos_z,
            
            -- memory signals
            store           =>      store,
            load            =>      load,
            in_state        =>      in_states(I),
            in_alloc_vc     =>      in_alloc_vcs(I),
            in_alloc_port   =>      in_alloc_ports(I),
            in_buf          =>      in_buf_array(I),
            out_state       =>      out_states(I),
            out_buf         =>      out_buf_array(I)
        );
end generate;


-- Send input flit to corresponding input unit
process(input_flit)
begin
    input_flits <= (others => (others => '0'));
    if(input_flit(0) = '1') then
        input_flits(to_integer(unsigned(input_flit(VC_BITS + 2 downto 3)))) <= input_flit;
    end if;
end process;

-- Set credit to input port
-- NOTE: assumes that only one input unit at a time will try to send credit
process(rst, output_credits)
begin
    if rst = '1' then
        output_credit <= (others => '0');
    else
        for I in 0 to VCS - 1 loop
            if(output_credits(I)(0) = '1') then 
                output_credit(VC_BITS downto 1) <= std_logic_vector(to_unsigned(I, VC_BITS));
                output_credit(0) <= output_credits(I)(0);
            end if;
        end loop;
    end if;

end process;

end Behavioral;
