#!usr/bin/perl

#use strict;
#use warnings;


#### the information we want to get for all accessions are:
#       usage:  perl scrapegenbank.pl  genbankfile.gb
#       This script will make a table of many gen bank information items.
#            One way to improve it would be to go through all the files in the gen bank 
#		file and determine howmany identifiers there are and then run the script.
#            This was initially formatted for Virus data so much of the lingo is for viruses.
#
#	1. Country
#	2. Host
#	3. Year
#	4. isolate or strain or sub_strain
#
#	Get all the info we can from gen bank, count the number of publications 
#		and divy up them to read to fill in the blanks.
#

# get isolate and strain.



$/="//";

$num=0;
$num1=0;
$file=shift;

##### make an array of accessions.
my @accessionarray=();
my $totalaccessions=0;
my $unique=0;
my %acceshash=();


open FH2, "<$file";
while(<FH2>) {
    if (/ACCESSION\s+(\S+)/) { 
	my $access = $1;
	$totalaccessions++;
	if (! exists $acceshash{$access}) {  push @accessionarray, $access; $acceshash{$access}=1;  $unique++; }
    }
}

print "There are $totalaccessions accessions and $unique unique ones\n";

close FH2;

open OUT, ">GenBank.Info.csv";
print OUT "Accession,Organism,Host,Country,Year,Genotype,Strain,Isolate,Serotype,Subtype,Isolation_Source,Definiton,Note,Title,Journal,Author\n";

my %organismhash=();
my %hosthash=();
my %countryhash=();
my %yearhash=();
my %definitionhash=();
my %notehash=();
my %titlehash=();
my %serotypehash=();
my %subtypehash=();
my %genotypehash=();
my %isolationsourcehash=();
my %strain = ();
my %isolatehash=();

my ($countorg, $countnote,$countisolationsource,$counthost,$countserotype,$countgenotype,$countsubtype,$countcountry,$countyear,$countdef,$counttitle,$countjournal,$countauthors);



open FH3, "<$file";
while (<FH3>) {
	if (/ACCESSION\s+(\S+)/) {
		my $accession=$1;
		if (/\/organism=\"(.*?)\"/) {
			my $org=$1; $org =~ s/,//g;
			$countorg++;
			$organismhash{$accession}=$org;
		}
		if (/\/strain=\"(.*?)\"/) {
			my $strain=$1; $strain =~ s/,//g;
			$countstrain++;
			$strainhash{$accession}=$strain; 
		}
		if (\/isolate=\"(.*?)\"/) {
			my $isolate=$1;  $isolate =~ s/,//g;
			$countisolate++;
			$isolatehash{$accession}=$isolate;
		}
		if (/subtype\s+(.*)/) {
			my $subtype=$1; $subtype =~ s/,//g;
			$countsubtype++;
			$subtypehash{$accession}=$subtype;
		}
		if (/[Gg]enotype.\s+(.*)/) {
                       my $genotype=$1; $genotype =~ s/,//g;
		       $genotype =~ s/\"//g;
                       $countgenotype++;
                       $genotypehash{$accession}=$genotype;
		}
		if (/\/note=\"(.*?)\"/)  {
			my $note=$1; $note =~ s/,//g;
			$countnote++;
			$notehash{$accession}=$note;	
		}
               if (/\/isolation_source=\"(.*?)\"/)  {
                         my $isolationsource=$1; $isolationsource =~ s/,//g;
                         $countisolationsource++;
                         $isolationsourcehash{$accession}=$isolationsource;
               }
	    	if (/DEFINITION(.*)/) {
			my $def=$1; $def =~ s/,//g;
			$countdef++;
                       $definitionhash{$accession}=$def;
		}
    		if (/\/host=\"(.*?)\"/) {
			my $host=$1; $host =~ s/,//g;
			$counthost++;
		        $hosthash{$accession}=$host;
		}
 		if (/\/country=\"(.*?)\"/) {
			my $country=$1; $country =~ s/,//g;
			$countcountry++;
			$countryhash{$accession}=$country;
		}
		if (/\/collection_date=\"(.*?)\"/) {
			my $year=$1; $year =~ s/,//g;
			$countyear++;
			$yearhash{$accession}=$year;
		}
		if (/TITLE\s+(.*)/) {
			my $title=$1; $title =~ s/,//g;
			$counttitle++;
			$titlehash{$accession}=$title;
		}
		if (/JOURNAL\s+(.*)/) {
			my $journal=$1; $journal =~ s/,//g;
			my $countjournal++;
			$journalhash{$accession}=$journal;
		}
		if (/AUTHORS\s+(.*)/) {
			my $authors=$1; $authors =~ s/,//g;
			$countauthors++;
			$authorhash{$accession}=$authors;
		}
		if (/\/serotype=\"(.*?)\"/) {
			my $serotype=$1;  $serotype =~ s/,//g;
			$countserotype++;
			$serotypehash{$accession}=$serotype;
		}
	}
}


for my $acces (@accessionarray) {
	print "$acces\t$titlehash{$acces}\n";
	
	print OUT "$acces,";

 	if (exists $organismhash{$acces})  { print OUT "$organismhash{$acces},"; }
 	if (! exists $organismhash{$acces})  { print OUT "NA,"; }

 	if (exists $hosthash{$acces})  { print OUT "$hosthash{$acces},"; }
        if (! exists $hosthash{$acces})  { print OUT "NA,"; }

 	if (exists $countryhash{$acces})  { print OUT "$countryhash{$acces},"; }
 	if (! exists $countryhash{$acces})  { print OUT "NA,"; }

 	if (exists $yearhash{$acces})  { print OUT "$yearhash{$acces},"; }
 	if (! exists $yearhash{$acces})  { print OUT "NA,"; }

 	if (exists $genotypehash{$acces})  { print OUT "$genotypehash{$acces},"; }
 	if (! exists $genotypehash{$acces})  { print OUT "NA,"; }

 	if (exists $strainhash{$acces})  { print OUT "$strainhash{$acces},"; }
 	if (! exists $strainhash{$acces})  { print OUT "NA,"; }

 	if (exists $isolatehash{$acces})  { print OUT "$isolatehash{$acces},"; }
 	if (! exists $isolatehash{$acces})  { print OUT "NA,"; }

 	if (exists $serotypehash{$acces})  { print OUT "$serotypehash{$acces},"; }
 	if (! exists $serotypehash{$acces})  { print OUT "NA,"; }

 	if (exists $subtypehash{$acces})  { print OUT "$subtypehash{$acces},"; }
 	if (! exists $subtypehash{$acces})  { print OUT "NA,"; }

 	if (exists $isolationsourcehash{$acces})  { print OUT "$isolationsourcehash{$acces},"; }
 	if (! exists $isolationsourcehash{$acces})  { print OUT "NA,"; }

 	if (exists $definitionhash{$acces})  { print OUT "$definitionhash{$acces},"; }
 	if (! exists $definitionhash{$acces})  { print OUT "NA,"; }

 	if (exists $notehash{$acces})  { print OUT "$notehash{$acces},"; }
 	if (! exists $notehash{$acces})  { print OUT "NA,"; }

 	if (exists $titlehash{$acces})  { print OUT "$titlehash{$acces}"; }
 	if (! exists $titlehash{$acces})  { print OUT "NA"; }
	print OUT "\n";
}


print "DEFINITION \t $countdef\n";
print "TITLE \t $counttitle\n";
print "JOURNAL \t $countjournal\n";
print "AUTHORS \t $countauthors\n";


print "/organism  \t $countorg\n";
print "/host \t $counthost\n";
print "/collection_date \t $countyear\n";
print "/note \t $countnote\n";
print "/country \t $countcountry\n";
print "/serotype \t $countserotype\n";
print "genotype $countgenotype\n";
print "subtype $countsubtype\n";
print "isolation source $countisolationsource\n";
print "isolate\t $countisolate\n";
print "strain \t $countstrain\n";

