package at.ac.tuwien.ec.provisioning.ares;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.uma.jmetal.operator.MutationOperator;

import at.ac.tuwien.ec.provisioning.DefaultNetworkPlanner;

public class FirstStageAresMutationOperator implements MutationOperator<FirstStageAresSolution>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6788440882109554270L;
	private double mutationProbability;
	
	public FirstStageAresMutationOperator(double mutationProbability) {
		this.mutationProbability = mutationProbability;
	}

	public FirstStageAresSolution execute(FirstStageAresSolution solution) {
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

	public double getMutationProbability() {
		// TODO Auto-generated method stub
		return this.mutationProbability;
	}

}
