package at.ac.tuwien.ec.model.infrastructure.provisioning.edge;

import com.google.ortools.linearsolver.MPSolver;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;

public class MFLPPlanner extends EdgePlanner{
	static {
	    System.loadLibrary("jniortools");
	  }

	public static void setupEdgeNodes(MobileCloudInfrastructure inf)
	{
		MPSolver solver = new MPSolver("MFLP", MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING);
		
		
	}
	

}
