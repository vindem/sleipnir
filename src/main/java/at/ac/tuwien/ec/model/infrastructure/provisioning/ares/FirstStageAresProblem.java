package at.ac.tuwien.ec.model.infrastructure.provisioning.ares;

import org.apache.commons.lang.math.RandomUtils;
import org.uma.jmetal.problem.ConstrainedProblem;
import org.uma.jmetal.util.solutionattribute.impl.NumberOfViolatedConstraints;
import org.uma.jmetal.util.solutionattribute.impl.OverallConstraintViolation;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.provisioning.DefaultNetworkPlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.mo.EdgePlanningSolution;
import at.ac.tuwien.ec.model.pricing.EdgePricingModel;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class FirstStageAresProblem implements ConstrainedProblem<FirstStageAresSolution> {
	
	protected static int MAP_M = SimulationSetup.MAP_M;
	protected static int MAP_N = SimulationSetup.MAP_N;
	protected static HardwareCapabilities defaultHardwareCapabilities = SimulationSetup.defaultEdgeNodeCapabilities.clone();
	protected static EdgePricingModel defaultEdgePricingModel = SimulationSetup.edgePricingModel;
	protected static CPUEnergyModel defaultCPUEnergyModel = SimulationSetup.edgeCPUEnergyModel;

	/**
	 * 
	 */
	private static final long serialVersionUID = 8457768991238369613L;
	private MobileCloudInfrastructure infrastructure;
	public OverallConstraintViolation<EdgePlanningSolution> overallConstraintViolationDegree = 
			new OverallConstraintViolation<EdgePlanningSolution>();
	public NumberOfViolatedConstraints<EdgePlanningSolution> numberOfViolatedConstraints =
			new NumberOfViolatedConstraints<EdgePlanningSolution>();
	
	public FirstStageAresProblem(MobileCloudInfrastructure inf)
	{
		this.infrastructure = inf;
	}
	
	@Override
	public int getNumberOfVariables() {
		// TODO Auto-generated method stub
		return SimulationSetup.MAP_M * SimulationSetup.MAP_N;
	}

	@Override
	public int getNumberOfObjectives() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "ARES First Stage";
	}

	@Override
	public void evaluate(FirstStageAresSolution solution) {
		MobileCloudInfrastructure infrastructure = solution.getInfrastructure();
		DefaultNetworkPlanner.setupNetworkConnections(infrastructure);
		
		double averageDistance, averageEnergy;
		averageDistance = computeAverageDistance(infrastructure);
		averageEnergy = computeEnergyConsumption(infrastructure);
		solution.setObjective(0, averageDistance);
		solution.setObjective(1, averageEnergy);
	}

	private double computeEnergyConsumption(MobileCloudInfrastructure infrastructure) {
		double maxTransmissionEnergy = Double.MIN_VALUE;
		MobileSoftwareComponent msc = new MobileSoftwareComponent("dummy", new Hardware(1,1,1),
				1, "anyone", 1, 1);
		for(MobileDevice dev : infrastructure.getMobileDevices().values())
		{
			double maxTransmissionDevEdge = Double.MIN_VALUE;
			double minHops = Integer.MAX_VALUE;
			
			for(EdgeNode edge : infrastructure.getEdgeNodes().values()) 
			{
				double currHops = infrastructure.getDistanceBetweenNodes(dev, edge);
				if(currHops < minHops)
				{
					minHops = currHops;
					maxTransmissionDevEdge = dev.getNetEnergyModel().computeNETEnergy(msc, edge, infrastructure);
				}
				if(currHops == minHops) 
				{
					double currDevEdgeEnergy = dev.getNetEnergyModel().computeNETEnergy(msc, edge, infrastructure);
					if(currDevEdgeEnergy > maxTransmissionDevEdge)
						maxTransmissionDevEdge = currDevEdgeEnergy;
				}
			}
			maxTransmissionEnergy += maxTransmissionDevEdge;
		}
		double instIdlePower = 0;
		for(EdgeNode edge : infrastructure.getEdgeNodes().values())
			instIdlePower += edge.getCPUEnergyModel().getIdlePower(msc, edge, infrastructure); 
		
		return maxTransmissionEnergy + instIdlePower;
	}

	private double computeAverageDistance(MobileCloudInfrastructure infrastructure) {
		double averageDistance;
		averageDistance = 0;
		MobileSoftwareComponent msc = new MobileSoftwareComponent("dummy", new Hardware(1,1,1),
				1, "anyone", 1, 1);
		for(MobileDevice dev : infrastructure.getMobileDevices().values())
		{
			double minDist = Double.MAX_VALUE;
			for(EdgeNode edge : infrastructure.getEdgeNodes().values()) 
			{
				double currDist = infrastructure.getTransmissionTime(msc, dev, edge);
				if(currDist < minDist)
					minDist = currDist;
			}
			averageDistance += minDist;
		}
		return averageDistance / infrastructure.getMobileDevices().size();
	}

	@Override
	public FirstStageAresSolution createSolution() {
		boolean[][] edgeNodeMap = new boolean[SimulationSetup.MAP_M][SimulationSetup.MAP_N];
		boolean wifi = true;
		for(int i = 0; i < MAP_M; i++)
			for(int j = 0; j < MAP_N; j++)
			{
				Coordinates currentEdgeNodeCoordinates = null;
				boolean val;
				if(val = RandomUtils.nextBoolean())
				{
					if(i % 2 == 0 && j%2 == 0)
						currentEdgeNodeCoordinates =  new Coordinates(i,j);
					if(i%2==1 && j%2==1)
						currentEdgeNodeCoordinates =  new Coordinates(i,j);
					if(currentEdgeNodeCoordinates != null)
					{
						EdgeNode edge = new EdgeNode("edge("+i+","+j+")", defaultHardwareCapabilities.clone(), defaultEdgePricingModel);
						edge.setCoords(currentEdgeNodeCoordinates);
						edge.setCPUEnergyModel(defaultCPUEnergyModel);
						infrastructure.addEdgeNode(edge);
						edgeNodeMap[i][j] = val;
					}
				}
			}
		DefaultNetworkPlanner.setupNetworkConnections(infrastructure);
		return new FirstStageAresSolution(this.infrastructure, edgeNodeMap);
	}

	@Override
	public int getNumberOfConstraints() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void evaluateConstraints(FirstStageAresSolution solution) {
		// TODO Auto-generated method stub
		
	}

}
