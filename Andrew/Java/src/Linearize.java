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
	public static void main(String[] args) { //call: Linearize <alignment to be linearized> <output name> <reference sequence>
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
					System.out.println("Found reference: " + referenceName);
				}
				if(!flip) count++;
			} else { //we are getting sequence data
				seq+=line;
			}
		}
		temp.setSequence(seq);
		taxa.add(temp); //takes care of last taxon
		System.out.println(referenceName);
		//close file
		try {
			br.close();
		} catch(IOException io) {
			io.printStackTrace();
			System.exit(6);
		}
		
		//standardize lengths, in case some sequences aren't long enough
		int seqLength = 0;
		
		for(Taxon t: taxa) {
			if(t.getSimpleSequence().length() > seqLength) {
				seqLength = t.getSimpleSequence().length();
			}
		}
		
		for(Taxon t:taxa) {
			//check length
			while(t.getSimpleSequence().length() < seqLength) {
				t.setSequence(t.getSimpleSequence()+"-");
			}
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
		Pattern beginning = Pattern.compile("\\-*[A-Z]"); //to detect overhanging ends, not only gaps
		Matcher m;
		System.out.println("Beginning to search for sequences that start before the reference.");
		for(Taxon x:taxa) {
			String leadingfrag = x.getSimpleSequence().substring(0,start);
			m = beginning.matcher(leadingfrag);
			if(m.find()) { //we have an overlapping end to fix!
				int e = m.end();
				leadingfrag = leadingfrag.substring(e-1);
				int l = leadingfrag.length();
				String tbr = "";
				try {
				  tbr = x.getSimpleSequence().substring(end-l,end);
				} catch(StringIndexOutOfBoundsException sioobe) {
					System.err.println("A sequence was not as long as expected.\n\nUsually, this occurs because all sequences are not the same length in the FASTA file.\n\nOne easy way to fix this is to save it as a NEXUS file in Seaview, open it in Seaview, and then save it as a FASTA file.");
					sioobe.printStackTrace();
					
					/*System.out.println("x.getSimpleSequence(): \t" + x.getSimpleSequence());
					System.out.println("x.getName():\t" + x.getName());
					System.out.println("\tseq length:\t" + x.getSimpleSequence().length());
					System.out.println("leadingfrag:\t" + leadingfrag);
					System.out.println("\tlength:    \t" + l);
					System.out.println("e: \t" + e);
					System.out.println("end:\t" + end);*/
					System.exit(0);
				}
				//does tbr have all gaps?
				Matcher m2 = beginning.matcher(tbr);
				if(m2.find()) {
					//end-l to end has sequence data where there should be none. Can we detect if there are any extra gaps nearby to shove sequence into?
					int extendedend = end-l*4>0?end-l*4:(end-l-200>0?end-l-200:end-l-1);
					String neartbr = x.getSimpleSequence().substring(extendedend,end-l);
					
					//now we have neartbr: which should be neartbr="......" tbr="ATT" (e.g.)
					//now, count if between tbr and neartbr we have l gaps
					int gaps = countGaps(tbr) + countGaps(neartbr);
					if(gaps<l) {
						System.err.println("Error: Detected sequence on left side of reference; but right edge of sequence " + x.getName() + " is not all gaps!\n" +
									"Most likely error: sequence is misaligned with respect to the reference, but probably only by one or two bases.");
						System.out.println("Left side (beginning):" + leadingfrag);
						System.out.println("Right side (end):" + tbr);
						System.exit(7);
					}
					//otherwise, we know that we have room to slide tbr over and add leadingfrag anyway, so do that
					String fragment = makeFragmentLeft(neartbr,tbr,leadingfrag,extendedend*-1+end);
					System.out.println("Note: needed to slide sequence to left to make room for " + leadingfrag + " in sequence " + x.getName());
					//System.out.println("Sequence: " + x.getName() + "\nPrefix: " + x.getSimpleSequence().substring(start,extendedend) + "\nFragment: " + fragment + " (same as " + neartbr + tbr + leadingfrag + ")\n"
					//		+"L:" + l + "\tTBR: " + tbr + "\tnearTBR: " + neartbr + "\tLeadingFrag: "+leadingfrag);
					//System.out.println("Count gaps: \tTBR: " + countGaps(tbr) + "\tneartbr: " + countGaps(neartbr));
					
					x.setSequence(x.getSimpleSequence().substring(start,extendedend) + fragment + x.getSimpleSequence().substring(end));
				} else {
					x.setSequence(x.getSimpleSequence().substring(start,end-l) + leadingfrag + x.getSimpleSequence().substring(end));
				}
			} else x.setSequence(x.getSimpleSequence().substring(start));
		}
		System.out.println("Finished!\nBeginning to search for sequences that end after the reference.");
		
		//Matches cases where the sequence is longer on the right side than the reference.
		Pattern ending = Pattern.compile("([A-Z])(\\-*\n)"); //to detect overhanging ends, not only gaps
		Matcher m3;
		for(Taxon x:taxa) {
			String endingfrag = x.getSequence().substring(end-start);
			m3 = ending.matcher(endingfrag);
			if(m3.find()) { //we have an overlapping end to fix!
				//System.out.println("Original endingfrag: " + endingfrag);
				int e = m3.start()+1;
				endingfrag = endingfrag.substring(0,e);
				//endingfrag = endingfrag.replace("-","");
				//endingfrag = endingfrag.replace("\n","");
				int l = endingfrag.length();
				String tbr = x.getSimpleSequence().substring(0,l);
				//System.out.println("Modified ef: " + endingfrag);
				//does tbr have all gaps?
				Matcher m4 = ending.matcher(tbr);
				if(m4.find()) {
					//end-l to end has sequence data where there should be none. Can we detect if there are any extra gaps nearby to shove sequence into?
					String neartbr = x.getSimpleSequence().substring(l,l+500);
					//now we have neartbr: which should be tbr = "ATT" neartbr="..." (e.g.)
					//now, detect if neartbr is all gaps
					int gaps = countGaps(tbr) + countGaps(neartbr);
					if(gaps<l) {
						System.err.println("Error: Detected sequence on right side of reference; but left edge of sequence " + x.getName() + " is not all gaps!\n" +
								"Most likely error: sequence is misaligned with respect to the reference, but probably only by one or two bases.");
						System.out.println("endingfrag:\n" + endingfrag);
						System.out.println("tbr:\n" + tbr);
						System.out.println("neartbr:\n" + neartbr);
						System.out.println("gaps: " + gaps);
						System.out.println("l: " + l);
						System.exit(7);
					}
					//otherwise, we know that we have room to slide tbr over and add endingfrag anyway, so do that
					System.out.println("Note: needed to slide sequence to right to make room for " + endingfrag + " in sequence " + x.getName());
					
					String fragment = makeFragmentRight(endingfrag,tbr,neartbr,(l+500));
					//System.out.println(fragment);
					x.setSequence(fragment + x.getSimpleSequence().substring(l+500,end-start));
				} else {
					x.setSequence(endingfrag + x.getSimpleSequence().substring(l,end-start));
				}
			} else x.setSequence(x.getSimpleSequence().substring(0,end-start));
		}
		
		
		//open new file and append all taxa/sequence in fasta or other format
		try {
			File f2 = new File(args[1]);
			if(!f2.exists()) f2.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(f2));
			for(Taxon x: taxa) {
				bw.append(">"+x.getName()+"\n");
				bw.append(x.getSequence());
			}
			bw.close();
			System.out.println("Finished!\nLinearized alignment written to disc.");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(8);
		}
		
	}
	
	private static String makeFragmentLeft(String a, String b, String c, int length) {
		String frag = "";
		for(int i=0;i<a.length();i++) {
			if(a.charAt(i)!='-') {
				frag = frag + a.charAt(i);
			}
		}
		for(int i=0;i<b.length();i++) {
			if(b.charAt(i)!='-') {
				frag = frag + b.charAt(i);
			}
		}
		int maxab = length-c.length();
		for(int i=frag.length();i<maxab;i++) {
			frag = frag + '-';
		}
		
		for(int i=0;i<c.length();i++) {
			if(c.charAt(i) == '-') {
				frag = frag.substring(0,maxab) + '-' + frag.substring(maxab);
			} else frag = frag + c.charAt(i);
		}
		return frag;
	}
	
	
	private static String makeFragmentRight(String a, String b, String c, int length) {
		String frag = "";
		for(int i=0;i<a.length();i++) {
			if(a.charAt(i)!='-') {
				frag = frag + a.charAt(i);
			}
		}
		for(int i=0;i<b.length();i++) {
			if(b.charAt(i)!='-') {
				frag = frag + b.charAt(i);
			}
		}
		
		for(int i=0;i<c.length();i++) {
			if(c.charAt(i) != '-') {
				frag = frag + c.charAt(i);
			}
		}
		System.out.println("Fraglength: " + frag.length() + " Length: " + length);
		for(int i=frag.length();i<length;i++) {
			frag = frag + "-";
		}
		return frag;
	}

	private static int countGaps(String s) {
		int gaps=0;
		for(int i=0;i<s.length();i++) {
			if(s.charAt(i)=='-') {
				gaps++;
			}
		}
		return gaps;
	}
}
