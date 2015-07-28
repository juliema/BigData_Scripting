
public class RGTaxon extends Taxon {
	public RGTaxon(String n) {
		super(n);
		counter=0;
	}
	
	private int counter;
	
	public int getCounter() { return counter; }
	
	public void incrementCounter() { counter++; }
	
	private StringBuilder correctedSequence;
	
	public String getSequence() {
		return super.getSequence();
	}

	//must end with a \n because Taxon's getSequence method does.
	public String getCorrectedSequence() {
		if(correctedSequence!=null)
			return correctedSequence.toString();
		return super.getSequence();
	}
	
	public void correctSequence(boolean[] notgaps) {
		correctedSequence = new StringBuilder(this.getSequence());
		for(int i=notgaps.length-1;i>=0;i--) {
			//only keep sites with "true"
			if(!notgaps[i]) correctedSequence.deleteCharAt(i);
		}
	}
	
}
