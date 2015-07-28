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

//Given a file that has many taxa, create args[1] files with args[2] taxa in each, using args[3] as the name root (or args[0], input file), randomly sampled without replacement
public class RandomSubsection {
	public static void main(String[] args) {
		List<Taxon> taxa = new ArrayList<Taxon>();
		Scanner scan=null;
		File f = new File(args[0]);
		String outputroot = args[0];
		int numtaxaperfile = 10;
		int numfiles = 10;
		switch(args.length){
			case 4:
				outputroot = args[3];
			case 3:
				numtaxaperfile = Integer.valueOf(args[2]);
			case 2:
				numfiles = Integer.valueOf(args[1]);
			default:
				break;
		}
		
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
		Taxon temp=null;
		String seq=null;
		
		while(scan.hasNextLine()) {
			line = scan.nextLine();
			if(line.startsWith(">")) { //new taxon
				if(temp!=null) {
					temp.setSequence(seq);
					taxa.add(temp);
				}
				temp = new Taxon(line.substring(1));
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
		
		List<Taxon> nonduplicatedtaxa = new ArrayList<Taxon>();
		//filter duplicates right quick
		for(int i=0;i<taxa.size();i++) {
			Taxon x = taxa.get(i);
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
		
		//output to console some quick facts re: the file
		System.out.println("Alignment successfully read.\nNumber of duplicates (removed):" + numduplicates + "\nNumber of sequences: " + nonduplicatedtaxa.size());
		System.out.println("Creating " + numfiles + " files, each with " + numtaxaperfile + " taxa.");
		
		//make sure directory exists for output files
		if(outputroot.lastIndexOf("/") != -1) {
			File dir = new File(outputroot.substring(0,outputroot.lastIndexOf("/")));
			dir.mkdirs();
		}
		
		//loop through for each file
		for(int i=1;i<=numfiles;i++) {
			//shuffle taxa
			Collections.shuffle(taxa);
			//write out the file
			try {
				File f2 = new File(outputroot + "." + withZeroes(i,numfiles) + ".fasta");
				if(!f2.exists()) f2.createNewFile();
				BufferedWriter bw = new BufferedWriter(new FileWriter(f2));
				for(int j=0;j<numtaxaperfile;j++) {
					bw.append(">" + taxa.get(j).getName() + "\n" + taxa.get(j).getSequence());
				}
				bw.close();
			} catch(IOException e) {
				e.printStackTrace();
				System.exit(8);
			}
		}
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
}
