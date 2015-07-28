package hbv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class MakeClusters {
	private static List<Sequence> seqlist;
	
	private static final int CLUSTERDISTANCE = 8;
	private static final int GENOMECUTOFF = 3002;
	private static final String filename = "Cluster0.ed.100bp.fasta\\Cluster0.ed.100bp.fasta";
	private static final String outputname = "G" + GENOMECUTOFF + "CD" + CLUSTERDISTANCE;
	private static boolean output = false;
	private static boolean removegenomes = true;
	
	public static void main(String[] args) {
		(new File(outputname)).mkdir();
		BufferedWriter out2=null;
		try {
			out2 = new BufferedWriter(new FileWriter(outputname+"\\genomes.fasta"));
		} catch(IOException ie) {
			ie.printStackTrace();
		}
	
		//input file into a list
		seqlist = new ArrayList<Sequence>();
		File file = new File(filename);
		try {
			Scanner scan = new Scanner(file);
			String tempg = "";
			String temps = "";
			while(scan.hasNext()) {
				tempg = scan.nextLine();
				temps = scan.nextLine();
				seqlist.add(new Sequence(tempg.substring(1),temps));
				if(temps.length()>GENOMECUTOFF) {
					out2.append(tempg+"\n"+temps+"\n");
				}
			}
		} catch(IOException ioe) {
			
		}
		
		/*Collections.sort(seqlist);
		//System.out.println(seqlist);
		
		
		Sequence prev = null;
		Sequence curr = null;
		List<List<Sequence>> clusterList = new ArrayList<List<Sequence>>();
		int iterator = 0;
		for(Sequence s: seqlist) {
			if(prev == null) {
				prev = s;
				clusterList.add(new ArrayList<Sequence>());
				clusterList.get(iterator).add(prev);
				continue;
			}
			curr = s;
			if(curr.compareTo(prev)>CLUSTERDISTANCE) {
				iterator++;
				clusterList.add(new ArrayList<Sequence>());
			}

			clusterList.get(iterator).add(curr);
			prev=curr;
		}
		
		System.out.println(clusterList);
		System.out.println(clusterList.size());
		System.out.println(seqlist.size());
		int min=60000;
		int max=0;
		int sum=0;
		int div=0;
		int one=0;
		int twototen=0;
		List<Integer> seqlengthspread = new ArrayList<Integer>();
		for(List<Sequence> l: clusterList) {
			int size = l.size();
			if(size<min) min=size;
			if(size>max) max=size;
			sum+=size;
			div++;
			if(size==1) one++;
			if(1<size && size<11) twototen++;
			
			int smin = 600000;
			int smax = 0;
			for(Sequence s:l) {
				int len = s.getSeq().length();
				if(len>smax) smax=len;
				if(len<smin) smin=len;
			}
			if(l.size()>1) seqlengthspread.add(smax-smin);
			
		}
		
		System.out.println("MIN: " + min + "; MAX: " + max + "\nMEAN: " + ((double)sum/(double)div) + "\nONES: " + one + "; TWO TO TEN: " + twototen + "\nOVER TEN: " + (clusterList.size()-one-twototen));
		div=0;
		sum=0;
		for(Integer inte: seqlengthspread) {
			sum+=inte;
			div++;
		}
		System.out.println("MEAN SEQ SPREAD: " + ((double)sum/(double)div));
		
		System.out.println(seqlengthspread);
		FileWriter fstr;
		BufferedWriter out = null;
		
		*/
		//BELOW: Outputting the sequence spread chart to text file
		/*try {
			fstr = new FileWriter(outputname+"seqspread.txt");
			out = new BufferedWriter(fstr);
			for(Integer inte : seqlengthspread) {
				out.append(inte+"\n");
			}
			out.close();
			fstr.close();
		} catch(IOException ie) {
			ie.printStackTrace();
		}*/
		
		/*if(output) {
		int i = 1;
		int j = 1;
		int k=1;
		(new File(outputname+"\\singletons")).mkdirs();
		(new File(outputname+"\\genomes")).mkdirs();
		(new File(outputname+"\\clusters")).mkdirs();
		for(List<Sequence> l: clusterList) {
			try {
				if(findMaxSequenceSize(l)>GENOMECUTOFF) {
					fstr = new FileWriter(outputname+"\\genomes\\GenomeCluster"+(k++)+".fasta");
					out = new BufferedWriter(fstr);
					for(Sequence s:l) {
						out2.append(s+"\n");
						out.append(s.output());
					}
				}
				else if(l.size()>1) {
					fstr = new FileWriter(outputname+"\\clusters\\Cluster"+(i++)+".fasta");
					out = new BufferedWriter(fstr);
				
					for(Sequence s: l) {
						out.append(s.output());
					} 
				}
				else {
					fstr = new FileWriter(outputname+"\\singletons\\Sequence"+(j++)+".fasta");
					out = new BufferedWriter(fstr);
					out.append(l.get(0).output());
				}
				out.close();
				fstr.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}}*/
		
		try {
			out2.close();
		} catch(IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private static int findMaxSequenceSize(List<Sequence> l) {
		int maxsize =0;
		for(Sequence s: l) {
			if(s.getSeq().length()>maxsize) maxsize=s.getSeq().length();
		}
		
		return maxsize;
	}
}
