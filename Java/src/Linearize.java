import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.*;

//Linearize takes a FASTA file with circular sequences that start in different places, and, based on one sequence, linearizes the others by copying the
//"overhanging" portions to the appropriate place, effectively forcing the same start site for each sequence.
public class Linearize {
	//call: Linearize <alignment to be linearized> <output name>
	public static void main(String[] args) { //call: Linearize <alignment to be linearized> <output name>
		List<Taxon> taxa = new ArrayList<Taxon>();
		Scanner scan=null;
		File f = new File(args[0]);
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
		int count=0; //at the end of the loop, count will be the index of the referenceName sequence
		boolean flip=false;
		String referenceName = args[2];
		while(scan.hasNextLine()) {
			line = scan.nextLine();
			if(line.startsWith(">")) { //new taxon
				if(temp!=null) {
					temp.setSequence(seq);
					taxa.add(temp);
				}
				temp = new Taxon(line.substring(1));
				seq="";
				if(line.substring(1).equals(referenceName)) {
					flip=true;
				}
				if(!flip) count++;
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
		
		//find where reference sequence starts
		int start=0;
		int end=0;
		Taxon t = taxa.get(count);
		Pattern pattern = Pattern.compile("\\-*([ACGTMKWSRYBHVDN][A-Z\\-]+[ACGTMKWSRYBHVDN])\\-*\n"); //should match any sequence, the entirety of any sequence
		Matcher matcher = pattern.matcher(t.getSequence());
		while(matcher.find()) {
			System.out.println("Reference sequence found.");
			start = matcher.start(1);
			end = matcher.end(1);
		}
		
		//Matches cases where the sequence is longer on the left side than the reference
		Pattern beginning = Pattern.compile("-*[A-Z]"); //to detect overhanging ends, not only gaps
		Matcher m;
		System.out.println("Beginning to search for sequences that start before the reference.");
		for(Taxon x:taxa) {
			String leadingfrag = x.getSequence().substring(0,start);
			m = beginning.matcher(leadingfrag);
			if(m.find()) { //we have an overlapping end to fix!
				int e = m.end();
				leadingfrag = leadingfrag.substring(e-1);
				int l = leadingfrag.length();
				String tbr = x.getSequence().substring(end-l,end);
				//does tbr have all gaps?
				Matcher m2 = beginning.matcher(tbr);
				if(m2.find()) {
					System.err.println("Error: Detected sequence on left side of reference; but right edge of sequence " + x.getName() + " is not all gaps!\n" +
								"Most likely error: sequence is misaligned with respect to the reference, but probably only by one or two bases.");
					System.out.println("leadingfrag:" + leadingfrag);
					System.out.println("tbr:" + tbr);
					System.exit(7);
				}
				
				x.setSequence(x.getSequence().substring(start,end-l) + leadingfrag + x.getSequence().substring(end));
			} else x.setSequence(x.getSequence().substring(start));
		}
		System.out.println("Finished!\nBeginning to search for sequences that end after the reference.");
		
		//Matches cases where the sequence is longer on the right side than the reference.
		Pattern ending = Pattern.compile("[A-Z]+"); //to detect overhanging ends, not only gaps
		Matcher m3;
		for(Taxon x:taxa) {
			String endingfrag = x.getSequence().substring(end-start);
			m3 = ending.matcher(endingfrag);
			if(m3.find()) { //we have an overlapping end to fix!
				//System.out.println("Original endingfrag: " + endingfrag);
				int e = m3.end();
				endingfrag = endingfrag.substring(0,e);
				int l = endingfrag.length();
				String tbr = x.getSequence().substring(0,l);
				//System.out.println("Modified ef: " + endingfrag);
				//does tbr have all gaps?
				Matcher m4 = ending.matcher(tbr);
				if(m4.find()) {
					System.err.println("Error: Detected sequence on right side of reference; but left edge of sequence " + x.getName() + " is not all gaps!\n" +
							"Most likely error: sequence is misaligned with respect to the reference, but probably only by one or two bases.");
					System.out.println("endingfrag:" + endingfrag);
					System.out.println("tbr:" + tbr);
					
					System.exit(7);
				}
				
				x.setSequence(endingfrag + x.getSequence().substring(l,end-start));
			} else x.setSequence(x.getSequence().substring(0,end-start));
		}
		
		//open new file and append all taxa/sequence in fasta or other format
		try {
			File f2 = new File(args[1]);
			if(!f2.exists()) f2.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(f2));
			for(Taxon x: taxa) {
				bw.append(">"+x.getName()+"\n");
				bw.append(x.getSequence(60)+"\n");
			}
			bw.close();
			System.out.println("Finished!\nLinearized alignment written to disc.");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(8);
		}
		
	}
}
