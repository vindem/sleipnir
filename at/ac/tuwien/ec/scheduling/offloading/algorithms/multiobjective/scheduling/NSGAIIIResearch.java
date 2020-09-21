package at.ac.tuwien.ec.scheduling.offloading.algorithms.multiobjective.scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.runner.multiobjective.NSGAIIRunner;

import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.ProblemUtils;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.comparator.RankingComparator;
import org.uma.jmetal.util.evaluator.impl.MultithreadedSolutionListEvaluator;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import scala.Tuple2;

public class NSGAIIIResearch extends OffloadScheduler{

	DeploymentProblem problem;
	private final int populationSize = 50;
    Algorithm<List<DeploymentSolution>> algorithm;
    CrossoverOperator<DeploymentSolution> crossover;
    MutationOperator<DeploymentSolution> mutation;
    SelectionOperator<List<DeploymentSolution>, DeploymentSolution> selection;
	private double crossoverProbability;
	private double mutationProbability;


	public NSGAIIIResearch(MobileApplication A, MobileCloudInfrastructure I) {
		super();
		this.problem = new DeploymentProblem(I, A);
		this.crossoverProbability = 0.9;
		this.mutationProbability = 1.0 / problem.getNumberOfVariables() ;
		crossover = new DeploymentCrossoverOperator(crossoverProbability);
		mutation = new DeploymentMutationOperator(mutationProbability);
		selection = new BinaryTournamentSelection<DeploymentSolution>(new RankingComparator<DeploymentSolution>());
		MultithreadedSolutionListEvaluator<DeploymentSolution> mtSolEvaluator = 
				new MultithreadedSolutionListEvaluator<DeploymentSolution>(Runtime.getRuntime().availableProcessors());
	}
	
	public NSGAIIIResearch(Tuple2<MobileApplication,MobileCloudInfrastructure> t) {
		super();
		this.problem = new DeploymentProblem(t._2(), t._1());
		this.crossoverProbability = 0.9;
		this.mutationProbability = 1.0 / problem.getNumberOfVariables() ;
		crossover = new DeploymentCrossoverOperator(crossoverProbability);
		mutation = new DeploymentMutationOperator(mutationProbability);
		selection = new BinaryTournamentSelection<DeploymentSolution>(new RankingComparator<DeploymentSolution>());
		MultithreadedSolutionListEvaluator<DeploymentSolution> mtSolEvaluator = 
				new MultithreadedSolutionListEvaluator<DeploymentSolution>(Runtime.getRuntime().availableProcessors());
	}

	
	@Override
	public ArrayList<OffloadScheduling> findScheduling() {
		double start = System.nanoTime();
		ArrayList<OffloadScheduling> deployments = new ArrayList<OffloadScheduling>();
		List<DeploymentSolution> population = new ArrayList<DeploymentSolution>();
		try{
			/*algorithm = new NSGAIIIBuilder<DeploymentSolution>(problem)
	            .setCrossoverOperator(crossover)
	            .setMutationOperator(mutation)
	            .setSelectionOperator(selection)
	            .setMaxIterations(100)
	            //.setPopulationSize(10)
	            .build() ;*/

			NSGAIIBuilder<DeploymentSolution> nsgaBuilder = new NSGAIIBuilder<DeploymentSolution>(problem, crossover, mutation, populationSize);
			nsgaBuilder.setMaxEvaluations(50);
			algorithm = nsgaBuilder.build();
			//AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
				//	.execute() ;
			algorithm.run();
			double end = System.nanoTime();
			population = algorithm.getResult() ;
			if(population!=null){
				Collections.sort(population, new RankingAndCrowdingDistanceComparator<>());
				int j = 0;
				for(int i = 0; i < population.size() ; i++)
				{
					OffloadScheduling tmp = population.get(i).getDeployment();
					tmp.setExecutionTime(end-start);
					deployments.add(tmp);
				}
			}
		}
		catch(Throwable T){
			System.err.println("Selection Error");
			population = algorithm.getResult() ;
			
			for(int i = 0; i < population.size(); i++)
			{
				if(!deployments.contains(population.get(i).getDeployment())
						&& problem.numberOfViolatedConstraints.getAttribute(population.get(i)) == 0)
					deployments.add(population.get(i).getDeployment());
			}
			return deployments;
		}
		
		return deployments;
	}

	

}
