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

//Ladderize is used to sort FASTA files in a specific way. See ladderize_readme.txt for more info.
public class Ladderize {
	public static void main(String[] args) {
		List<Taxon> taxa = new ArrayList<Taxon>();
		Scanner scan=null;
		File f = new File(args[0]);
		String outputname = args[0]+"_ladderized.fasta";
		int repeatdist = 0;
		String repeat = "";
		switch(args.length){
			case 3:
				repeatdist = 60;
			case 4:
				repeat = args[2];
				repeatdist = Integer.valueOf(args[3]);
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
		boolean repeatfound=false;
		
		//use regular expressions to find where each sequence begins (i.e., the first non-gap character)
		Pattern start = Pattern.compile("-*[A-Z]");
		Matcher matchstart;
		String repeatseq="";
		
		//loop through all lines and add taxa to the list
		while(scan.hasNextLine()) {
			line = scan.nextLine();
			if(line.startsWith(">")) { //new taxon
				if(temp!=null) {
					temp.setSequence(seq);
					matchstart = start.matcher(seq);
					matchstart.find();
					temp.setStart(matchstart.end());
					taxa.add(temp);
				}
				temp = new Taxon(line.substring(1));
				seq="";
				if(line.substring(1).equals(repeat)) {
					repeatfound=true;
				}
			} else { //we are getting sequence data
				seq+=line;
			}
		}
		//the next five lines take care of the last taxon
		temp.setSequence(seq);
		matchstart = start.matcher(seq);
		matchstart.find();
		temp.setStart(matchstart.end());
		taxa.add(temp); 
		
		//close file
		try {
			br.close();
		} catch(IOException io) {
			io.printStackTrace();
			System.exit(6);
		}
		
		if(repeatfound) {
			//set the repeatseq output string based on the first repeat sequence in the list, since files contain many repeat lines
			for(Taxon t:taxa) {
				if(t.getName().equals(repeat)) {
					repeatseq=">"+t.getName()+"\n"+t.getSequence(60)+"\n";
					break;
				}
			}
			
			//remove all repeat sequences from list, since we already have one to put in at intervals
			for(int i=0;i<taxa.size();i++) {
				Taxon t = taxa.get(i);
				if(t.getName().equals(repeat)) {
					taxa.remove(i--);
				}
			}
		}
		
		//sort based on the start of the sequence -- we can do this because Taxon uses the Comparable interface, which Collections.sort() uses
		Collections.sort(taxa);
		
		//open new file and append all taxa/sequence in fasta format
		try {
			File f2 = new File(outputname);
			if(!f2.exists()) f2.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(f2));
			for(int i=0;i<taxa.size();i++) {
				if(repeatfound && i%repeatdist == 0) {
					bw.append(repeatseq);
				}
				Taxon x = taxa.get(i);
				bw.append(">"+x.getName()+"\n");
				bw.append(x.getSequence(60)+"\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(8);
		}
	}
}
