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

//FastaToPhylip converts a standard FASTA file to a RAxML-compliant PHYLIP file
//Also cleans names to remove characters that may interfere with other programs.
//Also removes duplicated sequences.
//Call: java FastaToPhylip <input_fasta_file> <optional_output_name>
public class FastaToPhylip {
	public static void main(String[] args) {
		List<Taxon> taxa = new ArrayList<Taxon>();
		Scanner scan=null;
		File f = new File(args[0]);
		String outputname = args[0]+".phy";
		switch(args.length){
			case 2:
				outputname = args[1];
				break;
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
		
		List<Taxon> donetaxa = new ArrayList<Taxon>();
		
		for(int i=0;i<taxa.size();i++) {
			Taxon x = taxa.get(i);
			boolean duplicate = false;
			for(int j=0;j<donetaxa.size();j++){
				if(donetaxa.get(j).getName().equals(x.getName())) {
					duplicate = true;
				}
			}
			if(!duplicate) {
				donetaxa.add(x);
			}
		}
		
		int num_taxa = donetaxa.size();
		
		donetaxa = new ArrayList<Taxon>();
		
		//open new file and append all taxa/sequence in fasta format
		try {
			File f2 = new File(outputname);
			if(!f2.exists()) f2.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(f2));
			bw.append(num_taxa + " " + (taxa.get(0).getSequence().length()-1)+"\n");
			for(int i=0;i<taxa.size();i++) {
				Taxon x = taxa.get(i);
				boolean duplicate = false;
				for(int j=0;j<donetaxa.size();j++){
					if(donetaxa.get(j).getName().equals(x.getName())) {
						duplicate = true;
					}
				}
				if(!duplicate) {
					donetaxa.add(x);
					bw.append(x.getName(maxlength) + x.getSequence());
				}
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(8);
		}
	}
}
