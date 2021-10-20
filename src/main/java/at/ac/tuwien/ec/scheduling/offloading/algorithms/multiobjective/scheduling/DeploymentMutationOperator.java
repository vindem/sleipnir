package at.ac.tuwien.ec.scheduling.offloading.algorithms.multiobjective.scheduling;

import java.util.Random;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.uma.jmetal.operator.MutationOperator;

import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;


public class DeploymentMutationOperator implements MutationOperator<DeploymentSolution>{

	private UniformRealDistribution mutationRandomGenerator = new UniformRealDistribution();
	private double mutationProbability;
	
	public DeploymentMutationOperator(double mutationProbability){
		this.mutationProbability = mutationProbability;
	}
	
	@Override
	public DeploymentSolution execute(DeploymentSolution arg0) {
		DeploymentSolution mutated = arg0.copy();		
		UniformIntegerDistribution uDistr = new UniformIntegerDistribution(0,arg0.getNumberOfVariables()-1);
		MobileSoftwareComponent n1,n2;
		int idx1,idx2;
		boolean ex = false;
		if (mutationRandomGenerator.sample() < mutationProbability) {
			do
			{
				idx1 = uDistr.sample();
				while((idx2 = uDistr.sample()) == idx1) 
					;

				n1 = (MobileSoftwareComponent) mutated.getVariableValue(idx1)._1();
				n2 = (MobileSoftwareComponent) mutated.getVariableValue(idx2)._1();
				ex = n1.isOffloadable() && n2.isOffloadable();
			}
			while(!n1.isOffloadable() && !n2.isOffloadable() && !ex);

			ComputationalNode cn1 = (ComputationalNode) mutated.getVariableValue(idx1)._2();
			ComputationalNode cn2 = (ComputationalNode) mutated.getVariableValue(idx2)._2();
			
			mutated.setVariableValue(idx1, n1, cn2);
			mutated.setVariableValue(idx2, n2, cn1);
		}
		return mutated;
		
	}

	public double getMutationProbability() {
		// TODO Auto-generated method stub
		return this.mutationProbability;
	}

}
