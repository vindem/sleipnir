package at.ac.tuwien.ec.provisioning.triobj;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.util.JMetalException;

import at.ac.tuwien.ec.provisioning.DefaultNetworkPlanner;
import at.ac.tuwien.ec.provisioning.edge.mo.EdgePlanningSolution;

public class TriobjCrossoverOperator implements CrossoverOperator<TriobjSolution>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7320333101557582035L;
	private double crossoverProbability;
	UniformRealDistribution distr = new UniformRealDistribution(0.0, 1.0);
	Random rand;
	
	public TriobjCrossoverOperator(double crossoverProbability) {
		this.crossoverProbability = crossoverProbability;
		rand = new Random();
	}

	@Override
	public List<TriobjSolution> execute(List<TriobjSolution> source) {
		//System.out.println("CROSSOVER!");
		if(source == null) 
			throw new JMetalException("Parent list can't be null");
		if(source.size() != getNumberOfRequiredParents())
			throw new JMetalException("Need " + getNumberOfRequiredParents() + " parents.");
		//return doUniformCrossover(this.crossoverProbability, source.get(0), source.get(1));
		return doSinglePointCrossover(crossoverProbability, source.get(0), source.get(1));
	}
	
	private List<TriobjSolution> doUniformCrossover(double crossoverProbability, TriobjSolution parent0,
			TriobjSolution parent1) {
		List<TriobjSolution> offspring
			= new ArrayList<TriobjSolution>(getNumberOfGeneratedChildren());
		Random rand = new Random();	
		if(distr.sample() < crossoverProbability)
		{
			BitSet bs0 = parent0.getEdgeNodeMap();
			BitSet bs1 = parent1.getEdgeNodeMap();
			for(int i = 0; i < parent0.getNumberOfVariables(); i++)
				if(rand.nextBoolean())
				{
					boolean b0 = bs0.get(i);
					boolean b1 = bs1.get(i);
					bs0.set(i,b1);
					bs1.set(i,b0);
				}
			
			offspring.add(new TriobjSolution(bs0));
			offspring.add(new TriobjSolution(bs1));
			
		}
		else
		{
			offspring.add((TriobjSolution) parent0.copy());
			offspring.add((TriobjSolution) parent1.copy());
		}
		
		
		return offspring;
		
	}

	private List<TriobjSolution> doSinglePointCrossover(double crossoverProbability, TriobjSolution parent0,
			TriobjSolution parent1) {
		List<TriobjSolution> offspring
			= new ArrayList<TriobjSolution>(getNumberOfGeneratedChildren());
		Random rand = new Random();
		if(distr.sample() < crossoverProbability)
		{
			BitSet bs0 = parent0.getEdgeNodeMap();
			BitSet bs1 = parent1.getEdgeNodeMap();
			int singlePointIndex = rand.nextInt(parent0.getNumberOfVariables());
			for(int i = singlePointIndex; i < parent0.getNumberOfVariables(); i++)
			{
					boolean b0 = bs0.get(i);
					boolean b1 = bs1.get(i);
					bs0.set(i,b1);
					bs1.set(i,b0);
			}
			
			offspring.add(new TriobjSolution(bs0));
			offspring.add(new TriobjSolution(bs1));
			
		}
		else
		{
			offspring.add((TriobjSolution) parent0.copy());
			offspring.add((TriobjSolution) parent1.copy());
		}
		
		
		return offspring;
		
	}
	
	public int getNumberOfRequiredParents() {
		// TODO Auto-generated method stub
		return 2;
	}

	public int getNumberOfGeneratedChildren() {
		// TODO Auto-generated method stub
		return 2;
	}

	public double getCrossoverProbability() {
		// TODO Auto-generated method stub
		return 0;
	}

}
