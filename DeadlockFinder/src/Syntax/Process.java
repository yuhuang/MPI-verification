package Syntax;

import java.util.HashSet;
import java.util.LinkedList;

public class Process {
	int rank;
	LinkedList<Operation> ops;
	public int indicator;
	
	public Process(int rank)
	{
		this.rank = rank;
		indicator = 0;
		this.ops = new LinkedList<Operation>();
	}
	
	public void add(Operation op)
	{
		this.ops.add(op);
	}
	
	public void resetIndicator()
	{
		indicator = 0;
	}
	
	public Operation get(int i)
	{
		if(i >= ops.size())
			return null;
		
		return ops.get(i);
	}
	
	public void NextBlockPoint()
	{
		//LinkedList<Operation> visitedOPs = new LinkedList<Operation>();
		while(indicator < ops.size()){
			if(ops.get(indicator) instanceof Recv)//right now, only receives are considered as block points
				break;
			//visitedOPs.add(ops.get(indicator));
			indicator++;
		}
		
		//return visitedOPs;
	}
	
	public void ToPoint(Operation op)
	{
//		LinkedList<Operation> visitedOPs = new LinkedList<Operation>();
		while(indicator < ops.size()){
			if(ops.get(indicator).equals(op))//right now, only receives are considered as block points
				break;
//			visitedOPs.add(ops.get(indicator));
			indicator++;
		}
		
	}
	
	//quadratic algorithm
	public HashSet<UnmatchedEP_Pattern> getUnmatchedEPPatternSet()
	{
		HashSet<UnmatchedEP_Pattern> patterns = new HashSet<UnmatchedEP_Pattern>();
		boolean hasPred = false;
		for(Operation op : ops)
		{
			if(op instanceof Recv)
			{
				Recv dr = (Recv)op;
				if(dr.src == -1)
					hasPred = true;
				if(dr.src != -1 && hasPred)
					patterns.add(UnmatchedEP_Pattern.generatePattern(dr));
//				if(dr.src != -1)//dr is a determinstic receive
//				{
//					for(int i = 0; i <  ops.indexOf(op); i++)
//					{
//						if(ops.get(i) instanceof Recv)
//						{
//							Recv wr = (Recv)ops.get(i);
//							if(wr.src == -1)//wr is a wildcard receive
//							{
//								patterns.add(UnmatchedEP_Pattern.generatePattern(wr, dr));
//							}
//						}
//					}
//				}
			}
		}
		return patterns;
	}
	
	
	//linear algorithm
	public boolean hasDeterminsticRecv()
	{
		boolean result = false;
		for(Operation op : ops)
		{
			if(op instanceof Recv)
			{
				if(((Recv)op).src != -1)// r is a determinstic receive
					return true;
			}
		}
		
		return result;
	}
	
	public int getRank()
	{
		return rank;
	}
	
	public int size()
	{
		return ops.size();
	}
}
