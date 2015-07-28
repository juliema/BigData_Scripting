'''
Created on Jun 29, 2015

Given a fasta-formatted alignment file ("Final_Alignment.Oct20.2014.fasta" here) with GI
numbers as taxa names (in format of >gi####, >####, >gi|####, or >gi|gi|####), get
any sequence with the GI number in the provided file ("truncated_gis.txt" here) and
output 

Outputs:
A fasta-formatted alignment containing only the GIs found in truncated_gis.txt
("Truncated_prelinearized.fasta" here)
A file containing just the GI numbers that were found ("AllFoundGIs.txt" here)

To-do:
Should be rewritten to take command-line arguments
Readme needs to be written

'''
import re

fastafile = open("Final_Alignment.Oct20.2014.fasta")
outputfasta = open("Truncated_prelinearized.fasta","w")

sequences = open("truncated_gis.txt")
sequence_list = []
foundgis = open("AllFoundGIs.txt","w")

gi_match = r">g?i?\|?g?i?\|?(\d*)"

newlinematch = r"(.*)\n?"

for line in sequences:
    sequence_list.append(re.match(newlinematch,line).group(1))
sequences.close()

matched_sequence_list = []

copysequence=False
for line in fastafile:
    #match line with reg exp to get taxon name
    if re.search(gi_match,line) is None:
        if copysequence:
            outputfasta.write(line)
        continue
    copysequence=False
    taxon_gi = re.search(gi_match,line).group(1);
    foundgis.write(taxon_gi+"\n")
    for sequence in sequence_list:
        if sequence == taxon_gi:
            outputfasta.write(line)
            matched_sequence_list.append(line)
            copysequence=True

fastafile.close()
outputfasta.close()
foundgis.close()