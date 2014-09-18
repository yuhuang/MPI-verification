package Finder;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;

import Syntax.*;
import Syntax.Process;

public class Digraph 
{
	public Vector<V> Vlist;
	public Hashtable<V, Set<E>> Etable;
	public Program program;
	public Hashtable<Recv, LinkedList<Send>> matches;
	int Vrank = 0;
	
	public Digraph(Program p)
	{
		program = p;
		Vlist = new Vector<V>();
		Etable = new Hashtable<V, Set<E>>();
		matches = new Hashtable<Recv, LinkedList<Send>>();
		
		initGraph();
	}
	
	public void initGraph()
	{
		generateV();
		generateMatch();
		generateE();
	} 
	
	public void generateMatch()
	{
		//do not need to declare all the recelists and sendlists
				//TODO: consider revise
		LinkedList[] recvlist = new LinkedList[program.size()];
		for(int i = 0; i < program.size(); i++){
			recvlist[i] = new LinkedList<Recv>();
		}
		LinkedList[][] sendlist = new LinkedList[program.size()][program.size()];
		for(int i = 0; i < program.size(); i++){
			for(int j = 0; j < program.size(); j++){
				sendlist[i][j] = new LinkedList<Send>();
			}
		}	
		 
		 //set up recvlist and sendlist
		 for(Process process: program.processes)
		 {
			 for(int i = 0; i < process.size(); i++)
			 {
				 Operation op = process.get(i);
				 if(op instanceof Send)
				 {
					 Send send = (Send)op;
//					 if(sendlist[send.dest][send.src] == null)
//						 sendlist[send.dest][send.src] = new LinkedList<Send>();
					 sendlist[send.dest][send.src].add(send);
				 }
				 if(op instanceof Recv)
				 {
					 Recv recv = (Recv)op;
//					 if(recvlist[recv.dest] == null)
//						 recvlist[recv.dest] = new LinkedList<Recv>();
					 recvlist[recv.dest].add(recv);
				 }
			 }
		 }
		
		 for(int i = 0; i < recvlist.length; i ++){
			 Iterator<Recv> ite_r = recvlist[i].iterator();
			 
			 //calculate # of sends from any source to i
			 int sendstoi = 0;
			 for(int j = 0; j < sendlist[i].length; j++){
//				 if(sendlist[i][j] != null)
					 sendstoi += sendlist[i][j].size();
			 }
			 
			 while(ite_r.hasNext()){
				 Recv r = ite_r.next();
				 
				//	 System.out.println("recv in thread i = " + i + ": " + r.exp +" with rank " + r.rank);
				 LinkedList<Send> sendlistforR = new LinkedList<Send>();
				 for(int j = 0; j < sendlist[i].length; j++){
					 Iterator<Send> ite_s = sendlist[i][j].iterator();
					 while(ite_s.hasNext()){
						 Send s = ite_s.next();
						 //compare and set
						 if(r.rank >= s.rank //rule 1
								 &&r.rank <= s.rank + (sendstoi - sendlist[i][j].size())//rule2
								 &&(r.src == -1 || r.src == s.src)){ //either it is a wildcard receive or the endpoints commit
							 sendlistforR.add(s);
							 
							 //System.out.println("match (" + r.exp + " " + s.exp+"), total num of sends: " + sendstoi +", and total num of sends for ep " + i + ": " +  sendlist[i][j].size());
						 }
					 }
				 }
				 //add the list with r to the match table
				 if(!sendlistforR.isEmpty())
					 matches.put(r, sendlistforR);
			 }
		 }
	}

	
	
	public Set<E> adj(V v)
	{
		if(!Etable.containsKey(v))
			return null;
		return Etable.get(v);
 	}
	
	public int getVSize()
	{
		return Vlist.size();
	}
	
	public void generateV()
	{
		
		BitSet wildcardissued = new BitSet(program.size());
		BitSet sendissued = new BitSet(program.size());
		for(Process process: program.processes)
		{
			continuepoint:
			for(int i = 0; i < process.size(); i++)
			{
				//now consider all the receives (wildcard, determinstic)
				//TODO: 1) for any receive r and a sequence of sends with identical src and dest,
				//only one vertex for r and the first send in this sequence needs to be generated
				//2) for any deterministic receive r, if a wildcard receive is issued before r, 
				//r is not considered in any vertex (it is only considered in mismatched endpoint pattern)
				if(process.get(i) instanceof Recv)
				{
					Recv r = (Recv)process.get(i);
					if(r.src == -1)
						wildcardissued.set(process.getRank());
				
					if(r.src != -1 && wildcardissued.get(process.getRank()))
					{
						//this case is only considered in mismatched endpoint pattern
						continue continuepoint;
					}
					
					sendissued.clear();
					for(int j = i; j < process.size(); j++)
					{
						if(process.get(j) instanceof Send)
						{
							Send s = (Send)process.get(j);
							if(!sendissued.get(s.dest))
								Vlist.add(V.generateV(r, s));
							//mark a send from src to dest is issued
							//so no need to generate another vertex for sends with src and dest
							sendissued.set(s.dest);

						}
					}
				}
			}
		}
	}
	
	public void generateE()
	{
		//use match pairs
		for(V v: Vlist)
		{
			Recv r = v.getRecv();
			LinkedList<Send> matchedS = matches.get(r);
			for(Send matcheds : matchedS)
			{
				for(V matchedv : Vlist)
				{
					if(matchedv.send.equals(matcheds))
					{
						if(!Etable.containsKey(matchedv))
							Etable.put(matchedv, new HashSet<E>());
						
						E newE = E.generateE(matchedv, v);
						Etable.get(matchedv).add(newE);
						System.out.println(matchedv + " add Edge " + newE);
					}
				}
			}	
		}
	}
}
