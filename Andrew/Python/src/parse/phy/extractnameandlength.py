'''
Created on Jul 10, 2015

Given a relaxed-phylip formatted file (string argument provided to open for phy_in),
extract all GI numbers and their non-gap lengths, and output to CSV file "gi_length.csv".

To-do:
Should be rewritten to take command-line arguments
Readme needs to be written
'''

import re

def get_nongap_length(sequence):
    "Returns length of provided sequence excluding gaps"
    nongap = re.sub(r'[\-\?]', '', sequence)
    return str(len(nongap))

ginumber_sequence_expression = r"gi(\d+)\s*([A-Za-z\-\?]+)"

phy_in = open('Final_Alignment_Linearized.Nov.6.2014.CleanNames.DuplicatesRemoved.phy')

gi_out = open('gi_length.csv','w')

gi_out.write("GI Number,Length in alignment\n")
for line in phy_in:
    matching = re.match(ginumber_sequence_expression,line)
    if matching is None:
        continue
    else:
        gi_out.write(matching.group(1)+","+get_nongap_length(matching.group(2))+"\n")

phy_in.close()
gi_out.close()