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

//Given an alignment (args[0]) and a list of taxa names (args[1]) output a file with
//just the taxa found in args[1].
public class PullSequences {
	public static void main(String[] args) {
		List<Taxon> taxa = new ArrayList<Taxon>();
		Scanner alnscan = null;
		Scanner seqscan = null;
		File alignment = new File(args[0]);
		File sequences = new File(args[1]);
		String outputroot = args[1];
		BufferedReader aln = null;
		BufferedReader seq = null;
		//open file f to scanner
		try {
			aln = new BufferedReader(new FileReader(alignment));
			alnscan = new Scanner(aln);
			seq = new BufferedReader(new FileReader(sequences));
			seqscan = new Scanner(seq);
		} catch(FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			System.exit(5);
		}
		
		
		
		//add all taxa to List<Taxon> taxa
		String line;
		Taxon temp=null;
		String tseq=null;
		
		while(alnscan.hasNextLine()) {
			line = alnscan.nextLine();
			if(line.startsWith(">")) { //new taxon
				if(temp!=null) {
					temp.setSequence(tseq);
					taxa.add(temp);
				}
				temp = new Taxon(line.substring(1).trim());
				tseq="";
			} else { //we are getting sequence data
				tseq+=line;
			}
		}
		temp.setSequence(tseq);
		taxa.add(temp); //takes care of last taxon
		
		//close file
		try {
			aln.close();
		} catch(IOException io) {
			io.printStackTrace();
			System.exit(6);
		}
		
		List<Taxon> topull = new ArrayList<Taxon>();
		
		
		while(seqscan.hasNextLine()) {
			line = seqscan.nextLine();
			//find "line" in taxa list
			boolean found = false;
			for(Taxon t : taxa) {
				if(t.getName().equals(line)) {
					topull.add(t);
					found=true;
					break;
				}
			}
			if(!found) System.out.println("Could not find sequence " + line + " in provided alignment.");
		}
		
		
		
		try {
			File f2 = new File(outputroot+".alignment.fasta");
			if(!f2.exists()) f2.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(f2));
			for(Taxon t:topull) {
				bw.append(">" + t.getName() + "\n" + t.getSequence());
			}
			bw.close();
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(8);
		}
		
	}
}
