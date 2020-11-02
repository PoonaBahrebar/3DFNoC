# This file is used to create an array of destinations used for the local traffic pattern

import datetime
import math

today = datetime.date.today()
date = str(today.day) + "/" + str(today.month) + "/" + str(today.year)

size = 32768
radix = 3
N = radix*radix*radix

p = 0.5
max_hop = 3*(radix-1)
P = [0] * (max_hop+1)
for d in range(1,max_hop+1):
    P[d] = 1.0/(4*d) *( (1+d*(d-1))**p - (d*(d-1))**p + (d*(d+1))**p - (1+d*(d+1))**p )


print(P)

CPD = [0] * (max_hop + 1)
CPD_total = 0
for d in range(1, max_hop+1):
    S = 0
    for i in range(1, int(2*math.sqrt(N)-2)):
        if( math.sqrt(N) + i -d <= math.sqrt(N) and math.sqrt(N) + i - d > 0):
            S += (math.sqrt(N) - i) * (math.sqrt(N) + i - d)
    CPD[d] = P[d] * S
    CPD_total += CPD[d]
    
    
print(" CPD before normalize: " + str(CPD))

# Normalize CPD
CPD[:] = [x/CPD_total for x in CPD]

print(CPD)

# Scaled to size
# Determine for each source router, the list of destinations
factor = [0] * N
for z_s in range(radix):
    for y_s in range(radix):
        for x_s in range(radix):
            source = x_s + radix*y_s + radix*radix*z_s
            hop_occ = [0]*(max_hop+1)
            for z_d in range(radix):
                for y_d in range(radix):
                    for x_d in range(radix):
                        hops = abs(x_d - x_s) + abs(y_d - y_s) + abs(z_d - z_s)
                        hop_occ[hops] += 1
                        factor[source] += CPD[hops]
                        
            factor[source] = size/factor[source]
        


            
Matrix = [[0 for x in range(size)] for y in range(N)]                         

# Determine for each source router, the list of destinations
for z_s in range(radix):
    for y_s in range(radix):
        for x_s in range(radix):
            source = x_s + radix*y_s + radix*radix*z_s
            index = 0
            CPD_SCALED = [int(x * factor[source]) for x in CPD]
            print(CPD_SCALED)
            for z_d in range(radix):
                for y_d in range(radix):
                    for x_d in range(radix):
                        destination = x_d + radix*y_d + radix*radix*z_d
                        hops = abs(x_d - x_s) + abs(y_d - y_s) + abs(z_d - z_s)
                        steps = CPD_SCALED[hops]
                        for i in range(steps):
                            Matrix[source][index] = destination
                            index +=1
            i = 0
            while(index < size):
                if(i == source):
                    i = (i + 1) % N
                
                Matrix[source][index] = i
                index += 1
                i = (i + 1) % N
            
            
   
    
# WRITE TO FILE   
file = open("Destiantions_file_RENT_" + str(radix) + ".txt", "w") 


# WRITE HEADER
line = "----------------------------------------------------------------------------------"
file.write(line + "\n")
line = "--  File:           Destinationse_rent_file.vhd"
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
line = "package destinations_rent is"
file.write(line + "\n")
file.write("\n")

line = "type    INT_RADIX3_ARRAY is array(integer range <>) of integer range 0 to RADIX*RADIX*RADIX-1;"
file.write(line + "\n")
line = "type    INT_RADIX3_ARRAY_T is array(integer range <>) of INT_RADIX3_ARRAY(0 to " + str(size-1) + ");"
file.write(line + "\n")
line = "constant DESTINATIONS_RENT : INT_RADIX3_ARRAY_T(0 to " + str(N-1) + ") := ("
file.write(line + "\n")

for k in range(N):
    index = 0
    nums_per_line = 20
    line = "(";
    file.write(line)
    while(index < size):
        line = ""
        if(size-index < 20):
            nums_per_line = size-index
        for i in range(nums_per_line):
            if(index < size-1):
                line = line + str(Matrix[k][index]) + ", "
                index += 1
            elif(index == size-1 and k!= N-1):
                line = line + str(Matrix[k][index]) + "), "
                index += 1
            elif(index == size-1 and k == N-1):
                line = line + str(Matrix[k][index]) + ") "
                file.write(line + "\n")
                line = ");"
                index += 1
                
        file.write(line + "\n")
    file.write("\n")

file.write("\n")
line = "end package;"
file.write(line + "\n")



file.close()
                
                
                
                

            