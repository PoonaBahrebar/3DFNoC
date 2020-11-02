----------------------------------------------------------------------------------
--  File:           ip_core_uniform.vhd
--  Created:        09/08/2020
--  Last Changed:   09/08/2020
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


-- NOTE:    In this implementation, packet generation and sending of the first flit can not happen in the same simulation cycle!

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.numeric_std.ALL;

use work.common.all;

entity ip_core_uniform is
    Port ( clk              : in    STD_ULOGIC;
           rst              : in    STD_ULOGIC;
           enable           : in    STD_ULOGIC;
           input_flit       : in    FLIT;
           input_credit     : in    CREDIT;
           output_flit      : out   FLIT;
           output_credit    : out   CREDIT;
           latency          : out   integer;
           stall            : out   STD_ULOGIC;
           
           -- Memory signals
           -- INPUTS
           store                : in    STD_ULOGIC;
           load                 : in    STD_ULOGIC;
           
           in_source_queue      : in    PD_ARRAY(0 to SQ_SIZE-1);
           in_local_time        : in    STD_LOGIC_VECTOR(14 downto 0);
           in_vc_reqs           : in    STD_LOGIC_VECTOR(0 to SQ_SIZE-1);
           in_sending_vc        : in    integer range 0 to VCS-1;
           in_sq_wr_address     : in    integer range 0 to SQ_SIZE-1;
           
           in_credit_counts     : in    INT_CC_ARRAY(0 to VCS-1);
           in_vc_allocs         : in    INT_SQ_ARRAY(0 to VCS-1);
           in_flits_left        : in    INT_PS_ARRAY(0 to VCS-1);
           in_free_slots        : in    integer range 0 to SQ_SIZE;
           
           in_stalling          : in    STD_ULOGIC;
           
           in_position          : in    integer range 0 to RADIX*RADIX*RADIX-1;
           
           -- OUTPUTS
           out_source_queue     : out   PD_ARRAY(0 to SQ_SIZE-1);
           out_local_time       : out    STD_LOGIC_VECTOR(14 downto 0);
           out_vc_reqs          : out    STD_LOGIC_VECTOR(0 to SQ_SIZE-1);
           out_sending_vc       : out    integer range 0 to VCS-1;
           out_sq_wr_address    : out    integer range 0 to SQ_SIZE-1;
           out_credit_counts    : out    INT_CC_ARRAY(0 to VCS-1);
           out_vc_allocs        : out    INT_SQ_ARRAY(0 to VCS-1);
           out_flits_left       : out    INT_PS_ARRAY(0 to VCS-1);
           out_free_slots       : out    integer range 0 to SQ_SIZE;
           
           out_stalling         : out   STD_ULOGIC;
           out_position         : out    integer range 0 to RADIX*RADIX*RADIX-1
           );
end ip_core_uniform;

architecture Behavioral of ip_core_uniform is

type state_type     is  (STAGE_1, STAGE_2, STAGE_3 );

signal state                :   state_type;
signal rand0, rand1         :   STD_LOGIC_VECTOR(14 downto 0);
signal current_time         :   STD_LOGIC_VECTOR(14 downto 0);

signal new_packet           :   PACKET_DESCR;

-- Credit count
signal credit_count         :   INT_CC_ARRAY(0 to VCS-1);

-- Packet that is currently being transmitted
signal flit_available       :   STD_ULOGIC;

signal vc_reqs              :   STD_LOGIC_VECTOR(0 to SQ_SIZE-1);
signal vc_allocs            :   INT_SQ_ARRAY(0 to VCS-1);
signal flits_left           :   INT_PS_ARRAY(0 to VCS-1);

signal sending_vc           :   integer range 0 to VCS-1;

signal credit_incr          :   integer range 0 to VCS;

signal stalling                : STD_ULOGIC;

-- Source Queue signals
signal wr_address : integer range 0 to SQ_SIZE-1;
signal free_slots   : integer range 0 to SQ_SIZE;

signal position : integer range 0 to RADIX*RADIX*RADIX-1;

begin

-- RANDOM NUMBER GENERATOR
PRNG_0: entity work.prng_15bit(Behavioral)
    Port map(
        clk => clk,
        rst => rst,
        rand => rand0
        ); 
        
-- RANDOM NUMBER GENERATOR
PRNG_1: entity work.prng_15bit(Behavioral)
    Port map(
        clk => clk,
        rst => rst,
        rand => rand1
        );         

        

-- Main process
process(clk, rst)
variable vc_allocated : boolean := false;
variable flit_send     : boolean := false;
variable index : integer range 0 to VCS-1 := 0;
variable dest_x, dest_y, dest_z : integer range 0 to RADIX-1;
variable dest : integer range 0 to RADIX*RADIX*RADIX-1;
begin
    if rst = '1' then
        new_packet <= (others => '0');
        credit_count <= (others => BUFFER_SIZE);
        
        -- Reset VC allocation help signals
        vc_reqs     <= (others => '0');
        vc_allocs   <= (others => 0);
        flits_left  <= (others => 0);
        
        
        current_time <= (others => '0');
        free_slots <= SQ_SIZE;
        wr_address <= 0;
        
    elsif rising_edge(clk) then
        if enable = '1' then 
            case(state) is
                when STAGE_1 =>
                    state <= STAGE_2;
                            
                    -- 1) Packet creation
                    if(free_slots = 0) then
                        -- SQ IS FULL -> STALL PACKET GENERATION!!! 
                        stalling <= '1';
                    else -- SQ IS NOT FULL
                        if(free_slots = SQ_SIZE and stalling = '1') then
                            -- SQ EMPTY WHILE STALLING TG -> STALL NETWORK SO TG CAN KEEP UP!!!!                
                            stall <= '1';
                        end if;
                        if(to_integer(unsigned(rand0)) > TL_PROB) then
                            new_packet(29 downto 15) <= current_time;       -- Timestamp
                            -- Determine destination
                            dest := (to_integer(unsigned(rand1))) mod RADIX*RADIX*RADIX;
                            if(dest = in_position) then
                                dest := (dest + 1) mod RADIX*RADIX*RADIX;
                            end if;
                            dest_x := dest mod RADIX;
                            dest_y := ((dest - dest_x)/RADIX ) mod RADIX;
                            dest_z := (dest- dest_x - RADIX*dest_y)/(RADIX*RADIX); 
                            
                            new_packet(14 downto 10) <= std_logic_vector(to_unsigned(dest_z,5));
                            new_packet(9 downto 5) <= std_logic_vector(to_unsigned(dest_y,5));
                            new_packet(4 downto 0) <= std_logic_vector(to_unsigned(dest_x,5));
                            
                            -- Write packet to SQ
                            out_source_queue(wr_address) <= new_packet;
                            free_slots <= free_slots - 1;
                            
                            -- Request vc
                            vc_reqs(wr_address) <= '1';
                            
                            if wr_address = SQ_SIZE-1 then
                                wr_address <= 0;
                            else
                                wr_address <= wr_address + 1;
                            end if;
                        end if;
                    end if;
                    
                    -- 2) Flit receiving
                    if(input_flit(0) = '1') then
                        -- a) Send credit back
                            output_credit(0) <= '1';
                            output_credit(VC_BITS downto 1) <= input_flit(2+VC_BITS downto 3); 
                        
                        -- b) Compute latency if this is a tail flit
                        if(input_flit(2 downto 1) = "11") then
                            latency <= to_integer(unsigned(current_time)) - to_integer(unsigned(input_flit(19 downto 5)));          -- TODO: latency is just passed to higher level
                        end if;
                    else
                        -- No credit needs to be send
                        output_credit <= (others => '0');
                    end if;
                    
                    
                    -- 3) Credit receiving
                    if(input_credit(0) = '1') then
                        -- Update credit counter
                        credit_incr <= to_integer(unsigned(input_credit(VC_BITS downto 1)));
                    else
                        -- No credit received
                        credit_incr <= VCS;
                    end if;
                    
                    
                    
                    
                when others =>
                    state <= STAGE_1;
                    
                    -- 1) Allocate VCs 
                    vc_allocated := false;
                    for I in 0 to SQ_SIZE-1 loop
                        -- Find a packet that has no VC allocate dyet
                        if (vc_reqs(I) = '1') then
                            for J in 0 to VCS-1 loop
                                -- Find a free VC
                                if(flits_left(J) = 0) then
                                    -- Allocate VC to the packet
                                    vc_allocs(J) <= I;
                                    flits_left(J) <= PACKET_SIZE;
                                    vc_reqs(I) <= '0';
                                    vc_allocated := true;
                                    exit;
                                end if;
                            end loop;
                        end if;
                        if (vc_allocated = true) then
                            -- Only one VC needs to be allocated each cycle (only 1 flit can be send)
                            exit;
                        end if;
                    end loop;
                    
                    -- 2) Send flit
                    index := sending_vc;
                    flit_send := false;
                    for I in 0 to VCS-1 loop
                       if(flits_left(index) > 0 and credit_count(index) > 0 and flit_send = false) then
                           if(index = VCS- 1) then
                                sending_vc <= 0;
                            else
                                sending_vc <= index + 1;
                            end if;
                            flit_send := true;
                            
                            -- Create flit
                            output_flit(0) <= '1';              -- valid signal
                            
                            -- Determine flit type and corresponding payload
                            case(flits_left(index)) is
                                when PACKET_SIZE =>
                                    -- Header flit
                                    output_flit(2 downto 1) <= "01";
                                    output_flit(FLIT_SIZE-1 downto FLIT_SIZE-15) <= in_source_queue(vc_allocs(index))(29 downto 15);       -- Destination
                                    
                                when 1 =>
                                    -- Tail flit
                                    output_flit(2 downto 1) <= "11";
                                    output_flit(FLIT_SIZE-1 downto FLIT_SIZE-15) <= in_source_queue(vc_allocs(index))(14 downto 0);        -- Timestamp
                                    
                                    -- New slot free in SQ
                                    free_slots <= free_slots + 1;
                                when others =>
                                    -- Body flit
                                    output_flit(2 downto 1) <= "10";
                                    output_flit(FLIT_SIZE-1 downto FLIT_SIZE-15) <= in_source_queue(vc_allocs(index))(14 downto 0);        -- Timestamp (not really needed)
                            end case;
                            
                            -- Determine sending VC
                            output_flit(2+VC_BITS downto 3) <= std_logic_vector(to_unsigned(index, VC_BITS));
                            
                            -- Credit update
                            if(index /= credit_incr) then 
                                credit_count(index) <= credit_count(index) -1;
                                if credit_incr /= VCS then
                                    credit_count(credit_incr) <= credit_count(credit_incr) + 1;
                                end if;
                            end if;
                            
                            -- Decrease flits to send
                           flits_left(index) <= flits_left(index) - 1;
                        
                        -- If there are no flits to send, just update credit count (based on received credits)   
                        elsif credit_incr /= VCS then
                            credit_count(credit_incr) <= credit_count(credit_incr) + 1;
                        end if;
                       
                       if(index = VCS- 1) then
                            index := 0;
                        else
                            index := index + 1;
                        end if;
                    end loop;
                    
                    if(flit_send = false) then
                        output_flit <= (others => '0');
                    end if;
                    
                    
                    -- 3) Update time if not in stall
                        current_time <= std_logic_vector(to_unsigned(to_integer(unsigned(current_time)) + 1, 15));
                                     
            end case;
            
        elsif load = '1' then
            current_time <= in_local_time;
            vc_reqs <= in_vc_reqs;
            vc_allocs <= in_vc_allocs;
            flits_left <= in_flits_left;
            sending_vc <= in_sending_vc;
            wr_address <= in_sq_wr_address;
            credit_count <= in_credit_counts;
            free_slots <= in_free_slots;
            stalling <= in_stalling;
            position <= in_position;
            
            -- Set output source queue equal to input at start
            out_source_queue <= in_source_queue;
            
            
        elsif store = '1' then
            out_local_time <= current_time;
            out_vc_reqs <= vc_reqs;
            out_vc_allocs <= vc_allocs;
            out_flits_left <= flits_left;
            out_sending_vc <= sending_vc;
            out_sq_wr_address <= wr_address;
            out_credit_counts <= credit_count;
            out_free_slots <= free_slots;
            out_stalling <= stalling;
            out_position <= position;
        
        end if;
    end if;
end process;

end Behavioral;
