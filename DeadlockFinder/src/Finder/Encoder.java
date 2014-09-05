package Finder;

import Syntax.*;
import Syntax.Process;

import com.microsoft.z3.*;

import java.util.*;

public class Encoder 
{
	SMTSolver solver;
	Program program;
	UnmatchedEP_Pattern pattern;
	public LinkedList<Recv>[] recvlist;
	public LinkedList<Send>[][] sendlist;
	//for unmatched endpoint, make sure all sends in source endpoint should be matched
	public Hashtable<Send,LinkedList<Recv>> pattern_match;
	public Hashtable<Recv, LinkedList<Send>> match_table;
	Operation lastr = null;
	Hashtable<Integer, Operation> lasts = null;
	//pair.first is op expr, pair.second is op time
	Hashtable<Operation, Pair<Expr, IntExpr>> operation_expr_map = null;
	
	
	public Encoder(Program program, 
			UnmatchedEP_Pattern pattern, LinkedList<Recv>[] rlist,
			LinkedList<Send>[][] slist) throws Z3Exception
	{
		this.pattern = pattern;
		this.recvlist = rlist;
		this.sendlist = slist;
		pattern_match = new Hashtable<Send,LinkedList<Recv>>();
		match_table = new Hashtable<Recv,LinkedList<Send>>();
		lasts = new Hashtable<Integer,Operation>();
		operation_expr_map = new Hashtable<Operation,Pair<Expr,IntExpr>>();
		this.program = program;
		solver = new SMTSolver();
		solver.definition();
	}
	
	public void Encoding() throws Z3Exception
	{
		for(Process process: program.processes)
		{
			lastr = null;
			lasts.clear();
			for(int i = 0; i < process.indicator; i++)//up to process.indicator 
			{
				Encoding(process.get(i));
			}
		}
		
		generateMatch();
		encodeMatch();
	}
	
	public void Encoding(Operation op) throws Z3Exception
	{
		if(op instanceof Recv)
		{
			IntExpr time = solver.MkTime("T" + op.event);
			Expr recv = solver.mkRecv("R" + ((Recv)op).dest + "_" + ((Recv)op).rank);
			Pair<Expr,IntExpr> recvinfo = new Pair<Expr,IntExpr>(recv,time);
			operation_expr_map.put(op, recvinfo);
			IntExpr var = solver.MkTime("var" + op.event);
			IntExpr nw = null;
			if(!((Recv)op).isBlock)
			{
				//add nearest inclosing wait for recv
			}
			solver.addFormula(solver.initRecv(recv, ((Recv)op).src, ((Recv)op).dest, time, var, nw));
			if(lastr != null)
			{
				solver.addFormula(solver.HB((IntExpr)operation_expr_map.get(lastr).getSecond(), 
						time));
			}
			
			lastr = op;
			//lastr will be initilized to null at beginning of traversing each process
		}
		else if(op instanceof Send)
		{
			IntExpr time = solver.MkTime("T" + op.event);
			Expr send = solver.mkSend("S" + ((Send)op).src + "_" + ((Send)op).rank);
			Pair<Expr,IntExpr> sendinfo = new Pair<Expr,IntExpr>(send,time);
			operation_expr_map.put(op, sendinfo);
			solver.addFormula(solver.initSend(send, ((Send)op).src, ((Send)op).dest, time, ((Send)op).value));
			
			if(lastr != null)
			{
//				System.out.println("send: " + send + "recv: " + operation_expr_map.get(lastr).getFirst());
//				System.out.println("lastr_time: "+ (IntExpr)operation_expr_map.get(lastr).getSecond()+ " send_time: "+time);
				//if non-blocking receive is applied, should be nw < send
				solver.addFormula(solver.HB((IntExpr)operation_expr_map.get(lastr).getSecond(), 
						time));
			}
			
			if(lasts.containsKey(((Send)op).dest))
			{
				solver.addFormula(solver.HB((IntExpr)operation_expr_map.get(lasts.get(((Send)op).dest)).getSecond(), 
						time));
				
			}

			//add nearest inclosing send for dest
			lasts.put(((Send)op).dest,op);
		}
	}
	
	public void encodeMatch() throws Z3Exception
	{
		//two parts: first, for every receive r, there must be a match, (r,.)
		for(Recv r : match_table.keySet())
		{
			Expr rExpr = operation_expr_map.get(r).getFirst();
//			IntExpr rTime = operation_expr_map.get(r).getSecond();
			BoolExpr a = null;
			BoolExpr b = null;
			for(Send s : match_table.get(r))
			{
				Expr sExpr = operation_expr_map.get(s).getFirst();
//				IntExpr sTime = operation_expr_map.get(s).getSecond();
				if(rExpr != null && sExpr != null)//should not be null
				{
					a = solver.Match(rExpr, sExpr);
					b = (b!=null)?solver.mkOr(a, b):a;//make or for all matches for receive r
				}
			}
			solver.addFormula(b);
		}
		
		//second, for every send s that can match pattern receive R(c), there must be a match (s,.)
		
		for(Send s : pattern_match.keySet())
		{
			Expr sExpr = operation_expr_map.get(s).getFirst();
//			IntExpr sTime = operation_expr_map.get(s).getSecond();
			BoolExpr a = null;
			BoolExpr b = null;
			for(Recv r : pattern_match.get(s))
			{
				Expr rExpr = operation_expr_map.get(r).getFirst();
//				IntExpr rTime = operation_expr_map.get(r).getSecond();
				if(rExpr != null && sExpr != null)//should not be null
				{
					a = solver.Match(rExpr, sExpr);
					b = (b!=null)?solver.mkOr(a, b):a;//make or for all matches for send s
				}
			}
			solver.addFormula(b);
		}
	}
	
	public void generateMatch(){
		 for(int i = 0; i < recvlist.length; i ++){
			 Iterator<Recv> ite_r = recvlist[i].iterator();
			 
			 //calculate # of sends from any source to i
			 int sendstoi = 0;
			 for(int j = 0; j < sendlist[i].length; j++){
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
							 if(pattern != null)
							 {
								 //check if the send is pattern send
								 if(s.dest == pattern.process.getRank() 
										 && s.src == pattern.determinstic.src)
								 {
									 if(!pattern_match.containsKey(s))
									 {
										 pattern_match.put(s, new LinkedList<Recv>());
									 }
									 pattern_match.get(s).add(r);
								 }
							 }
							 //System.out.println("match (" + r.exp + " " + s.exp+"), total num of sends: " + sendstoi +", and total num of sends for ep " + i + ": " +  sendlist[i][j].size());
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
