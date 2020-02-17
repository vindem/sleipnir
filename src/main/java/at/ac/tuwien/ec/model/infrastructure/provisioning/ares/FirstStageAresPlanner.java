package at.ac.tuwien.ec.model.infrastructure.provisioning.ares;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.comparator.RankingComparator;
import org.uma.jmetal.util.evaluator.impl.MultithreadedSolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.EdgePlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.mo.EdgePlanningCrossoverOperator;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.mo.EdgePlanningMutationOperator;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.mo.EdgePlanningProblem;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.mo.EdgePlanningSolution;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import scala.Tuple2;

public class FirstStageAresPlanner extends EdgePlanner{
	
	private FirstStageAresProblem problem;
	private double crossoverProbability;
	private double mutationProbability;
	private BinaryTournamentSelection<FirstStageAresSolution> selectionMethod;
	private FirstStageAresMutationOperator mutationOperator;
	private FirstStageAresCrossoverOperator crossoverOperator;
	private SequentialSolutionListEvaluator<FirstStageAresSolution> mtSolEvaluator;
	Algorithm<List<FirstStageAresSolution>> algorithm;

	public FirstStageAresPlanner(Tuple2<MobileApplication, MobileCloudInfrastructure> arg0)
	{
		super();
		
		this.problem = new FirstStageAresProblem(arg0._2());
		this.crossoverProbability = 0.9;
		this.mutationProbability = 1.0 / problem.getNumberOfVariables() ;
		crossoverOperator = new FirstStageAresCrossoverOperator(crossoverProbability);
		mutationOperator = new FirstStageAresMutationOperator(mutationProbability);
		selectionMethod = new BinaryTournamentSelection<FirstStageAresSolution>(new RankingComparator<FirstStageAresSolution>());
		mtSolEvaluator = 
				new SequentialSolutionListEvaluator<FirstStageAresSolution>();
	}
	
	public FirstStageAresPlanner(MobileApplication a, MobileCloudInfrastructure i)
	{
		super();
		
		this.problem = new FirstStageAresProblem(i);
		this.crossoverProbability = 0.9;
		this.mutationProbability = 1.0 / problem.getNumberOfVariables() ;
		crossoverOperator = new FirstStageAresCrossoverOperator(crossoverProbability);
		mutationOperator = new FirstStageAresMutationOperator(mutationProbability);
		selectionMethod = new BinaryTournamentSelection<FirstStageAresSolution>(new RankingComparator<FirstStageAresSolution>());
		mtSolEvaluator = 
				new SequentialSolutionListEvaluator<FirstStageAresSolution>();
	}
	
	public boolean setupEdgeNodes(MobileCloudInfrastructure inf)
	{
		List<FirstStageAresSolution> population = new ArrayList<FirstStageAresSolution>();
		try
		{
			NSGAIIBuilder<FirstStageAresSolution> nsgaBuilder = 
					new NSGAIIBuilder<FirstStageAresSolution>(problem, crossoverOperator, mutationOperator);
			
			nsgaBuilder.setSelectionOperator(selectionMethod);
			nsgaBuilder.setSolutionListEvaluator(mtSolEvaluator);
			
			nsgaBuilder.setMaxEvaluations(50);
			nsgaBuilder.setPopulationSize(50);
			
			algorithm = nsgaBuilder.build();
			AlgorithmRunner nsgaRunner = new AlgorithmRunner.Executor(algorithm).execute();
			
			long executionTime = nsgaRunner.getComputingTime();
			
			population = algorithm.getResult();
			
			if(population != null)
				Collections.sort(population, new RankingAndCrowdingDistanceComparator<>());
			
			FirstStageAresSolution singleSolution = selectSingleSolution(population);
			
			if(singleSolution!=null) 
			{
				applySolutionToInfrastructure(singleSolution, inf);
				return true;
			}
					
			return true; 
		}
		catch(Throwable t)
		{
			System.err.println("OH NOOOOOOOOOOO!");
		}
		return false;
	}

	private void applySolutionToInfrastructure(FirstStageAresSolution solution, MobileCloudInfrastructure inf) {
		Coordinates edgeNodeCoordinates = null;
		double size_x = SimulationSetup.x_max/MAP_M;
		double size_y = SimulationSetup.y_max/(MAP_N*2);
		for(int k = 0; k < SimulationSetup.MAP_M * SimulationSetup.MAP_N; k++)
			if(solution.getVariableValue(k))
			{
				int i = k / SimulationSetup.MAP_N;
				int j = k % SimulationSetup.MAP_N;
				double x = i*size_x + size_x/2.0;
				double y = j*size_y + size_y/2.0;
				edgeNodeCoordinates = new Coordinates(x,y);
				EdgeNode edge = new EdgeNode("edge("+i+","+j+")", defaultHardwareCapabilities.clone(), defaultEdgePricingModel);
				edge.setCoords(edgeNodeCoordinates);
				edge.setCPUEnergyModel(defaultCPUEnergyModel);
				//edge.setAvailabilityModel(model);
				inf.addEdgeNode(edge);
			}
	}

	private FirstStageAresSolution selectSingleSolution(List<FirstStageAresSolution> population) {
		if(population != null)
			if(population.size() > 0)
				return population.get(0);
		return null;
	}
}
