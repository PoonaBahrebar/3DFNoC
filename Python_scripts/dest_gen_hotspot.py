# This file is used to create array of destinations for the hotspot traffic pattern

import datetime
import math

today = datetime.date.today()
date = str(today.day) + "/" + str(today.month) + "/" + str(today.year)

size = 32768
radix = 10

normal_nodes = radix*radix*radix-1
hotspot_nodes = 1
hotspot_factor = 10
prob_n = 1.0/(normal_nodes + hotspot_factor*hotspot_nodes)
prob_h = hotspot_factor * prob_n

prob_n_scaled = int(prob_n * size)
prob_h_scaled = int(prob_h * size)
total = prob_n_scaled * normal_nodes + prob_h_scaled * hotspot_nodes


# Make array of destinations
arr = [0] * size
hotspot = int((radix*radix*radix -1)/2)
index = 0
for i in range(radix*radix*radix):
    if(i == hotspot):
        for j in range(prob_h_scaled):
            arr[index] = i
            index += 1
    else:
        for j in range(prob_n_scaled):
            arr[index] = i
            index += 1
            
for i in range(size-index):
    arr[index] = i
    index += 1
    
    
# WRITE TO FILE   
file = open("Destiantions_file_" + str(radix) + ".txt", "w") 


# WRITE HEADER
line = "----------------------------------------------------------------------------------"
file.write(line + "\n")
line = "--  File:           Destinations_file.vhd"
file.write(line + "\n")
line = "--  Created:        29/07/2020"
file.write(line + "\n")
line = "--  Last Changed:   " + date
file.write(line + "\n")
line = "--  Author:         Jonathan D'Hoore"
file.write(line + "\n")
line = "--                  University of Ghent"
file.write(line + "\n")
line = "--"
file.write(line + "\n")
line = "--  Part of Master's dissertation submitted in order to obtain the academic degree of"
file.write(line + "\n")
line = "--  Master of Science in Electrical Engineering - main subject Electronic Circuits and Systems"
file.write(line + "\n")
line = "--"
file.write(line + "\n")
line = "--  Academic year 2019-2020"
file.write(line + "\n")
line = "-- "
file.write(line + "\n")
line = "----------------------------------------------------------------------------------"
file.write(line + "\n")


file.write("\n")
line = "use work.common.all;"
file.write(line + "\n")
file.write("\n")
line = "package destinations is"
file.write(line + "\n")
file.write("\n")

line = "type    INT_RADIX3_ARRAY is array(integer range <>) of integer range 0 to RADIX*RADIX*RADIX-1;"
file.write(line + "\n")
line = "constant DESTINATIONS : INT_RADIX3_ARRAY(0 to " + str(size-1) + ") := ("
file.write(line + "\n")


index = 0
nums_per_line = 20
while(index < size):
    line = ""
    if(size-index < 20):
        nums_per_line = size-index
    for i in range(nums_per_line):
        if(index < size-1):
            line = line + str(arr[index]) + ", "
            index += 1
        elif(index == size-1):
            line = line + str(arr[index]) + "); "
            index += 1
            
    file.write(line + "\n")

file.write("\n")
line = "end package;"
file.write(line + "\n")



file.close()
                
                
                
                

            