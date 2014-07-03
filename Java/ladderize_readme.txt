Authored by: Andrew Debevec, debevec2@illinois.edu

What does Ladderize do?
---------------------------
Ladderize is a program that, given a FASTA formatted file, creates a new FASTA formatted 
file with the sequences in a specific order. Sequences are sorted by their first non-gap character,
such that a sequence "ACCCCC" will be sorted first, and a sequence "--CCCC" second, etc.


How to use Ladderize.class
---------------------------
- make sure Ladderize.class and Taxon.class are in the same folder
- open up Terminal (or, in windows, the command prompt -- windows key + R, type cmd, hit enter.)
- usage:
   java Ladderize <inputfile> <outputfile> <repeat name> <how often you want repeat sequence repeated>

<inputfile> is a FASTA formatted file.

<outputfile> is the name of the outputted file. By default, it is "<inputfile>_ladderized.fasta"

<repeat name> is the name of a sequence that one wishes to be repeated at intervals through the file.
In practice, this can be a consensus file that contains a frame of reference sequence, or any sequence
of the user's choosing. If left blank, no sequence will be repeated. 
If this sequence is included multiple times, the first occurence will be used, and the others discarded.
This allows a file to be "reladderized" using the same repeat sequence without erroneously including 
all copies of the sequence.

<how often you want the repeat sequence repeated> is an integer greater than 0 that determines how many lines
should be included between each repeat sequence. Default is 80.

Example
---------------------------
   java Ladderize alignment.fasta alignment_sorted.fasta Consensus5 100

- if they're not in the same folder you can give the full path