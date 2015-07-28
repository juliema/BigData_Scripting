'''
Created on Jun 29, 2015

Given a GenBank "Summary" file ("all_hbv_summary.txt" here), generate a csv file
that is formatted with GI number in the first column and length in the second.
Currently works only with sequences up to 9,999 base pairs long, but regular expression
string length_expression can be easily modified to allow longer sequences.

To-do:
Should be rewritten to take command-line arguments
Readme needs to be written
'''

import re

length_expression = r"(\d?),?(\d?\d\d) bp"
ginumber_expression = r"GI:(\d*)"

full_HBV = open('all_hbv_summary.txt')

HBV_table = open("all_hbv_length.csv","w")

HBV_table.write("GI_Number,Length\n")

for line in full_HBV:
    if re.search(length_expression,line) is None:
        if re.search(ginumber_expression,line) is None:
            continue
        else:
            HBV_table.write(re.search(ginumber_expression,line).group(1)+"\n")
    else:
        HBV_table.write(re.search(length_expression,line).group(1)+re.search(length_expression,line).group(2)+",")

full_HBV.close()
HBV_table.close()