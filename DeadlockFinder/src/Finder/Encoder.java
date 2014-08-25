package Finder;

import com.microsoft.z3.*;

import java.lang.*;

public class Encoder 
{
	static IntSort intsort;
	static FuncDecl HB;
	
	public static void definition(Context ctx) throws Z3Exception
	{
		 intsort = ctx.MkIntSort();
		 HB = ctx.MkFuncDecl(ctx.MkSymbol("HB"), new Sort[]{intsort, intsort}, ctx.MkBoolSort());
	}

	
	public static void main(String[] args) throws Exception{
		Context ctx = new Context();
//		IntSort intsort = ctx.MkIntSort();
//		IntExpr x = ctx.MkIntConst("x");
//        IntExpr y = ctx.MkIntConst("y");
//        ArithExpr valuex = ctx.MkInt(100);
//        
//        BoolExpr xeq100 = ctx.MkEq(x, valuex);
//		BoolExpr xly = ctx.MkGt(x, y);
//		BoolExpr total = ctx.MkAnd(new BoolExpr[]{xeq100,xly});
		
//		Solver solver = ctx.MkSolver();
//		solver.Assert(xly);
		//HBDef(ctx);
		
		definition(ctx);
		
		Model model = Check(ctx, HBDef(ctx,"send_event", "recv_event"), Status.SATISFIABLE);
		System.out.println(model);
		//System.out.println("x = " + model.Evaluate(x, false) + ", y ="
              //  + model.Evaluate(y, false));
		
		
		
	}
	
	public static BoolExpr HBDef(Context ctx, String a, String b) throws Z3Exception
	{		
		Symbol symbola = ctx.MkSymbol(a);
		Symbol symbolb = ctx.MkSymbol(b);
		IntExpr inta = ctx.MkIntConst(symbola);
		IntExpr intb = ctx.MkIntConst(symbolb);
		Expr fapp = ctx.MkApp(HB, new Expr[]{inta, intb});
		Expr ite = ctx.MkITE(ctx.MkLt(inta, intb), ctx.MkTrue(), ctx.MkFalse());
		
		Quantifier q = ctx.MkForall(new Sort[]{intsort,intsort}, new Symbol[]{symbola,symbolb},
				ctx.MkEq(fapp, ite), 1, null, null, null, null);
		System.out.println(HB);
        System.out.println(q.toString());
        
        return q;
	}
	
	public static Model Check(Context ctx, BoolExpr f, Status sat) throws Exception
	{
		Solver s = ctx.MkSolver();
		
		s.Assert(f);
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
