----------------------------------------------------------------------------------
--  File:           input_unit_ext.vhd
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
use work.common.all;

USE ieee.numeric_std.ALL;

entity input_unit_ext is
    Port ( clk              : in    STD_ULOGIC;
           rst              : in    STD_ULOGIC;
           enable           : in    STD_ULOGIC;
           config           : in    STD_ULOGIC;
           input_flit       : in    FLIT;
           grant_vc         : in    integer range 0 to VCS;
           grant_sw         : in    STD_ULOGIC;
           credit_counters  : in    INT_CC_ARRAY(0 to PORTS*VCS-1);
           req_vc           : out   integer range 0 to PORTS;
           req_sw           : out   integer range 0 to PORTS;
           port_allocated   : out   integer range 0 to PORTS;
           vc_allocated     : out   integer range 0 to VCS;
           free_vc          : out   STD_ULOGIC;
           credit           : out   CREDIT;
           
           -- Position of this router
           pos_x            : in    integer range 0 to RADIX-1;
           pos_y            : in    integer range 0 to RADIX-1;  
           pos_z            : in    integer range 0 to RADIX-1;
           
           -- MEMORY SIGNALS
           store            : in    STD_ULOGIC;
           load             : in    STD_ULOGIC;
           -- INPUTS:            
           -- Global state 
           in_state      : in    global_state;
           -- Allocated VC
           in_alloc_vc   : in    integer range 0 to VCS;
           -- Allocated port
           in_alloc_port   : in    integer range 0 to PORTS;
           -- Buffer
           in_buf       : in    FLIT_ARRAY(0 to BUFFER_SIZE-1);
           --OUTPUTS:        
           -- Global state 
           out_state      : out    global_state;
           -- Buffer 
           out_buf      : out   FLIT_ARRAY(0 to BUFFER_SIZE-1)
           );
end input_unit_ext;

architecture Behavioral of input_unit_ext is
-- Global state
signal state            : global_state;

-- Buffer number of flits signal
signal bufferCount      : integer range 0 to BUFFER_SIZE;

-- Internal signals for tracking states
signal allocated_VC     : integer range 0 to VCS;
signal allocated_port   : integer range 0 to PORTS;
signal tail             : STD_ULOGIC;

-- Routing unit signals
signal enable_RU        : STD_ULOGIC;
signal dest_x, dest_y, dest_z : integer range 0 to RADIX-1;
signal output_port      : integer range 0 to PORTS;
signal output_ready :   STD_ULOGIC;


begin

-- Connect internal end output signals
port_allocated <= allocated_port;
vc_allocated <= allocated_vc;
    
ROUTING_UNIT: entity work.routing_unit(Behavioral)
    Port map(
        clk => clk,
        rst => rst,
        enable => enable_RU,
        pos_x => pos_x,
        pos_y => pos_y,
        pos_z => pos_z,
        dest_x => dest_x,
        dest_y => dest_y,
        dest_z => dest_z,
        output => output_port,
        output_ready => output_ready            
    );

-- main process
process(clk, rst)
begin
    if rst = '1' then
        state <= IDLE_0;
        enable_RU <= '0';
        req_vc <= PORTS;
        req_sw <= PORTS;
        bufferCount <= 0;
        
        allocated_port <= PORTS;
        allocated_vc <= VCS;
    
    elsif rising_edge(clk) then
        if enable = '1' then
            -- Reset outputs that only are 'on' for 1 cycle
            free_vc <= '0';
            
            -- Initially set output memory buffers
            out_buf <= in_buf;
                       
        
            case state is                    
                when ROUTING_0 =>
                    if(input_flit(0) = '1') then
                        bufferCount <= bufferCount + 1;
                        for I in 0 to BUFFER_SIZE-1 loop
                            if(in_buf(I)(0) = '0') then
                                out_buf(I) <= input_flit;
                            end if;
                        end loop;
                    end if;
                    enable_RU <= '1';
                    state <= ROUTING_1;
                    
                when ROUTING_1 =>
                    -- ROUTE IS BEING COMPUTED
                    allocated_port <= output_port;
                    enable_RU <= '0';
                    assert(output_ready = '1') report "ROUTING_UNIT NOT READY WHEN OUTPUT PORT SAVED" severity failure;
                    state <= VC_ALLOC_0;
                    
                when VC_ALLOC_0 =>
                    if(input_flit(0) = '1') then
                        bufferCount <= bufferCount + 1;
                        for I in 0 to BUFFER_SIZE-1 loop
                            if(in_buf(I)(0) = '0') then
                                out_buf(I) <= input_flit;
                            end if;
                        end loop;
                    end if;
                    
                    -- VC ALLOCATION IS PERFORMED IN THIS CYCLE
                    state <= VC_ALLOC_1;
                
                when VC_ALLOC_1 =>
                    -- Read grant
                    if(grant_vc /= VCS) then
                        -- Update VC field and reset request
                        allocated_vc <= grant_vc;
                        state <= ACTIVE_0;
                    else
                        state <= VC_ALLOC_0;
                    end if;
                
                
                when ACTIVE_0 =>
                    if(input_flit(0) = '1') then
                        bufferCount <= bufferCount + 1;
                        for I in 0 to BUFFER_SIZE-1 loop
                            if(in_buf(I)(0) = '0') then
                                out_buf(I) <= input_flit;
                            end if;
                        end loop;
                    end if;
                    -- SWITCH ALLOCATION IS PERFORMED
                    state <= ACTIVE_1;
                
                when ACTIVE_1 =>
                    if(grant_sw = '1') then
                        -- Output flit send to crossbar at higher level
                                            
                        -- Send credit to downstream router (VC field filled in on higher level)
                        credit(0) <= '1';   -- valid bit
                                         
                        
                        -- check if this is tail flit
                        if(bufferCount > 1 and in_buf(0)(2 downto 1) = "11") then   -- Check if there is already new flit in buffer
                            state <= ROUTING_0;
                            -- Deallocate resource
                            free_vc <= '1';
                        elsif (in_buf(0)(2 downto 1) = "11") then   -- No new flit
                            state <= IDLE_0;
                            -- Deallocate resource
                            free_vc <= '1';
                        else    -- Packet not completely send yet
                            state <= ACTIVE_0;
                        end if;
                    else
                        -- No credit to send
                        credit <= (others => '0');
                    end if;
                    
                when IDLE_0 =>
                    if(input_flit(0) = '1') then 
                        bufferCount <= bufferCount + 1;
                        state <= ROUTING_1;
                        enable_RU <= '1';
                        for I in 0 to BUFFER_SIZE-1 loop
                            if(in_buf(I)(0) = '0') then
                                out_buf(I) <= input_flit;
                            end if;
                        end loop;
                    else
                        state <= IDLE_1;
                    end if;
                
                when others =>
                    state <= IDLE_0;
            
            
            end case;
            
        
        elsif store = '1' then
            -- Data from this router is being read
            
        
        elsif load = '1' then 
            -- Load data from memory into this input unit
            state <= in_state;
            allocated_vc <= in_alloc_vc;
            allocated_port <= in_alloc_port;
            
            -- Connect flit destination data to routing unit
            if(in_buf(0)(0) = '0') then -- Buffer is emtpy
                dest_z  <=  to_integer(unsigned(input_flit(FLIT_SIZE-1 downto FLIT_SIZE-5)));
                dest_y  <=  to_integer(unsigned(input_flit(FLIT_SIZE-6 downto FLIT_SIZE-10)));
                dest_x  <=  to_integer(unsigned(input_flit(FLIT_SIZE-11 downto FLIT_SIZE-15)));
            else
                dest_z  <=  to_integer(unsigned(in_buf(0)(FLIT_SIZE-1 downto FLIT_SIZE-5)));
                dest_y  <=  to_integer(unsigned(in_buf(0)(FLIT_SIZE-6 downto FLIT_SIZE-10)));
                dest_x  <=  to_integer(unsigned(in_buf(0)(FLIT_SIZE-11 downto FLIT_SIZE-15)));                
            end if;
            
            -- Set up VC request if needed
            if(in_state = VC_ALLOC_0) then
                req_vc <= allocated_port;
            else
                req_vc <= PORTS;
            end if;
            
            -- Set up Switch request if needed
            if(in_state = ACTIVE_0  and (in_buf(0)(0) = '1' or input_flit(0) = '1') and credit_counters(allocated_port * VCS + allocated_VC) > 0) then
                req_sw <= allocated_port;
            else
                req_sw <= PORTS;
            end if;
            
            -- Set buffer count
            bufferCount <= 0;  
            for I in 0 to BUFFER_SIZE-1 loop
                if(in_buf(I)(0) = '1') then 
                    bufferCount <= I +1;
                end if;
            end loop;
        end if;
    end if;
end process;


-- Connect memory output data
out_state <= state;




end Behavioral;
