3D FNoC - 3D NoC Emulation Model on a Single FPGA
==============================

The FPGA-based NoC emulators proposed so far are mostly limited to 2D NoCs. Therefore, we extend the 2D <cite><a href="https://dl.acm.org/doi/abs/10.1145/3151758">FNoC</a></cite> emulation model to 3D using a single FPGA. 



The proposed model takes advantage of 3D Time-Division-Multiplexing (TDM) and a clustering method to be able to emulate large (up to 10,648 nodes) NoC designs. 
In order to acquire an estimate of the resource usage on FPGA, a VHDL implementation is developed for certain submodules of the emulator. 

The Java code used for the model is included in the "SW_Model" folder.
The VHDL code used to estimate the resource usage is included in the "VHDL_code" folder. This only contains the code used for the router and IP core modules. 
The VHDL code used for the memory is generated using Python scripts, included in the "Python_scripts" folder.

This emulator is developed for the Master's dissertation "3D NoC Simulation  Model for FPGA" by Jonathan D'Hoore in academic year 2019-2020.


Referencing the 3D FNoC Emulator
---------------
If you use the 3D FNoC emulator in your research, we would appreciate the following citation in any publications to which it has contributed:

Jonathan D'Hoore, Poona Bahrebar and Dirk Stroobandt. 3D NoC Emulation Model on a Single FPGA. In Proc. ACM/IEEE Int. Workshop on System-Level Interconnect Problems and Pathfinding (SLIP'20), pp. 1-8, 2020. 

License
---------------
Please see the <a href="https://github.com/PoonaBahrebar/3DFNoC/blob/main/LICENSE.pdf">license file</a>.


Contributors
---------------
<ul>
  <li>Jonathan D'Hoore - <a href="mailto:jonathan.dhoore@ugent.be">jonathan.dhoore@ugent.be</a></li>
  <li>Poona Bahrebar - <a href="mailto:poona.bahrebar@ugent.be">poona.bahrebar@ugent.be</a></li>
  <li>Dirk Stroobandt - <a href="mailto:dirk.stroobandt@ugent.be">dirk.stroobandt@ugent.be</a></li>
</ul>

Development
---------------
The 3D FNoC emulator is a work-in-progress, your input is welcome.
