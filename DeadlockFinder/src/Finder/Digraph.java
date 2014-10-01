package Finder;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;

import Syntax.*;
import Syntax.Process;

public class Digraph 
{
	public Vector<Operation> Vlist;
//	public Hashtable<Operation, Set<E>> Etable;
	public Hashtable<Operation, Set<Operation>> Etable;
	public Program program;
//	public Hashtable<Recv, LinkedList<Send>> matches;
	int Vrank = 0;
	
	public Digraph(Program p)
	{
		program = p;
		Vlist = new Vector<Operation>();
		Etable = new Hashtable<Operation, Set<Operation>>();
//		matches = p.match_table;
		
		initGraph();
	}
	
	public void initGraph()
	{
		generateGraph();
	} 
	
	
	
	public Set<Operation> adj(Operation v)
	{
		if(!Etable.containsKey(v))
			return null;
		return Etable.get(v);
 	}
	
	public int getVSize()
	{
		return Vlist.size();
	}
	
	public void generateGraph()
	{
		for(Process process: program.processes)
		{
			//add all vertices and partial hb relations from each process
			process.generateVE();
			Vlist.addAll(process.vertices);
			Etable.putAll(process.HB);
		}
		
		//add match relation as edge for vertices
		Hashtable<Recv, LinkedList<Send>> match_table = program.match_table;
		continuepoint:
		for(Operation op : Vlist)
		{
			if(!(op instanceof Recv))
			{
				continue continuepoint;
			}
			
			Recv r = (Recv)op;
			if(!match_table.containsKey(r))
			{
				continue continuepoint;
			}
			
			continuepoint1:
			for(Send s : match_table.get(r))
			{
				if(!Vlist.contains(s))
				{
					continue continuepoint1;
				}
				
				if(!Etable.containsKey(s))
					Etable.put(s, new HashSet<Operation>());
				Etable.get(s).add(r);
			}
		}
	}
	
//	public void generateV()
//	{
//		
//		BitSet wildcardissued = new BitSet(program.size());
//		BitSet sendissued = new BitSet(program.size());
//		for(Process process: program.processes)
//		{
//			continuepoint:
//			for(int i = 0; i < process.size(); i++)
//			{
//				//now consider all the receives (wildcard, determinstic)
//				//TODO: 1) for any receive r and a sequence of sends with identical src and dest,
//				//only one vertex for r and the first send in this sequence needs to be generated
//				//2) for any deterministic receive r, if a wildcard receive is issued before r, 
//				//r is not considered in any vertex (it is only considered in mismatched endpoint pattern)
//				if(process.get(i) instanceof Recv)
//				{
//					Recv r = (Recv)process.get(i);
//					if(r.src == -1)
//						wildcardissued.set(process.getRank());
//				
//					if(r.src != -1 && wildcardissued.get(process.getRank()))
//					{
//						//this case is only considered in mismatched endpoint pattern
//						continue continuepoint;
//					}
//					
//					sendissued.clear();
//					for(int j = i; j < process.size(); j++)
//					{
//						if(process.get(j) instanceof Send)
//						{
//							Send s = (Send)process.get(j);
//							if(!sendissued.get(s.dest))
//								Vlist.add(V.generateV(r, s));
//							//mark a send from src to dest is issued
//							//so no need to generate another vertex for sends with src and dest
//							sendissued.set(s.dest);
//
//						}
//					}
//				}
//			}
//		}
//	}
	
//	public void generateE()
//	{
//		//use match pairs
//		for(V v: Vlist)
//		{
//			Recv r = v.getRecv();
//			LinkedList<Send> matchedS = matches.get(r);
//			for(Send matcheds : matchedS)
//			{
//				for(V matchedv : Vlist)
//				{
//					if(matchedv.send.equals(matcheds))
//					{
//						if(!Etable.containsKey(matchedv))
//							Etable.put(matchedv, new HashSet<E>());
//						
//						E newE = E.generateE(matchedv, v);
//						Etable.get(matchedv).add(newE);
////						System.out.println(matchedv + " add Edge " + newE);
//					}
//				}
//			}	
//		}
//	}
}
