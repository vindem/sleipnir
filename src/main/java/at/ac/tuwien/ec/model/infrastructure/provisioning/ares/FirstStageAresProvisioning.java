package at.ac.tuwien.ec.model.infrastructure.provisioning.ares;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.comparator.RankingComparator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.EdgePlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.mo.EdgePlanningSolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FirstStageAresProvisioning extends EdgePlanner{
	
	private final int populationSize = 50;
	private final double crossoverProbability = 0.9, mutationProbability = 0.1;
	private FirstStageAresProblem problem; 
	Algorithm<List<FirstStageAresSolution>> algorithm;
	CrossoverOperator<FirstStageAresSolution> crossover;
	MutationOperator<FirstStageAresSolution> mutation;
	SelectionOperator<List<FirstStageAresSolution>, FirstStageAresSolution> selection;
	

	public FirstStageAresProvisioning(MobileCloudInfrastructure inf)
	{
		super();
		this.problem = new FirstStageAresProblem(inf);
		crossover = new FirstStageAresCrossoverOperator(crossoverProbability);
		mutation = new FirstStageAresMutationOperator(mutationProbability);
		selection = new BinaryTournamentSelection<FirstStageAresSolution>(new RankingComparator<FirstStageAresSolution>());
		SequentialSolutionListEvaluator<FirstStageAresSolution> mtSolEvaluator = 
				new SequentialSolutionListEvaluator<FirstStageAresSolution>();
	}
	
	public List<FirstStageAresSolution> getEdgePlan()
	{
		List<FirstStageAresSolution> population = new ArrayList<FirstStageAresSolution>();
		try
		{
			NSGAIIBuilder<FirstStageAresSolution> nsgaBuilder = 
					new NSGAIIBuilder<FirstStageAresSolution>(problem, crossover, mutation);
			nsgaBuilder.setMaxEvaluations(50);
			nsgaBuilder.setPopulationSize(populationSize);
			algorithm = nsgaBuilder.build();
			AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
			
			population = algorithm.getResult() ;
			//System.out.println("No population: " + population.isEmpty());
			if(population!=null)
				Collections.sort(population, new RankingAndCrowdingDistanceComparator<>());
			
			return population;
		}
		catch(Throwable T){
			System.err.println("Selection Error");
			return null;
		}
		
	}

	
	
}
