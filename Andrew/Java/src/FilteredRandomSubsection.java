import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Given a file args[0] that has many taxa, create args[1] files with args[2] taxa in each,
//with args[3] bp overlap between each sequence and using args[4] as the name root
//(or args[0], input file), randomly sampled without replacement.
//Usage: java FilteredRandomSubsection <input_file> <number_initial_files>
//                                          <number_taxa_per_file> <bp_overlap_required>
//                                               <optional_output_stem>
public class FilteredRandomSubsection {
	public static void main(String[] args) {
		List<RGTaxon> taxa = new ArrayList<RGTaxon>();
		Scanner scan=null;
		File f = new File(args[0]);
		String outputroot = args[0];
		int numtaxaperfile = 1000;
		int numfiles = 100;
		int bpoverlap = 20;
		switch(args.length) {
			case 5:
				outputroot = args[4];
			case 4:
				bpoverlap = Integer.valueOf(args[3]);
			case 3:
				numtaxaperfile = Integer.valueOf(args[2]);
			case 2:
				numfiles = Integer.valueOf(args[1]);
			default:
				break;
		}
		
		
		outputroot = outputroot + ".bp" + bpoverlap + "_nt" + numtaxaperfile;
		BufferedReader br=null;
		
		//open file f to scanner
		try {
			br= new BufferedReader(new FileReader(f));
			scan= new Scanner(br);
		} catch(FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			System.exit(5);
		}
		
		//add all taxa to List<Taxon> taxa
		String line;
		RGTaxon temp=null;
		String seq=null;
		
		while(scan.hasNextLine()) {
			line = scan.nextLine();
			if(line.startsWith(">")) { //new taxon
				if(temp!=null) {
					temp.setSequence(seq);
					taxa.add(temp);
				}
				temp = new RGTaxon(line.substring(1));
				seq="";
			} else { //we are getting sequence data
				seq+=line;
			}
		}
		temp.setSequence(seq);
		taxa.add(temp); //takes care of last taxon
		
		//close file
		try {
			br.close();
		} catch(IOException io) {
			io.printStackTrace();
			System.exit(6);
		}
		
		//filter taxon name data
		//also start to find max length of name
		int maxlength = 0;
		for(int i=0;i<taxa.size();i++) {
			boolean cleaned = taxa.get(i).cleanName();
			if(!cleaned) System.err.println("name not cleaned: " + taxa.get(i).getName());
			
			if(taxa.get(i).getName().length() > maxlength) maxlength = taxa.get(i).getName().length(); 
		}
		maxlength = maxlength+2;
		
		List<RGTaxon> nonduplicatedtaxa = new ArrayList<RGTaxon>();
		//filter duplicates right quick
		for(int i=0;i<taxa.size();i++) {
			RGTaxon x = taxa.get(i);
			boolean duplicate = false;
			for(int j=0;j<nonduplicatedtaxa.size();j++){
				if(nonduplicatedtaxa.get(j).getName().equals(x.getName())) {
					duplicate = true;
				}
			}
			if(!duplicate) {
				nonduplicatedtaxa.add(x);
			}
		}
		int numduplicates = nonduplicatedtaxa.size()-taxa.size();
		//replace original list with this one
		taxa = nonduplicatedtaxa;
		int allTaxaSize = taxa.size();
		
		//output to console some quick facts re: the file
		System.out.println("Alignment successfully read.\nNumber of duplicates (removed):" + numduplicates + "\nNumber of sequences: " + allTaxaSize);
		System.out.println("Creating " + numfiles + " files, each with " + numtaxaperfile + " taxa.");
		
		//make sure directory exists for output files
		if(outputroot.lastIndexOf("/") != -1) {
			File dir = new File(outputroot.substring(0,outputroot.lastIndexOf("/")));
			dir.mkdirs();
		}
		
		
		
		//APPROACH 1: naive
		System.out.println("Performing naive algorithm to guarantee pairwise overlap of " + bpoverlap + ".");
		
		//loop through for each file
		for(int i=1;i<=numfiles;i++) {
			//shuffle taxa
			Collections.shuffle(taxa);
			List<RGTaxon> temptaxa = new ArrayList<RGTaxon>();
			temptaxa.add(taxa.get(0));
			int j = 1;
			
			while(temptaxa.size() < numtaxaperfile && allTaxaSize > j) {
				RGTaxon temptaxon = taxa.get(j);
				if(pairwiseTest(temptaxa,temptaxon,bpoverlap)) {
					temptaxa.add(temptaxon);
				}
				j++;
			}
			
			if(temptaxa.size() == numtaxaperfile) {
			//write out the file
				removeGapOnlySites(temptaxa);
				try {
					File f2 = new File(outputroot + "." + withZeroes(i,numfiles) + ".fasta");
					if(!f2.exists()) f2.createNewFile();
					BufferedWriter bw = new BufferedWriter(new FileWriter(f2));
					for(int k=0;k<numtaxaperfile;k++) {
						bw.append(">" + temptaxa.get(k).getName() + "\n" + temptaxa.get(k).getCorrectedSequence());
						temptaxa.get(k).incrementCounter();
					}
					bw.close();
				} catch(IOException e) {
					e.printStackTrace();
					System.exit(8);
				}
				try {
					File f2 = new File(outputroot + "." + withZeroes(i,numfiles) + ".nex");
					if(!f2.exists()) f2.createNewFile();
					BufferedWriter bw = new BufferedWriter(new FileWriter(f2));
					bw.append(generateNEXUSHeader(numtaxaperfile, temptaxa.get(0).getCorrectedSequence().length()-1));
					for(int k=0;k<numtaxaperfile;k++) {
						bw.append(temptaxa.get(k).getName(maxlength) + temptaxa.get(k).getCorrectedSequence());
					}
					bw.append(generateNEXUSFooter());
					bw.close();
				} catch(IOException e) {
					e.printStackTrace();
					System.exit(8);
				}
			}
			else {
				i--;
				//System.out.println("unable to construct file, bp overlap not fulfilled; culprit taxon found later");
			}
		}
		System.out.println("Files successfully written. Checking for sequence use...");
		
		//verify each sequence was used at least once
		List<RGTaxon> unused = new ArrayList<RGTaxon>();
		for(RGTaxon r : taxa) {
			if(r.getCounter() == 0) {
				unused.add(r);
				
			}
		}
		System.out.println("\n\nTOTAL TAXA NOT INCLUDED: " + unused.size());
		System.out.println("TOTAL TAXA: " + allTaxaSize);
		System.out.println("Beginning attempt to construct however many additional files needed with " + numtaxaperfile + " per file...");
		
		//List<RGTaxon> ignore = new ArrayList<RGTaxon>();
		int i=1;
		int limit = 500;
		int counter = 0;
		while(unused.size() > 0 && limit > 0) {
			Collections.shuffle(unused);
			Collections.shuffle(taxa);
			List<RGTaxon> temptaxa = new ArrayList<RGTaxon>();
			temptaxa.add(unused.get(0));
			int j = 1;
			
			while(temptaxa.size() < numtaxaperfile && allTaxaSize > j) {
				RGTaxon temptaxon = taxa.get(j);
				if(pairwiseTest(temptaxa,temptaxon,bpoverlap)) {
					temptaxa.add(temptaxon);
				}
				j++;
			}
			
			if(temptaxa.size() == numtaxaperfile) {
			//write out the file
				removeGapOnlySites(temptaxa);
				try {
					File f2 = new File(outputroot + ".EXTRA." + withZeroes(i,numfiles*10) + ".fasta");
					if(!f2.exists()) f2.createNewFile();
					BufferedWriter bw = new BufferedWriter(new FileWriter(f2));
					for(int k=0;k<numtaxaperfile;k++) {
						bw.append(">" + temptaxa.get(k).getName() + "\n" + temptaxa.get(k).getCorrectedSequence());
						temptaxa.get(k).incrementCounter();
					}
					bw.close();
				} catch(IOException e) {
					e.printStackTrace();
					System.exit(8);
				}
				try {
					File f2 = new File(outputroot + ".EXTRA." + withZeroes(i,numfiles*10) + ".nex");
					if(!f2.exists()) f2.createNewFile();
					BufferedWriter bw = new BufferedWriter(new FileWriter(f2));
					bw.append(generateNEXUSHeader(numtaxaperfile, temptaxa.get(0).getCorrectedSequence().length()-1));
					for(int k=0;k<numtaxaperfile;k++) {
						bw.append(temptaxa.get(k).getName(maxlength) + temptaxa.get(k).getCorrectedSequence());
					}
					bw.append(generateNEXUSFooter());
					bw.close();
				} catch(IOException e) {
					e.printStackTrace();
					System.exit(8);
				}
				i++;
				counter=0;
			}
			else {
				System.out.println("ERROR: sequence may not have at least " + bpoverlap + " bp overlap with at least " + numtaxaperfile + " other taxa. SEQ:" + unused.get(0).getName());
				//System.out.println("Ignoring " + unused.get(0).getName() + "...");
				//ignore.add(unused.get(0));
				counter++;
				if(counter>=limit) {
					System.out.println("Did not create a file successfully in " + limit + " consecutive attempts. Exiting...");
					break;
				}
			}
			
			List<RGTaxon> tempunused = new ArrayList<RGTaxon>();
			for(RGTaxon r : unused) {
				if(r.getCounter() == 0) {
					tempunused.add(r);
				}
			}
			unused = tempunused;
			System.out.println("Extra files made: " + (i-1) + "\t Unused remaining: " + unused.size());
		}
		System.out.println("Extra files made: " + (i-1) + "\tExcluded: " + unused.size());
		System.out.println("Excluded sequences:");
		for(RGTaxon r : unused) {
			System.out.println(r.getName());
		}
		
		try {
			File f2 = new File(outputroot + ".REPORT.csv");
			if(!f2.exists()) f2.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(f2));
			bw.append("Taxon name,Number of times placed,Non-gap length"+"\n");
			for(RGTaxon r : taxa) {
				int l = r.getSequence().replace("-","").length();
				bw.append(r.getName()+","+r.getCounter()+","+l+"\n");
			}
			bw.close();
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(8);
		}
		//write csv: taxa name, counter
		
	}
	
	private static String generateNEXUSHeader(int ntax, int nchar) {
		return "#NEXUS\n\nBEGIN DATA;\nDIMENSIONS NTAX="+ntax+" NCHAR=" + nchar +";\n" +
				"FORMAT DATATYPE=DNA GAP=- MISSING=?;\nMATRIX\n";
	}
	
	private static String generateNEXUSFooter() {
		return ";\nEND;";
	}

	public static String withZeroes(int n, int total) {
		String t = "" + n;
		String x = "" + total;
		
		x.length();
		for(int a=t.length();a<x.length();a++) {
			t = "0" + t;
		}
		return t;
	}
	
	 // returns pairwise distance of two sequences
    // # differences / # sites
    // gaps are not applicable.
    private static boolean pairwiseTest(List<RGTaxon> all, RGTaxon taxon, int min) {
        for(RGTaxon rgt : all) {
    		if(howMuchOverlap(rgt,taxon) < min) {
    			return false;
    		}
    	}
           
        return true;
    }
    
    //if this method ends up taking too much time we should somehow store
    //previously computed values (maybe a map?) so that some
    //comparisons can be reused. e.g. use name1+name2 as a key,
    //and check that in the map before doing normal comparison.
    //when done with comparison add to map.
    // - order of names would matter; would probably want to map
    //   both name1+name2 and name2+name1 to the same overlap value
    private static int howMuchOverlap(RGTaxon rt1, RGTaxon rt2) {
    	int overlap = 0;
    	
    	String a = rt1.getSequence();
        String b = rt2.getSequence();
        
        if(a.length() != b.length()) {
            //throw an error
        }
        
        for(int i=0;i<a.length();i++) {
            //if both characters are not gaps, we have overlap
            if(a.charAt(i)!='-' && b.charAt(i) != '-') {
                overlap++;
            }
        }
        
        return overlap;
    }
    
    private static void removeGapOnlySites(List<RGTaxon> tax) {
    	//create boolean array, isNotGapOnly
    	boolean[] isNotGapOnly = new boolean[tax.get(0).getSequence().length()-1]; //there is a \n at end of getsequence so need to remove it from this comparison
    	//initially all values will be set to false
    	
    	//now, go through each taxon in tax and grab the sequence
    	for(RGTaxon r : tax) {
    		char[] seq = r.getSequence().toCharArray();
    		for(int i=0;i<isNotGapOnly.length;i++) {
    			isNotGapOnly[i] = isNotGapOnly[i] || seq[i]!='-'; //yay, it's going to short circuit well run decently optimally
    															  //more optimal might be to use binary representations or something but this is simpler
    		}
    	}
    	//we now have isNotGapOnly, which is FALSE for all gap only sites. these sites should be removed
    	
    	for(RGTaxon r : tax) {
    		r.correctSequence(isNotGapOnly);
    	}
    	
    }
}
