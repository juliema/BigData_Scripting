// these are at least a little wrong... fix in Eclipse
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class TaxonSubsection {
    //args[0] = sequence file (FASTA)
    //args[1] = threshold (proportion)
    //args[2] = all (all clusters) or a number (# randomly selected sequences from each cluster)
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
		
        
        List<Taxon> taxa = new ArrayList<Taxon>();
        
        String line="";
        String seq="";
        Taxon temp=null;
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
        
        //- set threshold
        double threshold = Double.valueOf(args[1]);
        
        //- Use pairwisedistance(one,two) to get distance
        int cluster_count = 1;
        for(int i=0;i<taxa.size();i++) {
            for(int j=0;j<i;j++) { 
                double pwd = pairwisedistance(taxa.get(j),taxa.get(i));
                if(pwd<=threshold) {
                    //same cluster
                    if(taxa.get(i).hasCluster() && taxa.get(j).hasCluster()) {
                        if(taxa.get(i).getCluster() != taxa.get(j).getCluster()) {
                            //merge cluster
                            mergeClusters(taxa,i,taxa.get(i).getCluster(),taxa.get(j).getCluster());
                        }
                            
                        
                    }
                    if(taxa.get(j).hasCluster()) {
                        taxa.get(i).setCluster(taxa.get(j).getCluster());
                    } else {
                        taxa.get(i).setCluster(cluster_count);
                        taxa.get(j).setCluster(cluster_count);
                        cluster_count++;
                    }
                } else {
                    //these sequences are far from each other. if they have a cluster, do nothing;
                    //if they do not, assign them cluster_count and increment it
                    if(!taxa.get(i).hasCluster()) {
                        taxa.get(i).setCluster(cluster_count++);
                    }
                    
                    if(!taxa.get(j).hasCluster()) {
                        taxa.get(j).setCluster(cluster_count++);
                    }
                }
            }
        }
        //i = 0, j = 0 -> nothing
        //i = 1, j = 0 -> compare 1 and 0 -> close, cluster=1
        //i = 2, j = 0 -> compare 2 and 0 -> close, cluster=1
        //i = 2, j = 1 -> compare 2 and 1 -> far, cluster=1 (because 2c=1 & 1c=1, but they are not close to each other)
        //i = 3, j = 0 -> compare 3 and 0 -> far, 3c = 2, 0c = 1
        //i = 3, j = 1 -> compare 3 and 1 -> close, 3c = 2, 1c = 1. what do? -> merge clusters
        
        //- what is the best way to do this? Need a somewhat heuristic method: doing all pairwise comparisons would take (N-1)*N/2 (for 30,000 sequences this is 449,985,000 comparisons, best case, which isn’t actually that inconceivable…) [but writing in O(n^2) makes me sad anyway so…]-> 
        
        //possible heuristic method:
        //   - take a sequence at random, assign it to "cluster 1"
        //   - loop through all other sequences:
        //      - do pairwise comparison with query. if under threshold, add it to cluster.
        //   - repeat this process with new random sequences.
        //   - once the "initial" list is empty, create consensus from each cluster. compare the pairwise distances of consensus sequences between clusters, and if the distance is under the threshold, combine those two clusters and create a new consensus, redo pairwise comparisons.
        
        //possible brute-force method:
        //   - perform all pairwise sequence comparisons:
        //      - next: could save all this data somewhere and use it later
        //      - or: if a comparison is under the threshold, put those two sequences into a cluster (possibly just use a cluster number variable that each sequence will have, and assign it as needed. can then use that number to compare and sort large list of sequences, so it is then in cluster-number-order.) If one sequence is in a cluster, add the other to that cluster. If both sequences are in a cluster already, merge those two clusters -- might require some tinkering since we wouldn’t be keeping track of which sequences are in which cluster, but is definitely possible.
        
        //for now, I am going to generate some stats about clusters
        clusterStats(taxa);
        //- based on input, output each cluster or ONE file with a randomly selected X sequences from each cluster.
        
        
        
        
        
    }
    
    // returns pairwise distance of two sequences
    // # differences / # sites
    // gaps are not applicable.
    private static double pairwisedistance(Taxon s1, Taxon s2) {
        int count=0;
        int total=0;
        
        String a = s1.getSequence();
        String b = s2.getSequence();
        
        if(a==b) return 0; //if this is the same string or indexed in the same place, return 0.
                            //here, this is preferable to .equals() because it is more efficient
                            //and the .equals() comparison is done before. If they are equal,
                            //that's completely fine -- but if they aren't, we are doing double
                            //the number of comparisons needed.
        
        if(a.length() != b.length()) {
            //throw an error
        }
        
        for(int i=0;i<a.length();i++) {
            //if either character is a gap,
            if(a.charAt(i)!='-' && b.charAt(i) != '-') {
                total++;
                if(a.charAt(i)!=b.charAt(i)) {
                    count++;
                }
            }
        }
        
        if(total==0) {
            //rare case where the sequences cannot be compared at all.
            System.err.println("Sequences cannot be compared.");
            return 2.0;
        }
        
        return (double)(count)/(double)(total);
    }
    
    private static void mergeClusters(List<Taxon> list, int max, int a, int b) {
        int c = a<b ? a : b;
        int d = a<b ? b : a;
        
        for(int i=0;i<=max;i++) {
            if(list.get(i).getCluster() == d) list.get(i).setCluster(c);
        }
    }
    
    private static void clusterStats(List<Taxon> taxa) {
    	int mc = maxCluster(taxa);
    	System.out.println(mc);
    	int[] numPerCluster = new int[mc+1];
    	for(Taxon t:taxa) {
    		numPerCluster[t.getCluster()]++;
    	}
    	for(int i=0;i<numPerCluster.length;i++) {
    		if(numPerCluster[i]!=0) {
    			System.out.println("Cluster " + i + " has " + numPerCluster[i] + " sequences.");
    		}
    	}
    }
    
    private static int maxCluster(List<Taxon> taxa) {
    	int numcluster=0;
    	for(Taxon t:taxa) {
    		if(t.getCluster()>numcluster) {
    			numcluster=t.getCluster();
    		}
    	}
    	
    	return numcluster;
    }
    
}