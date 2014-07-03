Authored by: Andrew Debevec, debevec2@illinois.edu
Compatible with Java 1.5+

What does Linearize do?
---------------------------
Linearize takes a FASTA file with circular sequences that start in different places, and, 
based on one sequence, linearizes the others by copying the "overhanging" portions to the 
appropriate place, effectively forcing the same start site for each sequence.


How to use Linearize.class
---------------------------
- make sure Linearize.class and Taxon.class are in the same folder
- open up Terminal (or, in windows, the command prompt -- windows key + R, type cmd, hit enter.)
- usage:
	java Linearize <alignment-to-be-linearized> <outputname> <reference_name>

<alignment-to-be-linearize> is a FASTA formatted file containing circular sequences that
start in different places.

<outputname> is the name of the linearized output file.

<reference_name> is the name of the sequence that will be used to determine the new "start" and "end" sites
for the alignment.

Example
---------------------------
   java Linearize alignment.fasta alignment_linearized.fasta Consensus5

- if they're not in the same folder you can give the full path
