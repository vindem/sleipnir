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
		OffloadScheduling d = arg0.getDeployment();
		UniformIntegerDistribution uDistr = new UniformIntegerDistribution(0,d.size()-1);
		MobileSoftwareComponent n1,n2;
		int idx1,idx2;
		boolean ex = false;
		if (mutationRandomGenerator.sample() < mutationProbability) {
			do
			{
				idx1 = uDistr.sample();
				while((idx2 = uDistr.sample()) == idx1) 
					;

				n1 = (MobileSoftwareComponent) d.keySet().toArray()[idx1];
				n2 = (MobileSoftwareComponent) d.keySet().toArray()[idx2];
				ex = n1.isOffloadable() && n2.isOffloadable();
			}
			while(!n1.isOffloadable() && !n2.isOffloadable() && !ex);

			ComputationalNode cn1 = (ComputationalNode) d.get(n1);
			ComputationalNode cn2 = (ComputationalNode) d.get(n2);
			
			arg0.setVariableValue(idx1, cn2);
			arg0.setVariableValue(idx2, cn1);
		}
		try {
			return new DeploymentSolution(d,arg0.getApplication(),arg0.getInfrastructure());
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public double getMutationProbability() {
		// TODO Auto-generated method stub
		return 0;
	}

}
