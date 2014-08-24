package Finder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import Syntax.*;
import Syntax.Process;

public class UnmatchedEP_Finder {
	Program program;
	int tracker[];
	//HashMap<Pair<Integer,Integer>, LinkedList<Send>> unvisitedSends;
	HashMap<Integer, HashMap<Integer,Integer>> sendNums;
	HashMap<Integer, HashMap<Integer,Integer>> recvNums;
	
	
	UnmatchedEP_Finder(Program p)
	{
		program = p;
		tracker = new int[p.size()];
		sendNums = new HashMap<Integer, HashMap<Integer, Integer>>();
		recvNums = new HashMap<Integer, HashMap<Integer, Integer>>();
	}
	
	void Run() throws Exception
	{
		HashSet<UnmatchedEP_Pattern> patterns = program.getUnmatchedEP_Pattern();
		Iterator<UnmatchedEP_Pattern> it = patterns.iterator();
		
		if(!it.hasNext())
		{
			//report no deadlock for unmatched ep pattern
			System.out.printf("No deadlock is found for unmatched Endpoint patterns!\n");
		}
		
		
		while(it.hasNext())
		{
			UnmatchedEP_Pattern pattern = it.next();
			Process patternProcess = pattern.process;
			
//			LinkedList<LinkedList<Operation>> prefix = new LinkedList<LinkedList<Operation>>();
			
			//initialize the prefix
			for(int i =0; i < program.size();i++)
			{
//				LinkedList<Operation> list = new LinkedList<Operation>();
//				LinkedList<Operation> ops;
				if(i != patternProcess.getRank()){
					program.get(i).NextBlockPoint();
				}
				else 
				{
					patternProcess.ToPoint(pattern.determinstic);
				}
				
//				list.addAll(ops);
				//initialize trackers to the first line of each process
				tracker[i] = 0;
				
//				prefix.add(list);
			}
			
			//scheduling(program,patternProcess);
			
			//judge prefix's feasibility 
			while(schedulable(pattern))
			{	
				scheduling(program,patternProcess);
				if(reachBlockPoints())
				{
					HashSet<Integer> reachableRanks = feasible(patternProcess);
					if(reachableRanks.isEmpty())
					{
						//may deadlock 
						//apply SMT here to check the feasibility of prefix
						//if it is feasible, deadlock for this pattern
						//if not, no deadlock for this pattern(could be A, deadlock in the prefix; or B, there is no deadlock)
						//A
						// 0      1       2
						//S(2)   R(*)    R(*)
						//       S(2)    S(1)
						//               R(0)
						//--------------------
						//<R(2)>          ...
						
						//B
						// 0      1       2
						//R(*)   R(*)    S(0)
						//S(1)   S(0)    
						//--------------------
						//<R(1)>          ...

						System.out.printf("May Deadlock!\n");
						//System.exit(0);
						return;
					}
					
					if(reachableRanks.contains(patternProcess.getRank()))
					{
						//report no deadlock for this pattern
						//could be deadlock in the prefix, see A above
						System.out.printf("No deadlock is found for this pattern: (" 
								+ pattern.determinstic.toString() + ")\n");
						break;
					}
					
					moveBlockPoints(reachableRanks,patternProcess);
				}
			}
			
			//reset the indicator for the next check;
			resetProgram();
			
		}
	}
	
	void resetProgram() throws Exception
	{
		for(int i = 0; i < program.size();i++)
		{
			program.get(i).resetIndicator();
		}
	}
	
	boolean reachBlockPoints() throws Exception
	{
		for(int i = 0; i < program.size(); i++)
		{
			if(tracker[i] < program.get(i).indicator )
					//|| (program.get(i).indicator == 0 && tracker[i] == 0))
				return false;
		}
		
		return true;
	}
	
	boolean schedulable(UnmatchedEP_Pattern pattern) throws Exception
	{
		for(int i = 0; i < program.size(); i++)
		{
			Process process = program.get(i);
			int rank = process.getRank();
			//if process does not reach the end of each process
			if(tracker[rank] < process.indicator || (process.indicator == 0 && tracker[rank] == 0))
			{
				Operation op = program.get(rank).get(tracker[rank]);
				//send still exist
				if(op instanceof Recv)
				{
					Recv rv = (Recv)op;
					if(checkAvailable(rv))
						return true;
				}
				else 
				{
					return true;
				}
				
			}
		}
		
		System.out.printf("No deadlock is found for this pattern: (" 
				 + pattern.determinstic.toString() + ")\n");
		
		//when false, is it a deadlock for the prefix?
		return false;
	}
	
	boolean checkAvailable(Recv r)
	{
		int src = r.src;
		int dest = r.dest;
		
		//more sends than receives with idential src and dest
		if(src!=-1)
			return (totalNUM(sendNums,src,dest) >= totalNUM(recvNums,src,dest));
		else 
		{
			//for wildcard receive, the number of send(*->dest) has to be greater or equal to the number 
			//of {recv(*->dest), recv(c1->dest), ...}
			if(recvNums.containsKey(dest))
			{
				int totalAvailableRecvs = 0;
				for(Integer rsrc : recvNums.get(dest).keySet())
					totalAvailableRecvs += recvNums.get(dest).get(rsrc);
				//should use ">" other than ">=" because 
				//at least one send is available for the next receive
				return (totalNUM(sendNums,src,dest) > totalAvailableRecvs);
			}
			
			return false;
		}
	}
	
	int totalNUM(HashMap<Integer, HashMap<Integer, Integer>> map, int src, int dest)
	{
		if(map.containsKey(dest))
		{
			if(map.get(dest).containsKey(src))
			{
				return map.get(dest).get(src);
			}
			
//			if(src == -1)
//			{
//				int total = 0;
//				for( Integer size : map.get(dest).values())
//				{
//					total += size;
//				}
//				return total;
//			}
		}
		return 0;
	}
	
	boolean scheduling(Program program, Process patternProcess) throws Exception
	{
		int patternRank = patternProcess.getRank();
//		if(tracker[patternRank] >= patternProcess.indicator)
//		{
//			return false;
//		}
		
		for(int i = patternRank+1; i < patternRank + program.size()+1; i++)
		{
			int rank = i % program.size();
			if(tracker[rank] == 0 && program.get(rank).indicator == 0)
			{
				Operation op = program.get(rank).get(tracker[rank]);
				//generate entry in recvNums for a indicator receive
				if(op instanceof Recv)
				{
					Recv rv = (Recv)op;
					int src = rv.src;
					int dest = rv.dest;
					
					if(!recvNums.containsKey(dest))
						recvNums.put(dest, new HashMap<Integer, Integer>());
					
					if(!recvNums.get(dest).containsKey(src))
						recvNums.get(dest).put(src, 0);
					
					continue;	
				}
			}
			
			//process has no operation left until indicator
			if(tracker[rank] >= program.get(rank).indicator)
			{
				continue;
			}
				
			Operation op;
			while(tracker[rank] < program.get(rank).indicator)
			{
				op = program.get(rank).get(tracker[rank]);
				if(op instanceof Send){
					Send sendop = (Send)op;
					int dest = sendop.dest;
					int src = sendop.src;
					
					if(!sendNums.containsKey(dest))
					{
						sendNums.put(dest, new HashMap<Integer,Integer>());
					}
					
					if(!sendNums.get(dest).containsKey(src))
					{
						sendNums.get(dest).put(src, 0);
					}
					
					if(!sendNums.get(dest).containsKey(-1))
					{
						sendNums.get(dest).put(-1, 0);
					}
					
					//increment the size of send in sendNums
					sendNums.get(dest).put(src, sendNums.get(dest).get(src)+1);
					sendNums.get(dest).put(-1, sendNums.get(dest).get(-1)+1);
					tracker[rank]++;
				}
				
				if(op instanceof Recv)
				{
					Recv rv = (Recv)op;
					int src = rv.src;
					int dest = rv.dest;
					
					if(!recvNums.containsKey(dest))
					{
						recvNums.put(dest, new HashMap<Integer, Integer>());
						
//						if(src != -1)
//						{
//							recvNums.get(dest).put(-1, 0);
//						}
					}
					
					if(!recvNums.get(dest).containsKey(src))
					{
						recvNums.get(dest).put(src, 0);
					}
					
					//increment the size of recv in recvNums
					if(checkAvailable(rv))
					{
						recvNums.get(dest).put(src, recvNums.get(dest).get(src)+1);
//						if(src != -1)
//							recvNums.get(dest).put(-1, recvNums.get(dest).get(-1)+1);
						tracker[rank]++;
					}
					else //when recv can not be matched, scheduling stops for this process
					{
						break;
					}
					
				}
			}
			
		}
		
		return true;
		
		
	}
	
	boolean mayDeadlock(Recv deadlockPoint)
	{
		//src must not be equal to -1
		
		int src = deadlockPoint.src;
		int dest = deadlockPoint.dest;
		
		int sendNum = 0;
		int recvNum = 0;
		if(sendNums.containsKey(dest))
		{
			if(sendNums.get(dest).containsKey(src))
			{
				sendNum = sendNums.get(dest).get(src);
			}
		}
		
		if(recvNums.containsKey(dest))
		{
			if(recvNums.get(dest).containsKey(-1))
				recvNum += recvNums.get(dest).get(-1);
			
			if(recvNums.get(dest).containsKey(src))
				recvNum += recvNums.get(dest).get(src);
		}
		
		return sendNum <= recvNum;
	}
	
	HashSet<Integer> feasible(Process patternProcess) throws Exception
	{
		HashSet<Integer> reachableRanks = new HashSet<Integer>();
		for(int i = 0; i < program.size(); i++)
		{
			Process process = program.get(i);
			if(process.indicator < process.size())
			{
				Operation blockOp = process.get(process.indicator);
				//for the pattern process
				if(i == patternProcess.getRank())
				{
					Recv deadlockPoint = (Recv)blockOp;
					if(!mayDeadlock(deadlockPoint))
					{
						reachableRanks.add(i);
						return reachableRanks;
					}
					
					continue;
				}
				
				//check if each block receive has no sends to match
				if(blockOp instanceof Recv)
				{
					Recv rv = (Recv)blockOp;
					if(checkAvailable(rv))
					{
						reachableRanks.add(i);
					}
				}
				
				//(done!)need to reconsider the availablility for R(1) in the pattern process,
				//(done!)also need to consider when send for 1 is larger 
				//enough (i.e., the previous receives is less than s(1)), abort this iteration for this pattern
			}
		}
		return reachableRanks;
	}
	
	void moveBlockPoints(HashSet<Integer> reachableRanks, Process patternProcess) throws Exception
	{
		for(int rank : reachableRanks)
		{
			if(rank != patternProcess.getRank())
			{
				Operation op = program.get(rank).get(program.get(rank).indicator);
				//generate entry in recvNums for a indicator receive
//				if(op instanceof Recv)
//				{
//					Recv rv = (Recv)op;
//					int src = rv.src;
//					int dest = rv.dest;
//					
//					if(!recvNums.containsKey(dest))
//						recvNums.put(dest, new HashMap<Integer, Integer>());
//					
//					if(!recvNums.get(dest).containsKey(src))
//						recvNums.get(dest).put(src, 0);
//					
//					//since op is proved available in feasible(), so just add it to recvNums
//					recvNums.get(dest).put(src,recvNums.get(dest).get(src)+1);
//				}
				program.get(rank).indicator++;
//				tracker[rank]++;
				program.get(rank).NextBlockPoint();
			}
		}
	}
		
}
