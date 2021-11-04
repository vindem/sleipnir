package at.ac.tuwien.ec.scheduling.offloading.algorithms.multiobjective.scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;

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
	private final int maxIterations = 100;
    Algorithm<List<DeploymentSolution>> algorithm;
    CrossoverOperator<DeploymentSolution> crossover;
    MutationOperator<DeploymentSolution> mutation;
    SelectionOperator<List<DeploymentSolution>, DeploymentSolution> selection;
	private double crossoverProbability;
	private double mutationProbability;
	MultithreadedSolutionListEvaluator<DeploymentSolution> mtSolEvaluator;


	public NSGAIIIResearch(MobileApplication A, MobileCloudInfrastructure I) {
		super();
		this.problem = new DeploymentProblem(I, A);
		this.crossoverProbability = 0.9;
		this.mutationProbability = 1.0 / problem.getNumberOfVariables() ;
		crossover = new DeploymentCrossoverOperator(crossoverProbability);
		mutation = new DeploymentMutationOperator(mutationProbability);
		selection = new BinaryTournamentSelection<DeploymentSolution>(new RankingComparator<DeploymentSolution>());
		 mtSolEvaluator = new MultithreadedSolutionListEvaluator<DeploymentSolution>(Runtime.getRuntime().availableProcessors(), problem);
	}
	
	public NSGAIIIResearch(Tuple2<MobileApplication,MobileCloudInfrastructure> t) {
		super();
		this.problem = new DeploymentProblem(t._2(), t._1());
		this.crossoverProbability = 0.9;
		this.mutationProbability = 1.0 / problem.getNumberOfVariables() ;
		crossover = new DeploymentCrossoverOperator(crossoverProbability);
		mutation = new DeploymentMutationOperator(mutationProbability);
		selection = new BinaryTournamentSelection<DeploymentSolution>(new RankingComparator<DeploymentSolution>());
		mtSolEvaluator = new MultithreadedSolutionListEvaluator<DeploymentSolution>(Runtime.getRuntime().availableProcessors(), problem);
	}

	
	@Override
	public ArrayList<OffloadScheduling> findScheduling() {
		double start = System.nanoTime();
		ArrayList<OffloadScheduling> finalDeployments = new ArrayList<OffloadScheduling>();
		double maxBatt = Double.MIN_VALUE, minRt = Double.MAX_VALUE, minCost = Double.MAX_VALUE;
		List<DeploymentSolution> population = new ArrayList<DeploymentSolution>();
		try{
			

			NSGAIIBuilder<DeploymentSolution> nsgaBuilder = new NSGAIIBuilder<DeploymentSolution>(problem, crossover, mutation);
			nsgaBuilder.setSolutionListEvaluator(mtSolEvaluator);
			nsgaBuilder.setMaxIterations(maxIterations);
			nsgaBuilder.setPopulationSize(populationSize);
			algorithm = nsgaBuilder.build();
			//AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
				//	.execute() ;
			algorithm.run();
			mtSolEvaluator.evaluate(population,problem);
			double end = System.nanoTime();
			population = algorithm.getResult() ;
			
			
			if(population!=null){
				Collections.sort(population, new RankingAndCrowdingDistanceComparator<DeploymentSolution>());
				int j = 0;
				for(int i = 0; i < population.size() ; i++)
				{
					OffloadScheduling tmp = population.get(i).getDeployment();
					tmp.setExecutionTime(end-start);
					if(tmp.getBatteryLifetime() > maxBatt && tmp.getBatteryLifetime() != Double.MAX_VALUE
							&& tmp.getBatteryLifetime() >= 0.0);
						maxBatt = tmp.getBatteryLifetime();
					if(tmp.getRunTime() < minRt)
						minRt = tmp.getRunTime();
					if(tmp.getUserCost() < minCost)
						minCost = tmp.getUserCost();
				}
				
				for(int i = 0; i < population.size() ; i++)
				{
					OffloadScheduling tmp = population.get(i).getDeployment();
					if(tmp.getBatteryLifetime() == maxBatt)
						finalDeployments.add(tmp);
				}
				/*
				if(finalDeployments.size()>1)
				{
					for(OffloadScheduling dep: finalDeployments)
						if(dep.getRunTime() > minRt)
							finalDeployments.remove(dep);
				}
				if(finalDeployments.size()>1)
				{
					for(OffloadScheduling dep: finalDeployments)
						if(dep.getUserCost() > minCost)
							finalDeployments.remove(dep);
				}*/
			}
			
		}
		catch(Throwable T){
			T.printStackTrace();
		}
		
		return finalDeployments;
	}

	@Override
	public ComputationalNode findTarget(OffloadScheduling s, MobileSoftwareComponent msc) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
