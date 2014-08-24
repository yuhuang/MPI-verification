package Finder;

import com.microsoft.z3.*;

public class Encoder 
{
	int main() throws Z3Exception
	{
		Context ctx = new Context();
		IntSort intsort = ctx.MkIntSort();
		Expr e = ctx.MkBound(0, intsort);
		
		
		return 0;
		
	}
	

}
