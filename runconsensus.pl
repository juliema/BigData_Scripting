#!usr/bin/perl

use strict;
use warnings;

system "ls -l *.out >filenames";

my $countfiles=0;
open FH, "<filenames";
while (<FH>) {
    if (/(\S+)\.(\S+)\.out/){
	my $file=$1;
	my $num=$2;
	$countfiles++;
	print "$file.$num.out\t $countfiles\n";
	system "perl /Users/jallen/Documents/GitHub/NEW_Phylogenomics/phylogenomics/filtering/consensus.pl $file.$num.out >$num.con.out"; 
    }
}

print "There are $countfiles files\n";
system "cat *.con.out >Consensus.fasta";   
