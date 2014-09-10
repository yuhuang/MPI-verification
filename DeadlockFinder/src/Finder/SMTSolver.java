package Finder;

import com.microsoft.z3.*;

public class SMTSolver {
	
	 IntSort intsort;
	 FuncDecl HB, MATCH;
	 BoolExpr initHB, initMatch;
	 TupleSort Recv, Send;
	 IntSort Wait;
	 Context ctx;
	 Solver s;
	 boolean display = false;
	 
	public SMTSolver()
	{}
	
	public void definition() throws Z3Exception
	{
		 ctx = new Context();
		 s = ctx.MkSolver();
		 intsort = ctx.MkIntSort();
//		 HB = ctx.MkFuncDecl(ctx.MkSymbol("HB"), new Sort[]{intsort, intsort}, ctx.MkBoolSort());
//		 initHB = initHB();
		 
		 Recv = ctx.MkTupleSort(ctx.MkSymbol("Recv"), 
				 new Symbol[] { ctx.MkSymbol("src"), ctx.MkSymbol("dest"), ctx.MkSymbol("time"), 
			 					ctx.MkSymbol("match"), ctx.MkSymbol("var"), ctx.MkSymbol("nw") }, 
				 new Sort[] { intsort, intsort,intsort,intsort,intsort,intsort});
		 
		 Send = ctx.MkTupleSort(ctx.MkSymbol("Send"), 
				 new Symbol[] { ctx.MkSymbol("src"), ctx.MkSymbol("dest"), ctx.MkSymbol("time"), 
				ctx.MkSymbol("match"), ctx.MkSymbol("value") }, 
				new Sort[] { intsort, intsort,intsort,intsort,intsort});
//		 MATCH = ctx.MkFuncDecl(ctx.MkSymbol("MATCH"), new Sort[]{Recv, Send}, ctx.MkBoolSort());
//		 initMatch = initMATCH();
		 
//		 if(display)
//			 System.out.println(HB + "\n" + initHB + "\n" + MATCH + "\n" + initMatch);
		 
	}

	
	public static void main(String[] args) throws Exception{	
		SMTSolver solver = new SMTSolver();
		solver.definition();
		
		
		
		Expr r = solver.mkRecv("recv1");
		Expr s = solver.mkSend("send1");
		
		IntExpr atime = solver.MkTime("T0_0");
		IntExpr btime = solver.MkTime("T1_0");
		solver.addFormula(solver.initRecv(r, -1, 0, atime, solver.MkTime("var1"), null));
		solver.addFormula(solver.initSend(s, 1, 0, btime, 99));
		
				
//        System.out.println(q.toString());
   
//		Expr fapp = solver.ctx.MkApp(solver.HB, new Expr[]{(IntExpr)atime,(IntExpr)btime});
//        Quantifier q = solver.ctx.MkForall(new Expr[]{atime,btime},
//        		solver.ctx.MkEq(fapp, solver.ctx.MkLt((IntExpr)atime, (IntExpr)btime)), 1, null, null, 
//				null,null);
        
//        solver.addFormula(solver.HB(atime,btime));
        
        solver.addFormula(solver.Match(r, s));
        
        
//        Expr ctime = solver.ctx.MkConst("c",solver.ctx.MkIntSort());
//		Expr dtime = solver.ctx.MkConst("d",solver.ctx.MkIntSort());
//		
//        BoolExpr hbinstance1 = (BoolExpr)solver.ctx.MkApp(solver.HB,new Expr[]{(IntExpr)ctime, (IntExpr)dtime});
//        solver.addFormula(hbinstance1);
//        System.out.println(hbinstance1);
//        
//        Expr etime = solver.ctx.MkConst("e",solver.ctx.MkIntSort());
//		Expr ftime = solver.ctx.MkConst("f",solver.ctx.MkIntSort());
//		      
//        BoolExpr hbinstance2 = (BoolExpr)solver.ctx.MkApp(solver.HB,new Expr[]{(IntExpr)etime, (IntExpr)ftime});
//        
//        
//        solver.addFormula(hbinstance2);
//        System.out.println(hbinstance2);

//        solver.addFormula(solver.ctx.MkEq(ctime, solver.ctx.MkInt(4)));
//        solver.addFormula(solver.ctx.MkEq(dtime, solver.ctx.MkInt(2)));
        
        
        
//        TupleSort tuple = solver.ctx.MkTupleSort(solver.ctx.MkSymbol("Tu"), 
//				 new Symbol[] { solver.ctx.MkSymbol("src")}, 
//				 new Sort[] { solver.intsort});
//        
//        	
////        solver.addFormula(solver.ctx.MkEq(first, solver.ctx.MkInt(100)));
//        
//
//        Expr te1 = solver.ctx.MkConst("te1",tuple);
//        Expr first = solver.ctx.MkApp(tuple.FieldDecls()[0], te1);
//        Expr te2 = solver.ctx.MkConst("te2",tuple);
//        Expr first1 = solver.ctx.MkApp(tuple.FieldDecls()[0], te2);
//
//        Expr ite = solver.ctx.MkITE(solver.ctx.MkEq(first, first1), solver.ctx.MkTrue(), 
//        		solver.ctx.MkFalse());
//        FuncDecl func = solver.ctx.MkFuncDecl(solver.ctx.MkSymbol("fun"), new Sort[]{tuple, tuple}, 
//				solver.ctx.MkBoolSort());
//        Expr app = solver.ctx.MkApp(func, new Expr[]{te1,te2});
//        Quantifier quan = solver.ctx.MkForall(new Expr[]{te1,te2},
//				solver.ctx.MkEq(app, ite),1,null,null,null,null);
//        System.out.println(quan);
//        Expr tupleExpr1 = solver.ctx.MkConst("tupleInstance1", tuple);	
//        Expr tupleExpr2 = solver.ctx.MkConst("tupleInstance2", tuple);
//        Expr first_tupleExpr1 = solver.ctx.MkApp(tuple.FieldDecls()[0], tupleExpr1);
//        Expr first_tupleExpr2 = solver.ctx.MkApp(tuple.FieldDecls()[0], tupleExpr2);
////        solver.addFormula(solver.ctx.MkEq(first_tupleExpr1, solver.ctx.MkInt(100)));
////        solver.addFormula(solver.ctx.MkEq(first_tupleExpr1, first_tupleExpr2));
//		solver.addFormula(solver.ctx.MkEq(solver.ctx.MkApp(func, new Expr[]{tupleExpr1, tupleExpr2}),
//				solver.ctx.MkTrue()));
        solver.displayFormulas();
		
		System.out.println("Result ================");
		Model model = solver.Check(Status.SATISFIABLE);
		System.out.println(model);
		
		//System.out.println("x = " + model.Evaluate(x, false) + ", y ="
              //  + model.Evaluate(y, false));
	}
	
	public BoolExpr mkOr(BoolExpr a, BoolExpr b) throws Z3Exception
	{
		return ctx.MkOr(new BoolExpr[]{a,b});
	}
	
	public Expr getRecvField(Expr recv, int index) throws Z3Exception
	{
		return ctx.MkApp(Recv.FieldDecls()[index], recv);
	}
	
	public Expr getSendField( Expr send, int index) throws Z3Exception
	{
		return ctx.MkApp(Send.FieldDecls()[index], send);
	}
	
	
	public BoolExpr Match(Expr r, Expr s) throws Z3Exception
	{
//		return (BoolExpr)ctx.MkApp(MATCH, new Expr[]{r, s});
		BoolExpr cond = ctx.MkAnd(new BoolExpr[]{
				ctx.MkOr(new BoolExpr[]{ctx.MkEq(getRecvField( r, 0), getSendField( s, 0)), 
										ctx.MkEq(getRecvField( r, 0), ctx.MkInt(-1))}),//r.src=s.src or r.src=-1
				ctx.MkEq(getRecvField( r, 1), getSendField( s, 1)),//r.dest=s.dest
				ctx.MkEq(getRecvField( r, 4), getSendField( s, 4)),//r.var=s.value
				HB((IntExpr)getSendField(s,2),(IntExpr)getRecvField(r,2)),//could be send < nw for non-blocking operations
				ctx.MkEq(getRecvField( r,3), getSendField(s,2)),//r.match = s.time
				ctx.MkEq(getRecvField( r,2), getSendField(s,3))//s.match=r.time
				});
		return cond;
	}
	
	public BoolExpr initMATCH() throws Z3Exception
	{
		
		Expr recv = ctx.MkConst("R", Recv);
		Expr send = ctx.MkConst("S", Send);
		Expr fapp = ctx.MkApp(MATCH, new Expr[]{recv, send});
		BoolExpr cond = ctx.MkAnd(new BoolExpr[]{
				ctx.MkOr(new BoolExpr[]{ctx.MkEq(getRecvField( recv, 0), getSendField( send, 0)), 
										ctx.MkEq(getRecvField( recv, 0), ctx.MkInt(-1))}),//r.src=s.src or r.src=-1
				ctx.MkEq(getRecvField( recv, 1), getSendField( send, 1)),//r.dest=s.dest
				ctx.MkEq(getRecvField( recv, 4), getSendField( send, 4)),//r.var=s.value
				HB((IntExpr)getSendField(send,2),(IntExpr)getRecvField(recv,2)),//could be send < nw for non-blocking operations
				ctx.MkEq(getRecvField( recv,3), getSendField(send,2)),//r.match = s.time
				ctx.MkEq(getRecvField( recv,2), getSendField(send,3))//s.match=r.time
				});
		Expr ite = ctx.MkITE(cond, ctx.MkTrue(), ctx.MkFalse());
		Quantifier q = ctx.MkForall(new Expr[]{recv,send},
						ctx.MkEq(fapp, ite),1,null,null,null,null);
//		System.out.println(MATCH);
//		System.out.println(q.toString());
		
		return q;
	}
	
	public IntExpr MkTime( String event) throws Z3Exception
	{
		return ctx.MkIntConst(event);
	}
	
	public BoolExpr HB(IntExpr a, IntExpr b) throws Z3Exception
	{
//		return (BoolExpr)ctx.MkApp(HB, new Expr[]{a,b});
		return ctx.MkLt(a, b);
	}
	
	public BoolExpr initHB() throws Z3Exception
	{		
		
		IntExpr a = ctx.MkIntConst("a");
		IntExpr b = ctx.MkIntConst("b");
		Expr fapp = ctx.MkApp(HB, new Expr[]{a,b});
		Expr ite = ctx.MkITE(ctx.MkLt(a, b), ctx.MkTrue(), ctx.MkFalse());
		
				
//        System.out.println(q.toString());
        
        return ctx.MkForall(new Expr[]{a,b},
				ctx.MkEq(fapp, ite), 1, null, null, 
				null,null);
	}
	
	public Expr mkSend( String name) throws Z3Exception
	{
		Expr sExpr = ctx.MkConst(name, Send);
		if(display)
			System.out.println(sExpr);
		return sExpr;		
	}
	
	public BoolExpr initSend( Expr send, int src, int dest, IntExpr time, int value) throws Z3Exception
	{	
		
		BoolExpr sDf = ctx.MkAnd(new BoolExpr[]{ctx.MkEq(getSendField(send,0), ctx.MkInt(src)), 
										ctx.MkEq(getSendField(send,1), ctx.MkInt(dest)),
										ctx.MkEq(getSendField(send,4), ctx.MkInt(value)),
										ctx.MkEq(getSendField(send,2), time)
										});
//		if(display)
//			System.out.println(sDf);
		
		return sDf;
		
		
	}
	
	public Expr mkRecv( String name) throws Z3Exception
	{
		Expr rExpr = ctx.MkConst(name, Recv);
		if(display)
			System.out.println(rExpr);
		return rExpr;
	} 
	
	public BoolExpr initRecv( Expr recv, int src, int dest, IntExpr time, IntExpr var, IntExpr nw) throws Z3Exception
	{		
		BoolExpr rDf;
		if(nw != null)
			rDf = ctx.MkAnd(new BoolExpr[]{ctx.MkEq(getRecvField(recv,0), ctx.MkInt(src)), 
										ctx.MkEq(getRecvField(recv,1), ctx.MkInt(dest)),
										ctx.MkEq(getRecvField(recv,4), var),
										ctx.MkEq(getRecvField(recv,5), nw),
										ctx.MkEq(getRecvField(recv,2), time)
										});
		else rDf = ctx.MkAnd(new BoolExpr[]{ctx.MkEq(getRecvField(recv,0), ctx.MkInt(src)), 
				ctx.MkEq(getRecvField(recv,1), ctx.MkInt(dest)),
				ctx.MkEq(getRecvField(recv,4), var),
				ctx.MkEq(getRecvField(recv,2), time)
				});
		
//		if(display)
//			System.out.println(rDf);
		
		return rDf;
	}
	
	
	public void addFormula(BoolExpr expr) throws Z3Exception
	{
		s.Assert(expr);
	}
	
	public Model Check(Status sat) throws Exception
	{	
		//s.Assert(f);
		if (s.Check() != sat)
		{
//			throw new Exception("Check Failed!");
			System.out.println("UNSAT!");
			return null;
		}
		if (sat == Status.SATISFIABLE)
		    return s.Model();
		else
		    return null;
	}
	
	public void displayFormulas() throws Z3Exception
	{
		for(BoolExpr assertion : s.Assertions())
		{
			System.out.println(assertion);
		}
	}

}
