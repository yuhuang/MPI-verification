package Syntax;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

public class Program {
	public LinkedList<Process> processes;
	public Hashtable<Recv, LinkedList<Send>> match_table;
	public Hashtable<Send, LinkedList<Recv>> pattern_match;
	boolean isPattern;
	public LinkedList<Recv>[] recvlist;
	public LinkedList<Send>[][] sendlist;
	
	public Program(boolean mismatchedEndpoint)
	{
		isPattern = mismatchedEndpoint;
		processes = new LinkedList<Process>();
		match_table = new Hashtable<Recv, LinkedList<Send>>();
		pattern_match = new Hashtable<Send, LinkedList<Recv>>();
	}
	
	public boolean isPattern()
	{
		return isPattern;
	}
	
	public void add(Process proc)
	{
		processes.add(proc);
	}
	
	public HashSet<UnmatchedEP_Pattern> getUnmatchedEP_Pattern()
	{
		HashSet<UnmatchedEP_Pattern> set = new HashSet<UnmatchedEP_Pattern>();
		for(Process p: processes)
		{
			if(p.hasDeterminsticRecv())
			{
				set.addAll(p.getUnmatchedEPPatternSet());
			}
		}
		
		return set;
	}
	
	public Process get(int i) throws Exception
	{
		if(i >= processes.size())
			throw new Exception(i + " is too large!");
		
		return processes.get(i);
	}
	
	public int size()
	{
		return processes.size();
	}
	
	public void InitGraph()
	{
		for(Process process : processes)
		{
			process.generateVE();
		}
		generateMatch();
	}
	
	public void generateMatch()
	{
		recvlist = new LinkedList[this.size()];
		sendlist = new LinkedList[this.size()][this.size()];
		//store the rlist and slist of each process 
		for(Process process: processes)
		{
			recvlist[process.getRank()] = process.rlist;
			for(Integer dest : process.slist.keySet())
			{
				sendlist[dest][process.getRank()] = process.slist.get(dest);
			}
		}
		
		for(int i = 0; i < recvlist.length; i ++)
		{
			 Iterator<Recv> ite_r = recvlist[i].iterator();
			 
			 //calculate # of sends from any source to i
			 int sendstoi = 0;
			 for(int j = 0; j < sendlist[i].length; j++){
				 if(sendlist[i][j] != null)
					 sendstoi += sendlist[i][j].size();
			 }
			 
			 while(ite_r.hasNext()){
				 Recv r = ite_r.next();
				 
				//	 System.out.println("recv in thread i = " + i + ": " + r.exp +" with rank " + r.rank);
				 LinkedList<Send> sendlistforR = new LinkedList<Send>();
				 for(int j = 0; j < sendlist[i].length; j++){
					 //no sends in sendlist[i][j]
					 if(sendlist[i][j] == null)
						 continue;
					 Iterator<Send> ite_s = sendlist[i][j].iterator();
					 while(ite_s.hasNext()){
						 Send s = ite_s.next();
						 //compare and set
						 if(r.rank >= s.rank //rule 1
								 &&r.rank <= s.rank + (sendstoi - sendlist[i][j].size())//rule2
								 &&(r.src == -1 || r.src == s.src)){ //either it is a wildcard receive or the endpoints commit
							 sendlistforR.add(s);
							 
							 //generate pattern_match
							 if(isPattern)
							 {
//								 //check if the send is pattern send
//								 if(s.dest == pattern.process.getRank() 
//										 && s.src == pattern.determinstic.src)
//								 {
								 if(!pattern_match.containsKey(s))
								 {
									 pattern_match.put(s, new LinkedList<Recv>());
								 }
								 pattern_match.get(s).add(r);
							 }
						 }
					 }
				 }
				 //add the list with r to the match table
				 if(!sendlistforR.isEmpty())
					 match_table.put(r, sendlistforR);
			 }
		 }
	}
	
}
