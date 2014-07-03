//Rather barebones Taxon class -- has a few variables, and is used for Ladderize and Linearize
public class Taxon implements Comparable<Object> {
	private String name;
	private String sequence;
	private int sequencestart;
	
	//basic constructor
	public Taxon(String n) {
		name = n;
	}
	
	//simple get methods
	public String getName() { return name; }
	public String getSequence() {return sequence + "\n"; }
	public int getStart() {return sequencestart; }
	
	//set methods
	public void setStart(int a) { sequencestart=a; }
	public void setSequence(String s) { sequence=s; }
	
	//This gets the sequence in interleaved format. n specifies the number of characters per line
	public String getSequence(int n) {
		String tbr = "";
		int i;
		for(i=n;i<sequence.length();i+=n) {
			tbr+=sequence.substring(i-n,i)+"\n";
		}
		tbr+=sequence.substring(i-n);
		
		return tbr;
	}

	//compares sequences based on where they start (first non-gap character)
	public int compareTo(Object arg0) {
		if(arg0 == null) throw new NullPointerException("Cannot compare to a null");
		if(!(arg0 instanceof Taxon)) throw new ClassCastException("Object " + arg0.toString() + " is not a Taxon.");
		else
			return this.getStart() -((Taxon)arg0).getStart();
	}
}
