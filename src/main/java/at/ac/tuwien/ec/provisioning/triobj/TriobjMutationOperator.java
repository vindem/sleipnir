package at.ac.tuwien.ec.provisioning.triobj;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.uma.jmetal.operator.mutation.MutationOperator;

import at.ac.tuwien.ec.provisioning.DefaultNetworkPlanner;

public class TriobjMutationOperator implements MutationOperator<TriobjSolution>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6788440882109554270L;
	private double mutationProbability;
	
	public TriobjMutationOperator(double mutationProbability) {
		this.mutationProbability = mutationProbability;
	}

	@Override
	public TriobjSolution execute(TriobjSolution solution) {
		UniformRealDistribution mutationRandomGenerator = new UniformRealDistribution();
		UniformIntegerDistribution indexGenerator = 
				new UniformIntegerDistribution(0, solution.getNumberOfVariables());
		
		if(mutationRandomGenerator.sample() < mutationProbability)
		{
			int index = indexGenerator.sample();
			boolean b1 = solution.getVariableValue(index);
			solution.setVariableValue(index, !b1);
		}
		
		return solution;
	}

	@Override
	public double getMutationProbability() {
		// TODO Auto-generated method stub
		return 0;
	}

}
