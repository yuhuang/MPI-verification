package Finder;

import com.microsoft.z3.*;

public class SMTSolver {
	
	 IntSort intsort;
	 FuncDecl HB, MATCH;
	 TupleSort Recv, Send;
	 IntSort Wait;
	 Context ctx;
	 Solver s;
	 
	public SMTSolver()
	{}
	
	public void definition() throws Z3Exception
	{
		 ctx = new Context();
		 s = ctx.MkSolver();
		 intsort = ctx.MkIntSort();
		 HB = ctx.MkFuncDecl(ctx.MkSymbol("HB"), new Sort[]{intsort, intsort}, ctx.MkBoolSort());
		 
		 Recv = ctx.MkTupleSort(ctx.MkSymbol("Recv"), 
				 new Symbol[] { ctx.MkSymbol("src"), ctx.MkSymbol("dest"), ctx.MkSymbol("time"), 
			 					ctx.MkSymbol("match"), ctx.MkSymbol("var"), ctx.MkSymbol("nw") }, 
				 new Sort[] { intsort, intsort,intsort,intsort,intsort,intsort});
		 
		 Send = ctx.MkTupleSort(ctx.MkSymbol("Send"), 
				 new Symbol[] { ctx.MkSymbol("src"), ctx.MkSymbol("dest"), ctx.MkSymbol("time"), 
				ctx.MkSymbol("match"), ctx.MkSymbol("value") }, 
				new Sort[] { intsort, intsort,intsort,intsort,intsort});
		 MATCH = ctx.MkFuncDecl(ctx.MkSymbol("MATCH"), new Sort[]{Recv, Send}, ctx.MkBoolSort());
		 
		 
	}

	
	public void main(String[] args) throws Exception{		
		definition();
		s.Assert(HB(ctx.MkIntConst("send_event"), ctx.MkIntConst("recv_event")));
		Model model = Check(Status.SATISFIABLE);
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
	
	public BoolExpr MATCH(Expr recv, Expr send) throws Z3Exception
	{
		Expr fapp = ctx.MkApp(MATCH, new Expr[]{recv, send});
		BoolExpr cond = ctx.MkAnd(new BoolExpr[]{
				ctx.MkOr(new BoolExpr[]{ctx.MkEq(getRecvField( recv, 0), getSendField( send, 0)), 
										ctx.MkEq(getRecvField( recv, 0), ctx.MkInt(-1))}),//r.src=s.src or r.src=-1
				ctx.MkEq(getRecvField( recv, 1), getSendField( send, 1)),//r.dest=s.dest
				ctx.MkEq(getRecvField( recv, 4), getSendField( send, 4)),//r.var=s.value
				HB((IntExpr)getSendField( send, 2), (IntExpr)getRecvField( recv, 2)),//could be send < nw for non-blocking operations
				ctx.MkEq(getRecvField( recv,3), getSendField(send,2)),//r.match = s.time
				ctx.MkEq(getRecvField( recv,2), getSendField(send,3))//s.match=r.time
				});
		Expr ite = ctx.MkITE(cond, ctx.MkTrue(), ctx.MkFalse());
		Quantifier q = ctx.MkForall(new Expr[]{recv,send},
						ctx.MkEq(fapp, ite),1,null,null,null,null);
		System.out.println(MATCH);
		System.out.println(q.toString());
		
		return q;
	}
	
	public IntExpr MkTime( String event) throws Z3Exception
	{
		return ctx.MkIntConst(event);
	}
	
	public BoolExpr HB( IntExpr a, IntExpr b) throws Z3Exception
	{		
		
		Expr fapp = ctx.MkApp(HB, new Expr[]{a,b});
		Expr ite = ctx.MkITE(ctx.MkLt(a, b), ctx.MkTrue(), ctx.MkFalse());
		
		Quantifier q = ctx.MkForall(new Expr[]{a,b},
				ctx.MkEq(fapp, ite), 1, null, null, null, null);
		System.out.println(HB);
        System.out.println(q.toString());
        
        return q;
	}
	
	public Expr mkSend( String name) throws Z3Exception
	{
		return ctx.MkConst(name, Send);
	}
	
	public BoolExpr initSend( Expr send, int src, int dest, IntExpr time, int value) throws Z3Exception
	{	
		return ctx.MkAnd(new BoolExpr[]{ctx.MkEq(getSendField(send,0), ctx.MkInt(src)), 
										ctx.MkEq(getSendField(send,1), ctx.MkInt(dest)),
										ctx.MkEq(getSendField(send,4), ctx.MkInt(value)),
										ctx.MkEq(getSendField(send,2), time)
										});
	}
	
	public Expr mkRecv( String name) throws Z3Exception
	{
		return ctx.MkConst(name, Recv);
	} 
	
	public BoolExpr initRecv( Expr recv, int src, int dest, IntExpr time, IntExpr var, IntExpr nw) throws Z3Exception
	{		
		return ctx.MkAnd(new BoolExpr[]{ctx.MkEq(getRecvField(recv,0), ctx.MkInt(src)), 
										ctx.MkEq(getRecvField(recv,1), ctx.MkInt(dest)),
										ctx.MkEq(getRecvField(recv,4), var),
										ctx.MkEq(getRecvField(recv,5), nw),
										ctx.MkEq(getRecvField(recv,2), time)
										});
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
			throw new Exception("Check Failed!");
		}
		if (sat == Status.SATISFIABLE)
		    return s.Model();
		else
		    return null;
	}

}
