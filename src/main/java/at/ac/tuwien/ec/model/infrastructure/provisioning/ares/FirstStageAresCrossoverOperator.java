package at.ac.tuwien.ec.model.infrastructure.provisioning.ares;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.util.JMetalException;

import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.mo.EdgePlanningSolution;

public class FirstStageAresCrossoverOperator implements CrossoverOperator<FirstStageAresSolution>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7320333101557582035L;
	private double crossoverProbability;
	UniformRealDistribution distr = new UniformRealDistribution(0.0, 1.0);
	Random rand;
	
	public FirstStageAresCrossoverOperator(double crossoverProbability) {
		this.crossoverProbability = crossoverProbability;
	}

	@Override
	public List<FirstStageAresSolution> execute(List<FirstStageAresSolution> source) {
		if(source == null) 
			throw new JMetalException("Parent list can't be null");
		if(source.size() != getNumberOfRequiredParents())
			throw new JMetalException("Need " + getNumberOfRequiredParents() + " parents.");
		return doCrossover(this.crossoverProbability, source.get(0), source.get(1));
	}

	private List<FirstStageAresSolution> doCrossover(double crossoverProbability, FirstStageAresSolution parent0,
			FirstStageAresSolution parent1) {
		List<FirstStageAresSolution> offsprings 
			= new ArrayList<FirstStageAresSolution>(getNumberOfGeneratedChildren());
		
		offsprings.add((FirstStageAresSolution) parent0.copy());
		offsprings.add((FirstStageAresSolution) parent1.copy());
		
		if(distr.sample() < crossoverProbability)
		{
			for(int i = 0; i < parent0.getNumberOfVariables(); i++)
			{
				if(rand.nextBoolean())
				{
					Boolean b0 = parent0.getVariableValue(i);
					Boolean b1 = parent1.getVariableValue(i);
					offsprings.get(0).setVariableValue(i, b1);
					offsprings.get(1).setVariableValue(i, b0);
				}
			}
		}
			
		return offsprings;
		
	}

	@Override
	public int getNumberOfRequiredParents() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public int getNumberOfGeneratedChildren() {
		// TODO Auto-generated method stub
		return 2;
	}

}
