package at.ac.tuwien.ec.sleipnir;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.microsoft.z3.*;

public class Z3Solver {

	public static void main(String[] arg)
	{
		final int num_task = 3;
		final int num_processors = 3;
		final double[] goals = new double[3];
		
		goals[0] = 8;
		goals[1] = 8;
		goals[2] = 8;
		
		System.err.println("java.library.path = " + System.getProperty("java.library.path"));
		System.err.println("trying to load lib z3java");
		try { 
			System.load("/home/vincenzo/z3/build/libz3.so");
			System.load("/home/vincenzo/z3/build/libz3java.so"); 
		} catch (UnsatisfiedLinkError ex1) { 
			ex1.printStackTrace();
			try { 
				System.err.println("Trying to load lib libz3java");
				System.loadLibrary("libz3java"); 
			} catch (UnsatisfiedLinkError ex2) { 
				ex2.printStackTrace();
			}
		}

		Map<String, String> config = new HashMap<String, String>();
		config.put("model", "true");
		Context ctx = new Context(config);
		Solver solver = ctx.mkSolver();
		
		IntExpr[][] assVar = new IntExpr[num_task][num_processors];
		
		for(int i = 0; i < num_task; i++)
			for(int j = 0; j < num_processors; j++)
				assVar[i][j] = ctx.mkIntConst("x"+i+""+j);
			
		BoolExpr[] assignment = new BoolExpr[num_task];
		
		for(int i = 0; i < num_task; i++)
		{
			BoolExpr domain = ctx.mkBool(true);
			ArithExpr sum = ctx.mkInt(0);
			for(int j = 0; j < num_processors; j++) 
			{
				domain = ctx.mkAnd(domain,
						ctx.mkOr(
								ctx.mkEq(assVar[i][j].simplify(),ctx.mkInt(0)),
								ctx.mkEq(assVar[i][j].simplify(),ctx.mkInt(1))
								)
						);
				sum = ctx.mkAdd(sum,assVar[i][j]);
			}
			assignment[i] = ctx.mkAnd(domain,ctx.mkEq(sum, ctx.mkInt(1)));
		}
		RealExpr[][] runtimes = new RealExpr[num_task][num_processors];
		RealExpr[][] costMatrix = new RealExpr[num_task][num_processors];
		RealExpr[][] energyMatrix = new RealExpr[num_task][num_processors];
		
		Random rand = new Random();
		System.out.println("RUNTIME");
		for(int i = 0; i < num_task; i++)
		{
			for(int j = 0; j < num_processors; j++)
			{
				runtimes[i][j] = ctx.mkReal(""+(1.0 + (9.0) * rand.nextDouble()));
				System.out.print(doubleValue(runtimes[i][j]) + " , ");
			}
			System.out.println("");
		}
		System.out.println("COST");
		for(int i = 0; i < num_task; i++)
		{
			for(int j = 0; j < num_processors; j++)
			{
				costMatrix[i][j] = ctx.mkReal(""+(1.0 + (9.0) * rand.nextDouble()));
				System.out.print(doubleValue(costMatrix[i][j]) + " , ");
			}
			System.out.println("");
		}
		System.out.println("ENERGY");
		for(int i = 0; i < num_task; i++)
		{
			for(int j = 0; j < num_processors; j++)
			{
				energyMatrix[i][j] = ctx.mkReal(""+(1.0 + (9.0) * rand.nextDouble()));
				System.out.print(doubleValue(energyMatrix[i][j])+ " , ");
			}
			System.out.println("");
		}
		
		
		ArithExpr[] runtimeForTask = new ArithExpr[num_task];
		BoolExpr[] rtConstraints = new BoolExpr[num_task];
		for(int i = 0; i < num_task; i++)
		{
			ArithExpr rt = ctx.mkMul(runtimes[i][0],assVar[i][0]);
			runtimeForTask[i] = ctx.mkAdd(rt);
			for(int j = 1; j < num_processors; j++)
			{
				rt = ctx.mkMul(runtimes[i][j],assVar[i][j]);
				runtimeForTask[i] = ctx.mkAdd(runtimeForTask[i],rt);
			}
			rtConstraints[i] = ctx.mkLe(runtimeForTask[i], ctx.mkInt((int)goals[0]));
			//System.out.println(rtConstraints[i]);
		}
		
		ArithExpr[] costForTask = new ArithExpr[num_task];
		BoolExpr[] costConstraints = new BoolExpr[num_task];
		for(int i = 0; i < num_task; i++)
		{
			ArithExpr cost = ctx.mkMul(costMatrix[i][0],assVar[i][0]);
			costForTask[i] = ctx.mkAdd(cost);
			for(int j = 1; j < num_processors; j++)
			{
				cost = ctx.mkMul(costMatrix[i][j],assVar[i][j]);
				costForTask[i] = ctx.mkAdd(runtimeForTask[i],cost);
			}
			costConstraints[i] = ctx.mkLe(costForTask[i], ctx.mkInt((int)goals[1]));
			//System.out.println(costConstraints[i]);
		}
		
		ArithExpr[] energyForTask = new ArithExpr[num_task];
		BoolExpr[] energyConstraints = new BoolExpr[num_task];
		for(int i = 0; i < num_task; i++)
		{
			ArithExpr energy = ctx.mkMul(energyMatrix[i][0],assVar[i][0]);
			energyForTask[i] = ctx.mkAdd(energy);
			for(int j = 1; j < num_processors; j++)
			{
				energy = ctx.mkMul(energyMatrix[i][j],assVar[i][j]);
				energyForTask[i] = ctx.mkAdd(energyForTask[i],energy);
			}
			energyConstraints[i] = ctx.mkLe(energyForTask[i], ctx.mkInt((int)goals[2]));
			//System.out.println(energyConstraints[i]);
		}
		
		BoolExpr taskConstraints[] = new BoolExpr[num_task];
		for(int i = 0; i < num_task; i++)
			taskConstraints[i] = ctx.mkAnd(rtConstraints[i],costConstraints[i],energyConstraints[i]);
		
		solver.add(assignment);
		solver.add(taskConstraints);
				
		Status status = solver.check();
		
		
		Model model = null;
		if(status == Status.SATISFIABLE)
		{
			model = solver.getModel();
			System.out.println("Satisfiable!");
			for(int i = 0; i < num_task; i++)
				for(int j = 0; j < num_processors; j++)
				{
					if(model.getConstInterp(assVar[i][j]).equals(ctx.mkInt(1)))
							System.out.println("Task "+i+ " assigned to " + j);
					
				}
		}
		//System.out.println(model);
		
		
		
		ctx.close();
	}

	private static double doubleValue(RealExpr realExpr) {
		Expr value = realExpr.simplify();
		if(value.isRatNum())
		{
			RatNum rational = (RatNum) value;
			IntNum num = rational.getNumerator();
			IntNum den = rational.getDenominator();
			return ((double) num.getInt64() / den.getInt64());
		}
		return ((IntNum)value.simplify()).getInt64();
	}
}
