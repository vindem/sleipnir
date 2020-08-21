package at.ac.tuwien.ec.model.infrastructure.provisioning.triobj;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.qualityindicator.QualityIndicator;
import org.uma.jmetal.qualityindicator.impl.Hypervolume;
import org.uma.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import org.uma.jmetal.qualityindicator.impl.hypervolume.WFGHypervolume;
import org.uma.jmetal.qualityindicator.impl.hypervolume.util.WfgHypervolumeFront;
import org.uma.jmetal.qualityindicator.impl.hypervolume.util.WfgHypervolumeVersion;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.comparator.RankingComparator;
import org.uma.jmetal.util.evaluator.impl.MultithreadedSolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import org.uma.jmetal.util.experiment.component.GenerateReferenceParetoSetAndFrontFromDoubleSolutions;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
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

public class TriobjPlanner extends EdgePlanner{
	
	private TriobjProblem problem;
	private double crossoverProbability;
	private double mutationProbability;
	private BinaryTournamentSelection<TriobjSolution> selectionMethod;
	private TriobjMutationOperator mutationOperator;
	private TriobjCrossoverOperator crossoverOperator;
	private SequentialSolutionListEvaluator<TriobjSolution> mtSolEvaluator;
	ArrayList<Algorithm<List<TriobjSolution>>> algorithms = new ArrayList<Algorithm<List<TriobjSolution>>>(); 	
	Algorithm<List<TriobjSolution>> algorithm;

	public TriobjPlanner()
	{
		super();
		this.problem = new TriobjProblem();
		this.crossoverProbability = 0.7;
		this.mutationProbability = 1.0 / problem.getNumberOfVariables() ;
		crossoverOperator = new TriobjCrossoverOperator(crossoverProbability);
		mutationOperator = new TriobjMutationOperator(mutationProbability);
		selectionMethod = new BinaryTournamentSelection<TriobjSolution>(
				new RankingAndCrowdingDistanceComparator<TriobjSolution>());
		mtSolEvaluator = 
				new SequentialSolutionListEvaluator<TriobjSolution>();
		
	}
	
		
	public List<TriobjSolution> getSolutionList()
	{
		List<TriobjSolution> population = new ArrayList<TriobjSolution>();
		try
		{
			NSGAIIBuilder<TriobjSolution> nsgaBuilder = 
					new NSGAIIBuilder<TriobjSolution>(this.problem, this.crossoverOperator, this.mutationOperator);
			//int[] iterations = {10,50,100,150,200,250,300,350,400,450,500,550,600,650,700 };
			int[] iterations = {300};
			nsgaBuilder.setSelectionOperator(this.selectionMethod);
			nsgaBuilder.setSolutionListEvaluator(this.mtSolEvaluator);
			
			//nsgaBuilder.setMaxEvaluations(50);
			nsgaBuilder.setPopulationSize(30);
			
			algorithm = nsgaBuilder.build();
			//AlgorithmRunner nsgaRunner = new AlgorithmRunner.Executor(algorithm).execute();
			AlgorithmRunner nsgaRunner;
			
			Point refPoint = new Point() {
				double latency,energy;
				private double failureProb;
								
				@Override
				public int getDimension() {
					// TODO Auto-generated method stub
					return 3;
				}

				@Override
				public double[] getValues() {
					// TODO Auto-generated method stub
					double[] values = new double[3];
					values[0] = latency;
					values[1] = energy;
					values[2] = failureProb;
					return values;
				}

				@Override
				public double getValue(int index) {
					switch(index)
					{
						case 0: return latency;
						case 1: return energy;
						case 2: return failureProb;
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
					case 2: this.failureProb = value;
							break;
					}
				}

				@Override
				public void update(double[] point) {
					this.latency = point[0];
					this.energy = point[1];
					this.failureProb = point[2];
				}
			};
			double[] point = { 10.0 , 1e11, 1.0 };
			refPoint.update(point);
			PISAHypervolume<TriobjSolution> HV = new PISAHypervolume<TriobjSolution>();
			
			double maxHypervolume = Double.MIN_VALUE;
			List<TriobjSolution> bestPopulation = null;
			for(int i = 0; i < iterations.length; i++) {
				nsgaBuilder.setMaxEvaluations(iterations[i]);
				algorithms.add(nsgaBuilder.build());
				nsgaRunner = new AlgorithmRunner.Executor(algorithms.get(i)).execute();
				population = algorithms.get(i).getResult();
				System.out.println("Obtained solution for " + iterations[i] + " iterations, calculating hypervolume");
				//double hypervolume = HV.hypervolume(population, refPoint);
				//if(hypervolume > maxHypervolume)
				//{
					//maxHypervolume = hypervolume;
					//bestPopulation = population;
				//}
				long executionTime = nsgaRunner.getComputingTime();
				//System.out.println("Hypervolume: " +hypervolume + " Runtime: " + executionTime + " milliseconds");
			}
												
			if(population != null)
				Collections.sort(population, new RankingAndCrowdingDistanceComparator<>());
			
			return population;
			 
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
		return population;
	}
	
	public boolean setupEdgeNodes(MobileDataDistributionInfrastructure inf)
	{
		List<TriobjSolution> population = new ArrayList<TriobjSolution>();
		try
		{
			NSGAIIBuilder<TriobjSolution> nsgaBuilder = 
					new NSGAIIBuilder<TriobjSolution>(problem, crossoverOperator, mutationOperator);
			
			nsgaBuilder.setSelectionOperator(selectionMethod);
			nsgaBuilder.setSolutionListEvaluator(mtSolEvaluator);
			
			nsgaBuilder.setMaxEvaluations(10);
			nsgaBuilder.setPopulationSize(100);
			
			algorithm = nsgaBuilder.build();
			AlgorithmRunner nsgaRunner = new AlgorithmRunner.Executor(algorithm).execute();
			
			long executionTime = nsgaRunner.getComputingTime();
			
			population = algorithm.getResult();
			
			if(population != null)
				Collections.sort(population, new RankingAndCrowdingDistanceComparator<>());
			
			TriobjSolution singleSolution = selectSingleSolution(population);
			
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

	public void applySolutionToInfrastructure(TriobjSolution solution, MobileDataDistributionInfrastructure inf) {
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
	
	private TriobjSolution selectSingleSolution(List<TriobjSolution> population) {
		if(population != null)
			if(population.size() > 0)
				return population.get(0);
		return null;
	}
}
