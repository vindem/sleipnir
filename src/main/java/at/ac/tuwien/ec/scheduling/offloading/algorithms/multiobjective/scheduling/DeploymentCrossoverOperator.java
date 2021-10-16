package at.ac.tuwien.ec.scheduling.offloading.algorithms.multiobjective.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import org.uma.jmetal.util.pseudorandom.impl.JavaRandomGenerator;

import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import scala.Tuple2;



public class DeploymentCrossoverOperator implements CrossoverOperator<DeploymentSolution>{

	private double crossoverProbability;
	Random randomGenerator;
	
	public DeploymentCrossoverOperator(double crossoverProbability){
		this.crossoverProbability = crossoverProbability;
		this.randomGenerator = new Random();
	}
	
	@Override
	public List<DeploymentSolution> execute(List<DeploymentSolution> solutions) {
		if (null == solutions) {
		      throw new JMetalException("Null parameter") ;
		    } else if (solutions.size() != 2) {
		      throw new JMetalException("There must be two parents instead of " + solutions.size()) ;
		    }

		    return doCrossover(crossoverProbability, solutions.get(0), solutions.get(1)) ;
	}

	private List<DeploymentSolution> doCrossover(double crossoverProbability, DeploymentSolution parent1,
			DeploymentSolution parent2) {
		List<DeploymentSolution> offsprings = new ArrayList<DeploymentSolution>(2);
		
		offsprings.add(parent1.copy());
		offsprings.add(parent2.copy());
		
		if(randomGenerator.nextDouble() < crossoverProbability)
		{
			for(int i = 0; i < parent1.getNumberOfVariables(); i++)
			{
				//System.out.println(parent1.getNumberOfVariables() + "," + comps1.length + "," + comps2.length);
				//if(((MobileSoftwareComponent)parent1.getDeployment().get()).isOffloadable()
					//	&& ((MobileSoftwareComponent)comps2[i]).isOffloadable())
				if(randomGenerator.nextDouble() < 0.5)
				{
					Tuple2<MobileSoftwareComponent,ComputationalNode> t0 = parent1.getVariableValue(i);
					Tuple2<MobileSoftwareComponent,ComputationalNode> t1 = parent2.getVariableValue(i);
					if(parent1.getNumberOfVariables() != parent2.getNumberOfVariables())
						System.out.println("OH NOOOOOOOOOOOOOOOOOO");
					offsprings.get(0).setVariableValue(i, t1);
					offsprings.get(1).setVariableValue(i, t0);
				}
			}
		}
		
		return offsprings;
	}

	public int getNumberOfGeneratedChildren() {
		return 2;
	}

	public int getNumberOfRequiredParents() {
		return 2;
	}

	public double getCrossoverProbability() {
		// TODO Auto-generated method stub
		return 0;
	}

}
