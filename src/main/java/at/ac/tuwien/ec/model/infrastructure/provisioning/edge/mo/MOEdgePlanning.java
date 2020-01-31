package at.ac.tuwien.ec.model.infrastructure.provisioning.edge.mo;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
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
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import scala.Tuple2;

public class MOEdgePlanning extends EdgePlanner{

	EdgePlanningProblem problem;
	private final int populationSize = 50;
    Algorithm<List<EdgePlanningSolution>> algorithm;
    CrossoverOperator<EdgePlanningSolution> crossover;
    MutationOperator<EdgePlanningSolution> mutation;
    SelectionOperator<List<EdgePlanningSolution>, EdgePlanningSolution> selection;
	private double crossoverProbability;
	private double mutationProbability;
	
	public MOEdgePlanning(Tuple2<MobileApplication, MobileCloudInfrastructure> arg0)
	{
		super();
		
		this.problem = new EdgePlanningProblem(arg0._1(), arg0._2());
		this.crossoverProbability = 0.9;
		this.mutationProbability = 1.0 / problem.getNumberOfVariables() ;
		crossover = new EdgePlanningCrossoverOperator(crossoverProbability);
		mutation = new EdgePlanningMutationOperator(mutationProbability);
		selection = new BinaryTournamentSelection<EdgePlanningSolution>(new RankingComparator<EdgePlanningSolution>());
		SequentialSolutionListEvaluator<EdgePlanningSolution> mtSolEvaluator = 
				new SequentialSolutionListEvaluator<EdgePlanningSolution>();
	}
	
	public MOEdgePlanning(MobileApplication A, MobileCloudInfrastructure I) {
		super();
		
		this.problem = new EdgePlanningProblem(A, I);
		this.crossoverProbability = 0.9;
		this.mutationProbability = 1.0 / problem.getNumberOfVariables() ;
		crossover = new EdgePlanningCrossoverOperator(crossoverProbability);
		mutation = new EdgePlanningMutationOperator(mutationProbability);
		selection = new BinaryTournamentSelection<EdgePlanningSolution>(new RankingComparator<EdgePlanningSolution>());
		SequentialSolutionListEvaluator<EdgePlanningSolution> mtSolEvaluator = 
				new SequentialSolutionListEvaluator<EdgePlanningSolution>();
	}

	public boolean setupEdgeNodes(MobileCloudInfrastructure inf)
	{
		List<EdgePlanningSolution> population = new ArrayList<EdgePlanningSolution>();
		try
		{
			NSGAIIBuilder<EdgePlanningSolution> nsgaBuilder = new NSGAIIBuilder<EdgePlanningSolution>(problem, crossover, mutation);
			nsgaBuilder.setMaxEvaluations(100);
			nsgaBuilder.setPopulationSize(populationSize);
			algorithm = nsgaBuilder.build();
			AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
			
			population = algorithm.getResult() ;
			//System.out.println("No population: " + population.isEmpty());
			if(population!=null)
				Collections.sort(population, new RankingAndCrowdingDistanceComparator<>());
			
			EdgePlanningSolution solution = selectSingleSolution(population);
			if(solution!=null) 
			{
				applySolutionToInfrastructure(solution, inf);
				return true;
			}
			return false;
		}
		catch(Throwable T){
			System.err.println("Selection Error");
			/*population = algorithm.getResult() ;
			for(int i = 0; i < population.size(); i++)
			{
				if(!deployments.contains(population.get(i).getDeployment())
						&& problem.numberOfViolatedConstraints.getAttribute(population.get(i)) == 0)
					deployments.add(population.get(i).getDeployment());
			}
			return deployments;*/
		}
		return false;
	}

	private void applySolutionToInfrastructure(EdgePlanningSolution solution, MobileCloudInfrastructure inf) {
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

	private EdgePlanningSolution selectSingleSolution(List<EdgePlanningSolution> population) {
		if(population != null)
			if(population.size() > 0)
				return population.get(0);
		return null;
	}

	
}
