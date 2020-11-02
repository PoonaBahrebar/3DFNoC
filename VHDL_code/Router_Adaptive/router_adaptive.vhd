----------------------------------------------------------------------------------
--  File:           router_adaptive.vhd
--  Created:        30/07/2020
--  Last Changed:   10/08/2020
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
USE ieee.numeric_std.ALL;
use work.common.all;

entity router_adaptive is
    Port ( clk              : in    STD_ULOGIC;
           rst              : in    STD_ULOGIC;
           enable           : in    STD_ULOGIC;
           config           : in    STD_ULOGIC;
           input_flits      : in    FLIT_ARRAY(0 to PORTS-1);
           input_credits    : in    CREDIT_ARRAY(0 to PORTS-1);
           output_flits     : out   FLIT_ARRAY(0 to PORTS-1);
           output_credits   : out   CREDIT_ARRAY(0 to PORTS-1);
           
           -- Position of this router
           pos_x            : in    integer range 0 to RADIX-1;
           pos_y            : in    integer range 0 to RADIX-1;  
           pos_z            : in    integer range 0 to RADIX-1;
           
           -- memory signals 
           store            : in    STD_ULOGIC;
           load             : in    STD_ULOGIC;
           -- INPUTS:            
           -- Global state for each input unit
           in_states            : in    global_state_array(0 to PORTS*VCS-1);
           
           -- Allocated VC for each input unit
           in_alloc_vcs         : in    INT_VC_ARRAY(0 to PORTS*VCS-1);
           
           -- Allocated port for each input unit
           in_alloc_ports         : in    INT_PORT_ARRAY(0 to PORTS*VCS-1);
                      
           -- Buffer for each input unit (each buffer is split in seperate flits)
           in_buf_array_P0  : in    FLIT_ARRAY_t(0 to VCS-1);
           in_buf_array_P1  : in    FLIT_ARRAY_t(0 to VCS-1);
           in_buf_array_P2  : in    FLIT_ARRAY_t(0 to VCS-1);
           in_buf_array_P3  : in    FLIT_ARRAY_t(0 to VCS-1);
           in_buf_array_P4  : in    FLIT_ARRAY_t(0 to VCS-1);
           in_buf_array_P5  : in    FLIT_ARRAY_t(0 to VCS-1);
           in_buf_array_P6  : in    FLIT_ARRAY_t(0 to VCS-1);  
           
           -- Priorities of VC Allocator output and input arbiters
           in_vc_out_prio_array     : in    INT_ARRAY(0 to PORTS*VCS-1);
           in_vc_in_prio_array      : in    INT_ARRAY(0 to PORTS*VCS-1);
           -- States of output VC (kept in VC allocator)
           in_output_vc_states      : in    STD_ULOGIC_VECTOR(0 to PORTS*VCS-1);     
           
           -- Priorities of Switch Allocator output and input Arbiters
           in_sw_out_prio_array :   in  INT_ARRAY(0 to PORTS-1);
           in_sw_in_prio_array  :   in  INT_ARRAY(0 to PORTS-1);
           
           -- Crossbar
           in_cb_config         :   in  STD_ULOGIC_VECTOR(0 to PORTS*PORTS-1);
           in_cb_inputs         :   in    FLIT_ARRAY(0 to PORTS-1);
           
           -- Credit counter for each output VC
           in_credits           :   in  INT_CC_ARRAY(0 to PORTS*VCS-1);
           
-- OUTPUTS :            
           -- Global state for each input unit
           out_states            : out    global_state_array(0 to PORTS*VCS-1);
           
           -- Allocated VC for each input unit
           out_alloc_vcs         : out    INT_VC_ARRAY(0 to PORTS*VCS-1);
           
           -- Allocated port for each input unit
           out_alloc_ports         : out    INT_PORT_ARRAY(0 to PORTS*VCS-1);
           
           -- Buffer for each input unit (each buffer is split in seperate flits)
           out_buf_array_P0  : out    FLIT_ARRAY_t(0 to VCS-1);
           out_buf_array_P1  : out    FLIT_ARRAY_t(0 to VCS-1);
           out_buf_array_P2  : out    FLIT_ARRAY_t(0 to VCS-1);
           out_buf_array_P3  : out    FLIT_ARRAY_t(0 to VCS-1);
           out_buf_array_P4  : out    FLIT_ARRAY_t(0 to VCS-1);
           out_buf_array_P5  : out    FLIT_ARRAY_t(0 to VCS-1);
           out_buf_array_P6  : out    FLIT_ARRAY_t(0 to VCS-1);
           
           -- Priorities of VC Allocator output and input arbiters
           out_vc_out_prio_array     : out    INT_ARRAY(0 to PORTS*VCS-1);
           out_vc_in_prio_array      : out    INT_ARRAY(0 to PORTS*VCS-1);
           
           -- States of output VC (kept in VC allocator)
           out_output_vc_states      : out    STD_ULOGIC_VECTOR(0 to PORTS*VCS-1);     
           
           -- Priorities of Switch Allocator output and input Arbiters
           out_sw_out_prio_array :   out  INT_ARRAY(0 to PORTS-1);
           out_sw_in_prio_array  :   out  INT_ARRAY(0 to PORTS-1);
           
           -- Crossbar
           out_cb_config         :   out  STD_ULOGIC_VECTOR(0 to PORTS*PORTS-1);
           out_cb_inputs         :   out  FLIT_ARRAY(0 to PORTS-1);
           
           -- Credit counter for each output VC
           out_credits           :   out  INT_CC_ARRAY(0 to PORTS*VCS-1)    
           );
end router_adaptive;

architecture Behavioral of router_adaptive is
-- signals for interfacing memory signals to correct port
type gs_array_t         is array (integer range <>) of global_state_array(0 to VCS-1);
type INT_VC_ARRAY_t     is array (integer range <>) of INT_VC_ARRAY(0 to VCS-1);
type INT_PORT_ARRAY_t   is array (integer range <>) of INT_PORT_ARRAY(0 to VCS-1);
type FLIT_ARRAY_t2      is array (integer range <>) of FLIT_ARRAY_t(0 to VCS-1);
signal in_states_t      : gs_array_t(0 to PORTS-1);
signal in_alloc_vcs_t   : INT_VC_ARRAY_t(0 to PORTS -1);
signal in_alloc_ports_t : INT_PORT_ARRAY_t(0 to PORTS-1);
signal out_states_t      : gs_array_t(0 to PORTS-1);
signal in_vc_in_prios    : INT_ARRAY(0 to VCS*PORTS-1);
signal in_vc_out_prios   : INT_ARRAY(0 to VCS*PORTS-1);
signal in_sw_in_prios    : INT_ARRAY(0 to PORTS-1);
signal in_sw_out_prios   : INT_ARRAY(0 to PORTS-1);
signal out_vc_in_prios   : INT_ARRAY(0 to VCS*PORTS-1);
signal out_vc_out_prios  : INT_ARRAY(0 to VCS*PORTS-1);
signal out_sw_in_prios   : INT_ARRAY(0 to PORTS-1);
signal out_sw_out_prios  : INT_ARRAY(0 to PORTS-1);

signal in_buf_arrays : FLIT_ARRAY_t2(0 to PORTS-1);
signal out_buf_arrays : FLIT_ARRAY_t2(0 to PORTS-1);
signal temp_out_buf_arrays : FLIT_ARRAY_t2(0 to PORTS-1);

-- Request and grant signals
signal vc_requests      :   INT_PORT_ARRAY(0 to VCS*PORTS-1);
signal vc_grants        :   INT_VC_ARRAY(0 to VCS*PORTS-1);
signal sw_requests      :   INT_PORT_ARRAY(0 to VCS*PORTS-1);
signal sw_grants        :   STD_ULOGIC_VECTOR(0 to VCS*PORTS-1);

-- Flits from input ports
signal input_port_flits : FLIT_ARRAY(0 to PORTS-1);

-- Crossbar
signal cb_inputs        :   FLIT_ARRAY(0 to PORTS-1);
signal sw_out_cb_config :   STD_ULOGIC_VECTOR(0 to PORTS*PORTS-1);
signal cb_config        :   STD_ULOGIC_VECTOR(0 to PORTS*PORTS-1);

-- enable signals
signal vc_enable        :   STD_ULOGIC;
signal sw_enable        :   STD_ULOGIC;
signal cb_enable        :   STD_ULOGIC;

-- output ready signals
signal vc_output_ready  :   STD_ULOGIC;
signal sw_output_ready  :   STD_ULOGIC;

-- VC ALLOCATOR free states
signal free_vc          :   STD_ULOGIC_VECTOR(0 to VCS*PORTS-1);

-- Intermediate signal used to convert free_vcs from input port to free_vc for VC ALLOCATOR
type arr_P_array is array(integer range <>) of INT_PORT_ARRAY(0 to VCS-1);
type arr_VC_array is array(integer range <>) of INT_VC_ARRAY(0 to VCS-1);
type arr_free_vc_arr is array(integer range <>) of STD_ULOGIC_VECTOR(0 to VCS-1);
signal all_vcs_array         :     arr_VC_array(0 to PORTS-1);
signal all_ports_array       :     arr_P_array(0 to PORTS-1);
signal free_vcs_array        :     arr_free_vc_arr(0 to PORTS -1);

-- Switch allocator: credits and states
signal credit_counters        : INT_CC_ARRAY(0 to PORTS*VCS-1);

-- clk counter
signal counter          : integer range 0 to 1;


begin

INPUT_PORTS:
for I in 0 to PORTS-1 generate
    INPUT_PORT: entity work.input_port_adaptive(Behavioral)
        Port map(
            clk             =>      clk,
            rst             =>      rst,
            enable          =>      enable,
            config          =>      config,
            input_flit      =>      input_flits(I),
            grants_vc       =>      vc_grants(I*VCS to (I+1)*VCS-1),
            grants_sw       =>      sw_grants(I*VCS to (I+1)*VCS-1),
            credit_counters =>      credit_counters,
            output_credit   =>      output_credits(I),
            reqs_vc         =>      vc_requests(I*VCS to (I+1)*VCS-1),
            reqs_sw         =>      sw_requests(I*VCS to (I+1)*VCS-1),
            allocated_ports =>      all_ports_array(I),
            allocated_vcs   =>      all_vcs_array(I),
            free_vcs        =>      free_vcs_array(I),
            
            pos_x           =>      pos_x,
            pos_y           =>      pos_y,
            pos_z           =>      pos_z,
            
            output_vc_states =>     in_output_vc_states,
            
            -- Memory signals
            store           =>      store,
            load            =>      load,
            in_states       =>      in_states_t(I),
            in_alloc_vcs    =>      in_alloc_vcs_t(I),
            in_alloc_ports  =>      in_alloc_ports_t(I),
            in_buf_array    =>      in_buf_arrays(I),      
            out_states       =>      out_states_t(I),
            out_buf_array    =>      temp_out_buf_arrays(I)        
        );
end generate;


VC_ALLOCATOR: entity work.vc_allocator_ext(Behavioral)
    Port map(
        clk                 =>      clk,
        rst                 =>      rst,
        enable              =>      vc_enable,
        requests            =>      vc_requests,
        free_vc             =>      free_vc,
        grants              =>      vc_grants,
        output_ready        =>      vc_output_ready,
        
        -- memory signals
        store                   =>  store,
        load                    =>  load,
        in_priorities_output    =>  in_vc_out_prios,
        in_priorities_input     =>  in_vc_in_prios,
        in_states               =>  in_output_vc_states,
        out_priorities_output   =>  out_vc_out_prios,
        out_priorities_input    =>  out_vc_in_prios,
        out_states              =>  out_output_vc_states
    );

SWITCH_ALLOCATOR: entity work.sw_allocator_ext(Behavioral)
    Port map(
        clk                 =>      clk,
        rst                 =>      rst,
        enable              =>      sw_enable,
        requests            =>      sw_requests,
        grants              =>      sw_grants,
        cb_config           =>      sw_out_cb_config,
        output_ready        =>      sw_output_ready,
        
        -- memory signals
        store                   =>  store,
        load                    =>  load,
        in_priorities_output    =>  in_sw_out_prios,
        in_priorities_input     =>  in_sw_in_prios,
        out_priorities_output   =>  out_sw_out_prios,
        out_priorities_input    =>  out_sw_in_prios
    );
    
CROSSBAR: entity work.crossbar(Behavioral)
    Port map( 
        clk                 =>      clk,
        rst                 =>      rst,
        enable              =>      cb_enable,
        config              =>      cb_config,
        inputs              =>      cb_inputs,
        outputs             =>      output_flits
    );  
    
    
    
-- Main control process
process(clk, rst)
variable index : integer range 0 to VCS*PORTS;
begin
    if rst = '1' then
        sw_enable <= '0';
        vc_enable <= '0';
        cb_enable <= '0';
        credit_counters <= (others => BUFFER_SIZE);
        counter <= 0;
        cb_inputs <= (others => (others => '0'));
    elsif rising_edge(clk) then
        -- Simulation cycle split in 3 FPGA cycles
        if enable = '1' then
            if(counter = 0) then
                vc_enable <= '0';
                sw_enable <= '0';
                cb_enable <= '0';
                
                -- Receive credits
                for I in 0 to PORTS-1 loop
                    if(input_credits(I)(0) = '1') then
                        -- Update credit count for this VC
                         credit_counters(I*VCS + to_integer(unsigned(input_credits(I)(VC_BITS downto 1)))) <= credit_counters(I*VCS + to_integer(unsigned(input_credits(I)(VC_BITS downto 1)))) +1;        
                    end if;
                end loop;
                counter <= 1;
            else
                vc_enable <= '0';
                sw_enable <= '0';
                cb_enable <= '0';
                
                
                -- Update credit count and buffers based on grants of switch
                out_cb_inputs <= (others => (others => '0'));
                out_buf_arrays <= temp_out_buf_arrays;
                for I in 0 to PORTS-1 loop
                    for J in 0 to VCS-1 loop
                        if(sw_grants(I*VCS + J) = '1') then
                            index := all_ports_array(I)(J)*VCS + all_vcs_array(I)(J);
                            credit_counters(index) <= credit_counters(index) -1;
                            
                            -- Update crossbar inputs
                            out_cb_inputs(I) <= temp_out_buf_arrays(I)(J)(0);
                            
                            -- Remove flit from buffer
                            for K in 0 to BUFFER_SIZE-1 loop
                                if(K /= BUFFER_SIZE-1) then
                                    out_buf_arrays(I)(J)(K) <= temp_out_buf_arrays(I)(J)(K+1);
                                else
                                    out_buf_arrays(I)(J)(K) <= (others => '0');
                                end if;
                            end loop;
                        end if;
                    end loop;
                end loop;
                
                counter <= 0;
            
            end if;
            
            
        elsif store = '1' then
            -- Storing of data is performed here
            
        elsif load = '1' then
            -- Load data from memory into router
            -- CREDIT COUNTER
            credit_counters <= in_credits;
            
            -- Crossbar config
            cb_config <= in_cb_config;
            
            -- Crossbar inputs
            cb_inputs <= in_cb_inputs;
            
            -- enable allocators & crossbar
            vc_enable <= '1';
            sw_enable <= '1';
            cb_enable <= '1';
            
            
            
        end if;
    end if;

end process;


-- Convert free_vcs from input port to free_vc for VC ALLOCATOR
process(rst, free_vcs_array)
begin
    if rst = '1' then
        free_vc <= (others => '0');
    else 
        free_vc <= (others => '0');
        for I in 0 to PORTS-1 loop
            for J in 0 to VCS-1 loop
                -- Check whether this input unit has freed an output VC
                if(free_vcs_array(I)(J) = '1') then
                    free_vc(all_ports_array(I)(J)*VCS + all_vcs_array(I)(J)) <= '1';
                end if;
            end loop;
        end loop;
    end if;
end process;


-- Connect I/O signals to internal signals for easy interconnection
OUTER_LOOP:
for I in 0 to PORTS-1 generate
    INNER_LOOP:
    for J in 0 to VCS-1 generate
        in_states_t(I)(J) <= in_states(I*VCS + J);
        out_states(I*VCS+J) <= out_states_t(I)(J);
        
        in_alloc_vcs_t(I)(J) <= in_alloc_vcs(I*VCS + J);
        out_alloc_vcs(I*VCS+J) <= all_vcs_array(I)(J);
        
        in_alloc_ports_t(I)(J) <= in_alloc_ports(I*VCS + J);
        out_alloc_ports(I*VCS+J) <= all_ports_array(I)(J);
        
    end generate;
end generate;

-- VC ALLOCATOR
in_vc_out_prios <= in_vc_out_prio_array;
in_vc_in_prios <= in_vc_out_prio_array;
out_vc_out_prio_array <= out_vc_out_prios;
out_vc_in_prio_array <= out_vc_in_prios;

-- SWITCH ALLOCATOR
in_sw_out_prios <= in_sw_out_prio_array;
in_sw_in_prios <= in_sw_out_prio_array;
out_sw_out_prio_array <= out_sw_out_prios;
out_sw_in_prio_array <= out_sw_in_prios;

-- Credit counters
out_credits <= credit_counters;

-- Crossbar config
out_cb_config <= sw_out_cb_config;


-- Buffers
in_buf_arrays(0) <= in_buf_array_P0;
in_buf_arrays(1) <= in_buf_array_P1;
in_buf_arrays(2) <= in_buf_array_P2;
in_buf_arrays(3) <= in_buf_array_P3;
in_buf_arrays(4) <= in_buf_array_P4;
in_buf_arrays(5) <= in_buf_array_P5;
in_buf_arrays(6) <= in_buf_array_P6;
out_buf_array_P0 <= out_buf_arrays(0);
out_buf_array_P1 <= out_buf_arrays(1);
out_buf_array_P2 <= out_buf_arrays(2);
out_buf_array_P3 <= out_buf_arrays(3);
out_buf_array_P4 <= out_buf_arrays(4);
out_buf_array_P5 <= out_buf_arrays(5);
out_buf_array_P6 <= out_buf_arrays(6);


end Behavioral;
