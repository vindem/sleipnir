package at.ac.tuwien.ec.sleipnir;

import com.microsoft.z3.*;

public class Z3Solver {

	public static void main(String[] arg)
	{
		Context ctx = new Context();
		Solver solver = ctx.mkSimpleSolver();
		final BoolExpr a = ctx.mkBoolConst("A");
		final BoolExpr b = ctx.mkBoolConst("B");
		final BoolExpr expr = ctx.mkOr(a,b);
		
		solver.add(expr);
		solver.check();
		final Model model = solver.getModel();
		
		System.out.println(model.getConstInterp(a));
		System.out.println(model.getConstInterp(b));
		
		ctx.close();
	}
}
