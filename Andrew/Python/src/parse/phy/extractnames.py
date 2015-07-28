'''
Created on Jul 10, 2015

Given a relaxed-phylip formatted file (string argument provided to open for phy_in),
extract all GI numbers and output to text file "GI_Numbers_Full.txt".

To-do:
Should be rewritten to take command-line arguments
Readme needs to be written
'''

import re

ginumber_expression = r"gi(\d+)"

phy_in = open('Final_Alignment_Linearized.Nov.6.2014.CleanNames.DuplicatesRemoved.phy')

gi_out = open('GI_Numbers_Full.txt','w')

for line in phy_in:
    if re.match(ginumber_expression,line) is None:
        continue
    else:
        gi_out.write(re.match(ginumber_expression,line).group(1)+"\n")

phy_in.close()
gi_out.close()