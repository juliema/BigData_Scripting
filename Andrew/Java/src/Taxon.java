//CLONED FROM GITHUB: Aug 20, 2014, 5:10pm and edited subsequently

//Rather barebones Taxon class -- has a few variables, and is used for Ladderize and Linearize
public class Taxon implements Comparable<Object> {
	private String name;
	private String sequence;
	private int sequencestart;
	private int cluster;
	
	//basic constructor
	public Taxon(String n) {
		name = n;
		cluster=-1;
	}
	
	//simple get methods
	public String getName()     { return name; }
	
	public String getSimpleSequence() { return sequence; } // can be used a lot
	public String getSequence() { return sequence.replace("\n","") + "\n"; } //should not be used a lot
	public int getStart()       { return sequencestart; }
	public int getCluster()     { return cluster; }
	
	//set methods
	public void setStart(int a)       { sequencestart=a; }
	public void setSequence(String s) { 
		sequence = s; }
	public void setCluster(int c)     { cluster=c; }
	
	//This gets the sequence in block format. n specifies the number of characters per line
	public String getSequence(int n) {
		String tbr = "";
		int i;
		for(i=n;i<sequence.length();i+=n) {
			tbr+=sequence.substring(i-n,i)+"\n";
		}
		tbr+=sequence.substring(i-n);
		
		return tbr;
	}
	
	//Cleans taxon name
	public boolean cleanName() {
		//name should be in format: gi#######
		//find the ###### part
		boolean numbers = false;
		int stindex = 0;
		int endindex=0;
		for(int i=0;i<name.length();i++) {
			if(Character.isDigit(name.charAt(i))) {
				if(numbers)
					endindex=i;
				else {
					numbers=true;
					stindex = i;
				}
			} else if(numbers) {
				name = "gi" + name.substring(stindex,endindex+1);
				return true;
			}
		}
		
		if(!numbers) return false; //numbers was never set, there is not a number and name cannot be cleaned.
		
		name = "gi" + name.substring(stindex); //only way this would happen if is # is at end
		return true;
	}
	
	//Gets the sequence in a block format. n specifices number of characters per line. a specifies starting point, b ending point.
	//ex. getSequence(60,30,370) will return all of the sequence from position 30 to 370 with 60 characters per line.
	public String getSequence(int n, int a, int b) {
		String tbr = "";
		int i;
		String s = sequence.substring(a,b+1);
		if(n==0)
			return s;
		for(i=n;i<s.length();i+=n) {
			tbr+=s.substring(i-n,i)+"\n";
		}
		tbr+=s.substring(i-n);
		
		return tbr;
	}

	public String getName(int max) {
		String n = new String(name);
		while(n.length()<max) {
			n+=" ";
		}
		return n;
	}
	//compares sequences based on where they start (first non-gap character)
	public int compareTo(Object arg0) {
		if(arg0 == null) throw new NullPointerException("Cannot compare to a null");
		if(!(arg0 instanceof Taxon)) throw new ClassCastException("Object " + arg0.toString() + " is not a Taxon.");
		else
			return this.getStart() -((Taxon)arg0).getStart();
	}
	
	//
	public boolean hasCluster() {
	    return cluster>0;
	}

	public boolean hasSequence(int a, int b) {
		return ((sequence.substring(a,b+1).indexOf("A"))!=-1) ||
				((sequence.substring(a,b+1).indexOf("C"))!=-1) ||
				((sequence.substring(a,b+1).indexOf("G"))!=-1) ||
				((sequence.substring(a,b+1).indexOf("T"))!=-1) ||

				((sequence.substring(a,b+1).indexOf("B"))!=-1) ||
				((sequence.substring(a,b+1).indexOf("D"))!=-1) ||
				((sequence.substring(a,b+1).indexOf("H"))!=-1) ||
				((sequence.substring(a,b+1).indexOf("V"))!=-1) ||
				((sequence.substring(a,b+1).indexOf("U"))!=-1) ||
				
				((sequence.substring(a,b+1).indexOf("R"))!=-1) ||
				((sequence.substring(a,b+1).indexOf("Y"))!=-1) ||
				((sequence.substring(a,b+1).indexOf("M"))!=-1) ||
				((sequence.substring(a,b+1).indexOf("K"))!=-1) ||
				((sequence.substring(a,b+1).indexOf("W"))!=-1) ||
				((sequence.substring(a,b+1).indexOf("S"))!=-1) ||
				
				((sequence.substring(a,b+1).indexOf("N"))!=-1);
	}
}
