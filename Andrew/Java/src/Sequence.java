package hbv;

public class Sequence implements Comparable {
	private int GI;
	private String sequence;
	
	public Sequence(String g, String s) {
		GI = Integer.parseInt(g);
		sequence=s;
	}
	
	public Sequence(int g, String s) {
		GI = g;
		sequence=s;
	}
	
	public int compareTo(Object o) {
		if(o instanceof Sequence) {
			return this.compareTo((Sequence)o);
		}
		else return 0;
	}
	
	public int compareTo(Sequence s) {
		return GI-s.getGI();
	}
	
	public String toString() { return ""+GI; }
	
	public int getGI() { return GI; }
	public String getSeq() { return sequence; }
	
	public String output() {
		return ">"+GI+"\n"+sequence+"\n";
	}
}
