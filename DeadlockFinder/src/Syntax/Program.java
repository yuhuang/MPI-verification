package Syntax;

import java.util.HashSet;
import java.util.LinkedList;

public class Program {
	public LinkedList<Process> processes;
	
	public Program()
	{
		processes = new LinkedList<Process>();
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
	
//	public void print()
//	{
//		
//		
//		for(int i = 0; i < this.size(); i++)
//		{
//			
//		}
//	}
}
