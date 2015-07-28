// these are at least a little wrong... fix in Eclipse
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Splice {
    //args[0] = sequence file (FASTA)
    //args[1] = point A
    //args[2] = point B
    public static void main(String[] args) {
        //- read in sequence file
    	Scanner scan = null;
    	BufferedReader br=null;
		File f = new File(args[0]);
		//open file f to scanner
		try {
			br= new BufferedReader(new FileReader(f));
			scan= new Scanner(br);
		} catch(FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			System.exit(5);
		}
		int a = Integer.valueOf(args[1]);
		int b = Integer.valueOf(args[2]);
		boolean blocked=true;
        int maxname=0;
		
        List<Taxon> taxa = new ArrayList<Taxon>();
        
        String line="";
        String seq="";
        Taxon temp=null;
        while(scan.hasNextLine()) {
            line = scan.nextLine();
            if(line.length() < 300 || !(line.startsWith(">")))  { //sequence broken up into blocks
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
            
	        } else { //sequence on same line as taxon name
				blocked=false;
				if(temp!=null) {
					temp.setSequence(seq);
					taxa.add(temp);
				}
				temp = new Taxon(line.substring(1,line.indexOf(" ")));
				if(line.lastIndexOf(" ") > maxname) {
					maxname = line.lastIndexOf(" ");
				}
				seq = line.substring(line.lastIndexOf(" ")+1);
				temp.setSequence(seq);
				taxa.add(temp);
				temp=null;
				seq="";
			}
        }
        if(temp!=null) {
        	temp.setSequence(seq);
        	taxa.add(temp); //takes care of last taxon
        }
        
        try {
			File f2 = new File((args[0]+"_"+a+"to"+b+".fasta"));
			if(!f2.exists()) f2.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(f2));
			for(int i=0;i<taxa.size();i++) {
				Taxon x = taxa.get(i);
				if(x.hasSequence(a,b)) {
					if(blocked) {
						bw.append(">"+x.getName()+"\n");
						bw.append(x.getSequence(60,a,b)+"\n");
					} else {
						bw.append(">"+x.getName(maxname)+x.getSequence(0,a,b)+"\n");
					}
				}
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(8);
		}
    }   
}