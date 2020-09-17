package at.ac.tuwien.ec.model.infrastructure.provisioning.ares;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.AlgorithmBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.qualityindicator.QualityIndicator;
import org.uma.jmetal.qualityindicator.impl.hypervolume.impl.WFGHypervolume;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.comparator.RankingComparator;
import org.uma.jmetal.util.evaluator.impl.MultithreadedSolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.point.Point;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.provisioning.DefaultNetworkPlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.EdgePlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.mo.EdgePlanningCrossoverOperator;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.mo.EdgePlanningMutationOperator;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.mo.EdgePlanningProblem;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.mo.EdgePlanningSolution;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import scala.Tuple2;

public class FirstStageAresPlanner extends EdgePlanner{
	
	private Problem<FirstStageAresSolution> problem;
	private double crossoverProbability;
	private double mutationProbability;
	private BinaryTournamentSelection<FirstStageAresSolution> selectionMethod;
	private MutationOperator<FirstStageAresSolution> mutationOperator;
	private CrossoverOperator<FirstStageAresSolution> crossoverOperator;
	private SequentialSolutionListEvaluator<FirstStageAresSolution> mtSolEvaluator;
	ArrayList<Algorithm<List<FirstStageAresSolution>>> algorithms = new ArrayList<Algorithm<List<FirstStageAresSolution>>>(); 	
	Algorithm<List<FirstStageAresSolution>> algorithm;

	public FirstStageAresPlanner()
	{
		super();
		this.problem = new FirstStageAresProblem();
		this.crossoverProbability = 0.5;
		this.mutationProbability = 1.0 / problem.getNumberOfVariables() ;
		crossoverOperator = new FirstStageAresCrossoverOperator(crossoverProbability);
		mutationOperator = new FirstStageAresMutationOperator(mutationProbability);
		selectionMethod = new BinaryTournamentSelection<FirstStageAresSolution>(
				new RankingAndCrowdingDistanceComparator<FirstStageAresSolution>());
		mtSolEvaluator = 
				new SequentialSolutionListEvaluator<FirstStageAresSolution>();
		
	}
	
		
	public List<FirstStageAresSolution> getSolutionList()
	{
		List<FirstStageAresSolution> population = new ArrayList<FirstStageAresSolution>();
		try
		{
			NSGAIIBuilder<FirstStageAresSolution> nsgaBuilder = 
					new NSGAIIBuilder<FirstStageAresSolution>(this.problem, this.crossoverOperator, this.mutationOperator, 100);
			//int[] iterations = {10,50,100,150,200,250,300,350,400,450,500,550,600,650,700,750,800,850,900,950,1000 };
			int[] iterations = {300};
			nsgaBuilder.setSelectionOperator(this.selectionMethod);
			nsgaBuilder.setSolutionListEvaluator(this.mtSolEvaluator);
			
			//nsgaBuilder.setMaxEvaluations(50);
			//nsgaBuilder.setPopulationSize(100);
			
			algorithm = nsgaBuilder.build();
			//AlgorithmRunner nsgaRunner = new AlgorithmRunner.Executor(algorithm).execute();
			AbstractAlgorithmRunner nsgaRunner;
			
			Point refPoint = new Point() {
				double latency,energy;
								
				@Override
				public int getDimension() {
					// TODO Auto-generated method stub
					return 2;
				}

				@Override
				public double[] getValues() {
					// TODO Auto-generated method stub
					double[] values = new double[2];
					values[0] = latency;
					values[1] = energy;
					return values;
				}

				@Override
				public double getValue(int index) {
					switch(index)
					{
						case 0: return latency;
						case 1: return energy;
					}
					return 0;
				}

				@Override
				public void setValue(int index, double value) {
					switch(index)
					{
					case 0 : this.latency = value;
								break;
					case 1 : this.energy = value;
								break;
					}
				}

				@Override
				public void update(double[] point) {
					this.latency = point[0];
					this.energy = point[1];
				}

				@Override
				public void set(double[] point) {
					// TODO Auto-generated method stub
					
				}
			};
			double[] point = { 1.0 , 1.0 };
			refPoint.update(point);
			WFGHypervolume<FirstStageAresSolution> HV = new WFGHypervolume<FirstStageAresSolution>();
			
			double maxHypervolume = Double.MIN_VALUE;
			List<FirstStageAresSolution> bestPopulation = null;
			for(int i = 0; i < iterations.length; i++) {
				nsgaBuilder.setMaxEvaluations(iterations[i]);
				algorithms.add(nsgaBuilder.build());
				//nsgaRunner = new AlgorithmBuilder.Executor(algorithms.get(i)).execute();
				population = algorithms.get(i).getResult();
				System.out.println("Obtained solution for " + iterations[i] + " iterations, calculating hypervolume");
				//double hypervolume = HV.computeHypervolume(population, refPoint);
				/*double hypervolume = HV.hypervolume();
				if(hypervolume > maxHypervolume)
				{
					maxHypervolume = hypervolume;
					bestPopulation = population;
				}
				long executionTime = nsgaRunner.getComputingTime();
				System.out.println("Hypervolume: " +hypervolume + " Runtime: " + executionTime + " milliseconds");
				*/
			}
												
			if(population != null)
				Collections.sort(bestPopulation, new RankingAndCrowdingDistanceComparator<>());
			
			return bestPopulation;
			 
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
		return population;
	}
	
	public boolean setupEdgeNodes(MobileDataDistributionInfrastructure inf)
	{
		List<FirstStageAresSolution> population = new ArrayList<FirstStageAresSolution>();
		try
		{
			NSGAIIBuilder<FirstStageAresSolution> nsgaBuilder = 
					new NSGAIIBuilder<FirstStageAresSolution>(problem, crossoverOperator, mutationOperator, 100);
			
			nsgaBuilder.setSelectionOperator(selectionMethod);
			nsgaBuilder.setSolutionListEvaluator(mtSolEvaluator);
			
			nsgaBuilder.setMaxEvaluations(10);
			//nsgaBuilder.setPopulationSize(100);
			
			algorithm = nsgaBuilder.build();
			//AlgorithmRunner nsgaRunner = new AlgorithmRunner.Executor(algorithm).execute();
			algorithm.run();
			
			//long executionTime = nsgaRunner.getComputingTime();
			
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
			t.printStackTrace();
		}
		return false;
	}

	public void applySolutionToInfrastructure(FirstStageAresSolution solution, MobileDataDistributionInfrastructure inf) {
		Coordinates edgeNodeCoordinates = null;
		
		inf.removeAllEdgeNodes();
		
		for(int k = 0; k < SimulationSetup.edgeNodeLimit && k < SimulationSetup.admissibleEdgeCoordinates.size(); k++)
			if(solution.getVariableValue(k))
			{
				edgeNodeCoordinates = SimulationSetup.admissibleEdgeCoordinates.get(k);
				//EdgeNode edge = new EdgeNode("edge("+edgeNodeCoordinates.getLatitude()+","+edgeNodeCoordinates.getLongitude()+")", defaultHardwareCapabilities.clone(), defaultEdgePricingModel);
				EdgeNode edge = new EdgeNode("edge_"+k, defaultHardwareCapabilities.clone(), defaultEdgePricingModel);
				edge.setCoords(edgeNodeCoordinates);
				edge.setCPUEnergyModel(defaultCPUEnergyModel);
				//edge.setAvailabilityModel(model);
				inf.addEdgeNode(edge);
			}
		DefaultNetworkPlanner.setupNetworkConnections(inf);
	}
	
	private FirstStageAresSolution selectSingleSolution(List<FirstStageAresSolution> population) {
		if(population != null)
			if(population.size() > 0)
				return population.get(0);
		return null;
	}
}
